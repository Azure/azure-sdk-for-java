/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.inbound;

import com.microsoft.azure.spring.integration.core.AbstractInboundChannelAdapter;
import com.microsoft.azure.spring.integration.core.api.SubscribeOperation;
import org.springframework.lang.NonNull;

public class ServiceBusQueueInboundChannelAdapter extends AbstractInboundChannelAdapter {

    public ServiceBusQueueInboundChannelAdapter(String destination, @NonNull SubscribeOperation subscribeOperation) {
        super(destination);
        this.subscribeOperation = subscribeOperation;
    }
}
