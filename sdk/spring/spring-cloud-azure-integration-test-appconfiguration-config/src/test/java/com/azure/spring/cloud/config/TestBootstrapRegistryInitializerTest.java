// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.SecretClientCustomizer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TestBootstrapRegistryInitializerTest {

    @Test
    void discoversInitializerThroughSpringFactories() {
        assertTrue(SpringFactoriesLoader.loadFactories(BootstrapRegistryInitializer.class, getClass().getClassLoader())
            .stream().anyMatch(TestBootstrapRegistryInitializer.class::isInstance));
    }

    @Test
    void registersSharedCustomizersBeforeConfigDataLoading() {
        DefaultBootstrapContext context = new DefaultBootstrapContext();
        new TestBootstrapRegistryInitializer().initialize(context);

        ConfigurationClientCustomizer configurationCustomizer = context.get(ConfigurationClientCustomizer.class);
        SecretClientCustomizer secretCustomizer = context.get(SecretClientCustomizer.class);
        assertSame(configurationCustomizer, secretCustomizer);

        ConfigurationClientBuilder configurationBuilder = mock(ConfigurationClientBuilder.class);
        SecretClientBuilder secretBuilder = mock(SecretClientBuilder.class);
        configurationCustomizer.customize(configurationBuilder, "https://config.example");
        secretCustomizer.customize(secretBuilder, "https://test.vault.azure.net");

        ArgumentCaptor<TokenCredential> credential = ArgumentCaptor.forClass(TokenCredential.class);
        verify(configurationBuilder).credential(credential.capture());
        verify(configurationBuilder).addPolicy(context.get(PaginationPagePolicy.class));
        assertInstanceOf(DefaultAzureCredential.class, credential.getValue());
        verify(secretBuilder).credential(credential.getValue());
    }
}
