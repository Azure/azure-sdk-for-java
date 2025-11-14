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
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MultiTenantTestRepositoryConfig.class)
public class MultiTenantDBCosmosFactoryIT {

    private final String testDB1 = "Database1";
    private final String testDB2 = "Database2";

    private final Person TEST_PERSON_1 = new Person(ID_1, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
    private final Person TEST_PERSON_2 = new Person(ID_2, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;

    private MultiTenantDBCosmosFactory cosmosFactory;
    private CosmosTemplate cosmosTemplate;
    private CosmosAsyncClient client;
    private CosmosEntityInformation<Person, String> personInfo;

    @Before
    public void setUp() throws ClassNotFoundException {
        /// Setup
        client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
        cosmosFactory = new MultiTenantDBCosmosFactory(client, testDB1);
        final CosmosMappingContext mappingContext = new CosmosMappingContext();

        try {
            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));
        } catch (Exception e) {
            Assert.fail();
        }

        final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, null);
        cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, cosmosConverter, null);
        personInfo = new CosmosEntityInformation<>(Person.class);
    }

    @Test
    public void testGetDatabaseFunctionality() {
        // Create DB1 and add TEST_PERSON_1 to it
        cosmosTemplate.createContainerIfNotExists(personInfo);
        cosmosTemplate.deleteAll(personInfo.getContainerName(), Person.class);
        assertThat(cosmosFactory.getDatabaseName()).isEqualTo(testDB1);
        cosmosTemplate.insert(TEST_PERSON_1, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_1)));

        // Create DB2 and add TEST_PERSON_2 to it
        cosmosFactory.manuallySetDatabaseName = testDB2;
        cosmosTemplate.createContainerIfNotExists(personInfo);
        cosmosTemplate.deleteAll(personInfo.getContainerName(), Person.class);
        assertThat(cosmosFactory.getDatabaseName()).isEqualTo(testDB2);
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        // Check that DB2 has the correct contents
        List<Person> expectedResultsDB2 = new ArrayList<>();
        expectedResultsDB2.add(TEST_PERSON_2);
        Iterable<Person> iterableDB2 = cosmosTemplate.findAll(personInfo.getContainerName(), Person.class);
        List<Person> resultDB2 = new ArrayList<>();
        iterableDB2.forEach(resultDB2::add);
        Assert.assertEquals(expectedResultsDB2, resultDB2);

        // Check that DB1 has the correct contents
        cosmosFactory.manuallySetDatabaseName = testDB1;
        List<Person> expectedResultsDB1 = new ArrayList<>();
        expectedResultsDB1.add(TEST_PERSON_1);
        Iterable<Person> iterableDB1 = cosmosTemplate.findAll(personInfo.getContainerName(), Person.class);
        List<Person> resultDB1 = new ArrayList<>();
        iterableDB1.forEach(resultDB1::add);
        Assert.assertEquals(expectedResultsDB1, resultDB1);

        //Cleanup
        deleteDatabaseIfExists(testDB1);
        deleteDatabaseIfExists(testDB2);
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
