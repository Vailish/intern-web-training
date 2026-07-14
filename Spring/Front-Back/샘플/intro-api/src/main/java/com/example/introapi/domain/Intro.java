package com.example.introapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * 자기소개서 한 건을 표현하는 엔티티(Entity) 클래스입니다.
 * intro-jpa의 Intro와 완전히 동일합니다 — 백엔드/프론트를 분리해도
 * "데이터를 다루는 코드"는 바뀌지 않는다는 것이 핵심입니다.
 *
 * 한 가지 새 역할: 컨트롤러가 이 객체를 반환하면 스프링이 자동으로
 * JSON으로 바꿔 줍니다(getter 이름 기준).
 *   Intro(id=1, name="홍길동") → {"id":1, "name":"홍길동", ...}
 */
@Entity
public class Intro {

    // 기본키(PK). DB가 1, 2, 3... 순서대로 번호를 자동으로 매겨줍니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 이름
    @Column(length = 50, nullable = false)
    private String name;

    // 자기소개서 제목
    @Column(length = 200, nullable = false)
    private String title;

    // 자기소개 내용. 길게 쓸 수 있도록 컬럼 길이를 넉넉하게 잡았습니다.
    @Column(length = 4000)
    private String content;

    // 작성 시각. JSON으로 나갈 때 "2026-07-14T10:30:00" 형태(ISO 8601)가 됩니다.
    private LocalDateTime createdAt;

    /**
     * JPA가 DB에서 읽어온 값으로 객체를 만들 때 사용하는 기본 생성자입니다.
     * 엔티티에는 파라미터 없는 생성자가 반드시 있어야 합니다.
     */
    public Intro() {
    }

    // ===== getter / setter =====
    // JSON 변환도 이 getter들을 사용합니다. getName() → JSON의 "name" 키.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
