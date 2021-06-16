// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

/**
 * ResponseStatusOption controls the behavior of an operation based on the status code of a response.
 */
public enum ResponseStatusOption {
    /**
     * Indicates that an operation should throw an exception when the response indicates a failure.
     */
    DEFAULT,

    /**
     * Indicates that an operation should not throw an exception when the response indicates a failure.
     */
    NO_THROW;
}
