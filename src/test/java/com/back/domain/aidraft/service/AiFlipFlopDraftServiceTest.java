package com.back.domain.aidraft.service;

import com.back.domain.aidraft.entity.AiFlipFlopDraft;
import com.back.domain.aidraft.repository.AiFlipFlopDraftRepository;
import com.back.domain.flipflop.dto.FlipFlopForm;
import com.back.domain.flipflop.entity.FlipFlop;
import com.back.domain.flipflop.service.FlipFlopService;
import com.back.domain.member.entity.Member;
import com.back.global.ai.GeminiService;
import com.back.global.ai.NewsSearchService;
import com.back.global.ai.dto.GeminiFlipFlopResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AiFlipFlopDraftServiceTest {

    @Mock AiFlipFlopDraftRepository draftRepository;
    @Mock NewsSearchService newsSearchService;
    @Mock GeminiService geminiService;
    @Mock FlipFlopService flipFlopService;

    @InjectMocks AiFlipFlopDraftService aiFlipFlopDraftService;

    @Nested
    @DisplayName("generateDraftsForPolitician")
    class GenerateDraftsForPolitician {

        @Test
        @DisplayName("기사가 없으면 초안이 생성되지 않는다")
        void noArticles_createsNoDrafts() {
            given(newsSearchService.searchRecentNews("이재명")).willReturn(List.of());

            int count = aiFlipFlopDraftService.generateDraftsForPolitician("이재명");

            assertThat(count).isZero();
            then(draftRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("기사 본문과 설명이 모두 비어있으면 해당 기사를 스킵한다")
        void blankContentAndDescription_skipsArticle() {
            NewsSearchService.NewsArticle article =
                    new NewsSearchService.NewsArticle("제목", "https://example.com", "");
            given(newsSearchService.searchRecentNews("이재명")).willReturn(List.of(article));
            given(newsSearchService.fetchArticleText("https://example.com")).willReturn("");

            int count = aiFlipFlopDraftService.generateDraftsForPolitician("이재명");

            assertThat(count).isZero();
            then(geminiService).should(never()).analyzeFlipFlop(any(), any(), any(), any());
            then(draftRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 분석된 URL이면 스킵한다")
        void duplicateUrl_skipsArticle() {
            NewsSearchService.NewsArticle article =
                    new NewsSearchService.NewsArticle("제목", "https://example.com", "설명");
            given(newsSearchService.searchRecentNews("이재명")).willReturn(List.of(article));
            given(newsSearchService.fetchArticleText("https://example.com")).willReturn("기사 내용");
            given(draftRepository.existsByNewsUrl("https://example.com")).willReturn(true);

            int count = aiFlipFlopDraftService.generateDraftsForPolitician("이재명");

            assertThat(count).isZero();
            then(geminiService).should(never()).analyzeFlipFlop(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Gemini가 빈 결과를 반환하면 초안이 저장되지 않는다")
        @org.junit.jupiter.api.Timeout(value = 10, unit = TimeUnit.SECONDS)
        void geminiReturnsEmpty_noDraftSaved() {
            NewsSearchService.NewsArticle article =
                    new NewsSearchService.NewsArticle("제목", "https://example.com", "설명");
            given(newsSearchService.searchRecentNews("이재명")).willReturn(List.of(article));
            given(newsSearchService.fetchArticleText("https://example.com")).willReturn("기사 내용");
            given(draftRepository.existsByNewsUrl("https://example.com")).willReturn(false);
            given(geminiService.analyzeFlipFlop(anyString(), anyString(), anyString(), anyString()))
                    .willReturn(Optional.empty());

            aiFlipFlopDraftService.generateDraftsForPolitician("이재명");

            then(draftRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Gemini가 결과를 반환하면 초안이 저장된다")
        @org.junit.jupiter.api.Timeout(value = 10, unit = TimeUnit.SECONDS)
        void geminiReturnsResult_savesDraft() {
            NewsSearchService.NewsArticle article =
                    new NewsSearchService.NewsArticle("이재명 법인세 발언", "https://example.com", "설명");
            given(newsSearchService.searchRecentNews("이재명")).willReturn(List.of(article));
            given(newsSearchService.fetchArticleText("https://example.com")).willReturn("기사 내용");
            given(draftRepository.existsByNewsUrl("https://example.com")).willReturn(false);
            given(geminiService.analyzeFlipFlop("이재명", "이재명 법인세 발언", "기사 내용", "https://example.com"))
                    .willReturn(Optional.of(sampleResult("이재명", "https://example.com")));
            given(draftRepository.save(any(AiFlipFlopDraft.class))).willAnswer(inv -> inv.getArgument(0));

            int count = aiFlipFlopDraftService.generateDraftsForPolitician("이재명");

            assertThat(count).isEqualTo(1);
            then(draftRepository).should().save(any(AiFlipFlopDraft.class));
        }

        @Test
        @DisplayName("기사 본문이 비어있으면 description을 내용으로 사용한다")
        @org.junit.jupiter.api.Timeout(value = 10, unit = TimeUnit.SECONDS)
        void blankContent_usesDescription() {
            NewsSearchService.NewsArticle article =
                    new NewsSearchService.NewsArticle("제목", "https://example.com", "기사 설명으로 대체");
            given(newsSearchService.searchRecentNews("이재명")).willReturn(List.of(article));
            given(newsSearchService.fetchArticleText("https://example.com")).willReturn("");
            given(draftRepository.existsByNewsUrl("https://example.com")).willReturn(false);
            given(geminiService.analyzeFlipFlop("이재명", "제목", "기사 설명으로 대체", "https://example.com"))
                    .willReturn(Optional.empty());

            aiFlipFlopDraftService.generateDraftsForPolitician("이재명");

            then(geminiService).should().analyzeFlipFlop("이재명", "제목", "기사 설명으로 대체", "https://example.com");
        }

        @Test
        @DisplayName("저장된 초안의 필드가 Gemini 결과와 일치한다")
        @org.junit.jupiter.api.Timeout(value = 10, unit = TimeUnit.SECONDS)
        void savedDraft_matchesGeminiResult() {
            NewsSearchService.NewsArticle article =
                    new NewsSearchService.NewsArticle("기사 제목", "https://example.com", "설명");
            GeminiFlipFlopResult result = sampleResult("이재명", "https://example.com");

            given(newsSearchService.searchRecentNews("이재명")).willReturn(List.of(article));
            given(newsSearchService.fetchArticleText("https://example.com")).willReturn("기사 내용");
            given(draftRepository.existsByNewsUrl("https://example.com")).willReturn(false);
            given(geminiService.analyzeFlipFlop(anyString(), anyString(), anyString(), anyString()))
                    .willReturn(Optional.of(result));
            given(draftRepository.save(any(AiFlipFlopDraft.class))).willAnswer(inv -> inv.getArgument(0));

            aiFlipFlopDraftService.generateDraftsForPolitician("이재명");

            then(draftRepository).should().save(argThat(draft ->
                    draft.getPoliticianName().equals("이재명") &&
                    draft.getTitle().equals("법인세 입장 번복") &&
                    draft.getBeforeStatement().equals("법인세를 올려야 한다") &&
                    draft.getAfterStatement().equals("법인세를 내려야 한다") &&
                    draft.getNewsUrl().equals("https://example.com") &&
                    draft.getStatus() == AiFlipFlopDraft.DraftStatus.PENDING
            ));
        }
    }

    @Nested
    @DisplayName("generateAllDrafts")
    class GenerateAllDrafts {

        @Test
        @DisplayName("주요 정치인 전원에 대해 뉴스 검색을 실행한다")
        void runsSearchForAllMajorPoliticians() {
            for (String politician : NewsSearchService.MAJOR_POLITICIANS) {
                given(newsSearchService.searchRecentNews(politician)).willReturn(List.of());
            }

            int count = aiFlipFlopDraftService.generateAllDrafts();

            assertThat(count).isZero();
            for (String politician : NewsSearchService.MAJOR_POLITICIANS) {
                then(newsSearchService).should().searchRecentNews(politician);
            }
        }

        @Test
        @DisplayName("각 정치인의 생성 건수 합계를 반환한다")
        void returnsTotalCountAcrossAllPoliticians() {
            for (String politician : NewsSearchService.MAJOR_POLITICIANS) {
                given(newsSearchService.searchRecentNews(politician)).willReturn(List.of());
            }

            int count = aiFlipFlopDraftService.generateAllDrafts();

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getPendingDrafts / getAllDrafts")
    class GetDrafts {

        @Test
        @DisplayName("getPendingDrafts는 PENDING 상태 초안만 반환한다")
        void getPendingDrafts_returnsPendingOnly() {
            AiFlipFlopDraft pendingDraft = new AiFlipFlopDraft();
            given(draftRepository.findAllByStatusOrderByCreatedAtDesc(AiFlipFlopDraft.DraftStatus.PENDING))
                    .willReturn(List.of(pendingDraft));

            List<AiFlipFlopDraft> result = aiFlipFlopDraftService.getPendingDrafts();

            assertThat(result).containsExactly(pendingDraft);
        }

        @Test
        @DisplayName("getAllDrafts는 모든 초안을 반환한다")
        void getAllDrafts_returnsAll() {
            AiFlipFlopDraft draft1 = new AiFlipFlopDraft();
            AiFlipFlopDraft draft2 = new AiFlipFlopDraft();
            given(draftRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(draft1, draft2));

            List<AiFlipFlopDraft> result = aiFlipFlopDraftService.getAllDrafts();

            assertThat(result).hasSize(2).containsExactly(draft1, draft2);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 초안을 반환한다")
        void found_returnsDraft() {
            AiFlipFlopDraft draft = new AiFlipFlopDraft();
            given(draftRepository.findById(1L)).willReturn(Optional.of(draft));

            AiFlipFlopDraft result = aiFlipFlopDraftService.findById(1L);

            assertThat(result).isEqualTo(draft);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
        void notFound_throwsException() {
            given(draftRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> aiFlipFlopDraftService.findById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("초안을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("초안 상태가 APPROVED로 변경되고 FlipFlop이 생성된다")
        void approve_setsStatusAndCreatesFlipFlop() throws Exception {
            AiFlipFlopDraft draft = new AiFlipFlopDraft();
            setId(draft, 1L);
            FlipFlopForm form = new FlipFlopForm();
            Member admin = new Member();
            FlipFlop flipFlop = new FlipFlop();

            given(draftRepository.findById(1L)).willReturn(Optional.of(draft));
            given(draftRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(flipFlopService.create(form, admin)).willReturn(flipFlop);

            FlipFlop result = aiFlipFlopDraftService.approve(1L, form, admin);

            assertThat(draft.getStatus()).isEqualTo(AiFlipFlopDraft.DraftStatus.APPROVED);
            assertThat(result).isEqualTo(flipFlop);
            then(flipFlopService).should().create(form, admin);
        }

        @Test
        @DisplayName("존재하지 않는 초안을 승인하면 예외가 발생한다")
        void approveNotFound_throwsException() {
            given(draftRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> aiFlipFlopDraftService.approve(999L, new FlipFlopForm(), new Member()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reject")
    class Reject {

        @Test
        @DisplayName("초안 상태가 REJECTED로 변경된다")
        void reject_setsStatusToRejected() throws Exception {
            AiFlipFlopDraft draft = new AiFlipFlopDraft();
            setId(draft, 1L);
            given(draftRepository.findById(1L)).willReturn(Optional.of(draft));
            given(draftRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            aiFlipFlopDraftService.reject(1L);

            assertThat(draft.getStatus()).isEqualTo(AiFlipFlopDraft.DraftStatus.REJECTED);
        }

        @Test
        @DisplayName("존재하지 않는 초안을 거절하면 예외가 발생한다")
        void rejectNotFound_throwsException() {
            given(draftRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> aiFlipFlopDraftService.reject(999L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // helpers

    private GeminiFlipFlopResult sampleResult(String politicianName, String url) {
        GeminiFlipFlopResult result = new GeminiFlipFlopResult();
        result.setPoliticianName(politicianName);
        result.setTitle("법인세 입장 번복");
        result.setBeforeStatement("법인세를 올려야 한다");
        result.setBeforeDate(LocalDateTime.of(2020, 1, 1, 0, 0));
        result.setBeforeSource("조선일보");
        result.setAfterStatement("법인세를 내려야 한다");
        result.setAfterDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        result.setAfterSource(url);
        result.setNewsUrl(url);
        return result;
    }

    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
