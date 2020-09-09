// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestRepositoryConfig.class, SecondaryTestRepositoryConfig.class})
public class MultiCosmosTemplateIT {
    private static final Person PRIMARY_TEST_PERSON = new Person(TestConstants.ID_1,
        TestConstants.FIRST_NAME,
        TestConstants.LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES);
    private static final Person SECONDARY_TEST_PERSON = new Person(TestConstants.ID_2,
        TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES);
    private static CosmosEntityInformation<Person, String> personInfo;
    private static boolean initialized;
    @Autowired
    @Qualifier("secondaryReactiveCosmosTemplate")
    private ReactiveCosmosTemplate secondaryReactiveCosmosTemplate;
    @Autowired
    @Qualifier("secondaryReactiveCosmosTemplate1")
    private ReactiveCosmosTemplate secondaryDiffDatabaseReactiveCosmosTemplate;
    @Autowired
    @Qualifier("reactiveCosmosTemplate")
    private ReactiveCosmosTemplate primaryReactiveCosmosTemplate;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (!initialized) {
            personInfo = new CosmosEntityInformation<>(Person.class);
            initialized = true;
        }
    }

    @After
    public void cleanup() {
    }

    @AfterClass
    public static void afterClassCleanup() {
    }

    @Test
    public void testPrimaryTemplate() {
        primaryReactiveCosmosTemplate.createContainerIfNotExists(personInfo).block();
        primaryReactiveCosmosTemplate.insert(PRIMARY_TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(PRIMARY_TEST_PERSON))).block();
        final Mono<Person> findById = primaryReactiveCosmosTemplate.findById(PRIMARY_TEST_PERSON.getId(), Person.class);
        Assertions.assertThat(findById.block().getFirstName()).isEqualTo(TestConstants.FIRST_NAME);
        primaryReactiveCosmosTemplate.deleteAll(Person.class.getSimpleName(), Person.class).block();
        primaryReactiveCosmosTemplate.deleteContainer(personInfo.getContainerName());
    }

    @Test
    public void testSecondaryTemplate() {
        secondaryReactiveCosmosTemplate.createContainerIfNotExists(personInfo).block();
        secondaryReactiveCosmosTemplate.insert(SECONDARY_TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(SECONDARY_TEST_PERSON))).block();
        final Mono<Person> findById = secondaryReactiveCosmosTemplate.findById(SECONDARY_TEST_PERSON.getId(), Person.class);
        Assertions.assertThat(findById.block().getFirstName()).isEqualTo(TestConstants.NEW_FIRST_NAME);
        secondaryReactiveCosmosTemplate.deleteAll(Person.class.getSimpleName(), Person.class).block();
        secondaryReactiveCosmosTemplate.deleteContainer(personInfo.getContainerName());
    }

    @Test
    public void testSecondaryTemplateWithDiffDatabase() {
        secondaryDiffDatabaseReactiveCosmosTemplate.createContainerIfNotExists(personInfo).block();
        secondaryDiffDatabaseReactiveCosmosTemplate.insert(SECONDARY_TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(SECONDARY_TEST_PERSON))).block();
        final Mono<Person> findById = secondaryDiffDatabaseReactiveCosmosTemplate.findById(SECONDARY_TEST_PERSON.getId(), Person.class);
        Assertions.assertThat(findById.block().getFirstName()).isEqualTo(TestConstants.NEW_FIRST_NAME);
        secondaryDiffDatabaseReactiveCosmosTemplate.deleteAll(Person.class.getSimpleName(), Person.class).block();
        secondaryDiffDatabaseReactiveCosmosTemplate.deleteContainer(personInfo.getContainerName());
    }

    @Test
    public void testSingleCosmosClientForMultipleCosmosTemplate() throws IllegalAccessException {
        final Field cosmosAsyncClient = FieldUtils.getDeclaredField(ReactiveCosmosTemplate.class,
            "cosmosAsyncClient", true);
        CosmosAsyncClient client1 = (CosmosAsyncClient) cosmosAsyncClient.get(secondaryReactiveCosmosTemplate);
        CosmosAsyncClient client2 = (CosmosAsyncClient) cosmosAsyncClient.get(secondaryDiffDatabaseReactiveCosmosTemplate);
        Assertions.assertThat(client1).isEqualTo(client2);
    }
}
