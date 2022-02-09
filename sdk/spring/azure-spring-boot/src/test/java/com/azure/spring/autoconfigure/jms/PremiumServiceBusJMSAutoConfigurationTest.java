// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PremiumServiceBusJMSAutoConfigurationTest extends AbstractServiceBusJMSAutoConfigurationTest {

    private static final Logger LOGGER = Logger.getLogger(PremiumServiceBusJMSAutoConfigurationTest.class.getName());

    @BeforeAll
    public static void init() {
        LOGGER.info("Starting PremiumServiceBusJMSAutoConfigurationTest");
    }

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

    @Test
    public void testPreminumPrefetchPolicy() {
        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();
        contextRunner.run(
            context -> {
                assertThat(context).hasBean("jmsConnectionFactory");
                SpringServiceBusJmsConnectionFactory jmsConnectionFactory = (SpringServiceBusJmsConnectionFactory) context.getBean("jmsConnectionFactory");
                assertNotNull(jmsConnectionFactory.getSettings().getConfigurationOptions());
                Map<String, String> configurationOptions = jmsConnectionFactory.getSettings().getConfigurationOptions();
                assertThat(configurationOptions.get("jms.prefetchPolicy.all")).isEqualTo("5");
                assertThat(configurationOptions.get("jms.prefetchPolicy.topicPrefetch")).isEqualTo("12");
                assertThat(configurationOptions.get("jms.prefetchPolicy.durableTopicPrefetch")).isEqualTo("999");
                assertThat(configurationOptions.get("jms.prefetchPolicy.queuePrefetch")).isEqualTo("19");
                assertThat(configurationOptions.get("jms.prefetchPolicy.queueBrowserPrefetch")).isEqualTo("21");
            }
        );
    }

    @Override
    protected ApplicationContextRunner getEmptyContextRunner() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PremiumServiceBusJMSAutoConfiguration.class, JmsAutoConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=premium"
            );
    }

    @Override
    protected ApplicationContextRunner getContextRunnerWithProperties() {

        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PremiumServiceBusJMSAutoConfiguration.class, JmsAutoConfiguration.class))
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
                "spring.jms.servicebus.prefetch-policy.all=5",
                "spring.jms.servicebus.prefetch-policy.topic-prefetch=12",
                "spring.jms.servicebus.prefetch-policy.durable-topic-prefetch=999",
                "spring.jms.servicebus.prefetch-policy.queue-prefetch= 19",
                "spring.jms.servicebus.prefetch-policy.queue-browser-prefetch= 21"
            );
    }
}
