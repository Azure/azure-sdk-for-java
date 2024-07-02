// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.ServiceBusArmConnectionStringProvider;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.ServiceBusProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AzureServiceBusResourceManagerAutoConfigurationTests {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusResourceManagerAutoConfiguration.class));

    private final String connectionString = "connection-string=Endpoint=sb://test.servicebus.windows.net/;"
        + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=ByyyxxxUw=";

    @Test
    void testServiceBusResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(ServiceBusProvisioner.class);
            });
    }

    @Test
    void testServiceBusResourceManagerWithoutServiceBusClientBuilderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusClientBuilder.class))
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusResourceManagerAutoConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusResourceMetadata.class);
                assertThat(context).hasSingleBean(ServiceBusProvisioner.class);
            });
    }

    @Test
    void testServiceBusResourceManagerWithoutServiceBusProvisionerClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusProvisioner.class))
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusResourceManagerAutoConfiguration.class));
    }

    @Test
    void testServiceBusArmConnectionStringProviderBeanDisabled() {
        this.contextRunner
            .withPropertyValues(AzureServiceBusProperties.PREFIX + "." + connectionString)
            .run(context -> assertThat(context).doesNotHaveBean(ServiceBusArmConnectionStringProvider.class));
    }

    @Test
    void testAzureServiceBusResourceManagerAutoConfigurationBeans() {
        this.contextRunner
            .withUserConfiguration(AzureResourceManagerAutoConfiguration.class)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .withBean(AzureServiceBusProperties.class, AzureServiceBusProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusProvisioner.class);
            });
    }
}
