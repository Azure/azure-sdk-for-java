// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.ServiceBusArmConnectionStringProvider;
import com.azure.spring.servicebus.provisioning.ServiceBusQueueProvisioner;
import com.azure.spring.servicebus.provisioning.ServiceBusTopicProvisioner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureServiceBusResourceManagerAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusResourceManagerAutoConfiguration.class));

    private final String connectionString = "connection-string=Endpoint=sb://test.servicebus.windows.net/;"
        + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=ByyyxxxUw=";

    @Test
    void testServiceBusResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.servicebus.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(ServiceBusTopicProvisioner.class);
                assertThat(context).doesNotHaveBean(ServiceBusQueueProvisioner.class);
            });
    }

    @Test
    void testServiceBusResourceManagerWithoutServiceBusClientBuilderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusClientBuilder.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ServiceBusTopicProvisioner.class);
                assertThat(context).doesNotHaveBean(ServiceBusQueueProvisioner.class);
            });
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
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class,
                AzureResourceManagerAutoConfiguration.class)
            .withBean(AzureResourceManager.class, TestAzureResourceManager::getAzureResourceManager)
            .withBean(AzureServiceBusProperties.class, AzureServiceBusProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusTopicProvisioner.class);
                assertThat(context).hasSingleBean(ServiceBusQueueProvisioner.class);
            });
    }
}
