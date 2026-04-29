package com.back.domain.flipflop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class FlipFlopForm {

    @NotBlank(message = "정치인 이름을 입력하세요.")
    private String politicianName;

    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotBlank(message = "이전 발언을 입력하세요.")
    private String beforeStatement;

    @NotBlank(message = "이전 발언 출처(뉴스 링크)를 입력하세요.")
    private String beforeSource;

    @NotNull(message = "이전 발언 날짜를 입력하세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime beforeDate;

    @NotBlank(message = "현재 발언을 입력하세요.")
    private String afterStatement;

    @NotBlank(message = "현재 발언 출처(뉴스 링크)를 입력하세요.")
    private String afterSource;

    @NotNull(message = "현재 발언 날짜를 입력하세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime afterDate;
}
