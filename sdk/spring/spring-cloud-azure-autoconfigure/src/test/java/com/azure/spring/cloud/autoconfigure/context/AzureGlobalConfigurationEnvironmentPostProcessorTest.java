// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID;

class AzureGlobalConfigurationEnvironmentPostProcessorTest {

    @Test
    void springPropertyShouldHaveValueIfAzureCoreEnvSet() {
        PropertiesPropertySource propertiesPropertySource = buildTestProperties(PROPERTY_AZURE_CLIENT_ID, "test-client-id");

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        Assertions.assertEquals("test-client-id", environment.getProperty(PROPERTY_AZURE_CLIENT_ID));
        Assertions.assertEquals("test-client-id", environment.getProperty("spring.cloud.azure.credential.client-id"));
    }

    @Test
    void springPropertyShouldHaveValueIfAzureSdkEnvSet() {
        PropertiesPropertySource propertiesPropertySource = buildTestProperties("AZURE_KEYVAULT_ENDPOINT", "test-endpoint");

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        Assertions.assertEquals("test-endpoint", environment.getProperty("AZURE_KEYVAULT_ENDPOINT"));
        Assertions.assertEquals("test-endpoint", environment.getProperty("spring.cloud.azure.keyvault.secret.endpoint"));
        Assertions.assertEquals("test-endpoint", environment.getProperty("spring.cloud.azure.keyvault.certificate.endpoint"));
    }

    @Test
    void azureCoreEnvShouldNotBeTakenIfSpringPropertiesSet() {
        Properties properties = new Properties();
        properties.put(PROPERTY_AZURE_CLIENT_ID, "core-client-id");
        properties.put("spring.cloud.azure.credential.client-id", "spring-client-id");
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        Assertions.assertEquals("core-client-id", environment.getProperty(PROPERTY_AZURE_CLIENT_ID));
        Assertions.assertEquals("spring-client-id", environment.getProperty("spring.cloud.azure.credential.client-id"));
    }

    @Test
    void azureSdkEnvShouldNotBeTakenIfSpringPropertiesSet() {
        Properties properties = new Properties();
        properties.put("AZURE_KEYVAULT_ENDPOINT", "sdk-endpoint");
        properties.put("spring.cloud.azure.keyvault.secret.endpoint", "spring-endpoint");
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        Assertions.assertEquals("sdk-endpoint", environment.getProperty("AZURE_KEYVAULT_ENDPOINT"));
        Assertions.assertEquals("spring-endpoint", environment.getProperty("spring.cloud.azure.keyvault.secret.endpoint"));
    }

    private PropertiesPropertySource buildTestProperties(String key, String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return new PropertiesPropertySource("test-properties", properties);
    }


    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource) {
        return getEnvironment(propertiesPropertySource, null);
    }

    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource,
                                                   EnvironmentPostProcessor environmentPostProcessor) {
        SpringApplication springApplication = new SpringApplicationBuilder()
            .sources(AzureGlobalConfigurationEnvironmentPostProcessorTest.class)
            .web(WebApplicationType.NONE).build();

        ConfigurableApplicationContext context = springApplication.run();

        if (propertiesPropertySource != null) {
            context.getEnvironment().getPropertySources().addFirst(propertiesPropertySource);
        }

        if (environmentPostProcessor == null) {
            environmentPostProcessor = new AzureGlobalConfigurationEnvironmentPostProcessor();
        }

        environmentPostProcessor.postProcessEnvironment(context.getEnvironment(), springApplication);

        ConfigurableEnvironment configurableEnvironment = context.getEnvironment();
        context.close();

        return configurableEnvironment;
    }

}
