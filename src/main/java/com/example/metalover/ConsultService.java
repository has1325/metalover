package com.example.metalover;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultService {

    private final ConsultRepository consultRepository;

    public void save(MetaloverConsult request) {
        consultRepository.save(request);
    }
}
