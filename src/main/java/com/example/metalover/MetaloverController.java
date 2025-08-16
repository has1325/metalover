package com.example.metalover;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = {
        "https://metalover.kr",
        "https://metalover.onrender.com"
}, allowCredentials = "true")
@RequiredArgsConstructor
@Controller
public class MetaloverController {

    private final MetaloverService metaloverService;
    private final PasswordEncoder passwordEncoder;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // index 페이지
    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        model.addAttribute("loginUser", session.getAttribute("loginUser"));
        return "index";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam("userid") String userid,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        if (userid == null || userid.isEmpty() || password == null || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "아이디와 비밀번호를 모두 입력해 주세요.");
            return "redirect:/login";
        }

        Metalover user = metaloverService.getMyInfoSafe(userid);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            session.setAttribute("loginUser", user);
            return "redirect:/index";
        } else {
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // 회원가입 API
    @PostMapping("/api/signup")
    public ResponseEntity<Map<String, String>> apiSignup(@RequestBody Map<String, String> payload) {
        String userid = payload.get("userid");
        String email = payload.get("email");
        String password1 = payload.get("password1");
        String password2 = payload.get("password2");
        String username = payload.get("username");
        String phone = payload.get("phone");

        if (userid == null || userid.isBlank() || email == null || email.isBlank() || password1 == null
                || password1.isBlank() || username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "필수 입력값이 누락되었습니다."));
        }

        if (!password1.equals(password2)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        try {
            metaloverService.create(userid, email, password1, username, phone);
            return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "이미 등록된 사용자입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 에러: " + e.getMessage()));
        }
    }

    // 세션 체크 API (프론트에서 로그인 상태 확인용)
    @GetMapping("/api/check-session")
    @ResponseBody
    public Map<String, Boolean> checkSession(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        return Map.of("loggedIn", loginUser != null);
    }
}
