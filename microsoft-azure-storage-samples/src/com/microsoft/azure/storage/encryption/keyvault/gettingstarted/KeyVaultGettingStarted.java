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
package com.microsoft.azure.storage.encryption.keyvault.gettingstarted;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.extensions.AggregateKeyResolver;
import com.microsoft.azure.keyvault.extensions.CachingKeyResolver;
import com.microsoft.azure.keyvault.extensions.KeyVaultKeyResolver;
import com.microsoft.azure.keyvault.extensions.RsaKey;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobEncryptionPolicy;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.util.KeyVaultUtility;
import com.microsoft.azure.storage.util.LocalResolver;
import com.microsoft.azure.storage.util.Utility;

public class KeyVaultGettingStarted {

    public static void main(String[] args) throws StorageException,
            NoSuchAlgorithmException, InterruptedException, ExecutionException,
            URISyntaxException, InvalidKeyException, IOException {
        Utility.printSampleStartInfo("KeyVaultGettingStarted");

        // Get the key ID from App.config if it exists.
        String keyID = Utility.keyVaultKeyID;

        // If no key ID was specified, we will create a new secret in Key Vault.
        // To create a new secret, this client needs full permission to Key
        // Vault secrets.
        // Once the secret is created, its ID can be added to App.config. Once
        // this is done,
        // this client only needs read access to secrets.
        if (keyID == null || keyID.isEmpty()) {
            keyID = KeyVaultUtility.createSecret("KVGettingStartedSecret");
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

        // To demonstrate how multiple different types of key can be used, we
        // also create a local key and resolver.
        // This key is temporary and won't be persisted.
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        final KeyPair wrapKey = keyGen.generateKeyPair();

        RsaKey rsaKey = new RsaKey("rsaKey1", wrapKey);
        LocalResolver resolver = new LocalResolver();
        resolver.add(rsaKey);

        // If there are multiple key sources like Azure Key Vault and local KMS,
        // set up an aggregate resolver as follows.
        // This helps users to define a plug-in model for all the different key
        // providers they support.
        AggregateKeyResolver aggregateResolver = new AggregateKeyResolver();
        aggregateResolver.Add(resolver);
        aggregateResolver.Add(cloudResolver);

        // Set up a caching resolver so the secrets can be cached on the client.
        // This is the recommended usage
        // pattern since the throttling targets for Storage and Key Vault
        // services are orders of magnitude
        // different.
        CachingKeyResolver cachingResolver = new CachingKeyResolver(2,
                aggregateResolver);

        // Create a key instance corresponding to the key ID. This will cache
        // the secret.
        IKey cloudKey = cachingResolver.resolveKeyAsync(keyID).get();

        try {
            container.createIfNotExists();
            int size = 5 * 1024 * 1024;
            byte[] buffer = new byte[size];

            Random rand = new Random();
            rand.nextBytes(buffer);

            // The first blob will use the key stored in Azure Key Vault.
            CloudBlockBlob blob = container.getBlockBlobReference("blockblob1");

            // Create the encryption policy using the secret stored in Azure Key
            // Vault to be used for upload.
            BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(
                    cloudKey, null);

            // Set the encryption policy on the request options.
            BlobRequestOptions uploadOptions = new BlobRequestOptions();
            uploadOptions.setEncryptionPolicy(uploadPolicy);

            System.out.println("Uploading the 1st encrypted blob.");

            // Upload the encrypted contents to the blob.
            ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
            blob.upload(inputStream, size, null, uploadOptions, null);

            // Download the encrypted blob.
            BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(
                    null, cachingResolver);

            // Set the decryption policy on the request options.
            BlobRequestOptions downloadOptions = new BlobRequestOptions();
            downloadOptions.setEncryptionPolicy(downloadPolicy);

            System.out.println("Downloading the 1st encrypted blob.");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blob.download(outputStream, null, downloadOptions, null);

            // Upload second blob using the local key.
            blob = container.getBlockBlobReference("blockblob2");

            // Create the encryption policy using the local key.
            uploadPolicy = new BlobEncryptionPolicy(rsaKey, null);

            // Set the encryption policy on the request options.
            uploadOptions = new BlobRequestOptions();
            uploadOptions.setEncryptionPolicy(uploadPolicy);

            System.out.println("Uploading the 2nd encrypted blob.");

            // Upload the encrypted contents to the blob.
            inputStream = new ByteArrayInputStream(buffer);
            blob.upload(inputStream, size, null, uploadOptions, null);

            // Download the encrypted blob. The same policy and options created
            // before can be used because the aggregate resolver contains both
            // resolvers and will pick the right one based on the key ID stored
            // in blob metadata on the service.
            System.out.println("Downloading the 2nd encrypted blob.");

            // Download and decrypt the encrypted contents from the blob.
            outputStream = new ByteArrayOutputStream();
            blob.download(outputStream, null, downloadOptions, null);
        } finally {
            container.deleteIfExists();
            Utility.printSampleCompleteInfo("KeyVaultGettingStarted");
        }
    }
}
