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
package com.microsoft.azure.storage.encryption.blob.gettingstarted;

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

import com.microsoft.azure.keyvault.extensions.RsaKey;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobEncryptionPolicy;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.util.LocalResolver;
import com.microsoft.azure.storage.util.Utility;

/**
 * Demonstrates how to use encryption with the Azure Blob service.
 */
public class BlobGettingStarted {

    public static void main(String[] args) throws InvalidKeyException,
            URISyntaxException, StorageException, NoSuchAlgorithmException,
            IOException {
        Utility.printSampleStartInfo("BlobBasicsEncryption");

        // Retrieve storage account information from connection string
        // How to create a storage connection string -
        // https://azure.microsoft.com/en-us/documentation/articles/storage-configure-connection-string/
        CloudStorageAccount account = CloudStorageAccount
                .parse(Utility.storageConnectionString);
        CloudBlobClient blobClient = account.createCloudBlobClient();

        // Get a reference to a container
        // The container name must be lower case
        // Append a random UUID to the end of the container name so that
        // this sample can be run more than once in quick succession.
        CloudBlobContainer container = blobClient
                .getContainerReference("blobencryptioncontainer"
                        + UUID.randomUUID().toString().replace("-", ""));

        try {
            // Create the container if it does not exist
            container.createIfNotExists();

            int size = 5 * 1024 * 1024;
            byte[] buffer = new byte[size];

            Random rand = new Random();
            rand.nextBytes(buffer);

            CloudBlockBlob blob = container.getBlockBlobReference("blockBlob");

            // Create the IKey used for encryption.
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            final KeyPair wrapKey = keyGen.generateKeyPair();
            RsaKey key = new RsaKey("rsaKey1", wrapKey);

            // Create the encryption policy to be used for upload.
            BlobEncryptionPolicy uploadPolicy = new BlobEncryptionPolicy(key,
                    null);

            // Set the encryption policy on the request options.
            BlobRequestOptions uploadOptions = new BlobRequestOptions();
            uploadOptions.setEncryptionPolicy(uploadPolicy);

            System.out.println("Uploading the encrypted blob.");

            // Upload the encrypted contents to the blob.
            ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
            blob.upload(inputStream, size, null, uploadOptions, null);

            // Download the encrypted blob.
            // For downloads, a resolver can be set up that will help pick the
            // key based on the key id.
            // Create the encryption policy to be used for download.
            LocalResolver resolver = new LocalResolver();
            resolver.add(key);
            BlobEncryptionPolicy downloadPolicy = new BlobEncryptionPolicy(
                    null, resolver);

            // Set the decryption policy on the request options.
            BlobRequestOptions downloadOptions = new BlobRequestOptions();
            downloadOptions.setEncryptionPolicy(downloadPolicy);

            System.out.println("Downloading the encrypted blob.");

            // Download and decrypt the encrypted contents from the blob.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blob.download(outputStream, null, downloadOptions, null);
        } finally {
            // Delete the container
            container.deleteIfExists();
            Utility.printSampleCompleteInfo("BlobBasicsEncryption");
        }
    }
}
