// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.core.DefaultMessageHandler;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
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
 * @author Warren Zhu
 */
@RestController
public class TopicSendController {
    private static final Logger log = LoggerFactory.getLogger(TopicSendController.class);
    private static final String OUTPUT_CHANNEL = "topic.output";
    private static final String TOPIC_NAME = "topic1";

    @Autowired
    TopicOutboundGateway messagingGateway;

    /**
     * Posts a message to a Service Bus Topic
     */
    @PostMapping("/topics")
    public String send(@RequestParam("message") String message) {
        this.messagingGateway.send(message);
        return message;
    }

    @Bean
    @ServiceActivator(inputChannel = OUTPUT_CHANNEL)
    public MessageHandler topicMessageSender(ServiceBusTopicOperation topicOperation) {
        DefaultMessageHandler handler = new DefaultMessageHandler(TOPIC_NAME, topicOperation);
        handler.setSendCallback(new ListenableFutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                log.info("Message was sent successfully.");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.info("There was an error sending the message.");
            }
        });

        return handler;
    }

    /**
     * Message gateway binding with {@link MessageHandler}
     * via {@link MessageChannel} has name {@value OUTPUT_CHANNEL}
     */
    @MessagingGateway(defaultRequestChannel = OUTPUT_CHANNEL)
    public interface TopicOutboundGateway {
        void send(String text);
    }
}
