package com.example.metalover;

import java.util.List;
import java.util.Optional;

import org.apache.catalina.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MetaloverService {
	
	private final MetaloverRepository metaloverRepository;
	private final PasswordEncoder passwordEncoder;
	
	public Metalover create(String userid, String email, String password, String username, String phone) {
		Metalover metalover = new Metalover();
		metalover.setUserid(userid);
		metalover.setEmail(email);
		metalover.setPassword(passwordEncoder.encode(password));
		metalover.setUsername(username);
		metalover.setPhone(phone);
		this.metaloverRepository.save(metalover);
		return metalover;
	}
	
	public List<Metalover> getAllMetalover() {
        return metaloverRepository.findAll();
    }
	
	public String findUserId(String username, String email) {
		Optional<Metalover> metaloverOptional = metaloverRepository.findByUsernameAndEmail(username, email);
	    return metaloverOptional.isPresent() ? metaloverOptional.get().getUserid() : null;
    }

	public Metalover getMetaloverByUseridAndUsernameAndEmail(String userid, String username, String email) {
		Optional<Metalover> metalover = this.metaloverRepository.findByUseridAndUsernameAndEmail(userid, username, email);
		if(metalover.isPresent()) {
			return metalover.get();
		}else {
			throw new DataNotFoundException("Email not found!!");
		}
    }

	public Metalover findByEmail(String email) {
	    Optional<Metalover> metaloverOptional = metaloverRepository.findByEmail(email);
	    return metaloverOptional.orElse(null);
	}
	
	public Metalover getMyInfo(String userId) {
        return metaloverRepository.findByUserid(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
	
}
