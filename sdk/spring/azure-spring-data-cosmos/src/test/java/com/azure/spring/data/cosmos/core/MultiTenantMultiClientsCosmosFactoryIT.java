// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.repository.MultiTenantMultiClientsTestConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static com.azure.spring.data.cosmos.common.TestConstants.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MultiTenantMultiClientsTestConfig.class})
public class MultiTenantMultiClientsCosmosFactoryIT {
    private final String TENANT_1 = "tenant1";
    private final String TENANT_2 = "tenant2";

    private final Person TEST_PERSON_1 = new Person(ID_1, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
    private final Person TEST_PERSON_2 = new Person(ID_2, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    @Qualifier("primaryCosmosAsyncClient")
    private CosmosAsyncClient primaryClient;
    @Autowired
    @Qualifier("secondaryCosmosAsyncClient")
    private CosmosAsyncClient secondaryClient;

    private MultiTenantMultiClientsCosmosFactory cosmosFactory;
    private CosmosTemplate cosmosTemplate;
    private MultiTenantMultiClientsCosmosFactory.MultiTenantClients clients;
    private CosmosEntityInformation<Person, String> personInfo;

    @Before
    public void setUp() throws ClassNotFoundException {
        clients = new MultiTenantMultiClientsCosmosFactory.MultiTenantClients();
        clients.add(TENANT_1, primaryClient);
        clients.add(TENANT_2, secondaryClient);
        cosmosFactory = new MultiTenantMultiClientsCosmosFactory(clients);
        final CosmosMappingContext mappingContext = new CosmosMappingContext();

        try {
            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));
        } catch (Exception e) {
            Assert.fail();
        }

        final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, null);
        cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, cosmosConverter, null);
        personInfo = new CosmosEntityInformation<>(Person.class);

        cleanUp();
    }

    @After
    public void cleanUp() {
        clients.getAllClientNames().forEach(this::deleteDatabaseIfExists);
    }

    @Test
    public void testWritingToIndividualTenantsInMultiClientMultiTenantSetup() {
        cosmosFactory.tenantId.set(TENANT_1);
        cosmosTemplate.createContainerIfNotExists(personInfo);
        cosmosTemplate.deleteAll(personInfo.getContainerName(), Person.class);

        cosmosFactory.tenantId.set(TENANT_2);
        cosmosTemplate.createContainerIfNotExists(personInfo);
        cosmosTemplate.deleteAll(personInfo.getContainerName(), Person.class);

        cosmosFactory.tenantId.set(TENANT_1);
        cosmosTemplate.insert(TEST_PERSON_1, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_1)));

        cosmosFactory.tenantId.set(TENANT_2);
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_1)));

        cosmosFactory.tenantId.set(TENANT_2);
        List<Person> expectedResultsDB2 = new ArrayList<>();
        expectedResultsDB2.add(TEST_PERSON_2);
        Iterable<Person> iterableDB2 = cosmosTemplate.findAll(personInfo.getContainerName(), Person.class);
        List<Person> resultDB2 = new ArrayList<>();
        iterableDB2.forEach(resultDB2::add);
        Assert.assertEquals(expectedResultsDB2, resultDB2);

        cosmosFactory.tenantId.set(TENANT_1);
        List<Person> expectedResultsDB1 = new ArrayList<>();
        expectedResultsDB1.add(TEST_PERSON_1);
        Iterable<Person> iterableDB1 = cosmosTemplate.findAll(personInfo.getContainerName(), Person.class);
        List<Person> resultDB1 = new ArrayList<>();
        iterableDB1.forEach(resultDB1::add);
        Assert.assertEquals(expectedResultsDB1, resultDB1);
    }

    private void deleteDatabaseIfExists(String dbName) {
        CosmosAsyncDatabase database = clients.get(dbName).get().cosmosAsyncClient.getDatabase(dbName);
        try {
            database.delete().block();
        } catch (CosmosException ex) {
            assertEquals(ex.getStatusCode(), 404);
        }
    }
}
