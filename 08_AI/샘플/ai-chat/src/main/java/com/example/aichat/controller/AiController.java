package com.example.aichat.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aichat.dto.ChatReply;
import com.example.aichat.dto.ChatRequest;
import com.example.aichat.dto.IntroAnalysis;
import com.example.aichat.dto.IntroAnalyzeRequest;
import com.example.aichat.service.AiService;

import jakarta.validation.Valid;

/**
 * 브라우저(static/index.html)가 호출하는 REST API.
 * 브라우저는 AI 서버를 직접 부르지 않고 항상 "우리 서버"를 거칩니다 → API 키가 브라우저로 새지 않음.
 */
@RestController
@RequestMapping("/api")
public class AiController {

	private final AiService aiService;

	public AiController(AiService aiService) {
		this.aiService = aiService;
	}

	/** POST /api/chat  {"message": "..."} → {"answer": "...", "model": "...", ...} */
	@PostMapping("/chat")
	public ChatReply chat(@Valid @RequestBody ChatRequest request) {
		return aiService.chat(request.message());
	}

	/** POST /api/intro/analyze  {"text": "..."} → {"summary": "...", "keywords": [...], "field": "..."} */
	@PostMapping("/intro/analyze")
	public IntroAnalysis analyze(@Valid @RequestBody IntroAnalyzeRequest request) {
		return aiService.analyzeIntro(request.text());
	}
}
