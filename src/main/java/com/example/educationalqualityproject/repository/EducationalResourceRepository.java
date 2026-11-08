package com.example.educationalqualityproject.repository;

import com.example.educationalqualityproject.entity.EducationalResource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EducationalResourceRepository extends MongoRepository<EducationalResource, String> {
}
