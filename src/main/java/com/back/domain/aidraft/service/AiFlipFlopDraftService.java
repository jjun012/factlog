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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFlipFlopDraftService {

    private final AiFlipFlopDraftRepository draftRepository;
    private final NewsSearchService newsSearchService;
    private final GeminiService geminiService;
    private final FlipFlopService flipFlopService;

    private final AtomicBoolean scanning = new AtomicBoolean(false);

    public boolean isScanning() {
        return scanning.get();
    }

    // 주요 정치인 전체 자동 분석 (비동기)
    @Async("aiScanExecutor")
    public void generateAllDrafts() {
        if (!scanning.compareAndSet(false, true)) {
            log.warn("이미 AI 스캔이 진행 중입니다.");
            return;
        }
        try {
            log.info("전체 AI 스캔 시작 - 정치인 {}명", NewsSearchService.MAJOR_POLITICIANS.size());
            for (String politician : NewsSearchService.MAJOR_POLITICIANS) {
                doScan(politician);
            }
            log.info("전체 AI 스캔 완료");
        } finally {
            scanning.set(false);
        }
    }

    // 특정 정치인 분석 (비동기)
    @Async("aiScanExecutor")
    public void generateDraftsForPolitician(String politicianName) {
        if (!scanning.compareAndSet(false, true)) {
            log.warn("이미 AI 스캔이 진행 중입니다.");
            return;
        }
        try {
            doScan(politicianName);
        } finally {
            scanning.set(false);
        }
    }

    private void doScan(String politicianName) {
        List<NewsSearchService.NewsArticle> recentArticles = newsSearchService.searchRecentNews(politicianName);
        log.info("최신 뉴스 검색 결과: {}건 - 정치인: {}", recentArticles.size(), politicianName);

        String pastContext = buildPastContext(politicianName);

        for (NewsSearchService.NewsArticle article : recentArticles) {
            String content = newsSearchService.fetchArticleText(article.url());
            if (content.isBlank()) content = article.description();
            if (content.isBlank()) continue;

            if (draftRepository.existsByNewsUrl(article.url())) {
                log.info("중복 기사 스킵: {}", article.url());
                continue;
            }

            geminiService.analyzeFlipFlop(politicianName, article.title(), content, article.url(), pastContext)
                    .ifPresent(result -> {
                        draftRepository.save(toDraft(result));
                        log.info("AI 초안 생성: {}", result.getTitle());
                    });

            // 무료 플랜 분당 15회 제한 대응
            try { Thread.sleep(10_000); } catch (InterruptedException ignored) {}
        }
    }

    private String buildPastContext(String politicianName) {
        List<NewsSearchService.NewsArticle> pastArticles = newsSearchService.searchPastNews(politicianName);
        log.info("과거 뉴스 검색 결과: {}건 - 정치인: {}", pastArticles.size(), politicianName);

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (NewsSearchService.NewsArticle article : pastArticles) {
            if (count >= 5) break;
            String text = article.description().isBlank()
                    ? newsSearchService.fetchArticleText(article.url())
                    : article.description();
            if (text.isBlank()) continue;
            sb.append("제목: ").append(article.title()).append("\n");
            sb.append("URL: ").append(article.url()).append("\n");
            sb.append("내용: ").append(text, 0, Math.min(text.length(), 500)).append("\n\n");
            count++;
        }
        return sb.toString();
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
