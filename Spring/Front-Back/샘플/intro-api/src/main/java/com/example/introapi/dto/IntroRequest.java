package com.example.introapi.dto;

/**
 * 등록/수정 요청의 본문(body)을 담는 DTO(Data Transfer Object)입니다.
 *
 * React가 이런 JSON을 보내면:
 *   { "name": "홍길동", "title": "안녕하세요", "content": "저는..." }
 *
 * 스프링이 키 이름을 보고 같은 이름의 필드에 자동으로 채워 줍니다.
 * (컨트롤러의 @RequestBody 파라미터에서 사용 — JSON → 자바 객체 자동 변환)
 *
 * "엔티티(Intro)를 바로 받으면 안 되나?" → 받을 수는 있지만 위험합니다.
 * 요청에 "id": 999 같은 값을 몰래 끼워 넣어도 그대로 들어와 버리기 때문에,
 * 실무에서는 "클라이언트가 보낼 수 있는 값만" 담은 별도 클래스를 만듭니다.
 * (클라이언트가 보낸 데이터는 항상 의심하기 — 서버 검증의 기본 원칙!)
 */
public class IntroRequest {

    private String name;
    private String title;
    private String content;

    /** JSON → 객체 변환 시 스프링(Jackson)이 사용하는 기본 생성자입니다. */
    public IntroRequest() {
    }

    // ===== getter / setter =====
    // JSON의 "name" 키 → setName()으로 값이 들어오고, 서비스에서 getName()으로 꺼냅니다.

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
}
