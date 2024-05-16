// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
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

        CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder().cosmosAsyncClient(client).keyEncryptionKeyResolver(
            new SimpleKeyEncryptionKeyResolver()).keyEncryptionKeyResolverName(CosmosEncryptionClientBuilder.KEY_RESOLVER_NAME_AZURE_KEY_VAULT).buildAsyncClient();
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
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

        ClientEncryptionIncludedPath includedPath2 = new ClientEncryptionIncludedPath();
        includedPath2.setClientEncryptionKeyId("key2");
        includedPath2.setPath("/nonValidPath");
        includedPath2.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath2.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

        ClientEncryptionIncludedPath includedPath3 = new ClientEncryptionIncludedPath();
        includedPath3.setClientEncryptionKeyId("key1");
        includedPath3.setPath("/sensitiveInt");
        includedPath3.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath3.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

        ClientEncryptionIncludedPath includedPath4 = new ClientEncryptionIncludedPath();
        includedPath4.setClientEncryptionKeyId("key2");
        includedPath4.setPath("/sensitiveFloat");
        includedPath4.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath4.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

        ClientEncryptionIncludedPath includedPath5 = new ClientEncryptionIncludedPath();
        includedPath5.setClientEncryptionKeyId("key1");
        includedPath5.setPath("/sensitiveLong");
        includedPath5.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath5.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

        ClientEncryptionIncludedPath includedPath6 = new ClientEncryptionIncludedPath();
        includedPath6.setClientEncryptionKeyId("key2");
        includedPath6.setPath("/sensitiveDouble");
        includedPath6.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath6.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

        ClientEncryptionIncludedPath includedPath7 = new ClientEncryptionIncludedPath();
        includedPath7.setClientEncryptionKeyId("key1");
        includedPath7.setPath("/sensitiveBoolean");
        includedPath7.setEncryptionType(CosmosEncryptionType.DETERMINISTIC.getName());
        includedPath7.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName());

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
        EncryptionKeyWrapMetadata metadata1 = new EncryptionKeyWrapMetadata("custom", "key1", "tempmetadata1", "RSA-OAEP");
        EncryptionKeyWrapMetadata metadata2 = new EncryptionKeyWrapMetadata("custom", "key2", "tempmetadata2", "RSA-OAEP");
        new EncryptionKeyWrapMetadata("custom", "key1", "tempmetadata1", "RSA-OAEP");
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key1",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata1).block().getProperties();
        cosmosEncryptionAsyncDatabase.createClientEncryptionKey("key2",
            CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata2).block().getProperties();
    }

    class SimpleKeyEncryptionKey implements KeyEncryptionKey {
        // this is a naive data encryption key which always uses the same data encryption key from the
        // service.
        // the user should implement KeyEncryptionKey as per use case;
        // To use key vault please use KeyEncryptionKeyClient from azure-security-keyvault-keys

        private final String keyId;
        public SimpleKeyEncryptionKey(String keyId) {
            this.keyId = keyId;
        }

        @Override
        public String getKeyId() {
            return this.keyId;
        }

        @Override
        public byte[] wrapKey(String s, byte[] bytes) {
            return bytes;
        }

        @Override
        public byte[] unwrapKey(String s, byte[] bytes) {
            return bytes;
        }
    }

    class SimpleKeyEncryptionKeyResolver implements KeyEncryptionKeyResolver {
        // this is a naive data encryption key resolver which always uses the same data encryption key from the
        // service.
        // the user should implement KeyEncryptionKeyResolver as per use case;
        // To use key vault please use KeyEncryptionKeyClientBuilder from azure-security-keyvault-keys

        @Override
        public KeyEncryptionKey buildKeyEncryptionKey(String s) {
            return new SimpleKeyEncryptionKey("SimpleEncryptionKeyStoreProvider");
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

