// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core.reactor;

import com.azure.spring.messaging.checkpoint.CheckpointMode;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

/**
 * Operations for receiving {@link Message}&lt;?&gt; from a destination.
 * Received message contain payload of type specified by {@link #setMessagePayloadType(Class)}}
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public interface ReceiveOperation {

    /**
     * Receive a message from destination async.
     * @param destination destination
     * @return {@link Mono} of the next available {@link Message} or {@code null} if empty
     */
    Mono<Message<?>> receiveAsync(String destination);

    /**
     * Set message payload type. Default is {@code byte[]}
     * @param messagePayloadType message payload type
     */
    void setMessagePayloadType(Class<?> messagePayloadType);

    void setCheckpointMode(CheckpointMode checkpointMode);
}
