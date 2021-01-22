// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link ServiceBusSenderClient}.
 */
public class ServiceBusSenderClientJavaDocCodeSamples {

    // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
    // We are reading 'connectionString/queueName' from environment variable.
    // You can configure them as it fits suitable for your application.
    // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
    // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
    //    inside the Service Bus namespace.
    private String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    private String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
    private ServiceBusSenderClient sender = new ServiceBusClientBuilder()
        .connectionString(connectionString)
        .sender()
        .queueName(queueName)
        .buildClient();

    /**
     * Code snippet demonstrating how to create an {@link ServiceBusSenderClient}.
     */
    @Test
    public void instantiate() {
        // BEGIN: com.azure.messaging.servicebus.servicebussenderclient.instantiation
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();
        // END: com.azure.messaging.servicebus.servicebussenderclient.instantiation

        sender.close();
    }

    /**
     * Code snippet demonstrating how to send a batch to Service Bus queue or topic.
     *
     * @throws IllegalArgumentException if an message is too large.
     */
    @Test
    public void sendBatch() {

        // BEGIN: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch
        List<ServiceBusMessage> messages = Arrays.asList(new ServiceBusMessage(BinaryData.fromBytes("test-1".getBytes(UTF_8))),
            new ServiceBusMessage(BinaryData.fromBytes("test-2".getBytes(UTF_8))));

        CreateMessageBatchOptions options = new CreateMessageBatchOptions().setMaximumSizeInBytes(10 * 1024);

        // Creating a batch without options set.
        ServiceBusMessageBatch batch = sender.createMessageBatch(options);
        for (ServiceBusMessage message : messages) {
            if (batch.tryAddMessage(message)) {
                continue;
            }

            sender.sendMessages(batch);
        }
        // END: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch

        sender.close();
    }

    /**
     * Code snippet demonstrating how to create a size-limited {@link ServiceBusMessageBatch} and send it.
     *
     * @throws IllegalArgumentException if an message is too large for an empty batch.
     */
    @Test
    public void batchSizeLimited() {

        ServiceBusMessage firstMessage = new ServiceBusMessage(BinaryData.fromBytes("message-1".getBytes(UTF_8)));
        firstMessage.getApplicationProperties().put("telemetry", "latency");
        ServiceBusMessage secondMessage = new ServiceBusMessage(BinaryData.fromBytes("message-2".getBytes(UTF_8)));
        secondMessage.getApplicationProperties().put("telemetry", "cpu-temperature");
        ServiceBusMessage thirdMessage = new ServiceBusMessage(BinaryData.fromBytes("message-3".getBytes(UTF_8)));
        thirdMessage.getApplicationProperties().put("telemetry", "fps");

        // BEGIN: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch#CreateMessageBatchOptions-int
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
        // END: com.azure.messaging.servicebus.servicebussenderclient.createMessageBatch#CreateMessageBatchOptions-int
    }
}
