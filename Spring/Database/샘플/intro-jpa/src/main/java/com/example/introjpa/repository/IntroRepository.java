package com.example.introjpa.repository;

import com.example.introjpa.domain.Intro;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DB에 접근하는 리포지토리(Repository)입니다.
 *
 * JpaRepository<Intro, Long> 을 상속받는 것만으로
 *   - save(intro)      : INSERT 또는 UPDATE (저장/수정)
 *   - findAll()        : SELECT * (전체 조회)
 *   - findById(id)     : SELECT ... WHERE id = ? (한 건 조회)
 *   - deleteById(id)   : DELETE ... WHERE id = ? (삭제)
 * 같은 메서드가 자동으로 만들어집니다. SQL을 직접 쓰지 않아도 됩니다!
 *
 * <Intro, Long> 의 의미: "Intro 엔티티를 다루고, 그 기본키(id) 타입은 Long이다"
 */
public interface IntroRepository extends JpaRepository<Intro, Long> {
}
