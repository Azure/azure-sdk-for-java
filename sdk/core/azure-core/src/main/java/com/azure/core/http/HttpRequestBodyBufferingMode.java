// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

/**
 * TODO (kasobol-msft) add docs.
 */
public enum HttpRequestBodyBufferingMode {
    /**
     * Always buffer streamable body.
     */
    DEEP,

    /**
     * Attempt to use shallow buffer copies or mark reset.
     */
    SHALLOW,

    /**
     * Don't buffer.
     */
    NEVER,
}
