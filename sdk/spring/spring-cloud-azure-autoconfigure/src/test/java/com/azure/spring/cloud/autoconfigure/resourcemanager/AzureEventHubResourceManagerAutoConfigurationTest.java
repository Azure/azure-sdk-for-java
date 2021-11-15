// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.EventHubArmConnectionStringProvider;
import com.azure.spring.eventhubs.provisioning.EventHubProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AzureEventHubResourceManagerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubResourceManagerAutoConfiguration.class));

    private final String connectionString = "connection-string=Endpoint=sb://eventhub-test-1"
        + ".servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;"
        + "SharedAccessKey=ByyyxxxUw=";

    @Test
    void testEventHubResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(EventHubArmConnectionStringProvider.class);
                assertThat(context).doesNotHaveBean(EventHubProvisioner.class);
            });
    }

    @Test
    void testEventHubResourceManagerWithoutEventHubClientBuilderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubClientBuilder.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(EventHubArmConnectionStringProvider.class);
                assertThat(context).doesNotHaveBean(EventHubProvisioner.class);
            });
    }

    @Test
    void testEventHubArmConnectionStringProviderBeanDisabled() {
        this.contextRunner
            .withPropertyValues(AzureEventHubProperties.PREFIX + "." + connectionString)
            .run(context -> assertThat(context).doesNotHaveBean(EventHubArmConnectionStringProvider.class));
    }

    @Test
    void testAzureEventHubResourceManagerAutoConfigurationBeans() {
        this.contextRunner
            .withUserConfiguration(AzureResourceManagerAutoConfiguration.class)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .withBean(AzureEventHubProperties.class, AzureEventHubProperties::new)
            .run(context -> assertThat(context).hasSingleBean(EventHubProvisioner.class));
    }
}
