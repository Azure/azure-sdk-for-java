// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.operation;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class QueueController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueController.class);
    private static final String QUEUE_NAME = "queue1";

    @Autowired
    ServiceBusQueueOperation queueOperation;

    @PostMapping("/queues")
    public String send(@RequestParam("message") String message) {
        this.queueOperation.sendAsync(QUEUE_NAME, MessageBuilder.withPayload(message).build());
        return message;
    }

    @PostConstruct
    public void subscribe() {
        this.queueOperation.setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL)
            .build());
        this.queueOperation.subscribe(QUEUE_NAME, this::messageReceiver, String.class);
    }

    private void messageReceiver(Message<?> message) {
        LOGGER.info("New message received: '{}'", message.getPayload());
        Checkpointer checkpointer = message.getHeaders().get(AzureHeaders.CHECKPOINTER, Checkpointer.class);
        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                LOGGER.info("Message '{}' successfully checkpointed", message.getPayload());
            }
            return null;
        });
    }
}
