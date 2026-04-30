package com.back.global.ai;

import com.back.global.ai.dto.GeminiFlipFlopResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<GeminiFlipFlopResult> analyzeFlipFlop(String politicianName, String title, String content, String url) {
        String prompt = buildPrompt(politicianName, title, content, url);

        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL + apiKey, entity, Map.class);
            if (response.getBody() == null) return Optional.empty();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates == null || candidates.isEmpty()) return Optional.empty();

            @SuppressWarnings("unchecked")
            Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
            String text = (String) parts.get(0).get("text");

            text = text.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode json = objectMapper.readTree(text);

            if (!json.path("detected").asBoolean(false)) {
                return Optional.empty();
            }

            GeminiFlipFlopResult result = new GeminiFlipFlopResult();
            result.setPoliticianName(json.path("politicianName").asText(politicianName));
            result.setTitle(json.path("title").asText(""));
            result.setBeforeStatement(json.path("beforeStatement").asText(""));
            result.setBeforeDate(parseDate(json.path("beforeDate").asText()));
            result.setBeforeSource(json.path("beforeSource").asText(""));
            result.setAfterStatement(json.path("afterStatement").asText(""));
            result.setAfterDate(parseDate(json.path("afterDate").asText()));
            result.setAfterSource(json.path("afterSource").asText(url));
            result.setNewsUrl(url);

            if (result.getBeforeStatement().isBlank() || result.getAfterStatement().isBlank()
                    || result.getTitle().isBlank()) {
                return Optional.empty();
            }

            return Optional.of(result);

        } catch (Exception e) {
            log.warn("Gemini 분석 실패 - URL: {}, 오류: {}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private String buildPrompt(String politicianName, String title, String content, String url) {
        return """
                아래는 한국 정치인 '%s'에 관한 최근 뉴스 기사입니다.

                기사 제목: %s
                기사 URL: %s
                기사 내용:
                %s

                [지시사항]
                1. 이 기사에서 '%s'의 현재 발언 또는 입장을 파악하세요.
                2. 당신이 학습한 데이터를 바탕으로, 이 발언이 그가 과거에 했던 발언과 모순되거나 입장이 바뀐 경우를 찾으세요.
                3. 명확한 말 바꾸기가 있다면 아래 JSON만 출력하세요 (설명 없이, 마크다운 없이):
                {
                  "detected": true,
                  "politicianName": "정치인 이름",
                  "title": "말 바꾸기 요약 제목 (40자 이내)",
                  "beforeStatement": "과거 발언 원문",
                  "beforeDate": "YYYY-MM-DD",
                  "beforeSource": "과거 발언 출처 (매체명 또는 URL)",
                  "afterStatement": "현재 발언 원문 (기사에 나온 그대로)",
                  "afterDate": "YYYY-MM-DD",
                  "afterSource": "현재 발언 출처 URL"
                }

                말 바꾸기가 없거나 확실하지 않으면:
                {"detected": false}
                """.formatted(politicianName, title, url, content, politicianName);
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now();
        try {
            return LocalDate.parse(dateStr.trim().substring(0, 10)).atStartOfDay();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
