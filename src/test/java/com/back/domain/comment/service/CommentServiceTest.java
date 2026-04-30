package com.back.domain.comment.service;

import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.repository.CommentRepository;
import com.back.domain.flipflop.entity.FlipFlop;
import com.back.domain.flipflop.repository.FlipFlopRepository;
import com.back.domain.member.entity.Member;
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

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock FlipFlopRepository flipFlopRepository;

    @InjectMocks CommentService commentService;

    private Member author;
    private Member anotherMember;
    private Member adminMember;
    private Post post;
    private FlipFlop flipFlop;

    @BeforeEach
    void setUp() throws Exception {
        author = memberWithId(1L, "author", false);
        anotherMember = memberWithId(2L, "other", false);
        adminMember = memberWithId(3L, "admin", true);

        post = new Post();
        post.setTitle("게시글");
        post.setContent("내용");
        setId(post, 100L);

        flipFlop = new FlipFlop();
        setId(flipFlop, 200L);
    }

    @Nested
    @DisplayName("게시글 댓글 생성")
    class CreatePostComment {

        @Test
        @DisplayName("존재하는 게시글에 댓글이 생성된다")
        void createPostComment_success() {
            given(postRepository.findById(100L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

            Comment comment = commentService.createPostComment(100L, "댓글 내용", author);

            assertThat(comment.getContent()).isEqualTo("댓글 내용");
            assertThat(comment.getAuthor()).isEqualTo(author);
            assertThat(comment.getPost()).isEqualTo(post);
            assertThat(comment.getFlipFlop()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 댓글 생성 시 예외가 발생한다")
        void createPostComment_postNotFound() {
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createPostComment(999L, "댓글", author))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글이 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("팩트로그 댓글 생성")
    class CreateFlipFlopComment {

        @Test
        @DisplayName("존재하는 팩트로그에 댓글이 생성된다")
        void createFlipFlopComment_success() {
            given(flipFlopRepository.findById(200L)).willReturn(Optional.of(flipFlop));
            given(commentRepository.save(any(Comment.class))).willAnswer(inv -> inv.getArgument(0));

            Comment comment = commentService.createFlipFlopComment(200L, "팩트로그 댓글", author);

            assertThat(comment.getContent()).isEqualTo("팩트로그 댓글");
            assertThat(comment.getFlipFlop()).isEqualTo(flipFlop);
            assertThat(comment.getPost()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 팩트로그에 댓글 생성 시 예외가 발생한다")
        void createFlipFlopComment_flipFlopNotFound() {
            given(flipFlopRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createFlipFlopComment(999L, "댓글", author))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글이 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class Delete {

        private Comment comment;

        @BeforeEach
        void setUp() throws Exception {
            comment = new Comment();
            comment.setContent("댓글");
            comment.setAuthor(author);
            comment.setPost(post);
            setId(comment, 50L);
        }

        @Test
        @DisplayName("작성자는 본인 댓글을 삭제할 수 있다")
        void delete_byAuthor_succeeds() {
            given(commentRepository.findById(50L)).willReturn(Optional.of(comment));

            commentService.delete(50L, author);

            then(commentRepository).should().delete(comment);
        }

        @Test
        @DisplayName("관리자는 다른 사람의 댓글을 삭제할 수 있다")
        void delete_byAdmin_succeeds() {
            given(commentRepository.findById(50L)).willReturn(Optional.of(comment));

            commentService.delete(50L, adminMember);

            then(commentRepository).should().delete(comment);
        }

        @Test
        @DisplayName("권한 없는 사용자가 삭제하면 예외가 발생한다")
        void delete_byNonAuthorNonAdmin_throwsException() {
            given(commentRepository.findById(50L)).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.delete(50L, anotherMember))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("삭제 권한이 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시 예외가 발생한다")
        void delete_commentNotFound() {
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.delete(999L, author))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글이 존재하지 않습니다.");
        }
    }

    // helpers

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
