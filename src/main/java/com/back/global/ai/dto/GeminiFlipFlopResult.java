package com.back.global.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GeminiFlipFlopResult {
    private String politicianName;
    private String title;
    private String beforeStatement;
    private LocalDateTime beforeDate;
    private String beforeSource;
    private String afterStatement;
    private LocalDateTime afterDate;
    private String afterSource;
    private String newsUrl;
}
