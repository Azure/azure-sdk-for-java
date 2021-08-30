// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
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
    private static final String TROUBLESHOOT_MESSAGE = "To mitigate this issue, please refer to the troubleshooting "
        + "guidelines here at https://aka.ms/azure-identity-java-default-azure-credential-troubleshoot";
    private final ClientLogger logger = new ClientLogger(getClass());
    private final List<TokenCredential> credentials;
    private final String unavailableError = this.getClass().getSimpleName() + " authentication failed. ---> ";

    /**
     * Create an instance of chained token credential that aggregates a list of token
     * credentials.
     */
    ChainedTokenCredential(List<TokenCredential> credentials) {
        this.credentials = Collections.unmodifiableList(credentials);
    }

    /**
     * Sequentially calls {@link TokenCredential#getToken(TokenRequestContext)} on all the specified credentials,
     * returning the first successfully obtained {@link AccessToken}.
     *
     * This method is called automatically by Azure SDK client libraries.
     * You may call this method directly, but you must also handle token
     * caching and token refreshing.
     *
     * @param request the details of the token request
     * @return a Publisher that emits a single access token
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        List<CredentialUnavailableException> exceptions = new ArrayList<>(4);
        return Flux.fromIterable(credentials)
            .flatMap(p -> p.getToken(request)
                .doOnNext(t -> logger.info("Azure Identity => Attempted credential {} returns a token",
                    p.getClass().getSimpleName()))
                .onErrorResume(Exception.class, t -> {
                    if (!t.getClass().getSimpleName().equals("CredentialUnavailableException")) {
                        return Mono.error(new ClientAuthenticationException(
                            unavailableError + p.getClass().getSimpleName()
                                + " authentication failed. Error Details: " + t.getMessage(),
                            null, t));
                    }
                    exceptions.add((CredentialUnavailableException) t);
                    logger.info("Azure Identity => Attempted credential {} is unavailable.",
                        p.getClass().getSimpleName());
                    return Mono.empty();
                }), 1)
            .next()
            .switchIfEmpty(Mono.defer(() -> {
                // Chain Exceptions.
                CredentialUnavailableException last = exceptions.get(exceptions.size() - 1);
                for (int z = exceptions.size() - 2; z >= 0; z--) {
                    CredentialUnavailableException current = exceptions.get(z);
                    last = new CredentialUnavailableException(current.getMessage() + "\r\n" + last.getMessage()
                        + (z == 0 ? TROUBLESHOOT_MESSAGE : ""));
                }
                return Mono.error(last);
            }));
    }
}
