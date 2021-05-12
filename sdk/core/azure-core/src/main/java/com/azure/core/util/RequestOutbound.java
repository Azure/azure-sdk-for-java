// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.nio.channels.WritableByteChannel;

/**
 * Represents the network outbound to where the request is being sent.
 */
public interface RequestOutbound {
    /**
     * Gets the {@link WritableByteChannel} representing the network outbound.
     *
     * @return The {@link WritableByteChannel} representing the network outbound.
     */
    WritableByteChannel getRequestChannel();
}
