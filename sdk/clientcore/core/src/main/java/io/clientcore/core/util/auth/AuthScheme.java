// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.auth;

/**
 * Enum representing different authentication schemes.
 */
public enum AuthScheme {
    BASIC("Basic "),
    DIGEST("Digest ");

    private final String scheme;

    /**
     * Constructor for AuthScheme.
     *
     * @param scheme The authentication scheme string.
     */
    AuthScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the authentication scheme string.
     *
     * @return The authentication scheme string.
     */
    public String getScheme() {
        return scheme;
    }
}
