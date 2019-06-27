// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.auth;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.rest.credentials.TokenCredentials;
import okhttp3.Request;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User token based credentials for use with a Batch Service Client.
 */
public class BatchUserTokenCredentials extends TokenCredentials implements BatchCredentials {

    /** The Active Directory application client id. */
    private final String clientId;
    /** The tenant or domain containing the application. */
    private final String domain;
    /** The user name for the Organization Id account. */
    private final String username;
    /** The password for the Organization Id account. */
    private final String password;
    /** The user's Batch service endpoint */
    private final String baseUrl;
    /** The Batch service auth endpoint */
    private final String batchEndpoint;
    /** The Active Directory auth endpoint */
    private final String authenticationEndpoint;
    /** The cached access token. */
    private AuthenticationResult authenticationResult;

    /**
     * Initializes a new instance of the BatchUserTokenCredentials.
     *
     * @param baseUrl     the Batch service endpoint.
     * @param clientId    the Active Directory application client id.
     * @param username the user name for the Organization Id account.
     * @param password the password for the Organization Id account.
     * @param domain      the domain or tenant id containing this application.
     * @param batchEndpoint the Batch service endpoint to authenticate with.
     * @param authenticationEndpoint the Active Directory endpoint to authenticate with.
     */
    public BatchUserTokenCredentials(String baseUrl, String clientId, String username, String password, String domain, String batchEndpoint, String authenticationEndpoint) {
        super(null, null);

        if (baseUrl == null) {
            throw new IllegalArgumentException("Parameter baseUrl is required and cannot be null.");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("Parameter clientId is required and cannot be null.");
        }
        if (username == null) {
            throw new IllegalArgumentException("Parameter username is required and cannot be null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Parameter password is required and cannot be null.");
        }
        if (domain == null) {
            throw new IllegalArgumentException("Parameter domain is required and cannot be null.");
        }
        if (batchEndpoint == null) {
            batchEndpoint = "https://batch.core.windows.net/";
        }
        if (authenticationEndpoint == null) {
            authenticationEndpoint = "https://login.microsoftonline.com/";
        }

        this.clientId = clientId;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.batchEndpoint = batchEndpoint;
        this.authenticationEndpoint = authenticationEndpoint;
        this.authenticationResult = null;
    }

    /**
     * Gets the Active Directory application client id.
     *
     * @return the active directory application client id.
     */
    public String clientId() {
        return this.clientId;
    }

    /**
     * Gets the tenant or domain containing the application.
     *
     * @return the tenant or domain containing the application.
     */
    public String domain() {
        return this.domain;
    }

    /**
     * Gets the user name for the Organization Id account.
     *
     * @return the user name.
     */
    public String username() {
        return username;
    }

    /**
     * Gets the Active Directory auth endpoint.
     *
     * @return the Active Directory auth endpoint.
     */
    public String authenticationEndpoint() {
        return this.authenticationEndpoint;
    }

    /**
     * Gets the Batch service auth endpoint.
     *
     * @return the Batch service auth endpoint.
     */
    public String batchEndpoint() {
        return this.batchEndpoint;
    }

    /**
     * Gets the Batch service endpoint
     *
     * @return The Batch service endpoint
     */
    @Override
    public String baseUrl() {
        return this.baseUrl;
    }

    @Override
    public String getToken(Request request) throws IOException {
        if (authenticationResult == null || authenticationResult.getExpiresOnDate().before(new Date())) {
            this.authenticationResult = acquireAccessToken();
        }
        return authenticationResult.getAccessToken();
    }

    private AuthenticationResult acquireAccessToken() throws IOException {
        String authorityUrl = this.authenticationEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context = new AuthenticationContext(authorityUrl, false, executor);
        try {
            this.authenticationResult = context.acquireToken(
                this.batchEndpoint(),
                this.clientId(),
                this.username(),
                this.password,
                null).get();
            return this.authenticationResult;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }
}
