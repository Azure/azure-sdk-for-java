// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.implementation;

/**
 * INTERNAL CLASS.
 *
 * An exception indicating that an operation on long-running operation
 * requires a {@link com.azure.core.util.polling.PollingContext}
 */
public class PollContextRequiredException extends RuntimeException {
    /**
     * Creates PollContextRequiredException.
     */
    public PollContextRequiredException() {
        super("");
    }
}
