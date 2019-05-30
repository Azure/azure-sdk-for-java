// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User account based credential.
 */
public class UserTokenCredential extends AadCredential<UserTokenCredential> {
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AuthenticationResult> tokens;
    /** The user name for the Organization Id account. */
    private String username;
    /** The password for the Organization Id account. */
    private String password;

    /**
     * Initializes a new instance of the UserTokenCredentials.
     */
    public UserTokenCredential() {
        super();
        this.tokens = new ConcurrentHashMap<>();
    }

    /**
     * Sets the user name for the Organization Id account.
     * @param username the user name for the Organization Id account
     * @return the credential itself
     */
    public UserTokenCredential username(String username) {
        this.username = username;
        return this;
    }

    /**
     * Sets the password for the Organization Id account.
     * @param password the password for the Organization Id account
     * @return the credential itself
     */
    public UserTokenCredential password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Gets the user name for the Organization Id account.
     *
     * @return the user name.
     */
    public String username() {
        return username;
    }

    @Override
    public synchronized Mono<String> getTokenAsync(String resource) {
        validate();
        if (username == null || password == null) {
            throw new IllegalArgumentException("Non-null values must be provided for username and password properties in UserTokenCredential");
        }
        // Find exact match for the resource
        AuthenticationResult[] authenticationResult = new AuthenticationResult[1];
        authenticationResult[0] = tokens.get(resource);
        // Return if found and not expired
        if (authenticationResult[0] != null && authenticationResult[0].getExpiresOnDate().after(new Date())) {
            return Mono.just(authenticationResult[0].getAccessToken());
        }
        // If found then refresh
        boolean shouldRefresh = authenticationResult[0] != null;
        // If not found for the resource, but is MRRT then also refresh
        if (authenticationResult[0] == null && !tokens.isEmpty()) {
            authenticationResult[0] = new ArrayList<>(tokens.values()).get(0);
            shouldRefresh = authenticationResult[0].isMultipleResourceRefreshToken();
        }

        if (shouldRefresh) {
            return Mono.defer(() -> acquireAccessTokenFromRefreshToken(resource, authenticationResult[0].getRefreshToken(), authenticationResult[0].isMultipleResourceRefreshToken())
                    .onErrorResume(t -> acquireNewAccessToken(resource))
                    .doOnNext(ar -> tokens.put(resource, ar))
                    .then(Mono.just(tokens.get(resource).getAccessToken())));
        } else {
            return Mono.just(tokens.get(resource).getAccessToken());
        }
    }

    Mono<AuthenticationResult> acquireNewAccessToken(String resource) {
        String authorityUrl = aadEndpoint() + tenantId();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Mono<AuthenticationResult> authMono = Mono.defer(() -> {
            AuthenticationContext context;
            try {
                context = new AuthenticationContext(authorityUrl, false, executor);
            } catch (MalformedURLException mue) {
                return Mono.error(mue);
            }
            return Mono.create(callback -> {
                context.acquireToken(
                        resource,
                        this.clientId(),
                        this.username(),
                        this.password,
                        new AuthenticationCallback() {
                            @Override
                            public void onSuccess(Object o) {
                                callback.success((AuthenticationResult) o);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                callback.error(throwable);
                            }
                        });
            });
        });
        return authMono.doFinally(s -> executor.shutdown());
    }

    // Refresh tokens are currently not used since we don't know if the refresh token has expired
    private Mono<AuthenticationResult> acquireAccessTokenFromRefreshToken(String resource, String refreshToken, boolean isMultipleResourceRefreshToken) {
        String authorityUrl = this.aadEndpoint() + this.tenantId();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Mono<AuthenticationResult> authMono = Mono.defer(() -> {
            AuthenticationContext context;
            try {
                context = new AuthenticationContext(authorityUrl, false, executor);
            } catch (MalformedURLException mue) {
                return Mono.error(mue);
            }
            return Mono.create(callback -> {
                context.acquireTokenByRefreshToken(
                        refreshToken,
                        clientId(),
                        resource,
                        new AuthenticationCallback() {
                            @Override
                            public void onSuccess(Object o) {
                                callback.success((AuthenticationResult) o);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                callback.error(throwable);
                            }
                        });
            });
        });
        return authMono.doFinally(s -> executor.shutdown());
    }
}
