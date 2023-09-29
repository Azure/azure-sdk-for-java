// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ServiceBusSenderClient} and
 * {@link ServiceBusSenderAsyncClient}.
 */
public class ServiceBusSenderClientJavaDocCodeSamples {
    /**
     * Fully qualified namespace is the host name of the Service Bus resource.  It can be found by navigating to the
     * Service Bus namespace and looking in the "Essentials" panel.
     */
    private final String fullyQualifiedNamespace = System.getenv("AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME");
    private final String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
    /**
     * Name of a session-enabled queue in the Service Bus namespace.
     */
    private final String sessionEnabledQueueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SESSION_QUEUE_NAME");

    /**
     * Code snippet demonstrating how to create an {@link ServiceBusSenderClient}.
     */
    @Test
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.servicebussenderclient.instantiation
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .sender()
            .queueName(queueName)
            .buildClient();

        sender.sendMessage(new ServiceBusMessage("Foo bar"));
        // END: com.azure.messaging.servicebus.servicebussenderclient.instantiation

        sender.close();
    }

    /**
     * Code snippet demonstrating how to create an {@link ServiceBusSenderAsyncClient}.
     */
    @Test
    public void instantiateAsync() {
        // BEGIN: com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiation
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusSenderAsyncClient asyncSender = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // Use the sender and finally close it.
        asyncSender.close();
        // END: com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiation
    }

    /**
     * Code snippet demonstrating how to send a batch to Service Bus queue or topic.
     *
     * @throws IllegalArgumentException if an message is too large.
     */
    @Test
    public void sendBatch() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .sender()
            .queueName(queueName)
            .buildClient();

        // BEGIN: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch
        List<ServiceBusMessage> messages = Arrays.asList(
            new ServiceBusMessage("test-1"),
            new ServiceBusMessage("test-2"));

        // Creating a batch without options set.
        ServiceBusMessageBatch batch = sender.createMessageBatch();
        for (ServiceBusMessage message : messages) {
            if (batch.tryAddMessage(message)) {
                continue;
            }

            // The batch is full. Send the current batch and create a new one.
            sender.sendMessages(batch);

            batch = sender.createMessageBatch();

            // Add the message we couldn't before.
            if (!batch.tryAddMessage(message)) {
                throw new IllegalArgumentException("Message is too large for an empty batch.");
            }
        }

        // Send the final batch if there are any messages in it.
        if (batch.getCount() > 0) {
            sender.sendMessages(batch);
        }

        // Finally dispose of the sender.
        sender.close();
        // END: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch
    }

    /**
     * Code snippet demonstrating how to send a batch to Service Bus queue or topic.
     */
    @Test
    public void sendBatchAsync() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusSenderAsyncClient asyncSender = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch
        // `subscribe` is a non-blocking call. The program will move onto the next line of code when it starts the
        // operation.  Users should use the callbacks on `subscribe` to understand the status of the send operation.
        asyncSender.createMessageBatch().flatMap(batch -> {
            batch.tryAddMessage(new ServiceBusMessage("test-1"));
            batch.tryAddMessage(new ServiceBusMessage("test-2"));
            return asyncSender.sendMessages(batch);
        }).subscribe(unused -> {
        }, error -> {
            System.err.println("Error occurred while sending batch:" + error);
        }, () -> {
            System.out.println("Send complete.");
        });
        // END: com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch

        asyncSender.close();
    }

    /**
     * Code snippet demonstrating how to create a size-limited {@link ServiceBusMessageBatch} and send it.
     *
     * @throws IllegalArgumentException if an message is too large for an empty batch.
     */
    @Test
    public void batchSizeLimited() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .sender()
            .queueName(queueName)
            .buildClient();

        ServiceBusMessage firstMessage = new ServiceBusMessage("message-1");
        firstMessage.getApplicationProperties().put("telemetry", "latency");
        ServiceBusMessage secondMessage = new ServiceBusMessage("message-2");
        secondMessage.getApplicationProperties().put("telemetry", "cpu-temperature");
        ServiceBusMessage thirdMessage = new ServiceBusMessage("message-3");
        thirdMessage.getApplicationProperties().put("telemetry", "fps");

        // BEGIN: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch#CreateMessageBatchOptions
        List<ServiceBusMessage> telemetryMessages = Arrays.asList(firstMessage, secondMessage, thirdMessage);

        // Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
        // In this case, all the batches created with these options are limited to 256 bytes.
        CreateMessageBatchOptions options = new CreateMessageBatchOptions()
            .setMaximumSizeInBytes(256);

        ServiceBusMessageBatch currentBatch = sender.createMessageBatch(options);

        // For each telemetry message, we try to add it to the current batch.
        // When the batch is full, send it then create another batch to add more mesages to.
        for (ServiceBusMessage message : telemetryMessages) {
            if (!currentBatch.tryAddMessage(message)) {
                sender.sendMessages(currentBatch);
                currentBatch = sender.createMessageBatch(options);

                // Add the message we couldn't before.
                if (!currentBatch.tryAddMessage(message)) {
                    throw new IllegalArgumentException("Message is too large for an empty batch.");
                }
            }
        }

        // Send the final batch if there are any messages in it.
        if (currentBatch.getCount() > 0) {
            sender.sendMessages(currentBatch);
        }

        // Dispose of the sender
        sender.close();
        // END: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch#CreateMessageBatchOptions
    }

    /**
     * Code snippet demonstrating how to create a size-limited {@link ServiceBusMessageBatch} and send it.
     */
    @Test
    public void batchSizeLimitedAsync() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusSenderAsyncClient asyncSender = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        ServiceBusMessage firstMessage = new ServiceBusMessage(BinaryData.fromBytes("92".getBytes(UTF_8)));
        firstMessage.getApplicationProperties().put("telemetry", "latency");
        ServiceBusMessage secondMessage = new ServiceBusMessage(BinaryData.fromBytes("98".getBytes(UTF_8)));
        secondMessage.getApplicationProperties().put("telemetry", "cpu-temperature");

        // BEGIN: com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch#CreateMessageBatchOptionsLimitedSize
        Flux<ServiceBusMessage> telemetryMessages = Flux.just(firstMessage, secondMessage);

        // Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
        // In this case, all the batches created with these options are limited to 256 bytes.
        CreateMessageBatchOptions options = new CreateMessageBatchOptions()
            .setMaximumSizeInBytes(256);
        AtomicReference<ServiceBusMessageBatch> currentBatch = new AtomicReference<>();

        // Sends the current batch if it is not null and not empty.  If the current batch is null, sets it.
        // Returns the batch to work with.
        Mono<ServiceBusMessageBatch> sendBatchAndGetCurrentBatchOperation = Mono.defer(() -> {
            ServiceBusMessageBatch batch = currentBatch.get();

            if (batch == null) {
                return asyncSender.createMessageBatch(options);
            }

            if (batch.getCount() > 0) {
                return asyncSender.sendMessages(batch).then(
                    asyncSender.createMessageBatch(options)
                        .handle((ServiceBusMessageBatch newBatch, SynchronousSink<ServiceBusMessageBatch> sink) -> {
                            // Expect that the batch we just sent is the current one. If it is not, there's a race
                            // condition accessing currentBatch reference.
                            if (!currentBatch.compareAndSet(batch, newBatch)) {
                                sink.error(new IllegalStateException(
                                    "Expected that the object in currentBatch was batch. But it is not."));
                            } else {
                                sink.next(newBatch);
                            }
                        }));
            } else {
                return Mono.just(batch);
            }
        });

        // The sample Flux contains two messages, but it could be an infinite stream of telemetry messages.
        Flux<Void> sendMessagesOperation = telemetryMessages.flatMap(message -> {
            return sendBatchAndGetCurrentBatchOperation.flatMap(batch -> {
                if (batch.tryAddMessage(message)) {
                    return Mono.empty();
                } else {
                    return sendBatchAndGetCurrentBatchOperation
                        .handle((ServiceBusMessageBatch newBatch, SynchronousSink<Void> sink) -> {
                            if (!newBatch.tryAddMessage(message)) {
                                sink.error(new IllegalArgumentException(
                                    "Message is too large to fit in an empty batch."));
                            } else {
                                sink.complete();
                            }
                        });
                }
            });
        });

        // `subscribe` is a non-blocking call. The program will move onto the next line of code when it starts the
        // operation.  Users should use the callbacks on `subscribe` to understand the status of the send operation.
        Disposable disposable = sendMessagesOperation.then(sendBatchAndGetCurrentBatchOperation)
            .subscribe(batch -> {
                System.out.println("Last batch should be empty: " + batch.getCount());
            }, error -> {
                System.err.println("Error sending telemetry messages: " + error);
            }, () -> {
                System.out.println("Completed.");

                // Clean up sender when done using it.  Publishers should be long-lived objects.
                asyncSender.close();
            });

        // END: com.azure.messaging.servicebus.servicebusasyncsenderclient.createMessageBatch#CreateMessageBatchOptionsLimitedSize
        // Dispose of subscription to cancel operations.
        disposable.dispose();
    }

    /**
     * Create a session message.
     */
    @Test
    public void sendSessionMessage() {
        // BEGIN: com.azure.messaging.servicebus.servicebussenderclient.sendMessage-session
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .sender()
            .queueName(sessionEnabledQueueName)
            .buildClient();

        // Setting sessionId publishes that message to a specific session, in this case, "greeting".
        ServiceBusMessage message = new ServiceBusMessage("Hello world")
            .setSessionId("greetings");

        sender.sendMessage(message);

        // Dispose of the sender.
        sender.close();
        // END: com.azure.messaging.servicebus.servicebussenderclient.sendMessage-session
    }
}
