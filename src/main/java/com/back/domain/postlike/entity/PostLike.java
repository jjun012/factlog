package com.back.domain.postlike.entity;

import com.back.domain.member.entity.Member;
import com.back.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "member_id"}))
@Getter
@NoArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PostLike(Post post, Member member) {
        this.post = post;
        this.member = member;
    }
}
