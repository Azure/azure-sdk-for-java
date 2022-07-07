// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import static com.azure.spring.cloud.autoconfigure.jdbc.AzureJdbcEnvironmentPostProcessor.ENHANCED_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

class AzureJdbcEnvironmentPostProcessorTest {

    private static final String AZURE_DATABASE = "AZURE_DATABASE";
    private final SpringApplication application = new SpringApplication();
    private AzureJdbcEnvironmentPostProcessor processor;
    private MockEnvironment environment;
    private MutablePropertySources propertySources;

    @BeforeEach
    void beforeEach() {
        processor = spy(new AzureJdbcEnvironmentPostProcessor(new DeferredLog()));
        environment = new MockEnvironment();
        propertySources = environment.getPropertySources();
    }

    @Test
    void postProcessorHasConfiguredOrder() {
        final AzureJdbcEnvironmentPostProcessor processor = new AzureJdbcEnvironmentPostProcessor();
        assertEquals(processor.getOrder(), ConfigDataEnvironmentPostProcessor.ORDER + 100);
    }

    @Test
    void passwordProvideTest() {
        environment.setProperty("spring.datasource.password", "mockPassword");
        processor.postProcessEnvironment(environment, application);
        assertFalse(propertySources.contains(AZURE_DATABASE));
    }

    @Test
    void NoSpringDataUrlProvidedTest() {
        processor.postProcessEnvironment(environment, application);
        assertFalse(propertySources.contains(AZURE_DATABASE));
    }

    @Test
    void mySqlJdbcTest(){
        environment.setProperty("spring.datasource.url", "jdbc:mysql://mockpostgresqlurl:3306/db");
        processor.postProcessEnvironment(environment, application);
        assertTrue(propertySources.contains(AZURE_DATABASE));
        String enhancedUrl = environment.getProperty("spring.datasource.url");
        for (String key : ENHANCED_PROPERTIES.get(DatabaseType.MYSQL).keySet()) {
            assertTrue((enhancedUrl.contains(key)));
        }
    }

    @Test
    void postgreSqlJdbcTest(){
        environment.setProperty("spring.datasource.url", "jdbc:postgresql://mockpostgresqlurl:5432/postgres");
        processor.postProcessEnvironment(environment, application);
        assertTrue(propertySources.contains(AZURE_DATABASE));
        String enhancedUrl = environment.getProperty("spring.datasource.url");
        for (String key : ENHANCED_PROPERTIES.get(DatabaseType.POSTGRESQL).keySet()) {
            assertTrue((enhancedUrl.contains(key)));
        }
    }

    @Test
    void invalidConnectionStringTest(){
        environment.setProperty("spring.datasource.url", "jdbc:PostMresql://mockpostgresqlurl:5432/postgres");
        assertThrows(IllegalArgumentException.class, () -> processor.postProcessEnvironment(environment, application));
    }

}
