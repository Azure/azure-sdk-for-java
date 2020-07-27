/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroup;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManager;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.spring.cloud.context.core.util.TypeMap;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.queue.CloudQueue;
import org.springframework.lang.NonNull;

public class AzureResourceManagerProvider implements ResourceManagerProvider {
    private TypeMap resourceManagerByType = new TypeMap();

    public AzureResourceManagerProvider(@NonNull Azure azure, @NonNull AzureProperties azureProperties) {
        this.resourceManagerByType
                .put(EventHubNamesapceManager.class, new EventHubNamesapceManager(azure, azureProperties));
        this.resourceManagerByType.put(EventHubManager.class, new EventHubManager(azure, azureProperties));
        this.resourceManagerByType
                .put(EventHubConsumerGroupManager.class, new EventHubConsumerGroupManager(azure, azureProperties));
        this.resourceManagerByType.put(RedisCacheManager.class, new RedisCacheManager(azure, azureProperties));
        this.resourceManagerByType.put(ResourceGroupManager.class, new ResourceGroupManager(azure, azureProperties));
        this.resourceManagerByType
                .put(ServiceBusNamesapceManager.class, new ServiceBusNamesapceManager(azure, azureProperties));
        this.resourceManagerByType
                .put(ServiceBusQueueManager.class, new ServiceBusQueueManager(azure, azureProperties));
        this.resourceManagerByType
                .put(ServiceBusTopicManager.class, new ServiceBusTopicManager(azure, azureProperties));
        this.resourceManagerByType.put(ServiceBusTopicSubscriptionManager.class,
                new ServiceBusTopicSubscriptionManager(azure, azureProperties));
        this.resourceManagerByType.put(StorageAccountManager.class, new StorageAccountManager(azure, azureProperties));
        this.resourceManagerByType.put(StorageQueueManager.class, new StorageQueueManager(azure, azureProperties));
        this.getResourceGroupManager().getOrCreate(azureProperties.getResourceGroup());
    }

    @Override
    public ResourceManager<EventHub, Tuple<EventHubNamespace, String>> getEventHubManager() {
        return this.resourceManagerByType.get(EventHubManager.class);
    }

    @Override
    public ResourceManager<EventHubConsumerGroup, Tuple<EventHub, String>> getEventHubConsumerGroupManager() {
        return this.resourceManagerByType.get(EventHubConsumerGroupManager.class);
    }

    @Override
    public ResourceManager<ResourceGroup, String> getResourceGroupManager() {
        return this.resourceManagerByType.get(ResourceGroupManager.class);
    }

    @Override
    public ResourceManager<EventHubNamespace, String> getEventHubNamespaceManager() {
        return this.resourceManagerByType.get(EventHubNamesapceManager.class);
    }

    @Override
    public ResourceManager<RedisCache, String> getRedisCacheManager() {
        return this.resourceManagerByType.get(RedisCacheManager.class);
    }

    @Override
    public ResourceManager<ServiceBusNamespace, String> getServiceBusNamespaceManager() {
        return this.resourceManagerByType.get(ServiceBusNamesapceManager.class);
    }

    @Override
    public ResourceManager<Queue, Tuple<ServiceBusNamespace, String>> getServiceBusQueueManager() {
        return this.resourceManagerByType.get(ServiceBusQueueManager.class);
    }

    @Override
    public ResourceManager<Topic, Tuple<ServiceBusNamespace, String>> getServiceBusTopicManager() {
        return this.resourceManagerByType.get(ServiceBusTopicManager.class);
    }

    @Override
    public ResourceManager<StorageAccount, String> getStorageAccountManager() {
        return this.resourceManagerByType.get(StorageAccountManager.class);
    }

    @Override
    public ResourceManager<CloudQueue, Tuple<CloudStorageAccount, String>> getStorageQueueManager() {
        return this.resourceManagerByType.get(StorageQueueManager.class);
    }

    @Override
    public ResourceManager<ServiceBusSubscription, Tuple<Topic, String>> getServiceBusTopicSubscriptionManager() {
        return this.resourceManagerByType.get(ServiceBusTopicSubscriptionManager.class);
    }
}
