// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureServiceBusQueueAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusQueueAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusQueueClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(QueueClient.class))
            .run(context -> assertThat(context).doesNotHaveBean(ServiceBusQueueOperation.class));
    }
}
