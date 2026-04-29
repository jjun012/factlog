package com.back.domain.member.controller;

import com.back.domain.member.dto.*;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /* ===== 로그인 ===== */
    @GetMapping("/login")
    public String loginPage() {
        return "member/login";
    }

    /* ===== 회원가입 ===== */
    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("joinRequest", new JoinRequest());
        return "member/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequest joinRequest,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (bindingResult.hasErrors()) return "member/join";
        if (!joinRequest.isPasswordMatch()) {
            model.addAttribute("passwordError", "비밀번호가 일치하지 않습니다.");
            return "member/join";
        }
        try {
            memberService.join(joinRequest);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/member/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";
        }
    }

    /* ===== 프로필 수정 ===== */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Member member = memberService.findByUsername(userDetails.getUsername());
        model.addAttribute("member", member);
        model.addAttribute("profileRequest", new ProfileUpdateRequest());
        model.addAttribute("passwordRequest", new PasswordChangeRequest());
        return "member/profile";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile/nickname")
    public String updateNickname(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute ProfileUpdateRequest profileRequest,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Member member = memberService.findByUsername(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", member);
            model.addAttribute("passwordRequest", new PasswordChangeRequest());
            model.addAttribute("nicknameError", bindingResult.getFieldError("nickname").getDefaultMessage());
            return "member/profile";
        }
        try {
            memberService.updateNickname(member, profileRequest);
            redirectAttributes.addFlashAttribute("successMessage", "닉네임이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("nicknameError", e.getMessage());
        }
        return "redirect:/member/profile";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/profile/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute PasswordChangeRequest passwordRequest,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        Member member = memberService.findByUsername(userDetails.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", member);
            model.addAttribute("profileRequest", new ProfileUpdateRequest());
            model.addAttribute("passwordFormError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "member/profile";
        }
        if (!passwordRequest.isNewPasswordMatch()) {
            redirectAttributes.addFlashAttribute("passwordFormError", "새 비밀번호가 일치하지 않습니다.");
            return "redirect:/member/profile";
        }
        try {
            memberService.changePassword(member, passwordRequest);
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("passwordFormError", e.getMessage());
        }
        return "redirect:/member/profile";
    }

    /* ===== 비밀번호 찾기 ===== */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "member/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            memberService.sendPasswordResetEmail(email, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "비밀번호 재설정 링크를 " + email + " 로 발송했습니다. 이메일을 확인해주세요.");
            return "redirect:/member/forgot-password";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/member/forgot-password";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/member/forgot-password";
        }
    }

    /* ===== 비밀번호 재설정 ===== */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        if (!memberService.isValidResetToken(token)) {
            model.addAttribute("errorMessage", "유효하지 않거나 만료된 링크입니다. 비밀번호 재설정을 다시 요청해주세요.");
            return "member/reset-password-error";
        }
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken(token);
        model.addAttribute("resetRequest", req);
        return "member/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute ResetPasswordRequest resetRequest,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "member/reset-password";
        }
        if (!resetRequest.isPasswordMatch()) {
            model.addAttribute("errorMessage", "새 비밀번호가 일치하지 않습니다.");
            return "member/reset-password";
        }
        try {
            memberService.resetPassword(resetRequest.getToken(), resetRequest.getNewPassword());
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인하세요.");
            return "redirect:/member/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/reset-password";
        }
    }
}
