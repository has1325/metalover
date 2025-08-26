package com.example.metalover;

import java.util.List;
import java.util.Optional;

import org.apache.catalina.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MetaloverService implements UserDetailsService {
	
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
	
	@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Metalover m = metaloverRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 권한 처리: 간단히 roles 문자열을 ','로 분리하거나 고정 ROLE_USER 사용
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(m.getEmail())
                .password(m.getPassword()) // 암호화된 비밀번호 저장되어 있어야 함 (BCrypt)
                .authorities(authorsToStrings(authorities))
                .build();
    }

	private String[] authorsToStrings(List<SimpleGrantedAuthority> auths) {
        return auths.stream().map(SimpleGrantedAuthority::getAuthority).toArray(String[]::new);
    }
	
}
