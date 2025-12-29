package com.example.metalover;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultService {

    private final ConsultRepository consultRepository;

    public void save(ConsultRequest request) {
        consultRepository.save(request);
    }
}
