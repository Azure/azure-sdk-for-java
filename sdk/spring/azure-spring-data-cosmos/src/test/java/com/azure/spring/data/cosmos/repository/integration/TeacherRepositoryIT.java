// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Teacher;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.TeacherRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class TeacherRepositoryIT {
    public static final String ID_0 = "id-0";

    public static final String FIRST_NAME_0 = "Moary";

    public static final String LAST_NAME_0 = "Chen";

    private static final Teacher TEACHER_0 = new Teacher(ID_0, FIRST_NAME_0, LAST_NAME_0);


    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private TeacherRepository repository;

    @BeforeEach
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Teacher.class);
        this.repository.save(TEACHER_0);
    }

    @AfterEach
    public void teardown() {
        collectionManager.deleteContainer(new CosmosEntityInformation<>(Teacher.class));
    }

    @Test
    public void testSaveWithSuppressedNullValue() {
        this.repository.deleteAll();
        String teacherId = ID_0 + "-Other";
        final Teacher teacher = new Teacher(teacherId, FIRST_NAME_0, null);
        Teacher save = repository.save(teacher);
        assertNotNull(save);

        final Optional<Teacher> optionalTeacher = repository.findById(teacherId);
        assertTrue(optionalTeacher.isPresent());
        assertTrue(optionalTeacher.get().getId().equals(teacherId));
        assertTrue(optionalTeacher.get().getFirstName().equals(FIRST_NAME_0));
        assertNull(optionalTeacher.get().getLastName());

        assertTrue(repository.existsByFirstNameIsNotNull());
        assertFalse(repository.existsByLastNameIsNull());
    }
}
