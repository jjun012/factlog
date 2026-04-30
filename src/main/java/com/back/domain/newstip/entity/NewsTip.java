package com.back.domain.newstip.entity;

import com.back.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_tip")
@Getter
@Setter
@NoArgsConstructor
public class NewsTip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String politicianName;

    @Column(nullable = false, length = 500)
    private String newsUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id", nullable = false)
    private Member submitter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipStatus status = TipStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TipStatus {
        PENDING, REVIEWED, REJECTED
    }
}
