// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.io.Closeable;

/**
 * Represents a unidirectional AMQP link.
 */
public interface AmqpLink extends Closeable {
    /**
     * Gets the name of the link.
     *
     * @return The name of the link.
     */
    String getLinkName();
}
