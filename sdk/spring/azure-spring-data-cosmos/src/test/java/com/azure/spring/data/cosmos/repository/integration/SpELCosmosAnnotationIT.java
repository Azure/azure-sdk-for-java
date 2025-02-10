// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.IntegrationTestCollectionManager;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.SpELBeanStudent;
import com.azure.spring.data.cosmos.domain.SpELPropertyStudent;
import com.azure.spring.data.cosmos.repository.TestRepositorySpELConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositorySpELConfig.class)
public class SpELCosmosAnnotationIT {

    @ClassRule
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    private static final SpELPropertyStudent TEST_PROPERTY_STUDENT = new SpELPropertyStudent(TestConstants.ID_1,
        TestConstants.FIRST_NAME, TestConstants.LAST_NAME);

    @Autowired
    private CosmosTemplate cosmosTemplate;

    @Before
    public void setUp() {
        collectionManager.ensureContainersCreatedAndEmpty(cosmosTemplate, SpELPropertyStudent.class);
    }

    @Test
    public void testDynamicContainerNameWithPropertySourceExpression() {
        final CosmosEntityInformation<SpELPropertyStudent, Object> propertyStudentInfo =
            new CosmosEntityInformation<>(SpELPropertyStudent.class);

        assertEquals(TestConstants.DYNAMIC_PROPERTY_COLLECTION_NAME, propertyStudentInfo.getContainerName());
    }

    @Test
    public void testDynamicContainerNameWithBeanExpression() {
        final CosmosEntityInformation<SpELBeanStudent, Object> beanStudentInfo =
            new CosmosEntityInformation<>(SpELBeanStudent.class);

        assertEquals(TestConstants.DYNAMIC_BEAN_COLLECTION_NAME, beanStudentInfo.getContainerName());
    }

    @Test
    public void testDatabaseOperationsOnDynamicallyNamedCollection() throws ClassNotFoundException {
        final CosmosEntityInformation<SpELPropertyStudent, Object> propertyStudentInfo =
            new CosmosEntityInformation<>(SpELPropertyStudent.class);
        cosmosTemplate.createContainerIfNotExists(propertyStudentInfo);

        final SpELPropertyStudent insertedRecord =
            cosmosTemplate.insert(propertyStudentInfo.getContainerName(), TEST_PROPERTY_STUDENT, null);
        assertNotNull(insertedRecord);

        final SpELPropertyStudent readRecord =
            cosmosTemplate.findById(TestConstants.DYNAMIC_PROPERTY_COLLECTION_NAME,
                insertedRecord.getId(), SpELPropertyStudent.class);
        assertNotNull(readRecord);
    }

}

