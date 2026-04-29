package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "닉네임을 입력하세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
    private String nickname;
}
