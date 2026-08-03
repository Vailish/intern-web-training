package com.example.introtest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Sort;

import com.example.introtest.domain.Intro;

/**
 * [2] 리포지토리 슬라이스 테스트 — @DataJpaTest
 *
 * "JPA/DB 관련 부분만 잘라서(slice)" 테스트합니다. 컨트롤러·서비스는 로딩하지 않아
 * 통합 테스트보다 가볍고, 진짜 SQL이 나가는지까지 확인할 수 있습니다.
 *
 *  - @DataJpaTest : 내장 인메모리 H2를 자동으로 띄우고, JPA 관련 빈만 로딩
 *  - 각 테스트가 끝나면 자동으로 롤백(rollback)되어 DB가 다시 깨끗해집니다.
 *    → 테스트끼리 서로 영향을 주지 않습니다.
 */
@DataJpaTest
class IntroRepositoryTest {

	@Autowired
	private IntroRepository introRepository;

	// 테스트용 Intro 를 간단히 만들어 주는 도우미 메서드
	private Intro makeIntro(String name) {
		Intro intro = new Intro();
		intro.setName(name);
		intro.setTitle(name + "의 제목");
		intro.setContent(name + "의 내용");
		return intro;
	}

	@Test
	@DisplayName("save 후 findById로 다시 조회된다 (id 자동 생성)")
	void save_후_조회() {
		// when
		Intro saved = introRepository.save(makeIntro("김인턴"));

		// then: DB가 id를 자동으로 매겼고, 그 id로 다시 찾을 수 있다
		assertThat(saved.getId()).isNotNull();
		assertThat(introRepository.findById(saved.getId())).isPresent();
	}

	@Test
	@DisplayName("findAll(id 내림차순): 나중에 저장한 글이 위로 온다")
	void findAll_정렬() {
		// given: 두 건 저장 (A 먼저, B 나중)
		introRepository.save(makeIntro("A"));
		introRepository.save(makeIntro("B"));

		// when: 서비스의 목록 조회와 동일한 정렬 조건으로 조회
		List<Intro> list = introRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

		// then: 최신(B)이 맨 앞
		assertThat(list).hasSize(2);
		assertThat(list.get(0).getName()).isEqualTo("B");
		assertThat(list.get(1).getName()).isEqualTo("A");
	}

	@Test
	@DisplayName("deleteById 후에는 조회되지 않는다")
	void 삭제() {
		Intro saved = introRepository.save(makeIntro("삭제대상"));
		Long id = saved.getId();

		introRepository.deleteById(id);

		assertThat(introRepository.findById(id)).isEmpty();
	}
}
