// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.credential;

import com.typespec.core.util.Base64Util;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/**
 * Basic Auth credentials for use with a REST Service Client.
 */
public class BasicAuthenticationCredential implements TokenCredential {
    /**
     * Base64 encoded username-password credential.
     */
    private final String encodedCredential;

    /**
     * Creates a basic authentication credential.
     *
     * @param username basic auth user name
     * @param password basic auth password
     */
    public BasicAuthenticationCredential(String username, String password) {
        String credential = username + ":" + password;
        this.encodedCredential = Base64Util.encodeToString(credential.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @throws RuntimeException If the UTF-8 encoding isn't supported.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.fromCallable(() -> new AccessToken(encodedCredential, OffsetDateTime.MAX));
    }
}
