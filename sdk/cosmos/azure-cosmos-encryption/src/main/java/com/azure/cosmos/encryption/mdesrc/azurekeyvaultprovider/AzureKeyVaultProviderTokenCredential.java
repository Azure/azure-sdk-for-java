/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.mdesrc.azurekeyvaultprovider;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.cosmos.encryption.mdesrc.cryptography.MicrosoftDataEncryptionException;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.SilentParameters;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;


/**
 * An Azure Active Directory credential that acquires a token with a client secret for an Azure Active Directory application.
 */
@Immutable
class AzureKeyVaultProviderTokenCredential implements TokenCredential {
    private final String clientId;
    private final String clientSecret;
    private final KeyVaultAuthenticationCallback authenticationCallback;
    private String authorization;
    private ConfidentialClientApplication confidentialClientApplication;
    private String resource;
    private String scope;

    /**
     * Creates a KeyVaultTokenCredential with the given identity client options.
     *
     * @param clientId
     *        the client ID of the application
     * @param clientSecret
     *        the secret value of the Azure Active Directory application
     * @throws MicrosoftDataEncryptionException
     */
    AzureKeyVaultProviderTokenCredential(String clientId, String clientSecret) throws MicrosoftDataEncryptionException {
        if (null == clientId || clientId.isEmpty()) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Client ID"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs1));
        }

        if (null == clientSecret || clientSecret.isEmpty()) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Client Secret"};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs1));
        }

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authenticationCallback = null;
    }

    /**
     * Creates a KeyVaultTokenCredential with the given identity client options.
     *
     * @param authenticationCallback
     *        The authentication callback that gets invoked when an access token is requested.
     */
    AzureKeyVaultProviderTokenCredential(KeyVaultAuthenticationCallback authenticationCallback) {
        this.authenticationCallback = authenticationCallback;
        this.clientId = null;
        this.clientSecret = null;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (null != authenticationCallback) {
            /*
             * If the callback is not null, invoke the callback to get the token. This gets invoked each time this
             * method is called and will not cache the token. It's the callback's responsibility to return a valid token
             * each time it's invoked.
             */
            String accessToken = authenticationCallback.getAccessToken(this.authorization, this.resource, this.scope);
            return Mono.just(new AccessToken(accessToken, OffsetDateTime.MIN));
        }

        // gets the token from MSAL
        return authenticateWithConfidentialClientCache(request).onErrorResume(t -> Mono.empty())
                .switchIfEmpty(Mono.defer(() -> authenticateWithConfidentialClient(request)));
    }

    /**
     * Sets the authority that will be used for authentication.
     *
     * @param authorization
     *        The name of the authorization.
     * @return The updated {@link AzureKeyVaultProviderTokenCredential} instance.
     */
    AzureKeyVaultProviderTokenCredential setAuthorization(String authorization) {
        if (null != this.authorization && this.authorization.equals(authorization)) {
            return this;
        }
        this.authorization = authorization;
        confidentialClientApplication = getConfidentialClientApplication();
        return this;
    }

    /**
     * Creates an instance of {@link ConfidentialClientApplication} using the provided client id and secret.
     *
     * @return An instance of {@link ConfidentialClientApplication}.
     */
    private ConfidentialClientApplication getConfidentialClientApplication() {
        if (null == clientId) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Client ID"};
            throw new IllegalArgumentException(form.format(msgArgs1), null);
        }

        if (null == authorization) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Authorization"};
            throw new IllegalArgumentException(form.format(msgArgs1), null);
        }

        if (null == clientSecret) {
            MessageFormat form = new MessageFormat(MicrosoftDataEncryptionException.getErrString("R_CannotBeNull"));
            Object[] msgArgs1 = {"Client Secret"};
            throw new IllegalArgumentException(form.format(msgArgs1), null);
        }

        // Create the credential using the MSAL factory method.
        IClientCredential credential;
        credential = ClientCredentialFactory.createFromSecret(clientSecret);
        ConfidentialClientApplication.Builder applicationBuilder = ConfidentialClientApplication.builder(clientId,
                credential);
        try {
            applicationBuilder = applicationBuilder.authority(authorization);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return applicationBuilder.build();
    }

    /**
     * Attempts to get the access token from the client cache if it's not expired. If it's expired this returns an empty
     * response.
     *
     * @param request
     *        The context for requesting the token including the scope.
     * @return The cached access token if it's not expired.
     */
    private Mono<AccessToken> authenticateWithConfidentialClientCache(TokenRequestContext request) {
        return Mono.fromFuture(() -> {
            SilentParameters.SilentParametersBuilder parametersBuilder = SilentParameters
                    .builder(new HashSet<>(request.getScopes()));
            try {
                return confidentialClientApplication.acquireTokenSilently(parametersBuilder.build());
            } catch (MalformedURLException e) {
                return getFailedCompletableFuture(new RuntimeException(e));
            }
        }).map(ar -> new AccessToken(ar.accessToken(),
                OffsetDateTime.ofInstant(ar.expiresOnDate().toInstant(), ZoneOffset.UTC))).filter(t -> !t.isExpired());
    }

    /**
     * If fetching the token resulted in an error, this method returns the error wrapped in a completable future.
     *
     * @param e
     *        The original exception.
     * @return A {@link CompletableFuture} that completes with an error.
     */
    private CompletableFuture<IAuthenticationResult> getFailedCompletableFuture(Exception e) {
        CompletableFuture<IAuthenticationResult> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(e);
        return completableFuture;
    }

    /**
     * Attempts to get the access token from the {@link ConfidentialClientApplication} for the requested scope.
     *
     * @param request
     *        The context for requesting the token that includes the scope.
     * @return The access token.
     */
    private Mono<AccessToken> authenticateWithConfidentialClient(TokenRequestContext request) {
        return Mono
                .fromFuture(() -> confidentialClientApplication
                        .acquireToken(ClientCredentialParameters.builder(new HashSet<>(request.getScopes())).build()))
                .map(ar -> new AccessToken(ar.accessToken(),
                        OffsetDateTime.ofInstant(ar.expiresOnDate().toInstant(), ZoneOffset.UTC)));
    }

    /**
     * Sets the resource name.
     *
     * @param resource
     *        The resource name.
     */
    void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Sets the scope for the access token.
     *
     * @param scope
     *        The scope for the access token.
     */
    void setScope(String scope) {
        this.scope = scope;
    }
}
