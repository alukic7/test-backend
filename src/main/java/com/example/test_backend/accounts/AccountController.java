package com.example.test_backend.accounts;

import com.example.test_backend.accounts.model.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Operations for manipulating user bank accounts")
public class AccountController {
    private final AccountService service;

    private static long currentUser(HttpServletRequest req) {
        Object id = req.getAttribute("USER_ID");
        if (id == null) throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Login required");
        return (Long) id;
    }

    @GetMapping
    @Operation(summary = "Get all accounts for the logged-in user")
    @ApiResponse(responseCode = "200", description = "List of accounts")
    public List<Account> all(HttpServletRequest req) {
        return service.myAccounts(currentUser(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific account balance")
    @ApiResponse(responseCode = "200", description = "Given account balance")
    public long accountBalance(HttpServletRequest req, @PathVariable long id) {
        return service.getBalance(currentUser(req), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open a new account")
    @ApiResponse(responseCode = "201", description = "Account successfully created")
    public Account open(HttpServletRequest req,
                        @RequestBody @Valid CreateAccountDto dto) {
        return service.open(currentUser(req), dto.initialAmount());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Close an account")
    @ApiResponse(responseCode = "204", description = "Account successfully closed")
    public void close(HttpServletRequest req, @PathVariable long id) {
        service.close(currentUser(req), id);
    }

    @Schema(description = "Initial amount for the new account")
    record CreateAccountDto(@Min(0) long initialAmount) {}

    @Schema(description = "Initial amount for the new account")
    record AccountDetailsDto(@Min(0) long balance) {}
}