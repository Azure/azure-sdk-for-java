// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

/**
 * Represents a state for a connection, session, or link.
 */
public enum AmqpEndpointState {
    /**
     * The endpoint has not been initialized.
     */
    UNINITIALIZED,
    /**
     * The endpoint is active.
     */
    ACTIVE,
    /**
     * The endpoint is closed.
     */
    CLOSED
}
