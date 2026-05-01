package com.back.global.ai;

import com.back.global.ai.dto.GeminiFlipFlopResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    RestTemplate restTemplate;

    GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(new ObjectMapper());
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(geminiService, "restTemplate", restTemplate);
    }

    @Nested
    @DisplayName("analyzeFlipFlop")
    class AnalyzeFlipFlop {

        @Test
        @DisplayName("말 바꾸기가 감지되면 결과를 반환한다")
        void detected_returnsResult() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse(
                            "{\"detected\":true,\"politicianName\":\"이재명\",\"title\":\"법인세 입장 번복\"," +
                            "\"beforeStatement\":\"법인세를 올려야 한다\",\"beforeDate\":\"2020-01-01\"," +
                            "\"beforeSource\":\"조선일보\",\"afterStatement\":\"법인세를 내려야 한다\"," +
                            "\"afterDate\":\"2024-01-01\",\"afterSource\":\"https://news.example.com\"}"
                    )));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "법인세 관련 발언", "기사 내용", "https://news.example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getPoliticianName()).isEqualTo("이재명");
            assertThat(result.get().getTitle()).isEqualTo("법인세 입장 번복");
            assertThat(result.get().getBeforeStatement()).isEqualTo("법인세를 올려야 한다");
            assertThat(result.get().getAfterStatement()).isEqualTo("법인세를 내려야 한다");
            assertThat(result.get().getNewsUrl()).isEqualTo("https://news.example.com");
        }

        @Test
        @DisplayName("detected가 false이면 빈 Optional을 반환한다")
        void notDetected_returnsEmpty() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse("{\"detected\":false}")));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("API 응답 바디가 null이면 빈 Optional을 반환한다")
        void nullBody_returnsEmpty() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(null));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("API 호출 예외 발생 시 빈 Optional을 반환한다")
        void apiException_returnsEmpty() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willThrow(new RuntimeException("API 오류"));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("title이 비어있으면 빈 Optional을 반환한다")
        void blankTitle_returnsEmpty() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse(
                            "{\"detected\":true,\"politicianName\":\"이재명\",\"title\":\"\"," +
                            "\"beforeStatement\":\"과거 발언\",\"beforeDate\":\"2020-01-01\"," +
                            "\"beforeSource\":\"출처\",\"afterStatement\":\"현재 발언\"," +
                            "\"afterDate\":\"2024-01-01\",\"afterSource\":\"https://news.example.com\"}"
                    )));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("beforeStatement가 비어있으면 빈 Optional을 반환한다")
        void blankBeforeStatement_returnsEmpty() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse(
                            "{\"detected\":true,\"politicianName\":\"이재명\",\"title\":\"제목\"," +
                            "\"beforeStatement\":\"\",\"beforeDate\":\"2020-01-01\"," +
                            "\"beforeSource\":\"출처\",\"afterStatement\":\"현재 발언\"," +
                            "\"afterDate\":\"2024-01-01\",\"afterSource\":\"https://news.example.com\"}"
                    )));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("afterStatement가 비어있으면 빈 Optional을 반환한다")
        void blankAfterStatement_returnsEmpty() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse(
                            "{\"detected\":true,\"politicianName\":\"이재명\",\"title\":\"제목\"," +
                            "\"beforeStatement\":\"과거 발언\",\"beforeDate\":\"2020-01-01\"," +
                            "\"beforeSource\":\"출처\",\"afterStatement\":\"\"," +
                            "\"afterDate\":\"2024-01-01\",\"afterSource\":\"https://news.example.com\"}"
                    )));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("마크다운 코드블록이 포함된 응답도 정상 파싱된다")
        void markdownCodeBlock_parsedSuccessfully() {
            String jsonWithMarkdown = "```json\n" +
                    "{\"detected\":true,\"politicianName\":\"이재명\",\"title\":\"입장 번복\"," +
                    "\"beforeStatement\":\"과거 발언\",\"beforeDate\":\"2020-06-15\"," +
                    "\"beforeSource\":\"출처\",\"afterStatement\":\"현재 발언\"," +
                    "\"afterDate\":\"2024-03-01\",\"afterSource\":\"https://news.example.com\"}\n```";
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse(jsonWithMarkdown)));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getTitle()).isEqualTo("입장 번복");
        }

        @Test
        @DisplayName("날짜가 비어있으면 현재 시각으로 대체된다")
        void blankDate_usesCurrentTime() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse(
                            "{\"detected\":true,\"politicianName\":\"이재명\",\"title\":\"입장 번복\"," +
                            "\"beforeStatement\":\"과거 발언\",\"beforeDate\":\"\"," +
                            "\"beforeSource\":\"출처\",\"afterStatement\":\"현재 발언\"," +
                            "\"afterDate\":\"\",\"afterSource\":\"https://news.example.com\"}"
                    )));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getBeforeDate()).isNotNull();
            assertThat(result.get().getAfterDate()).isNotNull();
        }

        @Test
        @DisplayName("날짜 형식이 올바르면 YYYY-MM-DD로 파싱된다")
        void validDate_parsedCorrectly() {
            given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                    .willReturn(ResponseEntity.ok(buildResponse(
                            "{\"detected\":true,\"politicianName\":\"이재명\",\"title\":\"입장 번복\"," +
                            "\"beforeStatement\":\"과거 발언\",\"beforeDate\":\"2019-03-15\"," +
                            "\"beforeSource\":\"출처\",\"afterStatement\":\"현재 발언\"," +
                            "\"afterDate\":\"2024-11-20\",\"afterSource\":\"https://news.example.com\"}"
                    )));

            Optional<GeminiFlipFlopResult> result = geminiService.analyzeFlipFlop(
                    "이재명", "제목", "내용", "https://news.example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getBeforeDate().getYear()).isEqualTo(2019);
            assertThat(result.get().getBeforeDate().getMonthValue()).isEqualTo(3);
            assertThat(result.get().getAfterDate().getYear()).isEqualTo(2024);
        }
    }

    private Map<String, Object> buildResponse(String jsonText) {
        return Map.of("candidates", List.of(
                Map.of("content", Map.of(
                        "parts", List.of(Map.of("text", jsonText))
                ))
        ));
    }
}
