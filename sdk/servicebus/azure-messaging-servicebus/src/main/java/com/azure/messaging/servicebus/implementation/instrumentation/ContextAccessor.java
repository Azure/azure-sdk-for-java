// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

public final class ContextAccessor {
    private static ReceiveMessageContextAccessor receiveAccessor;
    private static SendMessageContextAccessor sendAccessor;

    public interface ReceiveMessageContextAccessor {
        ServiceBusReceivedMessage setContext(ServiceBusReceivedMessage message, Context context);
        Context getContext(ServiceBusReceivedMessage message);
    }

    public interface SendMessageContextAccessor {
        Context getContext(ServiceBusMessage message);
    }

    public static ServiceBusReceivedMessage setContext(ServiceBusReceivedMessage message, Context context) {
        assert message != null; // message is never null on this path.
        return receiveAccessor.setContext(message, context);
    }

    public static Context getContext(ServiceBusReceivedMessage message) {
        return receiveAccessor.getContext(message);
    }

    public static Context getContext(ServiceBusMessage message) {
        return sendAccessor.getContext(message);
    }

    public static void setReceiveMessageContextAccessor(final ReceiveMessageContextAccessor accessor) {
        receiveAccessor = accessor;
    }

    public static void setSendMessageContextAccessor(final SendMessageContextAccessor accessor) {
        sendAccessor = accessor;
    }
}
