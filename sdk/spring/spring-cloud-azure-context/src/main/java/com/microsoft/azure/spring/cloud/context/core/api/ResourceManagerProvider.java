/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.api;

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
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.queue.CloudQueue;

/**
 * Interface to provide {@link ResourceManager}
 *
 * @author Warren Zhu
 */
public interface ResourceManagerProvider {
    ResourceManager<EventHub, Tuple<EventHubNamespace, String>> getEventHubManager();
    ResourceManager<EventHubConsumerGroup, Tuple<EventHub, String>> getEventHubConsumerGroupManager();
    ResourceManager<ResourceGroup, String> getResourceGroupManager();
    ResourceManager<EventHubNamespace, String> getEventHubNamespaceManager();
    ResourceManager<RedisCache, String> getRedisCacheManager();
    ResourceManager<ServiceBusNamespace, String> getServiceBusNamespaceManager();
    ResourceManager<Queue, Tuple<ServiceBusNamespace, String>> getServiceBusQueueManager();
    ResourceManager<Topic, Tuple<ServiceBusNamespace, String>> getServiceBusTopicManager();
    ResourceManager<StorageAccount, String> getStorageAccountManager();
    ResourceManager<CloudQueue, Tuple<CloudStorageAccount, String>> getStorageQueueManager();
    ResourceManager<ServiceBusSubscription, Tuple<Topic, String>> getServiceBusTopicSubscriptionManager();
}
