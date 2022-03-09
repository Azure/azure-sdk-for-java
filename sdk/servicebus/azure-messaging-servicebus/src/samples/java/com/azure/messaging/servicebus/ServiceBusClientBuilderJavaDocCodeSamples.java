// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ServiceBusClientBuilderJavaDocCodeSamples {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
    String topicName = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");
    String subscriptionName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SUBSCRIPTION_NAME");

    @Test
    public void instantiateReceiverAsync() {
        // BEGIN: com.azure.messaging.servicebus.receiver.async.client.instantiation
        // Retrieve 'connectionString', 'topicName' and 'subscriptionName' from your configuration.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);
        ServiceBusReceiverAsyncClient receiver = builder
            .receiver()
            .disableAutoComplete() // Allows user to take control of settling a message.
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.receiver.async.client.instantiation
        receiver.receiveMessages().blockFirst(Duration.ofSeconds(1));
    }

    public void instantiateSessionReceiver() {
        // BEGIN: com.azure.messaging.servicebus.session.receiver.async.client.instantiation
        // Retrieve 'connectionString', 'topicName' and 'subscriptionName' from your configuration.
        ServiceBusSessionReceiverAsyncClient sessionReceiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sessionReceiver()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        // Receiving messages from the first available sessions. It waits up to the AmqpRetryOptions.getTryTimeout().
        // If no session is available within that operation timeout, it completes with an error. Otherwise, a receiver
        // is returned when a lock on the session is acquired.
        Mono<ServiceBusReceiverAsyncClient> receiverMono = sessionReceiver.acceptNextSession();

        Flux.usingWhen(receiverMono,
            receiver -> receiver.receiveMessages(),
            receiver -> Mono.fromRunnable(receiver::close))
            .subscribe(message -> System.out.println(message.getBody().toString()));
        // END: com.azure.messaging.servicebus.session.receiver.async.client.instantiation
    }

    @Test
    public void instantiateSenderSync() {
        // BEGIN: com.azure.messaging.servicebus.sender.sync.client.instantiation
        // Retrieve 'connectionString' and 'queueName' from your configuration.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);
        ServiceBusSenderClient sender = builder
            .sender()
            .queueName(queueName)
            .buildClient();
        // END: com.azure.messaging.servicebus.sender.sync.client.instantiation
        sender.sendMessage(new ServiceBusMessage("payload"));
    }

    @Test
    public void instantiateProcessor() {
        // BEGIN: com.azure.messaging.servicebus.processor.client.instantiation
        // Retrieve 'connectionString' and 'queueName' from your configuration.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);
        ServiceBusProcessorClient processor = builder
            .processor()
            .queueName(queueName)
            .processMessage(System.out::println)
            .processError(context -> System.err.println(context.getErrorSource()))
            .buildProcessorClient();
        // END: com.azure.messaging.servicebus.processor.client.instantiation
        processor.start();
        processor.stop();
    }

    @Test
    public void connectionSharingAcrossClients() {
        // BEGIN: com.azure.messaging.servicebus.connection.sharing
        // Retrieve 'connectionString' and 'queueName' from your configuration.
        // Create shared builder.
        ServiceBusClientBuilder sharedConnectionBuilder = new ServiceBusClientBuilder()
            .connectionString(connectionString);
        // Create receiver and sender which will share the connection.
        ServiceBusReceiverClient receiver = sharedConnectionBuilder
            .receiver()
            .queueName(queueName)
            .buildClient();
        ServiceBusSenderClient sender = sharedConnectionBuilder
            .sender()
            .queueName(queueName)
            .buildClient();
        // END: com.azure.messaging.servicebus.connection.sharing

        sender.sendMessage(new ServiceBusMessage("payload"));
        receiver.receiveMessages(1);
    }

}
