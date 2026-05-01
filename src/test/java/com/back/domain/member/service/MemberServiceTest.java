package com.back.domain.member.service;

import com.back.domain.member.dto.JoinRequest;
import com.back.domain.member.dto.PasswordChangeRequest;
import com.back.domain.member.dto.ProfileUpdateRequest;
import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.PasswordResetToken;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.repository.PasswordResetTokenRepository;
import com.back.global.mail.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;

    @InjectMocks MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("testuser", "encodedPassword", "test@example.com", "테스터");
    }

    @Nested
    @DisplayName("회원가입")
    class Join {

        @Test
        @DisplayName("정상 가입 시 회원이 저장된다")
        void join_success() {
            JoinRequest request = joinRequest("newuser", "pass123", "new@example.com", "새유저");
            given(memberRepository.existsByUsername("newuser")).willReturn(false);
            given(memberRepository.existsByEmail("new@example.com")).willReturn(false);
            given(memberRepository.existsByNickname("새유저")).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encoded");

            memberService.join(request);

            then(memberRepository).should().save(any(Member.class));
        }

        @Test
        @DisplayName("중복 아이디로 가입하면 예외가 발생한다")
        void join_duplicateUsername() {
            JoinRequest request = joinRequest("testuser", "pass123", "new@example.com", "새유저");
            given(memberRepository.existsByUsername("testuser")).willReturn(true);

            assertThatThrownBy(() -> memberService.join(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 아이디입니다.");
        }

        @Test
        @DisplayName("중복 이메일로 가입하면 예외가 발생한다")
        void join_duplicateEmail() {
            JoinRequest request = joinRequest("newuser", "pass123", "test@example.com", "새유저");
            given(memberRepository.existsByUsername("newuser")).willReturn(false);
            given(memberRepository.existsByEmail("test@example.com")).willReturn(true);

            assertThatThrownBy(() -> memberService.join(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 이메일입니다.");
        }

        @Test
        @DisplayName("중복 닉네임으로 가입하면 예외가 발생한다")
        void join_duplicateNickname() {
            JoinRequest request = joinRequest("newuser", "pass123", "new@example.com", "테스터");
            given(memberRepository.existsByUsername("newuser")).willReturn(false);
            given(memberRepository.existsByEmail("new@example.com")).willReturn(false);
            given(memberRepository.existsByNickname("테스터")).willReturn(true);

            assertThatThrownBy(() -> memberService.join(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 닉네임입니다.");
        }

        private JoinRequest joinRequest(String username, String password, String email, String nickname) {
            JoinRequest r = new JoinRequest();
            r.setUsername(username);
            r.setPassword(password);
            r.setPasswordConfirm(password);
            r.setEmail(email);
            r.setNickname(nickname);
            return r;
        }
    }

    @Nested
    @DisplayName("닉네임 변경")
    class UpdateNickname {

        @Test
        @DisplayName("다른 닉네임으로 변경하면 저장된다")
        void updateNickname_success() {
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setNickname("새닉네임");
            given(memberRepository.existsByNickname("새닉네임")).willReturn(false);

            memberService.updateNickname(testMember, request);

            assertThat(testMember.getNickname()).isEqualTo("새닉네임");
            then(memberRepository).should().save(testMember);
        }

        @Test
        @DisplayName("현재와 동일한 닉네임으로 변경하면 중복 검사를 건너뛴다")
        void updateNickname_sameNickname_skipsCheck() {
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setNickname("테스터"); // 현재 닉네임과 동일

            memberService.updateNickname(testMember, request);

            then(memberRepository).should(never()).existsByNickname(anyString());
            then(memberRepository).should().save(testMember);
        }

        @Test
        @DisplayName("이미 사용 중인 닉네임으로 변경하면 예외가 발생한다")
        void updateNickname_duplicateNickname() {
            ProfileUpdateRequest request = new ProfileUpdateRequest();
            request.setNickname("다른유저닉네임");
            given(memberRepository.existsByNickname("다른유저닉네임")).willReturn(true);

            assertThatThrownBy(() -> memberService.updateNickname(testMember, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 닉네임입니다.");
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("현재 비밀번호가 맞고 새 비밀번호가 일치하면 변경된다")
        void changePassword_success() {
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("oldPass");
            request.setNewPassword("newPass123");
            request.setNewPasswordConfirm("newPass123");
            given(passwordEncoder.matches("oldPass", "encodedPassword")).willReturn(true);
            given(passwordEncoder.encode("newPass123")).willReturn("encodedNew");

            memberService.changePassword(testMember, request);

            assertThat(testMember.getPassword()).isEqualTo("encodedNew");
        }

        @Test
        @DisplayName("현재 비밀번호가 틀리면 예외가 발생한다")
        void changePassword_wrongCurrentPassword() {
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("wrongPass");
            request.setNewPassword("newPass123");
            request.setNewPasswordConfirm("newPass123");
            given(passwordEncoder.matches("wrongPass", "encodedPassword")).willReturn(false);

            assertThatThrownBy(() -> memberService.changePassword(testMember, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("현재 비밀번호가 올바르지 않습니다.");
        }

        @Test
        @DisplayName("새 비밀번호 확인이 불일치하면 예외가 발생한다")
        void changePassword_newPasswordMismatch() {
            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setCurrentPassword("oldPass");
            request.setNewPassword("newPass123");
            request.setNewPasswordConfirm("differentPass");
            given(passwordEncoder.matches("oldPass", "encodedPassword")).willReturn(true);

            assertThatThrownBy(() -> memberService.changePassword(testMember, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("새 비밀번호가 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정")
    class ResetPassword {

        @Test
        @DisplayName("유효한 토큰으로 비밀번호를 재설정하면 성공한다")
        void resetPassword_success() {
            PasswordResetToken token = new PasswordResetToken(testMember);
            given(tokenRepository.findByToken(token.getToken())).willReturn(Optional.of(token));
            given(passwordEncoder.encode("newPass123")).willReturn("encodedNew");

            memberService.resetPassword(token.getToken(), "newPass123");

            assertThat(testMember.getPassword()).isEqualTo("encodedNew");
            assertThat(token.isUsed()).isTrue();
        }

        @Test
        @DisplayName("이미 사용된 토큰으로 재설정하면 예외가 발생한다")
        void resetPassword_alreadyUsedToken() {
            PasswordResetToken token = new PasswordResetToken(testMember);
            token.markUsed();
            given(tokenRepository.findByToken(token.getToken())).willReturn(Optional.of(token));

            assertThatThrownBy(() -> memberService.resetPassword(token.getToken(), "newPass"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용된 링크입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 토큰으로 재설정하면 예외가 발생한다")
        void resetPassword_invalidToken() {
            given(tokenRepository.findByToken("invalid-token")).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.resetPassword("invalid-token", "newPass"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("유효하지 않은 링크입니다.");
        }

        @Test
        @DisplayName("유효하지 않은 토큰은 isValidResetToken이 false를 반환한다")
        void isValidResetToken_invalidToken_returnsFalse() {
            given(tokenRepository.findByToken("bad-token")).willReturn(Optional.empty());

            assertThat(memberService.isValidResetToken("bad-token")).isFalse();
        }

        @Test
        @DisplayName("사용된 토큰은 isValidResetToken이 false를 반환한다")
        void isValidResetToken_usedToken_returnsFalse() {
            PasswordResetToken token = new PasswordResetToken(testMember);
            token.markUsed();
            given(tokenRepository.findByToken(token.getToken())).willReturn(Optional.of(token));

            assertThat(memberService.isValidResetToken(token.getToken())).isFalse();
        }
    }
}
