/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.azure.common.annotations.Beta;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Token based credentials to authenticate an application on behalf of a user.
 */
@Beta(since = "1.2.0")
public class DelegatedTokenCredentials extends AzureTokenCredentials {
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AuthenticationResult> tokens;
    private String redirectUrl;
    private String authorizationCode;
    private ApplicationTokenCredentials applicationCredentials;

    /**
     * Initializes a new instance of the DelegatedTokenCredentials.
     *
     * @param applicationCredentials the credentials representing a service principal
     * @param redirectUrl the URL to redirect to after authentication in Active Directory
     */
    public DelegatedTokenCredentials(ApplicationTokenCredentials applicationCredentials, String redirectUrl) {
        super(applicationCredentials.environment(), applicationCredentials.domain()); // defer token acquisition
        this.applicationCredentials = applicationCredentials;
        this.tokens = new ConcurrentHashMap<>();
        this.redirectUrl = redirectUrl;
    }

    /**
     * Initializes a new instance of the DelegatedTokenCredentials, with a pre-acquired oauth2 authorization code.
     *
     * @param applicationCredentials the credentials representing a service principal
     * @param redirectUrl the URL to redirect to after authentication in Active Directory
     * @param authorizationCode the oauth2 authorization code
     */
    public DelegatedTokenCredentials(ApplicationTokenCredentials applicationCredentials, String redirectUrl, String authorizationCode) {
        this(applicationCredentials, redirectUrl);
        this.authorizationCode = authorizationCode;
    }

    /**
     * Creates a new instance of the DelegatedTokenCredentials from an auth file.
     *
     * @param authFile The credentials based on the file
     * @param redirectUrl the URL to redirect to after authentication in Active Directory
     * @return a new delegated token credentials
     * @throws IOException exception thrown from file access errors.
     */
    public static DelegatedTokenCredentials fromFile(File authFile, String redirectUrl) throws IOException {
        return new DelegatedTokenCredentials(ApplicationTokenCredentials.fromFile(authFile), redirectUrl);
    }

    /**
     * Creates a new instance of the DelegatedTokenCredentials from an auth file,
     * with a pre-acquired oauth2 authorization code.
     *
     * @param authFile The credentials based on the file
     * @param redirectUrl the URL to redirect to after authentication in Active Directory
     * @param authorizationCode the oauth2 authorization code
     * @return a new delegated token credentials
     * @throws IOException exception thrown from file access errors.
     */
    public static DelegatedTokenCredentials fromFile(File authFile, String redirectUrl, String authorizationCode) throws IOException {
        return new DelegatedTokenCredentials(ApplicationTokenCredentials.fromFile(authFile), redirectUrl, authorizationCode);
    }

    /**
     * @return the active directory application client id
     */
    public String clientId() {
        return applicationCredentials.clientId();
    }

    /**
     * @return the URL to authenticate through OAuth2
     */
    public String generateAuthenticationUrl() {
        return String.format("%s/%s/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=query&state=%s",
                environment().activeDirectoryEndpoint(), domain(), clientId(), this.redirectUrl, UUID.randomUUID());
    }

    /**
     * Generate the URL to authenticate through OAuth2.
     *
     * @param responseMode the method that should be used to send the resulting token back to your app
     * @param state a value included in the request that is also returned in the token response
     * @return the URL to authenticate through OAuth2
     */
    public String generateAuthenticationUrl(ResponseMode responseMode, String state) {
        return String.format("%s/%s/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=%s&response_mode=%s&state=%s",
                environment().activeDirectoryEndpoint(), domain(), clientId(), this.redirectUrl, responseMode.value, state);
    }

    /**
     * Set the authorization code acquired returned to the redirect URL.
     * @param authorizationCode the oauth2 authorization code
     */
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
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

    private Mono<AuthenticationResult> acquireNewAccessToken(String resource) {
        if (authorizationCode == null) {
            return Mono.error(new IllegalArgumentException("You must acquire an authorization code by redirecting to the authentication URL"));
        }
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        AuthenticationContext context;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            context = new AuthenticationContext(authorityUrl, false, executor);
        } catch (MalformedURLException mue) {
            executor.shutdown();
            throw Exceptions.propagate(mue);
        }
        if (proxy() != null) {
            context.setProxy(proxy());
        }
        Mono<AuthenticationResult> authMono;
        if (applicationCredentials.clientSecret() != null) {
            URI uri;
            try {
                uri = new URI(redirectUrl);
            } catch (URISyntaxException use) {
                return Mono.error(use);
            }
            authMono = Mono.create(callback -> {
                context.acquireTokenByAuthorizationCode(
                        authorizationCode,
                        uri,
                        new ClientCredential(applicationCredentials.clientId(), applicationCredentials.clientSecret()),
                        resource,
                        Util.authenticationDelegate(callback));
            });
        } else if (applicationCredentials.clientCertificate() != null && applicationCredentials.clientCertificatePassword() != null) {
            URI uri;
            try {
                uri = new URI(redirectUrl);
            } catch (URISyntaxException use) {
                return Mono.error(use);
            }
            authMono = Mono.create(callback -> {
                AsymmetricKeyCredential keyCredential  = Util.createAsymmetricKeyCredential(applicationCredentials.clientId(), applicationCredentials.clientCertificate(), applicationCredentials.clientCertificatePassword());
                context.acquireTokenByAuthorizationCode(
                        authorizationCode,
                        uri,
                        keyCredential,
                        Util.authenticationDelegate(callback));
            });
        } else if (applicationCredentials.clientCertificate() != null) {
            URI uri;
            try {
                uri = new URI(redirectUrl);
            } catch (URISyntaxException use) {
                return Mono.error(use);
            }
            AsymmetricKeyCredential keyCredential = AsymmetricKeyCredential.create(clientId(),
                    Util.privateKeyFromPem(new String(applicationCredentials.clientCertificate())),
                    Util.publicKeyFromPem(new String(applicationCredentials.clientCertificate())));
            authMono = Mono.create(callback -> {
                context.acquireTokenByAuthorizationCode(
                        authorizationCode,
                        uri,
                        keyCredential,
                        resource,
                        Util.authenticationDelegate(callback));
            });
        } else {
            authMono = Mono.error(new AuthenticationException("Please provide either a non-null secret or a non-null certificate."));
        }
        return authMono.doFinally(s -> executor.shutdown());
    }

    // Refresh tokens are currently not used since we don't know if the refresh token has expired
    private Mono<AuthenticationResult> acquireAccessTokenFromRefreshToken(String resource, String refreshToken, boolean isMultipleResourceRefreshToken) {
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Mono<AuthenticationResult> authMono = Mono.defer(() -> {
            AuthenticationContext context;
            try {
                context = new AuthenticationContext(authorityUrl, false, executor);
            } catch (MalformedURLException mue) {
                throw Exceptions.propagate(mue);
            }
            if (proxy() != null) {
                context.setProxy(proxy());
            }
            return Mono.create(callback -> context.acquireTokenByRefreshToken(
                    refreshToken,
                    clientId(),
                    resource,
                    Util.authenticationDelegate(callback)));
        });
        return authMono.doFinally(s -> executor.shutdown());
    }

    /**
     * Specifies the method that should be used to send the resulting token back to your app.
     */
    public enum ResponseMode {

        /**
         * the token is sent as a query parameter.
         */
        QUERY("query"),

        /**
         * the token is sent as part of a form data.
         */
        FORM_DATA("form_data");

        private String value;

        ResponseMode(String value) {
            this.value = value;
        }
    }
}
