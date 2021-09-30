// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import org.springframework.messaging.Message;
import rx.Observable;

/**
 * Operations for subscribing to a destination with a consumer group in a reactive way.
 *
 * @author Warren Zhu
 * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link SubscribeByGroupOperation}. From version 4.0.0, the reactor API support will be moved to
 * com.azure.spring.messaging.core.SubscribeByGroupOperation.
 */
@Deprecated
public interface RxSubscribeByGroupOperation extends Checkpointable {

    /**
     * Register a message consumer to a given destination with a given consumer group.
     * @param destination destination
     * @param consumerGroup consumer group
     * @param messagePayloadType message payload type
     * @return {@code Observable<Message<?>>}
     *
     * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link SubscribeByGroupOperation}. From version 4.0.0, the reactor API support will be moved to
     * com.azure.spring.messaging.core.SubscribeByGroupOperation.
     */
    @Deprecated
    Observable<Message<?>> subscribe(String destination, String consumerGroup,
                                     Class<?> messagePayloadType);
}
