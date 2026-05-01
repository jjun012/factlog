package com.back.domain.member.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetTokenTest {

    private Member dummyMember() {
        return new Member("user", "pass", "user@example.com", "유저");
    }

    @Test
    @DisplayName("생성 직후 토큰은 만료되지 않는다")
    void newToken_isNotExpired() {
        PasswordResetToken token = new PasswordResetToken(dummyMember());

        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("생성 직후 토큰은 사용되지 않은 상태다")
    void newToken_isNotUsed() {
        PasswordResetToken token = new PasswordResetToken(dummyMember());

        assertThat(token.isUsed()).isFalse();
    }

    @Test
    @DisplayName("만료 시각을 과거로 설정하면 만료 상태가 된다")
    void token_withPastExpiry_isExpired() throws Exception {
        PasswordResetToken token = new PasswordResetToken(dummyMember());
        Field expiresAtField = PasswordResetToken.class.getDeclaredField("expiresAt");
        expiresAtField.setAccessible(true);
        expiresAtField.set(token, LocalDateTime.now().minusSeconds(1));

        assertThat(token.isExpired()).isTrue();
    }

    @Test
    @DisplayName("markUsed() 호출 후 토큰은 사용된 상태가 된다")
    void markUsed_setsUsedTrue() {
        PasswordResetToken token = new PasswordResetToken(dummyMember());
        token.markUsed();

        assertThat(token.isUsed()).isTrue();
    }

    @Test
    @DisplayName("토큰 값은 UUID 형식이다")
    void token_valueIsUuidFormat() {
        PasswordResetToken token = new PasswordResetToken(dummyMember());

        assertThat(token.getToken()).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        );
    }

    @Test
    @DisplayName("두 토큰의 값은 서로 다르다")
    void twoTokens_haveDifferentValues() {
        PasswordResetToken token1 = new PasswordResetToken(dummyMember());
        PasswordResetToken token2 = new PasswordResetToken(dummyMember());

        assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
    }
}
