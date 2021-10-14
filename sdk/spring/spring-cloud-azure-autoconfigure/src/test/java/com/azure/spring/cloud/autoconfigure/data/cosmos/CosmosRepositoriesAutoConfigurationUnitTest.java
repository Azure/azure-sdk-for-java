// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.cloud.autoconfigure.data.cosmos.domain.Person;
import com.azure.spring.cloud.autoconfigure.data.cosmos.domain.PersonRepository;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CosmosRepositoriesAutoConfigurationUnitTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CosmosRepositoriesAutoConfiguration.class));

    private CosmosTemplate cosmosTemplate;

    @BeforeAll
    void beforeAll() {
        cosmosTemplate = mock(CosmosTemplate.class);
        CosmosMappingContext mappingContext = new CosmosMappingContext();
        MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, new ObjectMapper());

        when(cosmosTemplate.getContainerProperties(any())).thenReturn(mock(CosmosContainerProperties.class));
        when(cosmosTemplate.getConverter()).thenReturn(cosmosConverter);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testDefaultRepositoryConfiguration() {
        this.contextRunner
            .withBean(CosmosTemplate.class, () -> cosmosTemplate)
            .withUserConfiguration(TestConfiguration.class)
            .run(context -> assertThat(context).hasSingleBean(PersonRepository.class));
    }

    @Test
    void autoConfigNotKickInIfManualConfigDidNotCreateRepositories() throws Exception {
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
    @TestAutoConfigurationPackage(CosmosRepositoriesAutoConfigurationUnitTest.class)
    static class InvalidCustomConfiguration {

    }

}

