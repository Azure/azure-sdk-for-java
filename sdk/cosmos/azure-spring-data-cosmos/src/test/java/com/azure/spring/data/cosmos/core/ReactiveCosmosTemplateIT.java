// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosKeyCredential;
import com.azure.data.cosmos.PartitionKey;
import com.azure.spring.data.cosmos.CosmosDbFactory;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.core.query.DocumentQuery;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.exception.CosmosDBAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveCosmosTemplateIT {
    private static final Person TEST_PERSON = new Person(TestConstants.ID_1,
        TestConstants.FIRST_NAME,
        TestConstants.LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES);

    private static final Person TEST_PERSON_2 = new Person(TestConstants.ID_2,
        TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES);

    private static final Person TEST_PERSON_3 = new Person(TestConstants.ID_3,
        TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES);

    private static final Person TEST_PERSON_4 = new Person(TestConstants.ID_4,
        TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES);

    private static final String PRECONDITION_IS_NOT_MET = "is not met";
    private static final String WRONG_ETAG = "WRONG_ETAG";

    @Value("${cosmosdb.secondaryKey}")
    private String cosmosDbSecondaryKey;

    private static ReactiveCosmosTemplate cosmosTemplate;
    private static String containerName;
    private static CosmosEntityInformation<Person, String> personInfo;
    private static CosmosKeyCredential cosmosKeyCredential;

    private static boolean initialized;

    private Person insertedPerson;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosDBConfig dbConfig;
    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (!initialized) {
            cosmosKeyCredential = new CosmosKeyCredential(dbConfig.getKey());
            final CosmosDbFactory dbFactory = new CosmosDbFactory(dbConfig);

            final CosmosMappingContext mappingContext = new CosmosMappingContext();
            personInfo = new CosmosEntityInformation<>(Person.class);
            containerName = personInfo.getContainerName();

            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter dbConverter =
                new MappingCosmosConverter(mappingContext, null);
            cosmosTemplate = new ReactiveCosmosTemplate(dbFactory, dbConverter, dbConfig.getDatabase());
            cosmosTemplate.createContainerIfNotExists(personInfo).block().container();
            initialized = true;
        }

        insertedPerson = cosmosTemplate.insert(TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON))).block();
    }

    @After
    public void cleanup() {
        //  Reset master key
        cosmosKeyCredential.key(dbConfig.getKey());
        cosmosTemplate.deleteAll(Person.class.getSimpleName(),
            personInfo.getPartitionKeyFieldName()).block();
    }

    @AfterClass
    public static void afterClassCleanup() {
        cosmosTemplate.deleteContainer(personInfo.getContainerName());
    }

    @Test
    public void testInsertDuplicateId() {
        final Mono<Person> insertMono = cosmosTemplate.insert(TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));
        final TestSubscriber<? super Person> testSubscriber = new TestSubscriber<>();
        insertMono.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNotComplete();
        testSubscriber.assertTerminated();
        assertThat(testSubscriber.errors()).hasSize(1);
        assertThat(((List) testSubscriber.getEvents().get(1)).get(0))
            .isInstanceOf(CosmosDBAccessException.class);
    }

    @Test
    public void testFindByID() {
        final Mono<Person> findById = cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(),
            Person.class);
        StepVerifier.create(findById)
                    .consumeNextWith(actual -> Assert.assertEquals(actual, TEST_PERSON))
                    .verifyComplete();
        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindByIDBySecondaryKey() {
        cosmosKeyCredential.key(cosmosDbSecondaryKey);
        final Mono<Person> findById = cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(),
            Person.class);
        StepVerifier.create(findById).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getLastName(), is(equalTo(TEST_PERSON.getLastName())));
        }).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindAll() {
        final Flux<Person> flux = cosmosTemplate.findAll(Person.class.getSimpleName(),
            Person.class);
        StepVerifier.create(flux).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindByIdWithContainerName() {
        StepVerifier.create(cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(), Person.class))
                    .consumeNextWith(actual -> Assert.assertEquals(actual, TEST_PERSON))
                    .verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testInsert() {
        StepVerifier.create(cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3))))
                    .expectNext(TEST_PERSON_3).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();
    }

    @Test
    public void testInsertBySecondaryKey() {
        cosmosKeyCredential.key(cosmosDbSecondaryKey);
        StepVerifier.create(cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3))))
                    .expectNext(TEST_PERSON_3).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();
    }

    @Test
    public void testInsertWithContainerName() {
        StepVerifier.create(cosmosTemplate.insert(Person.class.getSimpleName(), TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2))))
                    .expectNext(TEST_PERSON_2).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();
    }

    @Test
    public void testUpsert() {
        final Person p = TEST_PERSON_2;
        p.set_etag(insertedPerson.get_etag());
        final ArrayList<String> hobbies = new ArrayList<>(p.getHobbies());
        hobbies.add("more code");
        p.setHobbies(hobbies);
        final Mono<Person> upsert = cosmosTemplate.upsert(p,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(p)));
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();
    }

    @Test
    public void testOptimisticLockWhenUpdatingWithWrongEtag() {
        final Person updated = new Person(TEST_PERSON.getId(), TestConstants.UPDATED_FIRST_NAME,
            TEST_PERSON.getLastName(), TEST_PERSON.getHobbies(), TEST_PERSON.getShippingAddresses());
        updated.set_etag(WRONG_ETAG);

        try {
            cosmosTemplate.upsert(updated, new PartitionKey(personInfo.getPartitionKeyFieldValue(updated))).block();
        } catch (CosmosDBAccessException cosmosDbAccessException) {
            assertThat(cosmosDbAccessException.getCosmosClientException()).isNotNull();
            final Throwable cosmosClientException = cosmosDbAccessException.getCosmosClientException();
            assertThat(cosmosClientException).isInstanceOf(CosmosClientException.class);
            assertThat(cosmosClientException.getMessage()).contains(PRECONDITION_IS_NOT_MET);

            final Mono<Person> unmodifiedPerson = cosmosTemplate.findById(Person.class.getSimpleName(),
                TEST_PERSON.getId(), Person.class);
            StepVerifier.create(unmodifiedPerson).expectNextMatches(person ->
                person.getFirstName().equals(insertedPerson.getFirstName())).verifyComplete();
            return;
        }
        fail();
    }

    @Test
    public void testUpsertBySecondaryKey() {
        cosmosKeyCredential.key(cosmosDbSecondaryKey);
        final Person p = TEST_PERSON_2;
        final ArrayList<String> hobbies = new ArrayList<>(p.getHobbies());
        hobbies.add("more code");
        p.setHobbies(hobbies);
        final Mono<Person> upsert = cosmosTemplate.upsert(p,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(p)));
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();
    }

    @Test
    public void testUpsertWithContainerName() {
        final Person p = TEST_PERSON_2;
        final ArrayList<String> hobbies = new ArrayList<>(p.getHobbies());
        hobbies.add("more code");
        p.setHobbies(hobbies);
        final Mono<Person> upsert = cosmosTemplate.upsert(Person.class.getSimpleName(), p,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(p)));
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();
    }

    @Test
    public void testDeleteById() {
        cosmosTemplate.insert(TEST_PERSON_4,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4))).block();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();

        Flux<Person> flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(2).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNull();

        final Mono<Void> voidMono = cosmosTemplate.deleteById(Person.class.getSimpleName(),
            TEST_PERSON_4.getId(),
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4)));
        StepVerifier.create(voidMono).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNotNull();

        flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNull();
    }

    @Test
    public void testDeleteByIdBySecondaryKey() {
        cosmosKeyCredential.key(cosmosDbSecondaryKey);
        cosmosTemplate.insert(TEST_PERSON_4,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4))).block();
        Flux<Person> flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(2).verifyComplete();
        final Mono<Void> voidMono = cosmosTemplate.deleteById(Person.class.getSimpleName(),
            TEST_PERSON_4.getId(),
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4)));
        StepVerifier.create(voidMono).verifyComplete();
        flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testFind() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Arrays.asList(TEST_PERSON.getFirstName()));
        final DocumentQuery query = new DocumentQuery(criteria);
        final Flux<Person> personFlux = cosmosTemplate.find(query, Person.class,
            Person.class.getSimpleName());
        StepVerifier.create(personFlux).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNull();
    }

    @Test
    public void testExists() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Arrays.asList(TEST_PERSON.getFirstName()));
        final DocumentQuery query = new DocumentQuery(criteria);
        final Mono<Boolean> exists = cosmosTemplate.exists(query, Person.class, containerName);
        StepVerifier.create(exists).expectNext(true).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNull();
    }

    @Test
    public void testCount() {
        final Mono<Long> count = cosmosTemplate.count(containerName);
        StepVerifier.create(count).expectNext((long) 1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getFeedResponseDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseDiagnostics()).isNull();
    }

    @Test
    public void testCountBySecondaryKey() {
        cosmosKeyCredential.key(cosmosDbSecondaryKey);
        final Mono<Long> count = cosmosTemplate.count(containerName);
        StepVerifier.create(count).expectNext((long) 1).verifyComplete();
    }

    @Test
    public void testInvalidSecondaryKey() {
        cosmosKeyCredential.key("Invalid secondary key");
        final Mono<Person> findById = cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(),
            Person.class);
        StepVerifier.create(findById).expectError(IllegalArgumentException.class);
    }

}
