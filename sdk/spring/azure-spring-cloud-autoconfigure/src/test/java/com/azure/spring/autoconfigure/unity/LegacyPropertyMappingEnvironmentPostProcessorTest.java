// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.unity;

import com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LegacyPropertyMappingEnvironmentPostProcessorTest {

    @Test
    public void testMapLegacyToCurrent() {
        LegacyPropertyMappingEnvironmentPostProcessor processor = new LegacyPropertyMappingEnvironmentPostProcessor();
        Properties properties = new Properties();
        properties.setProperty("azure.storage.account-key", "fakeuri");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);
        ConfigurableEnvironment environment = getEnvironment(propertySource, processor);

        assertEquals("fakeuri", environment.getProperty("spring.cloud.azure.storage.account-key"));

    }

    @Test
    public void postProcessorHasConfiguredOrder() {
        final LegacyPropertyMappingEnvironmentPostProcessor processor = new LegacyPropertyMappingEnvironmentPostProcessor();
        assertEquals(processor.getOrder(), LegacyPropertyMappingEnvironmentPostProcessor.DEFAULT_ORDER);
    }

    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource,
                                                   EnvironmentPostProcessor environmentPostProcessor) {
        SpringApplication springApplication = new SpringApplicationBuilder()
            .sources(LegacyPropertyMappingEnvironmentPostProcessor.class)
            .web(WebApplicationType.NONE).build();

        ConfigurableApplicationContext context = springApplication.run();
        ConfigurableEnvironment configurableEnvironment = context.getEnvironment();
        configurableEnvironment.getPropertySources().addFirst(propertiesPropertySource);
        environmentPostProcessor.postProcessEnvironment(configurableEnvironment, springApplication);
        context.close();

        return configurableEnvironment;
    }
}
