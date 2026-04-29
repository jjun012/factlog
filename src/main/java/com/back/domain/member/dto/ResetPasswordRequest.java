package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String newPassword;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String newPasswordConfirm;

    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
