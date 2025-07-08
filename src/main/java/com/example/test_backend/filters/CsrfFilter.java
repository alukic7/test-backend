package com.example.test_backend.filters;

import com.example.test_backend.session.model.Session;
import com.example.test_backend.session.repository.SessionRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.UUID;

public class CsrfFilter extends GenericFilter {

    private final SessionRepository sessions;

    public CsrfFilter(SessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    public void doFilter(ServletRequest r, ServletResponse s, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) r;
        HttpServletResponse res = (HttpServletResponse) s;

        if ("POST".equals(req.getMethod()) &&
                ("/api/auth/register".equals(req.getRequestURI()) ||
                "/api/auth/login".equals(req.getRequestURI()))) {
            chain.doFilter(r, s);
            return;
        }

        if (!switch (req.getMethod()) {
            case "GET", "HEAD", "OPTIONS" -> true;
            default -> false;
        }) {
            Cookie c = WebUtils.getCookie(req, "SESSION_ID");
            String header = req.getHeader("X-CSRF-TOKEN");

            if (c == null || header == null) {
                res.sendError(403, "Missing CSRF token"); return;
            }
            UUID sid = UUID.fromString(c.getValue());
            UUID provided = UUID.fromString(header);

            UUID expected = sessions.findValid(sid)
                    .map(Session::csrf_token)
                    .orElse(null);

            if (!provided.equals(expected)) {
                res.sendError(403, "Invalid CSRF token"); return;
            }
        }
        chain.doFilter(r, s);
    }
}

