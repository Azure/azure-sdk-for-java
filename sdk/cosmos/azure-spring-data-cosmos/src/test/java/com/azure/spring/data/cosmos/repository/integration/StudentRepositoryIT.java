// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.domain.Student;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.StudentRepository;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class StudentRepositoryIT {
    public static final String ID_0 = "id-0";
    public static final String ID_1 = "id-1";
    public static final String ID_2 = "id-2";
    public static final String ID_3 = "id-3";

    public static final String FIRST_NAME_0 = "Mary";
    public static final String FIRST_NAME_1 = "Cheng";
    public static final String FIRST_NAME_2 = "Zheng";
    public static final String FIRST_NAME_3 = "Zhen";

    public static final String LAST_NAME_0 = "Chen";
    public static final String LAST_NAME_1 = "Ch";
    public static final String LAST_NAME_2 = "N";
    public static final String LAST_NAME_3 = "H";

    public static final String SUB_FIRST_NAME = "eng";

    private static final Student STUDENT_0 = new Student(ID_0, FIRST_NAME_0, LAST_NAME_0);
    private static final Student STUDENT_1 = new Student(ID_1, FIRST_NAME_1, LAST_NAME_1);
    private static final Student STUDENT_2 = new Student(ID_2, FIRST_NAME_2, LAST_NAME_2);
    private static final Student STUDENT_3 = new Student(ID_3, FIRST_NAME_3, LAST_NAME_3);
    private static final List<Student> PEOPLE = Arrays.asList(STUDENT_0, STUDENT_1, STUDENT_2, STUDENT_3);

    private static final CosmosEntityInformation<Student, String> entityInformation =
            new CosmosEntityInformation<>(Student.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private StudentRepository repository;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        this.repository.saveAll(PEOPLE);
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        this.repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testFindByContaining() {
        final List<Student> people = repository.findByFirstNameContaining(SUB_FIRST_NAME);
        final List<Student> reference = Arrays.asList(STUDENT_1, STUDENT_2);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByContainingWithAnd() {
        final List<Student> people = repository.findByFirstNameContainingAndLastNameContaining("eng", "h");
        final List<Student> reference = Arrays.asList(STUDENT_1);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByEndsWith() {
        final List<Student> people = repository.findByFirstNameEndsWith("en");
        final List<Student> reference = Arrays.asList(STUDENT_3);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByNot() {
        final List<Student> people = repository.findByFirstNameNot("Mary");
        final List<Student> reference = Arrays.asList(STUDENT_1, STUDENT_2, STUDENT_3);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByStartsWith() {
        List<Student> people = repository.findByFirstNameStartsWith("Z");

        assertPeopleEquals(people, Arrays.asList(STUDENT_2, STUDENT_3));

        people = repository.findByLastNameStartsWith("C");

        assertPeopleEquals(people, Arrays.asList(STUDENT_0, STUDENT_1));
    }

    @Test
    public void testFindByStartsWithAndEndsWith() {
        List<Student> people = repository.findByFirstNameStartsWithAndLastNameEndingWith("Z", "H");

        assertPeopleEquals(people, Arrays.asList(STUDENT_3));

        people = repository.findByFirstNameStartsWithAndLastNameEndingWith("Z", "en");

        assertPeopleEquals(people, Arrays.asList());
    }

    @Test
    public void testFindByStartsWithOrContaining() {
        List<Student> people = repository.findByFirstNameStartsWithOrLastNameContaining("Zhen", "C");

        assertPeopleEquals(people, PEOPLE);

        people = repository.findByFirstNameStartsWithOrLastNameContaining("M", "N");

        assertPeopleEquals(people, Arrays.asList(STUDENT_0, STUDENT_2));
    }

    @Test
    public void testFindByContainingAndNot() {
        final List<Student> people = repository.findByFirstNameContainingAndLastNameNot("Zhe", "N");

        assertPeopleEquals(people, Arrays.asList(STUDENT_3));
    }

    private void assertPeopleEquals(List<Student> people, List<Student> reference) {
        people.sort(Comparator.comparing(Student::getId));
        reference.sort(Comparator.comparing(Student::getId));

        Assert.assertEquals(reference, people);
    }

    @Test
    public void testExists() {
        assertTrue(repository.existsByFirstName(FIRST_NAME_0));
        assertFalse(repository.existsByFirstName("xxx"));

        assertTrue(repository.existsByLastNameContaining("N"));
        assertFalse(repository.existsByLastNameContaining("X"));
    }
}
