// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.queue.AzureStorageQueueProperties;
import com.azure.spring.cloud.resourcemanager.connectionstring.StorageQueueArmConnectionStringProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureStorageQueueResourceManagerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageQueueResourceManagerAutoConfiguration.class));

    @Test
    void testStorageQueueResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues(AzureStorageQueueProperties.PREFIX + "enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(StorageQueueArmConnectionStringProvider.class));
    }

    @Test
    void testAzureServiceBusResourceManagerAutoConfigurationBeans() {
        this.contextRunner
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class,
                AzureResourceManagerAutoConfiguration.class)
            .withBean(AzureResourceManager.class, TestAzureResourceManager::getAzureResourceManager)
            .withBean(AzureStorageQueueProperties.class, AzureStorageQueueProperties::new)
            .withPropertyValues(AzureStorageQueueProperties.PREFIX + ".account-name=test-account")
            .run(context -> assertThat(context).hasSingleBean(StorageQueueArmConnectionStringProvider.class));
    }
}
