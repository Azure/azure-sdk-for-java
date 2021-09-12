// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
class AzureServiceBusAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusAutoConfiguration.class));

    @Test
    void configureWithoutCosmosClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusClientBuilder.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    void configureWithCosmosDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    void configureAzureServiceBusProperties() {
        AzureConfigurationProperties azureProperties = new AzureConfigurationProperties();
        azureProperties.getCredential().setClientId("azure-client-id");
        azureProperties.getCredential().setClientSecret("azure-client-secret");
        azureProperties.getRetry().getBackoff().setDelay(Duration.ofSeconds(2));

        this.contextRunner
            .withBean("azureProperties", AzureConfigurationProperties.class, () -> azureProperties)
            .withPropertyValues("spring.cloud.azure.servicebus.credential.client-id=servicebus-client-id",
                                "spring.cloud.azure.servicebus.retry.backoff.delay=2m")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                final AzureServiceBusProperties properties = context.getBean(AzureServiceBusProperties.class);
                assertThat(properties).extracting("credential.clientId").isEqualTo("servicebus-client-id");
                assertThat(properties).extracting("credential.clientSecret").isEqualTo("azure-client-secret");
                assertThat(properties).extracting("retry.backoff.delay").isEqualTo(Duration.ofMinutes(2));
            });
    }

    @Test
    void configureAmqpTransportTypeShouldApply() {
        this.contextRunner
            .withBean("azureProperties", AzureConfigurationProperties.class, AzureConfigurationProperties::new)
            .withPropertyValues("spring.cloud.azure.servicebus.client.transport-type=AmqpWebSockets")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                final AzureServiceBusProperties properties = context.getBean(AzureServiceBusProperties.class);
                assertThat(properties).extracting("client.transportType").isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS);
            });
    }

    @Test
    void configureRetryShouldApply() {
        this.contextRunner
            .withBean("azureProperties", AzureConfigurationProperties.class, AzureConfigurationProperties::new)
            .withPropertyValues("spring.cloud.azure.servicebus.retry.max-attempts=5",
                                "spring.cloud.azure.servicebus.retry.timeout=30s",
                                "spring.cloud.azure.servicebus.retry.backoff.delay=10s",
                                "spring.cloud.azure.servicebus.retry.backoff.max-delay=20s")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
                final AzureServiceBusProperties properties = context.getBean(AzureServiceBusProperties.class);
                assertThat(properties).extracting("retry.maxAttempts").isEqualTo(5);
                assertThat(properties).extracting("retry.timeout").isEqualTo(Duration.ofSeconds(30));
                assertThat(properties).extracting("retry.backoff.delay").isEqualTo(Duration.ofSeconds(10));
                assertThat(properties).extracting("retry.backoff.maxDelay").isEqualTo(Duration.ofSeconds(20));
            });
    }



}
