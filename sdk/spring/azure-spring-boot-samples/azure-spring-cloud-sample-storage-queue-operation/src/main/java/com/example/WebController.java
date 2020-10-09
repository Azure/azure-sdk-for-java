// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.reactor.Checkpointer;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Miao Cao
 */
@RestController
public class WebController {
    private static final Logger log = LoggerFactory.getLogger(WebController.class);
    private static final String STORAGE_QUEUE_NAME = "example";

    @Autowired
    StorageQueueOperation storageQueueOperation;

    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.storageQueueOperation.sendAsync(STORAGE_QUEUE_NAME, MessageBuilder.withPayload(message).build())
            .subscribe();
        return message;
    }

    @GetMapping("/messages")
    public String receive() {
        this.storageQueueOperation.setMessagePayloadType(String.class);
        this.storageQueueOperation.setCheckpointMode(CheckpointMode.MANUAL);
        Message<?> message = this.storageQueueOperation.receiveAsync(STORAGE_QUEUE_NAME).block();
        if (message == null) {
            log.info("You have no new messages.");
            return null;
        }
        log.info("Message arrived! Payload: " + message.getPayload());

        Checkpointer checkpointer = message.getHeaders().get(AzureHeaders.CHECKPOINTER, Checkpointer.class);
        checkpointer.success()
            .doOnError(e -> log.info("Message '{}' successfully checkpointed", message.getPayload()))
            .subscribe();

        return (String) message.getPayload();
    }
}
