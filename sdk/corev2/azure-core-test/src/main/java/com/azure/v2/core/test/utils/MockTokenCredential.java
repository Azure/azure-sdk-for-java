// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.OAuthTokenCredential;
import io.clientcore.core.credentials.oauth.OAuthTokenRequestContext;

import java.time.OffsetDateTime;

/**
 * This class mocks the behavior of {@code TokenCredential} without making a network call
 * with dummy credentials.
 */
public class MockTokenCredential implements OAuthTokenCredential {

    /**
     * Creates an instance of {@link MockTokenCredential}.
     */
    public MockTokenCredential() {
    }

    @Override
    public AccessToken getToken(OAuthTokenRequestContext request) {
        return new AccessToken("mockToken", OffsetDateTime.now().plusHours(2));
    }
}
