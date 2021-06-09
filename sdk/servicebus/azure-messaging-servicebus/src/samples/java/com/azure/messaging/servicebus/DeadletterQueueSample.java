// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to dead letter within a Service Bus Queue.
 */
public class DeadletterQueueSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");

    static final Gson GSON = new Gson();

    /**
     * Main method to show how to dead letter within a Service Bus Queue.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        DeadletterQueueSample sample = new DeadletterQueueSample();
        sample.run();
    }

    /**
     * Run method to invoke this demo on how to dead letter within a Service Bus Queue.
     *
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    @Test
    public void run() throws InterruptedException {
        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        // max delivery-count scenario
        sendMessagesAsync(senderAsyncClient, 1);
        exceedMaxDelivery(connectionString, queueName);

        // fix-up scenario
        sendMessagesAsync(senderAsyncClient, Integer.MAX_VALUE);
        this.receiveMessagesAsync(connectionString, queueName);
        this.PickUpAndFixDeadletters(connectionString, queueName, senderAsyncClient);

        senderAsyncClient.close();
    }

    /**
     * Send {@link ServiceBusMessage messages} to an Azure Service Bus Queue.
     *
     * @Param senderAsyncClient Service Bus Sender Async Client
     * @Param maxMessages Maximum Number Of Messages
     */
    void sendMessagesAsync(ServiceBusSenderAsyncClient senderAsyncClient, int maxMessages) {
        List<HashMap<String, String>> data =
            GSON.fromJson(
                "[" +
                    "{'name' = 'Einstein', 'firstName' = 'Albert'}," +
                    "{'name' = 'Heisenberg', 'firstName' = 'Werner'}," +
                    "{'name' = 'Curie', 'firstName' = 'Marie'}," +
                    "{'name' = 'Hawking', 'firstName' = 'Steven'}," +
                    "{'name' = 'Newton', 'firstName' = 'Isaac'}," +
                    "{'name' = 'Bohr', 'firstName' = 'Niels'}," +
                    "{'name' = 'Faraday', 'firstName' = 'Michael'}," +
                    "{'name' = 'Galilei', 'firstName' = 'Galileo'}," +
                    "{'name' = 'Kepler', 'firstName' = 'Johannes'}," +
                    "{'name' = 'Kopernikus', 'firstName' = 'Nikolaus'}" +
                    "]",
                new TypeToken<List<HashMap<String, String>>>() {
                }.getType());

        for (int i = 0; i < Math.min(data.size(), maxMessages); i++) {
            final String messageId = Integer.toString(i);
            ServiceBusMessage message = new ServiceBusMessage(GSON.toJson(data.get(i), Map.class).getBytes(UTF_8));
            message.setContentType("application/json");
            message.setSubject(i % 2 == 0 ? "Scientist" : "Physicist");
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(2));
            System.out.printf("Message sending: Id = %s\n", message.getMessageId());
            senderAsyncClient.sendMessage(message)
                .doOnSuccess(onSuccess -> System.out.printf("\tMessage acknowledged: Id = %s\n", message.getMessageId()))
                .block();
        }
    }

    /**
     * Receive {@link ServiceBusMessage messages} and dead letter within a Service Bus Queue
     *
     * @Param connectionString Service Bus Connection String
     * @Param queueName Queue Name
     * @throws InterruptedException If the program is unable to sleep while waiting for the receive to complete.
     */
    void exceedMaxDelivery(String connectionString, String queueName) throws InterruptedException {
        ServiceBusReceiverAsyncClient receiverAsyncClient
            = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .buildAsyncClient();
        receiverAsyncClient.receiveMessages().subscribe(receiveMessage -> {
            System.out.printf("Picked up message; DeliveryCount %d\n", receiveMessage.getDeliveryCount());
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
     * Receive {@link ServiceBusMessage messages} and dead letter within a Service Bus Queue
     *
     * @Param connectionString Service Bus Connection String
     * @Param queueName Queue Name
     */
    void receiveMessagesAsync(String connectionString, String queueName) {
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
                Map scientist = GSON.fromJson(new String(body, UTF_8), Map.class);
                System.out.printf(
                    "\n\t\t\t\tMessage received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                        "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ firstName = %s, name = %s ]\n",
                    receiveMessage.getMessageId(),
                    receiveMessage.getSequenceNumber(),
                    receiveMessage.getEnqueuedTime(),
                    receiveMessage.getExpiresAt(),
                    receiveMessage.getContentType(),
                    scientist != null ? scientist.get("firstName") : "",
                    scientist != null ? scientist.get("name") : "");
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
     * @Param resubmitSender Service Bus Send Async Client
     */
    void PickUpAndFixDeadletters(String connectionString, String queueName, ServiceBusSenderAsyncClient resubmitSender) {
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
}
