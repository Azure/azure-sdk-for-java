// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.Base64Util;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;

/**
 * Basic Auth credentials for use with a REST Service Client.
 */
public class BasicAuthenticationCredential implements TokenCredential {
    private final ClientLogger logger = new ClientLogger(BasicAuthenticationCredential.class);
    /**
     * Basic auth user name.
     */
    private final String username;

    /**
     * Basic auth password.
     */
    private final String password;

    /**
     * Creates a basic authentication credential.
     *
     * @param username basic auth user name
     * @param password basic auth password
     */
    public BasicAuthenticationCredential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @throws RuntimeException If the UTF-8 encoding isn't supported.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        String credential = username + ":" + password;
        String encodedCredential;
        try {
            encodedCredential = Base64Util.encodeToString(credential.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            // The encoding is hard-coded, so if it's unsupported, it needs to be fixed right here.
            throw logger.logExceptionAsError(new RuntimeException(e));
        }

        return Mono.just(new AccessToken(encodedCredential, OffsetDateTime.MAX));
    }
}
