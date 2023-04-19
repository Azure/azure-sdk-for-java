package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

public final class ContextAccessor {
    private static final ClientLogger LOGGER = new ClientLogger(ContextAccessor.class);
    private static MessageGetContextAccessor getAccessor;
    private static MessageSetContextAccessor setAccessor;

    public interface MessageGetContextAccessor {
        Context getContext();
    }

    public interface MessageSetContextAccessor {
        ServiceBusReceivedMessage setContext(Context context);
    }

    public static void setMessageGetContextAccessor(final MessageGetContextAccessor accessor) {
        getAccessor = accessor;
    }

    public static void setMessageSetContextAccessor(final MessageSetContextAccessor accessor) {
        setAccessor = accessor;
    }
}
