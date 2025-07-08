package com.example.test_backend.session.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Session(UUID id,
                      Long userId,
                      LocalDateTime createdAt,
                      LocalDateTime expiresAt,
                      boolean valid,
                      UUID csrf_token) {}
