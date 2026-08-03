package com.example.introtest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.introtest.domain.Intro;
import com.example.introtest.service.IntroService;

/**
 * [3] 컨트롤러 슬라이스 테스트 — @WebMvcTest + MockMvc
 *
 * "웹 계층(컨트롤러)만" 잘라서 테스트합니다. 실제 서버(톰캣)를 띄우지 않고
 * MockMvc 로 가짜 HTTP 요청을 보내, URL 매핑·뷰 이름·리다이렉트·모델을 검증합니다.
 * 서비스는 진짜 대신 가짜(@MockitoBean)로 바꿔, 컨트롤러의 "흐름"에만 집중합니다.
 *
 *  - @WebMvcTest(IntroController.class) : 이 컨트롤러와 웹 계층만 로딩
 *  - MockMvc          : 가짜 요청을 쏘는 도구
 *  - @MockitoBean     : 스프링 컨텍스트 안의 빈을 가짜로 교체
 *                       (스프링부트 4/스프링 6.2+의 표준. 옛 @MockBean 을 대체)
 */
@WebMvcTest(IntroController.class)
class IntroControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private IntroService introService; // 가짜 서비스 (DB로 가지 않음)

	@Test
	@DisplayName("GET / : 목록 화면(list)과 intros 모델을 반환한다")
	void 목록화면() throws Exception {
		given(introService.findAll()).willReturn(List.of());

		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("list"))
				.andExpect(model().attributeExists("intros"));
	}

	@Test
	@DisplayName("POST /intro : 저장 후 목록(/)으로 리다이렉트하고 서비스가 호출된다")
	void 저장_리다이렉트() throws Exception {
		given(introService.create(any(), any(), any())).willReturn(new Intro());

		mockMvc.perform(post("/intro")
						.param("name", "김인턴")
						.param("title", "제목")
						.param("content", "내용"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"));

		// 컨트롤러가 서비스에 올바른 값을 넘겼는지 검증
		verify(introService).create("김인턴", "제목", "내용");
	}

	@Test
	@DisplayName("GET /intro/{id} : 상세 화면(detail)과 intro 모델을 반환한다")
	void 상세화면() throws Exception {
		Intro intro = new Intro();
		intro.setName("김인턴");
		given(introService.findById(1L)).willReturn(intro);

		mockMvc.perform(get("/intro/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("detail"))
				.andExpect(model().attributeExists("intro"));
	}
}
