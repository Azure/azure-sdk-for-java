// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.spring.cloud.stream.binder.servicebus.ServiceBusMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.provisioning.ServiceBusChannelResourceManagerProvisioner;
import com.azure.spring.resourcemanager.provisioning.servicebus.ServiceBusProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.Binder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ServiceBusBinderConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ServiceBusBinderConfiguration.class));

    @Test
    void configurationNotMatchedWhenBinderBeanExist() {
        this.contextRunner
            .withBean(Binder.class, () -> mock(Binder.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ServiceBusBinderConfiguration.class);
                assertThat(context).doesNotHaveBean(ServiceBusMessageChannelBinder.class);
            });
    }

    @Test
    void shouldConfigureDefaultChannelProvisionerWhenNoResourceManagerProvided() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusBinderConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(ServiceBusChannelProvisioner.class);
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);

                ServiceBusChannelProvisioner channelProvisioner = context.getBean(ServiceBusChannelProvisioner.class);
                assertThat(channelProvisioner).isNotInstanceOf(ServiceBusChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void shouldConfigureArmChannelProvisionerWhenResourceManagerProvided() {
        this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=test")
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusBinderConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(ServiceBusChannelProvisioner.class);
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);

                ServiceBusChannelProvisioner channelProvisioner = context.getBean(ServiceBusChannelProvisioner.class);
                assertThat(channelProvisioner).isInstanceOf(ServiceBusChannelResourceManagerProvisioner.class);
            });
    }
}
