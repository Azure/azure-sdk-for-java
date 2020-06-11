// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.support;

import com.microsoft.azure.spring.data.cosmosdb.core.CosmosTemplate;
import com.microsoft.azure.spring.data.cosmosdb.repository.repository.PersonRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CosmosRepositoryFactoryBeanUnitTest {
    @Mock
    CosmosTemplate dbTemplate;

    @Test
    public void testCreateRepositoryFactory() {
        final CosmosRepositoryFactoryBean factoryBean =
                new CosmosRepositoryFactoryBean(PersonRepository.class);
        final RepositoryFactorySupport factory = factoryBean.createRepositoryFactory();
        assertThat(factory).isNotNull();
    }
}
