// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import org.springframework.messaging.Message;
import rx.Observable;

/**
 * Operations for subscribing to a destination in reactive way.
 *
 * @author Warren Zhu
 * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link SubscribeOperation}. From version 4.0.0, the reactor API support will be moved to
 * com.azure.spring.messaging.core.SubscribeOperation.
 */
@Deprecated
public interface RxSubscribeOperation {

    /**
     * Register a message consumer to a given destination.
     * @param destination destination
     * @param messagePayloadType message payload type
     * @return {@code Observable<Message<?>>}
     *
     * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link SubscribeOperation}. From version 4.0.0, the reactor API support will be moved to
     * com.azure.spring.messaging.core.SubscribeOperation.
     */
    @Deprecated
    Observable<Message<?>> subscribe(String destination, Class<?> messagePayloadType);

    @Deprecated
    void setCheckpointMode(CheckpointMode checkpointMode);
}
