package com.back.domain.postlike.repository;

import com.back.domain.member.entity.Member;
import com.back.domain.post.entity.Post;
import com.back.domain.postlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndMember(Post post, Member member);
    boolean existsByPostAndMember(Post post, Member member);
    long countByPost(Post post);
}
