/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

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
     *
     * @return @return {@code Observable<Message<?>>}
     */
    Observable<Message<?>> subscribe(String destination, Class<?> messagePayloadType);

    void setCheckpointMode(CheckpointMode checkpointMode);
}
