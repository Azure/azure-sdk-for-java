// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.ScopeUtil;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Adapt the legacy token credential {@link AzureTokenCredentials} to {@link TokenCredential}.
 */
public class LegacyTokenCredentialAdapter implements TokenCredential {

    private final AzureTokenCredentials azureTokenCredentials;

    public LegacyTokenCredentialAdapter(AzureTokenCredentials azureTokenCredentials) {
        this.azureTokenCredentials = azureTokenCredentials;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        String resource = ScopeUtil.scopesToResource(request.getScopes());
        try {
            return Mono.just(new AccessToken(azureTokenCredentials.getToken(resource), OffsetDateTime.MAX));
        } catch (IOException e) {
            throw new CredentialUnavailableException(e);
        }
    }

    /**
     * Thrown on failure to obtain a credential for the requested resource. 
     * @author yebronsh
     *
     */
    public static final class CredentialUnavailableException extends RuntimeException {
        /**
         * 
         */
        private static final long serialVersionUID = -5389931452186532039L;

        public CredentialUnavailableException(Throwable cause) {
            super(cause);
        }
    }
}
