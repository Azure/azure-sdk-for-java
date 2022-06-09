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
 * <!-- src_embed com.azure.identity.credential.chainedtokencredential.construct -->
 * <pre>
 * UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .username&#40;username&#41;
 *     .password&#40;password&#41;
 *     .build&#40;&#41;;
 * InteractiveBrowserCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .port&#40;8765&#41;
 *     .build&#40;&#41;;
 * ChainedTokenCredential credential = new ChainedTokenCredentialBuilder&#40;&#41;
 *     .addLast&#40;usernamePasswordCredential&#41;
 *     .addLast&#40;interactiveBrowserCredential&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.chainedtokencredential.construct -->
 */
@Immutable
public class ChainedTokenCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ChainedTokenCredential.class);
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
                .doOnNext(t -> LOGGER.info("Azure Identity => Attempted credential {} returns a token",
                    p.getClass().getSimpleName()))
                .onErrorResume(Exception.class, t -> {
                    if (!t.getClass().getSimpleName().equals("CredentialUnavailableException")) {
                        return Mono.error(new ClientAuthenticationException(
                            unavailableError + p.getClass().getSimpleName()
                                + " authentication failed. Error Details: " + t.getMessage(),
                            null, t));
                    }
                    exceptions.add((CredentialUnavailableException) t);
                    LOGGER.info("Azure Identity => Attempted credential {} is unavailable.",
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
                        + (z == 0 ? "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                            + "https://aka.ms/azure-identity-java-default-azure-credential-troubleshoot"
                            : ""));
                }
                return Mono.error(last);
            }));
    }
}
