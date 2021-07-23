// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.unity;

import com.azure.cosmos.ConnectionMode;
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
    public void testRelaxBinding() {
        Properties properties = new Properties();
        properties.setProperty("azure.storage.accountKey", "fakekey");
        properties.put("azure.cosmos.connection-mode", ConnectionMode.DIRECT);
        properties.put("azure.cosmos.allow_telemetry", false);
        properties.put("azure.keyvault.REFRESH_INTERVAL", 1000L);
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertTrue(environment.getPropertySources().contains(processor.getClass().getName()));
        assertEquals("fakekey", environment.getProperty("spring.cloud.azure.storage.account-key"));
        assertEquals(ConnectionMode.DIRECT, environment.getProperty("spring.cloud.azure.cosmos.connection-mode", ConnectionMode.class));
        assertEquals(false, environment.getProperty("spring.cloud.azure.cosmos.allow-telemetry", Boolean.class));
        assertEquals(1000L, environment.getProperty("spring.cloud.azure.keyvault.refresh-interval", Long.class));

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
        assertNull(environment.getProperty("spring.cloud.azure.keyvault.one.uri"));
        assertNull(environment.getProperty("spring.cloud.azure.keyvault.two.credential.client-id"));
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
