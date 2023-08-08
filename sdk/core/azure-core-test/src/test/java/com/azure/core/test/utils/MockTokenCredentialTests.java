// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MockTokenCredential}.
 */
public class MockTokenCredentialTests {

    @Test
    public void basicRetrieveToken() {
        MockTokenCredential credential = new MockTokenCredential();
        AccessToken credentialToken = credential.getTokenSync(new TokenRequestContext());
        assertEquals(credentialToken.getToken(), "mockToken");
    }
}
