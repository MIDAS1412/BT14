package com.example.NgoDangKhoa_2280601515.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private boolean active = true;

    @Column(nullable = false)
    private String role = "USER"; // Có thể là USER, MANAGER, ADMIN

    private LocalDateTime createdAt = LocalDateTime.now();
}
