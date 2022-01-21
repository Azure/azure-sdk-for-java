// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.service.implementation.cosmos.CosmosClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosPropertiesTest.TEST_ENDPOINT_HTTPS;
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
    void configureWithoutEndpoint() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosProperties.class));
    }

    @Test
    void configureWithEndpoint() {
        final CosmosClientBuilder mockCosmosClientBuilder = mock(CosmosClientBuilder.class);
        when(mockCosmosClientBuilder.buildClient()).thenReturn(mock(CosmosClient.class));
        when(mockCosmosClientBuilder.buildAsyncClient()).thenReturn(mock(CosmosAsyncClient.class));

        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS)
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
        azureProperties.getProxy().setHostname("localhost");
        azureProperties.getProxy().getHttp().setNonProxyHosts("localhost");

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.cosmos.credential.client-id=cosmos-client-id",
                "spring.cloud.azure.cosmos.proxy.nonProxyHosts=127.0.0.1",
                "spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS,
                "spring.cloud.azure.cosmos.key=cosmos-key"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                final AzureCosmosProperties properties = context.getBean(AzureCosmosProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("cosmos-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("azure-client-secret");
                assertThat(properties).extracting("proxy.hostname").isEqualTo("localhost");
                assertThat(properties).extracting("proxy.nonProxyHosts").isEqualTo("127.0.0.1");
                assertThat(properties).extracting("endpoint").isEqualTo(TEST_ENDPOINT_HTTPS);
                assertThat(properties).extracting("key").isEqualTo("cosmos-key");

                assertThat(azureProperties.getCredential().getClientId()).isEqualTo("azure-client-id");
            });
    }

    @Test
    void customizerShouldBeCalled() {
        CosmosBuilderCustomizer customizer = new CosmosBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS,
                "spring.cloud.azure.cosmos.key=cosmos-key"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", CosmosBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", CosmosBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        CosmosBuilderCustomizer customizer = new CosmosBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS,
                "spring.cloud.azure.cosmos.key=cosmos-key"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", CosmosBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", CosmosBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class CosmosBuilderCustomizer extends TestBuilderCustomizer<CosmosClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

}
