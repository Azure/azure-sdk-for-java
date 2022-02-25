// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusErrorContext;

import java.util.function.Consumer;

public class TestServiceBusErrorContextConsumer implements Consumer<ServiceBusErrorContext> {


    @Override
    public void accept(ServiceBusErrorContext errorContext) {

    }
}
