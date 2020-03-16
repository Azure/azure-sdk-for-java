// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A credential provider that provides token credentials from the MSAL shared token cache.
 * Requires a username and client Id. If a username is not provided, then the
 * {@link Configuration#PROPERTY_AZURE_USERNAME AZURE_USERNAME} environment variable will be used.
 */
public class SharedTokenCacheCredential implements TokenCredential {
    private final String username;
    private final String clientId;

    private PublicClientApplication pubClient = null;

    /**
     * Creates an instance of the Shared Token Cache Credential Provider.
     *
     * @param username the username of the account for the application
     * @param clientId the client ID of the application
     * @param identityClientOptions the options for configuring the identity client
     */
    SharedTokenCacheCredential(String username, String clientId, IdentityClientOptions identityClientOptions) {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        if (username == null) {
            this.username = configuration.get(Configuration.PROPERTY_AZURE_USERNAME);
        } else {
            this.username = username;
        }

        this.clientId = clientId;
    }

    /**
     * Gets token from shared token cache
     * */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        // Initialize here so that the constructor doesn't throw
        if (pubClient == null) {
            try {
                PersistentTokenCacheAccessAspect accessAspect = new PersistentTokenCacheAccessAspect();
                pubClient = PublicClientApplication.builder(this.clientId)
                    .setTokenCacheAccessAspect(accessAspect)
                    .build();
            } catch (Exception e) {
                return Mono.error(e);
            }
        }

        // find if the Public Client app with the requested username exists
        return Mono.fromFuture(pubClient.getAccounts())
            .flatMap(set -> {
                IAccount requestedAccount;
                Map<String, IAccount> accounts = new HashMap<>(); // home account id -> account

                for (IAccount cached : set) {
                    if (username == null || username.equals(cached.username())) {
                        if (!accounts.containsKey(cached.homeAccountId())) { // only put the first one
                            accounts.put(cached.homeAccountId(), cached);
                        }
                    }
                }

                if (accounts.size() == 0) {
                    if (username == null) {
                        return Mono.error(new RuntimeException("SharedTokenCacheCredential authentication unavailable. No accounts were discovered in the cache."));
                    } else {
                        return Mono.error(new RuntimeException(String.format("SharedTokenCacheCredential authentication unavailable. No account "
                            + "matching the specified %s was found in the cache.", username)));
                    }
                } else if (accounts.size() > 1) {
                    if (username == null) {
                        return Mono.error(new RuntimeException("SharedTokenCacheCredential authentication unavailable. "
                            + "Multiple accounts were found in the cache. Use username and tenant id to disambiguate."));
                    } else {
                        return Mono.error(new RuntimeException(String.format("SharedTokenCacheCredential authentication unavailable. Multiple accounts atching the specified "
                            + " %s were found in the cache.", username)));
                    }
                } else {
                    requestedAccount = accounts.values().iterator().next();
                }

                // if it does, then request the token
                SilentParameters params = SilentParameters.builder(
                    new HashSet<>(request.getScopes()), requestedAccount).build();

                CompletableFuture<IAuthenticationResult> future;
                try {
                    future = pubClient.acquireTokenSilently(params);
                    return Mono.fromFuture(() -> future).map(result ->
                        new AccessToken(result.accessToken(),
                            result.expiresOnDate().toInstant().atOffset(ZoneOffset.UTC)));

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return Mono.error(new RuntimeException("Token was not found"));
                }
            });
    }
}
