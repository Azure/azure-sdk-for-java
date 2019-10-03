// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import reactor.core.publisher.Flux;

/**
 * Notifies subscribers of the endpoint state and any errors that occur with the object.
 */
public interface EndpointStateNotifier {

    /**
     * Gets the current state of the endpoint.
     *
     * @return The current state of the endpoint.
     */
    AmqpEndpointState getCurrentState();

    /**
     * Gets the errors that occurred in the AMQP endpoint.
     *
     * @return A stream of errors that occurred in the AMQP endpoint.
     */
    Flux<Throwable> getErrors();

    /**
     * Gets the endpoint states for the AMQP endpoint.
     *
     * @return A stream of endpoint states as they occur in the endpoint.
     */
    Flux<AmqpEndpointState> getConnectionStates();

    /**
     * Gets any shutdown signals that occur in the AMQP endpoint.
     *
     * @return A stream of shutdown signals that occur in the AMQP endpoint.
     */
    Flux<AmqpShutdownSignal> getShutdownSignals();
}
