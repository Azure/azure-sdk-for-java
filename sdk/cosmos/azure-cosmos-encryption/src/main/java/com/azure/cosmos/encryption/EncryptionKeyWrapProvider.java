// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

/**
 * Interface for interacting with a provider that can be used to wrap (encrypt) and unwrap (decrypt) data encryption
 * keys for envelope based encryption. Implementations are expected to ensure that master keys are highly available and
 * protected against accidental deletion. See https://aka.ms/CosmosClientEncryption for more information on client-side
 * encryption support in Azure Cosmos DB.
 *
 * <p>
 * Provides functionality to wrap (encrypt) and unwrap (decrypt) data encryption keys using master keys stored in
 * Azure Key Vault.
 * Please see https://docs.microsoft.com/en-us/rest/api/azure/index#register-your-client-application-with-azure-ad
 * for details on registering your application with Azure AD.
 * The registered application must have the keys/readKey, keys/wrapKey and keys/unwrapKey permissions on the
 * Azure Key Vaults that will be used for wrapping and unwrapping data encryption keys
 * Please see https://docs.microsoft.com/en-us/azure/key-vault/about-keys-secrets-and-certificates#key-access-control for details
 * on this.
 * Azure key vaults used with client side encryption for Cosmos DB need to have soft delete and purge protection enabled -
 * Please see https://docs.microsoft.com/en-us/azure/key-vault/key-vault-ovw-soft-delete for details regarding the
 * same.
 * </p>
 */
public interface EncryptionKeyWrapProvider {

    /**
     * Wraps (i.e. encrypts) the provided data encryption key.
     *
     * @param key Data encryption key that needs to be wrapped.
     * @param metadata Metadata for the wrap provider that should be used to wrap / unwrap the key.
     * @return Wrapped (i.e. encrypted) version of data encryption key passed in possibly with updated metadata.
     */
    EncryptionKeyWrapResult wrapKey(byte[] key, EncryptionKeyWrapMetadata metadata);

    /**
     * Unwraps (i.e. decrypts) the provided wrapped data encryption key.
     *
     * @param wrappedKey Wrapped form of data encryption key that needs to be unwrapped.
     * @param metadata Metadata for the wrap provider that should be used to wrap / unwrap the key.
     * @return unwrapped (i.e. unencrypted) version of data encryption key passed in and how long the raw data
     * encryption key can be cached on the client.
     */
    EncryptionKeyUnwrapResult unwrapKey(byte[] wrappedKey, EncryptionKeyWrapMetadata metadata);
}
// TODO: @moderakh look into if this class needs to be async
