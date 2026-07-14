package com.example.introapi.service;

import com.example.introapi.domain.Intro;
import com.example.introapi.repository.IntroRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업무 규칙(비즈니스 로직)을 담당하는 서비스(Service) 계층입니다.
 * intro-jpa의 IntroService와 완전히 동일합니다.
 *
 * 👉 여기가 프론트/백 분리의 중요한 교훈입니다:
 *    화면 기술이 Thymeleaf에서 React로 통째로 바뀌어도
 *    서비스와 리포지토리는 한 글자도 바뀌지 않습니다.
 *    계층을 나눠 둔 덕분에 "화면 교체"의 영향이 컨트롤러에서 멈춥니다.
 */
@Service
public class IntroService {

    private final IntroRepository introRepository;

    /**
     * 생성자 주입(DI): 스프링이 IntroRepository 빈을 자동으로 넣어줍니다.
     * new IntroRepository() 라고 직접 만들지 않는 것이 핵심입니다.
     */
    public IntroService(IntroRepository introRepository) {
        this.introRepository = introRepository;
    }

    /** [R] 자기소개서 전체 목록을 최신 글이 위로 오도록 조회합니다. */
    public List<Intro> findAll() {
        return introRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** [R] id로 자기소개서 한 건을 조회합니다. 없으면 예외를 던집니다. */
    public Intro findById(Long id) {
        return introRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자기소개서입니다. id=" + id));
    }

    /** [C] 새 자기소개서를 저장합니다. 작성 시각은 서버가 기록합니다. */
    public Intro create(String name, String title, String content) {
        Intro intro = new Intro();
        intro.setName(name);
        intro.setTitle(title);
        intro.setContent(content);
        intro.setCreatedAt(LocalDateTime.now());
        return introRepository.save(intro); // 이 한 줄이 INSERT문으로 번역됩니다.
    }

    /**
     * [U] 자기소개서를 수정합니다.
     *
     * save()를 다시 부르지 않는 것에 주목!
     * @Transactional 안에서 조회한 엔티티는 JPA가 감시하고 있다가,
     * 값이 바뀐 채로 메서드가 끝나면 UPDATE문을 자동으로 실행합니다.
     * 이것을 "변경 감지(Dirty Checking)"라고 부릅니다.
     */
    @Transactional
    public Intro update(Long id, String name, String title, String content) {
        Intro intro = findById(id);   // 1. 조회하고
        intro.setName(name);          // 2. 값만 바꾸면
        intro.setTitle(title);
        intro.setContent(content);    // 3. 메서드가 끝날 때 UPDATE가 나갑니다.
        return intro;                 // 수정된 결과를 API 응답으로 돌려주기 위해 반환합니다.
    }

    /** [D] id로 자기소개서를 삭제합니다. (없는 id면 예외 → 404 응답) */
    public void delete(Long id) {
        Intro intro = findById(id);           // 존재하는지 먼저 확인하고
        introRepository.delete(intro);        // 이 한 줄이 DELETE문으로 번역됩니다.
    }
}
