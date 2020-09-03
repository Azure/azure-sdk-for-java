// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ServiceBusSenderAsyncClient}.
 */
public class ServiceBusSenderAsyncClientJavaDocCodeSamples {
    private final ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
        .connectionString("fake-string");

    /**
     * Code snippet demonstrating how to create an {@link ServiceBusSenderAsyncClient}.
     */
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiation
        // The required parameter is a way to authenticate with Service Bus using credentials.
        // The connectionString provides a way to authenticate with Service Bus.
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}")
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiation

        sender.close();
    }

    /**
     * Code snippet demonstrating how to create an {@link ServiceBusSenderAsyncClient}.
     */
    public void instantiateWithDefaultCredential() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiateWithDefaultCredential
        // The required parameter is a way to authenticate with Service Bus using credentials.
        // The connectionString provides a way to authenticate with Service Bus.
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>",
                new DefaultAzureCredentialBuilder().build())
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiateWithDefaultCredential

        sender.close();
    }

    /**
     * Code snippet demonstrating how to send a batch to Service Bus queue or topic.
     */
    public void sendBatch() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncsenderclient.createBatch
        // The required parameter is a way to authenticate with Service Bus using credentials.
        // The connectionString provides a way to authenticate with Service Bus.
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}")
            .sender()
            .queueName("<QUEUE OR TOPIC NAME>")
            .buildAsyncClient();

        // Creating a batch without options set, will allow for automatic routing of events to any partition.
        sender.createBatch().flatMap(batch -> {
            batch.tryAdd(new ServiceBusMessage("test-1".getBytes(UTF_8)));
            batch.tryAdd(new ServiceBusMessage("test-2".getBytes(UTF_8)));
            return sender.sendMessages(batch);
        }).subscribe(unused -> {
        },
            error -> System.err.println("Error occurred while sending batch:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.servicebus.servicebusasyncsenderclient.createBatch

        sender.close();
    }


    /**
     * Code snippet demonstrating how to create a size-limited {@link ServiceBusMessageBatch} and send it.
     */
    public void batchSizeLimited() {
        final ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .sender()
            .buildAsyncClient();

        final ServiceBusMessage firstMessage = new ServiceBusMessage("92".getBytes(UTF_8));
        firstMessage.getApplicationProperties().put("telemetry", "latency");
        final ServiceBusMessage secondMessage = new ServiceBusMessage("98".getBytes(UTF_8));
        secondMessage.getApplicationProperties().put("telemetry", "cpu-temperature");

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncsenderclient.createBatch#CreateBatchOptionsLimitedSize
        final Flux<ServiceBusMessage> telemetryMessages = Flux.just(firstMessage, secondMessage);

        // Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
        // In this case, all the batches created with these options are limited to 256 bytes.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setMaximumSizeInBytes(256);
        final AtomicReference<ServiceBusMessageBatch> currentBatch = new AtomicReference<>(
            sender.createBatch(options).block());

        // The sample Flux contains two messages, but it could be an infinite stream of telemetry messages.
        telemetryMessages.flatMap(message -> {
            final ServiceBusMessageBatch batch = currentBatch.get();
            if (batch.tryAdd(message)) {
                return Mono.empty();
            }

            return Mono.when(
                sender.sendMessages(batch),
                sender.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);

                    // Add the message that did not fit in the previous batch.
                    if (!newBatch.tryAdd(message)) {
                        throw Exceptions.propagate(new IllegalArgumentException(
                            "Message was too large to fit in an empty batch. Max size: " + newBatch.getMaxSizeInBytes()));
                    }

                    return newBatch;
                }));
        }).then()
            .doFinally(signal -> {
                final ServiceBusMessageBatch batch = currentBatch.getAndSet(null);
                if (batch != null && batch.getCount() > 0) {
                    sender.sendMessages(batch).block();
                }
            });
        // END: com.azure.messaging.servicebus.servicebusasyncsenderclient.createBatch#CreateBatchOptionsLimitedSize
    }
}
