// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.implementation.polling;

/**
 * INTERNAL CLASS.
 *
 * An exception indicating that an operation on long-running operation
 * requires a PollingContext.
 */
public class PollContextRequiredException extends RuntimeException {
    /**
     * Creates PollContextRequiredException.
     */
    public PollContextRequiredException() {
        super("");
    }
}
