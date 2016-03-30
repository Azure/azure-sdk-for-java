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
package com.microsoft.azure.storage.encryption.queue.gettingstarted;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.EnumSet;
import java.util.UUID;

import com.microsoft.azure.keyvault.extensions.RsaKey;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.azure.storage.queue.MessageUpdateFields;
import com.microsoft.azure.storage.queue.QueueEncryptionPolicy;
import com.microsoft.azure.storage.queue.QueueRequestOptions;
import com.microsoft.azure.storage.util.LocalResolver;
import com.microsoft.azure.storage.util.Utility;

public class QueueGettingStarted {

    public static void main(String[] args) throws InvalidKeyException,
            URISyntaxException, StorageException {

        Utility.printSampleStartInfo("QueueBasicsEncryption");

        // Retrieve storage account information from connection string
        // How to create a storage connection string -
        // https://azure.microsoft.com/en-us/documentation/articles/storage-configure-connection-string/
        CloudStorageAccount account = CloudStorageAccount
                .parse(Utility.storageConnectionString);
        CloudQueueClient client = account.createCloudQueueClient();
        CloudQueue queue = client.getQueueReference("encryptionqueue"
                + UUID.randomUUID().toString().replace("-", ""));

        try {
            queue.createIfNotExists();

            // Create the IKey used for encryption.
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            final KeyPair wrapKey = keyGen.generateKeyPair();

            RsaKey key = new RsaKey("rsaKey1", wrapKey);

            // Create the encryption policy to be used for insert and update.
            QueueEncryptionPolicy insertPolicy = new QueueEncryptionPolicy(key,
                    null);

            // Set the encryption policy on the request options.
            QueueRequestOptions insertOptions = new QueueRequestOptions();
            insertOptions.setEncryptionPolicy(insertPolicy);

            String messageStr = UUID.randomUUID().toString();
            CloudQueueMessage message = new CloudQueueMessage(messageStr);

            // Add message
            System.out.println("Inserting the encrypted message.");
            queue.addMessage(message, 0, 0, insertOptions, null);

            // For retrieves, a resolver can be set up that will help pick the
            // key based on the key id.
            LocalResolver resolver = new LocalResolver();
            resolver.add(key);

            QueueEncryptionPolicy retrPolicy = new QueueEncryptionPolicy(null,
                    resolver);
            QueueRequestOptions retrieveOptions = new QueueRequestOptions();
            retrieveOptions.setEncryptionPolicy(retrPolicy);

            // Retrieve message
            System.out.println("Retrieving the encrypted message.");
            CloudQueueMessage retrMessage = queue.retrieveMessage(1,
                    retrieveOptions, null);

            // Update message
            System.out.println("Updating the encrypted message.");
            String updatedMessage = UUID.randomUUID().toString();
            retrMessage.setMessageContent(updatedMessage);
            queue.updateMessage(retrMessage, 0, EnumSet
                    .of(MessageUpdateFields.CONTENT,
                            MessageUpdateFields.VISIBILITY), insertOptions,
                    null);

            // Retrieve updated message
            System.out.println("Retrieving the updated encrypted message.");
            retrMessage = queue.retrieveMessage(1, retrieveOptions, null);
        } catch (Throwable t) {
            Utility.printException(t);
        } finally {
            queue.deleteIfExists();
            Utility.printSampleCompleteInfo("QueueBasicsEncryption");
        }
    }

}
