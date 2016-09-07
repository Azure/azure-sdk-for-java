/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.credentials;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.rest.credentials.TokenCredentials;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Token based credentials for use with a REST Service Client.
 */
public class UserTokenCredentials extends TokenCredentials implements AzureTokenCredentials {
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AuthenticationResult> tokens;
    /** The Active Directory application client id. */
    private String clientId;
    /** The domain or tenant id containing this application. */
    private String domain;
    /** The user name for the Organization Id account. */
    private String username;
    /** The password for the Organization Id account. */
    private String password;
    /** The Azure environment to authenticate with. */
    private AzureEnvironment environment;

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
        super(null, null); // defer token acquisition
        this.clientId = clientId;
        this.domain = domain;
        this.username = username;
        this.password = password;
        this.environment = environment;
        this.tokens = new HashMap<>();
    }

    /**
     * Gets the active directory application client id.
     *
     * @return the active directory application client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the tenant or domain the containing the application.
     *
     * @return the tenant or domain the containing the application.
     */
    @Override
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the user name for the Organization Id account.
     *
     * @return the user name.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password for the Organization Id account.
     *
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    @Override
    public String getToken(String resource) throws IOException {
        AuthenticationResult authenticationResult = tokens.get(resource);
        if (authenticationResult == null || authenticationResult.getExpiresOnDate().before(new Date())) {
            authenticationResult = acquireAccessToken(resource);
        }
        return authenticationResult.getAccessToken();
    }

    /**
     * Gets the Azure environment to authenticate with.
     *
     * @return the Azure environment to authenticate with.
     */
    public AzureEnvironment getEnvironment() {
        return environment;
    }

    private AuthenticationResult acquireAccessToken(String resource) throws IOException {
        String authorityUrl = this.getEnvironment().getAuthenticationEndpoint() + this.getDomain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context = new AuthenticationContext(authorityUrl, this.getEnvironment().isValidateAuthority(), executor);
        try {
            AuthenticationResult result = context.acquireToken(
                    resource,
                    this.getClientId(),
                    this.getUsername(),
                    this.getPassword(),
                    null).get();
            tokens.put(resource, result);
            return result;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }

    // Refresh tokens are currently not used since we don't know if the refresh token has expired
    private AuthenticationResult acquireAccessTokenFromRefreshToken(String resource) throws IOException {
        String authorityUrl = this.getEnvironment().getAuthenticationEndpoint() + this.getDomain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context = new AuthenticationContext(authorityUrl, this.getEnvironment().isValidateAuthority(), executor);
        try {
            AuthenticationResult result = context.acquireTokenByRefreshToken(
                    tokens.get(resource).getRefreshToken(),
                    this.getClientId(),
                    null, null).get();
            tokens.put(resource, result);
            return result;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new AzureTokenCredentialsInterceptor(this));
    }
}
