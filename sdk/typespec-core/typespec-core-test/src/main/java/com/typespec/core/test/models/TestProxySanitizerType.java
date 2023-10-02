// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.typespec.core.test.models;

/**
 * The possible record sanitizer types.
 * Each sanitizer is optionally prefaced with the specific part of the request/response pair that it applies to.
 */
public enum TestProxySanitizerType {
    /**
     * Sanitize the request url.
     */
    URL("UriRegexSanitizer"),
    /**
     * Sanitize the response body.
     */
    BODY_KEY("BodyKeySanitizer"),

    BODY_REGEX("BodyRegexSanitizer"),
    /**
     * Sanitize the request/response headers.
     */
    HEADER("HeaderRegexSanitizer");

    private final String name;

    TestProxySanitizerType(String name) {
        this.name = name;
    }

    /**
     * Gets the name value of the enum.
     * @return the name value of the enum.
     */
    public String getName() {
        return name;
    }
}
