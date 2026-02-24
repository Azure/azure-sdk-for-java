// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.core.EntityInformation;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CosmosRepositoryFactoryUnitTest {

    @Mock
    CosmosTemplate cosmosTemplate;

    @Test
    public void useMappingCosmosDBEntityInfoIfMappingContextSet() {
        final CosmosRepositoryFactory factory = new CosmosRepositoryFactory(cosmosTemplate);
        final EntityInformation<Person, String> entityInfo = factory.getEntityInformation(Person.class);
        assertTrue(entityInfo instanceof CosmosEntityInformation);
    }
}
