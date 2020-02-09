// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.Context;

/**
 * Class to hold commonly used constants.
 */
public final class CoreConstants {

    /**
     * This constant is used as key in {@link Context}. This constant is reserved for disabling deep copy of byte
     * buffers from HTTP responses when using Netty HTTP client. If other HTTP clients are used, setting this in
     * context will have no effect.
     */
    public static final String DISABLE_BUFFER_COPY = "disable-buffer-copy";

    private CoreConstants() {
        // no instances allowed
    }
}
