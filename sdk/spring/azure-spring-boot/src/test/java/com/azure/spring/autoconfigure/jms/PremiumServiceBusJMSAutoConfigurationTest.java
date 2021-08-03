// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PremiumServiceBusJMSAutoConfigurationTest {

    private static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    @Test
    public void testAzureServiceBusPremiumAutoConfiguration() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.withPropertyValues("spring.jms.servicebus.pricing-tier=basic")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));

        contextRunner.withPropertyValues("spring.jms.servicebus.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));

        contextRunner.withPropertyValues("spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
                     .run(context -> assertThat(context).hasSingleBean(AzureServiceBusJMSProperties.class));
    }

    @Test
    public void testAzureServiceBusJMSPropertiesConnectionStringValidation() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.run(
            context -> Assertions.assertThrows(IllegalStateException.class,
                () -> context.getBean(AzureServiceBusJMSProperties.class)));
    }

    @Test
    public void testWithoutServiceBusJMSNamespace() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusJmsConnectionFactory.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));
    }

    @Test
    public void testCachingConnectionFactoryIsAutowired() {

        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(JmsTemplate.class);
                ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                assertTrue(connectionFactory == context.getBean(JmsTemplate.class).getConnectionFactory());
            }
        );
    }

    @Test
    public void testAzureServiceBusJMSPropertiesConfigured() {

        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJMSProperties.class);
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getConnectionString()).isEqualTo(
                    CONNECTION_STRING);
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getTopicClientId()).isEqualTo("cid");
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getIdleTimeout()).isEqualTo(123);
            }
        );
    }

    private ApplicationContextRunner getEmptyContextRunner() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PremiumServiceBusJMSAutoConfiguration.class, JmsAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=premium"
            );
    }

    private ApplicationContextRunner getContextRunnerWithProperties() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PremiumServiceBusJMSAutoConfiguration.class, JmsAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.topic-client-id=cid",
                "spring.jms.servicebus.idle-timeout=123",
                "spring.jms.servicebus.pricing-tier=premium"
            );
    }
}
