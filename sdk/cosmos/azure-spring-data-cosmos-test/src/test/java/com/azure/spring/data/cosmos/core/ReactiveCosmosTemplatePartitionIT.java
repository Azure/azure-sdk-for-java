// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfo;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.domain.PartitionPerson;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveCosmosTemplatePartitionIT {
    private static final PartitionPerson TEST_PERSON = new PartitionPerson(TestConstants.ID_1,
        TestConstants.FIRST_NAME, TestConstants.ZIP_CODE,
        TestConstants.HOBBIES, TestConstants.ADDRESSES);

    private static final PartitionPerson TEST_PERSON_2 = new PartitionPerson(TestConstants.ID_2,
        TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_ZIP_CODE, TestConstants.HOBBIES, TestConstants.ADDRESSES);

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager collectionManager = new ReactiveIntegrationTestCollectionManager();

    private static CosmosFactory cosmosFactory;
    private static ReactiveCosmosTemplate cosmosTemplate;
    private static String containerName;
    private static CosmosEntityInformation<PartitionPerson, String> personInfo;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (cosmosTemplate == null) {
            //  Enable Query plan caching for testing
            System.setProperty("COSMOS.QUERYPLAN_CACHING_ENABLED", "true");
            CosmosAsyncClient client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
            cosmosFactory = new CosmosFactory(client, TestConstants.DB_NAME);

            final CosmosMappingContext mappingContext = new CosmosMappingContext();
            personInfo =
                new CosmosEntityInformation<>(PartitionPerson.class);
            containerName = personInfo.getContainerName();

            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter dbConverter = new MappingCosmosConverter(mappingContext,
                null);
            cosmosTemplate = new ReactiveCosmosTemplate(cosmosFactory, cosmosConfig, dbConverter);
        }
        collectionManager.ensureContainersCreatedAndEmpty(cosmosTemplate, PartitionPerson.class);
        cosmosTemplate.insert(TEST_PERSON).block();
    }

    @Test
    public void testFindWithPartition() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, TestConstants.PROPERTY_ZIP_CODE,
            Collections.singletonList(TestConstants.ZIP_CODE), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);
        final Flux<PartitionPerson> partitionPersonFlux = cosmosTemplate.find(query,
            PartitionPerson.class,
            PartitionPerson.class.getSimpleName());
        StepVerifier.create(partitionPersonFlux).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getZipCode(), is(equalTo(TEST_PERSON.getZipCode())));
        }).verifyComplete();
    }

    @Test
    public void testFindWithPartitionWithQueryPlanCachingEnabled() {
        Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, TestConstants.PROPERTY_ZIP_CODE,
            Collections.singletonList(TestConstants.ZIP_CODE), Part.IgnoreCaseType.NEVER);
        CosmosQuery query = new CosmosQuery(criteria);
        SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        Flux<PartitionPerson> partitionPersonFlux = cosmosTemplate.find(query,
            PartitionPerson.class,
            PartitionPerson.class.getSimpleName());
        StepVerifier.create(partitionPersonFlux).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getZipCode(), is(equalTo(TEST_PERSON.getZipCode())));
        }).verifyComplete();

        CosmosAsyncClient cosmosAsyncClient = cosmosFactory.getCosmosAsyncClient();
        AsyncDocumentClient asyncDocumentClient = CosmosBridgeInternal.getAsyncDocumentClient(cosmosAsyncClient);
        ConcurrentMap<String, PartitionedQueryExecutionInfo> initialCache = asyncDocumentClient.getQueryPlanCache();
        assertThat(initialCache.containsKey(sqlQuerySpec.getQueryText())).isTrue();
        int initialSize = initialCache.size();

        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode())).block();

        //  Fire the same query with different partition key value to make sure query plan caching is enabled
        criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, TestConstants.PROPERTY_ZIP_CODE,
            Collections.singletonList(TestConstants.NEW_ZIP_CODE), Part.IgnoreCaseType.NEVER);
        query = new CosmosQuery(criteria);
        sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(query);
        partitionPersonFlux = cosmosTemplate.find(query,
            PartitionPerson.class,
            PartitionPerson.class.getSimpleName());
        StepVerifier.create(partitionPersonFlux).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON_2.getFirstName())));
            Assert.assertThat(actual.getZipCode(), is(equalTo(TEST_PERSON_2.getZipCode())));
        }).verifyComplete();

        ConcurrentMap<String, PartitionedQueryExecutionInfo> postQueryCallCache = asyncDocumentClient.getQueryPlanCache();
        assertThat(postQueryCallCache.containsKey(sqlQuerySpec.getQueryText())).isTrue();
        assertThat(postQueryCallCache.size()).isEqualTo(initialSize);
    }

    @Test
    public void testFindIgnoreCaseWithPartition() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, TestConstants.PROPERTY_ZIP_CODE,
            Collections.singletonList(TestConstants.ZIP_CODE), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);
        final Flux<PartitionPerson> partitionPersonFlux = cosmosTemplate.find(query,
            PartitionPerson.class,
            PartitionPerson.class.getSimpleName());
        StepVerifier.create(partitionPersonFlux).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getZipCode(), is(equalTo(TEST_PERSON.getZipCode())));
        }).verifyComplete();
    }


    @Test
    public void testFindByIdWithPartition() {
        final Mono<PartitionPerson> partitionPersonMono = cosmosTemplate.findById(TEST_PERSON.getId(),
            PartitionPerson.class,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));
        StepVerifier.create(partitionPersonMono).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getZipCode(), is(equalTo(TEST_PERSON.getZipCode())));
        }).verifyComplete();
    }

    //    @Test
    //    public void testFindByNonExistIdWithPartition() {
    //
    //    }

    @Test
    public void testUpsertNewDocumentPartition() {
        final String firstName = TestConstants.NEW_FIRST_NAME + "_" + UUID.randomUUID().toString();
        final PartitionPerson newPerson = new PartitionPerson(UUID.randomUUID().toString(),
            firstName, TestConstants.NEW_ZIP_CODE,
            null, null);
        final Mono<PartitionPerson> upsert = cosmosTemplate.upsert(newPerson);
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testUpdateWithPartition() {
        final PartitionPerson updated = new PartitionPerson(TEST_PERSON.getId(), TestConstants.UPDATED_FIRST_NAME,
            TEST_PERSON.getZipCode(), TEST_PERSON.getHobbies(),
            TEST_PERSON.getShippingAddresses());
        cosmosTemplate.upsert(updated).block();

        final PartitionPerson person = cosmosTemplate
            .findAll(PartitionPerson.class.getSimpleName(), PartitionPerson.class)
            .toStream()
            .filter(p -> TEST_PERSON.getId().equals(p.getId()))
            .findFirst().get();
        assertEquals(person, updated);
    }

    @Test
    public void testDeleteByIdPartition() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode())).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class)).expectNextCount(2).verifyComplete();

        cosmosTemplate.deleteById(PartitionPerson.class.getSimpleName(),
            TEST_PERSON.getId(), new PartitionKey(TEST_PERSON.getZipCode())).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class))
                    .expectNext(TEST_PERSON_2)
                    .verifyComplete();
    }

    @Test
    public void testDeleteAll() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode())).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class)).expectNextCount(2).verifyComplete();
        cosmosTemplate.deleteAll(containerName, PartitionPerson.class).block();
        StepVerifier.create(cosmosTemplate.findAll(PartitionPerson.class))
                    .expectNextCount(0)
                    .verifyComplete();
    }

    @Test
    public void testCountForPartitionedCollection() {
        StepVerifier.create(cosmosTemplate.count(containerName))
                    .expectNext((long) 1).verifyComplete();
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode())).block();
        StepVerifier.create(cosmosTemplate.count(containerName))
                    .expectNext((long) 2).verifyComplete();
    }

    @Test
    public void testCountForPartitionedCollectionByQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode())).block();
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON_2.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);
        StepVerifier.create(cosmosTemplate.count(query, containerName))
                    .expectNext((long) 1).verifyComplete();
    }

    @Test
    public void testCountIgnoreCaseForPartitionedCollectionByQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode())).block();
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON_2.getFirstName().toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase);
        StepVerifier.create(cosmosTemplate.count(queryIgnoreCase, containerName))
                    .expectNext((long) 1).verifyComplete();
    }
}

