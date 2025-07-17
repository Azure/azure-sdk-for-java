// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * <p>ChainedTokenCredential allows you to chain together a set of TokenCredential instances. Each credential in the chain is attempted
 * sequentially. The token from the first credential that successfully authenticates is returned. For more information, see
 * <a href="https://aka.ms/azsdk/java/identity/credential-chains#chainedtokencredential-overview">ChainedTokenCredential overview</a>.</p>
 *
 * <p><strong>Sample: Construct a ChainedTokenCredential.</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ChainedTokenCredential},
 * using the {@link ChainedTokenCredentialBuilder} to configure it. The sample below
 * tries silent username+password login tried first, then interactive browser login as needed
 * (e.g. when 2FA is turned on in the directory). Once this credential is created, it may be passed into the builder
 * of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder&#40;&#41;.clientId&#40;clientId&#41;
 *     .username&#40;fakeUsernamePlaceholder&#41;
 *     .password&#40;fakePasswordPlaceholder&#41;
 *     .build&#40;&#41;;
 * TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;.clientId&#40;clientId&#41;
 *     .port&#40;8765&#41;
 *     .build&#40;&#41;;
 * TokenCredential credential = new ChainedTokenCredentialBuilder&#40;&#41;.addLast&#40;usernamePasswordCredential&#41;
 *     .addLast&#40;interactiveBrowserCredential&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see ChainedTokenCredentialBuilder
 */
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

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        List<CredentialUnavailableException> exceptions = new ArrayList<>(4);

        if (selectedCredential.get() != null && useCachedWorkingCredential) {
            try {
                AccessToken accessToken = selectedCredential.get().getToken(request);
                LOGGER.atInfo()
                    .addKeyValue("credentialType", selectedCredential.get().getClass().getCanonicalName())
                    .log("Azure Identity => Returning token from cached credential.");
                return accessToken;
            } catch (RuntimeException e) {
                handleException(e, selectedCredential.get(), exceptions,
                    "Azure Identity => Cached credential is unavailable.");
            }
        } else {
            for (TokenCredential credential : credentials) {
                try {
                    AccessToken accessToken = credential.getToken(request);
                    LOGGER.atInfo()
                        .addKeyValue("credentialType", selectedCredential.get().getClass().getCanonicalName())
                        .log("Azure Identity => Attempted credential returns a token.");
                    selectedCredential.set(credential);
                    return accessToken;

                } catch (RuntimeException e) {
                    handleException(e, credential, exceptions,
                        "Azure Identity => Attempted credential is unavailable.");
                }
            }
        }

        String lastMessage = exceptions.get(exceptions.size() - 1).getMessage();
        for (int z = exceptions.size() - 2; z >= 0; z--) {
            CredentialUnavailableException current = exceptions.get(z);
            lastMessage = current.getMessage() + "\r\n" + lastMessage
                + (z == 0
                    ? "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                        + "https://aka.ms/azure-identity-java-default-azure-credential-troubleshoot"
                    : "");
        }
        throw LOGGER.throwableAtError().log(lastMessage, CredentialUnavailableException::new);
    }

    private void handleException(Exception e, TokenCredential selectedCredential,
        List<CredentialUnavailableException> exceptions, String logMessage) {
        if (e.getClass() != CredentialUnavailableException.class) {

            throw LOGGER.throwableAtError()
                .addKeyValue("credentialType", selectedCredential.getClass().getCanonicalName())
                .log(unavailableError, e, CredentialAuthenticationException::new);
        }

        exceptions.add((CredentialUnavailableException) e);
        LOGGER.atInfo().addKeyValue("credentialType", selectedCredential.getClass().getCanonicalName()).log(logMessage);
    }

    WorkloadIdentityCredential getWorkloadIdentityCredentialIfPresent() {
        List<TokenCredential> tokenCredentials = this.credentials.stream()
            .filter(tokenCredential -> tokenCredential instanceof WorkloadIdentityCredential)
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
