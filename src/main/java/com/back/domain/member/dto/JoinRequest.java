package com.back.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequest {

    @NotBlank(message = "아이디를 입력하세요.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자 사이여야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력하세요.")
    private String passwordConfirm;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "닉네임을 입력하세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;

    public boolean isPasswordMatch() {
        return password != null && password.equals(passwordConfirm);
    }
}
