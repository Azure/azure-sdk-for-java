// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@SpringBootApplication
public class ServiceBusSampleApplication implements CommandLineRunner {

    @Autowired
    private ServiceBusSenderAsyncClient queueSender;

    @Autowired
    private ServiceBusReceiverAsyncClient queueReceiver;

    @Autowired
    private ServiceBusSenderAsyncClient topicSender;

    @Autowired
    private ServiceBusReceiverAsyncClient topicSubscriber;

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusSampleApplication.class);
    }

    public void run(String... var1) throws InterruptedException {
        sendQueueMessage();
        receiveQueueMessage();

        sendTopicMessage();
        receiveSubscriptionMessage();
    }

    // NOTE: Please be noted that below are the minimum code for demonstrating the usage of autowired clients.
    // For complete documentation of Service Bus, reference https://azure.microsoft.com/services/service-bus/
    private void sendQueueMessage() throws InterruptedException {
        final String messageBody = "queue message";

        queueSender.sendMessage(new ServiceBusMessage(BinaryData.fromBytes(messageBody.getBytes(UTF_8)))).subscribe(
            v -> System.out.println("Sent message: " + messageBody),
            e -> System.err.println("Error occurred while sending message: " + e),
            () -> System.out.println("Send message to queue complete.")
        );

        TimeUnit.SECONDS.sleep(5);

        queueSender.close();
    }

    private void receiveQueueMessage() throws InterruptedException {
        queueReceiver.receiveMessages().subscribe(message ->
            System.out.println("Received Message: " + message.getBody().toString()));

        TimeUnit.SECONDS.sleep(5);

        queueReceiver.close();
    }

    private void sendTopicMessage() throws InterruptedException {
        final String messageBody = "topic message";

        topicSender.sendMessage(new ServiceBusMessage(BinaryData.fromBytes(messageBody.getBytes(UTF_8)))).subscribe(
            v -> System.out.println("Sent message: " + messageBody),
            e -> System.err.println("Error occurred while sending message: " + e),
            () -> System.out.println("Send message to topic complete.")
        );

        TimeUnit.SECONDS.sleep(10);

        topicSender.close();
    }

    private void receiveSubscriptionMessage() throws InterruptedException {
        topicSubscriber.receiveMessages().subscribe(message ->
            System.out.println("Received Message: " + message.getBody().toString()));

        TimeUnit.SECONDS.sleep(10);

        topicSubscriber.close();
    }

}
