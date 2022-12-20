// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class MultiTenantDBCosmosFactoryUnitTest {

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

    @Test
    public void testGetDatabaseFunctionality() {
        /// Setup
        CosmosAsyncClient client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
        MultiTenantDBCosmosFactory cf = new MultiTenantDBCosmosFactory(client, testDB1);
        final CosmosMappingContext mappingContext = new CosmosMappingContext();

        try {
            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));
        } catch (Exception e) {
            Assert.fail();
        }

        final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, null);
        CosmosTemplate cosmosTemplate = new CosmosTemplate(cf, cosmosConfig, cosmosConverter, null);
        CosmosEntityInformation<Person, String> personInfo = new CosmosEntityInformation<>(Person.class);

        // Create DB1 and add TEST_PERSON_1 to it
        cosmosTemplate.createContainerIfNotExists(personInfo);
        cosmosTemplate.deleteAll(personInfo.getContainerName(), Person.class);
        assertThat(cf.getDatabaseName()).isEqualTo(testDB1);
        cosmosTemplate.insert(TEST_PERSON_1, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_1)));

        // Create DB2 and add TEST_PERSON_2 to it
        cf.manuallySetDatabaseName = testDB2;
        cosmosTemplate.createContainerIfNotExists(personInfo);
        cosmosTemplate.deleteAll(personInfo.getContainerName(), Person.class);
        assertThat(cf.getDatabaseName()).isEqualTo(testDB2);
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        // Check that DB2 has the correct contents
        List<Person> expectedResultsDB2 = new ArrayList<>();
        expectedResultsDB2.add(TEST_PERSON_2);
        Iterable<Person> iterableDB2 = cosmosTemplate.findAll(personInfo.getContainerName(), Person.class);
        List<Person> resultDB2 = new ArrayList<>();
        iterableDB2.forEach(resultDB2::add);
        Assert.assertEquals(expectedResultsDB2, resultDB2);

        // Check that DB1 has the correct contents
        cf.manuallySetDatabaseName = testDB1;
        List<Person> expectedResultsDB1 = new ArrayList<>();
        expectedResultsDB1.add(TEST_PERSON_1);
        Iterable<Person> iterableDB1 = cosmosTemplate.findAll(personInfo.getContainerName(), Person.class);
        List<Person> resultDB1 = new ArrayList<>();
        iterableDB1.forEach(resultDB1::add);
        Assert.assertEquals(expectedResultsDB1, resultDB1);
    }
}
