// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.support;

import com.microsoft.azure.spring.data.cosmosdb.core.CosmosTemplate;
import com.microsoft.azure.spring.data.cosmosdb.domain.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.core.EntityInformation;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class CosmosRepositoryFactoryUnitTest {

    @Mock
    CosmosTemplate dbTemplate;

    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void useMappingCosmosDBEntityInfoIfMappingContextSet() {
        final CosmosRepositoryFactory factory = new CosmosRepositoryFactory(dbTemplate, applicationContext);
        final EntityInformation<Person, String> entityInfo = factory.getEntityInformation(Person.class);
        assertTrue(entityInfo instanceof CosmosEntityInformation);
    }
}
