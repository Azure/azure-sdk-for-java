// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelResourceManagerProvisioner;
import com.azure.spring.resourcemanager.provisioner.eventhubs.EventHubsProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.Binder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EventHubsBinderConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(EventHubsBinderConfiguration.class));

    @Test
    void configurationNotMatchedWhenBinderBeanExist() {
        this.contextRunner
            .withBean(Binder.class, () -> mock(Binder.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(EventHubsBinderConfiguration.class);
                assertThat(context).doesNotHaveBean(EventHubsMessageChannelBinder.class);
            });
    }

    @Test
    void shouldConfigureDefaultChannelProvisionerWhenNoResourceManagerProvided() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsBinderConfiguration.class);
                assertThat(context).hasSingleBean(EventHubsExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(EventHubsChannelProvisioner.class);
                assertThat(context).hasSingleBean(EventHubsMessageChannelBinder.class);

                EventHubsChannelProvisioner channelProvisioner = context.getBean(EventHubsChannelProvisioner.class);
                assertThat(channelProvisioner).isNotInstanceOf(EventHubsChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void shouldConfigureArmChannelProvisionerWhenResourceManagerProvided() {
        AzureEventHubsProperties properties = new AzureEventHubsProperties();
        properties.setNamespace("test");
        this.contextRunner
            .withBean(EventHubsProvisioner.class, () -> mock(EventHubsProvisioner.class))
            .withBean(AzureEventHubsProperties.class, () -> properties)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubsBinderConfiguration.class);
                assertThat(context).hasSingleBean(EventHubsExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(EventHubsChannelProvisioner.class);
                assertThat(context).hasSingleBean(EventHubsMessageChannelBinder.class);

                EventHubsChannelProvisioner channelProvisioner = context.getBean(EventHubsChannelProvisioner.class);
                assertThat(channelProvisioner).isInstanceOf(EventHubsChannelResourceManagerProvisioner.class);
            });
    }
}
