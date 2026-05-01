package com.back.global.ai;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NewsSearchServiceTest {

    @Mock
    RestTemplate restTemplate;

    NewsSearchService newsSearchService;

    @BeforeEach
    void setUp() {
        newsSearchService = new NewsSearchService();
        ReflectionTestUtils.setField(newsSearchService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(newsSearchService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(newsSearchService, "restTemplate", restTemplate);
    }

    @Nested
    @DisplayName("searchRecentNews")
    class SearchRecentNews {

        @Test
        @DisplayName("API 성공 시 기사 목록을 반환한다")
        void success_returnsArticleList() {
            List<Map<String, Object>> items = List.of(
                    Map.of("title", "이재명 발언 기사", "link", "https://news.example.com/1", "description", "기사 설명"),
                    Map.of("title", "이재명 관련 뉴스", "link", "https://news.example.com/2", "description", "뉴스 설명")
            );
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(Map.of("items", items)));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles).hasSize(2);
            assertThat(articles.get(0).title()).isEqualTo("이재명 발언 기사");
            assertThat(articles.get(0).url()).isEqualTo("https://news.example.com/1");
            assertThat(articles.get(0).description()).isEqualTo("기사 설명");
        }

        @Test
        @DisplayName("API 응답 바디가 null이면 빈 목록을 반환한다")
        void nullBody_returnsEmpty() {
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(null));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles).isEmpty();
        }

        @Test
        @DisplayName("items 키가 없으면 빈 목록을 반환한다")
        void missingItems_returnsEmpty() {
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(Map.of()));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles).isEmpty();
        }

        @Test
        @DisplayName("API 예외 발생 시 빈 목록을 반환한다")
        void exception_returnsEmpty() {
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willThrow(new RuntimeException("네트워크 오류"));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles).isEmpty();
        }

        @Test
        @DisplayName("link가 비어있는 항목은 제외된다")
        void blankLink_skipsItem() {
            List<Map<String, Object>> items = List.of(
                    Map.of("title", "빈링크 기사", "link", "", "description", "설명"),
                    Map.of("title", "정상 기사", "link", "https://news.example.com/1", "description", "설명")
            );
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(Map.of("items", items)));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles).hasSize(1);
            assertThat(articles.get(0).url()).isEqualTo("https://news.example.com/1");
        }

        @Test
        @DisplayName("title이 비어있는 항목은 제외된다")
        void blankTitle_skipsItem() {
            List<Map<String, Object>> items = List.of(
                    Map.of("title", "", "link", "https://news.example.com/1", "description", "설명"),
                    Map.of("title", "정상 기사", "link", "https://news.example.com/2", "description", "설명")
            );
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(Map.of("items", items)));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles).hasSize(1);
            assertThat(articles.get(0).title()).isEqualTo("정상 기사");
        }

        @Test
        @DisplayName("제목의 HTML 태그가 제거된다")
        void htmlTagsRemovedFromTitle() {
            List<Map<String, Object>> items = List.of(
                    Map.of("title", "<b>이재명</b> 발언 기사", "link", "https://news.example.com/1", "description", "설명")
            );
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(Map.of("items", items)));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles.get(0).title()).isEqualTo("이재명 발언 기사");
        }

        @Test
        @DisplayName("설명의 HTML 태그도 제거된다")
        void htmlTagsRemovedFromDescription() {
            List<Map<String, Object>> items = List.of(
                    Map.of("title", "기사 제목", "link", "https://news.example.com/1",
                            "description", "<em>핵심</em> 내용 요약")
            );
            given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(Map.of("items", items)));

            List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews("이재명");

            assertThat(articles.get(0).description()).isEqualTo("핵심 내용 요약");
        }
    }

    @Nested
    @DisplayName("MAJOR_POLITICIANS")
    class MajorPoliticians {

        @Test
        @DisplayName("주요 정치인 목록에 10명이 포함된다")
        void listHasTenMembers() {
            assertThat(NewsSearchService.MAJOR_POLITICIANS).hasSize(10);
        }

        @Test
        @DisplayName("주요 정치인 목록에 핵심 인물들이 포함된다")
        void listContainsKeyPoliticians() {
            assertThat(NewsSearchService.MAJOR_POLITICIANS)
                    .contains("이재명", "윤석열", "한동훈", "이준석", "조국");
        }
    }
}
