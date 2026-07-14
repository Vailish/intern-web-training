package com.example.intromybatis.domain;

import java.time.LocalDateTime;

/**
 * 자기소개서 한 건을 표현하는 클래스입니다.
 *
 * JPA 버전(intro-jpa)과 비교해 보세요 — @Entity, @Id, @Column이 전부 사라졌습니다!
 * MyBatis에서 이 클래스는 "SQL 결과를 담는 그릇"일 뿐이라, 아무 표식도 필요 없습니다.
 * 테이블과의 연결은 매퍼 XML(resources/mapper/IntroMapper.xml)의 SQL이 담당합니다.
 * (created_at 컬럼 → createdAt 필드 변환은 map-underscore-to-camel-case 설정이 해 줍니다)
 */
public class Intro {

    // 기본키(PK). 번호는 DB가 매기고(AUTO_INCREMENT), INSERT 후 여기로 돌려받습니다.
    private Long id;

    // 작성자 이름
    private String name;

    // 자기소개서 제목
    private String title;

    // 자기소개 내용
    private String content;

    // 작성 시각
    private LocalDateTime createdAt;

    // ===== getter / setter =====

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
