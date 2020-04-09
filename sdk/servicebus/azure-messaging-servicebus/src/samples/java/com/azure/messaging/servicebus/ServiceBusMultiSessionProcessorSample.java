package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.models.ServiceBusErrorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceBusMultiSessionProcessorSample {

    public static void main(String[] args) throws Exception {
        final MessageProcessor processor = new MessageProcessor();
        ServiceBusMultiSessionProcessorClient multiSessionProcessorClient = new ServiceBusClientBuilder()
            .connectionString("connectionString")
            .receiverMultiSession()
            .processMessage((receivedMessage, sessionManager) -> processor.onMessage(receivedMessage, sessionManager))
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .buildMultiSessionProcessorClient();

        System.out.println("Starting message processor");
        multiSessionProcessorClient.start();
        System.out.println("Stopping message processor");
        multiSessionProcessorClient.stop();

        System.out.println("Exiting process");
    }
    static class MessageProcessor implements AutoCloseable {
        private final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final ConcurrentHashMap<String, Set<String>> partitionsProcessing = new ConcurrentHashMap<>();
        void onMessage(ServiceBusReceivedMessage message, SessionManager manager) {
        }
        void onError(ServiceBusErrorContext errorContext) {
        }
        @Override
        public void close() {
            if (isDisposed.getAndSet(true)) {
                return;
            }
        }
    }
}
