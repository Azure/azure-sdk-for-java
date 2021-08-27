// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.core.management.AzureEnvironment;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureEventHubAutoConfigurationTest {

    private static final String EVENT_HUB_PROPERTY_PREFIX = "spring.cloud.azure.eventhub.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureEventHubAutoConfiguration.class));

    @Test
    public void testAzureEventHubDisabled() {
        this.contextRunner.withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + "enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    public void testWithoutEventHubClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(EventHubConsumerAsyncClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubProperties.class));
    }

    @Test
    public void testAzureEventHubPropertiesStorageAccountIllegal() {
        this.contextRunner.withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + "checkpoint-storage-account=1")
                          .run(context -> assertThrows(IllegalStateException.class,
                                  () -> context.getBean(AzureEventHubProperties.class)));
    }

    @Test
    public void testAzureEventHubPropertiesConfigured() {
        this.contextRunner.withPropertyValues(
            EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
            EVENT_HUB_PROPERTY_PREFIX + "checkpoint-storage-account=sa1",
            EVENT_HUB_PROPERTY_PREFIX + "connection-string=str1")
                          .run(context -> {
                              assertThat(context).hasSingleBean(AzureEventHubProperties.class);
                              assertThat(context.getBean(AzureEventHubProperties.class).getNamespace()).isEqualTo(
                                  "ns1");
                              assertThat(context.getBean(AzureEventHubProperties.class).getConnectionString()).isEqualTo("str1");
                              assertThat(context.getBean(AzureEventHubProperties.class).getCheckpointStorageAccount()).isEqualTo("sa1");
                          });
    }

    @Test
    public void testConnectionStringProvided() {
        this.contextRunner.withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + "connection-string=str1")
                          .run(context -> {
                              assertThat(context.getBean(EventHubConnectionStringProvider.class).getConnectionString()).isEqualTo("str1");
                              assertThat(context).hasSingleBean(EventHubClientFactory.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                              assertThat(context).doesNotHaveBean(EventHubNamespaceManager.class);
                              assertThat(context).doesNotHaveBean(StorageAccountManager.class);
                          });
    }

    @Test
    public void testResourceManagerProvided() {
        this.contextRunner.withUserConfiguration(
            TestConfigWithAzureResourceManagerAndConnectionProvider.class,
            AzureEventHubAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
                              EVENT_HUB_PROPERTY_PREFIX + "checkpoint-storage-account=sa1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(EventHubClientFactory.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                              assertThat(context).hasSingleBean(EventHubNamespaceManager.class);
                              assertThat(context).hasSingleBean(StorageAccountManager.class);
                          });
    }

    @Test
    public void testEventHubOperationProvidedNotStorageUnder () {
        this.contextRunner.withUserConfiguration(
                TestConfigWithAzureResourceManagerAndConnectionProvider.class,
                AzureEventHubAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(EventHubNamespaceManager.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                          });
    }

    @Test
    public void testEventHubOperationProvidedNotStorageUnderMSI() {
        this.contextRunner.withUserConfiguration(
                TestConfigWithAzureResourceManagerAndConnectionProvider.class,
                AzureEventHubAutoConfiguration.class)
                          .withPropertyValues(
                              AZURE_PROPERTY_PREFIX + "resource-group=rg1",
                              AZURE_PROPERTY_PREFIX + "msi-enabled=true",
                              EVENT_HUB_PROPERTY_PREFIX + "namespace=ns1",
                              AZURE_PROPERTY_PREFIX + "subscription-id=sub"
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(EventHubNamespaceManager.class);
                              assertThat(context).hasSingleBean(EventHubOperation.class);
                          });
    }

    @Configuration
    @EnableConfigurationProperties(AzureProperties.class)
    public static class TestConfigWithAzureResourceManagerAndConnectionProvider {

        @Bean
        public AzureResourceManager azureResourceManager() {
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
        public EventHubConnectionStringProvider eventHubConnectionStringProvider() {
            return new EventHubConnectionStringProvider("fake-string");
        }

    }

}
