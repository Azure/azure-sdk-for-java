// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import com.azure.v2.core.credentials.AccessToken;
import com.azure.v2.core.credentials.TokenRequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MockTokenCredential}.
 */
public class MockTokenCredentialTests {

    @Test
    public void basicRetrieveToken() {
        MockTokenCredential credential = new MockTokenCredential();
        AccessToken credentialToken = credential.getToken(new TokenRequestContext());
        assertEquals("mockToken", credentialToken.getToken());
    }
}
