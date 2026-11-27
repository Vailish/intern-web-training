package com.example.aichat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 하나를 만들어 빈으로 등록합니다.
 *
 * - ChatClient.Builder 는 Spring AI가 자동으로 만들어 줍니다.
 *   (application.properties 의 spring.ai.model.chat 값에 따라 Ollama 또는 Claude 에 연결된 Builder)
 * - 그래서 이 코드는 "어느 회사 모델인지" 전혀 모릅니다. 모델을 바꿔도 이 파일은 그대로입니다.
 *   → 외부API 모듈에서 RestClient 를 직접 만들던 것과 비교해 보세요.
 */
@Configuration
public class AiConfig {

	/** 모든 요청에 공통으로 붙는 "역할 지정" 문장 (system 메시지). */
	static final String DEFAULT_SYSTEM = """
			너는 신입 웹 개발자 교육을 돕는 조수다.
			- 반드시 한국어로, 5문장 이내로 짧게 답한다.
			- 모르는 내용은 지어내지 말고 "확실하지 않습니다"라고 말한다.
			- 비밀번호, API 키, 주민등록번호 같은 민감정보를 요구하거나 출력하지 않는다.
			""";

	@Bean
	public ChatClient chatClient(ChatClient.Builder builder) {
		return builder
				.defaultSystem(DEFAULT_SYSTEM)
				.build();
	}
}
