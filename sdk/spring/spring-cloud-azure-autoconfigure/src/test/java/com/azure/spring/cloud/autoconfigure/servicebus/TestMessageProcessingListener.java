// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;

public class TestMessageProcessingListener implements RecordMessageProcessingListener {

    @Override
    public void onMessage(ServiceBusReceivedMessageContext messageContext) {

    }
}
