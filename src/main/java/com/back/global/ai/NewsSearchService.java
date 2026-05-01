package com.back.global.ai;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NewsSearchService {

    private static final int MAX_ARTICLES = 10;
    private static final int TIMEOUT_MS = 8000;

    // 분석 대상 주요 정치인 목록
    public static final List<String> MAJOR_POLITICIANS = List.of(
            "이재명", "윤석열", "한동훈", "이준석", "조국", "안철수",
            "홍준표", "원희룡", "나경원", "오세훈"
    );

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public record NewsArticle(String title, String url, String description) {}

    public List<NewsArticle> searchRecentNews(String politicianName) {
        return search(politicianName + " 발언", "date", 10);
    }

    public List<NewsArticle> searchPastNews(String politicianName) {
        return search(politicianName + " 과거 발언 입장", "sim", 10);
    }

    private List<NewsArticle> search(String query, String sort, int display) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String apiUrl = "https://openapi.naver.com/v1/search/news.json?query=" + encodedQuery
                + "&display=" + display + "&sort=" + sort;

        List<NewsArticle> articles = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
            if (response.getBody() == null) return articles;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
            if (items == null) return articles;

            for (Map<String, Object> item : items) {
                String title = stripHtml((String) item.getOrDefault("title", ""));
                String link = (String) item.getOrDefault("link", "");
                String description = stripHtml((String) item.getOrDefault("description", ""));
                if (!link.isBlank() && !title.isBlank() && isNewsLink(link)) {
                    articles.add(new NewsArticle(title, link, description));
                }
            }
        } catch (Exception e) {
            log.warn("네이버 뉴스 검색 실패 - 검색어: {}, 오류: {}", query, e.getMessage());
        }
        return articles;
    }

    public String fetchArticleText(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            String[] contentSelectors = {"article", ".article-body", ".article_body", "#articleBodyContents",
                    ".news-article-body", ".story-body", "[itemprop=articleBody]", "main"};

            for (String selector : contentSelectors) {
                Element el = doc.selectFirst(selector);
                if (el != null) {
                    String text = el.text();
                    if (text.length() > 200) {
                        return text.substring(0, Math.min(text.length(), 3000));
                    }
                }
            }

            String bodyText = doc.body().text();
            return bodyText.substring(0, Math.min(bodyText.length(), 3000));

        } catch (Exception e) {
            log.warn("기사 본문 스크래핑 실패 - URL: {}, 오류: {}", url, e.getMessage());
            return "";
        }
    }

    private boolean isNewsLink(String link) {
        // 네이버 제휴 광고성 콘텐츠 제외
        if (link.contains("utm_source=naver") && link.contains("utm_medium=partnership")) {
            return false;
        }
        // 뉴스와 무관한 도메인 제외 (패션, 쇼핑, 블로그 등)
        String[] nonNewsDomains = {"allurekorea.com", "blog.naver.com", "post.naver.com",
                "smartstore.naver.com", "shopping.naver.com"};
        for (String domain : nonNewsDomains) {
            if (link.contains(domain)) {
                return false;
            }
        }
        return true;
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", "").trim();
    }
}
