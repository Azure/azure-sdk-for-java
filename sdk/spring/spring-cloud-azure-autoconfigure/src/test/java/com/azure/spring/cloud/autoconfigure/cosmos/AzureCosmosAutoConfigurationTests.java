// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.AbstractAzureServiceConfigurationTests;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.service.implementation.cosmos.CosmosClientBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
class AzureCosmosAutoConfigurationTests extends AbstractAzureServiceConfigurationTests<
    CosmosClientBuilderFactory, AzureCosmosProperties> {

    static final String TEST_ENDPOINT_HTTPS = "https://test.https.documents.azure.com:443/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureCosmosAutoConfiguration.class));

    @Override
    protected ApplicationContextRunner getMinimalContextRunner() {
        final CosmosClientBuilder mockCosmosClientBuilder = mock(CosmosClientBuilder.class);
        when(mockCosmosClientBuilder.buildClient()).thenReturn(mock(CosmosClient.class));
        when(mockCosmosClientBuilder.buildAsyncClient()).thenReturn(mock(CosmosAsyncClient.class));

        return this.contextRunner
            .withBean(CosmosClientBuilder.class, () -> mockCosmosClientBuilder)
            .withPropertyValues("spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS);
    }

    @Override
    protected String getPropertyPrefix() {
        return AzureCosmosProperties.PREFIX;
    }

    @Override
    protected Class<CosmosClientBuilderFactory> getBuilderFactoryType() {
        return CosmosClientBuilderFactory.class;
    }

    @Override
    protected Class<AzureCosmosProperties> getConfigurationPropertiesType() {
        return AzureCosmosProperties.class;
    }

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
    void configureAzureCosmosPropertiesWithGlobalDefaults() {
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
                assertThat(properties.getCredential().getClientId()).isEqualTo("cosmos-client-id");
                assertThat(properties.getCredential().getClientSecret()).isEqualTo("azure-client-secret");
                assertThat(properties.getProxy().getHostname()).isEqualTo("localhost");
                assertThat(properties.getProxy().getNonProxyHosts()).isEqualTo("127.0.0.1");
                assertThat(properties.getEndpoint()).isEqualTo(TEST_ENDPOINT_HTTPS);
                assertThat(properties.getKey()).isEqualTo("cosmos-key");

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
                "spring.cloud.azure.cosmos.credential.client-id=cosmos-client-id",
                "spring.cloud.azure.cosmos.proxy.nonProxyHosts=127.0.0.1",
                "spring.cloud.azure.cosmos.endpoint=test-endpoint",
                "spring.cloud.azure.cosmos.key=cosmos-key",
                "spring.cloud.azure.cosmos.database=test-database",
                "spring.cloud.azure.cosmos.resource-token=test-resource-token",
                "spring.cloud.azure.cosmos.client-telemetry-enabled=true",
                "spring.cloud.azure.cosmos.endpoint-discovery-enabled=true",
                "spring.cloud.azure.cosmos.connection-sharing-across-clients-enabled=true",
                "spring.cloud.azure.cosmos.content-response-on-write-enabled=true",
                "spring.cloud.azure.cosmos.multiple-write-regions-enabled=true",
                "spring.cloud.azure.cosmos.session-capturing-override-enabled=true",
                "spring.cloud.azure.cosmos.read-requests-fallback-enabled=true",
                "spring.cloud.azure.cosmos.preferred-regions=a,b,c",
                "spring.cloud.azure.cosmos.throttling-retry-options.max-retry-attempts-on-throttled-requests=1",
                "spring.cloud.azure.cosmos.throttling-retry-options.max-retry-wait-time=2s",
                "spring.cloud.azure.cosmos.consistency-level=eventual",
                "spring.cloud.azure.cosmos.connection-mode=gateway",
                "spring.cloud.azure.cosmos.gateway-connection.max-connection-pool-size=3",
                "spring.cloud.azure.cosmos.gateway-connection.idle-connection-timeout=4s",
                "spring.cloud.azure.cosmos.direct-connection.connection-endpoint-rediscovery-enabled=true",
                "spring.cloud.azure.cosmos.direct-connection.connect-timeout=5s",
                "spring.cloud.azure.cosmos.direct-connection.idle-connection-timeout=6s",
                "spring.cloud.azure.cosmos.direct-connection.idle-endpoint-timeout=7s",
                "spring.cloud.azure.cosmos.direct-connection.network-request-timeout=8s",
                "spring.cloud.azure.cosmos.direct-connection.max-connections-per-endpoint=9",
                "spring.cloud.azure.cosmos.direct-connection.max-requests-per-connection=10",
                "spring.cloud.azure.cosmos.populate-query-metrics=true"

            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                AzureCosmosProperties properties = context.getBean(AzureCosmosProperties.class);
                assertEquals("cosmos-client-id", properties.getCredential().getClientId());
                assertEquals("127.0.0.1", properties.getProxy().getNonProxyHosts());
                assertEquals("test-endpoint", properties.getEndpoint());
                assertEquals("cosmos-key", properties.getKey());
                assertEquals("test-database", properties.getDatabase());
                assertEquals("test-resource-token", properties.getResourceToken());
                assertTrue(properties.getClientTelemetryEnabled());
                assertTrue(properties.getEndpointDiscoveryEnabled());
                assertTrue(properties.getConnectionSharingAcrossClientsEnabled());
                assertTrue(properties.getContentResponseOnWriteEnabled());
                assertTrue(properties.getMultipleWriteRegionsEnabled());
                assertTrue(properties.getSessionCapturingOverrideEnabled());
                assertTrue(properties.getReadRequestsFallbackEnabled());
                assertEquals(Arrays.asList("a", "b", "c"), properties.getPreferredRegions());
                assertEquals(1, properties.getThrottlingRetryOptions().getMaxRetryAttemptsOnThrottledRequests());
                assertEquals(Duration.ofSeconds(2), properties.getThrottlingRetryOptions().getMaxRetryWaitTime());
                assertEquals(ConsistencyLevel.EVENTUAL, properties.getConsistencyLevel());
                assertEquals(ConnectionMode.GATEWAY, properties.getConnectionMode());
                assertEquals(3, properties.getGatewayConnection().getMaxConnectionPoolSize());
                assertEquals(Duration.ofSeconds(4), properties.getGatewayConnection().getIdleConnectionTimeout());
                assertTrue(properties.getDirectConnection().getConnectionEndpointRediscoveryEnabled());
                assertEquals(Duration.ofSeconds(5), properties.getDirectConnection().getConnectTimeout());
                assertEquals(Duration.ofSeconds(6), properties.getDirectConnection().getIdleConnectionTimeout());
                assertEquals(Duration.ofSeconds(7), properties.getDirectConnection().getIdleEndpointTimeout());
                assertEquals(Duration.ofSeconds(8), properties.getDirectConnection().getNetworkRequestTimeout());
                assertEquals(9, properties.getDirectConnection().getMaxConnectionsPerEndpoint());
                assertEquals(10, properties.getDirectConnection().getMaxRequestsPerConnection());
                assertTrue(properties.isPopulateQueryMetrics());
            });
    }

    private static class CosmosBuilderCustomizer extends TestBuilderCustomizer<CosmosClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<EventHubClientBuilder> {

    }

}
