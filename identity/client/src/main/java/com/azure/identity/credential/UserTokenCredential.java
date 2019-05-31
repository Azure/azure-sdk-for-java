// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User account based credential.
 */
public class UserTokenCredential extends AadCredential<UserTokenCredential> {
    /** The user name for the Organization Id account. */
    private String username;
    /** The password for the Organization Id account. */
    private String password;

    /**
     * Initializes a new instance of the UserTokenCredentials.
     */
    public UserTokenCredential() {
        super();
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
    protected Mono<AuthenticationResult> refreshAsync(AuthenticationResult expiredResult, String resource) {
        return acquireAccessTokenFromRefreshToken(resource, expiredResult.getRefreshToken(), expiredResult.isMultipleResourceRefreshToken());
    }

    @Override
    protected synchronized Mono<AuthenticationResult> authenticateAsync(String resource) {
        validate();
        if (username == null || password == null) {
            throw new IllegalArgumentException("Non-null values must be provided for username and password properties in UserTokenCredential");
        }
        return acquireNewAccessToken(resource);
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
