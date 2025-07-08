package com.example.test_backend.filters;

import com.example.test_backend.session.repository.SessionRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.WebUtils;
import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class SessionFilter extends GenericFilter {

    private static final String COOKIE = "SESSION_ID";
    private final SessionRepository sessions;

    // Filter for getting the users id at each authorized request
    @Override
    public void doFilter(ServletRequest r, ServletResponse s, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) r;
        Cookie c = WebUtils.getCookie(req, COOKIE);
        if (c != null) {
            try {
                sessions.findValid(UUID.fromString(c.getValue()))
                        .ifPresent(sess -> req.setAttribute("USER_ID", sess.userId()));
            } catch (IllegalArgumentException ignored) {}
        }
        chain.doFilter(r, s);
    }
}
