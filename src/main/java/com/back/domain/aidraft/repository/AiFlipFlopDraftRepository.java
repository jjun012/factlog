package com.back.domain.aidraft.repository;

import com.back.domain.aidraft.entity.AiFlipFlopDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiFlipFlopDraftRepository extends JpaRepository<AiFlipFlopDraft, Long> {
    List<AiFlipFlopDraft> findAllByStatusOrderByCreatedAtDesc(AiFlipFlopDraft.DraftStatus status);
    List<AiFlipFlopDraft> findAllByOrderByCreatedAtDesc();
    boolean existsByNewsUrl(String newsUrl);
}
