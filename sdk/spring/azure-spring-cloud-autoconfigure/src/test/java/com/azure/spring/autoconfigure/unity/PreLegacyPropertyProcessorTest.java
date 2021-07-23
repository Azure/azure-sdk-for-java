// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.unity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreLegacyPropertyProcessorTest {

    private PreLegacyPropertyEnvironmentPostProcessor processor = new PreLegacyPropertyEnvironmentPostProcessor();

    @Test
    public void postProcessorHasConfiguredOrder() {
        assertEquals(processor.getOrder(), PreLegacyPropertyEnvironmentPostProcessor.DEFAULT_ORDER);
    }

    @Test
    public void testMapLegacyToCurrent() {
        Properties properties = new Properties();
        properties.setProperty("azure.storage.account-key", "fakekey");
        properties.setProperty("azure.keyvault.uri", "fakeuri");
        properties.setProperty("spring.cloud.azure.keyvault.uri", "trueuri");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertTrue(environment.getPropertySources().contains(processor.getClass().getName()));
        assertEquals("fakekey", environment.getProperty("spring.cloud.azure.storage.account-key"));
        assertEquals("trueuri", environment.getProperty("spring.cloud.azure.keyvault.uri"));

    }

    @Test
    public void testMultipleKeyVaults() {
        Properties properties = new Properties();
        properties.setProperty("azure.keyvault.order", "one, two");
        properties.setProperty("spring.cloud.azure.keyvault.order", "three, four");
        properties.setProperty("azure.keyvault.one.uri", "uri");
        properties.setProperty("azure.keyvault.two.client-id", "id");
        properties.setProperty("azure.keyvault.three.client-key", "key");
        properties.setProperty("azure.keyvault.four.authority-host", "host");
        properties.setProperty("azure.keyvault.five.enabled", "true");

        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertTrue(environment.getPropertySources().contains(processor.getClass().getName()));
        assertEquals("uri", environment.getProperty("spring.cloud.azure.keyvault.one.uri"));
        assertEquals("id", environment.getProperty("spring.cloud.azure.keyvault.two.credential.client-id"));
        assertEquals("key", environment.getProperty("spring.cloud.azure.keyvault.three.credential.client-secret"));
        assertEquals("host", environment.getProperty("spring.cloud.azure.keyvault.four.environment.authority-host"));
        assertNull(environment.getProperty("spring.cloud.azure.keyvault.five.enabled"));

    }

    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource,
                                                   EnvironmentPostProcessor environmentPostProcessor) {
        SpringApplication springApplication = getSpringApplication(environmentPostProcessor.getClass());

        ConfigurableApplicationContext context = springApplication.run();
        ConfigurableEnvironment configurableEnvironment = context.getEnvironment();
        configurableEnvironment.getPropertySources().addFirst(propertiesPropertySource);
        environmentPostProcessor.postProcessEnvironment(configurableEnvironment, springApplication);
        context.close();

        return configurableEnvironment;
    }

    protected SpringApplication getSpringApplication(Class<?>... sources) {
        return new SpringApplicationBuilder().sources(sources).web(WebApplicationType.NONE).build();
    }
}
