// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import com.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.azure.spring.integration.storage.queue.factory.DefaultStorageQueueClientFactory;
import com.azure.spring.integration.storage.queue.factory.StorageQueueClientFactory;
import com.azure.storage.queue.QueueServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class AzureStorageQueueAutoConfigurationTest {

    private ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureStorageQueueOperationAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

   /* TODO (xiada): test
   @Test
    public void testAzureStoragePropertiesWhenMissingQueueServiceClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(QueueServiceClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(LegacyAzureStorageProperties.class));
    }

    @Test
    public void testAzureStoragePropertiesWhenMissingStorageQueueClientFactoryClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(StorageQueueClientFactory.class))
                          .run(context -> assertThat(context).doesNotHaveBean(LegacyAzureStorageProperties.class));
    }

    @Test
    public void testAzureStoragePropertiesNotConfigured() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(LegacyAzureStorageProperties.class));
    }

    @Test
    public void testAzureStoragePropertiesIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=a")
                          .run(context -> assertThrows(IllegalStateException.class,
                              () -> context.getBean(LegacyAzureStorageProperties.class)));
    }

    @Test
    public void testAzureStoragePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=squeue").run(context -> {
            assertThat(context).hasSingleBean(LegacyAzureStorageProperties.class);
            assertThat(context.getBean(LegacyAzureStorageProperties.class).getAccount()).isEqualTo("squeue");
        });
    }

    @Test
    public void testAzureStoragePropertiesOtherItemsConfigured() {
        this.contextRunner.withPropertyValues(
            "spring.cloud.azure.storage.account=squeue",
            "spring.cloud.azure.storage.access-key=fake-access-key",
            "spring.cloud.azure.storage.resource-group=fake-resource-group"
        ).run(context -> {
            assertThat(context).hasSingleBean(LegacyAzureStorageProperties.class);
            assertThat(context.getBean(LegacyAzureStorageProperties.class).getAccessKey()).isEqualTo("fake-access-key");
            assertThat(context.getBean(LegacyAzureStorageProperties.class).getResourceGroup()).isEqualTo("fake-resource-group");
        });
    }*/

    @Test
    public void testStorageQueueClientFactoryBean() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=squeue").run(c -> {
            final StorageQueueClientFactory storageQueueClientFactory = c.getBean(StorageQueueClientFactory.class);
            assertThat(storageQueueClientFactory).isNotNull();
        });
    }

    @Test
    public void testStorageQueueOperationBean() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=squeue").run(c -> {
            final StorageQueueOperation storageQueueOperation = c.getBean(StorageQueueOperation.class);
            assertThat(storageQueueOperation).isNotNull();
        });
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        DefaultStorageQueueClientFactory defaultStorageQueueClientFactory() {
            return mock(DefaultStorageQueueClientFactory.class);
        }
    }
}
