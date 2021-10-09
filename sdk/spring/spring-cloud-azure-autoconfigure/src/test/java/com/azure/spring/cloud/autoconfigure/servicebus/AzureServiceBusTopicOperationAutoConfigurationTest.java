// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.servicebus.core.ServiceBusTopicClientFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicTemplate;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class AzureServiceBusTopicOperationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusTopicOperationAutoConfiguration.class));


    @Test
    void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicClientFactory.class));
    }

    @Test
    void testWithoutAzureServiceBusTopicClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusTopicClientFactory.class))
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicClientFactory.class));
    }

    @Test
    void testTopicClientFactoryCreated() {
        this.contextRunner.withBean(ServiceBusClientBuilder.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class)
                                                             .hasSingleBean(ServiceBusTopicTemplate.class));
    }

    @Test
    void testMessageConverterProvided() {
        this.contextRunner
            .withBean(ServiceBusClientBuilder.class)
            .withBean(ServiceBusMessageConverter.class, () -> mock(ServiceBusMessageConverter.class))
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                assertThat(context).hasSingleBean(ServiceBusTopicTemplate.class);

                ServiceBusMessageConverter messageConverter = context.getBean(ServiceBusMessageConverter.class);
                ServiceBusTopicTemplate topicTemplate = context.getBean(ServiceBusTopicTemplate.class);
                assertSame(messageConverter, topicTemplate.getMessageConverter());
            });
    }

}
