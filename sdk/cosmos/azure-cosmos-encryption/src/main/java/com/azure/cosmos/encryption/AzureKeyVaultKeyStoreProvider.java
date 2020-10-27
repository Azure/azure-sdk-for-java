// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.implementation.encryption.AzureKeyVaultKeyWrapProvider;
import com.azure.cosmos.implementation.encryption.UserProvidedTokenCredentialFactory;

import java.net.URL;

public class AzureKeyVaultKeyStoreProvider extends EncryptionKeyStoreProvider {
    private final AzureKeyVaultKeyWrapMetadata metaddata;
    private final AzureKeyVaultKeyWrapProvider wrapProvider;

    // TODO exception contract
    // TODO: public api contract finalization
    public AzureKeyVaultKeyStoreProvider(String masterUri, AzureKeyVaultKeyWrapProvider azureKeyVaultKeyWrapProvider) {
        try {
            metaddata = new AzureKeyVaultKeyWrapMetadata(new URL(masterUri));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.wrapProvider = azureKeyVaultKeyWrapProvider;
    }

    public AzureKeyVaultKeyStoreProvider(String masterUri, TokenCredential tokenCredential) {
        try {
            metaddata = new AzureKeyVaultKeyWrapMetadata(new URL(masterUri));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.wrapProvider =
            new AzureKeyVaultKeyWrapProvider(new UserProvidedTokenCredentialFactory(tokenCredential));
    }

    // TODO: block vs non-block
    // TODO: what happens if the key doesn't exit?
    @Override
    public byte[] unwrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] encryptedKey) {
        return this.wrapProvider.unwrapKey(encryptedKey, metaddata).block().getDataEncryptionKey();
    }

    @Override
    public byte[] wrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] key) {
        return this.wrapProvider.wrapKey(key, metaddata).block().getWrappedDataEncryptionKey();
    }
}
