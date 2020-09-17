// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureServiceBusTopicAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusTopicAutoConfiguration.class));

    @Test
    public void testAzureServiceBusTopicDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusTopicClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(TopicClient.class))
            .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicOperation.class));
    }
}
