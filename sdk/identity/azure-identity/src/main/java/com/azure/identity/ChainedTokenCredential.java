// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A token credential provider that can provide a credential from a list of providers.
 *
 * <p><strong>Sample: Construct a ChainedTokenCredential with silent username+password login tried first, then
 * interactive browser login as needed (e.g. when 2FA is turned on in the directory).</strong></p>
 * {@codesnippet com.azure.identity.credential.chainedtokencredential.construct}
 */
@Immutable
public class ChainedTokenCredential implements TokenCredential {
    private final Deque<TokenCredential> credentials;
    private final String unavailableError = this.getClass().getSimpleName() + " authentication failed. ---> ";


    /**
     * Create an instance of chained token credential that aggregates a list of token
     * credentials.
     */
    ChainedTokenCredential(Deque<TokenCredential> credentials) {
        this.credentials = credentials;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        List<CredentialUnavailableException> exceptions = new ArrayList<>(4);
        return Flux.fromIterable(credentials)
                   .flatMap(p -> p.getToken(request).onErrorResume(Exception.class, t -> {
                       if (!t.getClass().getSimpleName().equals("CredentialUnavailableException")) {
                           return Mono.error(new ClientAuthenticationException(
                            unavailableError + p.getClass().getSimpleName()
                            + " authentication failed. Error Details: " + t.getMessage(),
                            null, t));
                       }
                       exceptions.add((CredentialUnavailableException) t);
                       return Mono.empty();
                   }), 1)
                   .next()
                   .switchIfEmpty(Mono.defer(() -> {
                       // Chain Exceptions.
                       CredentialUnavailableException last = exceptions.get(exceptions.size() - 1);
                       for (int z = exceptions.size() - 2; z >= 0; z--) {
                           CredentialUnavailableException current = exceptions.get(z);
                           last = new CredentialUnavailableException(current.getMessage() + "\r\n" + last.getMessage(),
                                last.getCause());
                       }
                       return Mono.error(last);
                   }));
    }
}
