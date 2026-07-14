package com.example.intromybatis.controller;

import com.example.intromybatis.service.IntroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 브라우저의 요청을 가장 먼저 받는 컨트롤러(Controller)입니다.
 * JPA 버전(intro-jpa)과 한 글자도 다르지 않습니다(패키지명 제외)!
 * 리포지토리 계층을 통째로 갈아끼워도 컨트롤러는 모릅니다 — 계층을 나눈 보람입니다.
 */
@Controller
public class IntroController {

    private final IntroService introService;

    public IntroController(IntroService introService) {
        this.introService = introService;
    }

    /** [R] 목록 화면: GET / */
    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("intros", introService.findAll());
        return "list";
    }

    /** 작성 폼 화면: GET /intro/new */
    @GetMapping("/intro/new")
    public String form() {
        return "form";
    }

    /** [C] 저장 처리: POST /intro */
    @PostMapping("/intro")
    public String create(@RequestParam String name,
                         @RequestParam String title,
                         @RequestParam String content) {
        introService.create(name, title, content);
        return "redirect:/";
    }

    /** [R] 상세 화면: GET /intro/{id} */
    @GetMapping("/intro/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("intro", introService.findById(id));
        return "detail";
    }

    /** 수정 폼 화면: GET /intro/{id}/edit */
    @GetMapping("/intro/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("intro", introService.findById(id));
        return "edit";
    }

    /** [U] 수정 처리: POST /intro/{id}/edit */
    @PostMapping("/intro/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String name,
                       @RequestParam String title,
                       @RequestParam String content) {
        introService.update(id, name, title, content);
        return "redirect:/intro/" + id;
    }

    /** [D] 삭제 처리: POST /intro/{id}/delete */
    @PostMapping("/intro/{id}/delete")
    public String delete(@PathVariable Long id) {
        introService.delete(id);
        return "redirect:/";
    }
}
