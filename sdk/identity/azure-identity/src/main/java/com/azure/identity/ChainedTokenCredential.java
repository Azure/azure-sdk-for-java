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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The ChainedTokenCredential is a convenience credential that allows users to chain together a set of TokenCredential
 * together. The credential executes each credential in the chain sequentially and returns the token from the first
 * credential in the chain that successfully authenticates.
 *
 * <p><strong>Sample: Construct a ChainedTokenCredential.</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ChainedTokenCredential},
 * using the {@link ChainedTokenCredentialBuilder} to configure it. The sample below
 * tries silent username+password login tried first, then interactive browser login as needed
 * (e.g. when 2FA is turned on in the directory). Once this credential is created, it may be passed into the builder
 * of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.chainedtokencredential.construct -->
 * <pre>
 * TokenCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .username&#40;fakeUsernamePlaceholder&#41;
 *     .password&#40;fakePasswordPlaceholder&#41;
 *     .build&#40;&#41;;
 * TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41;
 *     .port&#40;8765&#41;
 *     .build&#40;&#41;;
 * TokenCredential credential = new ChainedTokenCredentialBuilder&#40;&#41;
 *     .addLast&#40;usernamePasswordCredential&#41;
 *     .addLast&#40;interactiveBrowserCredential&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.chainedtokencredential.construct -->
 *
 * @see com.azure.identity
 * @see ChainedTokenCredentialBuilder
 */
@Immutable
public class ChainedTokenCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ChainedTokenCredential.class);
    private final List<TokenCredential> credentials;
    private final String unavailableError = this.getClass().getSimpleName() + " authentication failed. ---> ";
    private final AtomicReference<TokenCredential> selectedCredential;
    private boolean useCachedWorkingCredential = false;

    /**
     * Create an instance of chained token credential that aggregates a list of token
     * credentials.
     */
    ChainedTokenCredential(List<TokenCredential> credentials) {
        this.credentials = Collections.unmodifiableList(credentials);
        selectedCredential = new AtomicReference<>();
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
        Mono<AccessToken> accessTokenMono;
        if (selectedCredential.get() != null && useCachedWorkingCredential) {
            accessTokenMono =  Mono.defer(() -> selectedCredential.get().getToken(request)
                .doOnNext(t -> logTokenMessage("Azure Identity => Returning token from cached credential {}",
                    selectedCredential.get()))
                .onErrorResume(Exception.class, handleExceptionAsync(exceptions,
                    selectedCredential.get(), "Azure Identity => Cached credential {} is unavailable.")));
        } else {
            accessTokenMono = Flux.fromIterable(credentials)
                .flatMap(p -> p.getToken(request)
                    .doOnNext(t -> {
                        logTokenMessage("Azure Identity => Attempted credential {} returns a token", p);
                        selectedCredential.set(p);
                    }).onErrorResume(Exception.class, handleExceptionAsync(exceptions, p,
                        "Azure Identity => Attempted credential {} is unavailable.")), 1)
                .next();
        }
        return accessTokenMono.switchIfEmpty(Mono.defer(() -> {
            // Chain Exceptions.
            CredentialUnavailableException last = exceptions.get(exceptions.size() - 1);
            for (int z = exceptions.size() - 2; z >= 0; z--) {
                CredentialUnavailableException current = exceptions.get(z);
                last = new CredentialUnavailableException(current.getMessage() + "\r\n" + last.getMessage()
                    + (z == 0 ? "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                    + "https://aka.ms/azure-identity-java-default-azure-credential-troubleshoot" : ""));
            }
            return Mono.error(last);
        }));
    }

    private Function<Exception, Mono<? extends AccessToken>> handleExceptionAsync(List<CredentialUnavailableException> exceptions,
                                                                                  TokenCredential p, String logMessage) {
        return t -> {
            if (!t.getClass().getSimpleName().equals("CredentialUnavailableException")) {
                return Mono.error(new ClientAuthenticationException(
                    getCredUnavailableMessage(p, t),
                    null, t));
            }
            exceptions.add((CredentialUnavailableException) t);
            logTokenMessage(logMessage, p);
            return Mono.empty();
        };
    }


    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        List<CredentialUnavailableException> exceptions = new ArrayList<>(4);

        if (selectedCredential.get() != null && useCachedWorkingCredential) {
            try {
                AccessToken accessToken = selectedCredential.get().getTokenSync(request);
                logTokenMessage("Azure Identity => Returning token from cached credential {}", selectedCredential.get());
                return accessToken;
            } catch (Exception e) {
                handleExceptionSync(e, selectedCredential.get(), exceptions,
                    "Azure Identity => Cached credential {} is unavailable.", selectedCredential.get());
            }
        } else {
            for (TokenCredential credential : credentials) {
                try {
                    AccessToken accessToken = credential.getTokenSync(request);
                    logTokenMessage("Azure Identity => Attempted credential {} returns a token", credential);
                    selectedCredential.set(credential);
                    return accessToken;

                } catch (Exception e) {
                    handleExceptionSync(e, credential, exceptions,
                        "Azure Identity => Attempted credential {} is unavailable.", credential);
                }
            }
        }

        CredentialUnavailableException last = exceptions.get(exceptions.size() - 1);
        for (int z = exceptions.size() - 2; z >= 0; z--) {
            CredentialUnavailableException current = exceptions.get(z);
            last = new CredentialUnavailableException(current.getMessage() + "\r\n" + last.getMessage()
                + (z == 0 ? "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                + "https://aka.ms/azure-identity-java-default-azure-credential-troubleshoot"
                : ""));
        }
        throw last;
    }

    private void logTokenMessage(String format, TokenCredential selectedCredential) {
        LOGGER.info(format,
            selectedCredential.getClass().getSimpleName());
    }

    private String getCredUnavailableMessage(TokenCredential p, Exception t) {
        return unavailableError + p.getClass().getSimpleName()
            + " authentication failed. Error Details: " + t.getMessage();
    }

    private void handleExceptionSync(Exception e, TokenCredential selectedCredential,
                                     List<CredentialUnavailableException> exceptions, String logMessage,
                                     TokenCredential selectedCredential1) {
        if (e.getClass() != CredentialUnavailableException.class) {
            throw new ClientAuthenticationException(
                getCredUnavailableMessage(selectedCredential, e),
                null, e);
        } else {
            if (e instanceof CredentialUnavailableException) {
                exceptions.add((CredentialUnavailableException) e);
            }
        }
        logTokenMessage(logMessage, selectedCredential1);
    }

    WorkloadIdentityCredential getWorkloadIdentityCredentialIfPresent() {
        List<TokenCredential> tokenCredentials = this.credentials
            .stream().filter(tokenCredential -> tokenCredential instanceof WorkloadIdentityCredential)
            .collect(Collectors.toList());
        if (tokenCredentials.size() == 1) {
            return (WorkloadIdentityCredential) tokenCredentials.get(0);
        } else {
            return null;
        }
    }

    void enableUseCachedWorkingCredential() {
        this.useCachedWorkingCredential = true;
    }
}
