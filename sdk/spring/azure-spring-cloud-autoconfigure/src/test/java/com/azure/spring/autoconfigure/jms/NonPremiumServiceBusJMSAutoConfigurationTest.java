// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.context.AzurePropertyAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.properties.AzureConfigurationProperties;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

import static com.azure.spring.cloud.autoconfigure.context.AzurePropertyAutoConfiguration.AZURE_PROPERTY_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NonPremiumServiceBusJMSAutoConfigurationTest {

    private static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    @Test
    public void testAzureServiceBusNonPremiumAutoConfiguration() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.withPropertyValues("spring.jms.servicebus.pricing-tier=premium")
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
    public void testAzureServiceBusJMSPropertiesPricingTireValidation() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.withPropertyValues(
            "spring.jms.servicebus.pricing-tier=fake",
            "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
                     .run(context -> Assertions.assertThrows(IllegalStateException.class,
                         () -> context.getBean(AzureServiceBusJMSProperties.class)));
    }

    @Test
    public void testWithoutServiceBusJMSNamespace() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.withClassLoader(new FilteredClassLoader(JmsConnectionFactory.class))
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

    @Test
    public void testAzurePropertiesConfigured() {
        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasBean(AZURE_PROPERTY_BEAN_NAME);

                assertThat(((AzureConfigurationProperties) context.getBean(AZURE_PROPERTY_BEAN_NAME)).getCredential().getClientCertificatePassword())
                    .isEqualTo("for-test-purpose");
            }
        );
    }

    private ApplicationContextRunner getEmptyContextRunner() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NonPremiumServiceBusJMSAutoConfiguration.class,
                JmsAutoConfiguration.class, AzurePropertyAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=basic"
            );
    }

    private ApplicationContextRunner getContextRunnerWithProperties() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NonPremiumServiceBusJMSAutoConfiguration.class,
                JmsAutoConfiguration.class, AzurePropertyAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.topic-client-id=cid",
                "spring.jms.servicebus.idle-timeout=123",
                "spring.jms.servicebus.pricing-tier=basic",
                "spring.jms.servicebus.credential.client-secret=for-test-purpose",
                "spring.jms.servicebus.environment.cloud=AzureGermany",
                "spring.cloud.azure.credential.client-certificate-password=for-test-purpose",
                "spring.cloud.azure.environment.authority-host=for-test-purpose"
            );
    }
}
