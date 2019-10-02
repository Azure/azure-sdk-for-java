// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.credentials.TokenRequest;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.msalextensions.PersistentTokenCacheAccessAspect;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

/**
 * A credential provider that provides token credentials from the MSAL shared token cache.
 * Requires a username and client ID. If a username is not provided, then the AZURE_USERNAME
 * environment variable will be used
 */
public class SharedTokenCacheCredential implements TokenCredential {
    private final String username;
    private final String clientID;
    private final Configuration configuration;

    private PublicClientApplication pubClient;

    /**
     * Creates an instance of the Shared Token Cache Credential Provider.
     *
     * @param username the username of the account for the application
     * @param clientID the client ID of the application
     * @param identityClientOptions the options for configuring the identity client
     */
    SharedTokenCacheCredential(String username, String clientID, IdentityClientOptions identityClientOptions) {
        this.configuration = Configuration.getGlobalConfiguration().clone();

        if (username == null) {
            this.username = configuration.get(Configuration.PROPERTY_AZURE_USERNAME);
        } else {
            this.username = username;
        }

        this.clientID = clientID;
    }

    /**
     * Gets token from shared token cache
     * */
    @Override
    public Mono<AccessToken> getToken(TokenRequest request) {
        // Initialize here so that the constructor doesn't throw
        if (pubClient == null) {
            try {
                PersistentTokenCacheAccessAspect accessAspect = new PersistentTokenCacheAccessAspect();
                pubClient = PublicClientApplication.builder(this.clientID)
                    .setTokenCacheAccessAspect(accessAspect)
                    .build();
            } catch (Exception e) {
                return Mono.error(e);
            }
        }

        IAccount requestedAccount = null;

        // find if the Public Client app with the requested username exists
        Collection<IAccount> accounts = pubClient.getAccounts().join();
        Iterator<IAccount> iter = accounts.iterator();


        if (username == null && iter.hasNext()) {
            requestedAccount = iter.next();
        } else {
            while (iter.hasNext()) {
                IAccount account = iter.next();
                if (account.username().equals(username)) {
                    requestedAccount = account;
                    break;
                }
            }
        }

        if (requestedAccount == null) {
            return Mono.error(new RuntimeException("Requested account was not found"));
        }

        // if it does, then request the token
        SilentParameters params = SilentParameters.builder(
            new HashSet<>(request.getScopes()), requestedAccount).build();

        CompletableFuture<IAuthenticationResult> future;
        try {
            future = pubClient.acquireTokenSilently(params);
            return Mono.fromFuture(() -> future).map(result ->
                new AccessToken(result.accessToken(), result.expiresOnDate().toInstant().atOffset(ZoneOffset.UTC)));

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Mono.error(new RuntimeException("Token was not found"));
        }
    }
}
