package com.example.intro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * 자기소개서 한 건을 표현하는 엔티티(Entity) 클래스입니다.
 * - @Entity : "이 클래스는 DB 테이블과 짝을 이룬다"고 JPA에게 알려주는 표식
 * - 클래스 이름(Intro)이 테이블 이름이 되고, 필드 하나하나가 컬럼이 됩니다.
 */
@Entity
public class Intro {

    // 기본키(PK). DB가 1, 2, 3... 순서대로 번호를 자동으로 매겨줍니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 이름
    private String name;

    // 자기소개서 제목
    private String title;

    // 자기소개 내용. 길게 쓸 수 있도록 컬럼 길이를 넉넉하게 잡았습니다.
    @Column(length = 4000)
    private String content;

    // 작성 시각
    private LocalDateTime createdAt;

    /**
     * JPA가 DB에서 읽어온 값으로 객체를 만들 때 사용하는 기본 생성자입니다.
     * 엔티티에는 파라미터 없는 생성자가 반드시 있어야 합니다.
     */
    public Intro() {
    }

    // ===== getter / setter =====
    // 다른 클래스에서 private 필드를 읽고 쓸 수 있게 해주는 메서드들입니다.
    // (실무에서는 Lombok 라이브러리로 자동 생성하기도 합니다. → 04 문서 참고)

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
