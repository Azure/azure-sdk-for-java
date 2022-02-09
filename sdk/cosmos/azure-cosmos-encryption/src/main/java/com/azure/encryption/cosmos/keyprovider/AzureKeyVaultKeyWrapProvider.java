// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.encryption.cosmos.keyprovider;

import com.azure.core.credential.TokenCredential;
import com.azure.encryption.cosmos.models.KeyEncryptionKeyAlgorithm;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.microsoft.data.encryption.AzureKeyVaultKeyStoreProvider.AzureKeyVaultKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;

/**
 * Provides an implementation for an Azure key store provider. A DEK encrypted with a key store provider
 * should be decryptable by this provider and vice versa.
 * <p>
 * Envelope Format for the encrypted data encryption key: version + keyPathLength + ciphertextLength + keyPath +
 * ciphertext + signature.
 * <p>
 * version: A single byte indicating the format version.
 * <p>
 * keyPathLength: Length of the keyPath.
 * <p>
 * ciphertextLength: ciphertext length.
 * <p>
 * keyPath: keyPath used to encrypt the data encryption key. This is only used for troubleshooting purposes,
 * and is not verified during decryption.
 * <p>
 * ciphertext: Encrypted data encryption key.
 * <p>
 * signature: Signature of the entire byte array. Signature is validated before decrypting the data encryption key.
 */
public class AzureKeyVaultKeyWrapProvider extends EncryptionKeyWrapProvider {
    private AzureKeyVaultKeyStoreProvider azureKeyVaultKeyStoreProvider;
    private final static ImplementationBridgeHelpers.CosmosExceptionHelper.CosmosExceptionAccessor cosmosExceptionAccessor =
        ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor();

    public AzureKeyVaultKeyWrapProvider(TokenCredential tokenCredential) {
        try {
            this.azureKeyVaultKeyStoreProvider = new AzureKeyVaultKeyStoreProvider(tokenCredential);
        } catch (MicrosoftDataEncryptionException ex) {
            throw cosmosExceptionAccessor.createCosmosException(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, ex);
        }
    }

    /**
     * Getter for name of the key vault provider.
     *
     * @return provider name
     */
    @Override
    public String getProviderName() {
        return this.azureKeyVaultKeyStoreProvider.getProviderName();
    }

    /**
     * Decrypts an encrypted DEK with the RSA encryption algorithm using the asymmetric key specified by the key path
     *
     * @param encryptionKeyId                 - Complete path of an asymmetric key in Azure Key Vault
     * @param cosmosKeyEncryptionKeyAlgorithm - Asymmetric Key Encryption Algorithm
     * @param encryptedKey                    - Encrypted Data Encryption Key
     * @return Plain text data encryption key
     */
    @Override
    public byte[] unwrapKey(String encryptionKeyId, String cosmosKeyEncryptionKeyAlgorithm, byte[] encryptedKey) {
        try {
            if (!KeyEncryptionKeyAlgorithm.RSA_OAEP.equals(cosmosKeyEncryptionKeyAlgorithm)) {
                throw new IllegalArgumentException("The specified KeyEncryptionAlgorithm is not supported. Please " +
                    "refer to https://aka.ms/CosmosClientEncryption for more details. ");
            }
            return this.azureKeyVaultKeyStoreProvider.unwrapKey(encryptionKeyId, com.microsoft.data.encryption.cryptography.KeyEncryptionKeyAlgorithm.RSA_OAEP,
                encryptedKey);
        } catch (MicrosoftDataEncryptionException ex) {
            throw cosmosExceptionAccessor.createCosmosException(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, ex);
        }
    }

    /**
     * Encrypts a DEK with the RSA encryption algorithm using the asymmetric key specified by the key path.
     *
     * @param encryptionKeyId                 - Complete path of an asymmetric key in Azure Key Vault
     * @param cosmosKeyEncryptionKeyAlgorithm - Asymmetric Key Encryption Algorithm
     * @param key                             - Plain text data encryption key
     * @return Encrypted data encryption key
     */
    @Override
    public byte[] wrapKey(String encryptionKeyId, String cosmosKeyEncryptionKeyAlgorithm, byte[] key) {
        try {
            if (!KeyEncryptionKeyAlgorithm.RSA_OAEP.equals(cosmosKeyEncryptionKeyAlgorithm)) {
                throw new IllegalArgumentException("The specified KeyEncryptionAlgorithm is not supported. Please " +
                    "refer to https://aka.ms/CosmosClientEncryption for more details. ");
            }
            return this.azureKeyVaultKeyStoreProvider.wrapKey(encryptionKeyId, com.microsoft.data.encryption.cryptography.KeyEncryptionKeyAlgorithm.RSA_OAEP, key);
        } catch (MicrosoftDataEncryptionException ex) {
            throw cosmosExceptionAccessor.createCosmosException(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, ex);
        }
    }

    public AzureKeyVaultKeyStoreProvider getAzureKeyVaultKeyStoreProvider() {
        return azureKeyVaultKeyStoreProvider;
    }
}
