// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.core.api.SubscribeOperation;
import com.microsoft.azure.spring.integration.servicebus.ServiceBusClientConfig;

/**
 * Azure service bus queue operation to support send
 * {@link org.springframework.messaging.Message} asynchronously and subscribe
 *
 * @author Warren Zhu
 */
public interface ServiceBusQueueOperation extends SendOperation, SubscribeOperation {
    void setClientConfig(ServiceBusClientConfig clientConfig);
}
