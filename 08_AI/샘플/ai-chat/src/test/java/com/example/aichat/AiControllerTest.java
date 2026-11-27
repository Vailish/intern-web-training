package com.example.aichat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.aichat.controller.AiController;
import com.example.aichat.dto.ChatReply;
import com.example.aichat.exception.AiUnavailableException;
import com.example.aichat.service.AiService;

/**
 * 컨트롤러만 띄워서 "HTTP 상태코드와 에러 JSON" 을 확인합니다. (AiService 는 가짜로 대체)
 * ⚠️ Boot 4 에서 @WebMvcTest 패키지가 org.springframework.boot.webmvc.test.autoconfigure 로 바뀌었습니다.
 */
@WebMvcTest(AiController.class)
class AiControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	AiService aiService;

	@Test
	void 정상_질문은_200과_답변() throws Exception {
		given(aiService.chat(anyString())).willReturn(new ChatReply("안녕하세요!", "fake-model", 12, 5));

		mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"안녕\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer").value("안녕하세요!"))
				.andExpect(jsonPath("$.totalTokens").value(12));
	}

	@Test
	void 빈_질문은_AI를_부르지_않고_400() throws Exception {
		mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"   \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("질문을 입력하세요."));
	}

	@Test
	void 너무_긴_질문은_400() throws Exception {
		String longText = "가".repeat(501);
		mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"" + longText + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("질문은 500자 이하로 입력하세요."));
	}

	@Test
	void AI_서버가_죽으면_503과_친절한_문장() throws Exception {
		given(aiService.chat(anyString()))
				.willThrow(new AiUnavailableException("AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.", null));

		mockMvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"안녕\"}"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.error").value("AI 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요."));
	}
}
