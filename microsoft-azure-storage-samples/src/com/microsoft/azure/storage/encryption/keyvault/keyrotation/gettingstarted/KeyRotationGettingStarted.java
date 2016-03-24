/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.encryption.keyvault.keyrotation.gettingstarted;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.extensions.CachingKeyResolver;
import com.microsoft.azure.keyvault.extensions.KeyVaultKeyResolver;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobEncryptionPolicy;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.util.KeyVaultUtility;
import com.microsoft.azure.storage.util.Utility;

public class KeyRotationGettingStarted {

    public static void main(String[] args) throws StorageException,
            InterruptedException, ExecutionException, URISyntaxException,
            NoSuchAlgorithmException, InvalidKeyException, IOException {
        Utility.printSampleStartInfo("KeyRotationGettingStarted");

        // Create two secrets and obtain their IDs. This is normally a one-time
        // setup step.
        // Although it is possible to use keys (rather than secrets) stored in
        // Key Vault, this prevents caching.
        // Therefore it is recommended to use secrets along with a caching
        // resolver (see below).
        String keyID1 = Utility.keyVaultKeyIDForRotation1;
        String keyID2 = Utility.keyVaultKeyIDForRotation2;

        if (keyID1 == null || keyID1.isEmpty()) {
            keyID1 = KeyVaultUtility.createSecret("KeyRotationSampleSecret1");
        }

        if (keyID2 == null || keyID2.isEmpty()) {
            keyID2 = KeyVaultUtility.createSecret("KeyRotationSampleSecret2");
        }

        // Retrieve storage account information from connection string
        // How to create a storage connection string -
        // https://azure.microsoft.com/en-us/documentation/articles/storage-configure-connection-string/
        CloudStorageAccount storageAccount = CloudStorageAccount
                .parse(Utility.storageConnectionString);
        CloudBlobClient client = storageAccount.createCloudBlobClient();
        CloudBlobContainer container = client
                .getContainerReference("blobencryptioncontainer"
                        + UUID.randomUUID().toString().replace("-", ""));

        // Construct a resolver capable of looking up keys and secrets stored in
        // Key Vault.
        KeyVaultKeyResolver cloudResolver = new KeyVaultKeyResolver(
                KeyVaultUtility.GetKeyVaultClient());

        // Set up a caching resolver so the secrets can be cached on the client.
        // This is the recommended usage
        // pattern since the throttling targets for Storage and Key Vault
        // services are orders of magnitude
        // different.
        CachingKeyResolver cachingResolver = new CachingKeyResolver(2,
                cloudResolver);

        // Create key instances corresponding to the key IDs. This will cache
        // the secrets.
        IKey cloudKey1 = cachingResolver.resolveKeyAsync(keyID1).get();
        IKey cloudKey2 = cachingResolver.resolveKeyAsync(keyID2).get();

        // We begin with cloudKey1, and a resolver capable of resolving and
        // caching Key Vault secrets.
        BlobEncryptionPolicy encryptionPolicy = new BlobEncryptionPolicy(
                cloudKey1, cachingResolver);
        BlobRequestOptions defaultRequestOptions = client
                .getDefaultRequestOptions();
        defaultRequestOptions.setEncryptionPolicy(encryptionPolicy);
        defaultRequestOptions.setRequireEncryption(true);

        try {
            container.createIfNotExists();
            int size = 5 * 1024 * 1024;
            byte[] buffer1 = new byte[size];
            byte[] buffer2 = new byte[size];

            Random rand = new Random();
            rand.nextBytes(buffer1);
            rand.nextBytes(buffer2);

            // Upload the first blob using the secret stored in Azure Key Vault.
            CloudBlockBlob blob = container.getBlockBlobReference("blockblob1");

            System.out.println("Uploading Blob 1 using Key 1.");

            // Upload the encrypted contents to the first blob.
            ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer1);
            blob.upload(inputStream, size);

            System.out.println("Downloading and decrypting Blob 1.");

            // Download and decrypt the encrypted contents from the first blob.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blob.download(outputStream);

            // At this point we will rotate our keys so new encrypted content
            // will use the
            // second key. Note that the same resolver is used, as this resolver
            // is capable
            // of decrypting blobs encrypted using either key.
            System.out.println("Rotating the active encryption key to Key 2.");

            client.getDefaultRequestOptions().setEncryptionPolicy(
                    new BlobEncryptionPolicy(cloudKey2, cachingResolver));

            // Upload the second blob using the key stored in Azure Key Vault.
            CloudBlockBlob blob2 = container
                    .getBlockBlobReference("blockblob2");

            System.out.println("Uploading Blob 2 using Key 2.");

            // Upload the encrypted contents to the second blob.
            inputStream = new ByteArrayInputStream(buffer2);
            blob2.upload(inputStream, size);

            System.out.println("Downloading and decrypting Blob 2.");

            // Download and decrypt the encrypted contents from the second blob.
            outputStream = new ByteArrayOutputStream();
            blob2.download(outputStream);

            // Here we download and re-upload the first blob. This has the
            // effect of updating
            // the blob to use the new key.
            System.out.println("Downloading and decrypting Blob 1.");
            outputStream = new ByteArrayOutputStream();
            blob.download(outputStream);

            System.out.println("Re-uploading Blob 1 using Key 2.");
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            blob.upload(inputStream, size);

            // For the purposes of demonstration, we now override the encryption
            // policy to only recognize key 2.
            BlobEncryptionPolicy key2OnlyPolicy = new BlobEncryptionPolicy(
                    cloudKey2, null);
            BlobRequestOptions key2OnlyOptions = new BlobRequestOptions();
            key2OnlyOptions.setEncryptionPolicy(key2OnlyPolicy);

            System.out.println("Downloading and decrypting Blob 1.");

            outputStream = new ByteArrayOutputStream();
            blob.download(outputStream, null, key2OnlyOptions, null);
            // The first blob can still be decrypted because it is using the
            // second key.
        } finally {
            container.deleteIfExists();
            Utility.printSampleCompleteInfo("KeyRotationGettingStarted");
        }

    }

}
