// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Sample demonstrates how to send and receive messages with {@link AmqpMessageBodyType type} of
 * {@link AmqpMessageBodyType#SEQUENCE SEQUENCE} and {@link AmqpMessageBodyType#VALUE VALUE}.
 * <p>
 * Note that the message of type {@link AmqpMessageBodyType#SEQUENCE SEQUENCE} is limited to send and receive only one
 * AmqpSequence only. And a {@link AmqpMessageBodyType#SEQUENCE SEQUENCE} can have list of Objects.
 *
 * The {@link Object object} can be any of the AMQP supported primitive data type.
 *
 * @see <a href="https://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-types-v1.0-os.html#section-primitive-type-definitions" target="_blank">
 *     Amqp primitive data type.</a>
 */
public class SendReceiveAmqpMessageBodySample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    /**
     * Main method to invoke this demo on how to send and receive an {@link ServiceBusReceivedMessage} from
     * Service Bus with AMQP Sequence message body.
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        SendReceiveAmqpMessageBodySample sample = new SendReceiveAmqpMessageBodySample();
        sample.run();
    }

    /**
     * Invoke this demo on how to send and receive an {@link ServiceBusReceivedMessage} from
     * Service Bus with AMQP Sequence message body
     */
    @Test
    public void run() throws InterruptedException {
        AtomicBoolean sampleSuccessful = new AtomicBoolean(true);
        CountDownLatch countdownLatch = new CountDownLatch(1);

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.

        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "queueName" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.

        // At most, the receiver will automatically renew the message lock until 120 seconds have elapsed.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);

        ServiceBusReceiverAsyncClient receiver = builder
            .receiver()
            .queueName(queueName)
            .maxAutoLockRenewDuration(Duration.ofMinutes(2))
            .buildAsyncClient();

        ServiceBusSenderClient sender = builder
            .sender()
            .queueName(queueName)
            .buildClient();

        // Prepare the data to send as AMQP Sequence
        List<Object> list = new ArrayList<>();
        list.add(10L);
        list.add("The cost of this product is $10.");

        sender.sendMessage(new ServiceBusMessage(AmqpMessageBody.fromSequence(list)));

        // Now receive the data.
        Disposable subscription = receiver.receiveMessages()
            .subscribe(message -> {
                AmqpMessageBody amqpMessageBody = message.getRawAmqpMessage().getBody();
                // You should check the body type of the received message and call appropriate getter on AMQPMessageBody
                switch (amqpMessageBody.getBodyType()) {
                    case SEQUENCE:
                        amqpMessageBody.getSequence().forEach(payload -> {
                            System.out.printf("Sequence #: %s, Body Type: %s, Contents: %s%n", message.getSequenceNumber(),
                                amqpMessageBody.getBodyType(), payload);
                        });
                        break;
                    case VALUE:
                        System.out.printf("Sequence #: %s, Body Type: %s, Contents: %s%n", message.getSequenceNumber(),
                            amqpMessageBody.getBodyType(), amqpMessageBody.getValue());
                        break;
                    case DATA:
                        System.out.printf("Sequence #: %s. Contents: %s%n", message.getSequenceNumber(),
                                message.getBody().toString());
                        break;
                    default:
                        System.out.println("Invalid message body type: " + amqpMessageBody.getBodyType());
                }
            },
                error -> {
                    System.err.println("Error occurred while receiving message: " + error);
                    sampleSuccessful.set(false);
                });

        // Subscribe is not a blocking call so we wait here so the program does not end.
        countdownLatch.await(30, TimeUnit.SECONDS);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();

        // This assertion is to ensure that samples are working. Users should remove this.
        Assertions.assertTrue(sampleSuccessful.get());
    }
}
