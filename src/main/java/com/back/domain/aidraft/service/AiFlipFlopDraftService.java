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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFlipFlopDraftService {

    private final AiFlipFlopDraftRepository draftRepository;
    private final NewsSearchService newsSearchService;
    private final GeminiService geminiService;
    private final FlipFlopService flipFlopService;

    // 주요 정치인 전체 자동 분석
    @Transactional
    public int generateAllDrafts() {
        int total = 0;
        for (String politician : NewsSearchService.MAJOR_POLITICIANS) {
            total += generateDraftsForPolitician(politician);
        }
        return total;
    }

    // 특정 정치인 분석
    @Transactional
    public int generateDraftsForPolitician(String politicianName) {
        List<NewsSearchService.NewsArticle> articles = newsSearchService.searchRecentNews(politicianName);
        log.info("뉴스 검색 결과: {}건 - 정치인: {}", articles.size(), politicianName);

        int created = 0;
        for (NewsSearchService.NewsArticle article : articles) {
            String content = newsSearchService.fetchArticleText(article.url());
            if (content.isBlank()) content = article.description();
            if (content.isBlank()) continue;

            if (draftRepository.existsByNewsUrl(article.url())) {
                log.info("중복 기사 스킵: {}", article.url());
                continue;
            }

            geminiService.analyzeFlipFlop(politicianName, article.title(), content, article.url())
                    .ifPresent(result -> {
                        AiFlipFlopDraft draft = toDraft(result);
                        draftRepository.save(draft);
                        log.info("AI 초안 생성: {}", draft.getTitle());
                    });
            created++;

            // 무료 플랜 분당 15회 제한 대응
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        }
        return created;
    }

    public List<AiFlipFlopDraft> getPendingDrafts() {
        return draftRepository.findAllByStatusOrderByCreatedAtDesc(AiFlipFlopDraft.DraftStatus.PENDING);
    }

    public List<AiFlipFlopDraft> getAllDrafts() {
        return draftRepository.findAllByOrderByCreatedAtDesc();
    }

    public AiFlipFlopDraft findById(Long id) {
        return draftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("초안을 찾을 수 없습니다."));
    }

    @Transactional
    public FlipFlop approve(Long draftId, FlipFlopForm editedForm, Member admin) {
        AiFlipFlopDraft draft = findById(draftId);
        draft.setStatus(AiFlipFlopDraft.DraftStatus.APPROVED);
        draftRepository.save(draft);
        return flipFlopService.create(editedForm, admin);
    }

    @Transactional
    public void reject(Long draftId) {
        AiFlipFlopDraft draft = findById(draftId);
        draft.setStatus(AiFlipFlopDraft.DraftStatus.REJECTED);
        draftRepository.save(draft);
    }

    private AiFlipFlopDraft toDraft(GeminiFlipFlopResult result) {
        AiFlipFlopDraft draft = new AiFlipFlopDraft();
        draft.setPoliticianName(result.getPoliticianName());
        draft.setTitle(result.getTitle());
        draft.setBeforeStatement(result.getBeforeStatement());
        draft.setBeforeDate(result.getBeforeDate());
        draft.setBeforeSource(result.getBeforeSource());
        draft.setAfterStatement(result.getAfterStatement());
        draft.setAfterDate(result.getAfterDate());
        draft.setAfterSource(result.getAfterSource());
        draft.setNewsUrl(result.getNewsUrl());
        return draft;
    }
}
