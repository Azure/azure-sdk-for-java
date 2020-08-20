// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import com.microsoft.azure.spring.integration.core.api.reactor.Checkpointer;
import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import com.microsoft.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Miao Cao
 */
@RestController
public class ReceiveController {
    /*Storage queue name can only be made up of lowercase letters, the numbers and the hyphen(-).*/
    private static final String STORAGE_QUEUE_NAME = "example";
    private static final String INPUT_CHANNEL = "input";

    @Bean
    @InboundChannelAdapter(channel = INPUT_CHANNEL, poller = @Poller(fixedDelay = "1000"))
    public StorageQueueMessageSource storageQueueMessageSource(StorageQueueOperation storageQueueOperation) {
        storageQueueOperation.setCheckpointMode(CheckpointMode.MANUAL);
        storageQueueOperation.setVisibilityTimeoutInSeconds(10);

        StorageQueueMessageSource messageSource =
            new StorageQueueMessageSource(STORAGE_QUEUE_NAME, storageQueueOperation);
        return messageSource;
    }

    /**
     * This message receiver binding with {@link StorageQueueMessageSource}
     * via {@link MessageChannel} has name {@value INPUT_CHANNEL}
     */
    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer) {
        String message = new String(payload);
        System.out.println(String.format("New message received: '%s'", message));
        checkpointer.success()
            .doOnError(Throwable::printStackTrace)
            .doOnSuccess(t -> System.out.println(String.format("Message '%s' successfully checkpointed", message)))
            .subscribe();

    }
}
