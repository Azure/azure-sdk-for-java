// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class AzureServiceBusQueueOperationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusQueueOperationAutoConfiguration.class));

    @Test
    void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    void testWithoutAzureServiceBusQueueClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusQueueClientFactory.class))
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    void testQueueClientFactoryCreated() {
        this.contextRunner.withBean(ServiceBusClientBuilder.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusQueueClientFactory.class)
                                                             .hasSingleBean(ServiceBusQueueOperation.class));
    }

    @Test
    void testMessageConverterProvided() {
        this.contextRunner
            .withBean(ServiceBusClientBuilder.class)
            .withBean(ServiceBusMessageConverter.class, () -> mock(ServiceBusMessageConverter.class))
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                assertThat(context).hasSingleBean(ServiceBusQueueTemplate.class);

                ServiceBusMessageConverter messageConverter = context.getBean(ServiceBusMessageConverter.class);
                ServiceBusQueueTemplate queueTemplate = context.getBean(ServiceBusQueueTemplate.class);
                assertSame(messageConverter, queueTemplate.getMessageConverter());
        });
    }


}
