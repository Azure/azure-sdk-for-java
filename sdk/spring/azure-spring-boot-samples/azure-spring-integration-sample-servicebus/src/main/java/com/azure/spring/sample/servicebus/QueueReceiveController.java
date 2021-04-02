// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class QueueReceiveController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueReceiveController.class);
    private static final String INPUT_CHANNEL = "queue.input";
    private static final String QUEUE_NAME = "queue1";

    /**
     * This message receiver binding with {@link ServiceBusQueueInboundChannelAdapter}
     * via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
        String message = new String(payload);
        LOGGER.info("New message received: '{}'", message);
        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                LOGGER.info("Message '{}' successfully checkpointed.", message);
            }
            return null;
        });
    }

    @Bean
    public ServiceBusQueueInboundChannelAdapter queueMessageChannelAdapter(
        @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, ServiceBusQueueOperation queueOperation) {
        queueOperation.setCheckpointConfig(CheckpointConfig.builder().checkpointMode(CheckpointMode.MANUAL).build());
        ServiceBusQueueInboundChannelAdapter adapter = new ServiceBusQueueInboundChannelAdapter(QUEUE_NAME,
            queueOperation);
        adapter.setOutputChannel(inputChannel);
        return adapter;
    }

    @Bean(name = INPUT_CHANNEL)
    public MessageChannel input() {
        return new DirectChannel();
    }
}
