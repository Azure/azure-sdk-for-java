// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.cryptography.KeyEncryptionKey;

/**
 * A key client which is used to synchronously encrypt, or wrap, another key.
 */
public final class LocalKeyEncryptionKeyClient implements KeyEncryptionKey {
    private final LocalKeyEncryptionKeyAsyncClient client;

    /**
     * Creates a LocalKeyEncryptionKeyClient for local cryptography operations.
     *
     * @param client The {@link LocalKeyEncryptionKeyClient} that the client routes its request through.
     */
    LocalKeyEncryptionKeyClient(LocalKeyEncryptionKeyAsyncClient client) {
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
    public byte[] wrapKey(String algorithm, byte[] key) {
        return client.wrapKey(algorithm, key).block();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] unwrapKey(String algorithm, byte[] encryptedKey) {
        return client.unwrapKey(algorithm, encryptedKey).block();
    }
}
