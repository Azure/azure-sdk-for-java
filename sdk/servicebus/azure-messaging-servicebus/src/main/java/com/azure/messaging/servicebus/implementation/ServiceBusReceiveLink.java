// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.AsyncCloseable;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents an AMQP receive link.
 */
public interface ServiceBusReceiveLink extends AmqpReceiveLink, AsyncCloseable {
    /**
     * Gets the session id associated with the link.
     *
     * @return The session id associated with the link or an empty mono if this is not a session link.
     */
    Mono<String> getSessionId();

    /**
     * Gets the {@link OffsetDateTime} the session is locked until.
     *
     * @return The {@link OffsetDateTime} the session is locked until or an empty Mono if this is not a session link.
     */
    Mono<OffsetDateTime> getSessionLockedUntil();

    /**
     * Gets the properties of the session if the link is associated with a session enabled entity.
     *
     * <p>The API waits for the link to Active then read the session properties. The API returns
     * {@link com.azure.core.amqp.exception.AmqpException} if the link has no session id property, session id property
     * will be absent for entity that is not session enabled.  The AmqpException can also be returned if the link never
     * gets Active.</p>
     *
     * @return The session properties.
     */
    // TODO (anu): remove getSessionId and getSessionLockedUntil APIs in favor if this API.
    Mono<SessionProperties> getSessionProperties();

    /**
     * Updates the disposition status of a message with corresponding lock token.
     *
     * @param lockToken Lock token of message.
     * @param deliveryState Delivery state of message.
     *
     * @return A Mono that completes when the state is successfully updated and acknowledged by message broker.
     */
    // TODO (anu,connie): remove updateDisposition from this contract as it is now exported by the updated base AmqpReceiveLink.
    Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState);

    class SessionProperties {
        private final String id;
        private final OffsetDateTime lockedUntil;

        public SessionProperties(String sessionId, OffsetDateTime sessionLockedUntil) {
            this.id = Objects.requireNonNull(sessionId, "sessionId cannot be null.");
            this.lockedUntil = Objects.requireNonNull(sessionLockedUntil, "sessionLockedUntil cannot be null");
        }

        /**
         * Gets the session id.
         *
         * @return the session id.
         */
        public String getId() {
            return id;
        }

        /**
         * Gets the UTC time until which the session is initially locked.
         *
         * @return the UTC time at which the initial session lock expires.
         */
        public OffsetDateTime getLockedUntil() {
            return lockedUntil;
        }
    }
}
