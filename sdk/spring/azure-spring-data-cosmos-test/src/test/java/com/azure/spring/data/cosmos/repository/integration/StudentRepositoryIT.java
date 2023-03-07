// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Student;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.StudentRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
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
    public static final String ID_4 = "id-4";
    public static final String ID_5 = "id-5";

    public static final String FIRST_NAME_0 = "Mary";
    public static final String FIRST_NAME_1 = "Cheng";
    public static final String FIRST_NAME_2 = "Zheng";
    public static final String FIRST_NAME_3 = "Zhen";
    public static final String FIRST_NAME_4 = "Jack";

    public static final String LAST_NAME_0 = "Chen";
    public static final String LAST_NAME_1 = "Ch";
    public static final String LAST_NAME_2 = "N";
    public static final String LAST_NAME_3 = "H";
    public static final String LAST_NAME_4 = "lu";

    public static final String SUB_FIRST_NAME = "eng";

    private static final Student STUDENT_0 = new Student(ID_0, FIRST_NAME_0, LAST_NAME_0);
    private static final Student STUDENT_1 = new Student(ID_1, FIRST_NAME_1, LAST_NAME_1);
    private static final Student STUDENT_2 = new Student(ID_2, FIRST_NAME_2, LAST_NAME_2);
    private static final Student STUDENT_3 = new Student(ID_3, FIRST_NAME_3, LAST_NAME_3);
    private static final Student STUDENT_4 = new Student(ID_4, FIRST_NAME_4, LAST_NAME_4);
    private static final Student STUDENT_5 = new Student(ID_5, FIRST_NAME_4, FIRST_NAME_4);
    private static final List<Student> PEOPLE =
        Arrays.asList(STUDENT_0, STUDENT_1, STUDENT_2, STUDENT_3, STUDENT_4, STUDENT_5);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private StudentRepository repository;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(template, Student.class);
        this.repository.saveAll(PEOPLE);
    }

    @Test
    public void testFindByContaining() {
        final List<Student> people = TestUtils.toList(repository.findByFirstNameContaining(SUB_FIRST_NAME));
        final List<Student> reference = Arrays.asList(STUDENT_1, STUDENT_2);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByContainingWithAnd() {
        final List<Student> people = TestUtils.toList(repository
            .findByFirstNameContainingAndLastNameContaining("eng", "h"));
        final List<Student> reference = Arrays.asList(STUDENT_1);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByEndsWith() {
        final List<Student> people = TestUtils.toList(repository.findByFirstNameEndsWith("en"));
        final List<Student> reference = Arrays.asList(STUDENT_3);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByNot() {
        final List<Student> people = TestUtils.toList(repository.findByFirstNameNot("Mary"));
        final List<Student> reference = Arrays.asList(STUDENT_1, STUDENT_2, STUDENT_3, STUDENT_4, STUDENT_5);

        assertPeopleEquals(people, reference);
    }

    @Test
    public void testFindByStartsWith() {
        List<Student> people = TestUtils.toList(repository.findByFirstNameStartsWith("Z"));

        assertPeopleEquals(people, Arrays.asList(STUDENT_2, STUDENT_3));

        people = TestUtils.toList(repository.findByLastNameStartsWith("C"));

        assertPeopleEquals(people, Arrays.asList(STUDENT_0, STUDENT_1));
    }

    @Test
    public void testFindByStartsWithAndEndsWith() {
        List<Student> people = TestUtils.toList(repository.findByFirstNameStartsWithAndLastNameEndingWith("Z", "H"));

        assertPeopleEquals(people, Arrays.asList(STUDENT_3));

        people = TestUtils.toList(repository.findByFirstNameStartsWithAndLastNameEndingWith("Z", "en"));

        assertPeopleEquals(people, Arrays.asList());
    }

    @Test
    public void testFindByStartsWithOrContaining() {
        List<Student> people = TestUtils.toList(repository.findByFirstNameStartsWithOrLastNameContaining("Zhen", "C"));

        assertPeopleEquals(people, Arrays.asList(STUDENT_0, STUDENT_1, STUDENT_2, STUDENT_3));

        people = TestUtils.toList(repository.findByFirstNameStartsWithOrLastNameContaining("M", "N"));

        assertPeopleEquals(people, Arrays.asList(STUDENT_0, STUDENT_2));
    }

    @Test
    public void testFindByContainingAndNot() {
        final List<Student> people = TestUtils.toList(repository.findByFirstNameContainingAndLastNameNot("Zhe", "N"));

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

    @Test
    public void testFindByLastNameIgnoreCase() {
        List<Student> people = TestUtils.toList(repository.findByLastNameIgnoreCase(LAST_NAME_0.toLowerCase()));
        assertPeopleEquals(people, Arrays.asList(STUDENT_0));
        assertTrue(people.get(0).getLastName().equals(LAST_NAME_0));
    }

    @Test
    public void testFindByLastNameAndFirstNameAllIgnoreCase() {
        List<Student> people = TestUtils.toList(repository
            .findByLastNameAndFirstNameAllIgnoreCase(LAST_NAME_0.toLowerCase(), FIRST_NAME_0.toLowerCase()));
        assertPeopleEquals(people, Arrays.asList(STUDENT_0));
        assertTrue(people.get(0).getFirstName().equals(FIRST_NAME_0));
        assertTrue(people.get(0).getLastName().equals(LAST_NAME_0));
    }

    @Test
    public void testFindByLastNameOrFirstNameAllIgnoreCase() {
        List<Student> people = TestUtils.toList(repository
            .findByLastNameOrFirstNameAllIgnoreCase(LAST_NAME_0.toLowerCase(), FIRST_NAME_1.toLowerCase()));
        assertPeopleEquals(people, Arrays.asList(STUDENT_0, STUDENT_1));
    }

    @Test
    public void testFindByFirstNameEndsWithIgnoreCase() {
        List<Student> people = TestUtils.toList(repository
            .findByFirstNameEndsWithIgnoreCase(FIRST_NAME_0.toLowerCase().substring(2)));
        assertPeopleEquals(people, Arrays.asList(STUDENT_0));
        assertTrue(people.get(0).getFirstName().equals(FIRST_NAME_0));
    }

    @Test
    public void testFindByLastNameStartsWithAndFirstNameStartsWithAllIgnoreCase() {
        List<Student> people = TestUtils.toList(repository
            .findByLastNameStartsWithAndFirstNameStartsWithAllIgnoreCase(
                LAST_NAME_0.toLowerCase().substring(0, 2), FIRST_NAME_0.toLowerCase().substring(0, 2)));
        assertPeopleEquals(people, Arrays.asList(STUDENT_0));
        assertTrue(people.get(0).getLastName().equals(LAST_NAME_0));
        assertTrue(people.get(0).getFirstName().equals(FIRST_NAME_0));
    }

    @Test
    public void testFindByLastNameStartsWithOrFirstNameStartsWithAllIgnoreCase() {
        List<Student> people = TestUtils.toList(repository
            .findByLastNameStartsWithOrFirstNameStartsWithAllIgnoreCase(
                LAST_NAME_0.toLowerCase().substring(0, 2), FIRST_NAME_1.toLowerCase().substring(0, 3)));
        assertPeopleEquals(people, Arrays.asList(STUDENT_0, STUDENT_1));
    }

    @Test
    public void testLimitingQuery() {
        List<Student> people = TestUtils.toList(repository.findFirstByFirstName(FIRST_NAME_4));
        assertPeopleEquals(people, Arrays.asList(STUDENT_4));
        people = TestUtils.toList(repository.findFirst1ByFirstName(FIRST_NAME_4));
        assertPeopleEquals(people, Arrays.asList(STUDENT_4));
        people = TestUtils.toList(repository.findFirst2ByFirstName(FIRST_NAME_4));
        assertPeopleEquals(people, Arrays.asList(STUDENT_4, STUDENT_5));

        people = TestUtils.toList(repository.findTopByFirstName(FIRST_NAME_4));
        assertPeopleEquals(people, Arrays.asList(STUDENT_4));
        people = TestUtils.toList(repository.findTop1ByFirstName(FIRST_NAME_4));
        assertPeopleEquals(people, Arrays.asList(STUDENT_4));
        people = TestUtils.toList(repository.findTop2ByFirstName(FIRST_NAME_4));
        assertPeopleEquals(people, Arrays.asList(STUDENT_4, STUDENT_5));
    }
}
