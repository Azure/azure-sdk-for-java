// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosPropertiesTest.TEST_URI_HTTPS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
class AzureCosmosAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureCosmosAutoConfiguration.class));

    @Test
    void configureWithoutCosmosClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(CosmosClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosProperties.class));
    }

    @Test
    void configureWithCosmosDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosProperties.class));
    }

    @Test
    void configureWithoutUri() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosProperties.class));
    }

    @Test
    void configureWithUri() {
        final CosmosClientBuilder mockCosmosClientBuilder = mock(CosmosClientBuilder.class);
        when(mockCosmosClientBuilder.buildClient()).thenReturn(mock(CosmosClient.class));
        when(mockCosmosClientBuilder.buildAsyncClient()).thenReturn(mock(CosmosAsyncClient.class));

        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.uri=" + TEST_URI_HTTPS)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(CosmosClientBuilder.class, () -> mockCosmosClientBuilder)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                assertThat(context).hasSingleBean(CosmosClientBuilderFactory.class);
                assertThat(context).hasSingleBean(CosmosClientBuilder.class);
                assertThat(context).hasSingleBean(CosmosClient.class);
                assertThat(context).hasSingleBean(CosmosAsyncClient.class);
            });
    }

    @Test
    void configureAzureCosmosProperties() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("azure-client-id");
        azureProperties.getCredential().setClientSecret("azure-client-secret");
        azureProperties.getRetry().getBackoff().setDelay(Duration.ofSeconds(2));

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
            .withPropertyValues("spring.cloud.azure.cosmos.credential.client-id=cosmos-client-id",
                                "spring.cloud.azure.cosmos.retry.backoff.delay=2m",
                                "spring.cloud.azure.cosmos.uri=" + TEST_URI_HTTPS,
                                "spring.cloud.azure.cosmos.key=cosmos-key"
                                )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                final AzureCosmosProperties properties = context.getBean(AzureCosmosProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("cosmos-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("azure-client-secret");
                assertThat(properties).extracting("retry.backoff.delay").isEqualTo(Duration.ofMinutes(2));
                assertThat(properties).extracting("uri").isEqualTo(TEST_URI_HTTPS);
                assertThat(properties).extracting("key").isEqualTo("cosmos-key");

                assertThat(azureProperties.getCredential().getClientId()).isEqualTo("azure-client-id");
            });
    }

}
