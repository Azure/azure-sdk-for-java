// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.keyprovider;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.encryption.implementation.mdesrc.azurekeyvaultprovider.AzureKeyVaultKeyStoreProvider;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.KeyEncryptionKeyAlgorithm;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;
import com.azure.cosmos.encryption.models.KeyEncryptionAlgorithm;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

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
public final class AzureKeyVaultKeyWrapProvider extends EncryptionKeyWrapProvider {
    private AzureKeyVaultKeyStoreProvider azureKeyVaultKeyStoreProvider;
    private final static ImplementationBridgeHelpers.CosmosExceptionHelper.CosmosExceptionAccessor cosmosExceptionAccessor =
        ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor();

    /**
     * Constructs an AzureKeyVaultKeyWrapProvider using the provided TokenCredential to authenticate to Azure AD. This
     * is used by the KeyVault client at runtime to authenticate to Azure Key Vault.
     *
     * @param tokenCredential
     *        The TokenCredential to use to authenticate to Azure Key Vault.
     */
    public AzureKeyVaultKeyWrapProvider(TokenCredential tokenCredential) {
        try {
            this.azureKeyVaultKeyStoreProvider = new AzureKeyVaultKeyStoreProvider(tokenCredential);
        } catch (MicrosoftDataEncryptionException ex) {
            throw cosmosExceptionAccessor.createCosmosException(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, ex);
        }
    }

    /**
     * Gets the name of the key vault provider.
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
            if (!KeyEncryptionAlgorithm.RSA_OAEP.getName().equals(cosmosKeyEncryptionKeyAlgorithm)) {
                throw new IllegalArgumentException("The specified KeyEncryptionAlgorithm is not supported. Please " +
                    "refer to https://aka.ms/CosmosClientEncryption for more details. ");
            }
            return this.azureKeyVaultKeyStoreProvider.unwrapKey(encryptionKeyId, KeyEncryptionKeyAlgorithm.RSA_OAEP,
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
            if (!KeyEncryptionAlgorithm.RSA_OAEP.getName().equals(cosmosKeyEncryptionKeyAlgorithm)) {
                throw new IllegalArgumentException("The specified KeyEncryptionAlgorithm is not supported. Please " +
                    "refer to https://aka.ms/CosmosClientEncryption for more details. ");
            }
            return this.azureKeyVaultKeyStoreProvider.wrapKey(encryptionKeyId, KeyEncryptionKeyAlgorithm.RSA_OAEP, key);
        } catch (MicrosoftDataEncryptionException ex) {
            throw cosmosExceptionAccessor.createCosmosException(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, ex);
        }
    }
}
