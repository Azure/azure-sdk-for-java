package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;

import java.time.Duration;

public class ServiceBusClientBuilderJavaDocCodeSamples {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
    String topicName = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");
    String subscriptionName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SUBSCRIPTION_NAME");

    @Test
    public void instantiateReceiverSync() {
        // BEGIN: com.azure.messaging.servicebus.receiver.sync.client.instantiation
        // Retrieve 'connectionString' and queueName from your configuration.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);
        ServiceBusReceiverClient receiver = builder
            .receiver()
            .maxAutoLockRenewDuration(Duration.ofMinutes(1))
            .queueName(queueName)
            .buildClient();
        // END: com.azure.messaging.servicebus.receiver.sync.client.instantiation
        receiver.receiveMessages(1);
    }

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
    public void instantiateSenderAsync() {
        // BEGIN: com.azure.messaging.servicebus.sender.async.client.instantiation
        // Retrieve 'connectionString' and 'topicName' from your configuration.
        ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString);
        ServiceBusSenderAsyncClient sender = builder
            .sender()
            .topicName(topicName)
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.sender.async.client.instantiation
        sender.sendMessage(new ServiceBusMessage("payload")).subscribe();;
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
