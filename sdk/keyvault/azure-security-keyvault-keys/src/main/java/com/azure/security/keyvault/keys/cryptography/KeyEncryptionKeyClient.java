// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.cryptography.KeyEncryptionKey;

/**
 * A key client which is used to asynchronously encrypt, or wrap, another key.
 */
public final class KeyEncryptionKeyClient implements KeyEncryptionKey {
    private final KeyEncryptionKeyAsyncClient client;

    /**
     * Creates a KeyEncryptionKeyClient that uses {@code pipeline} to service requests
     *
     * @param client The {@link KeyEncryptionKeyClient} that the client routes its request through.
     */
    KeyEncryptionKeyClient(KeyEncryptionKeyAsyncClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
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

    KeyEncryptionKeyAsyncClient getKeyEncryptionKeyAsyncClient() {
        return client;
    }
}
