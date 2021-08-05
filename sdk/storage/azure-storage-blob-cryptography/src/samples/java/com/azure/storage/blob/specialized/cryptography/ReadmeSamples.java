// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.credential.TokenCredential;
import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.KeyEncryptionKeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.storage.blob.BlobClient;

import javax.crypto.spec.SecretKeySpec;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    private BlobClient blobClient;
    private String connectionString;
    private String containerName;
    private String blobName;
    private AsyncKeyEncryptionKey key;
    private AsyncKeyEncryptionKeyResolver keyResolver;
    private String keyWrapAlgorithm;
    private byte[] keyBytes;
    private String secretKeyAlgorithm;
    private TokenCredential tokenCredential;
    private String keyVaultUrl;
    private String keyName;

    public void getEncryptedBlobClientBlobClient() {
        EncryptedBlobClient client = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .blobClient(blobClient)
            .buildEncryptedBlobClient();
    }

    public void getEncryptedBlobClient() {
        EncryptedBlobClient client = new EncryptedBlobClientBuilder()
            .key(key, keyWrapAlgorithm)
            .keyResolver(keyResolver)
            .connectionString(connectionString)
            .containerName(containerName)
            .blobName(blobName)
            .buildEncryptedBlobClient();
    }

    public void getClientLocalKey() {
        JsonWebKey localKey = JsonWebKey.fromAes(new SecretKeySpec(keyBytes, secretKeyAlgorithm),
            Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY))
            .setId("my-id");
        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder()
            .buildAsyncKeyEncryptionKey(localKey).block();

        EncryptedBlobClient client = new EncryptedBlobClientBuilder()
            .key(akek, keyWrapAlgorithm)
            .connectionString(connectionString)
            .containerName(containerName)
            .blobName(blobName)
            .buildEncryptedBlobClient();
    }

    public void getClientKeyVaultKey() {
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl(keyVaultUrl)
            .credential(tokenCredential)
            .buildClient();
        KeyVaultKey rsaKey = keyClient.createRsaKey(new CreateRsaKeyOptions(keyName)
            .setExpiresOn(OffsetDateTime.now().plusYears(1))
            .setKeySize(2048));
        AsyncKeyEncryptionKey akek = new KeyEncryptionKeyClientBuilder()
            .credential(tokenCredential)
            .buildAsyncKeyEncryptionKey(rsaKey.getId())
            .block();

        EncryptedBlobClient client = new EncryptedBlobClientBuilder()
            .key(akek, keyWrapAlgorithm)
            .connectionString(connectionString)
            .containerName(containerName)
            .blobName(blobName)
            .buildEncryptedBlobClient();
    }
}

