// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.StorageQueueArmConnectionStringProvider;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AzureStorageQueueResourceManagerAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueResourceManagerAutoConfiguration.class));

    @Test
    void testStorageQueueResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues(AzureStorageQueueProperties.PREFIX + "enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(StorageQueueArmConnectionStringProvider.class));
    }

    @Test
    void testStorageQueueResourceManagerAutoConfigurationBeans() {
        this.contextRunner
            .withUserConfiguration(AzureResourceManagerAutoConfiguration.class)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .withBean(AzureStorageQueueProperties.class, AzureStorageQueueProperties::new)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withPropertyValues(AzureStorageQueueProperties.PREFIX + ".account-name=test-account")
            .run(context -> assertThat(context).hasSingleBean(StorageQueueArmConnectionStringProvider.class));
    }

    @Test
    void shouldConfigureWithoutStorageQueueClientBuilderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(QueueServiceClientBuilder.class))
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageQueueResourceManagerAutoConfiguration.class);
                assertThat(context).hasSingleBean(StorageQueueResourceMetadata.class);
            });
    }

    @Test
    void testStorageQueueResourceManagerWithoutArmConnectionStringProviderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(StorageQueueArmConnectionStringProvider.class))
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageQueueResourceManagerAutoConfiguration.class));
    }
}
