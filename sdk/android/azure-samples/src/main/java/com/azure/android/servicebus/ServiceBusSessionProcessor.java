// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.servicebus;

import com.azure.identity.ClientSecretCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import org.junit.jupiter.api.Test;

import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Sample to demonstrate the creation of a session-enabled {@link ServiceBusProcessorClient} and starting the processor
 * to receive messages.
 */
public class ServiceBusSessionProcessor {

    private static final String TAG = "ServiceBusSessionProcessorOutput";

    public static void main(String queueName, ClientSecretCredential credential) throws InterruptedException {

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace("https://android-service-bus.servicebus.windows.net")
            .credential(credential) // Use DefaultAzureCredential for authentication
            .sessionProcessor()
            .queueName(queueName)
            .maxConcurrentSessions(2)
            .processMessage(ServiceBusSessionProcessor::processMessage)
            .processError(ServiceBusSessionProcessor::processError)
            .buildProcessorClient();

        Log.i(TAG, "Starting the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(10);
        Log.i(TAG, "Stopping the processor");
        processorClient.stop();

        TimeUnit.SECONDS.sleep(10);
        Log.i(TAG, "Resuming the processor");
        processorClient.start();

        TimeUnit.SECONDS.sleep(10);
        Log.i(TAG, "Closing the processor");
        processorClient.close();
    }

    /**
     * Processes each message from the Service Bus entity.
     *
     * @param context Received message context.
     */
    private static void processMessage(ServiceBusReceivedMessageContext context) {
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
    private static void processError(ServiceBusErrorContext context) {
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
