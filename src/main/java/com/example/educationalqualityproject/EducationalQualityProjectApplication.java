package com.example.educationalqualityproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration")
public class EducationalQualityProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationalQualityProjectApplication.class, args);
    }

}