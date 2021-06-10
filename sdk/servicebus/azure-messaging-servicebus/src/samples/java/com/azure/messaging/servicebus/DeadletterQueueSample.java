// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to dead letter within an Azure Service Bus Queue.
 */
public class DeadletterQueueSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    /**
     * Main method to show how to dead letter within an Azure Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        DeadletterQueueSample sample = new DeadletterQueueSample();
        sample.run();
    }

    /**
     * Run method to invoke this demo on how to dead letter within an Azure Service Bus Queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    @Test
    public void run() throws InterruptedException {
        ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildClient();

        // max delivery-count scenario
        sendMessagesAsync(senderClient, 1);
        deadLetterByExceedingMaxDelivery(connectionString, queueName);

        // fix-up scenario
        sendMessagesAsync(senderClient, Integer.MAX_VALUE);
        this.receiveAndDeadletterMessagesAsync(connectionString, queueName);
        this.pickUpAndFixDeadletters(connectionString, queueName, senderClient);

        senderClient.close();
    }

    /**
     * Send {@link ServiceBusMessage messages} to an Azure Service Bus Queue.
     *
     * @Param senderAsyncClient Service Bus Sender Client
     * @Param maxMessages Maximum Number Of Messages
     */
    void sendMessagesAsync(ServiceBusSenderClient senderAsyncClient, int maxMessages) {
        List<ServiceBusMessage> messageList = new ArrayList<ServiceBusMessage>(){{
            add(createServiceBusMessage("{\"name\" : \"Einstein\", \"firstName\" : \"Albert\"}"));
            add(createServiceBusMessage("{\"name\" : \"Heisenberg\", \"firstName\" : \"Werner\"}"));
            add(createServiceBusMessage("{\"name\" : \"Curie\", \"firstName\" : \"Marie\"}"));
            add(createServiceBusMessage("{\"name\" : \"Hawking\", \"firstName\" : \"Steven\"}"));
            add(createServiceBusMessage("{\"name\" : \"Newton\", \"firstName\" : \"Isaac\"}"));
            add(createServiceBusMessage("{\"name\" : \"Bohr\", \"firstName\" : \"Niels\"}"));
            add(createServiceBusMessage("{\"name\" : \"Faraday\", \"firstName\" : \"Michael\"}"));
            add(createServiceBusMessage("{\"name\" : \"Galilei\", \"firstName\" : \"Galileo\"}"));
            add(createServiceBusMessage("{\"name\" : \"Kepler\", \"firstName\" : \"Johannes\"}"));
            add(createServiceBusMessage("{\"name\" : \"Kopernikus\", \"firstName\" : \"Nikolaus\"}"));
        }};
        for (int i = 0; i < Math.min(messageList.size(), maxMessages); i++) {
            final String messageId = Integer.toString(i);
            ServiceBusMessage message = messageList.get(i);
            message.setContentType("application/json");
            message.setSubject(i % 2 == 0 ? "Scientist" : "Physicist");
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(2));
            System.out.printf("\tMessage sending: Id = %s\n", message.getMessageId());
            senderAsyncClient.sendMessage(message);
            System.out.printf("\tMessage acknowledged: Id = %s\n", message.getMessageId());
        }
    }

    /**
     * Receive {@link ServiceBusMessage messages} and return {@link ServiceBusMessage messages} back to the queue.
     * When the time to life of the {@link ServiceBusMessage messages} expires,
     * the {@link ServiceBusMessage messages} will be dumped as dead letters into the dead letter queue.
     * We can receive these {@link ServiceBusMessage messages} from the dead letter queue.
     *
     * @Param connectionString Service Bus Connection String
     * @Param queueName Queue Name
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    void deadLetterByExceedingMaxDelivery(String connectionString, String queueName) throws InterruptedException {
        ServiceBusReceiverAsyncClient receiverAsyncClient
            = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .buildAsyncClient();
        receiverAsyncClient.receiveMessages().subscribe(receiveMessage -> {
            System.out.printf("Picked up message; DeliveryCount %d\n", receiveMessage.getDeliveryCount());
            // return message back to the queue
            receiverAsyncClient.abandon(receiveMessage);
        });
        Thread.sleep(10000);
        receiverAsyncClient.close();

        Thread.sleep(120000);

        ServiceBusReceiverAsyncClient deadletterReceiverAsyncClient
            = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName.concat("/$deadletterqueue"))
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .buildAsyncClient();
        deadletterReceiverAsyncClient.receiveMessages().subscribe(receiveMessage -> {
            System.out.printf("\nDeadletter message:\n");
            receiveMessage.getApplicationProperties().keySet().forEach(key -> System.out.printf("\t%s=%s\n", key, receiveMessage.getApplicationProperties().get(key)));
            deadletterReceiverAsyncClient.complete(receiveMessage);
        });
        Thread.sleep(10000);
        deadletterReceiverAsyncClient.close();
    }

    /**
     * Receive {@link ServiceBusMessage messages} and transfer to the dead letter queue as a dead letter.
     *
     * @Param connectionString Service Bus Connection String
     * @Param queueName Queue Name
     */
    void receiveAndDeadletterMessagesAsync(String connectionString, String queueName) {
        ServiceBusReceiverAsyncClient receiverAsyncClient
            = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .queueName(queueName)
            .buildAsyncClient();

        receiverAsyncClient.receiveMessages().subscribe(receiveMessage -> {
            if (receiveMessage.getSubject() != null &&
                receiveMessage.getContentType() != null &&
                receiveMessage.getSubject().contentEquals("Scientist") &&
                receiveMessage.getContentType().contentEquals("application/json")) {
                byte[] body = receiveMessage.getBody().toBytes();
                JSONObject jsonObject = null;
                try {
                    jsonObject = JSONObjectUtils.parse(new String(body, UTF_8));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                System.out.printf(
                    "\n\t\t\t\tMessage received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                        "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ firstName = %s, name = %s ]\n",
                    receiveMessage.getMessageId(),
                    receiveMessage.getSequenceNumber(),
                    receiveMessage.getEnqueuedTime(),
                    receiveMessage.getExpiresAt(),
                    receiveMessage.getContentType(),
                    jsonObject != null ? jsonObject.get("firstName") : "",
                    jsonObject != null ? jsonObject.get("name") : "");
            } else {
                receiverAsyncClient.deadLetter(receiveMessage);
            }
            receiverAsyncClient.complete(receiveMessage);
        });
    }

    /**
     * Receive dead letter {@link ServiceBusMessage messages} and resend its.
     *
     * @Param connectionString Service Bus Connection String
     * @Param queueName Queue Name
     * @Param resubmitSender Service Bus Send Client
     */
    void pickUpAndFixDeadletters(String connectionString, String queueName, ServiceBusSenderClient resubmitSender) {
        ServiceBusReceiverAsyncClient receiverAsyncClient
            = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .queueName(queueName.concat("/$deadletterqueue"))
            .buildAsyncClient();

        receiverAsyncClient.receiveMessages().subscribe(receiveMessage -> {
            if (receiveMessage.getSubject() != null && receiveMessage.getSubject().contentEquals("Physicist")) {
                ServiceBusMessage resubmitMessage = new ServiceBusMessage(receiveMessage.getBody());
                System.out.printf(
                    "\n\t\tFixing: \n\t\t\tMessageId = %s, \n\t\t\tSequenceNumber = %s, \n\t\t\tLabel = %s\n",
                    receiveMessage.getMessageId(),
                    receiveMessage.getSequenceNumber(),
                    receiveMessage.getSubject());
                resubmitMessage.setMessageId(receiveMessage.getMessageId());
                resubmitMessage.setSubject("Scientist");
                resubmitMessage.setContentType(receiveMessage.getContentType());
                resubmitMessage.setTimeToLive(Duration.ofMinutes(2));
                resubmitSender.sendMessage(resubmitMessage);
            }
            receiverAsyncClient.complete(receiveMessage);
        });
    }

    /**
     * Create a {@link ServiceBusMessage} for add to a {@link ServiceBusMessageBatch}.
     */
    static ServiceBusMessage createServiceBusMessage(String label) {
        ServiceBusMessage message = new ServiceBusMessage(label.getBytes(UTF_8));
        return message;
    }
}
