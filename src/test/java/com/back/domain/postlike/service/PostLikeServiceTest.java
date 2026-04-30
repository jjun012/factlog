package com.back.domain.postlike.service;

import com.back.domain.member.entity.Member;
import com.back.domain.post.entity.Post;
import com.back.domain.postlike.entity.PostLike;
import com.back.domain.postlike.repository.PostLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock PostLikeRepository postLikeRepository;

    @InjectMocks PostLikeService postLikeService;

    private Post post;
    private Member member;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setTitle("제목");
        post.setContent("내용");
        member = new Member("user", "pass", "user@test.com", "유저");
    }

    @Nested
    @DisplayName("좋아요 토글")
    class ToggleLike {

        @Test
        @DisplayName("좋아요가 없으면 생성하고 true를 반환한다")
        void toggleLike_notLiked_createsAndReturnsTrue() {
            given(postLikeRepository.findByPostAndMember(post, member)).willReturn(Optional.empty());

            boolean result = postLikeService.toggleLike(post, member);

            assertThat(result).isTrue();
            then(postLikeRepository).should().save(any(PostLike.class));
        }

        @Test
        @DisplayName("이미 좋아요가 있으면 삭제하고 false를 반환한다")
        void toggleLike_alreadyLiked_deletesAndReturnsFalse() {
            PostLike existing = new PostLike(post, member);
            given(postLikeRepository.findByPostAndMember(post, member)).willReturn(Optional.of(existing));

            boolean result = postLikeService.toggleLike(post, member);

            assertThat(result).isFalse();
            then(postLikeRepository).should().delete(existing);
        }
    }

    @Nested
    @DisplayName("좋아요 여부 확인")
    class IsLiked {

        @Test
        @DisplayName("좋아요를 눌렀으면 true를 반환한다")
        void isLiked_liked_returnsTrue() {
            given(postLikeRepository.existsByPostAndMember(post, member)).willReturn(true);

            assertThat(postLikeService.isLiked(post, member)).isTrue();
        }

        @Test
        @DisplayName("좋아요를 누르지 않았으면 false를 반환한다")
        void isLiked_notLiked_returnsFalse() {
            given(postLikeRepository.existsByPostAndMember(post, member)).willReturn(false);

            assertThat(postLikeService.isLiked(post, member)).isFalse();
        }
    }

    @Nested
    @DisplayName("좋아요 수 조회")
    class CountLikes {

        @Test
        @DisplayName("게시글의 좋아요 수를 반환한다")
        void countLikes_returnsCount() {
            given(postLikeRepository.countByPost(post)).willReturn(7L);

            assertThat(postLikeService.countLikes(post)).isEqualTo(7L);
        }
    }
}
