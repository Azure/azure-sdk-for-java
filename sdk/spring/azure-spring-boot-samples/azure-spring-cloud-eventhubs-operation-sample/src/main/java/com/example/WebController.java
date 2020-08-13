// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.reactor.Checkpointer;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@RestController
public class WebController {

    private static final String EVENT_HUB_NAME = "eventhub1";
    private static final String CONSUMER_GROUP = "cg1";

    @Autowired
    EventHubOperation eventHubOperation;

    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.eventHubOperation.sendAsync(EVENT_HUB_NAME, MessageBuilder.withPayload(message).build()).block();
        return message;
    }

    @PostConstruct
    public void subscribeToEventHub() {
        this.eventHubOperation
            .setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());
        this.eventHubOperation.subscribe(EVENT_HUB_NAME, CONSUMER_GROUP, this::messageReceiver, String.class);
    }

    private void messageReceiver(Message<?> message) {
        System.out.println(String.format("New message received: '%s'", message.getPayload()));
        Checkpointer checkpointer = message.getHeaders().get(AzureHeaders.CHECKPOINTER, Checkpointer.class);
        checkpointer.success()
            .doOnSuccess(s -> System.out.println(String.format("Message '%s' successfully checkpointed",
                message.getPayload())))
            .doOnError(System.out::println)
            .subscribe();
    }
}
