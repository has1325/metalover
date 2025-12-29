package com.example.metalover;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "consult_request")
@Getter
@Setter
public class MetaloverConsult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
}
