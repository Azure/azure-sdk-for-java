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
        properties.setProperty("azure.storage.account-key", "fakeuri");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertTrue(environment.getPropertySources().contains(processor.getClass().getName()));
        assertEquals("fakeuri", environment.getProperty("spring.cloud.azure.storage.account-key"));

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
