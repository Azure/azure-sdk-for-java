// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.encryption.KeyVaultTokenCredentialFactory;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Initializes a new instance of the {@link UserProvidedTokenCredentialFactory} class. Class implements Client
 * Certificate Based TokenCredential. Inits the TokenCredentials with user provided TokenCredential required for
 * accessing Key Vault services.
 */
public class UserProvidedTokenCredentialFactory extends KeyVaultTokenCredentialFactory {

    private final TokenCredential tokenCredential;

    /**
     * Takes a TokenCredentials which can be used to access keyVault services.
     *
     * @param tokenCredential TokenCredentials.
     */
    public UserProvidedTokenCredentialFactory(TokenCredential tokenCredential) {
        if (tokenCredential != null) {
            this.tokenCredential = tokenCredential;
        } else {
            throw new IllegalArgumentException("UserProvidedTokenCredentialFactory: Invalid null TokenCredentials "
                + "Passed");
        }
    }

    /**
     * Get the TokenCredentials for the Given KeyVaultKey URI
     *
     * @param keyVaultKeyUri Key-Vault Key  Uri
     * @return Mono of TokenCredential User passed TokenCredential.
     */
    @Override
    public Mono<TokenCredential> getTokenCredential(URI keyVaultKeyUri) {
        return Mono.just(this.tokenCredential);
    }
}
