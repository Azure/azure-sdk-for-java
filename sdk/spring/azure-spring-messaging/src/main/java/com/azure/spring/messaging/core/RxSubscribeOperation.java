// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.core;

import com.azure.spring.messaging.checkpoint.CheckpointMode;
import org.springframework.messaging.Message;
import rx.Observable;

/**
 * Operations for subscribing to a destination in reactive way.
 *
 * @author Warren Zhu
 */
public interface RxSubscribeOperation {

    /**
     * Register a message consumer to a given destination.
     * @param destination destination
     * @param messagePayloadType message payload type
     * @return {@code Observable<Message<?>>}
     */
    Observable<Message<?>> subscribe(String destination, Class<?> messagePayloadType);

    void setCheckpointMode(CheckpointMode checkpointMode);
}
