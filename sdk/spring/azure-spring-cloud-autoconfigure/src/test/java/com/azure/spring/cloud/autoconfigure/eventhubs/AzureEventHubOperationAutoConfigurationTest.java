// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AzureEventHubOperationAutoConfigurationTest {
    
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubOperationAutoConfiguration.class));

   /* @Test
    void testAzureEventHubDisabled() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    void testWithoutEventHubClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(EventHubConsumerAsyncClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    void testAzureEventHubPropertiesStorageAccountIllegal() {
        this.contextRunner.withPropertyValues("spring.cloud.azure.eventhub.checkpoint-storage-account=1")
                          .run(context -> assertThrows(IllegalStateException.class,
                                  () -> context.getBean(AzureEventHubProperties.class)));
    }

    @Test
    void testAzureEventHubPropertiesConfigured() {
        this.contextRunner.withPropertyValues(
            EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
            EVENT_HUB_PROPERTY_PREFIX + "checkpoint-storage-account=sa1",
            EVENT_HUB_PROPERTY_PREFIX + "connection-string=str1")
                          .run(context -> {
                              assertThat(context).hasSingleBean(AzureEventHubProperties.class);
                              assertThat(context.getBean(AzureEventHubProperties.class).getNamespace()).isEqualTo(
                                  "ns1");
                              assertThat(context.getBean(AzureEventHubProperties.class).getConnectionString()).isEqualTo("str1");
                          });
    }*/
    // TODO (xiada): test
/**
    @Test
    void testConnectionStringProvided() {
        this.contextRunner.withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + "connection-string=str1")
                          .run(context -> {
                              assertThat(context.getBean(EventHubConnectionStringProvider.class).getConnectionString()).isEqualTo("str1");
                              assertThat(context).hasSingleBean(EventHubClientFactory.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                              assertThat(context).doesNotHaveBean(EventHubNamespaceManager.class);
                              assertThat(context).doesNotHaveBean(
                                  com.azure.spring.cloud.resourcemanager.core.impl.StorageAccountCrud.class);
                          });
    }

    @Test
    void testResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(
            TestConfigWithAzureResourceManagerAndConnectionProvider.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
                              EVENT_HUB_PROPERTY_PREFIX + "checkpoint-storage-account=sa1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(EventHubClientFactory.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                              assertThat(context).hasSingleBean(EventHubNamespaceManager.class);
                              assertThat(context).hasSingleBean(
                                  com.azure.spring.cloud.resourcemanager.core.impl.StorageAccountCrud.class);
                          });
    }

    @Test
    void testEventHubOperationProvidedNotStorageUnderSP() {
        this.contextRunner.withUserConfiguration(
                TestConfigWithAzureResourceManagerAndConnectionProvider.class,
                AzureEventHubOperationAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(EventHubNamespaceManager.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                              assertThat(context).doesNotHaveBean(
                                  com.azure.spring.cloud.resourcemanager.core.impl.StorageAccountCrud.class);
                          });
    }

    @Test
    void testEventHubOperationProvidedNotStorageUnderMSI() {
        this.contextRunner.withUserConfiguration(
                TestConfigWithAzureResourceManagerAndConnectionProvider.class,
                AzureEventHubOperationAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              AZURE_PROPERTY_PREFIX + "msi-enabled=true",
                              EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
                              AZURE_PROPERTY_PREFIX + "subscription-id=sub"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(EventHubNamespaceManager.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                              assertThat(context).doesNotHaveBean(
                                  com.azure.spring.cloud.resourcemanager.core.impl.StorageAccountCrud.class);
                          });
    }

    @Configuration
    @Import(TestConfigWithAzureResourceManager.class)
    static class TestConfigWithAzureResourceManagerAndConnectionProvider {

        @Bean
        @Primary
        AzureResourceManager azureResourceManagerMock() {
            final AzureResourceManager mockResourceManager = mock(AzureResourceManager.class);
            final StorageManager mockStorageManager = mock(StorageManager.class);
            final StorageAccounts mockStorageAccounts = mock(StorageAccounts.class);
            final StorageAccount mockStorageAccount = mock(StorageAccount.class);
            final List<StorageAccountKey> mockStorageAccountKeys = singletonList(mock(StorageAccountKey.class));

            when(mockResourceManager.storageAccounts()).thenReturn(mockStorageAccounts);
            when(mockStorageAccounts.getByResourceGroup(anyString(), anyString())).thenReturn(mockStorageAccount);
            when(mockStorageAccount.getKeys()).thenReturn(mockStorageAccountKeys);
            when(mockStorageAccount.manager()).thenReturn(mockStorageManager);
            when(mockStorageManager.environment()).thenReturn(AzureEnvironment.AZURE);
            return mockResourceManager;
        }

        @Bean
        EventHubConnectionStringProvider eventHubConnectionStringProvider() {
            return new EventHubConnectionStringProvider("fake-string");
        }

    }*/

}
