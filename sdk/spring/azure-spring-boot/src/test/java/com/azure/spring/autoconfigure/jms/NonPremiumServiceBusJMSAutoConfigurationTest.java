// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class NonPremiumServiceBusJMSAutoConfigurationTest extends AbstractServiceBusJMSAutoConfigurationTest {

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
    public void testWithoutServiceBusJMSNamespace() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.withClassLoader(new FilteredClassLoader(JmsConnectionFactory.class))
                     .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));
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

    @Override
    protected ApplicationContextRunner getEmptyContextRunner() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NonPremiumServiceBusJMSAutoConfiguration.class,
                JmsAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=basic"
            );
    }

    @Override
    protected ApplicationContextRunner getContextRunnerWithProperties() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NonPremiumServiceBusJMSAutoConfiguration.class,
                JmsAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.listener.autoStartup=false",
                "spring.jms.listener.acknowledgeMode=client",
                "spring.jms.listener.concurrency=2",
                "spring.jms.listener.receiveTimeout=2s",
                "spring.jms.listener.maxConcurrency=10",
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.topic-client-id=cid",
                "spring.jms.servicebus.idle-timeout=123",
                "spring.jms.servicebus.pricing-tier=basic",
                "spring.jms.servicebus.listener.reply-pub-sub-domain=false",
                "spring.jms.servicebus.listener.reply-qos-settings.priority=1"
            );
    }
}
