// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.PageTestUtils;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestUtils;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.domain.PersonCrossPartition;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.repository.AddressRepository;
import com.azure.spring.data.cosmos.repository.repository.AuditableRepository;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.assertj.core.util.Lists;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
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
import static com.azure.spring.data.cosmos.common.TestConstants.ID_3;
import static com.azure.spring.data.cosmos.common.TestConstants.LAST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.NEW_FIRST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.NEW_LAST_NAME;
import static com.azure.spring.data.cosmos.common.TestConstants.PASSPORT_IDS_BY_COUNTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Sort.Direction.ASC;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class PersonCrossPartitionIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    private static final PersonCrossPartition TEST_PERSON_CP = new PersonCrossPartition(ID_1, FIRST_NAME, LAST_NAME, HOBBIES,
        ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final PersonCrossPartition TEST_PERSON_CP_2 = new PersonCrossPartition(ID_2, NEW_FIRST_NAME, NEW_LAST_NAME, HOBBIES,
        ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    private static final PersonCrossPartition TEST_PERSON_CP_3 = new PersonCrossPartition(ID_3, NEW_FIRST_NAME, NEW_LAST_NAME, HOBBIES,
        ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);

    @Autowired
    private ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils;

    @Autowired
    private CosmosTemplate cosmosTemplate;

    private static CosmosEntityInformation<PersonCrossPartition, String> personCrossPartitionInfo;

    private static String containerName;

    @Autowired
    private AuditableRepository auditableRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Before
    public void setUp() {
        personCrossPartitionInfo = new CosmosEntityInformation<>(PersonCrossPartition.class);
        containerName = personCrossPartitionInfo.getContainerName();

        collectionManager.ensureContainersCreatedAndEmpty(cosmosTemplate, PersonCrossPartition.class);
    }

    @AfterClass
    public static void cleanUp() {
        collectionManager.deleteContainer(personCrossPartitionInfo);
    }

    @Test
    public void testFindAllPageableMultiPagesMultiPartition() {
        cosmosTemplate.insert(TEST_PERSON_CP,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP)));
        cosmosTemplate.insert(TEST_PERSON_CP_2,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_2)));
        cosmosTemplate.insert(TEST_PERSON_CP_3,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_3)));
        final List<PersonCrossPartition> expected = Lists.newArrayList(TEST_PERSON_CP, TEST_PERSON_CP_2, TEST_PERSON_CP_3);

        for (int i=4; i<=10; i++) {
            PersonCrossPartition temp = new PersonCrossPartition("id_" + i, "fred", LAST_NAME + "_" + i, HOBBIES,
                ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
            cosmosTemplate.insert(temp, new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(temp)));
            expected.add(temp);
        }

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, 100, null);
        final Page<PersonCrossPartition> page1 = cosmosTemplate.findAll(pageRequest, PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage1 = TestUtils.toList(page1);
        assertThat(resultPage1.size()).isEqualTo(expected.size());
        assertThat(resultPage1).containsAll(expected);
        PageTestUtils.validateLastPage(page1, 100);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);
    }

    @Test
    public void testFindAllPageableMultiPagesMultiPartition2() {
        cosmosTemplate.insert(TEST_PERSON_CP,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP)));
        cosmosTemplate.insert(TEST_PERSON_CP_2,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_2)));
        cosmosTemplate.insert(TEST_PERSON_CP_3,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_3)));
        final List<PersonCrossPartition> expected = Lists.newArrayList(TEST_PERSON_CP, TEST_PERSON_CP_2, TEST_PERSON_CP_3);

        for (int i=4; i<=10; i++) {
            PersonCrossPartition temp = new PersonCrossPartition("id_" + i, "fred", LAST_NAME + "_" + i, HOBBIES,
                ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
            cosmosTemplate.insert(temp, new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(temp)));
            expected.add(temp);
        }

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, 7, null);
        final Page<PersonCrossPartition> page1 = cosmosTemplate.findAll(pageRequest, PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage1 = TestUtils.toList(page1);
        assertThat(resultPage1.size()).isEqualTo(7);
        PageTestUtils.validateNonLastPage(page1, 7);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Page<PersonCrossPartition> page2 = cosmosTemplate.findAll(page1.nextPageable(), PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage2 = TestUtils.toList(page2);
        assertThat(resultPage2.size()).isEqualTo(3);
        PageTestUtils.validateLastPage(page2, 7);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final List<PersonCrossPartition> allResults = new ArrayList<>();
        allResults.addAll(resultPage1);
        allResults.addAll(resultPage2);
        assertThat(allResults).containsAll(expected);
    }

    @Test
    public void testFindAllPageableMultiPagesMultiPartition3() {
        cosmosTemplate.insert(TEST_PERSON_CP,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP)));
        cosmosTemplate.insert(TEST_PERSON_CP_2,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_2)));
        cosmosTemplate.insert(TEST_PERSON_CP_3,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_3)));
        final List<PersonCrossPartition> expected = Lists.newArrayList(TEST_PERSON_CP, TEST_PERSON_CP_2, TEST_PERSON_CP_3);

        for (int i=4; i<=10; i++) {
            PersonCrossPartition temp = new PersonCrossPartition("id_" + i, "fred", LAST_NAME + "_" + i, HOBBIES,
                ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
            cosmosTemplate.insert(temp, new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(temp)));
            expected.add(temp);
        }

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, 3, null);
        final Page<PersonCrossPartition> page1 = cosmosTemplate.findAll(pageRequest, PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage1 = TestUtils.toList(page1);
        assertThat(resultPage1.size()).isEqualTo(3);
        PageTestUtils.validateNonLastPage(page1, 3);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Page<PersonCrossPartition> page2 = cosmosTemplate.findAll(page1.nextPageable(), PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage2 = TestUtils.toList(page2);
        assertThat(resultPage2.size()).isEqualTo(3);
        PageTestUtils.validateNonLastPage(page2, 3);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Page<PersonCrossPartition> page3 = cosmosTemplate.findAll(page2.nextPageable(), PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage3 = TestUtils.toList(page3);
        assertThat(resultPage3.size()).isEqualTo(3);
        PageTestUtils.validateNonLastPage(page3, 3);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Page<PersonCrossPartition> page4 = cosmosTemplate.findAll(page3.nextPageable(), PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage4 = TestUtils.toList(page4);
        assertThat(resultPage4.size()).isEqualTo(1);
        PageTestUtils.validateLastPage(page4, 3);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final List<PersonCrossPartition> allResults = new ArrayList<>();
        allResults.addAll(resultPage1);
        allResults.addAll(resultPage2);
        allResults.addAll(resultPage3);
        allResults.addAll(resultPage4);
        assertThat(allResults).containsAll(expected);
    }

    @Test
    public void testFindAllPageableMultiPagesMultiPartitionWithOffset() {
        cosmosTemplate.insert(TEST_PERSON_CP,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP)));
        cosmosTemplate.insert(TEST_PERSON_CP_2,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_2)));
        cosmosTemplate.insert(TEST_PERSON_CP_3,
            new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(TEST_PERSON_CP_3)));
        final List<PersonCrossPartition> expected = Lists.newArrayList(TEST_PERSON_CP_2, TEST_PERSON_CP_3);

        for (int i=4; i<=10; i++) {
            PersonCrossPartition temp = new PersonCrossPartition("id_" + i, "fred", LAST_NAME + "_" + i, HOBBIES,
                ADDRESSES, AGE, PASSPORT_IDS_BY_COUNTRY);
            cosmosTemplate.insert(temp, new PartitionKey(personCrossPartitionInfo.getPartitionKeyFieldValue(temp)));
            expected.add(temp);
        }

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNull();

        final CosmosPageRequest pageRequest = CosmosPageRequest.of(1, 0, 7,
            null, Sort.by(ASC, "id"));
        final Page<PersonCrossPartition> page1 = cosmosTemplate.findAll(pageRequest, PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage1 = TestUtils.toList(page1);
        assertThat(resultPage1.size()).isEqualTo(7);
        PageTestUtils.validateNonLastPage(page1, 7);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final Page<PersonCrossPartition> page2 = cosmosTemplate.findAll(page1.nextPageable(), PersonCrossPartition.class, containerName);

        final List<PersonCrossPartition> resultPage2 = TestUtils.toList(page2);
        assertThat(resultPage2.size()).isEqualTo(2);
        PageTestUtils.validateLastPage(page2, 7);

        assertThat(responseDiagnosticsTestUtils.getCosmosDiagnostics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics()).isNotNull();
        assertThat(responseDiagnosticsTestUtils.getCosmosResponseStatistics().getRequestCharge()).isGreaterThan(0);

        final List<PersonCrossPartition> allResults = new ArrayList<>();
        allResults.addAll(resultPage1);
        allResults.addAll(resultPage2);
        assertThat(allResults).containsAll(expected);
    }
}
