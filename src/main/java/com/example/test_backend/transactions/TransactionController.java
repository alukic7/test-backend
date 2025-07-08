package com.example.test_backend.transactions;

import com.example.test_backend.transactions.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Operations related to money transfers")
public class TransactionController {
    private final TransactionService service;

    private static long currentUser(HttpServletRequest req) {
        Object id = req.getAttribute("USER_ID");
        if (id == null)
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Login required");
        return (Long) id;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer money between accounts")
    @ApiResponse(responseCode = "201", description = "Transaction successfully completed")
    public Transaction transfer(HttpServletRequest req, @RequestBody @Valid TransferDto dto) {
        var transferCmd = new TransactionService.TransferCmd(
                dto.fromAccountId(), dto.toAccountId(), dto.amount(), dto.description());
        return service.transfer(currentUser(req), transferCmd);
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get transaction history for an account")
    @ApiResponse(responseCode = "200", description = "List of transactions")
    public List<Transaction> history(HttpServletRequest req, @PathVariable long accountId) {
        return service.history(accountId, currentUser(req));
    }

    record TransferDto(
            @Schema(description = "ID of the account to transfer money from", example = "1")
            long fromAccountId,
            @Schema(description = "ID of the account to transfer money to", example = "2")
            long toAccountId,
            @Schema(description = "Amount to transfer", example = "100")
            @Min(1) long amount,
            @Schema(description = "Transfer description", example = "Rent for July")
            @NotBlank String description) {}
}
