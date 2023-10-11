// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.credential;

import com.generic.core.util.Base64Util;

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

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        return null;
    }
}
