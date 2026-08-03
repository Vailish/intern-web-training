package com.example.introtest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.introtest.domain.Intro;
import com.example.introtest.repository.IntroRepository;

/**
 * [1] 단위 테스트(Unit Test) — Mockito
 *
 * 가장 빠르고 가벼운 테스트입니다. 스프링을 아예 띄우지 않고(=DB도 안 씀),
 * IntroService "혼자만" 검사합니다. 서비스가 의존하는 IntroRepository 는
 * 진짜 대신 "가짜(Mock)"로 바꿔치기해서, 서비스의 로직에만 집중합니다.
 *
 *  - @ExtendWith(MockitoExtension.class) : Mockito 를 켬
 *  - @Mock         : 가짜 객체를 만든다 (여기선 리포지토리)
 *  - @InjectMocks  : 가짜들을 주입해 테스트 대상(서비스)을 만든다
 *  - given(...)    : "이 메서드가 호출되면 이렇게 답해라"라고 가짜의 행동을 정한다
 *  - verify(...)   : "이 메서드가 실제로 호출됐는지" 검증한다
 *
 * 테스트 메서드 이름은 한글로 "무엇을 검증하는가"가 드러나게 지었습니다.
 */
@ExtendWith(MockitoExtension.class)
class IntroServiceTest {

	@Mock
	private IntroRepository introRepository; // 가짜 리포지토리 (DB에 안 감)

	@InjectMocks
	private IntroService introService; // 위 가짜를 주입받은 진짜 서비스

	@Test
	@DisplayName("create: 입력값과 작성시각이 채워지고 save가 호출된다")
	void create_저장한다() {
		// given: save가 불리면 "받은 그 객체를 그대로 돌려준다"고 가짜에게 지시
		given(introRepository.save(any(Intro.class))).willAnswer(inv -> inv.getArgument(0));

		// when: 서비스 실행
		Intro saved = introService.create("김인턴", "제목입니다", "내용입니다");

		// then: 필드가 채워졌고, 작성시각이 서버에서 세팅됐는지 확인
		assertThat(saved.getName()).isEqualTo("김인턴");
		assertThat(saved.getTitle()).isEqualTo("제목입니다");
		assertThat(saved.getCreatedAt()).isNotNull();
		// 리포지토리의 save가 정확히 호출됐는지 검증
		verify(introRepository).save(any(Intro.class));
	}

	@Test
	@DisplayName("findById: 없는 id면 IllegalArgumentException")
	void findById_없으면_예외() {
		// given: 999번을 찾으면 빈 결과(Optional.empty)를 준다고 지시
		given(introRepository.findById(999L)).willReturn(Optional.empty());

		// when & then: 예외가 나는지, 메시지에 안내가 담겼는지 확인
		assertThatThrownBy(() -> introService.findById(999L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("존재하지 않는");
	}

	@Test
	@DisplayName("update: 조회한 엔티티의 값만 바꾼다(변경 감지) — save를 다시 부르지 않는다")
	void update_변경감지() {
		// given: 1번 글이 있다고 가정
		Intro existing = new Intro();
		existing.setName("옛이름");
		given(introRepository.findById(1L)).willReturn(Optional.of(existing));

		// when: 수정
		introService.update(1L, "새이름", "새제목", "새내용");

		// then: 조회한 객체의 값이 바뀌었고
		assertThat(existing.getName()).isEqualTo("새이름");
		assertThat(existing.getContent()).isEqualTo("새내용");
		// JPA 변경 감지(Dirty Checking)라 save()를 다시 호출하지 않는다는 점이 핵심!
		verify(introRepository, never()).save(any());
	}
}
