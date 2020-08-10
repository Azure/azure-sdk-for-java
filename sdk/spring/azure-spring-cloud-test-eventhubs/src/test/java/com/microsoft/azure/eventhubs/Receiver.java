// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.reactor.Checkpointer;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;

import java.util.concurrent.Exchanger;

@EnableBinding(Sink.class)
public class Receiver {

    public static final Exchanger<String> EXCHANGER = new Exchanger<>();

    @StreamListener(Sink.INPUT)
    public void handleMessage(String message, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) throws InterruptedException {
        checkpointer.success();
        EXCHANGER.exchange(message);
        throw new RuntimeException();
    }

    @ServiceActivator(inputChannel = "test.$Default.errors")
    public void consumerError(Message<?> message) throws InterruptedException {
        EXCHANGER.exchange("ERROR!");
    }

}
