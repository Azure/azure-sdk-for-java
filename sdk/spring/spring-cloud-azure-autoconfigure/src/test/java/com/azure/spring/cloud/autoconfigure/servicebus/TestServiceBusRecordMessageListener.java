// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;

public class TestServiceBusRecordMessageListener implements ServiceBusRecordMessageListener {

    @Override
    public void onMessage(ServiceBusReceivedMessageContext messageContext) {

    }
}
