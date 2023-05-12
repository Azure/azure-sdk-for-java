// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Contact;
import com.azure.spring.data.cosmos.domain.Course;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.ContactRepository;
import com.azure.spring.data.cosmos.repository.repository.ReactiveCourseRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CountIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private ContactRepository repository;
    @Autowired
    private ReactiveCourseRepository reactiveRepository;
    @Autowired
    private CosmosTemplate template;

    @Before
    public void setUp() throws ClassNotFoundException {
        collectionManager.ensureContainersCreatedAndEmpty(template, Contact.class, Course.class);
    }

    @Test
    public void testCountByField() {
        Contact contact1 = new Contact("1", "title", 25, true);
        Contact contact2 = new Contact("2", "title", 30, true);
        Contact contact3 = new Contact("3", "title", 30, true);
        Contact contact4 = new Contact("4", "other", 30, true);
        repository.saveAll(Arrays.asList(contact1, contact2, contact3, contact4));

        Assert.assertEquals(3, repository.countByTitle("title"));
        Assert.assertEquals(1, repository.countByTitle("other"));

        Assert.assertEquals(Long.valueOf(1), repository.countByTitleAndIntValue("title", 25));
        Assert.assertEquals(Long.valueOf(2), repository.countByTitleAndIntValue("title", 30));
    }

    @Test
    public void testCountByQuery() {
        Contact contact1 = new Contact("1", "same");
        Contact contact2 = new Contact("2", "same");
        Contact contact3 = new Contact("3", "different");
        repository.saveAll(Arrays.asList(contact1, contact2, contact3));

        Assert.assertEquals(2, repository.countByQueryWithPrimitive("same"));
        Assert.assertEquals(1, repository.countByQueryWithPrimitive("different"));

        Assert.assertEquals(Long.valueOf(2), repository.countByQueryWithNonPrimitive("same"));
        Assert.assertEquals(Long.valueOf(1), repository.countByQueryWithNonPrimitive("different"));
    }

    @Test
    public void testReactiveCountByField() {
        Course course1 = new Course("1", "course", "department");
        Course course2 = new Course("2", "course", "department");
        Course course3 = new Course("3", "course2", "department");
        reactiveRepository.saveAll(Arrays.asList(course1, course2, course3)).blockLast();

        Assert.assertEquals(Long.valueOf(2), reactiveRepository.countByName("course").block());
        Assert.assertEquals(Long.valueOf(1), reactiveRepository.countByName("course2").block());
    }

    @Test
    public void testReactiveCountByQuery() {
        Course course1 = new Course("1", "course", "department");
        Course course2 = new Course("2", "course", "department");
        Course course3 = new Course("3", "course2", "department");
        reactiveRepository.saveAll(Arrays.asList(course1, course2, course3)).blockLast();

        Assert.assertEquals(Long.valueOf(2), reactiveRepository.countByQuery("course").block());
        Assert.assertEquals(Long.valueOf(1), reactiveRepository.countByQuery("course2").block());
    }

}
