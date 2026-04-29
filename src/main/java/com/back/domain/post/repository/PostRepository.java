package com.back.domain.post.repository;

import com.back.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
            String title, String content, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE SIZE(p.likes) >= 10 ORDER BY SIZE(p.likes) DESC, p.createdAt DESC")
    List<Post> findHotPosts(Pageable pageable);
}
