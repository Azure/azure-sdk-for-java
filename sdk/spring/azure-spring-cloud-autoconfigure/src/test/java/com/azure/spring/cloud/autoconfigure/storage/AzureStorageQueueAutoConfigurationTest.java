// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AzureStorageQueueAutoConfigurationTest {

    private ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureStorageQueueOperationAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

   /* TODO (xiada): tests
   @Test
    void testAzureStoragePropertiesWhenMissingQueueServiceClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(QueueServiceClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(LegacyAzureStorageProperties.class));
    }

    @Test
    void testAzureStoragePropertiesWhenMissingStorageQueueClientFactoryClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(StorageQueueClientFactory.class))
                          .run(context -> assertThat(context).doesNotHaveBean(LegacyAzureStorageProperties.class));
    }

    @Test
    void testAzureStoragePropertiesNotConfigured() {
        this.contextRunner.run(context -> assertThat(context).doesNotHaveBean(LegacyAzureStorageProperties.class));
    }

    @Test
    void testAzureStoragePropertiesIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=a")
                          .run(context -> assertThrows(IllegalStateException.class,
                              () -> context.getBean(LegacyAzureStorageProperties.class)));
    }

    @Test
    void testAzureStoragePropertiesConfigured() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=squeue").run(context -> {
            assertThat(context).hasSingleBean(LegacyAzureStorageProperties.class);
            assertThat(context.getBean(LegacyAzureStorageProperties.class).getAccount()).isEqualTo("squeue");
        });
    }

    @Test
    void testAzureStoragePropertiesOtherItemsConfigured() {
        this.contextRunner.withPropertyValues(
            "spring.cloud.azure.storage.account=squeue",
            "spring.cloud.azure.storage.access-key=fake-access-key",
            "spring.cloud.azure.storage.resource-group=fake-resource-group"
        ).run(context -> {
            assertThat(context).hasSingleBean(LegacyAzureStorageProperties.class);
            assertThat(context.getBean(LegacyAzureStorageProperties.class).getAccessKey()).isEqualTo("fake-access-key");
            assertThat(context.getBean(LegacyAzureStorageProperties.class).getResourceGroup()).isEqualTo("fake-resource-group");
        });
    }

    @Test
    void testStorageQueueClientFactoryBean() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.storage.account=squeue").run(c -> {
            final StorageQueueClientFactory storageQueueClientFactory = c.getBean(StorageQueueClientFactory.class);
            assertThat(storageQueueClientFactory).isNotNull();
        });
    }

    @Test
    void testStorageQueueOperationBean() {
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
    }*/
}
