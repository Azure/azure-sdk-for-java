// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.topic;

import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.messaging.core.SubscribeByGroupOperation;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;
import com.azure.spring.servicebus.health.InstrumentationManager;

/**
 * Azure service bus topic operation to support sending {@link org.springframework.messaging.Message} asynchronously
 * and subscribing a topic.
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicOperation extends SendOperation, SubscribeByGroupOperation {

    InstrumentationManager getInstrumentationManager();

    void setClientConfig(ServiceBusClientConfig clientConfig);

}
