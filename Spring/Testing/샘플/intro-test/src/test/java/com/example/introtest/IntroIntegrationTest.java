package com.example.introtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.example.introtest.domain.Intro;
import com.example.introtest.repository.IntroRepository;

/**
 * [4] 통합 테스트(Integration Test) — @SpringBootTest
 *
 * 가장 무겁지만 가장 "진짜 같은" 테스트입니다. 애플리케이션 전체(컨트롤러 → 서비스 →
 * 리포지토리 → 실제 인메모리 H2)를 모두 띄워, 가짜 없이 전 과정을 그대로 돌립니다.
 *
 *  - @SpringBootTest       : 앱 전체 컨텍스트를 띄움 (이 테스트가 통과하면 설정이 정상)
 *  - @AutoConfigureMockMvc : 실제 톰캣 없이 MockMvc 로 요청을 보냄
 *  - DB는 src/test/resources/application.properties 의 인메모리 H2 사용
 *
 * 슬라이스 테스트(2·3번)로 부분을 빠르게 검증하고, 통합 테스트로 "연결"을 확인하는 것이
 * 일반적인 조합입니다. (통합 테스트만 잔뜩 쓰면 느려집니다 → 테스트 피라미드)
 */
@SpringBootTest
@AutoConfigureMockMvc
class IntroIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private IntroRepository introRepository;

	@Test
	@DisplayName("등록(POST) → DB 저장 확인 → 상세(GET) 조회까지 전 과정이 동작한다")
	void 등록부터_조회까지() throws Exception {
		// 1) 등록 요청 → 목록으로 리다이렉트
		mockMvc.perform(post("/intro")
						.param("name", "김인턴")
						.param("title", "안녕하세요")
						.param("content", "성장하는 개발자입니다"))
				.andExpect(status().is3xxRedirection());

		// 2) 진짜 DB(인메모리 H2)에 실제로 저장됐는지 확인
		assertThat(introRepository.findAll()).hasSize(1);
		Long id = introRepository.findAll().get(0).getId();

		// 3) 상세 화면 요청 → 저장한 내용이 모델에 담겨 나오는지 확인
		mockMvc.perform(get("/intro/" + id))
				.andExpect(status().isOk())
				.andExpect(model().attribute("intro", hasProperty("name", equalTo("김인턴"))));

		// 저장된 엔티티의 필드도 직접 확인
		Intro saved = introRepository.findById(id).orElseThrow();
		assertThat(saved.getTitle()).isEqualTo("안녕하세요");
		assertThat(saved.getCreatedAt()).isNotNull();
	}
}
