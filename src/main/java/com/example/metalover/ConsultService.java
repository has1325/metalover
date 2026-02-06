package com.example.metalover;

import org.springframework.stereotype.Service;

@Service
public class ConsultService {

    private final ConsultRepository consultRepository;

    // ✅ 생성자 직접 작성 (이게 핵심)
    public ConsultService(ConsultRepository consultRepository) {
        this.consultRepository = consultRepository;
    }

    public void save(MetaloverConsult request) {
        consultRepository.save(request);
    }
}
