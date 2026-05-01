package com.back.domain.aidraft.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_flipflop_draft")
@Getter
@Setter
@NoArgsConstructor
public class AiFlipFlopDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String politicianName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String beforeStatement;

    @Column(nullable = false, length = 500)
    private String beforeSource;

    @Column(nullable = false)
    private LocalDateTime beforeDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String afterStatement;

    @Column(nullable = false, length = 500)
    private String afterSource;

    @Column(nullable = false)
    private LocalDateTime afterDate;

    @Column(length = 500)
    private String newsUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DraftStatus status = DraftStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DraftStatus {
        PENDING, APPROVED, REJECTED
    }
}
