// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.io.Closeable;

/**
 * Represents a unidirectional AMQP link.
 */
public interface AmqpLink extends EndpointStateNotifier, Closeable {
    /**
     * Gets the name of the link.
     *
     * @return The name of the link.
     */
    String getLinkName();

    /**
     * The remote endpoint path this link is connected to.
     *
     * @return The remote endpoint path this link is connected to.
     */
    String getEntityPath();
}
