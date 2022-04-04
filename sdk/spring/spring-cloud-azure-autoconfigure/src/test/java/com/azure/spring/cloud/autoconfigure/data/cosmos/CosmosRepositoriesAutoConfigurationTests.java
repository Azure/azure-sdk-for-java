// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.cloud.autoconfigure.data.cosmos.domain.Person;
import com.azure.spring.cloud.autoconfigure.data.cosmos.domain.PersonRepository;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.config.CosmosRepositoryConfigurationExtension;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.support.CosmosRepositoryFactoryBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CosmosRepositoriesAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CosmosRepositoriesAutoConfiguration.class));

    private CosmosTemplate cosmosTemplate;

    @BeforeEach
    void beforeEach() {
        cosmosTemplate = mock(CosmosTemplate.class);
        CosmosMappingContext mappingContext = new CosmosMappingContext();
        MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, new ObjectMapper());

        when(cosmosTemplate.getContainerProperties(any())).thenReturn(mock(CosmosContainerProperties.class));
        when(cosmosTemplate.getConverter()).thenReturn(cosmosConverter);
    }

    @Test
    void enabledDefaultRepositoryConfiguration() {
        this.contextRunner
            .withBean(CosmosTemplate.class, () -> cosmosTemplate)
            .withUserConfiguration(TestConfiguration.class)
            .run(context -> assertThat(context).hasSingleBean(PersonRepository.class));
    }

    @Test
    void disabledWhenCosmosRepositoryMissing() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(CosmosRepository.class))
            .withBean(CosmosTemplate.class, () -> cosmosTemplate)
            .withUserConfiguration(TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(PersonRepository.class));
    }

    @Test
    void disabledWhenCosmosRepositoryFactoryBeanExists() {
        this.contextRunner
            .withBean(CosmosTemplate.class, () -> cosmosTemplate)
            .withBean(CosmosRepositoryFactoryBean.class, () -> mock(CosmosRepositoryFactoryBean.class))
            .withUserConfiguration(TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(PersonRepository.class));
    }

    @Test
    void disabledWhenCosmosRepositoryConfigurationExtensionBeanExists() {
        this.contextRunner
            .withBean(CosmosTemplate.class, () -> cosmosTemplate)
            .withBean(CosmosRepositoryConfigurationExtension.class, () -> mock(CosmosRepositoryConfigurationExtension.class))
            .withUserConfiguration(TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(PersonRepository.class));
    }

    @Test
    void disabledWhenPropertiesDisabled() {
        this.contextRunner
            .withBean(CosmosTemplate.class, () -> cosmosTemplate)
            .withPropertyValues("spring.cloud.azure.cosmos.repositories.enabled=false")
            .withUserConfiguration(TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(PersonRepository.class));
    }

    @Test
    void disabledWhenCosmosTemplateBeanMissing() {
        this.contextRunner
            .withUserConfiguration(TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(PersonRepository.class));
    }

    @Test
    void autoConfigNotKickInIfManualConfigDidNotCreateRepositories() {
        this.contextRunner
            .withBean(CosmosTemplate.class, () -> cosmosTemplate)
            .withUserConfiguration(InvalidCustomConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(PersonRepository.class));
    }


    @Configuration
    @TestAutoConfigurationPackage(Person.class)
    static class TestConfiguration {
    }

    @Configuration
    @EnableCosmosRepositories("foo.bar")
    @TestAutoConfigurationPackage(CosmosRepositoriesAutoConfigurationTests.class)
    static class InvalidCustomConfiguration {

    }

}

