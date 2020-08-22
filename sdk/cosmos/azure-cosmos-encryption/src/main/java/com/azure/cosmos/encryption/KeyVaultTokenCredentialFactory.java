// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.credential.TokenCredential;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Factory Class for acquiring the Token Credentials depending on the Type of Method.
 */
abstract public class KeyVaultTokenCredentialFactory {


    /**
     * Implements an interface to get TokenCredentials.
     * @param keyVaultKeyUri Azure Key-Vault Key URI to acquire a TokenCredentials for
     * @return Mono of TokenCredentials result token credentials
     */
    public abstract Mono<TokenCredential> getTokenCredentialAsync(URI keyVaultKeyUri);
}
