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

    /**
     * Find student list by last name without case sensitive
     * @param lastName last name
     * @return student list
     */
    List<Student> findByLastNameIgnoreCase(String lastName);

    /**
     * Find student list by last name and first name without case sensitive
     * @param lastName last name
     * @param firstName first name
     * @return student list
     */
    List<Student> findByLastNameAndFirstNameAllIgnoreCase(String lastName, String firstName);

    /**
     * Find student list by last name or first name without case sensitive
     * @param lastName last name
     * @param firstName first name
     * @return Student list
     */
    List<Student> findByLastNameOrFirstNameAllIgnoreCase(String lastName, String firstName);

    /**
     * Find student list by first name matching end without case sensitive
     * @param firstName
     * @return
     */
    List<Student> findByFirstNameEndsWithIgnoreCase(String firstName);

    /**
     * Find student list by last name matching end and first name matching end without case sensitive
     * @param lastName
     * @param firstName
     * @return
     */
    List<Student> findByLastNameStartsWithAndFirstNameStartsWithAllIgnoreCase(String lastName, String firstName);

    /**
     * Find student list by last name matching or and first name matching end without case sensitive
     * @param lastName
     * @param firstName
     * @return
     */
    List<Student> findByLastNameStartsWithOrFirstNameStartsWithAllIgnoreCase(String lastName, String firstName);

}
