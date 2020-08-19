// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.PageTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.domain.PartitionPerson;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.azure.spring.data.cosmos.common.TestConstants.ADDRESSES;
import static com.azure.spring.data.cosmos.common.TestConstants.FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.HOBBIES;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_1;
import static com.azure.spring.data.cosmos.common.TestConstants.ID_2;
import static com.azure.spring.data.cosmos.common.TestConstants.NEW_FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.NEW_ZIP_CODE;
import static com.azure.spring.data.cosmos.common.TestConstants.NOT_EXIST_ID;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_1;
import static com.azure.spring.data.cosmos.common.TestConstants.PAGE_SIZE_2;
import static com.azure.spring.data.cosmos.common.TestConstants.PROPERTY_ID;
import static com.azure.spring.data.cosmos.common.TestConstants.PROPERTY_ZIP_CODE;
import static com.azure.spring.data.cosmos.common.TestConstants.UPDATED_FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.ZIP_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CosmosTemplatePartitionIT {
    private static final PartitionPerson TEST_PERSON = new PartitionPerson(ID_1, FIRST_NAME, ZIP_CODE,
        HOBBIES, ADDRESSES);

    private static final PartitionPerson TEST_PERSON_2 = new PartitionPerson(ID_2, NEW_FIRST_NAME,
        TEST_PERSON.getZipCode(), HOBBIES, ADDRESSES);

    private static CosmosTemplate cosmosTemplate;
    private static String containerName;
    private static CosmosEntityInformation<PartitionPerson, String> personInfo;
    private static boolean initialized;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CosmosConfig cosmosConfig;
    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;

    @Before
    public void setUp() throws ClassNotFoundException {
        if (!initialized) {
            CosmosAsyncClient client = CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
            final CosmosFactory cosmosFactory = new CosmosFactory(client, TestConstants.DB_NAME);
            final CosmosMappingContext mappingContext = new CosmosMappingContext();

            personInfo = new CosmosEntityInformation<>(PartitionPerson.class);
            mappingContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

            final MappingCosmosConverter dbConverter = new MappingCosmosConverter(mappingContext, null);

            cosmosTemplate = new CosmosTemplate(cosmosFactory, cosmosConfig, dbConverter);
            containerName = personInfo.getContainerName();

            cosmosTemplate.createContainerIfNotExists(personInfo);
            initialized = true;
        }

        cosmosTemplate.insert(PartitionPerson.class.getSimpleName(), TEST_PERSON,
            new PartitionKey(TEST_PERSON.getZipCode()));
    }

    @After
    public void cleanup() {
        cosmosTemplate.deleteAll(personInfo.getContainerName(), PartitionPerson.class);
    }

    @AfterClass
    public static void afterClassCleanup() {
        cosmosTemplate.deleteContainer(personInfo.getContainerName());
    }

    @Test
    public void testFindWithPartition() {
        Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, PROPERTY_ZIP_CODE,
            Collections.singletonList(ZIP_CODE), Part.IgnoreCaseType.NEVER);
        CosmosQuery query = new CosmosQuery(criteria);
        List<PartitionPerson> result = TestUtils.toList(cosmosTemplate.find(query, PartitionPerson.class,
            PartitionPerson.class.getSimpleName()));

        assertThat(result.size()).isEqualTo(1);
        assertEquals(TEST_PERSON, result.get(0));

        criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, PROPERTY_ID,
            Collections.singletonList(ID_1), Part.IgnoreCaseType.NEVER);
        query = new CosmosQuery(criteria);
        result = TestUtils.toList(cosmosTemplate.find(query, PartitionPerson.class,
            PartitionPerson.class.getSimpleName()));

        assertThat(result.size()).isEqualTo(1);
        assertEquals(TEST_PERSON, result.get(0));
    }

    @Test
    public void testFindIgnoreCaseWithPartition() {
        Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, PROPERTY_ZIP_CODE,
            Collections.singletonList(ZIP_CODE), Part.IgnoreCaseType.NEVER);
        CosmosQuery query = new CosmosQuery(criteria);
        List<PartitionPerson> result = TestUtils.toList(cosmosTemplate.find(query, PartitionPerson.class,
            PartitionPerson.class.getSimpleName()));

        assertThat(result.size()).isEqualTo(1);
        assertEquals(TEST_PERSON, result.get(0));

        criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, PROPERTY_ID,
            Collections.singletonList(ID_1.toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        query = new CosmosQuery(criteria);
        result = TestUtils.toList(cosmosTemplate.find(query, PartitionPerson.class,
            PartitionPerson.class.getSimpleName()));

        assertThat(result.size()).isEqualTo(1);
        assertEquals(TEST_PERSON, result.get(0));
    }


    @Test
    public void testFindByIdWithPartition() {
        final PartitionPerson partitionPersonById = cosmosTemplate.findById(TEST_PERSON.getId(),
            PartitionPerson.class,
            new PartitionKey(personInfo.getPartitionKeyFieldValue(TEST_PERSON)));

        assertEquals(TEST_PERSON, partitionPersonById);
    }

    @Test
    public void testFindByNonExistIdWithPartition() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, PROPERTY_ID,
            Collections.singletonList(NOT_EXIST_ID), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final List<PartitionPerson> result = TestUtils.toList(cosmosTemplate.find(query, PartitionPerson.class,
            PartitionPerson.class.getSimpleName()));
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void testUpsertNewDocumentPartition() {
        final String firstName = NEW_FIRST_NAME
            + "_" + UUID.randomUUID().toString();
        final PartitionPerson newPerson = new PartitionPerson(TEST_PERSON.getId(),
            firstName, NEW_ZIP_CODE,
            null, null);
        final PartitionPerson partitionPerson =
            cosmosTemplate.upsertAndReturnEntity(PartitionPerson.class.getSimpleName(), newPerson);

        final List<PartitionPerson> result = TestUtils.toList(cosmosTemplate.findAll(PartitionPerson.class));

        assertThat(result.size()).isEqualTo(2);
        assertThat(partitionPerson.getFirstName()).isEqualTo(firstName);
    }

    @Test
    public void testUpdatePartition() {
        final PartitionPerson updated = new PartitionPerson(TEST_PERSON.getId(), UPDATED_FIRST_NAME,
            TEST_PERSON.getZipCode(), TEST_PERSON.getHobbies(), TEST_PERSON.getShippingAddresses());
        final PartitionPerson partitionPerson =
            cosmosTemplate.upsertAndReturnEntity(PartitionPerson.class.getSimpleName(), updated);

        assertEquals(partitionPerson, updated);
    }

    @Test
    public void testDeleteByIdPartition() {
        // insert new document with same partition key
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode()));

        final List<PartitionPerson> inserted = TestUtils.toList(cosmosTemplate.findAll(PartitionPerson.class));
        assertThat(inserted.size()).isEqualTo(2);
        assertThat(inserted.get(0).getZipCode()).isEqualTo(TEST_PERSON.getZipCode());
        assertThat(inserted.get(1).getZipCode()).isEqualTo(TEST_PERSON.getZipCode());

        cosmosTemplate.deleteById(PartitionPerson.class.getSimpleName(),
            TEST_PERSON.getId(), new PartitionKey(TEST_PERSON.getZipCode()));

        final List<PartitionPerson> result = TestUtils.toList(cosmosTemplate.findAll(PartitionPerson.class));
        assertThat(result.size()).isEqualTo(1);
        assertEquals(result.get(0), TEST_PERSON_2);
    }

    @Test
    public void testCountForPartitionedCollection() {
        final long prevCount = cosmosTemplate.count(containerName);
        assertThat(prevCount).isEqualTo(1);

        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode()));

        final long newCount = cosmosTemplate.count(containerName);
        assertThat(newCount).isEqualTo(2);
    }

    @Test
    public void testCountForPartitionedCollectionByQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode()));

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON_2.getFirstName()), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final long count = cosmosTemplate.count(query, containerName);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testCountIgnoreCaseForPartitionedCollectionByQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode()));
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(TEST_PERSON_2.getFirstName().toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase);

        final long countIgnoreCase = cosmosTemplate.count(queryIgnoreCase, containerName);
        assertThat(countIgnoreCase).isEqualTo(1);
    }

    @Test
    public void testNonExistFieldValue() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList("non-exist-first-name"), Part.IgnoreCaseType.NEVER);
        final CosmosQuery query = new CosmosQuery(criteria);

        final long count = cosmosTemplate.count(query, containerName);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testPartitionedFindAllPageableMultiPages() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode()));

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_1, null);
        final Page<PartitionPerson> page1 = cosmosTemplate.findAll(pageRequest, PartitionPerson.class, containerName);

        assertThat(page1.getContent().size()).isEqualTo(PAGE_SIZE_1);
        PageTestUtils.validateNonLastPage(page1, PAGE_SIZE_1);

        final Page<PartitionPerson> page2 = cosmosTemplate.findAll(page1.getPageable(),
            PartitionPerson.class, containerName);
        assertThat(page2.getContent().size()).isEqualTo(1);
        PageTestUtils.validateLastPage(page2, PAGE_SIZE_1);
    }

    @Test
    public void testPartitionedPaginationQuery() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode()));

        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(FIRST_NAME), Part.IgnoreCaseType.NEVER);
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_2, null);
        final CosmosQuery query = new CosmosQuery(criteria).with(pageRequest);

        final Page<PartitionPerson> page = cosmosTemplate.paginationQuery(query, PartitionPerson.class, containerName);
        assertThat(page.getContent().size()).isEqualTo(1);
        PageTestUtils.validateLastPage(page, page.getContent().size());
    }

    @Test
    public void testPartitionedPaginationQueryIgnoreCase() {
        cosmosTemplate.insert(TEST_PERSON_2, new PartitionKey(TEST_PERSON_2.getZipCode()));
        final Criteria criteriaIgnoreCase = Criteria.getInstance(CriteriaType.IS_EQUAL, "firstName",
            Collections.singletonList(FIRST_NAME.toUpperCase()), Part.IgnoreCaseType.ALWAYS);
        final PageRequest pageRequest = new CosmosPageRequest(0, PAGE_SIZE_2, null);
        final CosmosQuery queryIgnoreCase = new CosmosQuery(criteriaIgnoreCase).with(pageRequest);

        final Page<PartitionPerson> pageIgnoreCase = cosmosTemplate
            .paginationQuery(queryIgnoreCase, PartitionPerson.class, containerName);
        assertThat(pageIgnoreCase.getContent().size()).isEqualTo(1);
        PageTestUtils.validateLastPage(pageIgnoreCase, pageIgnoreCase.getContent().size());
    }
}
