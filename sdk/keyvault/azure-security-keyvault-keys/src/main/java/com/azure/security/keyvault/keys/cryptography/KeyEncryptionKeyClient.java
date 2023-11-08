// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.http.HttpPipeline;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.models.JsonWebKey;

/**
 * A key client which is used to synchronously wrap or unwrap another key.
 */
@ServiceClient(builder = KeyEncryptionKeyClientBuilder.class)
public final class KeyEncryptionKeyClient extends CryptographyClient implements KeyEncryptionKey {

    /**
     * Creates a {@link KeyEncryptionKeyClient} that uses a given {@link HttpPipeline pipeline} to service requests.
     *
     * @param keyId The identifier of the key to use for cryptography operations.
     * @param pipeline The {@link HttpPipeline} that the HTTP requests and responses flow through.
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     */
    KeyEncryptionKeyClient(String keyId, HttpPipeline pipeline, CryptographyServiceVersion version) {
        super(keyId, pipeline, version);
    }

    /**
     * Creates a {@link KeyEncryptionKeyClient} that uses {@code pipeline} to service requests.
     *
     * @param jsonWebKey The {@link JsonWebKey} to use for local cryptography operations.
     */
    KeyEncryptionKeyClient(JsonWebKey jsonWebKey) {
        super(jsonWebKey);
    }

    /**
     * Get the identifier of the key to use for cryptography operations.
     *
     * @return The key identifier.
     */
    @Override
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] wrapKey(String algorithm, byte[] key) {
        KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);

        return wrapKey(wrapAlgorithm, key).getEncryptedKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] unwrapKey(String algorithm, byte[] encryptedKey) {
        KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.fromString(algorithm);

        return unwrapKey(wrapAlgorithm, encryptedKey).getKey();
    }
}
