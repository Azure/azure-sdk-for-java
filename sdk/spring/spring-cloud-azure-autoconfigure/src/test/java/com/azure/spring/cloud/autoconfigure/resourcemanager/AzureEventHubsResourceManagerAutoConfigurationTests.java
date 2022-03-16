// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.resourcemanager.EventHubsResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.EventHubsArmConnectionStringProvider;
import com.azure.spring.cloud.resourcemanager.provisioning.EventHubsProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AzureEventHubsResourceManagerAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubsResourceManagerAutoConfiguration.class));

    private final String connectionString = "connection-string=Endpoint=sb://eventhub-test-1"
        + ".servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;"
        + "SharedAccessKey=ByyyxxxUw=";

    @Test
    void testEventHubResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.eventhubs.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureEventHubsResourceManagerAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(EventHubsArmConnectionStringProvider.class);
                assertThat(context).doesNotHaveBean(EventHubsProvisioner.class);
            });
    }

    @Test
    void testEventHubResourceManagerWithoutEventHubClientBuilderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubClientBuilder.class))
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureEventHubsResourceManagerAutoConfiguration.class);
                assertThat(context).hasSingleBean(EventHubsResourceMetadata.class);
                assertThat(context).hasSingleBean(EventHubsProvisioner.class);
                assertThat(context).doesNotHaveBean(EventHubsArmConnectionStringProvider.class);
            });
    }

    @Test
    void testEventHubResourceManagerWithoutEventHubsProvisionerClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubsProvisioner.class))
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsResourceManagerAutoConfiguration.class));
    }

    @Test
    void testEventHubResourceManagerWithoutResourceManagerClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(AzureResourceManager.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(EventHubsArmConnectionStringProvider.class);
                assertThat(context).doesNotHaveBean(EventHubsProvisioner.class);
            });
    }

    @Test
    void testEventHubArmConnectionStringProviderBeanDisabled() {
        this.contextRunner
            .withPropertyValues(AzureEventHubsProperties.PREFIX + "." + connectionString)
            .run(context -> assertThat(context).doesNotHaveBean(EventHubsArmConnectionStringProvider.class));
    }

    @Test
    void testAzureEventHubsResourceManagerAutoConfigurationBeans() {
        this.contextRunner
            .withUserConfiguration(AzureResourceManagerAutoConfiguration.class)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .withBean(AzureEventHubsProperties.class, AzureEventHubsProperties::new)
            .run(context -> assertThat(context).hasSingleBean(EventHubsProvisioner.class));
    }
}
