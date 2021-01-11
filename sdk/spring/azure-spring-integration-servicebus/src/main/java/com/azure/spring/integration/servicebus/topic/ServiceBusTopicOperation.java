// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;

/**
 * Azure service bus topic operation to support sending {@link org.springframework.messaging.Message} asynchronously
 * and subscribing a topic.
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicOperation extends SendOperation, SubscribeByGroupOperation {

    void setClientConfig(ServiceBusClientConfig clientConfig);

}
