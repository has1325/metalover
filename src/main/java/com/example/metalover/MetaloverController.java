package com.example.metalover;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class MetaloverController {
	
	private final MetaloverService metaloverService;
	private final PasswordEncoder passwordEncoder;

	@GetMapping("/index")
	public String index(HttpSession session, Model model) {
		Object loginUser = session.getAttribute("loginUser");
	    model.addAttribute("loginUser", loginUser);
	    return "index";  // templates/index.html (Thymeleaf 등) 렌더링
	}
	
	@GetMapping("/login")
    public String loginPage() {
        return "login";
    }
	
	@GetMapping("/logout")
	public String logout(HttpSession session) {
	    session.invalidate();  // 세션 초기화 → 로그아웃 처리
	    return "redirect:/login";
	}

	
	@GetMapping("/signup")
    public String signupForm(Model model) {
		model.addAttribute("userDto", new UserCreateForm());
        return "signup";
    }
	
	@GetMapping("/mypage")
	public String mypage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
	    // 1. 세션에서 로그인 사용자 정보 꺼내기
	    Object loginUserObj = session.getAttribute("loginUser");
	    
	    if (loginUserObj == null) {
	        // 로그인 안 된 사용자면 로그인 페이지로 이동
	        redirectAttributes.addFlashAttribute("error", "로그인이 필요한 페이지입니다.");
	        return "redirect:/login";
	    }

	    // 2. Metalover 타입으로 캐스팅 (안전하게 검증했으면 바로 캐스팅 가능)
	    Metalover loginUser = (Metalover) loginUserObj;

	    // 3. DB에서 최신 사용자 정보 다시 조회 (선택 사항, 세션 정보가 오래됐을 수 있음)
	    Metalover user = metaloverService.findByEmail(loginUser.getEmail());
	    if (user == null) {
	        // 사용자 정보가 DB에 없으면 세션 무효화 후 로그인 페이지로
	        session.invalidate();
	        redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
	        return "redirect:/login";
	    }

	    // 4. 사용자 정보를 모델에 담아 뷰로 전달
	    model.addAttribute("user", user);

	    // 5. 마이페이지 템플릿 이름 리턴
	    return "mypage"; // templates/mypage.html 이라는 뷰 파일을 만들어야 합니다.
	}

	
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
    
    @PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "signup";
		}

		if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 비밀번호가 일치하지 않습니다.");
			return "signup";
		}

		try {
			metaloverService.create(userCreateForm.getUserid(), userCreateForm.getEmail(), userCreateForm.getPassword1(),
					userCreateForm.getUsername(), userCreateForm.getPhone());
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());
			return "signup";
		}
		return "redirect:/index";
	}
}