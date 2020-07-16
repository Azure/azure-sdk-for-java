// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.spring.data.cosmos.CosmosDbFactory;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.convert.ObjectMapperFactory;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.domain.SpELBeanStudent;
import com.azure.spring.data.cosmos.domain.SpELPropertyStudent;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.Persistent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class SpELCosmosDBAnnotationIT {
    private static final SpELPropertyStudent TEST_PROPERTY_STUDENT =
            new SpELPropertyStudent(TestConstants.ID_1, TestConstants.FIRST_NAME,
            TestConstants.LAST_NAME);

    @Value("${cosmosdb.uri}")
    private String dbUri;

    @Value("${cosmosdb.key}")
    private String dbKey;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CosmosTemplate cosmosTemplate;

    private static CosmosTemplate staticTemplate;
    private static CosmosEntityInformation<SpELPropertyStudent, String> cosmosEntityInformation;

    @Before
    public void setUp() {
        if (staticTemplate == null) {
            staticTemplate = cosmosTemplate;
        }
    }

    @AfterClass
    public static void afterClassCleanup() {
        if (cosmosEntityInformation != null) {
            staticTemplate.deleteContainer(cosmosEntityInformation.getContainerName());
        }
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
        final CosmosDBConfig dbConfig = CosmosDBConfig.builder(dbUri, dbKey, TestConstants.DB_NAME).build();
        final CosmosDbFactory dbFactory = new CosmosDbFactory(dbConfig);

        cosmosEntityInformation = new CosmosEntityInformation<>(SpELPropertyStudent.class);
        final CosmosMappingContext dbContext = new CosmosMappingContext();
        dbContext.setInitialEntitySet(new EntityScanner(this.applicationContext).scan(Persistent.class));

        final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        final MappingCosmosConverter mappingConverter = new MappingCosmosConverter(dbContext, objectMapper);
        staticTemplate = new CosmosTemplate(dbFactory, mappingConverter, TestConstants.DB_NAME);

        staticTemplate.createContainerIfNotExists(cosmosEntityInformation);

        final SpELPropertyStudent insertedRecord =
              staticTemplate.insert(cosmosEntityInformation.getContainerName(), TEST_PROPERTY_STUDENT, null);
        assertNotNull(insertedRecord);

        final SpELPropertyStudent readRecord =
              staticTemplate.findById(TestConstants.DYNAMIC_PROPERTY_COLLECTION_NAME,
                      insertedRecord.getId(), SpELPropertyStudent.class);
        assertNotNull(readRecord);
    }

}

