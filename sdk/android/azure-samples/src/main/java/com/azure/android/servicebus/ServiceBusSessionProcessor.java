// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;


import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import android.util.Log;

/**
 * Sample to demonstrate the creation of a session-enabled {@link ServiceBusProcessorClient} and starting the processor
 * to receive messages.
 */
public class ServiceBusSessionProcessor {

    private static final String TAG = "ServiceBusSessionProcessorOutput";

    /**
     * Processes each message from the Service Bus entity.
     *
     * @param context Received message context.
     */
    public static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        Log.i(TAG, String.format("Processing message. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
            message.getSequenceNumber(), message.getBody()));

        // When this message function completes, the message is automatically completed. If an exception is
        // thrown in here, the message is abandoned.
        // To disable this behaviour, toggle ServiceBusSessionProcessorClientBuilder.disableAutoComplete()
        // when building the session receiver.
    }

    /**
     * Processes an exception that occurred in the Service Bus Processor.
     *
     * @param context Context around the exception that occurred.
     */
    public static void processError(ServiceBusErrorContext context) {
        Log.e(TAG, String.format("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
            context.getFullyQualifiedNamespace(), context.getEntityPath()));

        if (!(context.getException() instanceof ServiceBusException)) {
            Log.e(TAG, String.format("Non-ServiceBusException occurred: %s%n", context.getException()));
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        Log.e(TAG, String.format("ServiceBusException source: %s. Reason: %s. Is transient? %s%n", context.getErrorSource(),
            exception.getReason(), exception.isTransient()));
    }
}
