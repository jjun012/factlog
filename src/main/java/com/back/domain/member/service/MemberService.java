package com.back.domain.member.service;

import com.back.domain.member.dto.JoinRequest;
import com.back.domain.member.dto.PasswordChangeRequest;
import com.back.domain.member.dto.ProfileUpdateRequest;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.PasswordResetToken;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.repository.PasswordResetTokenRepository;
import com.back.global.mail.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void join(JoinRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        Member member = new Member(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                request.getNickname()
        );
        memberRepository.save(member);
    }

    @Transactional
    public void updateNickname(Member member, ProfileUpdateRequest request) {
        String newNickname = request.getNickname().trim();
        if (!member.getNickname().equals(newNickname) && memberRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        member.setNickname(newNickname);
        memberRepository.save(member);
    }

    @Transactional
    public void changePassword(Member member, PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (!request.isNewPasswordMatch()) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        memberRepository.save(member);
    }

    @Transactional
    public void sendPasswordResetEmail(String email, HttpServletRequest servletRequest) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다."));

        tokenRepository.deleteAllByMember(member);

        PasswordResetToken token = new PasswordResetToken(member);
        tokenRepository.save(token);

        String baseUrl = servletRequest.getScheme() + "://" + servletRequest.getServerName()
                + ":" + servletRequest.getServerPort();
        String resetLink = baseUrl + "/member/reset-password?token=" + token.getToken();

        emailService.sendPasswordResetEmail(member.getEmail(), resetLink);
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 링크입니다."));

        if (token.isUsed()) {
            throw new IllegalArgumentException("이미 사용된 링크입니다.");
        }
        if (token.isExpired()) {
            throw new IllegalArgumentException("만료된 링크입니다. 비밀번호 재설정을 다시 요청해주세요.");
        }

        Member member = token.getMember();
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);

        token.markUsed();
        tokenRepository.save(token);
    }

    public boolean isValidResetToken(String tokenValue) {
        return tokenRepository.findByToken(tokenValue)
                .map(t -> !t.isUsed() && !t.isExpired())
                .orElse(false);
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
