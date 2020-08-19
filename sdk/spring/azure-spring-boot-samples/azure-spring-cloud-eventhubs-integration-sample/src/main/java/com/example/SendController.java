// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.core.api.reactor.DefaultMessageHandler;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
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
 * @author Warren Zhu
 */
@RestController
public class SendController {

    private static final String OUTPUT_CHANNEL = "output";
    private static final String EVENTHUB_NAME = "eventhub1";

    @Autowired
    EventHubOutboundGateway messagingGateway;

    /**
     * Posts a message to a Azure Event Hub
     */
    @PostMapping("/messages")
    public String send(@RequestParam("message") String message) {
        this.messagingGateway.send(message);
        return message;
    }

    @Bean
    @ServiceActivator(inputChannel = OUTPUT_CHANNEL)
    public MessageHandler messageSender(EventHubOperation queueOperation) {
        DefaultMessageHandler handler = new DefaultMessageHandler(EVENTHUB_NAME, queueOperation);
        handler.setSendCallback(new ListenableFutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                System.out.println("Message was sent successfully.");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.out.println("There was an error sending the message.");
            }
        });

        return handler;
    }

    /**
     * Message gateway binding with {@link MessageHandler}
     * via {@link MessageChannel} has name {@value OUTPUT_CHANNEL}
     */
    @MessagingGateway(defaultRequestChannel = OUTPUT_CHANNEL)
    public interface EventHubOutboundGateway {
        void send(String text);
    }
}
