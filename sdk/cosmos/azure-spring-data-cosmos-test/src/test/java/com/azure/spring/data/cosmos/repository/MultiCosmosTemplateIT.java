// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static com.azure.spring.data.cosmos.common.TestConstants.AGE;
import static com.azure.spring.data.cosmos.common.TestConstants.PASSPORT_IDS_BY_COUNTRY;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestRepositoryConfig.class, SecondaryTestRepositoryConfig.class})
public class MultiCosmosTemplateIT {
    private static final Person PRIMARY_TEST_PERSON = new Person(TestConstants.ID_1, TestConstants.FIRST_NAME,
        TestConstants.LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
    private static final Person SECONDARY_TEST_PERSON = new Person(TestConstants.ID_2, TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager primaryCollectionManager = new ReactiveIntegrationTestCollectionManager();

    @Autowired
    @Qualifier("secondaryReactiveCosmosTemplate")
    private ReactiveCosmosTemplate secondaryReactiveCosmosTemplate;
    @Autowired
    @Qualifier("secondaryReactiveCosmosTemplate1")
    private ReactiveCosmosTemplate secondaryDiffDatabaseReactiveCosmosTemplate;
    @Autowired
    @Qualifier("reactiveCosmosTemplate")
    private ReactiveCosmosTemplate primaryReactiveCosmosTemplate;
    private CosmosEntityInformation<Person, String> personInfo = new CosmosEntityInformation<>(Person.class);

    @Test
    public void testPrimaryTemplate() {
        primaryCollectionManager.ensureContainersCreatedAndEmpty(primaryReactiveCosmosTemplate, Person.class);
        primaryReactiveCosmosTemplate.insert(PRIMARY_TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(PRIMARY_TEST_PERSON))).block();
        final Mono<Person> findById = primaryReactiveCosmosTemplate.findById(PRIMARY_TEST_PERSON.getId(), Person.class);
        Assertions.assertThat(findById.block().getFirstName()).isEqualTo(TestConstants.FIRST_NAME);
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
