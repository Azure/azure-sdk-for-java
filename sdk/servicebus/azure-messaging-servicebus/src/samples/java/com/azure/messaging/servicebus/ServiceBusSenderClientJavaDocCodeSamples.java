// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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

            batch.tryAddMessage(message);

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
}
