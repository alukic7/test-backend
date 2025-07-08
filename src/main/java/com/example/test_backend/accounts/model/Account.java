package com.example.test_backend.accounts.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Account(
        Long id,
        Long userId,
        long balance,
        boolean closed,
        LocalDateTime createdAt
) {}