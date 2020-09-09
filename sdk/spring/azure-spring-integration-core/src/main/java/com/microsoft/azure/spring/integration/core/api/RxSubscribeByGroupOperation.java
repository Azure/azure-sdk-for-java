// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;
import rx.Observable;

/**
 * Operations for subscribing to a destination with a consumer group in a reactive way.
 *
 * @author Warren Zhu
 */
public interface RxSubscribeByGroupOperation extends Checkpointable {

    /**
     * Register a message consumer to a given destination with a given consumer group.
     * @param destination destination
     * @param consumerGroup consumer group
     * @param messagePayloadType message payload type
     * @return {@code Observable<Message<?>>}
     */
    Observable<Message<?>> subscribe(String destination, String consumerGroup,
                                     Class<?> messagePayloadType);
}
