// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.spring.cloud.autoconfigure.implementation.data.cosmos.domain.Person;
import com.azure.spring.cloud.autoconfigure.implementation.data.cosmos.domain.ReactivePersonRepository;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.ReactiveCosmosRepositoryConfigurationExtension;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.azure.spring.data.cosmos.repository.support.ReactiveCosmosRepositoryFactoryBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CosmosReactiveRepositoriesAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CosmosReactiveRepositoriesAutoConfiguration.class));
    private ReactiveCosmosTemplate reactiveCosmosTemplate;

    @BeforeEach
    void beforeEach() {
        reactiveCosmosTemplate = mock(ReactiveCosmosTemplate.class);
        CosmosMappingContext mappingContext = new CosmosMappingContext();
        MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, new ObjectMapper());
        when(reactiveCosmosTemplate.createContainerIfNotExists(any(CosmosEntityInformation.class)))
            .thenReturn(Mono.just(mock(CosmosContainerResponse.class)));
        when(reactiveCosmosTemplate.getContainerProperties(any()))
            .thenReturn(Mono.just(mock(CosmosContainerProperties.class)));
        when(reactiveCosmosTemplate.getConverter()).thenReturn(cosmosConverter);
    }

    @Test
    void enabledDefaultRepositoryConfiguration() {
        this.contextRunner
            .withBean(ReactiveCosmosTemplate.class, () -> reactiveCosmosTemplate)
            .withUserConfiguration(CosmosReactiveRepositoriesAutoConfigurationTests.TestConfiguration.class)
            .run((context) -> assertThat(context).hasSingleBean(ReactivePersonRepository.class));
    }

    @Test
    void disabledWhenReactiveCosmosRepositoryMissing() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ReactiveCosmosRepository.class))
            .withBean(ReactiveCosmosTemplate.class, () -> reactiveCosmosTemplate)
            .withUserConfiguration(CosmosReactiveRepositoriesAutoConfigurationTests.TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(ReactivePersonRepository.class));
    }

    @Test
    void disabledWhenSpecialBeanExist() {
        this.contextRunner
            .withBean(ReactiveCosmosRepositoryFactoryBean.class, () -> mock(ReactiveCosmosRepositoryFactoryBean.class))
            .withBean(ReactiveCosmosTemplate.class, () -> reactiveCosmosTemplate)
            .withUserConfiguration(CosmosReactiveRepositoriesAutoConfigurationTests.TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(ReactivePersonRepository.class));

        this.contextRunner
            .withBean(ReactiveCosmosRepositoryConfigurationExtension.class, () -> mock(ReactiveCosmosRepositoryConfigurationExtension.class))
            .withBean(ReactiveCosmosTemplate.class, () -> reactiveCosmosTemplate)
            .withUserConfiguration(CosmosReactiveRepositoriesAutoConfigurationTests.TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(ReactivePersonRepository.class));
    }

    @Test
    void disabledWhenReactiveCosmosTemplateMissing() {
        this.contextRunner
            .withUserConfiguration(CosmosReactiveRepositoriesAutoConfigurationTests.TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(ReactivePersonRepository.class));
    }

    @Test
    void disabledWhenPropertiesDisabled() {
        this.contextRunner
            .withBean(ReactiveCosmosTemplate.class, () -> reactiveCosmosTemplate)
            .withPropertyValues("spring.cloud.azure.cosmos.repositories.enabled=false")
            .withUserConfiguration(CosmosReactiveRepositoriesAutoConfigurationTests.TestConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(ReactivePersonRepository.class));
    }

    @Test
    void autoConfigNotKickInIfManualConfigDidNotCreateRepositories() {
        this.contextRunner
            .withBean(ReactiveCosmosTemplate.class, () -> reactiveCosmosTemplate)
            .withUserConfiguration(CosmosReactiveRepositoriesAutoConfigurationTests.InvalidCustomConfiguration.class)
            .run(context -> assertThat(context).doesNotHaveBean(ReactivePersonRepository.class));
    }

    @Configuration
    @TestAutoConfigurationPackage(Person.class)
    static class TestConfiguration {
    }

    @Configuration
    @EnableReactiveCosmosRepositories("foo.bar")
    @TestAutoConfigurationPackage(CosmosReactiveRepositoriesAutoConfigurationTests.class)
    static class InvalidCustomConfiguration {

    }
}
