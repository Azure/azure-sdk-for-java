// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClientOptions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Creates a credential using environment variables or the shared token cache. It tries to create a valid credential in
 * the following order:
 *
 * <ol>
 * <li>{@link EnvironmentCredential}</li>
 * <li>{@link ManagedIdentityCredential}</li>
 * <li>{@link SharedTokenCacheCredential}</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 */
@Immutable
public final class DefaultAzureCredential extends ChainedTokenCredential {

    /**
     * Creates default DefaultAzureCredential instance to use. This will use AZURE_CLIENT_ID,
     * AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables to create a
     * ClientSecretCredential.
     *
     * If these environment variables are not available, then this will use the Shared MSAL
     * token cache.
     *
     * @param identityClientOptions the options to configure the IdentityClient
     */
    DefaultAzureCredential(IdentityClientOptions identityClientOptions) {
        super(new ArrayDeque<>(Arrays.asList(new EnvironmentCredential(identityClientOptions),
            new ManagedIdentityCredential(null, identityClientOptions),
            new SharedTokenCacheCredential(null, "04b07795-8ddb-461a-bbee-02f9e1bf7b46",
                identityClientOptions))));
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        final AtomicReference<Throwable> cause = new AtomicReference<>();
        StringBuilder errorMsg = new StringBuilder();
        return Flux.fromIterable(credentials).flatMap(p -> p.getToken(request).onErrorResume(t -> {
            System.out.println(t.getMessage()+"aaaaaaaaaaaaaaaaaaa");
            if (t.getMessage() != null && !t.getMessage().contains("authentication unavailable")) {
                if (cause.get() != null) {
                    t=new Throwable(t.getMessage(),cause.get());
                }
                cause.set(t);
            }
            errorMsg.append(" ").append(t.getMessage());
            return Mono.empty();
        }))
            .next()
            .switchIfEmpty(Mono.defer(() -> { 
                if(cause.get() == null){
                   return Mono.error(new RuntimeException("DefaultAzureCredential failed to retrieve a token from the included credentials.("+errorMsg.toString()+" )"));
                }else{
                    return Mono.error(new RuntimeException("DefaultAzureCredential authentication failed. -> "+cause.get().getMessage(),cause.get().getCause()));
                }
            }));
    }
}
