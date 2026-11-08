package com.example.educationalqualityproject.config;

import com.example.educationalqualityproject.entity.UserAccount;
import com.example.educationalqualityproject.entity.UserRole;
import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationSlot;
import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
public class DatabaseBootstrap implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DatabaseBootstrap(
            MongoTemplate mongoTemplate,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_EMAIL:}") String adminEmail,
            @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        this.mongoTemplate = mongoTemplate;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() != adminPassword.isBlank()) {
            throw new IllegalStateException("Configure ADMIN_EMAIL e ADMIN_PASSWORD juntos.");
        }

        for (Class<?> entityType : List.of(
                Student.class,
                Teacher.class,
                UserAccount.class,
                EducationalResource.class,
                Reservation.class,
                ReservationSlot.class)) {
            if (!mongoTemplate.collectionExists(entityType)) {
                mongoTemplate.createCollection(entityType);
            }
        }

        mongoTemplate.indexOps("user_accounts")
                .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps("reservation_slots")
                .ensureIndex(new Index().on("resourceKey", Sort.Direction.ASC)
                        .on("slotStart", Sort.Direction.ASC).unique());

        if (!adminEmail.isBlank() && !userAccountRepository.existsByEmailIgnoreCase(adminEmail)) {
            userAccountRepository.save(new UserAccount(
                    adminEmail.trim().toLowerCase(),
                    passwordEncoder.encode(adminPassword),
                    "Administrador",
                    UserRole.ADMIN));
        }
    }
}
