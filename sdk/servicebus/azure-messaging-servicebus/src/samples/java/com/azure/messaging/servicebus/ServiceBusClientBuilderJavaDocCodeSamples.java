// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ServiceBusClientBuilderJavaDocCodeSamples {
    private final String fullyQualifiedNamespace = System.getenv("AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME");
    private final String queueName = System.getenv("AZURE_SERVICEBUS_SAMPLE_QUEUE_NAME");
    private final String topicName = System.getenv("AZURE_SERVICEBUS_SAMPLE_TOPIC_NAME");
    private final String subscriptionName = System.getenv("AZURE_SERVICEBUS_SAMPLE_SUBSCRIPTION_NAME");

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


}
