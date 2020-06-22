// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.repository;

import com.microsoft.azure.spring.data.cosmosdb.domain.Student;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends CosmosRepository<Student, String> {

    List<Student> findByFirstNameContaining(String firstName);

    List<Student> findByFirstNameContainingAndLastNameContaining(String firstName, String lastName);

    List<Student> findByFirstNameEndsWith(String firstName);

    List<Student> findByFirstNameStartsWith(String firstName);

    List<Student> findByLastNameStartsWith(String lastName);

    List<Student> findByFirstNameStartsWithAndLastNameEndingWith(String firstName, String lastName);

    List<Student> findByFirstNameStartsWithOrLastNameContaining(String firstName, String lastName);

    List<Student> findByFirstNameNot(String firstName);

    List<Student> findByFirstNameContainingAndLastNameNot(String firstName, String lastName);

    Boolean existsByFirstName(String firstName);

    Boolean existsByLastNameContaining(String lastName);
}
