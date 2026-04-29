package com.back.domain.comment.service;

import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.repository.CommentRepository;
import com.back.domain.flipflop.entity.FlipFlop;
import com.back.domain.flipflop.repository.FlipFlopRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final FlipFlopRepository flipFlopRepository;

    @Transactional
    public Comment createPostComment(Long postId, String content, Member author) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment createFlipFlopComment(Long flipFlopId, String content, Member author) {
        FlipFlop flipFlop = flipFlopRepository.findById(flipFlopId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setFlipFlop(flipFlop);
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Long commentId, Member currentMember) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        if (!comment.getAuthor().getId().equals(currentMember.getId()) && !currentMember.isAdmin()) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
}
