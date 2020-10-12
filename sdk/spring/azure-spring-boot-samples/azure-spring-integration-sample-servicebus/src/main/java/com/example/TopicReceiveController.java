// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointConfig;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import com.microsoft.azure.spring.integration.servicebus.inbound.ServiceBusTopicInboundChannelAdapter;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Warren Zhu
 */
@RestController
public class TopicReceiveController {

    private static final String INPUT_CHANNEL = "topic.input";
    private static final String TOPIC_NAME = "topic1";
    private static final String SUBSCRIPTION_NAME = "group1";

    /**
     * This message receiver binding with {@link ServiceBusTopicInboundChannelAdapter}
     * via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
        String message = new String(payload);
        System.out.println(String.format("New message received: '%s'", message));
        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                System.out.println(String.format("Message '%s' successfully checkpointed", message));
            }
            return null;
        });
    }

    @Bean
    public ServiceBusTopicInboundChannelAdapter topicMessageChannelAdapter(
        @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, ServiceBusTopicOperation topicOperation) {
        topicOperation.setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());
        ServiceBusTopicInboundChannelAdapter adapter = new ServiceBusTopicInboundChannelAdapter(TOPIC_NAME,
            topicOperation, SUBSCRIPTION_NAME);
        adapter.setOutputChannel(inputChannel);
        return adapter;
    }

    @Bean(name = INPUT_CHANNEL)
    public MessageChannel input() {
        return new DirectChannel();
    }

}
