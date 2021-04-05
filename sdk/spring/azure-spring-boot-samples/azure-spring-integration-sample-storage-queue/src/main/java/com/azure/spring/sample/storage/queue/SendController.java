// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.storage.queue;

import com.azure.spring.integration.core.api.reactor.DefaultMessageHandler;
import com.azure.spring.integration.storage.queue.StorageQueueOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Miao Cao
 */
@RestController
public class SendController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendController.class);
    /*Storage queue name can only be made up of lowercase letters, the numbers and the hyphen(-).*/
    private static final String STORAGE_QUEUE_NAME = "example";
    private static final String OUTPUT_CHANNEL = "output";

    @Autowired
    StorageQueueOutboundGateway storageQueueOutboundGateway;

    /**
     * Posts a message to a Azure Storage Queue
     */
    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        storageQueueOutboundGateway.send(message);
        return message;
    }

    @Bean
    @ServiceActivator(inputChannel = OUTPUT_CHANNEL)
    public MessageHandler messageSender(StorageQueueOperation storageQueueOperation) {
        DefaultMessageHandler handler = new DefaultMessageHandler(STORAGE_QUEUE_NAME, storageQueueOperation);
        handler.setSendCallback(new ListenableFutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOGGER.info("Message was sent successfully.");
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.info("There was an error sending the message.");
            }
        });
        return handler;
    }

    /**
     * Message gateway binding with {@link MessageHandler}
     * via {@link MessageChannel} has name {@value OUTPUT_CHANNEL}
     */
    @MessagingGateway(defaultRequestChannel = OUTPUT_CHANNEL)
    public interface StorageQueueOutboundGateway {
        void send(String text);
    }
}
