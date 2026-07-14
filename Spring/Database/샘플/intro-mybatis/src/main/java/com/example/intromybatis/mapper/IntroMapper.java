package com.example.intromybatis.mapper;

import com.example.intromybatis.domain.Intro;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * DB에 접근하는 매퍼(Mapper) 인터페이스입니다. JPA의 리포지토리에 해당하는 자리입니다.
 *
 * - @Mapper : "이 인터페이스의 구현체를 MyBatis가 만들어 줘"라는 표식
 * - 각 메서드의 실제 SQL은 resources/mapper/IntroMapper.xml 에 있습니다.
 *   메서드 이름과 XML의 id가 짝을 이룹니다. (findAll ↔ <select id="findAll">)
 *
 * JPA와의 결정적 차이: JpaRepository는 상속만 하면 메서드가 공짜로 생겼지만,
 * MyBatis는 메서드 하나마다 SQL을 우리가 직접 씁니다.
 */
@Mapper
public interface IntroMapper {

    /** [R] 전체 목록 (최신 글이 위로 — 정렬도 XML의 SQL에서 직접) */
    List<Intro> findAll();

    /** [R] 한 건 조회. 없으면 null을 반환합니다. */
    Intro findById(Long id);

    /** [C] 저장. 실행 후 intro.id에 DB가 매긴 번호가 채워집니다. */
    int insert(Intro intro);

    /** [U] 수정. MyBatis에는 변경 감지가 없어서 UPDATE도 직접 부릅니다. */
    int update(Intro intro);

    /** [D] 삭제 */
    int deleteById(Long id);
}
