package com.example.metalover;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "https://metalover.kr") // 필요하다면 유지, 전역설정 가능
@RequiredArgsConstructor
@Controller // ✅ RestController → Controller로 변경
public class MetaloverController {
    
    private final MetaloverService metaloverService;
    private final PasswordEncoder passwordEncoder;

    // === API 엔드포인트 ===
    @GetMapping("/api/hello")
    public String hello(Model model) {
        model.addAttribute("message", "Hello from Spring Boot!");
        return "hello"; // templates/hello.html 필요
    }

    // === 페이지 이동 ===
    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        Object loginUser = session.getAttribute("loginUser");
        model.addAttribute("loginUser", loginUser);
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("userDto", new UserCreateForm());
        return "signup";
    }

    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Object loginUserObj = session.getAttribute("loginUser");

        if (loginUserObj == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요한 페이지입니다.");
            return "redirect:/login";
        }

        Metalover loginUser = (Metalover) loginUserObj;
        Metalover user = metaloverService.findByEmail(loginUser.getEmail());

        if (user == null) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "mypage";
    }

    // === POST 요청 ===
    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "이메일과 비밀번호를 모두 입력해 주세요.");
            return "redirect:/login";
        }

        Metalover metalover = metaloverService.findByEmail(email);

        if (metalover != null && passwordEncoder.matches(password, metalover.getPassword())) {
            session.setAttribute("loginUser", metalover);
            return "redirect:/index";
        } else {
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
    }

    @PostMapping("/api/signup")
    public ResponseEntity<?> apiSignup(@RequestBody Map<String, String> payload) {
        // 간단한 입력값 추출
        String userid = payload.get("userid");
        String email = payload.get("email");
        String password1 = payload.get("password1");
        String password2 = payload.get("password2");
        String username = payload.get("username");
        String phone = payload.get("phone");

        // 간단 검증 (필수값 확인)
        if (userid == null || userid.isBlank()
            || email == null || email.isBlank()
            || password1 == null || password1.isBlank()
            || username == null || username.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "필수 입력값이 누락되었습니다."));
        }

        // 비밀번호 일치 확인
        if (!password1.equals(password2)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "비밀번호가 일치하지 않습니다."));
        }

        try {
            // service 레이어에서 내부적으로 password 인코딩하고 저장하도록 구현되어 있어야 합니다.
            metaloverService.create(userid, email, password1, username, phone);
            return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
        } catch (DataIntegrityViolationException e) {
            // 예: 유니크 제약조건 위반(이미 존재)
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "이미 등록된 사용자입니다."));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 에러: " + e.getMessage()));
        }
    }
}
