package com.example.test_backend.user;

import com.example.test_backend.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, registration and logout")
public class UserController {

    private static final String COOKIE = "SESSION_ID";
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    public User register(@Valid @RequestBody RegisterDto dto) {
        return userService.register(dto.email(), dto.password());
    }

    @PostMapping("/login")
    @Operation(summary = "Log in a user")
    @ApiResponse(responseCode = "200", description = "Login successful, session cookie and CSRF token returned")
    public ResponseEntity<CsrfDto> login(@Valid @RequestBody LoginDto dto,
                                         HttpServletResponse res) {
        UserService.AuthTokens t = userService.login(dto.email(), dto.password());

        ResponseCookie cookie = ResponseCookie.from(COOKIE, t.sessionId().toString())
                // httpOnly to prevent XSS, secure(true) when in prod, false for localhost
                .httpOnly(true).secure(false)
                // we can still keep same-site even though CSRF token is implemented
                .sameSite("Strict").path("/")
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new CsrfDto(t.csrfToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        Cookie c = WebUtils.getCookie(req, COOKIE);
        if (c != null) {
            userService.logout(UUID.fromString(c.getValue()));
            c.setMaxAge(0);
            res.addCookie(c);
        }
    }

    @Schema(name = "RegisterDto", description = "User registration payload")
    record RegisterDto(
            @Schema(description = "User email address", example = "user@example.com")
            @Email String email,

            @Schema(description = "User password (min length, etc)", example = "StrongPassword123!")
            @NotBlank String password) {}
    record LoginDto(
            @Schema(description = "User email address", example = "user@example.com")
            @Email String email,

            @Schema(description = "User password", example = "StrongPassword123!")
            @NotBlank String password) {}
    record CsrfDto(
            @Schema(name = "CsrfDto", description = "CSRF token obtained after the new session is created")
            UUID csrfToken
    ) {}
}
