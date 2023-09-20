// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.repository.PersonRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CosmosRepositoryFactoryBeanUnitTest {

    @Mock
    CosmosTemplate template;

    @Test
    public void testCreateRepositoryFactory() {
        final CosmosRepositoryFactoryBean<?, ?, ?> factoryBean =
                new CosmosRepositoryFactoryBean<>(PersonRepository.class);
        factoryBean.setCosmosOperations(template);
        final RepositoryFactorySupport factory = factoryBean.createRepositoryFactory();
        assertThat(factory).isNotNull();
    }
}
