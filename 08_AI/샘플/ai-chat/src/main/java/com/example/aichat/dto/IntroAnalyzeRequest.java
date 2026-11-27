package com.example.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 자기소개 분석 요청. */
public record IntroAnalyzeRequest(
		@NotBlank(message = "자기소개 내용을 입력하세요.")
		@Size(max = 2000, message = "자기소개는 2000자 이하로 입력하세요.")
		String text) {
}
