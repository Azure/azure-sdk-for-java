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
package com.microsoft.azure.storage.encryption.table.gettingstarted.attributes;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import com.microsoft.azure.keyvault.extensions.RsaKey;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import com.microsoft.azure.storage.table.TableEncryptionPolicy;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableRequestOptions;
import com.microsoft.azure.storage.table.TableResult;
import com.microsoft.azure.storage.util.LocalResolver;
import com.microsoft.azure.storage.util.Utility;

public class TableGettingStartedAttributes {

    public static void main(String[] args) throws URISyntaxException,
            StorageException, InvalidKeyException, NoSuchAlgorithmException {
        Utility.printSampleStartInfo("TableResolverAttributes");

        // Retrieve storage account information from connection string
        // How to create a storage connection string -
        // https://azure.microsoft.com/en-us/documentation/articles/storage-configure-connection-string/
        CloudStorageAccount storageAccount = CloudStorageAccount
                .parse(Utility.storageConnectionString);
        CloudTableClient client = storageAccount.createCloudTableClient();
        CloudTable table = client.getTableReference("encryptiontableattributes"
                + UUID.randomUUID().toString().replace("-", ""));

        try {
            table.createIfNotExists();

            // Create the IKey used for encryption.
            RsaKey key = new RsaKey("private:key1");

            EncryptedEntity ent = new EncryptedEntity(UUID.randomUUID()
                    .toString(), String.valueOf(new Date().getTime()));
            ent.Populate();

            TableRequestOptions insertOptions = new TableRequestOptions();
            insertOptions.setEncryptionPolicy(new TableEncryptionPolicy(key,
                    null));

            // Insert Entity
            System.out.println("Inserting the encrypted entity.");
            table.execute(TableOperation.insert(ent), insertOptions, null);

            // For retrieves, a resolver can be set up that will help pick the
            // key based on the key id.
            LocalResolver resolver = new LocalResolver();
            resolver.add(key);

            TableRequestOptions retrieveOptions = new TableRequestOptions();
            retrieveOptions.setEncryptionPolicy(new TableEncryptionPolicy(null,
                    resolver));

            // Retrieve Entity
            System.out.println("Retrieving the encrypted entity.");
            TableOperation operation = TableOperation.retrieve(
                    ent.getPartitionKey(), ent.getRowKey(),
                    EncryptedEntity.class);
            TableResult result = table
                    .execute(operation, retrieveOptions, null);
            EncryptedEntity resultEntity = result.getResultAsType();
            System.out.println("EncryptedProperty2 = "
                    + resultEntity.getEncryptedProperty2());
        } finally {
            table.deleteIfExists();
            Utility.printSampleCompleteInfo("TableResolverAttributes");
        }
    }
}
