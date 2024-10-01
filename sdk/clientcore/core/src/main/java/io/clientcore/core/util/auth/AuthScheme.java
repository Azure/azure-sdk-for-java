//  Copyright (c) Microsoft Corporation. All rights reserved.
//  Licensed under the MIT License.

package io.clientcore.core.util.auth;

public enum AuthScheme {
    BASIC("Basic"),
    DIGEST("Digest");

    private final String scheme;

    AuthScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }
}
