// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core;

import com.azure.data.cosmos.PartitionKey;
import com.microsoft.azure.spring.data.cosmosdb.CosmosDbFactory;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import com.microsoft.azure.spring.data.cosmosdb.core.convert.MappingCosmosConverter;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosMappingContext;
import com.microsoft.azure.spring.data.cosmosdb.core.query.Criteria;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.domain.PartitionPerson;
import com.microsoft.azure.spring.data.cosmosdb.repository.TestRepositoryConfig;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.UUID;

import static com.microsoft.azure.spring.data.cosmosdb.common.TestConstants.*;
import static com.microsoft.azure.spring.data.cosmosdb.core.query.CriteriaType.IS_EQUAL;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveCosmosTemplatePartitionIT {
    private static final PartitionPerson TEST_PERSON = new PartitionPerson(ID_1, FIRST_NAME, LAST_NAME,
            HOBBIES, ADDRESSES);

    private static final PartitionPerson TEST_PERSON_2 = new PartitionPerson(ID_2, NEW_FIRST_NAME,
            TEST_PERSON.getLastName(), HOBBIES, ADDRESSES);

    private static ReactiveCosmosTemplate cosmosTemplate;
    private static String containerName;
    private static CosmosEntityInformation<PartitionPerson, String> personInfo;

    private static boolean initialized;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosDBConfig dbConfig;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (!initialized) {
            final CosmosDbFactory dbFactory = new CosmosDbFactory(dbConfig);

            final CosmosMappingContext mappingContext = new CosmosMappingContext();
            personInfo =
                new CosmosEntityInformation<>(PartitionPerson.class);
            containerName = personInfo.getContainerName();

            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter dbConverter = new MappingCosmosConverter(mappingContext,
                null);
            cosmosTemplate = new ReactiveCosmosTemplate(dbFactory, dbConverter, dbConfig.getDatabase());
            cosmosTemplate.createContainerIfNotExists(personInfo).block();

            initialized = true;
        }
        cosmosTemplate.insert(TEST_PERSON).block();
    }

    @After
    public void cleanup() {
        cosmosTemplate.deleteAll(PartitionPerson.class.getSimpleName(),
            personInfo.getPartitionKeyFieldName()).block();
    }

    @AfterClass
    public static void afterClassCleanup() {
        cosmosTemplate.deleteContainer(personInfo.getContainerName());
    }

    @Test
    public void testFindWithPartition() {
        final Criteria criteria = Criteria.getInstance(IS_EQUAL, PROPERTY_LAST_NAME,
            Arrays.asList(LAST_NAME));
        final DocumentQuery query = new DocumentQuery(criteria);
        final Flux<PartitionPerson> partitionPersonFlux = cosmosTemplate.find(query,
            PartitionPerson.class,
            PartitionPerson.class.getSimpleName());
        StepVerifier.create(partitionPersonFlux).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getLastName(), is(equalTo(TEST_PERSON.getLastName())));
        }).verifyComplete();
    }


    @Test
    public void testFindByIdWithPartition() {
        final Mono<PartitionPerson> partitionPersonMono = cosmosTemplate.findById(TEST_PERSON.getId(),
            PartitionPerson.class,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));
        StepVerifier.create(partitionPersonMono).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getLastName(), is(equalTo(TEST_PERSON.getLastName())));
        }).verifyComplete();
    }

    //    @Test
    //    public void testFindByNonExistIdWithPartition() {
    //
    //    }

    @Test
    public void testUpsertNewDocumentPartition() {
        final String firstName = NEW_FIRST_NAME + "_" + UUID.randomUUID().toString();
        final PartitionPerson newPerson = new PartitionPerson(UUID.randomUUID().toString(),
            firstName, NEW_LAST_NAME,
            null, null);
        final String partitionKeyValue = newPerson.getLastName();
        final Mono<PartitionPerson> upsert = cosmosTemplate.upsert(newPerson,
            new PartitionKey(partitionKeyValue));
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testUpdateWithPartition() {
        final PartitionPerson updated = new PartitionPerson(TEST_PERSON.getId(), UPDATED_FIRST_NAME,
            TEST_PERSON.getLastName(), TEST_PERSON.getHobbies(),
            TEST_PERSON.getShippingAddresses());
        cosmosTemplate.upsert(updated, new PartitionKey(updated.getLastName())).block();

        final PartitionPerson person = cosmosTemplate
            .findAll(PartitionPerson.class.getSimpleName(), PartitionPerson.class)
            .toStream()
            .filter(p -> TEST_PERSON.getId().equals(p.getId()))
            .findFirst().get();
        assertTrue(person.equals(updated));
    }

    @Test
    public void testDeleteByIdPartition() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName())).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class)).expectNextCount(2).verifyComplete();

        cosmosTemplate.deleteById(PartitionPerson.class.getSimpleName(),
            TEST_PERSON.getId(), new PartitionKey(TEST_PERSON.getLastName())).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class))
                    .expectNext(TEST_PERSON_2)
                    .verifyComplete();
    }

    @Test
    public void testDeleteAll() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName())).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class)).expectNextCount(2).verifyComplete();
        final CosmosEntityInformation<PartitionPerson, String> personInfo =
            new CosmosEntityInformation<>(PartitionPerson.class);
        cosmosTemplate.deleteAll(containerName, personInfo.getPartitionKeyFieldName()).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class))
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    public void testCountForPartitionedCollection() {
        StepVerifier.create(cosmosTemplate.count(containerName))
                    .expectNext((long) 1).verifyComplete();
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName())).block();
        StepVerifier.create(cosmosTemplate.count(containerName))
                    .expectNext((long) 2).verifyComplete();
    }

    @Test
    public void testCountForPartitionedCollectionByQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName())).block();
        final Criteria criteria = Criteria.getInstance(IS_EQUAL, "firstName",
            Arrays.asList(TEST_PERSON_2.getFirstName()));
        final DocumentQuery query = new DocumentQuery(criteria);
        StepVerifier.create(cosmosTemplate.count(query, containerName))
                    .expectNext((long) 1).verifyComplete();

    }
}

