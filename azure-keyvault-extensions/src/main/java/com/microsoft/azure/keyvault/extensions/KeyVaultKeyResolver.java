/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.extensions;

import java.security.Provider;

import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import com.microsoft.azure.keyvault.cryptography.SymmetricKey;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.SecretBundle;

/**
 * The key resolver class that handles resolving key id to type {@link IKey} 
 * to be used for cryptography operations.
 */
public class KeyVaultKeyResolver implements IKeyResolver {

    static final Base64 BASE64 = new Base64(-1, null, true);
    
    /**
     * Transforms {@link KeyBundle} to {@link IKey}.
     */
    class FutureKeyFromKey implements Function<KeyBundle, IKey> {

        protected FutureKeyFromKey() {
            super();
        }

        @Override
        public IKey apply(KeyBundle keyBundle) {

            if (keyBundle != null) {
                return new KeyVaultKey(client, keyBundle);
            }

            return null;
        }
    }

    /**
     * Transforms {@link SecretBundle} to {@link IKey}.
     */
    class FutureKeyFromSecret implements Function<SecretBundle, IKey> {

        protected FutureKeyFromSecret() {
            super();
        }

        @Override
        public IKey apply(SecretBundle secretBundle) {

            if (secretBundle != null && secretBundle.contentType().equalsIgnoreCase("application/octet-stream")) {
                byte[] keyBytes = BASE64.decode(secretBundle.value());

                if (keyBytes != null) {
                    return new SymmetricKey(secretBundle.id(), keyBytes, provider);
                }
            }

            return null;
        }
    }

    private final KeyVaultClient client;
    private final Provider       provider;

    /**
     * Constructor.
     * @param client the key vault client
     */
    public KeyVaultKeyResolver(KeyVaultClient client) {
        this.client   = client;
        this.provider = null;
    }
    
    /**
     * Constructor.
     * @param client the key vault client 
     * @param provider the java security provider
     */
    public KeyVaultKeyResolver(KeyVaultClient client, Provider provider) {
        this.client   = client;
        this.provider = provider;
    }

    private ListenableFuture<IKey> resolveKeyFromSecretAsync(String kid) {
        
        ListenableFuture<SecretBundle> futureCall = client.getSecretAsync(kid, null);
        return Futures.transform(futureCall, new FutureKeyFromSecret(), MoreExecutors.directExecutor());
    }

    private ListenableFuture<IKey> resolveKeyFromKeyAsync(String kid) {
        
        ListenableFuture<KeyBundle> futureCall = client.getKeyAsync(kid, null);
        return Futures.transform(futureCall, new FutureKeyFromKey(), MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<IKey> resolveKeyAsync(String kid) {

        if (KeyIdentifier.isKeyIdentifier(kid)) {
            return resolveKeyFromKeyAsync(kid);
        } else if (SecretIdentifier.isSecretIdentifier(kid)) {
            return resolveKeyFromSecretAsync(kid);
        }

        return Futures.immediateFuture(null);
    }

}
