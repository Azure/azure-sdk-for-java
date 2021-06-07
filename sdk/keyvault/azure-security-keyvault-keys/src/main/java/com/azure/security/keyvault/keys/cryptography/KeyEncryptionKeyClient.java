// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.http.HttpPipeline;

/**
 * A key client which is used to synchronously wrap or unwrap another key.
 */
@ServiceClient(builder = KeyEncryptionKeyClientBuilder.class)
public final class KeyEncryptionKeyClient implements KeyEncryptionKey {
    private final KeyEncryptionKeyAsyncClient client;

    /**
     * Creates a {@link KeyEncryptionKeyClient} that uses a given {@link HttpPipeline pipeline} to service requests.
     *
     * @param client The {@link KeyEncryptionKeyAsyncClient} that the client routes its request through.
     */
    KeyEncryptionKeyClient(KeyEncryptionKeyAsyncClient client) {
        this.client = client;
    }

    /**
     * Get the identifier of the key to use for cryptography operations.
     *
     * @return The key identifier.
     */
    @Override
    public String getKeyId() {
        return client.getKeyId().block();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] wrapKey(String algorithm, byte[] key) {
        return client.wrapKey(algorithm, key).block();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] unwrapKey(String algorithm, byte[] encryptedKey) {
        return client.unwrapKey(algorithm, encryptedKey).block();
    }

    KeyEncryptionKeyAsyncClient getKeyEncryptionKeyAsyncClient() {
        return client;
    }
}
