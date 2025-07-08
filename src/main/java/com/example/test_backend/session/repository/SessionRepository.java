package com.example.test_backend.session.repository;

import com.example.test_backend.session.model.Session;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    Session create(long userId);
    Optional<Session> findValid(UUID token);
    void invalidate(UUID token);
}
