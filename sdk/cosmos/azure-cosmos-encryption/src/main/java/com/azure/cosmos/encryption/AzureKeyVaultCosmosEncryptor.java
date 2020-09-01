// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.encryption.CosmosDataEncryptionKeyProvider;
import com.azure.cosmos.implementation.encryption.CosmosEncryptor;
import com.azure.cosmos.implementation.encryption.DataEncryptionKeyContainer;
import reactor.core.publisher.Mono;

/**
 * Provides the default implementation for client-side encryption for Cosmos DB. Azure Key Vault has keys which are used
 * to control the data access. Data Encryption Keys (which intermediate keys) are stored in a Cosmos DB container that
 * instances of this class are initialized with after wrapping (aka encrypting) it using the Azure Key Vault key
 * provided during the creation of each Data Encryption Key. See https://aka.ms/CosmosClientEncryption for more
 * information on client-side encryption support in Azure Cosmos DB.
 */
public class AzureKeyVaultCosmosEncryptor implements Encryptor {
    private final CosmosEncryptor cosmosEncryptor;
    private final CosmosDataEncryptionKeyProvider cosmosDekProvider;

    /**
     * Gets Container for data encryption keys. TODO: is this needed?
     *
     * @return DataEncryptionKeyContainer key container.
     */
    public DataEncryptionKeyContainer getDataEncryptionKeyContainer() {
        return this.cosmosDekProvider.getDataEncryptionKeyContainer();
    }

    /**
     * Initializes a new instance of the {@link AzureKeyVaultKeyWrapProvider} class. Creates an Encryption Key Provider
     * for wrap and unwrapping Data Encryption key via a Key Vault.
     *
     * @param keyVaultTokenCredentialFactory Factory Instance which represents a method to acquire TokenCredentials for
     * accessing Key Vault Services.
     */
    public AzureKeyVaultCosmosEncryptor(KeyVaultTokenCredentialFactory keyVaultTokenCredentialFactory) {
        EncryptionKeyWrapProvider wrapProvider = new AzureKeyVaultKeyWrapProvider(keyVaultTokenCredentialFactory);
        this.cosmosDekProvider = new CosmosDataEncryptionKeyProvider(wrapProvider);
        this.cosmosEncryptor = new CosmosEncryptor(this.cosmosDekProvider);
    }

    /**
     * Initializes a new instance of the {@link AzureKeyVaultCosmosEncryptor} class. Creates an Encryption Key Provider
     * for wrap and unwrapping Data Encryption key via a Key Vault.
     *
     * @param tokenCredential User provided TokenCredential for accessing Key Vault services.
     */
    public AzureKeyVaultCosmosEncryptor(TokenCredential tokenCredential) {
        EncryptionKeyWrapProvider wrapProvider =
            new AzureKeyVaultKeyWrapProvider(new UserProvidedTokenCredentialFactory(tokenCredential));
        this.cosmosDekProvider = new CosmosDataEncryptionKeyProvider(wrapProvider);
        this.cosmosEncryptor = new CosmosEncryptor(this.cosmosDekProvider);
    }

    /**
     * Initialize Cosmos DB container to store wrapped DEKs
     *
     * @param dekStorageDatabase DEK storage database
     * @param dekStorageContainerId DEK storage container id
     */
    public void initialize( // TODO: should this be async? moderakh
                            CosmosAsyncDatabase dekStorageDatabase,
                            String dekStorageContainerId) {
        this.cosmosDekProvider.initialize(dekStorageDatabase, dekStorageContainerId);
    }

    @Override
    public Mono<byte[]> encryptAsync(
        byte[] plainText,
        String dataEncryptionKeyId,
        String encryptionAlgorithm) {
        return this.cosmosEncryptor.encryptAsync(
            plainText,
            dataEncryptionKeyId,
            encryptionAlgorithm);
    }

    @Override
    public Mono<byte[]> decryptAsync(
        byte[] cipherText,
        String dataEncryptionKeyId,
        String encryptionAlgorithm) {
        return this.cosmosEncryptor.decryptAsync(
            cipherText,
            dataEncryptionKeyId,
            encryptionAlgorithm);
    }
}
