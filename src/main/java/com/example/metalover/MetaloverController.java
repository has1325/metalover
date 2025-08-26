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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "https://metalover.kr") // 필요하다면 유지, 전역설정 가능
@RequiredArgsConstructor
@Controller
public class MetaloverController {

	private final MetaloverService metaloverService;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	
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
	public String login() {
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
	@PostMapping("/api/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
		String email = loginData.get("email");
		String password = loginData.get("password");

		if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "이메일과 비밀번호를 모두 입력해 주세요."));
		}

		Metalover metalover = metaloverService.findByEmail(email);

		if (metalover != null && passwordEncoder.matches(password, metalover.getPassword())) {
			// ✅ JWT 토큰 생성
			String token = jwtUtil.generateToken(email);

			return ResponseEntity.ok(Map.of("token", token, "message", "로그인 성공", "user", metalover.getUsername()));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "아이디 또는 비밀번호가 올바르지 않습니다."));
		}
	}

	@PostMapping("/api/signup")
	public ResponseEntity<Map<String, String>> apiSignup(@Valid @RequestBody UserCreateForm form, BindingResult bindingResult) {

	    // 1. 입력값 검증
	    if (bindingResult.hasErrors()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("error", "입력값이 올바르지 않습니다."));
	    }

	    // 2. 비밀번호 일치 여부 확인
	    if (!form.getPassword1().equals(form.getPassword2())) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("error", "비밀번호가 일치하지 않습니다."));
	    }

	    try {
	        // 3. 회원 생성 (Service 내부에서 PasswordEncoder 사용하여 암호화 저장)
	        metaloverService.create(
	                form.getUserid(),
	                form.getEmail(),
	                form.getPassword1(),
	                form.getUsername(),
	                form.getPhone()
	        );

	        return ResponseEntity.ok(Map.of("message", "회원가입 성공"));
	    } catch (DataIntegrityViolationException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	                .body(Map.of("error", "이미 등록된 사용자입니다."));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "서버 에러: " + e.getMessage()));
	    }
	}

}
