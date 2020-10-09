// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmos;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.microsoft.azure.spring.autoconfigure.cosmos.domain.Person;
import com.microsoft.azure.spring.autoconfigure.cosmos.domain.PersonRepository;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class CosmosRepositoriesAutoConfigurationUnitTest {

    private AnnotationConfigApplicationContext context;

    @InjectMocks
    private CosmosTemplate cosmosTemplate;

    @After
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

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void autConfigNotKickInIfManualConfigDidNotCreateRepositories() throws Exception {
        prepareApplicationContext(InvalidCustomConfiguration.class);
        this.context.getBean(PersonRepository.class);
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

