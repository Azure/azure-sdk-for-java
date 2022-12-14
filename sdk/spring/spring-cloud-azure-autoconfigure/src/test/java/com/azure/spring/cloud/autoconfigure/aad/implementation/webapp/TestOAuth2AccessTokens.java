// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;

public final class TestOAuth2AccessTokens {

    private TestOAuth2AccessTokens() {
    }

    public static OAuth2AccessToken noScopes() {
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "no-scopes", Instant.now(),
            Instant.now().plus(Duration.ofDays(1)));
    }

    public static OAuth2AccessToken scopes(String... scopes) {
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "scopes", Instant.now(),
            Instant.now().plus(Duration.ofDays(1)), new HashSet<>(Arrays.asList(scopes)));
    }

}
