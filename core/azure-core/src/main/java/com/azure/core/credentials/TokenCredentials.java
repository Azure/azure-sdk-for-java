// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credentials;

import java.io.IOException;

/**
 * Token based credentials for use with a REST Service Client.
 */
public class TokenCredentials implements ServiceClientCredentials {
    /**
     * The authentication scheme.
     */
    private String scheme;

    /**
     * The secure token.
     */
    private String token;

    /**
     * Creates TokenCredentials.
     *
     * @param scheme scheme to use. If null, defaults to Bearer
     * @param token  valid token
     */
    public TokenCredentials(String scheme, String token) {
        if (scheme == null) {
            scheme = "Bearer";
        }
        this.scheme = scheme;
        this.token = token;
    }

    @Override
    public String authorizationHeaderValue(String uri) throws IOException {
        return scheme + " " + token;
    }
}
