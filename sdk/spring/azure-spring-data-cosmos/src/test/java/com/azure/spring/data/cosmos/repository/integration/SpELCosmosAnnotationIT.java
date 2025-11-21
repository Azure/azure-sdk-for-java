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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositorySpELConfig.class)
public class SpELCosmosAnnotationIT {

    
    public static final IntegrationTestCollectionManager collectionManager = new IntegrationTestCollectionManager();

    private static final SpELPropertyStudent TEST_PROPERTY_STUDENT = new SpELPropertyStudent(TestConstants.ID_1,
        TestConstants.FIRST_NAME, TestConstants.LAST_NAME);

    @Autowired
    private CosmosTemplate cosmosTemplate;

    @BeforeEach
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

