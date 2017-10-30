/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.v2.AzureEnvironment;

import java.io.IOException;
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

    private RefreshTokenClient refreshTokenClient;

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
        this.refreshTokenClient = new RefreshTokenClient(environment.activeDirectoryEndpoint(), proxy());
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
    public synchronized String getToken(String resource) throws IOException {
        // Find exact match for the resource
        AuthenticationResult authenticationResult = tokens.get(resource);
        // Return if found and not expired
        if (authenticationResult != null && authenticationResult.getExpiresOnDate().after(new Date())) {
            return authenticationResult.getAccessToken();
        }
        // If found then refresh
        boolean shouldRefresh = authenticationResult != null;
        // If not found for the resource, but is MRRT then also refresh
        if (authenticationResult == null && !tokens.isEmpty()) {
            authenticationResult = new ArrayList<>(tokens.values()).get(0);
            shouldRefresh = authenticationResult.isMultipleResourceRefreshToken();
        }
        // Refresh
        if (shouldRefresh) {
            authenticationResult = acquireAccessTokenFromRefreshToken(resource, authenticationResult.getRefreshToken(), authenticationResult.isMultipleResourceRefreshToken());
        }
        // If refresh fails or not refreshable, acquire new token
        if (authenticationResult == null) {
            authenticationResult = acquireNewAccessToken(resource);
        }
        tokens.put(resource, authenticationResult);
        return authenticationResult.getAccessToken();
    }

    AuthenticationResult acquireNewAccessToken(String resource) throws IOException {
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context = new AuthenticationContext(authorityUrl, false, executor);
        if (proxy() != null) {
            context.setProxy(proxy());
        }
        try {
            return context.acquireToken(
                    resource,
                    this.clientId(),
                    this.username(),
                    this.password,
                    null).get();
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }

    // Refresh tokens are currently not used since we don't know if the refresh token has expired
    AuthenticationResult acquireAccessTokenFromRefreshToken(String resource, String refreshToken, boolean isMultipleResourceRefreshToken) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return refreshTokenClient.refreshToken(domain(), clientId(), resource, refreshToken, isMultipleResourceRefreshToken);
        } catch (Exception e) {
            return null;
        } finally {
            executor.shutdown();
        }
    }
}
