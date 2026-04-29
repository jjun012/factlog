package com.back.domain.flipflop.entity;

import com.back.domain.comment.entity.Comment;
import com.back.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flip_flop")
@Getter
@Setter
@NoArgsConstructor
public class FlipFlop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String politicianName;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String beforeStatement;

    @Column(nullable = false, length = 200)
    private String beforeSource;

    @Column(nullable = false)
    private LocalDateTime beforeDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String afterStatement;

    @Column(nullable = false, length = 200)
    private String afterSource;

    @Column(nullable = false)
    private LocalDateTime afterDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private int viewCount = 0;

    @OneToMany(mappedBy = "flipFlop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public void incrementViewCount() {
        this.viewCount++;
    }
}
