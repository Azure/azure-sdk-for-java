// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.domain.Student;
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
