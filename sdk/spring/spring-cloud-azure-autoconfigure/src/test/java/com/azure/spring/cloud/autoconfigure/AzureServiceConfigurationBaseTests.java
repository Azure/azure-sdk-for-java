// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretAutoConfiguration;
import com.azure.spring.cloud.core.properties.client.HeaderProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.assertj.core.extractor.Extractors;
import org.assertj.core.util.introspection.IntrospectionError;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AzureServiceConfigurationBaseTests {

    static final String TEST_ENDPOINT_HTTPS = "https://test.https.documents.azure.com:443/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureCosmosAutoConfiguration.class,
            AzureKeyVaultSecretAutoConfiguration.class,
            AzureEventHubsAutoConfiguration.class));

    @Test
    void configureGlobalShouldApplyToAzureCosmosProperties() {
        HeaderProperties headerProperties = new HeaderProperties();
        headerProperties.setName("global-header");
        headerProperties.setValues(Arrays.asList("a", "b", "c"));
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("global-client-id");
        azureProperties.getCredential().setClientSecret("global-client-secret");
        azureProperties.getClient().setApplicationId("global-application-id");
        azureProperties.getClient().getAmqp().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        azureProperties.getClient().getHttp().setConnectTimeout(Duration.ofMinutes(1));
        azureProperties.getClient().getHttp().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        azureProperties.getClient().getHttp().getLogging().getAllowedHeaderNames().add("abc");
        azureProperties.getClient().getHttp().getHeaders().add(headerProperties);
        azureProperties.getProxy().setHostname("localhost");
        azureProperties.getProxy().getHttp().setNonProxyHosts("localhost");
        azureProperties.getProxy().getAmqp().setAuthenticationType("basic");
        azureProperties.getRetry().getExponential().setMaxRetries(2);
        azureProperties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        azureProperties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));
        azureProperties.getRetry().getFixed().setMaxRetries(5);
        azureProperties.getRetry().getFixed().setDelay(Duration.ofSeconds(6));
        azureProperties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        azureProperties.getRetry().getAmqp().setTryTimeout(Duration.ofSeconds(7));
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

                assertThat(properties).extracting("profile.cloudType").isEqualTo(AZURE);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("abc");

                assertThat(properties).extracting("endpoint").isEqualTo(TEST_ENDPOINT_HTTPS);
                assertThat(properties).extracting("key").isEqualTo("cosmos-key");

                assertThatThrownBy(() -> Extractors.byName("client.transportType").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.headers").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.allowedHeaderNames").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.level").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.connectTimeout").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.exponential.maxRetries").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.exponential.baseDelay").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.exponential.maxDelay").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.fixed.maxRetries").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.fixed.delay").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.mode").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.tryTimeout").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("proxy.authenticationType").apply(properties))
                    .isInstanceOf(IntrospectionError.class);

            });
    }

    @Test
    void configureEnvGlobalAndCosmosShouldApplyCosmos() {
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getProfile().setCloudType(AZURE_CHINA);
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");
        azureProperties.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion("v2");

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
            .withPropertyValues(
                "spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT_HTTPS,
                "spring.cloud.azure.cosmos.key=cosmos-key",
                "spring.cloud.azure.cosmos.profile.cloud-type=other",
                "spring.cloud.azure.cosmos.profile.environment.activeDirectoryEndpoint=bcd"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCosmosProperties.class);
                final AzureCosmosProperties properties = context.getBean(AzureCosmosProperties.class);

                assertThat(properties).extracting("profile.cloudType").isEqualTo(OTHER);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("bcd");
                assertThat(properties).extracting("profile.environment.activeDirectoryGraphApiVersion").isEqualTo("v2");

                assertThat(properties).extracting("endpoint").isEqualTo(TEST_ENDPOINT_HTTPS);
                assertThat(properties).extracting("key").isEqualTo("cosmos-key");
            });
    }

    @Test
    void configureGlobalShouldApplyToAmqpAzureEventHubsProperties() {
        HeaderProperties headerProperties = new HeaderProperties();
        headerProperties.setName("global-header");
        headerProperties.setValues(Arrays.asList("a", "b", "c"));
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("global-client-id");
        azureProperties.getCredential().setClientSecret("global-client-secret");
        azureProperties.getClient().setApplicationId("global-application-id");
        azureProperties.getClient().getAmqp().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        azureProperties.getClient().getHttp().getHeaders().add(headerProperties);
        azureProperties.getClient().getHttp().setConnectTimeout(Duration.ofMinutes(1));
        azureProperties.getClient().getHttp().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        azureProperties.getClient().getHttp().getLogging().getAllowedHeaderNames().add("abc");
        azureProperties.getProxy().setHostname("localhost");
        azureProperties.getProxy().getHttp().setNonProxyHosts("localhost");
        azureProperties.getProxy().getAmqp().setAuthenticationType("basic");
        azureProperties.getRetry().getExponential().setMaxRetries(2);
        azureProperties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        azureProperties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));
        azureProperties.getRetry().getFixed().setMaxRetries(5);
        azureProperties.getRetry().getFixed().setDelay(Duration.ofSeconds(6));
        azureProperties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        azureProperties.getRetry().getAmqp().setTryTimeout(Duration.ofSeconds(7));
        azureProperties.getProfile().getEnvironment().setActiveDirectoryEndpoint("abc");

        this.contextRunner
            .withBean(AzureGlobalProperties.class, () -> azureProperties)
            .withPropertyValues(
                "spring.cloud.azure.eventhubs.namespace=test-namespace"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsProperties.class);
                final AzureEventHubsProperties properties = context.getBean(AzureEventHubsProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("global-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("global-client-secret");

                assertThat(properties).extracting("client.applicationId").isEqualTo("global-application-id");
                assertThat(properties).extracting("client.transportType").isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS);

                assertThat(properties).extracting("proxy.hostname").isEqualTo("localhost");
                assertThat(properties).extracting("proxy.authenticationType").isEqualTo("basic");

                assertThat(properties).extracting("retry.exponential.maxRetries").isEqualTo(2);
                assertThat(properties).extracting("retry.exponential.baseDelay").isEqualTo(Duration.ofSeconds(3));
                assertThat(properties).extracting("retry.exponential.maxDelay").isEqualTo(Duration.ofSeconds(4));
                assertThat(properties).extracting("retry.fixed.maxRetries").isEqualTo(5);
                assertThat(properties).extracting("retry.fixed.delay").isEqualTo(Duration.ofSeconds(6));
                assertThat(properties).extracting("retry.tryTimeout").isEqualTo(Duration.ofSeconds(7));
                assertThat(properties).extracting("retry.mode").isEqualTo(RetryOptionsProvider.RetryMode.FIXED);

                assertThat(properties).extracting("namespace").isEqualTo("test-namespace");

                assertThat(properties).extracting("profile.cloudType").isEqualTo(AZURE);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("abc");

                assertThatThrownBy(() -> Extractors.byName("client.connectTimeout").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.headers").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.level").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("client.logging.allowedHeaderNames").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("proxy.nonProxyHosts").apply(properties))
                    .isInstanceOf(IntrospectionError.class);

            });
    }

    @Test
    void configureGlobalShouldApplyToHttpAzureKeyVaultSecretProperties() {
        HeaderProperties headerProperties = new HeaderProperties();
        headerProperties.setName("global-header");
        headerProperties.setValues(Arrays.asList("a", "b", "c"));
        AzureGlobalProperties azureProperties = new AzureGlobalProperties();
        azureProperties.getCredential().setClientId("global-client-id");
        azureProperties.getCredential().setClientSecret("global-client-secret");
        azureProperties.getClient().setApplicationId("global-application-id");
        azureProperties.getClient().getAmqp().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        azureProperties.getClient().getHttp().getHeaders().add(headerProperties);
        azureProperties.getClient().getHttp().setConnectTimeout(Duration.ofMinutes(1));
        azureProperties.getClient().getHttp().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        azureProperties.getClient().getHttp().getLogging().getAllowedHeaderNames().add("abc");
        azureProperties.getProxy().setHostname("localhost");
        azureProperties.getProxy().getHttp().setNonProxyHosts("localhost");
        azureProperties.getProxy().getAmqp().setAuthenticationType("basic");
        azureProperties.getRetry().getExponential().setMaxRetries(2);
        azureProperties.getRetry().getExponential().setBaseDelay(Duration.ofSeconds(3));
        azureProperties.getRetry().getExponential().setMaxDelay(Duration.ofSeconds(4));
        azureProperties.getRetry().getFixed().setMaxRetries(5);
        azureProperties.getRetry().getFixed().setDelay(Duration.ofSeconds(6));
        azureProperties.getRetry().setMode(RetryOptionsProvider.RetryMode.FIXED);
        azureProperties.getRetry().getAmqp().setTryTimeout(Duration.ofSeconds(7));
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
                assertThat(properties).extracting("client.headers").isEqualTo(Arrays.asList(headerProperties));

                assertThat(properties).extracting("proxy.hostname").isEqualTo("localhost");
                assertThat(properties).extracting("proxy.nonProxyHosts").isEqualTo("localhost");

                assertThat(properties).extracting("retry.exponential.maxRetries").isEqualTo(2);
                assertThat(properties).extracting("retry.exponential.baseDelay").isEqualTo(Duration.ofSeconds(3));
                assertThat(properties).extracting("retry.exponential.maxDelay").isEqualTo(Duration.ofSeconds(4));
                assertThat(properties).extracting("retry.fixed.maxRetries").isEqualTo(5);
                assertThat(properties).extracting("retry.fixed.delay").isEqualTo(Duration.ofSeconds(6));
                assertThat(properties).extracting("retry.mode").isEqualTo(RetryOptionsProvider.RetryMode.FIXED);

                assertThat(properties).extracting("profile.cloudType").isEqualTo(AZURE);
                assertThat(properties).extracting("profile.environment.activeDirectoryEndpoint").isEqualTo("abc");

                assertThat(properties).extracting("endpoint").isEqualTo("test");

                assertThatThrownBy(() -> Extractors.byName("client.transportType").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("retry.tryTimeout").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
                assertThatThrownBy(() -> Extractors.byName("proxy.authenticationType").apply(properties))
                    .isInstanceOf(IntrospectionError.class);
            });
    }


}
