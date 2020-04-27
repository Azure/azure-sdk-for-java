// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Performs operations on Service Bus sessions.
 */
public interface SessionManagementNode extends AutoCloseable {
    /**
     * Gets the current session state.
     *
     * @param sessionId Identifier of session.
     *
     * @return The state of the session.
     */
    Mono<byte[]> getSessionState(String sessionId);

    /**
     * Renews the lock on the session.
     *
     * @param sessionId Identifier of the session.
     *
     * @return The next expiration time for the session.
     */
    Mono<Instant> renewSessionLock(String sessionId);

    /**
     * Updates the session state.
     *
     * @param sessionId identifier of session to update.
     * @param state State to update session.
     *
     * @return A Mono that completes when the state is updated.
     */
    Mono<Void> setSessionState(String sessionId, byte[] state);
}
