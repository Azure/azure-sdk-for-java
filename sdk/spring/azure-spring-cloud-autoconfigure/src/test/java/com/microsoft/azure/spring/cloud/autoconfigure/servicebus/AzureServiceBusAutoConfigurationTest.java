// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureServiceBusAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testWithoutServiceBusNamespace() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(IMessage.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureServiceBusPropertiesValidation() {
        this.contextRunner.run(context -> context.getBean(AzureServiceBusProperties.class));
    }

    @Test
    public void testAzureServiceBusPropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.servicebus.namespace=ns1").run(context -> {
            assertThat(context).hasSingleBean(AzureServiceBusProperties.class);
            assertThat(context.getBean(AzureServiceBusProperties.class).getNamespace()).isEqualTo("ns1");
        });
    }
}
