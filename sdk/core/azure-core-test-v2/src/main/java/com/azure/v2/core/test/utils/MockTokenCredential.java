// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.AccessToken;

import java.time.OffsetDateTime;

/**
 * This class mocks the behavior of {@code TokenCredential} without making a network call
 * with dummy credentials.
 */
public class MockTokenCredential implements TokenCredential {

    /**
     * Creates an instance of {@link MockTokenCredential}.
     */
    public MockTokenCredential() {
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        return new AccessToken("mockToken", OffsetDateTime.now().plusHours(2));
    }
}
