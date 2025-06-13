// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;

import java.time.OffsetDateTime;

/**
 * Common test utilities.
 */
public final class TestUtils {
    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TestUtils() {
    }

    static class TestCredential implements TokenCredential {
        @Override
        public AccessToken getToken(TokenRequestContext request) {
            return new AccessToken("TestAccessToken", OffsetDateTime.now().plusHours(1));
        }
    }
}
