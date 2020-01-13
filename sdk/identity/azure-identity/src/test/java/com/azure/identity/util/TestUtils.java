// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.util;

import com.azure.core.credential.AccessToken;
import com.azure.identity.implementation.MsalToken;
import com.microsoft.aad.msal4j.Account;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Utilities for identity tests.
 */
public final class TestUtils {
    /**
     * Creates a mock {@link IAuthenticationResult} instance.
     * @param accessToken the access token to return
     * @param expiresOn the expiration time
     * @return a completable future of the result
     */
    public static CompletableFuture<IAuthenticationResult> getMockAuthenticationResult(String accessToken, OffsetDateTime expiresOn) {
        return CompletableFuture.completedFuture(new IAuthenticationResult() {
            @Override
            public String accessToken() {
                return accessToken;
            }

            @Override
            public String idToken() {
                return null;
            }

            @Override
            public IAccount account() {
                return new Account(UUID.randomUUID().toString(), "http://login.microsoftonline.com", "testuser");
            }

            @Override
            public String environment() {
                return null;
            }

            @Override
            public String scopes() {
                return null;
            }

            @Override
            public Date expiresOnDate() {
                // Access token dials back 2 minutes
                return Date.from(expiresOn.plusMinutes(2).toInstant());
            }
        });
    }

    /**
     * Creates a mock {@link MsalToken} instance.
     * @param accessToken the access token to return
     * @param expiresOn the expiration time
     * @return a Mono publisher of the result
     */
    public static Mono<MsalToken> getMockMsalToken(String accessToken, OffsetDateTime expiresOn) {
        return Mono.fromFuture(getMockAuthenticationResult(accessToken, expiresOn))
            .map(MsalToken::new);
    }

    /**
     * Creates a mock {@link AccessToken} instance.
     * @param accessToken the access token to return
     * @param expiresOn the expiration time
     * @return a Mono publisher of the result
     */
    public static Mono<AccessToken> getMockAccessToken(String accessToken, OffsetDateTime expiresOn) {
        return Mono.just(new AccessToken(accessToken, expiresOn.plusMinutes(2)));
    }

    public static Mono<AccessToken> getMockAccessToken(String accessToken, String expiresOn) {
        OffsetDateTime EPOCH = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a XXX");
        Long seconds = null;
        try {
            seconds = Long.parseLong(expiresOn);
        } catch (NumberFormatException e) {
        }
        try {
            seconds = Instant.from(dtf.parse(expiresOn)).toEpochMilli() / 1000L;
        } catch (DateTimeParseException e) {
        }
        return Mono.just(new AccessToken(accessToken, EPOCH.plusSeconds(seconds)));
    }


    private TestUtils() {
    }
}