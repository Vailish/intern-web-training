package com.example.introjpa.controller;

import com.example.introjpa.service.IntroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 브라우저의 요청을 가장 먼저 받는 컨트롤러(Controller)입니다.
 * - @Controller : 요청을 받아 "보여줄 화면(템플릿) 이름"을 반환하는 클래스라는 표식
 * - 메서드가 반환한 문자열("list")은 templates/list.html 파일을 가리킵니다.
 *
 * URL 설계 (CRUD 전체):
 *   GET  /                  목록          (Read)
 *   GET  /intro/new         작성 폼
 *   POST /intro             저장          (Create)
 *   GET  /intro/{id}        상세          (Read)
 *   GET  /intro/{id}/edit   수정 폼
 *   POST /intro/{id}/edit   수정          (Update)
 *   POST /intro/{id}/delete 삭제          (Delete)
 * (HTML의 form은 GET/POST만 보낼 수 있어서 수정·삭제도 POST를 씁니다.)
 */
@Controller
public class IntroController {

    private final IntroService introService;

    // 생성자 주입: 스프링이 IntroService 빈을 자동으로 넣어줍니다.
    public IntroController(IntroService introService) {
        this.introService = introService;
    }

    /** [R] 목록 화면: GET / */
    @GetMapping("/")
    public String list(Model model) {
        // Model: 서버의 데이터를 템플릿(HTML)에 전달하는 "쟁반" 역할
        model.addAttribute("intros", introService.findAll());
        return "list"; // → templates/list.html
    }

    /** 작성 폼 화면: GET /intro/new */
    @GetMapping("/intro/new")
    public String form() {
        return "form"; // → templates/form.html
    }

    /**
     * [C] 저장 처리: POST /intro
     * @RequestParam : 폼에서 넘어온 입력값(name 속성 기준)을 파라미터로 받습니다.
     * 저장 후에는 화면을 직접 그리지 않고 목록으로 "다시 가라"고 지시합니다.
     * (redirect를 쓰는 이유: 새로고침 시 중복 등록 방지)
     */
    @PostMapping("/intro")
    public String create(@RequestParam String name,
                         @RequestParam String title,
                         @RequestParam String content) {
        introService.create(name, title, content);
        return "redirect:/";
    }

    /**
     * [R] 상세 화면: GET /intro/{id}  (예: /intro/3)
     * @PathVariable : URL 경로에 들어있는 값(3)을 파라미터로 받습니다.
     */
    @GetMapping("/intro/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("intro", introService.findById(id));
        return "detail"; // → templates/detail.html
    }

    /** 수정 폼 화면: GET /intro/{id}/edit — 기존 값을 채워서 보여줍니다. */
    @GetMapping("/intro/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("intro", introService.findById(id));
        return "edit"; // → templates/edit.html
    }

    /** [U] 수정 처리: POST /intro/{id}/edit — 수정 후 상세 화면으로 돌아갑니다. */
    @PostMapping("/intro/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String name,
                       @RequestParam String title,
                       @RequestParam String content) {
        introService.update(id, name, title, content);
        return "redirect:/intro/" + id;
    }

    /** [D] 삭제 처리: POST /intro/{id}/delete — 삭제 후 목록으로 돌아갑니다. */
    @PostMapping("/intro/{id}/delete")
    public String delete(@PathVariable Long id) {
        introService.delete(id);
        return "redirect:/";
    }
}
