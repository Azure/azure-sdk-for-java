// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureServiceClientBuilderFactoryConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureTokenCredentialAutoConfiguration.class));

    @Test
    void defaultAzureCredentialSetToBuilderFactories() {
        TestCosmosClientBuilderFactory builderFactory = new TestCosmosClientBuilderFactory(new AzureCosmosProperties());
        contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(TestCosmosClientBuilderFactory.class, () -> builderFactory)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceClientBuilderFactoryPostProcessor.class);
                assertThat(context).hasSingleBean(TokenCredential.class);

                TokenCredential tokenCredential = context.getBean(TokenCredential.class);
                assertTrue(tokenCredential instanceof DefaultAzureCredential);
                assertEquals(builderFactory.getDefaultTokenCredential(), tokenCredential);
            });
    }

    @Test
    void defaultAzureCredentialResolverSetToBuilderFactories() {
        TestCosmosClientBuilderFactory builderFactory = new TestCosmosClientBuilderFactory(new AzureCosmosProperties());
        contextRunner
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(TestCosmosClientBuilderFactory.class, () -> builderFactory)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceClientBuilderFactoryPostProcessor.class);
                assertThat(context).hasSingleBean(AzureTokenCredentialResolver.class);

                AzureTokenCredentialResolver tokenCredentialResolver = context.getBean(AzureTokenCredentialResolver.class);
                assertEquals(builderFactory.getAzureTokenCredentialResolver(), tokenCredentialResolver);
            });
    }
}
