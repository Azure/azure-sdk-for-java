// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.ChainedTokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.SharedTokenCacheCredential;
import com.azure.identity.VisualStudioCodeCredential;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Creates a credential using environment variables or the shared token cache. It tries to create a valid credential in
 * the following order:
 *
 * <ol>
 * <li>{@link SpringEnvironmentCredential}</li>
 * <li>{@link ManagedIdentityCredential}</li>
 * <li>{@link SharedTokenCacheCredential}</li>
 * <li>{@link IntelliJCredential}</li>
 * <li>{@link VisualStudioCodeCredential}</li>
 * <li>{@link AzureCliCredential}</li>
 * <li>{@link AzurePowerShellCredential}</li>
 * <li>Fails if none of the credentials above could be created.</li>
 * </ol>
 */
public class DefaultSpringCredential implements TokenCredential {

    private final ChainedTokenCredential chainedTokenCredential;
    private final List<TokenCredential> tokenCredentials;

    DefaultSpringCredential(List<TokenCredential> tokenCredentials) {
        this.tokenCredentials = tokenCredentials;
        this.chainedTokenCredential = new ChainedTokenCredentialBuilder().addAll(tokenCredentials).build();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return this.chainedTokenCredential.getToken(request);
    }

    public List<TokenCredential> getTokenCredentials() {
        return Collections.unmodifiableList(this.tokenCredentials);
    }

}
