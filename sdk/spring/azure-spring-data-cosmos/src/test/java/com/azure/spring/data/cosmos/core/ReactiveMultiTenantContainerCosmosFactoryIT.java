// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.MultiTenantTestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.azure.spring.data.cosmos.common.TestConstants.ADDRESSES;
import static com.azure.spring.data.cosmos.common.TestConstants.AGE;
import static com.azure.spring.data.cosmos.common.TestConstants.FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.HOBBIES;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_1;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_2;
import static com.azure.spring.data.cosmos.common.TestConstants.LAST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.PASSPORT_IDS_BY_COUNTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MultiTenantTestRepositoryConfig.class)
public class ReactiveMultiTenantContainerCosmosFactoryIT {

    private final String testDB1 = "Database1";

    private final String testContainer1= "Container1";
    private final String testContainer2 = "Container2";

    private final Person TEST_PERSON_1 = new Person(ID_1, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
    private final Person TEST_PERSON_2 = new Person(ID_2, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);


    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;

    private MultiTenantContainerCosmosFactory cosmosFactory;
    private ReactiveCosmosTemplate reactiveCosmosTemplate;
    private CosmosAsyncClient client;
    private CosmosEntityInformation<Person, String> personInfo;

    @BeforeEach
    public void setUp() throws ClassNotFoundException {
        /// Setup
        client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
        cosmosFactory = new MultiTenantContainerCosmosFactory(client, testDB1);
        final CosmosMappingContext mappingContext = new CosmosMappingContext();

        try {
            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));
        } catch (Exception e) {
            Assertions.fail();
        }

        final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, null);
        reactiveCosmosTemplate = new ReactiveCosmosTemplate(cosmosFactory, cosmosConfig, cosmosConverter, null);
        personInfo = new CosmosEntityInformation<>(Person.class);
    }

    @Test
    public void testGetContainerFunctionality() {
        // Create testContainer1 and add TEST_PERSON_1 to it
        cosmosFactory.manuallySetContainerName = testContainer1;
        reactiveCosmosTemplate.createContainerIfNotExists(personInfo).block();
        reactiveCosmosTemplate.deleteAll(reactiveCosmosTemplate.getContainerNameOverride(personInfo.getContainerName()), Person.class).block();
        assertThat(cosmosFactory.overrideContainerName()).isEqualTo(testContainer1);
        reactiveCosmosTemplate.insert(TEST_PERSON_1, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_1))).block();

        // Create testContainer1 and add TEST_PERSON_2 to it
        cosmosFactory.manuallySetContainerName = testContainer2;
        reactiveCosmosTemplate.createContainerIfNotExists(personInfo).block();
        reactiveCosmosTemplate.deleteAll(reactiveCosmosTemplate.getContainerNameOverride(personInfo.getContainerName()), Person.class).block();
        assertThat(cosmosFactory.overrideContainerName()).isEqualTo(testContainer2);
        reactiveCosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2))).block();

        // Check that testContainer2 has the correct contents
        List<Person> expectedResultsContainer2 = new ArrayList<>();
        expectedResultsContainer2.add(TEST_PERSON_2);
        Flux<Person> fluxDB2 = reactiveCosmosTemplate.findAll(reactiveCosmosTemplate.getContainerNameOverride(personInfo.getContainerName()), Person.class);
        StepVerifier.create(fluxDB2).expectNextCount(1).verifyComplete();
        List<Person> resultDB2 = new ArrayList<>();
        fluxDB2.toIterable().forEach(resultDB2::add);
        assertEquals(expectedResultsContainer2, resultDB2);

        // Check that testContainer1 has the correct contents
        cosmosFactory.manuallySetContainerName = testContainer1;
        List<Person> expectedResultsContainer1 = new ArrayList<>();
        expectedResultsContainer1.add(TEST_PERSON_1);
        Flux<Person> fluxDB1 = reactiveCosmosTemplate.findAll(reactiveCosmosTemplate.getContainerNameOverride(personInfo.getContainerName()), Person.class);
        StepVerifier.create(fluxDB1).expectNextCount(1).verifyComplete();
        List<Person> resultDB1 = new ArrayList<>();
        fluxDB1.toIterable().forEach(resultDB1::add);
        assertEquals(expectedResultsContainer1, resultDB1);

        //Cleanup
        deleteDatabaseIfExists(testDB1);
    }

    private void deleteDatabaseIfExists(String dbName) {
        CosmosAsyncDatabase database = client.getDatabase(dbName);
        try {
            database.delete().block();
        } catch (CosmosException ex) {
            assertEquals(ex.getStatusCode(), 404);
        }
    }
}
