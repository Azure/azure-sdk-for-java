// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncDatabase;
import com.azure.cosmos.encryption.CosmosEncryptionContainer;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.PartitionKey;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.microsoft.data.encryption.AzureKeyVaultKeyStoreProvider.AzureKeyVaultKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReadmeSamples {
    private final TokenCredential tokenCredentials = new EnvironmentCredentialBuilder().build();
    private final CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .buildAsyncClient();
    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient =
        CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(cosmosAsyncClient,
            new AzureKeyVaultKeyStoreProvider(tokenCredentials));
    private final EncryptionKeyStoreProvider encryptionKeyStoreProvider = new AzureKeyVaultKeyStoreProvider(tokenCredentials);
    private final CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase = cosmosEncryptionAsyncClient
        .getCosmosEncryptionAsyncDatabase("<YOUR DATABASE NAME>");
    private final CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncDatabase
        .getCosmosEncryptionAsyncContainer("<YOUR CONTAINER NAME>");

    public ReadmeSamples() throws MicrosoftDataEncryptionException {
    }

    public void createCosmosEncryptionClient() throws MicrosoftDataEncryptionException {
        // BEGIN: readme-sample-createCosmosEncryptionClient
        // Create a new CosmosEncryptionAsyncClient
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint("<YOUR ENDPOINT HERE>")
            .key("<YOUR KEY HERE>")
            .buildAsyncClient();
        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient =
            CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(cosmosAsyncClient,
                new AzureKeyVaultKeyStoreProvider(tokenCredentials));
        // END: readme-sample-createCosmosEncryptionClient
    }

    public void createCosmosEncryptionDatabase() {
        // BEGIN: readme-sample-createCosmosEncryptionDatabase
        // This will create a database with the regular cosmosAsyncClient.
        CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase = cosmosEncryptionAsyncClient.getCosmosAsyncClient()
            .createDatabaseIfNotExists("<YOUR DATABASE NAME>")
            // TIP: Our APIs are Reactor Core based, so try to chain your calls
            .map(databaseResponse ->
                // Get a reference to the encryption database
                // This will create a cosmos encryption database proxy object.
                cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(databaseResponse.getProperties().getId()))
            .block(); // Blocking for demo purposes (avoid doing this in production unless you must)
        // END: readme-sample-createCosmosEncryptionDatabase
    }

    public void createCosmosEncryptionContainer() {
        // BEGIN: readme-sample-createCosmosEncryptionContainer
        //Create Client Encryption Key
        EncryptionKeyWrapMetadata metadata = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key", "tempmetadata");
        CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncDatabase
            .createClientEncryptionKey("key", CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256, metadata)
            // TIP: Our APIs are Reactor Core based, so try to chain your calls
            .then(Mono.defer(() -> {
                //Create Encryption Container
                ClientEncryptionIncludedPath includedPath = new ClientEncryptionIncludedPath();
                includedPath.setClientEncryptionKeyId("key");
                includedPath.setPath("/sensitiveString");
                includedPath.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
                includedPath.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

                List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
                paths.add(includedPath);
                ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(paths);
                CosmosContainerProperties properties = new CosmosContainerProperties("<YOUR CONTAINER NAME>", "/mypk");
                properties.setClientEncryptionPolicy(clientEncryptionPolicy);
                return cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(properties);
            }))
            .map(containerResponse ->
                // Create a reference to the encryption container
                // This will create a cosmos encryption container proxy object.
                cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerResponse.getProperties().getId()))
            .block(); // Blocking for demo purposes (avoid doing this in production unless you must)
        // END: readme-sample-createCosmosEncryptionContainer
    }

    public void crudOperationsOnItems() {
        // BEGIN: readme-sample-crudOperationsOnItems
        // Create an item
        Pojo pojo = new Pojo();
        pojo.setSensitiveString("Sensitive Information need to be encrypted");
        cosmosEncryptionAsyncContainer.createItem(pojo)
            .flatMap(response -> {
                System.out.println("Created item: " + response.getItem());
                // Read that item 👓
                return cosmosEncryptionAsyncContainer.readItem(response.getItem().getId(),
                    new PartitionKey(response.getItem().getId()),
                    Pojo.class);
            })
            .flatMap(response -> {
                System.out.println("Read item: " + response.getItem());
                // Replace that item 🔁
                Pojo p = response.getItem();
                pojo.setSensitiveString("New Sensitive Information");
                return cosmosEncryptionAsyncContainer.replaceItem(p, response.getItem().getId(),
                    new PartitionKey(response.getItem().getId()));
            })
            // delete that item 💣
            .flatMap(response -> cosmosEncryptionAsyncContainer.deleteItem(response.getItem().getId(),
                new PartitionKey(response.getItem().getId())))
            .subscribe();
        // END: readme-sample-crudOperationsOnItems
    }

    private static final class Pojo {
        private final String id;

        private String sensitiveString;

        Pojo() {
            this.id = UUID.randomUUID().toString();
        }

        String getId () {
            return id;
        }

        void setSensitiveString(String sensitiveString) {
            this.sensitiveString = sensitiveString;
        }
    }
}
