// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.models;

/**
 * The possible record sanitizer types.
 * Each sanitizer is optionally prefaced with the specific part of the request/response pair that it applies to.
 */
public enum TestProxySanitizerType {
    /**
     * Sanitize the request url.
     */
    URL,
    /**
     * Sanitize the response body.
     */
    BODY,
    /**
     * Sanitize the request/response headers.
     */
    HEADER,
}
