package com.example.test_backend.user.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String email,
        String passwordHash,
        LocalDateTime createdAt) {}