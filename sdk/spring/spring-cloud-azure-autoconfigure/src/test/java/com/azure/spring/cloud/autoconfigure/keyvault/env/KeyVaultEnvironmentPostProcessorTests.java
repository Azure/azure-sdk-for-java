// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.env;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.Iterator;

import static com.azure.spring.cloud.autoconfigure.keyvault.env.KeyVaultPropertySource.DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

class KeyVaultEnvironmentPostProcessorTests {

    private final SpringApplication application = new SpringApplication();
    private KeyVaultEnvironmentPostProcessor processor;
    private MockEnvironment environment;
    private MutablePropertySources propertySources;

    @BeforeEach
    void setup() {
        processor = spy(new KeyVaultEnvironmentPostProcessor(new DeferredLog()));
        environment = new MockEnvironment();
        propertySources = environment.getPropertySources();
    }

    @Test
    void postProcessorHasConfiguredOrder() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor(new DeferredLog());
        assertEquals(processor.getOrder(), KeyVaultEnvironmentPostProcessor.ORDER);
    }

    @Test
    void keyVaultClientIsNotAvailable() {
        try (MockedStatic<ClassUtils> classUtils = mockStatic(ClassUtils.class)) {
            classUtils.when(() ->
                          ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient",
                              this.getClass().getClassLoader()))
                      .thenReturn(false);
            processor.postProcessEnvironment(this.environment, this.application);
            final MutablePropertySources sources = this.environment.getPropertySources();
            assertFalse(sources.contains(DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME));
        }
    }

    @Test
    void sourcesNotExistsWhenConfigureEnabledFalse() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.enabled", "false");
        processor.postProcessEnvironment(this.environment, this.application);
        final MutablePropertySources sources = this.environment.getPropertySources();
        assertFalse(sources.contains(DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME));
    }

    @Test
    void sourcesNotExistsWhenConfigurePropertySourceEnabledFalseAndPropertySourcesEmpty() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.propertySourceEnabled", "false");
        processor.postProcessEnvironment(this.environment, this.application);
        final MutablePropertySources sources = this.environment.getPropertySources();
        assertFalse(sources.contains(DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME));
    }

    @Test
    void defaultPropertySourcesAdded() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint",
            "https://test.vault.azure.net/");
        SecretClient secretClient = mock(SecretClient.class);
        doReturn(secretClient).when(processor).buildSecretClient(any(AzureKeyVaultSecretProperties.class));
        processor.postProcessEnvironment(this.environment, this.application);
        final MutablePropertySources sources = this.environment.getPropertySources();
        assertTrue(sources.contains(DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME));
    }

    @Test
    void configuredPropertySourcesAddedWhenEnabled() {
        String propertySourcesOne = "testkeyOne";
        String propertySourcesTwo = "testkeyTwo";
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", propertySourcesOne);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint",
            "https://test.vault.azure.net/");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].enabled", "false");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", propertySourcesTwo);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint",
            "https://test2.vault.azure.net/");
        SecretClient secretClient = mock(SecretClient.class);
        doReturn(secretClient).when(processor).buildSecretClient(any(AzureKeyVaultSecretProperties.class));
        processor.postProcessEnvironment(this.environment, this.application);
        final MutablePropertySources sources = this.environment.getPropertySources();
        assertTrue(sources.contains(propertySourcesOne));
        assertFalse(sources.contains(propertySourcesTwo));
    }

    @Test
    void configuredPropertySourcesIsFirst() {
        String sourceName = "testkey";
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", sourceName);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint",
            "https://test.vault.azure.net/");
        SecretClient secretClient = mock(SecretClient.class);
        doReturn(secretClient).when(processor).buildSecretClient(any(AzureKeyVaultSecretProperties.class));
        processor.postProcessEnvironment(this.environment, this.application);
        final MutablePropertySources sources = this.environment.getPropertySources();
        Iterator<PropertySource<?>> iterator = sources.iterator();
        assertTrue(iterator.next().getName().equals(sourceName));
    }

    @Test
    void configuredPropertySourcesLocationIsAfterSystemEnvironmentPropertySources() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint",
            "https://test.vault.azure.net/");
        SecretClient secretClient = mock(SecretClient.class);
        doReturn(secretClient).when(processor).buildSecretClient(any(AzureKeyVaultSecretProperties.class));
        propertySources.addLast(new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, Collections.emptyMap()));
        processor.postProcessEnvironment(this.environment, this.application);
        final MutablePropertySources sources = this.environment.getPropertySources();
        Iterator<PropertySource<?>> it = sources.iterator();
        while (it.hasNext()) {
            PropertySource<?> propertySource = it.next();
            if (SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME.equals(propertySource.getName())) {
                break;
            }
        }
        assertTrue(DEFAULT_AZURE_KEYVAULT_PROPERTYSOURCE_NAME.equals(it.next().getName()));
    }
}

