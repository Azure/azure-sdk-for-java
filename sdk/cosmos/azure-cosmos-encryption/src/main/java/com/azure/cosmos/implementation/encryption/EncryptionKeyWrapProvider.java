// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

/**
 * TODO: @moderakh look into if this class needs to be async
 * Interface for interacting with a provider that can be used to wrap (encrypt) and unwrap (decrypt) data encryption keys for envelope based encryption.
 * Implementations are expected to ensure that master keys are highly available and protected against accidental deletion.
 * See https://aka.ms/CosmosClientEncryption for more information on client-side encryption support in Azure Cosmos DB.
 */
public interface EncryptionKeyWrapProvider {

    /**
     * Wraps (i.e. encrypts) the provided data encryption key.
     * @param key Data encryption key that needs to be wrapped.
     * @param metadata Metadata for the wrap provider that should be used to wrap / unwrap the key.<
     * @return Wrapped (i.e. encrypted) version of data encryption key passed in possibly with updated metadata.
     */
    EncryptionKeyWrapResult wrapKey(byte[] key, EncryptionKeyWrapMetadata metadata);

    /**
     * Unwraps (i.e. decrypts) the provided wrapped data encryption key.
     * @param wrappedKey Wrapped form of data encryption key that needs to be unwrapped.
     * @param metadata Metadata for the wrap provider that should be used to wrap / unwrap the key.
     * @return unwrapped (i.e. unencrypted) version of data encryption key passed in and how long the raw data encryption key can be cached on the client.
     */
    EncryptionKeyUnwrapResult unwrapKey(byte[] wrappedKey, EncryptionKeyWrapMetadata metadata);
}
