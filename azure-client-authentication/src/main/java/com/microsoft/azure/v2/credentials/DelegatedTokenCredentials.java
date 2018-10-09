/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.rest.v2.annotations.Beta;
import io.reactivex.Completable;
import io.reactivex.Single;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
    public synchronized Single<String> getToken(String resource) {
        // Find exact match for the resource
        AuthenticationResult authenticationResult = tokens.get(resource);
        // Return if found and not expired
        if (authenticationResult != null && authenticationResult.getExpiresOnDate().after(new Date())) {
            return Single.just(authenticationResult.getAccessToken());
        }
        // If found then refresh
        boolean shouldRefresh = authenticationResult != null;
        // If not found for the resource, but is MRRT then also refresh
        if (authenticationResult == null && !tokens.isEmpty()) {
            authenticationResult = new ArrayList<>(tokens.values()).get(0);
            shouldRefresh = authenticationResult.isMultipleResourceRefreshToken();
        }
        Completable task = Completable.complete();
        // Refresh
        if (shouldRefresh) {
            task = task.andThen(acquireAccessTokenFromRefreshToken(resource, authenticationResult.getRefreshToken(), authenticationResult.isMultipleResourceRefreshToken())
                    .flatMapCompletable(ar -> Completable.fromAction(() -> tokens.put(resource, ar))));
        }
        // If refresh fails or not refreshable, acquire new token
        task = task.onErrorResumeNext(t -> acquireNewAccessToken(resource)
                .flatMapCompletable(ar -> Completable.fromAction(() -> tokens.put(resource, ar))));
        return task.andThen(Single.just(tokens.get(resource).getAccessToken()));
    }

    Single<AuthenticationResult> acquireNewAccessToken(String resource) throws IOException {
        if (authorizationCode == null) {
            throw new IllegalArgumentException("You must acquire an authorization code by redirecting to the authentication URL");
        }
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context = new AuthenticationContext(authorityUrl, false, executor);
        if (proxy() != null) {
            context.setProxy(proxy());
        }
        return Single.defer(() -> {
            if (applicationCredentials.clientSecret() != null) {
                return Single.fromFuture(context.acquireTokenByAuthorizationCode(
                        authorizationCode,
                        new URI(redirectUrl),
                        new ClientCredential(applicationCredentials.clientId(), applicationCredentials.clientSecret()),
                        resource, null));
            } else if (applicationCredentials.clientCertificate() != null && applicationCredentials.clientCertificatePassword() != null) {
                return Single.fromFuture(context.acquireTokenByAuthorizationCode(
                        authorizationCode,
                        new URI(redirectUrl),
                        AsymmetricKeyCredential.create(
                                applicationCredentials.clientId(),
                                new ByteArrayInputStream(applicationCredentials.clientCertificate()),
                                applicationCredentials.clientCertificatePassword()),
                        resource,
                        null));
            } else if (applicationCredentials.clientCertificate() != null) {
                return Single.fromFuture(context.acquireTokenByAuthorizationCode(
                        authorizationCode,
                        new URI(redirectUrl),
                        AsymmetricKeyCredential.create(
                                clientId(),
                                ApplicationTokenCredentials.privateKeyFromPem(new String(applicationCredentials.clientCertificate())),
                                ApplicationTokenCredentials.publicKeyFromPem(new String(applicationCredentials.clientCertificate()))),
                        resource,
                        null));
            }
            throw new AuthenticationException("Please provide either a non-null secret or a non-null certificate.");
        }).doFinally(executor::shutdown);
    }

    // Refresh tokens are currently not used since we don't know if the refresh token has expired
    private Single<AuthenticationResult> acquireAccessTokenFromRefreshToken(String resource, String refreshToken, boolean isMultipleResourceRefreshToken) {
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return Single.defer(() -> {
            AuthenticationContext context = new AuthenticationContext(authorityUrl, false, executor);
            if (proxy() != null) {
                context.setProxy(proxy());
            }
            return Single.fromFuture(context.acquireTokenByRefreshToken(refreshToken, clientId(), resource, null));
        }).doFinally(executor::shutdown);
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
