// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.TestSpringTokenCredentialProviderContextProviderAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueProperties;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.StorageQueueArmConnectionStringProvider;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(strings = {
        "spring.cloud.azure.storage.queue.connection-string=DefaultEndpointsProtocol=https;AccountName=test;AccountKey=key;EndpointSuffix=core.windows.net",
        "spring.cloud.azure.credential.token-credential-bean-name=my-token-credential",
        "spring.cloud.azure.storage.queue.credential.token-credential-bean-name=my-token-credential"
    })
    void testNotCreateProviderBeanWhenMissingPropertiesConfigured(String missingProperty) {
        this.contextRunner
            .withUserConfiguration(TestSpringTokenCredentialProviderContextProviderAutoConfiguration.class,
                AzureGlobalPropertiesAutoConfiguration.class)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=test-tenant",
                "spring.cloud.azure.profile.subscription-id=test-subscription-id",
                missingProperty
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureResourceManager.class);
                assertThat(context).doesNotHaveBean(StorageQueueArmConnectionStringProvider.class);
            });
    }
}
