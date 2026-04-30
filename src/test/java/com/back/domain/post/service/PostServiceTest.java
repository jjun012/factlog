package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.post.dto.PostForm;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock PostRepository postRepository;

    @InjectMocks PostService postService;

    private Member author;
    private Member anotherMember;
    private Member adminMember;
    private Post post;

    @BeforeEach
    void setUp() throws Exception {
        author = memberWithId(1L, "author", false);
        anotherMember = memberWithId(2L, "other", false);
        adminMember = memberWithId(3L, "admin", true);

        post = new Post();
        post.setTitle("테스트 제목");
        post.setContent("테스트 내용");
        post.setAuthor(author);
        setId(post, 10L);
    }

    @Nested
    @DisplayName("게시글 생성")
    class Create {

        @Test
        @DisplayName("폼 데이터로 게시글이 저장된다")
        void create_savesPost() {
            PostForm form = postForm("제목", "내용");
            given(postRepository.save(any(Post.class))).willAnswer(inv -> inv.getArgument(0));

            Post saved = postService.create(form, author);

            assertThat(saved.getTitle()).isEqualTo("제목");
            assertThat(saved.getContent()).isEqualTo("내용");
            assertThat(saved.getAuthor()).isEqualTo(author);
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class Update {

        @Test
        @DisplayName("작성자는 게시글을 수정할 수 있다")
        void update_byAuthor_succeeds() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            postService.update(10L, postForm("수정 제목", "수정 내용"), author);

            assertThat(post.getTitle()).isEqualTo("수정 제목");
            assertThat(post.getContent()).isEqualTo("수정 내용");
        }

        @Test
        @DisplayName("다른 사용자가 수정하면 예외가 발생한다")
        void update_byNonAuthor_throwsException() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.update(10L, postForm("제목", "내용"), anotherMember))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("수정 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class Delete {

        @Test
        @DisplayName("작성자는 본인 게시글을 삭제할 수 있다")
        void delete_byAuthor_succeeds() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            postService.delete(10L, author);

            then(postRepository).should().delete(post);
        }

        @Test
        @DisplayName("관리자는 다른 사람의 게시글도 삭제할 수 있다")
        void delete_byAdmin_succeeds() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            postService.delete(10L, adminMember);

            then(postRepository).should().delete(post);
        }

        @Test
        @DisplayName("권한 없는 사용자가 삭제하면 예외가 발생한다")
        void delete_byNonAuthorNonAdmin_throwsException() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.delete(10L, anotherMember))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("삭제 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회")
    class GetDetail {

        @Test
        @DisplayName("상세 조회 시 조회수가 1 증가한다")
        void getDetail_incrementsViewCount() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            int before = post.getViewCount();

            postService.getDetail(10L);

            assertThat(post.getViewCount()).isEqualTo(before + 1);
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 예외가 발생한다")
        void getDetail_notFound_throwsException() {
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getDetail(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 게시글입니다.");
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회")
    class GetList {

        @Test
        @DisplayName("키워드 없이 조회하면 전체 목록을 반환한다")
        void getList_noKeyword_returnsAll() {
            Page<Post> page = new PageImpl<>(List.of(post));
            given(postRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).willReturn(page);

            Page<Post> result = postService.getList(0, "");

            assertThat(result.getContent()).containsExactly(post);
            then(postRepository).should(never())
                    .findByTitleContainingOrContentContainingOrderByCreatedAtDesc(any(), any(), any());
        }

        @Test
        @DisplayName("키워드로 조회하면 검색 결과를 반환한다")
        void getList_withKeyword_returnsFiltered() {
            Page<Post> page = new PageImpl<>(List.of(post));
            given(postRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                    eq("테스트"), eq("테스트"), any(Pageable.class))).willReturn(page);

            Page<Post> result = postService.getList(0, "테스트");

            assertThat(result.getContent()).containsExactly(post);
        }
    }

    // helpers

    private PostForm postForm(String title, String content) {
        PostForm f = new PostForm();
        f.setTitle(title);
        f.setContent(content);
        return f;
    }

    private Member memberWithId(Long id, String username, boolean admin) throws Exception {
        Member m = new Member(username, "pass", username + "@test.com", username);
        setId(m, id);
        if (admin) {
            Field roleField = Member.class.getDeclaredField("role");
            roleField.setAccessible(true);
            roleField.set(m, Member.Role.ADMIN);
        }
        return m;
    }

    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
