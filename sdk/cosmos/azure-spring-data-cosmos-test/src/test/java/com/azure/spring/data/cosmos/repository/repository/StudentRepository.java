// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Student;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CosmosRepository<Student, String> {

    Iterable<Student> findByFirstNameContaining(String firstName);

    Iterable<Student> findByFirstNameContainingAndLastNameContaining(String firstName, String lastName);

    Iterable<Student> findByFirstNameEndsWith(String firstName);

    Iterable<Student> findByFirstNameStartsWith(String firstName);

    Iterable<Student> findByLastNameStartsWith(String lastName);

    Iterable<Student> findByFirstNameStartsWithAndLastNameEndingWith(String firstName, String lastName);

    Iterable<Student> findByFirstNameStartsWithOrLastNameContaining(String firstName, String lastName);

    Iterable<Student> findByFirstNameNot(String firstName);

    Iterable<Student> findByFirstNameContainingAndLastNameNot(String firstName, String lastName);

    Boolean existsByFirstName(String firstName);

    Boolean existsByLastNameContaining(String lastName);

    /**
     * Find student list by last name without case sensitive
     *
     * @param lastName last name
     * @return student Iterable
     */
    Iterable<Student> findByLastNameIgnoreCase(String lastName);

    /**
     * Find student list by last name and first name without case sensitive
     *
     * @param lastName last name
     * @param firstName first name
     * @return student Iterable
     */
    Iterable<Student> findByLastNameAndFirstNameAllIgnoreCase(String lastName, String firstName);

    /**
     * Find student list by last name or first name without case sensitive
     *
     * @param lastName last name
     * @param firstName first name
     * @return Student Iterable
     */
    Iterable<Student> findByLastNameOrFirstNameAllIgnoreCase(String lastName, String firstName);

    /**
     * Find student list by first name matching end without case sensitive
     *
     * @param firstName first name
     * @return Student Iterable
     */
    Iterable<Student> findByFirstNameEndsWithIgnoreCase(String firstName);

    /**
     * Find student list by last name matching end and first name matching end without case sensitive
     *
     * @param lastName last name
     * @param firstName first name
     * @return Student Iterable
     */
    Iterable<Student> findByLastNameStartsWithAndFirstNameStartsWithAllIgnoreCase(String lastName, String firstName);

    /**
     * Find student list by last name matching or and first name matching end without case sensitive
     *
     * @param lastName last name
     * @param firstName first name
     * @return Student Iterable
     */
    Iterable<Student> findByLastNameStartsWithOrFirstNameStartsWithAllIgnoreCase(String lastName, String firstName);
    
    Iterable<Student> findFirstByFirstName(String firstName);
    
    Iterable<Student> findFirst1ByFirstName(String firstName);
    
    Iterable<Student> findFirst2ByFirstName(String firstName);
    
    Iterable<Student> findTopByFirstName(String firstName);
    
    Iterable<Student> findTop1ByFirstName(String firstName);
    
    Iterable<Student> findTop2ByFirstName(String firstName);

}
