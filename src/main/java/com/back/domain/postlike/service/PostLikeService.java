package com.back.domain.postlike.service;

import com.back.domain.member.entity.Member;
import com.back.domain.post.entity.Post;
import com.back.domain.postlike.entity.PostLike;
import com.back.domain.postlike.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;

    @Transactional
    public boolean toggleLike(Post post, Member member) {
        Optional<PostLike> existing = postLikeRepository.findByPostAndMember(post, member);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            return false;
        } else {
            postLikeRepository.save(new PostLike(post, member));
            return true;
        }
    }

    public boolean isLiked(Post post, Member member) {
        return postLikeRepository.existsByPostAndMember(post, member);
    }

    public long countLikes(Post post) {
        return postLikeRepository.countByPost(post);
    }
}
