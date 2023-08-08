// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import org.apache.qpid.proton.engine.EndpointState;

/**
 * Helper class for managing endpoint states from proton-j.
 */
class AmqpEndpointStateUtil {
    /**
     * Translates proton-j endpoint states into an AMQP endpoint state.
     * @param state proton-j endpoint state.
     * @return The corresponding {@link AmqpEndpointState}.
     * @throws IllegalArgumentException if {@code state} is not a supported {@link AmqpEndpointState}.
     */
    static AmqpEndpointState getConnectionState(EndpointState state) {
        switch (state) {
            case ACTIVE:
                return AmqpEndpointState.ACTIVE;
            case UNINITIALIZED:
                return AmqpEndpointState.UNINITIALIZED;
            case CLOSED:
                return AmqpEndpointState.CLOSED;
            default:
                throw new IllegalArgumentException("This endpoint state is not supported. State:" + state);
        }
    }
}
