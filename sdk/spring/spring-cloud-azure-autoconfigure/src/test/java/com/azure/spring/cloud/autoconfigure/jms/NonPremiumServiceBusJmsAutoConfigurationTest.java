// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NonPremiumServiceBusJmsAutoConfigurationTest extends AbstractServiceBusJmsAutoConfigurationTest {

    NonPremiumServiceBusJmsAutoConfigurationTest() {
        this.contextRunner = super.contextRunner
            .withPropertyValues("spring.jms.servicebus.pricing-tier=basic")
            .withConfiguration(AutoConfigurations.of(NonPremiumServiceBusJmsAutoConfiguration.class));
    }

    @Test
    void testAzureServiceBusNonPremiumAutoConfiguration() {
        this.contextRunner.withPropertyValues("spring.jms.servicebus.pricing-tier=premium")
                     .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class));

        this.contextRunner.withPropertyValues("spring.jms.servicebus.enabled=false")
                     .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class));

        this.contextRunner.withPropertyValues("spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
                          .run(context -> assertThat(context).hasSingleBean(AzureServiceBusJmsProperties.class));
    }

    @Test
    void testWithoutServiceBusJMSNamespace() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(JmsConnectionFactory.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class));
    }

    @Test
    void testAzureServiceBusJMSPropertiesPricingTireValidation() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=fake",
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> assertThrows(IllegalStateException.class,
                                         () -> context.getBean(AzureServiceBusJmsProperties.class)));
    }

    @Test
    void connectionFactoryPropertiesConfigured() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.pricing-tier=basic")
            .run(context -> {
                assertThat(context).hasSingleBean(JmsConnectionFactory.class);
                JmsConnectionFactory factory = context.getBean(JmsConnectionFactory.class);
                AzureServiceBusJmsProperties properties = context.getBean(AzureServiceBusJmsProperties.class);
                assertEquals(factory.getRemoteURI(), "amqps://host?amqp.idleTimeout=1800000");
                assertNull(factory.getClientID());
                assertEquals(factory.getUsername(), "sasKeyName");
                assertEquals(factory.getPassword(), "sasKey");

                AzureServiceBusJmsProperties.PrefetchPolicy propertiesPrefetchPolicy = properties.getPrefetchPolicy();
                JmsDefaultPrefetchPolicy prefetchPolicy = (JmsDefaultPrefetchPolicy) factory.getPrefetchPolicy();
                assertThat(prefetchPolicy).hasFieldOrPropertyWithValue("queuePrefetch",
                    propertiesPrefetchPolicy.getQueuePrefetch());
                assertThat(prefetchPolicy).hasFieldOrPropertyWithValue("topicPrefetch",
                    propertiesPrefetchPolicy.getTopicPrefetch());
                assertThat(prefetchPolicy).hasFieldOrPropertyWithValue("queueBrowserPrefetch",
                    propertiesPrefetchPolicy.getQueueBrowserPrefetch());
                assertThat(prefetchPolicy).hasFieldOrPropertyWithValue("durableTopicPrefetch",
                    propertiesPrefetchPolicy.getDurableTopicPrefetch());
            });
    }
}
