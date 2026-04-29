package com.back.domain.post.controller;

import com.back.domain.comment.entity.Comment;
import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.post.dto.PostForm;
import com.back.domain.post.entity.Post;
import com.back.domain.post.service.PostService;
import com.back.domain.postlike.service.PostLikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;
    private final PostLikeService postLikeService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "") String keyword) {
        Page<Post> postPage = postService.getList(page, keyword);
        model.addAttribute("postPage", postPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "post/list";
    }

    @GetMapping("/hot")
    public String hotList(Model model) {
        List<Post> hotPosts = postService.getHotPosts();
        model.addAttribute("hotPosts", hotPosts);
        return "post/hot";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.getDetail(id);
        model.addAttribute("post", post);
        model.addAttribute("newComment", new Comment());
        if (userDetails != null) {
            Member currentMember = memberService.findByUsername(userDetails.getUsername());
            model.addAttribute("currentMember", currentMember);
            model.addAttribute("isLiked", postLikeService.isLiked(post, currentMember));
        }
        return "post/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "post/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute PostForm postForm,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "post/form";
        }
        Member member = memberService.findByUsername(userDetails.getUsername());
        Post post = postService.create(postForm, member);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 등록되었습니다.");
        return "redirect:/post/" + post.getId();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.findById(id);
        Member currentMember = memberService.findByUsername(userDetails.getUsername());
        if (!post.getAuthor().getId().equals(currentMember.getId())) {
            return "redirect:/post/" + id;
        }
        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());
        model.addAttribute("postForm", form);
        model.addAttribute("postId", id);
        return "post/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute PostForm postForm,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "post/form";
        }
        Member currentMember = memberService.findByUsername(userDetails.getUsername());
        postService.update(id, postForm, currentMember);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 수정되었습니다.");
        return "redirect:/post/" + id;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Member currentMember = memberService.findByUsername(userDetails.getUsername());
        postService.delete(id, currentMember);
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        return "redirect:/post";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.findById(id);
        Member member = memberService.findByUsername(userDetails.getUsername());
        boolean liked = postLikeService.toggleLike(post, member);
        long likeCount = postLikeService.countLikes(post);
        return ResponseEntity.ok(Map.of("liked", liked, "likeCount", likeCount));
    }
}
