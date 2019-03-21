/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import com.azure.common.AzureEnvironment;
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
 * Token based credentials for use with a REST Service Client.
 */
public class UserTokenCredentials extends AzureTokenCredentials {
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AuthenticationResult> tokens;
    /** The Active Directory application client id. */
    private String clientId;
    /** The user name for the Organization Id account. */
    private String username;
    /** The password for the Organization Id account. */
    private String password;

    /**
     * Initializes a new instance of the UserTokenCredentials.
     *
     * @param clientId the active directory application client id.
     * @param domain the domain or tenant id containing this application.
     * @param username the user name for the Organization Id account.
     * @param password the password for the Organization Id account.
     * @param environment the Azure environment to authenticate with.
     *                    If null is provided, AzureEnvironment.AZURE will be used.
     */
    public UserTokenCredentials(String clientId, String domain, String username, String password, AzureEnvironment environment) {
        super(environment, domain); // defer token acquisition
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.tokens = new ConcurrentHashMap<>();
    }

    /**
     * Gets the active directory application client id.
     *
     * @return the active directory application client id.
     */
    public String clientId() {
        return clientId;
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
    public synchronized Mono<String> getToken(String resource) {
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
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Mono<AuthenticationResult> authMono = Mono.defer(() -> {
            AuthenticationContext context;
            try {
                context = new AuthenticationContext(authorityUrl, false, executor);
            } catch (MalformedURLException mue) {
                return Mono.error(mue);
            }
            if (proxy() != null) {
                context.setProxy(proxy());
            }
            return Mono.create(callback -> {
                context.acquireToken(
                        resource,
                        this.clientId(),
                        this.username(),
                        this.password,
                        Util.authenticationDelegate(callback));
            });
        });
        return authMono.doFinally(s -> executor.shutdown());
    }

    // Refresh tokens are currently not used since we don't know if the refresh token has expired
    Mono<AuthenticationResult> acquireAccessTokenFromRefreshToken(String resource, String refreshToken, boolean isMultipleResourceRefreshToken) {
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Mono<AuthenticationResult> authMono = Mono.defer(() -> {
            AuthenticationContext context;
            try {
                context = new AuthenticationContext(authorityUrl, false, executor);
            } catch (MalformedURLException mue) {
                return Mono.error(mue);
            }
            if (proxy() != null) {
                context.setProxy(proxy());
            }
            return Mono.create(callback -> {
                context.acquireTokenByRefreshToken(
                        refreshToken,
                        clientId(),
                        resource,
                        Util.authenticationDelegate(callback));
            });
        });
        return authMono.doFinally(s -> executor.shutdown());
    }
}
