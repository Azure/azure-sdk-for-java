// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.spring.autoconfigure.cosmos.domain.Person;
import com.azure.spring.autoconfigure.cosmos.domain.PersonRepository;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

@Disabled
public class CosmosRepositoriesAutoConfigurationUnitTest {

    private AnnotationConfigApplicationContext context;

    @InjectMocks
    private CosmosTemplate cosmosTemplate;

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

        Assertions.assertThat(this.context.getBean(PersonRepository.class)).isNotNull();
    }

    @Test
    public void autConfigNotKickInIfManualConfigDidNotCreateRepositories() throws Exception {
        prepareApplicationContext(InvalidCustomConfiguration.class);

        org.junit.jupiter.api.Assertions.assertThrows(NoSuchBeanDefinitionException.class,
            () -> this.context.getBean(PersonRepository.class));
    }

    private void prepareApplicationContext(Class<?>... configurationClasses) {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(configurationClasses);
        this.context.register(CosmosRepositoriesAutoConfiguration.class);
        this.context.getBeanFactory().registerSingleton(CosmosTemplate.class.getName(), cosmosTemplate);
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

