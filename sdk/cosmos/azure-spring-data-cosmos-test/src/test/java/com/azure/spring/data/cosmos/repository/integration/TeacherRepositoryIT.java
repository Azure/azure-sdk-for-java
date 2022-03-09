// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Teacher;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.TeacherRepository;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class TeacherRepositoryIT {
    public static final String ID_0 = "id-0";

    public static final String FIRST_NAME_0 = "Moary";

    public static final String LAST_NAME_0 = "Chen";

    private static final Teacher TEACHER_0 = new Teacher(ID_0, FIRST_NAME_0, LAST_NAME_0);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private TeacherRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Teacher.class);
        this.repository.save(TEACHER_0);
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
