package com.example.introapi.controller;

import com.example.introapi.domain.Intro;
import com.example.introapi.dto.IntroRequest;
import com.example.introapi.service.IntroService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API 컨트롤러 — intro-jpa의 IntroController와 비교하며 읽어 보세요!
 *
 * ┌─────────────────┬──────────────────────────┬─────────────────────────┐
 * │                 │ intro-jpa (@Controller)  │ intro-api (@RestController) │
 * ├─────────────────┼──────────────────────────┼─────────────────────────┤
 * │ 반환값의 의미   │ 템플릿 이름 ("list")     │ 데이터 그 자체 (JSON 변환) │
 * │ 화면(HTML)      │ 서버가 만들어서 응답     │ 안 만듦 — React의 몫    │
 * │ 입력값 받기     │ @RequestParam (폼 전송)  │ @RequestBody (JSON 본문) │
 * │ HTTP 메서드     │ GET/POST만 (HTML 폼 한계)│ GET/POST/PUT/DELETE     │
 * │ 저장 후         │ redirect:/               │ 201 Created + 저장된 데이터│
 * └─────────────────┴──────────────────────────┴─────────────────────────┘
 *
 * URL 설계 (REST 스타일):
 *   GET    /api/intros        목록 조회   (Read)
 *   POST   /api/intros        등록        (Create)
 *   GET    /api/intros/{id}   한 건 조회  (Read)
 *   PUT    /api/intros/{id}   수정        (Update)
 *   DELETE /api/intros/{id}   삭제        (Delete)
 *
 * intro-jpa에서는 /intro/new, /intro/{id}/edit, /intro/{id}/delete처럼
 * URL에 "동작"이 들어갔지만, REST에서는 URL은 자원(intros)만 가리키고
 * 동작은 HTTP 메서드(GET/POST/PUT/DELETE)로 구분합니다.
 * "작성 폼 화면(GET /intro/new)"에 해당하는 API가 없다는 것도 포인트 —
 * 화면은 이제 전부 React가 알아서 그립니다.
 */
@RestController                 // = @Controller + "반환값을 JSON으로 바꿔서 응답해라"
@RequestMapping("/api/intros")  // 이 클래스의 모든 URL 앞에 공통으로 붙는 경로
public class IntroApiController {

    private final IntroService introService;

    // 생성자 주입: 스프링이 IntroService 빈을 자동으로 넣어줍니다. (intro-jpa와 동일)
    public IntroApiController(IntroService introService) {
        this.introService = introService;
    }

    /**
     * [R] 목록 조회: GET /api/intros
     * List<Intro>를 반환하면 스프링이 JSON 배열로 바꿔 응답합니다.
     *   [ {"id":2, "name":"...", ...}, {"id":1, ...} ]
     * Model에 담아 템플릿에 넘기던 코드가 "그냥 반환"으로 바뀌었습니다.
     */
    @GetMapping
    public List<Intro> list() {
        return introService.findAll();
    }

    /**
     * [R] 한 건 조회: GET /api/intros/{id}  (예: /api/intros/3)
     * @PathVariable : URL 경로에 들어있는 값(3)을 파라미터로 받습니다.
     */
    @GetMapping("/{id}")
    public Intro detail(@PathVariable Long id) {
        return introService.findById(id);
    }

    /**
     * [C] 등록: POST /api/intros
     *
     * @RequestBody : 요청 본문의 JSON을 IntroRequest 객체로 자동 변환해 받습니다.
     *   (intro-jpa의 @RequestParam 3개가 이것 하나로 바뀌었습니다)
     *
     * ResponseEntity : 데이터뿐 아니라 "상태 코드"까지 직접 정해서 응답할 때 씁니다.
     *   - 성공적으로 "만들었다"는 뜻의 201 Created를 돌려줍니다. (그냥 200도 동작은
     *     하지만, 의미를 정확히 전달하는 것이 좋은 API의 매너입니다)
     *   - 저장된 결과(id, createdAt 포함)를 본문에 담아 주면, React가 방금 만든
     *     글의 id를 바로 알 수 있습니다.
     *
     * redirect가 없다는 것에 주목! "저장 후 어느 화면으로 갈지"는
     * 이제 서버가 아니라 React(프론트)가 결정합니다.
     */
    @PostMapping
    public ResponseEntity<Intro> create(@RequestBody IntroRequest request) {
        Intro saved = introService.create(request.getName(), request.getTitle(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved); // 201 Created
    }

    /**
     * [U] 수정: PUT /api/intros/{id}
     * PUT은 "이 자원을 이 내용으로 바꿔 달라"는 뜻의 HTTP 메서드입니다.
     * HTML 폼은 GET/POST만 보낼 수 있어서 intro-jpa에서는 POST /intro/{id}/edit로
     * 흉내 냈지만, JavaScript의 fetch()는 PUT을 제대로 보낼 수 있습니다.
     */
    @PutMapping("/{id}")
    public Intro update(@PathVariable Long id, @RequestBody IntroRequest request) {
        return introService.update(id, request.getName(), request.getTitle(), request.getContent());
    }

    /**
     * [D] 삭제: DELETE /api/intros/{id}
     * 성공 시 "잘 지웠고, 돌려줄 내용은 없다"는 뜻의 204 No Content를 응답합니다.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        introService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 예외 처리: 서비스가 "존재하지 않는 id"라며 IllegalArgumentException을 던지면
     * 이 메서드가 대신 받아서 404 Not Found + 에러 메시지(JSON)로 응답합니다.
     *   { "message": "존재하지 않는 자기소개서입니다. id=999" }
     *
     * 이걸 안 하면 스프링은 500 Internal Server Error를 돌려주는데,
     * "서버가 고장났다(500)"와 "네가 찾는 게 없다(404)"는 전혀 다른 의미입니다.
     * 상태 코드를 정확히 주어야 프론트가 상황에 맞는 안내를 할 수 있습니다.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }
}
