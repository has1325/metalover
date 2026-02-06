package com.example.metalover;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Metalover {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userid;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String phone;

    // ===== getter =====
    public Long getId() { return id; }
    public String getUserid() { return userid; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
    public String getPhone() { return phone; }

    // ===== setter =====
    public void setUserid(String userid) { this.userid = userid; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPhone(String phone) { this.phone = phone; }

    // 비밀번호는 전용 메서드
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
