// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.core.api.SubscribeByGroupOperation;

/**
 * Azure service bus topic operation to support send {@link org.springframework.messaging.Message} asynchronously
 * and subscribe by {@link ServiceBusSubscription} as consumer group
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicOperation extends SendOperation, SubscribeByGroupOperation {
    void setClientConfig(ServiceBusClientConfig clientConfig);
}
