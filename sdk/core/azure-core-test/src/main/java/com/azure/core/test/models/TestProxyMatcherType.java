// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test.models;

/**
 * The possible record sanitizer types.
 * Each sanitizer is optionally prefaced with the specific part of the request/response pair that it applies to.
 */
public enum TestProxyMatcherType {
    /**
     * Sanitize the request url.
     */
    BODILESS("BodilessMatcher"),
    /**
     * Sanitize the response body.
     */
    CUSTOM("CustomDefaultMatcher"),

    HEADERLESS("HeaderlessMatcher");

    private final String name;

    TestProxyMatcherType(String name) {
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
