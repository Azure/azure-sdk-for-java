// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.context.AzurePropertyAutoConfiguration;
import com.azure.spring.core.properties.AzureProperties;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.context.AzurePropertyAutoConfiguration.AZURE_PROPERTY_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

public class PremiumServiceBusJMSAutoConfigurationTest extends AbstractServiceBusJMSAutoConfigurationTest {

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
    public void testWithoutServiceBusJMSNamespace() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusJmsConnectionFactory.class))
                     .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));
    }

    @Override
    protected ApplicationContextRunner getEmptyContextRunner() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PremiumServiceBusJMSAutoConfiguration.class,
                JmsAutoConfiguration.class, AzurePropertyAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=premium"
            );
    }

    // TODO (xiada): do we need this test here?
    /*@Test
    public void testAzurePropertiesConfigured() {
        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasBean(AZURE_PROPERTY_BEAN_NAME);

                assertThat(((AzureProperties) context.getBean(AZURE_PROPERTY_BEAN_NAME)).getCredential().getClientCertificatePassword())
                    .isEqualTo("for-test-purpose");
                assertThat(((AzureProperties) context.getBean(AZURE_PROPERTY_BEAN_NAME)).getEnvironment().getAuthorityHost())
                    .isEqualTo("for-test-purpose");
            }
        );
    }*/

    @Override
    protected ApplicationContextRunner getContextRunnerWithProperties() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PremiumServiceBusJMSAutoConfiguration.class,
                JmsAutoConfiguration.class, AzurePropertyAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.listener.autoStartup=false",
                "spring.jms.listener.acknowledgeMode=client",
                "spring.jms.listener.concurrency=2",
                "spring.jms.listener.receiveTimeout=2s",
                "spring.jms.listener.maxConcurrency=10",
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.topic-client-id=cid",
                "spring.jms.servicebus.idle-timeout=123",
                "spring.jms.servicebus.pricing-tier=premium",
                "spring.jms.servicebus.listener.reply-pub-sub-domain=false",
                "spring.jms.servicebus.listener.reply-qos-settings.priority=1",
                "spring.jms.servicebus.credential.client-secret=for-test-purpose",
                "spring.jms.servicebus.environment.cloud=AzureGermany",
                "spring.cloud.azure.credential.client-certificate-password=for-test-purpose",
                "spring.cloud.azure.environment.authority-host=for-test-purpose"
            );
    }
}
