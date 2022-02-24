// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.AzureGlobalProperties;
import com.azure.spring.service.implementation.cosmos.CosmosClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
class AzureCosmosAutoConfigurationTests {

    static final String TEST_ENDPOINT_HTTPS = "https://test.https.documents.azure.com:443/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureCosmosAutoConfiguration.class));

    @Test
    void configureWithoutCosmosClientBuilder() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS)
            .withClassLoader(new FilteredClassLoader(CosmosClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosProperties.class));
    }

    @Test
    void configureWithCosmosDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=false",
                "spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS)
            .run(context -> assertThat(context).doesNotHaveBean(AzureCosmosProperties.class));
    }

    @Test
    void configureWithoutEndpoint() {
        this.contextRunner
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

    @Test
    void configurationPropertiesShouldBind() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.cosmos.endpoint=test-endpoint",
                "spring.cloud.azure.cosmos.credential.client-id=cosmos-client-id",
                "spring.cloud.azure.cosmos.proxy.nonProxyHosts=127.0.0.1",
                "spring.cloud.azure.cosmos.key=cosmos-key",
                "spring.cloud.azure.cosmos.gateway-connection.max-connection-pool-size=1",
                "spring.cloud.azure.cosmos.gateway-connection.idle-connection-timeout=2s",
                "spring.cloud.azure.cosmos.direct-connection.connection-endpoint-rediscovery-enabled=true",
                "spring.cloud.azure.cosmos.direct-connection.connect-timeout=3s",
                "spring.cloud.azure.cosmos.direct-connection.idle-connection-timeout=4s",
                "spring.cloud.azure.cosmos.direct-connection.idle-endpoint-timeout=5s",
                "spring.cloud.azure.cosmos.direct-connection.network-request-timeout=6s",
                "spring.cloud.azure.cosmos.direct-connection.max-connections-per-endpoint=7",
                "spring.cloud.azure.cosmos.direct-connection.max-requests-per-connection=8",
                "spring.cloud.azure.cosmos.throttling-retry-options.max-retry-attempts-on-throttled-requests=9",
                "spring.cloud.azure.cosmos.throttling-retry-options.max-retry-wait-time=10s"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                AzureCosmosProperties properties = context.getBean(AzureCosmosProperties.class);
                assertEquals("test-endpoint", properties.getEndpoint());
                assertEquals("cosmos-key", properties.getKey());
                assertEquals("cosmos-client-id", properties.getCredential().getClientId());
                assertEquals("127.0.0.1", properties.getProxy().getNonProxyHosts());
                assertEquals(1, properties.getGatewayConnection().getMaxConnectionPoolSize());
                assertEquals(Duration.ofSeconds(2), properties.getGatewayConnection().getIdleConnectionTimeout());
                assertTrue(properties.getDirectConnection().getConnectionEndpointRediscoveryEnabled());
                assertEquals(Duration.ofSeconds(3), properties.getDirectConnection().getConnectTimeout());
                assertEquals(Duration.ofSeconds(4), properties.getDirectConnection().getIdleConnectionTimeout());
                assertEquals(Duration.ofSeconds(5), properties.getDirectConnection().getIdleEndpointTimeout());
                assertEquals(Duration.ofSeconds(6), properties.getDirectConnection().getNetworkRequestTimeout());
                assertEquals(7, properties.getDirectConnection().getMaxConnectionsPerEndpoint());
                assertEquals(8, properties.getDirectConnection().getMaxRequestsPerConnection());
                assertEquals(9, properties.getThrottlingRetryOptions().getMaxRetryAttemptsOnThrottledRequests());
                assertEquals(Duration.ofSeconds(10), properties.getThrottlingRetryOptions().getMaxRetryWaitTime());
            });
    }

    private static class CosmosBuilderCustomizer extends TestBuilderCustomizer<CosmosClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

}
