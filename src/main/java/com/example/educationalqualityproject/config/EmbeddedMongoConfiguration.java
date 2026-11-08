package com.example.educationalqualityproject.config;

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("embedded")
@Import(EmbeddedMongoAutoConfiguration.class)
public class EmbeddedMongoConfiguration {
}
