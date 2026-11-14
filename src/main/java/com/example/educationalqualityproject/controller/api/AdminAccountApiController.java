package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.dto.AccountResponse;
import com.example.educationalqualityproject.dto.ManagedAccountRequest;
import com.example.educationalqualityproject.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminAccountApiController {

    private final UserAccountService accountService;

    public AdminAccountApiController(UserAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> list() {
        return accountService.getAllAccounts().stream().map(AccountResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@RequestBody ManagedAccountRequest request, Authentication authentication) {
        return AccountResponse.from(accountService.createManagedAccount(
                request.name(), request.email(), request.password(), request.role(), authentication.getName()));
    }
}
