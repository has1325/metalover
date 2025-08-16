package com.example.metalover;

import java.util.List;
import java.util.Optional;

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
        return this.metaloverRepository.save(metalover);
    }

    public List<Metalover> getAllMetalover() {
        return metaloverRepository.findAll();
    }

    public String findUserId(String username, String email) {
        Optional<Metalover> metaloverOptional = metaloverRepository.findByUsernameAndEmail(username, email);
        return metaloverOptional.map(Metalover::getUserid).orElse(null);
    }

    public Metalover getMetaloverByUseridAndUsernameAndEmail(String userid, String username, String email) {
        return metaloverRepository.findByUseridAndUsernameAndEmail(userid, username, email)
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));
    }

    public Metalover findByEmail(String email) {
        return metaloverRepository.findByEmail(email).orElse(null);
    }

    // ✅ 로그인용 안전 메서드 (사용자 없으면 null 반환)
    public Metalover getMyInfoSafe(String userId) {
        return metaloverRepository.findByUserid(userId).orElse(null);
    }
}
