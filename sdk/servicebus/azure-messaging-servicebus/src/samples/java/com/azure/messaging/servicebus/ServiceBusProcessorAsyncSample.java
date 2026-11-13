// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Processes a seeded message with reactive manual settlement. Pass {@code session} for a session-enabled queue.
 */
public class ServiceBusProcessorAsyncSample {
    private final CountDownLatch processed = new CountDownLatch(1);

    /**
     * Runs against an empty queue using Microsoft Entra ID authentication.
     * @param args Pass {@code session} when the queue requires sessions.
     * @throws InterruptedException If the wait for processing is interrupted.
     */
    public static void main(String[] args) throws InterruptedException {
        String namespace = requiredEnvironment("AZURE_SERVICEBUS_FULLY_QUALIFIED_NAMESPACE");
        String queue = requiredEnvironment("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
        boolean sessions = args.length > 0 && "session".equals(args[0]);
        ServiceBusProcessorAsyncSample sample = new ServiceBusProcessorAsyncSample();
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            // The namespace has the form {your-namespace}.servicebus.windows.net.
            .credential(namespace, new DefaultAzureCredentialBuilder().build());
        ServiceBusProcessorAsyncClient processor = sessions
            ? builder.sessionProcessorAsync().queueName(queue).disableAutoComplete()
                .maxConcurrentSessions(2).maxConcurrentCalls(2)
                .processMessage(sample::processMessage).processError(sample::processError).buildProcessorAsyncClient()
            : builder.processorAsync().queueName(queue).disableAutoComplete().maxConcurrentCalls(2)
                .processMessage(sample::processMessage).processError(sample::processError).buildProcessorAsyncClient();

        try {
            try (ServiceBusSenderClient sender = new ServiceBusClientBuilder()
                .credential(namespace, new DefaultAzureCredentialBuilder().build()).sender().queueName(queue)
                .buildClient()) {
                ServiceBusMessage message = new ServiceBusMessage("Process before acknowledgement")
                    .setMessageId(UUID.randomUUID().toString());
                if (sessions) {
                    message.setSessionId("sample-session");
                }
                sender.sendMessage(message);
            }
            // start() is cold. Subscribe (or block on this controlling thread) to start receiving.
            processor.start().block();
            if (!sample.processed.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The sample message was not processed within 30 seconds.");
            }
        } finally {
            // close() stops receiving and drains accepted handlers; call it outside the handler.
            processor.close();
        }
    }

    private Mono<Void> processMessage(ServiceBusReceivedMessageAsyncContext context) {
        // Represents non-blocking downstream work. Replace this delay with the Mono for your durable write or flush.
        // Do not call subscribe() here: return the entire operation so concurrency, renewal and drain track its lifetime.
        return Mono.delay(Duration.ofMillis(100)).then(Mono.defer(() -> {
            // Lock loss can cause redelivery. Use deduplication checks or upserts to make real work idempotent:
            // https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement
            return context.complete();
        })).doOnSuccess(ignored -> processed.countDown());
    }

    private Mono<Void> processError(ServiceBusErrorContext context) {
        return Mono.fromRunnable(() -> System.err.println("Processor error: " + context.getException()));
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Set " + name + " before running this sample.");
        }
        return value;
    }
}
