// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.ReactiveIntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.domain.AutoScaleSample;
import com.azure.spring.data.cosmos.domain.GenIdEntity;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AuditableRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static com.azure.spring.data.cosmos.common.TestConstants.ADDRESSES;
import static com.azure.spring.data.cosmos.common.TestConstants.AGE;
import static com.azure.spring.data.cosmos.common.TestConstants.FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.HOBBIES;
import static com.azure.spring.data.cosmos.common.TestConstants.LAST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.PASSPORT_IDS_BY_COUNTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ReactiveCosmosTemplateIT {
    private static final Person TEST_PERSON = new Person(TestConstants.ID_1, TestConstants.FIRST_NAME,
        TestConstants.LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final Person TEST_PERSON_2 = new Person(TestConstants.ID_2, TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final Person TEST_PERSON_3 = new Person(TestConstants.ID_3, TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final Person TEST_PERSON_4 = new Person(TestConstants.ID_4, TestConstants.NEW_FIRST_NAME,
        TestConstants.NEW_LAST_NAME, TestConstants.HOBBIES, TestConstants.ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final String PRECONDITION_IS_NOT_MET = "is not met";
    private static final String WRONG_ETAG = "WRONG_ETAG";

    @ClassRule
    public static final ReactiveIntegrationTestCollectionManager collectionManager = new ReactiveIntegrationTestCollectionManager();

    @Value("${cosmos.secondaryKey}")
    private String cosmosDbSecondaryKey;

    @Value("${cosmos.key}")
    private String cosmosDbKey;

    private static CosmosAsyncClient client;
    private static ReactiveCosmosTemplate cosmosTemplate;
    private static String containerName;
    private static CosmosEntityInformation<Person, String> personInfo;
    private static AzureKeyCredential azureKeyCredential;

    private Person insertedPerson;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;
    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;
    @Autowired
    private AuditableRepository auditableRepository;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (cosmosTemplate == null) {
            azureKeyCredential = new AzureKeyCredential(cosmosDbKey);
            cosmosClientBuilder.credential(azureKeyCredential);
            client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
            personInfo = new CosmosEntityInformation<>(Person.class);
            containerName = personInfo.getContainerName();
            cosmosTemplate = createReactiveCosmosTemplate(cosmosConfig, TestConstants.DB_NAME);
        }

        collectionManager.ensureContainersCreatedAndEmpty(cosmosTemplate, Person.class, GenIdEntity.class, AuditableEntity.class);

        insertedPerson = cosmosTemplate.insert(TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON))).block();
    }

    private ReactiveCosmosTemplate createReactiveCosmosTemplate(CosmosConfig config, String dbName) throws ClassNotFoundException {
        final CosmosFactory cosmosFactory = new CosmosFactory(client, dbName);
        final CosmosMappingContext mappingContext = new CosmosMappingContext();
        mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));
        final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, null);
        return new ReactiveCosmosTemplate(cosmosFactory, config, cosmosConverter);
    }

    @After
    public void cleanup() {
        //  Reset master key
        azureKeyCredential.update(cosmosDbKey);
    }

    @Test
    public void testInsertDuplicateId() {
        final Mono<Person> insertMono = cosmosTemplate.insert(TEST_PERSON,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));
        StepVerifier.create(insertMono)
                    .expectErrorMatches(ex -> ex instanceof CosmosAccessException && ((CosmosAccessException) ex).getCosmosException() instanceof ConflictException)
                    .verify();
    }

    @Test
    public void testFindByID() {
        final Mono<Person> findById = cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(),
            Person.class);
        StepVerifier.create(findById)
                    .consumeNextWith(actual -> Assert.assertEquals(actual, TEST_PERSON))
                    .verifyComplete();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindByIDBySecondaryKey() {
        azureKeyCredential.update(cosmosDbSecondaryKey);
        final Mono<Person> findById = cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(),
            Person.class);
        StepVerifier.create(findById).consumeNextWith(actual -> {
            Assert.assertThat(actual.getFirstName(), is(equalTo(TEST_PERSON.getFirstName())));
            Assert.assertThat(actual.getLastName(), is(equalTo(TEST_PERSON.getLastName())));
        }).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindAll() {
        final Flux<Person> flux = cosmosTemplate.findAll(Person.class.getSimpleName(),
            Person.class);
        StepVerifier.create(flux).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindByIdWithContainerName() {
        StepVerifier.create(cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(), Person.class))
                    .consumeNextWith(actual -> Assert.assertEquals(actual, TEST_PERSON))
                    .verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testInsert() {
        StepVerifier.create(cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3))))
                    .expectNext(TEST_PERSON_3).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
    }

    @Test
    public void testInsertBySecondaryKey() {
        azureKeyCredential.update(cosmosDbSecondaryKey);
        StepVerifier.create(cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3))))
                    .expectNext(TEST_PERSON_3).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
    }

    @Test
    public void testInsertWithContainerName() {
        StepVerifier.create(cosmosTemplate.insert(Person.class.getSimpleName(), TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2))))
                    .expectNext(TEST_PERSON_2).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
    }

    @Test
    public void testInsertShouldFailIfColumnNotAnnotatedWithAutoGenerate() {
        final Person person = new Person(null, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        Mono<GenIdEntity> entityMono = cosmosTemplate.insert(Person.class.getSimpleName(),
            person, new PartitionKey(person.getLastName()));
        StepVerifier.create(entityMono).verifyError(CosmosAccessException.class);

    }

    @Test
    public void testInsertShouldGenerateIdIfColumnAnnotatedWithAutoGenerate() {
        final GenIdEntity entity = new GenIdEntity(null, "foo");
        final Mono<GenIdEntity> insertedEntityMono = cosmosTemplate.insert(GenIdEntity.class.getSimpleName(),
            entity, null);
        GenIdEntity insertedEntity = insertedEntityMono.block();
        assertThat(insertedEntity).isNotNull();
        assertThat(insertedEntity.getId()).isNotNull();
    }

    @Test
    public void testUpsert() {
        final Person p = TEST_PERSON_2;
        p.set_etag(insertedPerson.get_etag());
        final ArrayList<String> hobbies = new ArrayList<>(p.getHobbies());
        hobbies.add("more code");
        p.setHobbies(hobbies);
        final Mono<Person> upsert = cosmosTemplate.upsert(p);
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
    }

    @Test
    public void testOptimisticLockWhenUpdatingWithWrongEtag() {
        final Person updated = new Person(TEST_PERSON.getId(), TestConstants.UPDATED_FIRST_NAME,
            TEST_PERSON.getLastName(), TEST_PERSON.getHobbies(), TEST_PERSON.getShippingAddresses(),
            AGE, PASSPORT_IDS_BY_COUNTRY);
        updated.set_etag(WRONG_ETAG);

        try {
            cosmosTemplate.upsert(updated).block();
        } catch (CosmosAccessException cosmosAccessException) {
            assertThat(cosmosAccessException.getCosmosException()).isNotNull();
            final Throwable cosmosClientException = cosmosAccessException.getCosmosException();
            assertThat(cosmosClientException).isInstanceOf(CosmosException.class);
            assertThat(cosmosClientException.getMessage()).contains(PRECONDITION_IS_NOT_MET);

            final Mono<Person> unmodifiedPerson =
                cosmosTemplate.findById(Person.class.getSimpleName(),
                TEST_PERSON.getId(), Person.class);
            StepVerifier.create(unmodifiedPerson).expectNextMatches(person ->
                person.getFirstName().equals(insertedPerson.getFirstName())).verifyComplete();
            return;
        }
        fail();
    }

    @Test
    public void testUpsertBySecondaryKey() {
        azureKeyCredential.update(cosmosDbSecondaryKey);
        final Person p = TEST_PERSON_2;
        final ArrayList<String> hobbies = new ArrayList<>(p.getHobbies());
        hobbies.add("more code");
        p.setHobbies(hobbies);
        final Mono<Person> upsert = cosmosTemplate.upsert(p);
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
    }

    @Test
    public void testUpsertWithContainerName() {
        final Person p = TEST_PERSON_2;
        final ArrayList<String> hobbies = new ArrayList<>(p.getHobbies());
        hobbies.add("more code");
        p.setHobbies(hobbies);
        final Mono<Person> upsert = cosmosTemplate.upsert(Person.class.getSimpleName(), p);
        StepVerifier.create(upsert).expectNextCount(1).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
    }

    @Test
    public void testDeleteById() {
        cosmosTemplate.insert(TEST_PERSON_4,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4))).block();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();

        Flux<Person> flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(2).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);


        final Mono<Void> voidMono = cosmosTemplate.deleteById(Person.class.getSimpleName(),
            TEST_PERSON_4.getId(),
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4)));
        StepVerifier.create(voidMono).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();

        flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

    }

    @Test
    public void testDeleteByEntity() {
        Person insertedPerson = cosmosTemplate.insert(TEST_PERSON_4,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4))).block();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();

        Flux<Person> flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(2).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);


        final Mono<Void> voidMono = cosmosTemplate.deleteEntity(Person.class.getSimpleName(), insertedPerson);
        StepVerifier.create(voidMono).verifyComplete();


        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();

        flux = cosmosTemplate.findAll(Person.class.getSimpleName(), Person.class);
        StepVerifier.create(flux).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

    }

    @Test
    public void testDeleteByIdBySecondaryKey() {
        azureKeyCredential.update(cosmosDbSecondaryKey);
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
            Collections.singletonList(TEST_PERSON.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);
        final Flux<Person> personFlux = cosmosTemplate.find(query, Person.class,
            Person.class.getSimpleName());
        StepVerifier.create(personFlux).expectNextCount(1).verifyComplete();

        // add ignore testing
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName().toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase);
        final Flux<Person> personFluxIgnoreCase = cosmosTemplate.find(queryIgnoreCase, Person.class,
            Person.class.getSimpleName());
        StepVerifier.create(personFluxIgnoreCase).expectNextCount(1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testExists() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);
        final Mono<Boolean> exists = cosmosTemplate.exists(query, Person.class, containerName);
        StepVerifier.create(exists).expectNext(true).verifyComplete();

        // add ignore testing
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName().toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase);
        final Mono<Boolean> existsIgnoreCase = cosmosTemplate.exists(queryIgnoreCase, Person.class, containerName);
        StepVerifier.create(existsIgnoreCase).expectNext(true).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testCount() {
        final Mono<Long> count = cosmosTemplate.count(containerName);
        StepVerifier.create(count).expectNext((long) 1).verifyComplete();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        Assertions.assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testCountBySecondaryKey() {
        azureKeyCredential.update(cosmosDbSecondaryKey);
        final Mono<Long> count = cosmosTemplate.count(containerName);
        StepVerifier.create(count).expectNext((long) 1).verifyComplete();
    }

    @Test
    public void testInvalidSecondaryKey() {
        azureKeyCredential.update("Invalid secondary key");
        final Mono<Person> findById = cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(),
            Person.class);
        StepVerifier.create(findById)
                    .expectError(CosmosAccessException.class)
                    .verify();
    }

    @Test
    public void testRunQueryWithSimpleReturnType() {
        Criteria ageBetween = Criteria.getInstance(CriteriaType.BETWEEN, "age", Arrays.asList(AGE - 1, AGE + 1),
                                                   Part.IgnoreCaseType.NEVER);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(ageBetween));
        final Flux<Person> flux = cosmosTemplate.runQuery(sqlQuerySpec, Person.class, Person.class);

        StepVerifier.create(flux).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testRunQueryWithReturnTypeContainingLocalDateTime() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());

        auditableRepository.save(entity);

        Criteria equals = Criteria.getInstance(CriteriaType.IS_EQUAL, "id", Collections.singletonList(entity.getId()), Part.IgnoreCaseType.NEVER);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(equals));
        final Flux<AuditableEntity> flux = cosmosTemplate.runQuery(sqlQuerySpec, AuditableEntity.class, AuditableEntity.class);

        StepVerifier.create(flux).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testFindWithEqualCriteriaContainingNestedProperty() {
        String postalCode = ADDRESSES.get(0).getPostalCode();
        String subjectWithNestedProperty = "shippingAddresses[0]['postalCode']";
        Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, subjectWithNestedProperty,
            Collections.singletonList(postalCode), Part.IgnoreCaseType.NEVER);

        final Flux<Person> people = cosmosTemplate.find(new CosmosQuery(criteria), Person.class, containerName);

        StepVerifier.create(people).expectNextCount(1).verifyComplete();
    }

    @Test
    public void testRunQueryWithEqualCriteriaContainingSpecialChars() {
        String ivoryCoastPassportId = PASSPORT_IDS_BY_COUNTRY.get("Côte d'Ivoire");
        String subjectWithSpecialChars = "passportIdsByCountry[\"Côte d'Ivoire\"]";
        Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, subjectWithSpecialChars,
            Collections.singletonList(ivoryCoastPassportId), Part.IgnoreCaseType.NEVER);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(criteria));

        final Flux<Person> people = cosmosTemplate.runQuery(sqlQuerySpec, Person.class, Person.class);

        StepVerifier.create(people).expectNextCount(1).verifyComplete();
    }

    @Test
    public void createWithAutoscale() {
        final CosmosEntityInformation<AutoScaleSample, String> autoScaleSampleInfo =
            new CosmosEntityInformation<>(AutoScaleSample.class);
        CosmosContainerResponse containerResponse = cosmosTemplate
            .createContainerIfNotExists(autoScaleSampleInfo)
            .block();
        assertNotNull(containerResponse);
        ThroughputResponse throughput = client.getDatabase(TestConstants.DB_NAME)
            .getContainer(autoScaleSampleInfo.getContainerName())
            .readThroughput()
            .block();
        assertNotNull(throughput);
        assertEquals(Integer.parseInt(TestConstants.AUTOSCALE_MAX_THROUGHPUT),
            throughput.getProperties().getAutoscaleMaxThroughput());
    }

    @Test
    public void createDatabaseWithThroughput() throws ClassNotFoundException {
        final String configuredThroughputDbName = TestConstants.DB_NAME + "-other";
        deleteDatabaseIfExists(configuredThroughputDbName);

        Integer expectedRequestUnits = 700;
        final CosmosConfig config = CosmosConfig.builder()
            .enableDatabaseThroughput(false, expectedRequestUnits)
            .build();
        final ReactiveCosmosTemplate configuredThroughputCosmosTemplate = createReactiveCosmosTemplate(config, configuredThroughputDbName);

        final CosmosEntityInformation<Person, String> personInfo =
            new CosmosEntityInformation<>(Person.class);
        configuredThroughputCosmosTemplate.createContainerIfNotExists(personInfo).block();

        final CosmosAsyncDatabase database = client.getDatabase(configuredThroughputDbName);
        final ThroughputResponse response = database.readThroughput().block();
        assertEquals(expectedRequestUnits, response.getProperties().getManualThroughput());
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
