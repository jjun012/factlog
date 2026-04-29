package com.back.domain.post.service;

import com.back.domain.member.entity.Member;
import com.back.domain.post.dto.PostForm;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Post create(PostForm form, Member author) {
        Post post = new Post();
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
        post.setAuthor(author);
        return postRepository.save(post);
    }

    @Transactional
    public void update(Long id, PostForm form, Member currentMember) {
        Post post = findById(id);
        if (!post.getAuthor().getId().equals(currentMember.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        post.setTitle(form.getTitle());
        post.setContent(form.getContent());
    }

    @Transactional
    public void delete(Long id, Member currentMember) {
        Post post = findById(id);
        if (!post.getAuthor().getId().equals(currentMember.getId()) && !currentMember.isAdmin()) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        postRepository.delete(post);
    }

    @Transactional
    public Post getDetail(Long id) {
        Post post = findById(id);
        post.incrementViewCount();
        return post;
    }

    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    }

    public Page<Post> getList(int page, String keyword) {
        Pageable pageable = PageRequest.of(page, 15);
        if (StringUtils.hasText(keyword)) {
            return postRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                    keyword, keyword, pageable);
        }
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<Post> getHotPosts() {
        return postRepository.findHotPosts(PageRequest.of(0, 20));
    }
}
