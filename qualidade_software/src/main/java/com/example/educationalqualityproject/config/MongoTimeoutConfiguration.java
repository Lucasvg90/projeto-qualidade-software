package com.example.educationalqualityproject.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoTimeoutConfiguration {

    @Bean
    MongoClientSettingsBuilderCustomizer mongoTimeouts() {
        return builder -> {
            builder.applyToClusterSettings(settings ->
                    settings.serverSelectionTimeout(5, TimeUnit.SECONDS));
            builder.applyToSocketSettings(settings ->
                    settings.connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(5, TimeUnit.SECONDS));
        };
    }
}
