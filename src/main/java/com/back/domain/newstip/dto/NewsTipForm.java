package com.back.domain.newstip.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsTipForm {

    @NotBlank(message = "정치인 이름을 입력하세요.")
    @Size(max = 100, message = "정치인 이름은 100자 이내로 입력하세요.")
    private String politicianName;

    @NotBlank(message = "뉴스 URL을 입력하세요.")
    @Size(max = 500, message = "URL은 500자 이내로 입력하세요.")
    private String newsUrl;

    @NotBlank(message = "제보 내용을 입력하세요.")
    private String description;
}
