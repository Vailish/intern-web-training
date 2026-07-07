package com.example.intro.service;

import com.example.intro.domain.Intro;
import com.example.intro.repository.IntroRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 업무 규칙(비즈니스 로직)을 담당하는 서비스(Service) 계층입니다.
 * - @Service : "이 클래스를 빈(Bean)으로 등록해 관리해 줘"라고 스프링에게 알리는 표식
 * - 컨트롤러는 요청을 받기만 하고, 실제 일 처리는 여기서 합니다.
 */
@Service
public class IntroService {

    private final IntroRepository introRepository;

    /**
     * 생성자 주입(DI): 스프링이 IntroRepository 빈을 자동으로 넣어줍니다.
     * new IntroRepository() 라고 직접 만들지 않는 것이 핵심입니다. (→ 04 문서 A5)
     */
    public IntroService(IntroRepository introRepository) {
        this.introRepository = introRepository;
    }

    /** 자기소개서 전체 목록을 최신 글이 위로 오도록 조회합니다. */
    public List<Intro> findAll() {
        return introRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /** id로 자기소개서 한 건을 조회합니다. 없으면 예외를 던집니다. */
    public Intro findById(Long id) {
        return introRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자기소개서입니다. id=" + id));
    }

    /** 새 자기소개서를 저장합니다. 작성 시각은 서버가 기록합니다. */
    public Intro create(String name, String title, String content) {
        Intro intro = new Intro();
        intro.setName(name);
        intro.setTitle(title);
        intro.setContent(content);
        intro.setCreatedAt(LocalDateTime.now());
        return introRepository.save(intro); // 이 한 줄이 INSERT문으로 번역됩니다.
    }
}
