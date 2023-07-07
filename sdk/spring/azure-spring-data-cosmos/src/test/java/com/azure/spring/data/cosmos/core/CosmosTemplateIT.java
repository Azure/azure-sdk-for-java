// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.PageTestUtils;
import com.azure.spring.data.cosmos.common.PropertyLoader;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.domain.AutoScaleSample;
import com.azure.spring.data.cosmos.domain.BasicItem;
import com.azure.spring.data.cosmos.domain.GenIdEntity;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.azure.spring.data.cosmos.common.TestConstants.ADDRESSES;
import static com.azure.spring.data.cosmos.common.TestConstants.AGE;
import static com.azure.spring.data.cosmos.common.TestConstants.FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.HOBBIES;
import static com.azure.spring.data.cosmos.common.TestConstants.HOBBY1;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_1;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_2;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_3;
import static com.azure.spring.data.cosmos.common.TestConstants.LAST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.NEW_FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.NEW_LAST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.NEW_PASSPORT_IDS_BY_COUNTRY;
import static com.azure.spring.data.cosmos.common.TestConstants.NOT_EXIST_ID;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_1;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_2;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_3;
import static com.azure.spring.data.cosmos.common.TestConstants.PASSPORT_IDS_BY_COUNTRY;
import static com.azure.spring.data.cosmos.common.TestConstants.PATCH_AGE_1;
import static com.azure.spring.data.cosmos.common.TestConstants.PATCH_AGE_INCREMENT;
import static com.azure.spring.data.cosmos.common.TestConstants.PATCH_FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.PATCH_HOBBIES;
import static com.azure.spring.data.cosmos.common.TestConstants.PATCH_HOBBY1;
import static com.azure.spring.data.cosmos.common.TestConstants.UPDATED_FIRST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosTemplateIT {
    private static final Person TEST_PERSON = new Person(ID_1, FIRST_NAME, LAST_NAME, HOBBIES,
        ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final Person TEST_PERSON_2 = new Person(ID_2, NEW_FIRST_NAME, NEW_LAST_NAME, HOBBIES,
        ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final Person TEST_PERSON_3 = new Person(ID_3, NEW_FIRST_NAME, NEW_LAST_NAME, HOBBIES,
        ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final BasicItem BASIC_ITEM = new BasicItem(ID_1);

    private static final String PRECONDITION_IS_NOT_MET = "is not met";

    private static final String WRONG_ETAG = "WRONG_ETAG";

    private static final String INVALID_ID = "http://xxx.html";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonNode NEW_PASSPORT_IDS_BY_COUNTRY_JSON = OBJECT_MAPPER.convertValue(NEW_PASSPORT_IDS_BY_COUNTRY, JsonNode.class);

    private static final CosmosPatchOperations operations = CosmosPatchOperations
        .create()
        .replace("/age", PATCH_AGE_1);

    CosmosPatchOperations multiPatchOperations = CosmosPatchOperations
        .create()
        .set("/firstName", PATCH_FIRST_NAME)
        .replace("/passportIdsByCountry", NEW_PASSPORT_IDS_BY_COUNTRY_JSON)
        .add("/hobbies/2", PATCH_HOBBY1)
        .remove("/shippingAddresses/1")
        .increment("/age", PATCH_AGE_INCREMENT);

    private static final CosmosPatchItemRequestOptions options = new CosmosPatchItemRequestOptions();


    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    private static CosmosAsyncClient client;
    private static CosmosTemplate cosmosTemplate;
    private static CosmosEntityInformation<Person, String> personInfo;
    private static String containerName;

    private MappingCosmosConverter cosmosConverter;

    private Person insertedPerson;

    private BasicItem pointReadItem;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    public CosmosTemplateIT() throws JsonProcessingException {
    }

    @Before
    public void setUp() throws ClassNotFoundException {
        if (cosmosTemplate == null) {
            client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
            personInfo = new CosmosEntityInformation<>(Person.class);
            containerName = personInfo.getContainerName();
            cosmosTemplate = createCosmosTemplate(cosmosConfig, TestConstants.DB_NAME);
        }

        collectionManager.ensureContainersCreatedAndEmpty(cosmosTemplate, Person.class,
                                                          GenIdEntity.class, AuditableEntity.class, BasicItem.class);
        insertedPerson = cosmosTemplate.insert(Person.class.getSimpleName(), TEST_PERSON,
            new PartitionKey(TEST_PERSON.getLastName()));
        pointReadItem = cosmosTemplate.insert(BasicItem.class.getSimpleName(), BASIC_ITEM,
            new PartitionKey(BASIC_ITEM.getId()));
    }

    private CosmosTemplate createCosmosTemplate(CosmosConfig config, String dbName) throws ClassNotFoundException {
        final CosmosFactory cosmosFactory = new CosmosFactory(client, dbName);
        final CosmosMappingContext mappingContext = new CosmosMappingContext();
        mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));
        cosmosConverter = new MappingCosmosConverter(mappingContext, null);
        return new CosmosTemplate(cosmosFactory, config, cosmosConverter);
    }

    private void insertPerson(Person person) {
        cosmosTemplate.insert(person,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(person)));
    }

    @Test
    public void testInsertDuplicateIdShouldFailWithConflictException() {
        try {
            cosmosTemplate.insert(Person.class.getSimpleName(), TEST_PERSON,
                new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));
            fail();
        } catch (CosmosAccessException ex) {
            assertThat(ex.getCosmosException().getStatusCode()).isEqualTo(TestConstants.CONFLICT_STATUS_CODE);
            assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        }
    }


    @Test(expected = CosmosAccessException.class)
    public void testInsertShouldFailIfColumnNotAnnotatedWithAutoGenerate() {
        final Person person = new Person(null, FIRST_NAME, LAST_NAME, HOBBIES, ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        cosmosTemplate.insert(Person.class.getSimpleName(), person, new PartitionKey(person.getLastName()));
    }

    @Test
    public void testInsertShouldGenerateIdIfColumnAnnotatedWithAutoGenerate() {
        final GenIdEntity entity = new GenIdEntity(null, "foo");
        final GenIdEntity insertedEntity = cosmosTemplate.insert(GenIdEntity.class.getSimpleName(),
            entity, null);
        assertThat(insertedEntity.getId()).isNotNull();
    }

    @Test
    public void testFindAll() {
        final List<Person> result = TestUtils.toList(cosmosTemplate.findAll(Person.class.getSimpleName(),
            Person.class));
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(TEST_PERSON);
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testDiagnosticsLogged() {
        TestRepositoryConfig.capturingLogger.loggedMessages = new ArrayList<>();
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));
        final List<Person> result = TestUtils.toList(cosmosTemplate.findAll(Person.class.getSimpleName(),
            Person.class));
        assertTrue(TestRepositoryConfig.capturingLogger.getLoggedMessages().size() > 0);
    }

    public void testFindByIdPointRead() {
        final BasicItem result = cosmosTemplate.findById(BasicItem.class.getSimpleName(),
            BASIC_ITEM.getId(), BasicItem.class);
        assertEquals(result, BASIC_ITEM);
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();
        final BasicItem nullResult = cosmosTemplate.findById(BasicItem.class.getSimpleName(),
            NOT_EXIST_ID, BasicItem.class);
        assertThat(nullResult).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics().toString().contains("\"requestOperationType\":\"Read\"")).isTrue();
    }

    @Test
    public void testFindById() {
        final Person result = cosmosTemplate.findById(Person.class.getSimpleName(),
            TEST_PERSON.getId(), Person.class);
        assertEquals(result, TEST_PERSON);
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Person nullResult = cosmosTemplate.findById(Person.class.getSimpleName(),
            NOT_EXIST_ID, Person.class);
        assertThat(nullResult).isNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
    }

    @Test
    public void testFindByIdWithInvalidId() {
        try {
            cosmosTemplate.findById(BasicItem.class.getSimpleName(),
                INVALID_ID, BasicItem.class);
            fail();
        } catch (CosmosAccessException ex) {
            assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        }
    }

    @Test
    public void testFindByMultiIds() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));

        final List<Object> ids = Lists.newArrayList(ID_1, ID_2, ID_3);
        final List<Person> result = TestUtils.toList(cosmosTemplate.findByIds(ids, Person.class, containerName));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final List<Person> expected = Lists.newArrayList(TEST_PERSON, TEST_PERSON_2, TEST_PERSON_3);
        assertThat(result.size()).isEqualTo(expected.size());
        assertThat(result).containsAll(expected);
    }

    @Test
    public void testUpsertNewDocument() {
        // Delete first as was inserted in setup
        cosmosTemplate.deleteById(Person.class.getSimpleName(), TEST_PERSON.getId(),
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));

        final String firstName = NEW_FIRST_NAME
            + "_"
            + UUID.randomUUID();
        final Person newPerson = new Person(TEST_PERSON.getId(), firstName, NEW_FIRST_NAME, null, null,
            AGE, PASSPORT_IDS_BY_COUNTRY);

        final Person person = cosmosTemplate.upsertAndReturnEntity(Person.class.getSimpleName(), newPerson);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        assertEquals(person.getFirstName(), firstName);
    }

    @Test
    public void testUpdateWithReturnEntity() {
        final Person updated = new Person(TEST_PERSON.getId(), UPDATED_FIRST_NAME,
            TEST_PERSON.getLastName(), TEST_PERSON.getHobbies(), TEST_PERSON.getShippingAddresses(),
            AGE, PASSPORT_IDS_BY_COUNTRY);
        updated.set_etag(insertedPerson.get_etag());

        final Person updatedPerson = cosmosTemplate.upsertAndReturnEntity(Person.class.getSimpleName(), updated);

        final Person findPersonById = cosmosTemplate.findById(Person.class.getSimpleName(),
            updatedPerson.getId(), Person.class);

        assertEquals(updatedPerson, updated);
        assertThat(updatedPerson.get_etag()).isEqualTo(findPersonById.get_etag());
    }

    @Test
    public void testUpdate() {
        final Person updated = new Person(TEST_PERSON.getId(), UPDATED_FIRST_NAME,
            TEST_PERSON.getLastName(), TEST_PERSON.getHobbies(), TEST_PERSON.getShippingAddresses(),
            AGE, PASSPORT_IDS_BY_COUNTRY);
        updated.set_etag(insertedPerson.get_etag());

        final Person person = cosmosTemplate.upsertAndReturnEntity(Person.class.getSimpleName(), updated);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        assertEquals(person, updated);
    }

    @Test
    public void testPatch() {
        Person patchedPerson = cosmosTemplate.patch(insertedPerson.getId(), new PartitionKey(insertedPerson.getLastName()), Person.class, operations);
        assertEquals(patchedPerson.getAge(), PATCH_AGE_1);
    }

    @Test
    public void testPatchMultiOperations() {
        Person patchedPerson = cosmosTemplate.patch(insertedPerson.getId(), new PartitionKey(insertedPerson.getLastName()), Person.class, multiPatchOperations);
        assertEquals(patchedPerson.getAge().intValue(), (AGE + PATCH_AGE_INCREMENT));
        assertEquals(patchedPerson.getHobbies(), PATCH_HOBBIES);
        assertEquals(patchedPerson.getFirstName(), PATCH_FIRST_NAME);
        assertEquals(patchedPerson.getShippingAddresses().size(), 1);
        assertEquals(patchedPerson.getPassportIdsByCountry(), NEW_PASSPORT_IDS_BY_COUNTRY);
    }

    @Test
    public void testPatchPreConditionSuccess() {
        options.setFilterPredicate("FROM person p WHERE p.lastName = '"+LAST_NAME+"'");
        Person patchedPerson = cosmosTemplate.patch(insertedPerson.getId(), new PartitionKey(insertedPerson.getLastName()), Person.class, operations, options);
        assertEquals(patchedPerson.getAge(), PATCH_AGE_1);
    }

    @Test
    public void testPatchPreConditionFail() {
        try {
            options.setFilterPredicate("FROM person p WHERE p.lastName = 'dummy'");
            Person patchedPerson = cosmosTemplate.patch(insertedPerson.getId(), new PartitionKey(insertedPerson.getLastName()), Person.class,  operations, options);
            assertEquals(patchedPerson.getAge(), PATCH_AGE_1);
            fail();
        } catch (CosmosAccessException ex) {
            assertThat(ex.getCosmosException().getStatusCode()).isEqualTo(TestConstants.PRECONDITION_FAILED_STATUS_CODE);
            assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        }
    }

    @Test
    public void testOptimisticLockWhenUpdatingWithWrongEtag() {
        final Person updated = new Person(TEST_PERSON.getId(), UPDATED_FIRST_NAME,
            TEST_PERSON.getLastName(), TEST_PERSON.getHobbies(), TEST_PERSON.getShippingAddresses(),
            AGE, PASSPORT_IDS_BY_COUNTRY);
        updated.set_etag(WRONG_ETAG);

        try {
            cosmosTemplate.upsert(Person.class.getSimpleName(), updated);
        } catch (CosmosAccessException e) {
            assertThat(e.getCosmosException()).isNotNull();
            final Throwable cosmosClientException = e.getCosmosException();
            assertThat(cosmosClientException).isInstanceOf(CosmosException.class);
            assertThat(cosmosClientException.getMessage()).contains(PRECONDITION_IS_NOT_MET);
            assertThat(responseDiagnosticsTestUtils.getDiagnostics()).isNotNull();

            final Person unmodifiedPerson = cosmosTemplate.findById(Person.class.getSimpleName(),
                TEST_PERSON.getId(), Person.class);
            assertThat(unmodifiedPerson.getFirstName()).isEqualTo(insertedPerson.getFirstName());
            return;
        }

        fail();
    }

    @Test
    public void testDeleteById() {
        cosmosTemplate.insert(TEST_PERSON_2, null);
        assertThat(cosmosTemplate.count(Person.class.getSimpleName())).isEqualTo(2);

        cosmosTemplate.deleteById(Person.class.getSimpleName(), TEST_PERSON.getId(),
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final List<Person> result = TestUtils.toList(cosmosTemplate.findAll(Person.class));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
        assertThat(result.size()).isEqualTo(1);
        assertEquals(result.get(0), TEST_PERSON_2);
    }

    @Test
    public void testDeleteByEntity() {
        Person insertedPerson = cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getLastName()));
        assertThat(cosmosTemplate.count(Person.class.getSimpleName())).isEqualTo(2);

        cosmosTemplate.deleteEntity(Person.class.getSimpleName(), insertedPerson);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final List<Person> result = TestUtils.toList(cosmosTemplate.findAll(Person.class));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
        assertThat(result.size()).isEqualTo(1);
        assertEquals(result.get(0), TEST_PERSON);
    }

    @Test
    public void testCountByContainer() {
        final long prevCount = cosmosTemplate.count(containerName);
        assertThat(prevCount).isEqualTo(1);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();

        final long newCount = cosmosTemplate.count(containerName);
        assertThat(newCount).isEqualTo(2);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testCountByQuery() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON_2.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final long count = cosmosTemplate.count(query, containerName);
        assertThat(count).isEqualTo(1);

        // add ignoreCase testing
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON_2.getFirstName().toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase);

        final long countIgnoreCase = cosmosTemplate.count(queryIgnoreCase, containerName);
        assertThat(countIgnoreCase).isEqualTo(1);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindAllPageableMultiPages() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_1, null);
        final Page<Person> page1 = cosmosTemplate.findAll(pageRequest, Person.class, containerName);

        assertThat(page1.getContent().size()).isEqualTo(PAGE_SIZE_1);
        PageTestUtils.validateNonLastPage(page1, PAGE_SIZE_1);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Page<Person> page2 = cosmosTemplate.findAll(page1.nextPageable(), Person.class,
            containerName);
        assertThat(page2.getContent().size()).isEqualTo(1);
        PageTestUtils.validateLastPage(page2, PAGE_SIZE_1);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindAllPageableMultiPagesPageSizeTwo() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_2, null);
        final Page<Person> page1 = cosmosTemplate.findAll(pageRequest, Person.class, containerName);

        final List<Person> resultPage1 = TestUtils.toList(page1);
        final List<Person> expected = Lists.newArrayList(TEST_PERSON, TEST_PERSON_2);
        assertThat(resultPage1.size()).isEqualTo(expected.size());
        assertThat(resultPage1).containsAll(expected);
        PageTestUtils.validateNonLastPage(page1, PAGE_SIZE_2);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Page<Person> page2 = cosmosTemplate.findAll(page1.nextPageable(), Person.class, containerName);

        final List<Person> resultPage2 = TestUtils.toList(page2);
        final List<Person> expected2 = Lists.newArrayList(TEST_PERSON_3);
        assertThat(resultPage2.size()).isEqualTo(expected2.size());
        assertThat(resultPage2).containsAll(expected2);
        PageTestUtils.validateLastPage(page2, PAGE_SIZE_2);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testPaginationQuery() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(FIRST_NAME), Part.IgnoreCaseType.NEVER);
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_2, null);
        final CosmosQuery query = new CosmosQuery(criteria).with(pageRequest);

        final Page<Person> page = cosmosTemplate.paginationQuery(query, Person.class, containerName);
        assertThat(page.getContent().size()).isEqualTo(1);
        PageTestUtils.validateLastPage(page, PAGE_SIZE_2);

        // add ignore case testing
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(FIRST_NAME.toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase).with(pageRequest);

        final Page<Person> pageIgnoreCase = cosmosTemplate.paginationQuery(queryIgnoreCase, Person.class,
            containerName);
        assertThat(pageIgnoreCase.getContent().size()).isEqualTo(1);
        PageTestUtils.validateLastPage(pageIgnoreCase, PAGE_SIZE_2);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindWithSortAndLimit() {
        final Person testPerson4 = new Person("id_4", "fred", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        final Person testPerson5 = new Person("id_5", "barney", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        final Person testPerson6 = new Person("id_6", "george", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

        insertPerson(testPerson4);
        insertPerson(testPerson5);
        insertPerson(testPerson6);

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "lastName",
            Collections.singletonList(NEW_LAST_NAME), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery query = new CosmosQuery(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "firstName"));

        final List<Person> result = TestUtils.toList(cosmosTemplate.find(query, Person.class, containerName));
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0).getFirstName()).isEqualTo("barney");
        assertThat(result.get(1).getFirstName()).isEqualTo("fred");
        assertThat(result.get(2).getFirstName()).isEqualTo("george");

        query.withLimit(1);
        final List<Person> resultWithLimit = TestUtils.toList(cosmosTemplate.find(query, Person.class, containerName));
        assertThat(resultWithLimit.size()).isEqualTo(1);
        assertThat(resultWithLimit.get(0).getFirstName()).isEqualTo("barney");
    }

    @Test
    public void testFindWithOffsetAndLimit() {
        final Person testPerson4 = new Person("id_4", "fred", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        final Person testPerson5 = new Person("id_5", "barney", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        final Person testPerson6 = new Person("id_6", "george", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

        insertPerson(testPerson4);
        insertPerson(testPerson5);
        insertPerson(testPerson6);

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "lastName",
            Collections.singletonList(NEW_LAST_NAME), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery query = new CosmosQuery(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "firstName"));

        final List<Person> result = TestUtils.toList(cosmosTemplate.find(query, Person.class, containerName));
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0).getFirstName()).isEqualTo("barney");
        assertThat(result.get(1).getFirstName()).isEqualTo("fred");
        assertThat(result.get(2).getFirstName()).isEqualTo("george");

        query.withOffsetAndLimit(1, 1);
        final List<Person> resultWithLimit = TestUtils.toList(cosmosTemplate.find(query, Person.class, containerName));
        assertThat(resultWithLimit.size()).isEqualTo(1);
        assertThat(resultWithLimit.get(0).getFirstName()).isEqualTo("fred");
    }

    @Test
    public void testFindAllWithPageableAndSort() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final Sort sort = Sort.by(Sort.Direction.DESC, "firstName");
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_3, null, sort);

        final Page<Person> page = cosmosTemplate.findAll(pageRequest, Person.class, containerName);
        assertThat(page.getContent().size()).isEqualTo(3);
        PageTestUtils.validateLastPage(page, PAGE_SIZE_3);

        final List<Person> result = page.getContent();
        assertThat(result.get(0).getFirstName()).isEqualTo(NEW_FIRST_NAME);
        assertThat(result.get(1).getFirstName()).isEqualTo(NEW_FIRST_NAME);
        assertThat(result.get(2).getFirstName()).isEqualTo(FIRST_NAME);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

    }

    @Test
    public void testFindAllWithTwoPagesAndVerifySortOrder() {
        final Person testPerson4 = new Person("id_4", "barney", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        final Person testPerson5 = new Person("id_5", "fred", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));
        cosmosTemplate.insert(testPerson4,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(testPerson4)));
        cosmosTemplate.insert(testPerson5,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(testPerson5)));

        final Sort sort = Sort.by(Sort.Direction.ASC, "firstName");
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_3, null, sort);

        final Page<Person> firstPage = cosmosTemplate.findAll(pageRequest, Person.class,
            containerName);

        assertThat(firstPage.getContent().size()).isEqualTo(3);
        PageTestUtils.validateNonLastPage(firstPage, firstPage.getContent().size());

        final List<Person> firstPageResults = firstPage.getContent();
        assertThat(firstPageResults.get(0).getFirstName()).isEqualTo(testPerson4.getFirstName());
        assertThat(firstPageResults.get(1).getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(firstPageResults.get(2).getFirstName()).isEqualTo(testPerson5.getFirstName());

        final Page<Person> secondPage = cosmosTemplate.findAll(firstPage.nextPageable(), Person.class,
            containerName);

        assertThat(secondPage.getContent().size()).isEqualTo(2);
        PageTestUtils.validateLastPage(secondPage, PAGE_SIZE_3);

        final List<Person> secondPageResults = secondPage.getContent();
        assertThat(secondPageResults.get(0).getFirstName()).isEqualTo(NEW_FIRST_NAME);
        assertThat(secondPageResults.get(1).getFirstName()).isEqualTo(NEW_FIRST_NAME);
    }

    @Test
    public void testExists() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);
        final Boolean exists = cosmosTemplate.exists(query, Person.class, containerName);
        assertThat(exists).isTrue();

        // add ignore testing
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName().toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase);
        final Boolean existsIgnoreCase = cosmosTemplate.exists(queryIgnoreCase, Person.class, containerName);
        assertThat(existsIgnoreCase).isTrue();

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testArrayContainsCriteria() {
        Criteria hasHobby = Criteria.getInstance(CriteriaType.ARRAY_CONTAINS, "hobbies",
            Collections.singletonList(HOBBY1), Part.IgnoreCaseType.NEVER);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(hasHobby), Person.class,
            containerName));

        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testContainsCriteria() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));
        Person TEST_PERSON_4 = new Person("id-4", "NEW_FIRST_NAME", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        cosmosTemplate.insert(TEST_PERSON_4, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4)));

        Criteria containsCaseSensitive = Criteria.getInstance(CriteriaType.CONTAINING, "firstName",
            Collections.singletonList("first"), Part.IgnoreCaseType.NEVER);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(containsCaseSensitive), Person.class,
            containerName));
        assertThat(people).containsExactly(TEST_PERSON, TEST_PERSON_2, TEST_PERSON_3);

        Criteria containsNotCaseSensitive = Criteria.getInstance(CriteriaType.CONTAINING, "firstName",
            Collections.singletonList("first"), Part.IgnoreCaseType.ALWAYS);
        List<Person> people2 = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(containsNotCaseSensitive), Person.class,
            containerName));
        assertThat(people2).containsExactly(TEST_PERSON, TEST_PERSON_2, TEST_PERSON_3, TEST_PERSON_4);
    }

    @Test
    public void testContainsCriteria2() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));

        Criteria containsCaseSensitive = Criteria.getInstance(CriteriaType.CONTAINING, "id",
            Collections.singletonList("1"), Part.IgnoreCaseType.NEVER);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(containsCaseSensitive), Person.class,
            containerName));
        assertThat(people).containsExactly(TEST_PERSON);

        Criteria containsCaseSensitive2 = Criteria.getInstance(CriteriaType.CONTAINING, "id",
            Collections.singletonList("2"), Part.IgnoreCaseType.NEVER);
        List<Person> people2 = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(containsCaseSensitive2), Person.class,
            containerName));
        assertThat(people2).containsExactly(TEST_PERSON_2);

        Criteria containsCaseSensitive3 = Criteria.getInstance(CriteriaType.CONTAINING, "id",
            Collections.singletonList("3"), Part.IgnoreCaseType.NEVER);
        List<Person> people3 = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(containsCaseSensitive3), Person.class,
            containerName));
        assertThat(people3).containsExactly(TEST_PERSON_3);
    }

    @Test
    public void testNotContainsCriteria() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));
        Person TEST_PERSON_4 = new Person("id-4", "NEW_FIRST_NAME", NEW_LAST_NAME, HOBBIES,
            ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
        cosmosTemplate.insert(TEST_PERSON_4, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_4)));

        Criteria notContainsCaseSensitive = Criteria.getInstance(CriteriaType.NOT_CONTAINING, "firstName",
            Collections.singletonList("li"), Part.IgnoreCaseType.NEVER);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(notContainsCaseSensitive), Person.class,
            containerName));
        assertThat(people).containsExactly(TEST_PERSON_2, TEST_PERSON_3, TEST_PERSON_4);

        Criteria notContainsNotCaseSensitive = Criteria.getInstance(CriteriaType.NOT_CONTAINING, "firstName",
            Collections.singletonList("new"), Part.IgnoreCaseType.ALWAYS);
        List<Person> people2 = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(notContainsNotCaseSensitive), Person.class,
            containerName));
        assertThat(people2).containsExactly(TEST_PERSON);
    }

    @Test
    public void testNotContainsCriteria2() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));
        cosmosTemplate.insert(TEST_PERSON_3, new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_3)));

        Criteria notContainsCaseSensitive = Criteria.getInstance(CriteriaType.NOT_CONTAINING, "id",
            Collections.singletonList("1"), Part.IgnoreCaseType.NEVER);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(notContainsCaseSensitive), Person.class,
            containerName));
        assertThat(people).containsExactly(TEST_PERSON_2, TEST_PERSON_3);

        Criteria notContainsCaseSensitive2 = Criteria.getInstance(CriteriaType.NOT_CONTAINING, "id",
            Collections.singletonList("2"), Part.IgnoreCaseType.NEVER);
        List<Person> people2 = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(notContainsCaseSensitive2), Person.class,
            containerName));
        assertThat(people2).containsExactly(TEST_PERSON, TEST_PERSON_3);

        Criteria notContainsCaseSensitive3 = Criteria.getInstance(CriteriaType.NOT_CONTAINING, "id",
            Collections.singletonList("3"), Part.IgnoreCaseType.NEVER);
        List<Person> people3 = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(notContainsCaseSensitive3), Person.class,
            containerName));
        assertThat(people3).containsExactly(TEST_PERSON, TEST_PERSON_2);
    }

    @Test
    public void testIsNotNullCriteriaCaseSensitive() {
        Criteria hasLastName = Criteria.getInstance(CriteriaType.IS_NOT_NULL, "lastName",
            Collections.emptyList(),
            Part.IgnoreCaseType.ALWAYS);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(hasLastName), Person.class,
            containerName));

        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testStartsWithCriteriaCaseSensitive() {
        Criteria nameStartsWith = Criteria.getInstance(CriteriaType.STARTS_WITH, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName().toUpperCase()),
            Part.IgnoreCaseType.ALWAYS);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(nameStartsWith), Person.class,
            containerName));

        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testIsEqualCriteriaCaseSensitive() {
        Criteria nameStartsWith = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName().toUpperCase()),
            Part.IgnoreCaseType.ALWAYS);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(nameStartsWith), Person.class,
            containerName));

        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testStringEqualsCriteriaCaseSensitive() {
        Criteria nameStartsWith = Criteria.getInstance(CriteriaType.STRING_EQUALS, "firstName",
                Collections.singletonList(TEST_PERSON.getFirstName().toUpperCase()),
                Part.IgnoreCaseType.ALWAYS);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(nameStartsWith), Person.class,
                containerName));

        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testBetweenCriteria() {
        Criteria ageBetween = Criteria.getInstance(CriteriaType.BETWEEN, "age", Arrays.asList(AGE - 1, AGE + 1),
            Part.IgnoreCaseType.NEVER);
        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(ageBetween), Person.class,
            containerName));
        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testFindWithEqualCriteriaContainingNestedProperty() {
        String postalCode = ADDRESSES.get(0).getPostalCode();
        String subjectWithNestedProperty = "shippingAddresses[0]['postalCode']";
        Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, subjectWithNestedProperty,
            Collections.singletonList(postalCode), Part.IgnoreCaseType.NEVER);

        List<Person> people = TestUtils.toList(cosmosTemplate.find(new CosmosQuery(criteria), Person.class,
            containerName));

        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testRunQueryWithEqualCriteriaContainingSpaces() {
        String usaPassportId = PASSPORT_IDS_BY_COUNTRY.get("United States of America");
        String subjectWithSpaces = "passportIdsByCountry['United States of America']";
        Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, subjectWithSpaces,
            Collections.singletonList(usaPassportId), Part.IgnoreCaseType.NEVER);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(criteria));

        List<Person> people = TestUtils.toList(cosmosTemplate.runQuery(sqlQuerySpec, Person.class, Person.class));

        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testRunQueryWithSimpleReturnType() {
        Criteria ageBetween = Criteria.getInstance(CriteriaType.BETWEEN, "age", Arrays.asList(AGE - 1, AGE + 1),
                                                   Part.IgnoreCaseType.NEVER);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(ageBetween));
        List<Person> people = TestUtils.toList(cosmosTemplate.runQuery(sqlQuerySpec, Person.class, Person.class));
        assertThat(people).containsExactly(TEST_PERSON);
    }

    @Test
    public void testSliceQuery() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(FIRST_NAME), Part.IgnoreCaseType.NEVER);
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_2, null);
        final CosmosQuery query = new CosmosQuery(criteria).with(pageRequest);

        final Slice<Person> slice = cosmosTemplate.sliceQuery(query, Person.class, containerName);
        assertThat(slice.getContent().size()).isEqualTo(1);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }
    @Test
    public void testRunSliceQuery() {
        cosmosTemplate.insert(TEST_PERSON_2,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON_2)));

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(FIRST_NAME), Part.IgnoreCaseType.NEVER);
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_2, null);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(criteria));
        final Slice<Person> slice = cosmosTemplate.runSliceQuery(sqlQuerySpec, pageRequest, Person.class, Person.class);
        assertThat(slice.getContent().size()).isEqualTo(1);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void createWithAutoscale() throws ClassNotFoundException {
        final CosmosEntityInformation<AutoScaleSample, String> autoScaleSampleInfo =
            new CosmosEntityInformation<>(AutoScaleSample.class);
        CosmosContainerProperties containerProperties = cosmosTemplate.createContainerIfNotExists(autoScaleSampleInfo);
        assertNotNull(containerProperties);
        ThroughputResponse throughput = client.getDatabase(TestConstants.DB_NAME)
            .getContainer(autoScaleSampleInfo.getContainerName())
            .readThroughput()
            .block();
        assertNotNull(throughput);
        assertEquals(Integer.parseInt(TestConstants.AUTOSCALE_MAX_THROUGHPUT),
            throughput.getProperties().getAutoscaleMaxThroughput());
        collectionManager.deleteContainer(autoScaleSampleInfo);
    }

    @Test
    public void createDatabaseWithThroughput() throws ClassNotFoundException {
        final String configuredThroughputDbName = TestConstants.DB_NAME + "-configured-throughput";
        deleteDatabaseIfExists(configuredThroughputDbName);

        Integer expectedRequestUnits = 700;
        final CosmosConfig config = CosmosConfig.builder()
            .enableDatabaseThroughput(false, expectedRequestUnits)
            .build();
        final CosmosTemplate configuredThroughputCosmosTemplate = createCosmosTemplate(config, configuredThroughputDbName);

        final CosmosEntityInformation<Person, String> personInfo =
            new CosmosEntityInformation<>(Person.class);
        configuredThroughputCosmosTemplate.createContainerIfNotExists(personInfo);

        final CosmosAsyncDatabase database = client.getDatabase(configuredThroughputDbName);
        final ThroughputResponse response = database.readThroughput().block();
        assertEquals(expectedRequestUnits, response.getProperties().getManualThroughput());
        deleteDatabaseIfExists(configuredThroughputDbName);
    }

    @Test
    public void queryWithMaxDegreeOfParallelism() throws ClassNotFoundException {
        final CosmosConfig config = CosmosConfig.builder()
            .maxDegreeOfParallelism(20)
            .build();
        final CosmosTemplate maxDegreeOfParallelismCosmosTemplate = createCosmosTemplate(config, TestConstants.DB_NAME);

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final long count = maxDegreeOfParallelismCosmosTemplate.count(query, containerName);

        assertEquals((int) ReflectionTestUtils.getField(maxDegreeOfParallelismCosmosTemplate, "maxDegreeOfParallelism"), 20);
    }

    @Test
    public void queryWithMaxBufferedItemCount() throws ClassNotFoundException {
        final CosmosConfig config = CosmosConfig.builder()
            .maxBufferedItemCount(500)
            .build();
        final CosmosTemplate maxBufferedItemCountCosmosTemplate = createCosmosTemplate(config, TestConstants.DB_NAME);

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final long count = maxBufferedItemCountCosmosTemplate.count(query, containerName);

        assertEquals((int) ReflectionTestUtils.getField(maxBufferedItemCountCosmosTemplate, "maxBufferedItemCount"), 500);
    }

    @Test
    public void queryWithResponseContinuationTokenLimitInKb() throws ClassNotFoundException {
        final CosmosConfig config = CosmosConfig.builder()
            .responseContinuationTokenLimitInKb(2000)
            .build();
        final CosmosTemplate responseContinuationTokenLimitInKbCosmosTemplate =
            createCosmosTemplate(config, TestConstants.DB_NAME);

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final long count = responseContinuationTokenLimitInKbCosmosTemplate.count(query, containerName);

        assertEquals((int) ReflectionTestUtils.getField(responseContinuationTokenLimitInKbCosmosTemplate,
            "responseContinuationTokenLimitInKb"), 2000);
    }

    @Test
    public void queryDatabaseWithQueryMerticsEnabled() throws ClassNotFoundException {
        final CosmosConfig config = CosmosConfig.builder()
            .enableQueryMetrics(true)
            .build();
        final CosmosTemplate queryMetricsEnabledCosmosTemplate = createCosmosTemplate(config, TestConstants.DB_NAME);

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final long count = queryMetricsEnabledCosmosTemplate.count(query, containerName);

        assertEquals((boolean) ReflectionTestUtils.getField(queryMetricsEnabledCosmosTemplate, "queryMetricsEnabled"), true);
    }

    @Test
    public void userAgentSpringDataCosmosSuffix() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //  getUserAgentSuffix method from CosmosClientBuilder
        Method getUserAgentSuffix = CosmosClientBuilder.class.getDeclaredMethod("getUserAgentSuffix");
        getUserAgentSuffix.setAccessible(true);
        String userAgentSuffix = (String) getUserAgentSuffix.invoke(cosmosClientBuilder);
        assertThat(userAgentSuffix).contains(Constants.USER_AGENT_SUFFIX);
        assertThat(userAgentSuffix).contains(PropertyLoader.getProjectVersion());
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
