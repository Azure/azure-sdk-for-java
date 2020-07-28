/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api.reactor;

import com.microsoft.azure.spring.integration.core.api.CheckpointMode;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

/**
 * Operations for receiving {@link Message<?>} from a destination.
 * Received message contain payload of type specified by {@link #setMessagePayloadType(Class)}}
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public interface ReceiveOperation {

    /**
     * Receive a message from destination async.
     * @return {@link Mono} of the next available {@link Message} or {@code null} if empty
     */
    Mono<Message<?>> receiveAsync(String destination);

    /**
     * Set message payload type. Default is {@link byte[]}
     */
    void setMessagePayloadType(Class<?> messagePayloadType);

    void setCheckpointMode(CheckpointMode checkpointMode);
}
