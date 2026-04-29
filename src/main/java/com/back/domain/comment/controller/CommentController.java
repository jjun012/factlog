package com.back.domain.comment.controller;

import com.back.domain.comment.service.CommentService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberService memberService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}")
    public String createPostComment(@PathVariable Long postId,
                                    @RequestParam String content,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("commentError", "댓글 내용을 입력하세요.");
            return "redirect:/post/" + postId;
        }
        Member member = memberService.findByUsername(userDetails.getUsername());
        commentService.createPostComment(postId, content.trim(), member);
        return "redirect:/post/" + postId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/flipflop/{flipFlopId}")
    public String createFlipFlopComment(@PathVariable Long flipFlopId,
                                        @RequestParam String content,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("commentError", "댓글 내용을 입력하세요.");
            return "redirect:/flipflop/" + flipFlopId;
        }
        Member member = memberService.findByUsername(userDetails.getUsername());
        commentService.createFlipFlopComment(flipFlopId, content.trim(), member);
        return "redirect:/flipflop/" + flipFlopId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{commentId}/delete")
    public String delete(@PathVariable Long commentId,
                         @RequestParam String redirectUrl,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Member member = memberService.findByUsername(userDetails.getUsername());
        commentService.delete(commentId, member);
        return "redirect:" + redirectUrl;
    }
}
