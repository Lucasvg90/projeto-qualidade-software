package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository repository;

    public UserDetailsServiceImpl(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCase(email)
                .map(account -> User.withUsername(account.getEmail())
                        .password(account.getPassword())
                        .roles(account.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada."));
    }
}
