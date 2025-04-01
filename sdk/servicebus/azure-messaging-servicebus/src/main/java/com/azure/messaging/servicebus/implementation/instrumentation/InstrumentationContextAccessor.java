// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import io.clientcore.core.instrumentation.InstrumentationContext;

public final class InstrumentationContextAccessor {
    public interface ReceiveMessageInstrumentationContextAccessor {
        ServiceBusReceivedMessage set(ServiceBusReceivedMessage message, InstrumentationContext context);
        InstrumentationContext get(ServiceBusReceivedMessage message);
    }
    private static ReceiveMessageInstrumentationContextAccessor receiveAccessor;
    public static void setReceiveMessageContextAccessor(final ReceiveMessageInstrumentationContextAccessor accessor) {
        receiveAccessor = accessor;
    }
    public static ServiceBusReceivedMessage set(ServiceBusReceivedMessage message, InstrumentationContext context) {
        assert message != null;
        return receiveAccessor.set(message, context);
    }

    public static InstrumentationContext get(ServiceBusReceivedMessage message) {
        return receiveAccessor.get(message);
    }

    public interface SendMessageInstrumentationContextAccessor {
        InstrumentationContext get(ServiceBusMessage message);
    }
    private static SendMessageInstrumentationContextAccessor sendAccessor;
    public static void setSendMessageContextAccessor(final SendMessageInstrumentationContextAccessor accessor) {
        sendAccessor = accessor;
    }
    public static InstrumentationContext get(ServiceBusMessage message) {
        return sendAccessor.get(message);
    }
}
