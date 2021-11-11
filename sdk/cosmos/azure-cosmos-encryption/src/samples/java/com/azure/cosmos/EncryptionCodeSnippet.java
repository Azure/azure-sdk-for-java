// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.encryption.CosmosEncryptionAsyncClient;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncContainer;
import com.azure.cosmos.encryption.CosmosEncryptionAsyncDatabase;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.KeyEncryptionKeyAlgorithm;
import com.microsoft.data.encryption.cryptography.MicrosoftDataEncryptionException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Code snippets for {@link ChangeFeedProcessor}
 */
public class EncryptionCodeSnippet {

    public void encryptionSample() {
        CosmosClientBuilder builder = new CosmosClientBuilder();

        CosmosAsyncClient client = builder.key("key")
            .endpoint("endpoint")
            .buildAsyncClient();
        createContainerWithClientEncryptionPolicy(client); //creating container with client encryption policy

        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient =
            CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(client, new SimpleEncryptionKeyStoreProvider());
        CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase("myDb");
        CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer =
            cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer("myCol");

        createClientEncryptionKey(cosmosEncryptionAsyncDatabase);//create client encryption key to be store on database

        Pojo originalItem = new Pojo();
        originalItem.id = UUID.randomUUID().toString();
        originalItem.mypk = UUID.randomUUID().toString();
        originalItem.nonSensitive = UUID.randomUUID().toString();
        originalItem.sensitiveString = "this is a secret string to be encrypted";
        originalItem.sensitiveInt = 1234; //"this is a secret int to be encrypted";
        originalItem.sensitiveFloat = 1234.01f; //"this is a secret float to be encrypted";
        originalItem.sensitiveDouble = 1234.1234; //"this is a secret double to be encrypted";
        originalItem.sensitiveLong = 1234; //"this is a secret long to be encrypted";
        originalItem.sensitiveBoolean = true; //"this is a secret boolean to be encrypted";

        CosmosItemResponse<Pojo> response = cosmosEncryptionAsyncContainer.createItem(originalItem,
            new PartitionKey(originalItem.mypk), new CosmosItemRequestOptions()).block();

        // read and decrypt the item
        CosmosItemResponse<Pojo> readResponse = cosmosEncryptionAsyncContainer.readItem(originalItem.id,
            new PartitionKey(originalItem.mypk), null, Pojo.class).block();
        Pojo readItem = readResponse.getItem();

        assert (originalItem.sensitiveString.equals(readItem.sensitiveString));
        assert (originalItem.sensitiveInt == readItem.sensitiveInt);
        assert (originalItem.sensitiveFloat == readItem.sensitiveFloat);
        assert (originalItem.sensitiveDouble == readItem.sensitiveDouble);
        assert (originalItem.sensitiveLong == readItem.sensitiveLong);
        assert (originalItem.sensitiveBoolean == readItem.sensitiveBoolean);
    }

    void createContainerWithClientEncryptionPolicy(CosmosAsyncClient client) {
        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.setClientEncryptionKeyId("key1");
        includedPath1.setPath("/sensitive");
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

        ClientEncryptionIncludedPath includedPath2 = new ClientEncryptionIncludedPath();
        includedPath2.setClientEncryptionKeyId("key2");
        includedPath2.setPath("/nonValidPath");
        includedPath2.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath2.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

        ClientEncryptionIncludedPath includedPath3 = new ClientEncryptionIncludedPath();
        includedPath3.setClientEncryptionKeyId("key1");
        includedPath3.setPath("/sensitiveInt");
        includedPath3.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath3.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

        ClientEncryptionIncludedPath includedPath4 = new ClientEncryptionIncludedPath();
        includedPath4.setClientEncryptionKeyId("key2");
        includedPath4.setPath("/sensitiveFloat");
        includedPath4.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath4.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

        ClientEncryptionIncludedPath includedPath5 = new ClientEncryptionIncludedPath();
        includedPath5.setClientEncryptionKeyId("key1");
        includedPath5.setPath("/sensitiveLong");
        includedPath5.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath5.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

        ClientEncryptionIncludedPath includedPath6 = new ClientEncryptionIncludedPath();
        includedPath6.setClientEncryptionKeyId("key2");
        includedPath6.setPath("/sensitiveDouble");
        includedPath6.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath6.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

        ClientEncryptionIncludedPath includedPath7 = new ClientEncryptionIncludedPath();
        includedPath7.setClientEncryptionKeyId("key1");
        includedPath7.setPath("/sensitiveBoolean");
        includedPath7.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath7.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256);

        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        paths.add(includedPath2);
        paths.add(includedPath3);
        paths.add(includedPath4);
        paths.add(includedPath5);
        paths.add(includedPath6);
        paths.add(includedPath7);

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(paths);
        String containerId = "myCol";
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        client.getDatabase("myDb").createContainer(properties).block();
    }

    void createClientEncryptionKey(CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase) {
        EncryptionKeyWrapMetadata metadata1 = new EncryptionKeyWrapMetadata("custom", "key1", "tempmetadata1");
        EncryptionKeyWrapMetadata metadata2 = new EncryptionKeyWrapMetadata("custom", "key2", "tempmetadata2");
        new EncryptionKeyWrapMetadata("custom", "key1", "tempmetadata1");
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256, metadata1).block().getProperties();
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256, metadata2).block().getProperties();
    }

    class SimpleEncryptionKeyStoreProvider extends EncryptionKeyStoreProvider {
        // this is a naive data encryption key store provider which always uses the same data encryption key from the
        // service.
        // the user should implement EncryptionKeyStoreProvider as per use case;
        // To use key value please use AzureKeyVaultKeyStoreProvider

        @Override
        public String getProviderName() {
            return "SimpleEncryptionKeyStoreProvider";
        }

        @Override
        public byte[] unwrapKey(String s, KeyEncryptionKeyAlgorithm keyEncryptionKeyAlgorithm, byte[] encryptedBytes) {
            return encryptedBytes;
        }

        @Override
        public byte[] wrapKey(String s, KeyEncryptionKeyAlgorithm keyEncryptionKeyAlgorithm, byte[] key) {
            return key;
        }

        @Override
        public byte[] sign(String s, boolean b) throws MicrosoftDataEncryptionException {
            return new byte[0];
        }

        @Override
        public boolean verify(String s, boolean b, byte[] bytes) throws MicrosoftDataEncryptionException {
            return true;
        }
    }

    public static class Pojo {
        public String id;
        @JsonProperty
        public String mypk;
        @JsonProperty
        public String nonSensitive;
        @JsonProperty
        public String sensitiveString;
        @JsonProperty
        public int sensitiveInt;
        @JsonProperty
        public float sensitiveFloat;
        @JsonProperty
        public long sensitiveLong;
        @JsonProperty
        public double sensitiveDouble;
        @JsonProperty
        public boolean sensitiveBoolean;
    }
}

