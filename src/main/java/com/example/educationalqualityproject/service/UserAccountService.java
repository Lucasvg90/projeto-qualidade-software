package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.UserAccount;
import com.example.educationalqualityproject.entity.UserRole;
import com.example.educationalqualityproject.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class UserAccountService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount registerRequester(String name, String email, String password) {
        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Nome e e-mail são obrigatórios.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Este e-mail já possui uma conta.");
        }
        return repository.save(new UserAccount(
                normalizedEmail, passwordEncoder.encode(password), name.trim(), UserRole.SOLICITANTE));
    }

    public List<UserAccount> getAllAccounts() {
        return repository.findAll();
    }

    public UserAccount createManagedAccount(String name, String email, String password, UserRole role) {
        if (role == null || name == null || name.isBlank() || email == null || email.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Nome, e-mail e perfil são obrigatórios.");
        }
        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(BAD_REQUEST, "A senha deve ter pelo menos 8 caracteres.");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(CONFLICT, "Este e-mail já possui uma conta.");
        }
        return repository.save(new UserAccount(
                normalizedEmail, passwordEncoder.encode(password), name.trim(), role));
    }
}
