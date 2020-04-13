// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.models.ServiceBusErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceBusMultiSessionProcessorSample {

    public static void main(String[] args) {
        final MyMessageProcessor myMessageProcessor = new MyMessageProcessor();
        ServiceBusMultiSessionProcessorClient multiSessionProcessorClient = new ServiceBusClientBuilder()
            .connectionString("connectionString")
            .multiSessionProcessor()
            .processMessage((receivedMessage, sessionManager) -> myMessageProcessor.onMessage(receivedMessage, sessionManager))
            .processError(serviceBusErrorContext -> myMessageProcessor.onError(serviceBusErrorContext))
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .buildMultiSessionProcessorClient();

        System.out.println("Starting message processor");
        multiSessionProcessorClient.start();
        System.out.println("Stopping message processor");
        multiSessionProcessorClient.stop();

        System.out.println("Exiting process");
    }
    static class MyMessageProcessor implements AutoCloseable {
        private final Logger logger = LoggerFactory.getLogger(MyMessageProcessor.class);
        private final AtomicBoolean isDisposed = new AtomicBoolean();

        void onMessage(ServiceBusReceivedMessage message, SessionMessageManager manager) {
            System.out.println("Process message here. Session id : " + message.getSessionId()
                + ", message Id :" + message.getMessageId());
            manager.complete(message);
        }
        void onError(ServiceBusErrorContext errorContext) {
            System.out.println("Error !!! . FullyQualifiedNamespace : "
                + errorContext.getFullyQualifiedNamespace()
                + ", EntityPath :" + errorContext.getEntityPath());
        }
        @Override
        public void close() {
            if (isDisposed.getAndSet(true)) {
                return;
            }
        }
    }
}
