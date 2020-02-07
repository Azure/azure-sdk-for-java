/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.azure.data.cosmos.CosmosClient;
import com.microsoft.azure.spring.autoconfigure.cosmosdb.domain.Person;
import com.microsoft.azure.spring.autoconfigure.cosmosdb.domain.PersonRepository;
import com.microsoft.azure.spring.data.cosmosdb.CosmosDbFactory;
import com.microsoft.azure.spring.data.cosmosdb.core.CosmosTemplate;
import com.microsoft.azure.spring.data.cosmosdb.core.convert.MappingCosmosConverter;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.EnableCosmosRepositories;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class CosmosDbRepositoriesAutoConfigurationUnitTest {

    private AnnotationConfigApplicationContext context;

    @InjectMocks
    private CosmosTemplate cosmosTemplate;

    @Mock
    private CosmosDbFactory cosmosDbFactory;

    @Mock
    private MappingCosmosConverter mappingCosmosConverter;

    @Mock
    private CosmosClient cosmosClient;

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void testDefaultRepositoryConfiguration() throws Exception {
        prepareApplicationContext(TestConfiguration.class);

        assertThat(this.context.getBean(PersonRepository.class)).isNotNull();
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void autConfigNotKickInIfManualConfigDidNotCreateRepositories() throws Exception {
        prepareApplicationContext(InvalidCustomConfiguration.class);
        this.context.getBean(PersonRepository.class);
    }

    private void prepareApplicationContext(Class<?>... configurationClasses) {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(configurationClasses);
        this.context.register(CosmosDbRepositoriesAutoConfiguration.class);
        this.context.getBeanFactory().registerSingleton(CosmosTemplate.class.getName(), cosmosTemplate);
        this.context.refresh();
    }

    @Configuration
    @TestAutoConfigurationPackage(Person.class)
    protected static class TestConfiguration {
    }

    @Configuration
    @EnableCosmosRepositories("foo.bar")
    @TestAutoConfigurationPackage(CosmosDbRepositoriesAutoConfigurationUnitTest.class)
    protected static class InvalidCustomConfiguration {

    }

}

