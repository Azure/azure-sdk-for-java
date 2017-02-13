/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.credentials;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;

/**
 * Token based credentials for use with a REST Service Client.
 */
public class TokenCredentials implements ServiceClientCredentials {
    /** The authentication scheme. */
    private String scheme;

    /** The secure token. */
    private String token;

    /**
     * Initializes a new instance of the TokenCredentials.
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

    /**
     * Get the secure token. Override this method to provide a mechanism
     * for acquiring tokens.
     *
     * @param request the context of the HTTP request
     * @return the secure token.
     * @throws IOException exception thrown from token acquisition operations.
     */
    protected String getToken(Request request) throws IOException {
        return token;
    }

    /**
     * Get the authentication scheme.
     *
     * @return the authentication scheme
     */
    protected String getScheme() {
        return scheme;
    }

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new TokenCredentialsInterceptor(this));
    }
}
