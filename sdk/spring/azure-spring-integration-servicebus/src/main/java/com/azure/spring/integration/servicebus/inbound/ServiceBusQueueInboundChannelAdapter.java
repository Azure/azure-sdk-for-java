// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound;

import com.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.azure.spring.integration.core.api.SubscribeOperation;
import org.springframework.lang.NonNull;

/**
 * Inbound channel adapter for Service Bus Queue.
 */
public class ServiceBusQueueInboundChannelAdapter extends AbstractInboundChannelAdapter {

    public ServiceBusQueueInboundChannelAdapter(String destination, @NonNull SubscribeOperation subscribeOperation) {
        super(destination);
        this.subscribeOperation = subscribeOperation;
    }
}
