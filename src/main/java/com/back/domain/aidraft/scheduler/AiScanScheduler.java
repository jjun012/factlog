package com.back.domain.aidraft.scheduler;

import com.back.domain.aidraft.service.AiFlipFlopDraftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiScanScheduler {

    private final AiFlipFlopDraftService draftService;

    // 매일 새벽 3시 자동 분석
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void scheduledScan() {
        log.info("자동 AI 스캔 시작");
        draftService.generateAllDrafts();
    }
}
