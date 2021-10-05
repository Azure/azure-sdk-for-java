// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.PageTestUtils;
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
import com.azure.spring.data.cosmos.domain.GenIdEntity;
import com.azure.spring.data.cosmos.domain.Person;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AuditableRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
import static com.azure.spring.data.cosmos.common.TestConstants.NOT_EXIST_ID;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_1;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_2;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_3;
import static com.azure.spring.data.cosmos.common.TestConstants.PASSPORT_IDS_BY_COUNTRY;
import static com.azure.spring.data.cosmos.common.TestConstants.UPDATED_FIRST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    private static final String PRECONDITION_IS_NOT_MET = "is not met";

    private static final String WRONG_ETAG = "WRONG_ETAG";

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    private static CosmosAsyncClient client;
    private static CosmosTemplate cosmosTemplate;
    private static CosmosEntityInformation<Person, String> personInfo;
    private static String containerName;

    private Person insertedPerson;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;
    @Autowired
    private AuditableRepository auditableRepository;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (cosmosTemplate == null) {
            client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
            final CosmosFactory cosmosFactory = new CosmosFactory(client, TestConstants.DB_NAME);

            final CosmosMappingContext mappingContext = new CosmosMappingContext();
            personInfo = new CosmosEntityInformation<>(Person.class);
            containerName = personInfo.getContainerName();

            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext,
                null);

            cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, cosmosConverter);
        }

        collectionManager.ensureContainersCreatedAndEmpty(cosmosTemplate, Person.class,
                                                          GenIdEntity.class, AuditableEntity.class);
        insertedPerson = cosmosTemplate.insert(Person.class.getSimpleName(), TEST_PERSON,
            new PartitionKey(TEST_PERSON.getLastName()));
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
            assertThat(ex.getCosmosException() instanceof ConflictException);
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
            + UUID.randomUUID().toString();
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
        PageTestUtils.validateLastPage(page, page.getContent().size());

        // add ignore case testing
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(FIRST_NAME.toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase).with(pageRequest);

        final Page<Person> pageIgnoreCase = cosmosTemplate.paginationQuery(queryIgnoreCase, Person.class,
            containerName);
        assertThat(pageIgnoreCase.getContent().size()).isEqualTo(1);
        PageTestUtils.validateLastPage(pageIgnoreCase, pageIgnoreCase.getContent().size());

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

        query.setLimit(1);
        final List<Person> resultWithLimit = TestUtils.toList(cosmosTemplate.find(query, Person.class, containerName));
        assertThat(resultWithLimit.size()).isEqualTo(1);
        assertThat(resultWithLimit.get(0).getFirstName()).isEqualTo("barney");
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
        PageTestUtils.validateLastPage(secondPage, secondPage.getContent().size());

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
    public void testRunQueryWithReturnTypeContainingLocalDateTime() {
        final AuditableEntity entity = new AuditableEntity();
        entity.setId(UUID.randomUUID().toString());

        auditableRepository.save(entity);

        Criteria equals = Criteria.getInstance(CriteriaType.IS_EQUAL, "id", Collections.singletonList(entity.getId()), Part.IgnoreCaseType.NEVER);
        final SqlQuerySpec sqlQuerySpec = new FindQuerySpecGenerator().generateCosmos(new CosmosQuery(equals));
        List<AuditableEntity> results = TestUtils.toList(cosmosTemplate.runQuery(sqlQuerySpec, AuditableEntity.class, AuditableEntity.class));
        assertEquals(results.size(), 1);
        AuditableEntity foundEntity = results.get(0);
        assertEquals(entity.getId(), foundEntity.getId());
        assertNotNull(foundEntity.getCreatedDate());
        assertNotNull(foundEntity.getLastModifiedByDate());
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
        final CosmosQuery query = new CosmosQuery(criteria).with(pageRequest);
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
    }
}
