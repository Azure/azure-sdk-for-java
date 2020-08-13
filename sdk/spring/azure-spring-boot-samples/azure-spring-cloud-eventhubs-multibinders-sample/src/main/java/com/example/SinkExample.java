// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.reactor.Checkpointer;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;

@EnableBinding(CustomProcessor.class)
public class SinkExample {

    @StreamListener(CustomProcessor.INPUT)
    public void handleMessage(String message, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
        System.out.println(String.format("[1] New message received: '%s'", message));
        checkpointer.success()
            .doOnSuccess(s -> System.out.println(String.format("[1] Message '%s' successfully checkpointed",
                message)))
            .doOnError(System.out::println)
            .subscribe();
    }

    @StreamListener(CustomProcessor.INPUT1)
    public void handleMessage1(String message, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
        System.out.println(String.format("[2] New message received: '%s'", message));
        checkpointer.success()
            .doOnSuccess(s -> System.out.println(String.format("[2] Message '%s' successfully checkpointed",
                message)))
            .doOnError(System.out::println)
            .subscribe();
    }

    // Replace destination with spring.cloud.stream.bindings.input.destination
    // Replace group with spring.cloud.stream.bindings.input.group
    @ServiceActivator(inputChannel = "{destination}.{group}.errors")
    public void consumerError(Message<?> message) {
        System.out.println("Handling customer ERROR: " + message);
    }


    // Replace destination with spring.cloud.stream.bindings.output.destination
    @ServiceActivator(inputChannel = "{destination}.errors")
    public void producerError(Message<?> message) {
        System.out.println("Handling Producer ERROR: " + message);
    }

}
