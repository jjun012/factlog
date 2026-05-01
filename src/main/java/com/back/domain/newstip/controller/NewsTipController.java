package com.back.domain.newstip.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.newstip.dto.NewsTipForm;
import com.back.domain.newstip.service.NewsTipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/news-tip")
@RequiredArgsConstructor
public class NewsTipController {

    private final NewsTipService newsTipService;
    private final MemberService memberService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("newsTipForm", new NewsTipForm());
        return "news-tip/form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute NewsTipForm newsTipForm,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "news-tip/form";
        }
        Member member = memberService.findByUsername(userDetails.getUsername());
        newsTipService.submit(newsTipForm, member);
        redirectAttributes.addFlashAttribute("successMessage", "제보가 접수되었습니다. 검토 후 반영하겠습니다.");
        return "redirect:/news-tip/my";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my")
    public String myTips(Model model,
                         @AuthenticationPrincipal UserDetails userDetails) {
        Member member = memberService.findByUsername(userDetails.getUsername());
        model.addAttribute("tips", newsTipService.getMyTips(member));
        return "news-tip/my";
    }
}
