// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpReceiveLink;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Represents an AMQP receive link.
 */
public interface ServiceBusReceiveLink extends AmqpReceiveLink {
    /**
     * Gets the session id associated with the link.
     *
     * @return The session id associated with the link or an empty mono if this is not a session link.
     */
    Mono<String> getSessionId();

    /**
     * Gets the {@link Instant} the session is locked until.
     *
     * @return The {@link Instant} the session is locked until or an empty Mono if this is not a session link.
     */
    Mono<Instant> getSessionLockedUntil();
}
