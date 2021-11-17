// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.assertj.core.extractor.Extractors;
import org.assertj.core.util.introspection.IntrospectionError;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE_CHINA;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AzureServiceConfigurationBaseTest {

    static final String TEST_ENDPOINT_HTTPS = "https://test.https.documents.azure.com:443/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureCosmosAutoConfiguration.class,
            AzureKeyVaultSecretAutoConfiguration.class,
            AzureEventHubsAutoConfiguration.class));

    @Test
    void configureGlobalShouldApplyToAzureCosmosProperties() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("global-client-id");
        azureProperties.getCredential().setClientSecret("global-client-secret");
        azureProperties.getClient().setApplicationId("global-application-id");
        azureProperties.getClient().getAmqp().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        azureProperties.getClient().getHttp().setConnectTimeout(Duration.ofMinutes(1));
        azureProperties.getClient().getHttp().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        azureProperties.getClient().getHttp().getLogging().getAllowedHeaderNames().add("abc");
        azureProperties.getProxy().setHostname("localhost");
        azureProperties.getProxy().getHttp().setNonProxyHosts("localhost");
        azureProperties.getRetry().getBackoff().setDelay(Duration.ofMillis(2));
        azureProperties.getRetry().setMaxAttempts(3);
        azureProperties.getRetry().getHttp().setRetryAfterHeader("x-ms-xxx");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS,
                "spring.cloud.azure.cosmos.key=cosmos-key"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                final AzureCosmosProperties properties = context.getBean(AzureCosmosProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("global-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("global-client-secret");

                assertThat(properties).extracting("client.applicationId").isEqualTo("global-application-id");

                assertThat(properties).extracting("proxy.hostname").isEqualTo("localhost");
                assertThat(properties).extracting("proxy.nonProxyHosts").isEqualTo("localhost");

                // retry configuration doesn't apply to cosmos client
                assertThat(properties).extracting("retry.maxAttempts").isEqualTo(null);
                assertThat(properties).extracting("retry.backoff.delay").isEqualTo(null);

                assertThat(properties).extracting("profile.cloud").isEqualTo(AZURE);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("abc");

                assertThat(properties).extracting("endpoint").isEqualTo(TEST_ENDPOINT_HTTPS);
                assertThat(properties).extracting("key").isEqualTo("cosmos-key");

                assertThatThrownBy(() -> Extractors.byName("client.transportType").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.allowedHeaderNames").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.level").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.connectTimeout").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.retryAfterHeader").apply(properties))
                    .isInstanceOf(IntrospectionError.class);

            });
    }

    @Test
    void configureEnvGlobalAndCosmosShouldApplyCosmos() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getProfile().setCloud(AZURE_CHINA);
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion("v2");

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS,
                "spring.cloud.azure.cosmos.key=cosmos-key",
                "spring.cloud.azure.cosmos.profile.cloud=other",
                "spring.cloud.azure.cosmos.profile.environment.activeDirectoryEndpoint=bcd"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                final AzureCosmosProperties properties = context.getBean(AzureCosmosProperties.class);

                assertThat(properties).extracting("profile.cloud").isEqualTo(OTHER);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("bcd");
                assertThat(properties).extracting("profile.environment.activeDirectoryGraphApiVersion").isEqualTo("v2");

                assertThat(properties).extracting("endpoint").isEqualTo(TEST_ENDPOINT_HTTPS);
                assertThat(properties).extracting("key").isEqualTo("cosmos-key");
            });
    }

    @Test
    void configureGlobalShouldApplyToAmqpAzureEventHubsProperties() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("global-client-id");
        azureProperties.getCredential().setClientSecret("global-client-secret");
        azureProperties.getClient().setApplicationId("global-application-id");
        azureProperties.getClient().getAmqp().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        azureProperties.getClient().getHttp().setConnectTimeout(Duration.ofMinutes(1));
        azureProperties.getClient().getHttp().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        azureProperties.getClient().getHttp().getLogging().getAllowedHeaderNames().add("abc");
        azureProperties.getProxy().setHostname("localhost");
        azureProperties.getProxy().getHttp().setNonProxyHosts("localhost");
        azureProperties.getRetry().getBackoff().setDelay(Duration.ofMillis(2));
        azureProperties.getRetry().setMaxAttempts(3);
        azureProperties.getRetry().getHttp().setRetryAfterHeader("x-ms-xxx");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsProperties.class);
                final AzureEventHubsProperties properties = context.getBean(AzureEventHubsProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("global-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("global-client-secret");

                assertThat(properties).extracting("client.applicationId").isEqualTo("global-application-id");
                assertThat(properties).extracting("client.transportType").isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS);

                assertThat(properties).extracting("proxy.hostname").isEqualTo("localhost");

                assertThat(properties).extracting("retry.maxAttempts").isEqualTo(3);
                assertThat(properties).extracting("retry.backoff.delay").isEqualTo(Duration.ofMillis(2));

                assertThat(properties).extracting("namespace").isEqualTo("test");

                assertThat(properties).extracting("profile.cloud").isEqualTo(AZURE);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("abc");

                assertThatThrownBy(() -> Extractors.byName("client.connectTimeout").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.level").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.allowedHeaderNames").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("proxy.nonProxyHosts").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.retryAfterHeader").apply(properties))
                    .isInstanceOf(IntrospectionError.class);

            });
    }

    @Test
    void configureGlobalShouldApplyToHttpAzureKeyVaultSecretProperties() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("global-client-id");
        azureProperties.getCredential().setClientSecret("global-client-secret");
        azureProperties.getClient().setApplicationId("global-application-id");
        azureProperties.getClient().getAmqp().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        azureProperties.getClient().getHttp().setConnectTimeout(Duration.ofMinutes(1));
        azureProperties.getClient().getHttp().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        azureProperties.getClient().getHttp().getLogging().getAllowedHeaderNames().add("abc");
        azureProperties.getProxy().setHostname("localhost");
        azureProperties.getProxy().getHttp().setNonProxyHosts("localhost");
        azureProperties.getRetry().getBackoff().setDelay(Duration.ofMillis(2));
        azureProperties.getRetry().setMaxAttempts(3);
        azureProperties.getRetry().getHttp().setRetryAfterHeader("x-ms-xxx");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withBean(SecretClientBuilder.class, () -> mock(SecretClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.keyvault.secret.endpoint=test"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultSecretProperties.class);
                final AzureKeyVaultSecretProperties properties = context.getBean(AzureKeyVaultSecretProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("global-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("global-client-secret");

                assertThat(properties).extracting("client.applicationId").isEqualTo("global-application-id");
                assertThat(properties).extracting("client.connectTimeout").isEqualTo(Duration.ofMinutes(1));
                assertThat(properties).extracting("client.logging.level").isEqualTo(HttpLogDetailLevel.HEADERS);
                Set<String> allowedHeaderNames = new HashSet<>();
                allowedHeaderNames.add("abc");
                assertThat(properties).extracting("client.logging.allowedHeaderNames").isEqualTo(allowedHeaderNames);

                assertThat(properties).extracting("proxy.hostname").isEqualTo("localhost");
                assertThat(properties).extracting("proxy.nonProxyHosts").isEqualTo("localhost");

                assertThat(properties).extracting("retry.maxAttempts").isEqualTo(3);
                assertThat(properties).extracting("retry.retryAfterHeader").isEqualTo("x-ms-xxx");
                assertThat(properties).extracting("retry.backoff.delay").isEqualTo(Duration.ofMillis(2));

                assertThat(properties).extracting("profile.cloud").isEqualTo(AZURE);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("abc");

                assertThat(properties).extracting("endpoint").isEqualTo("test");

                assertThatThrownBy(() -> Extractors.byName("client.transportType").apply(properties))
                    .isInstanceOf(IntrospectionError.class);

            });
    }


}
