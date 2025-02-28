// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.OAuthTokenRequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MockTokenCredential}.
 */
public class MockTokenCredentialTests {

    @Test
    public void basicRetrieveToken() {
        MockTokenCredential credential = new MockTokenCredential();
        AccessToken credentialToken = credential.getToken(new OAuthTokenRequestContext());
        assertEquals("mockToken", credentialToken.getToken());
    }
}
