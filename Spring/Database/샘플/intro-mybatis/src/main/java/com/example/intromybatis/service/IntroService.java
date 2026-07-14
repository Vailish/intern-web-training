package com.example.intromybatis.service;

import com.example.intromybatis.domain.Intro;
import com.example.intromybatis.mapper.IntroMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업무 규칙(비즈니스 로직)을 담당하는 서비스(Service) 계층입니다.
 * JPA 버전(intro-jpa)과 비교해 보세요 — 리포지토리가 매퍼로 바뀌었을 뿐,
 * 컨트롤러에 제공하는 메서드(findAll/findById/create/update/delete)는 완전히 같습니다.
 */
@Service
public class IntroService {

    private final IntroMapper introMapper;

    /** 생성자 주입(DI): 스프링이 MyBatis가 만든 매퍼 구현체를 자동으로 넣어줍니다. */
    public IntroService(IntroMapper introMapper) {
        this.introMapper = introMapper;
    }

    /** [R] 자기소개서 전체 목록 (정렬은 매퍼 XML의 ORDER BY가 담당) */
    public List<Intro> findAll() {
        return introMapper.findAll();
    }

    /** [R] id로 한 건 조회. MyBatis는 없으면 null을 주므로 직접 검사합니다. */
    public Intro findById(Long id) {
        Intro intro = introMapper.findById(id);
        if (intro == null) {
            throw new IllegalArgumentException("존재하지 않는 자기소개서입니다. id=" + id);
        }
        return intro;
    }

    /** [C] 새 자기소개서를 저장합니다. 작성 시각은 서버가 기록합니다. */
    public Intro create(String name, String title, String content) {
        Intro intro = new Intro();
        intro.setName(name);
        intro.setTitle(title);
        intro.setContent(content);
        intro.setCreatedAt(LocalDateTime.now());
        introMapper.insert(intro); // XML의 INSERT 실행. intro.id에 새 번호가 채워집니다.
        return intro;
    }

    /**
     * [U] 자기소개서를 수정합니다.
     *
     * JPA 버전과 비교해 보세요 — JPA는 "조회 후 값만 바꾸면" 변경 감지가 UPDATE를
     * 만들어 줬지만, MyBatis에는 그런 기능이 없습니다. UPDATE도 우리가 직접 부릅니다.
     * @Transactional의 역할(메서드 단위 커밋/롤백)은 JPA 때와 똑같습니다.
     */
    @Transactional
    public void update(Long id, String name, String title, String content) {
        Intro intro = findById(id);   // 존재 확인 (없으면 예외)
        intro.setName(name);
        intro.setTitle(title);
        intro.setContent(content);
        introMapper.update(intro);    // ← 이 호출이 없으면 아무 일도 일어나지 않습니다!
    }

    /** [D] id로 자기소개서를 삭제합니다. */
    public void delete(Long id) {
        introMapper.deleteById(id);
    }
}
