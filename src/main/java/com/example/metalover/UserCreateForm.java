package com.example.metalover;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {

	@NotEmpty(message = "아이디는 필수 항목입니다.")
	private String userid;
	
	@NotEmpty(message = "이메일은 필수 항목입니다.")
	@Email
	private String email;
	
	@NotEmpty(message = "비밀번호는 필수 항목입니다.")
	private String password1;
	
	@NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")
	private String password2;
	
	@NotEmpty(message = "이름은 필수 항목입니다.")
	private String username;
	
	@NotEmpty(message = "전화번호는 필수 항목입니다.")
	private String phone;
	
}
