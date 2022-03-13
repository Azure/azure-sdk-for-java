// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.autoconfigure.cosmos.domain.Person;
import com.azure.spring.autoconfigure.cosmos.domain.PersonRepository;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CosmosRepositoriesAutoConfigurationUnitTest {

    private AnnotationConfigApplicationContext context;

    private CosmosTemplate cosmosTemplate;

    @BeforeAll
    public void beforeAll() {
        cosmosTemplate = mock(CosmosTemplate.class);
        CosmosMappingContext mappingContext = new CosmosMappingContext();
        MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, new ObjectMapper());

        when(cosmosTemplate.getContainerProperties(any())).thenReturn(mock(CosmosContainerProperties.class));
        when(cosmosTemplate.getConverter()).thenReturn(cosmosConverter);
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void testDefaultRepositoryConfiguration() throws Exception {
        prepareApplicationContext(TestConfiguration.class);
        assertNotNull(this.context.getBean(PersonRepository.class));
    }

    @Test
    public void autoConfigNotKickInIfManualConfigDidNotCreateRepositories() throws Exception {
        prepareApplicationContext(InvalidCustomConfiguration.class);
        assertThrows(NoSuchBeanDefinitionException.class,
            () -> this.context.getBean(PersonRepository.class));
    }

    private void prepareApplicationContext(Class<?>... configurationClasses) {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(configurationClasses);
        this.context.register(CosmosRepositoriesAutoConfiguration.class);
        this.context.getBeanFactory().registerSingleton("cosmosTemplate", cosmosTemplate);
        this.context.refresh();

    }

    @Configuration
    @TestAutoConfigurationPackage(Person.class)
    protected static class TestConfiguration {
    }

    @Configuration
    @EnableCosmosRepositories("foo.bar")
    @TestAutoConfigurationPackage(CosmosRepositoriesAutoConfigurationUnitTest.class)
    protected static class InvalidCustomConfiguration {

    }

}

