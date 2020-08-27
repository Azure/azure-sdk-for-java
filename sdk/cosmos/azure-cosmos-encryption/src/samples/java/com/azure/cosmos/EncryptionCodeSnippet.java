// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.encryption.EncryptionCosmosAsyncContainer;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.DataEncryptionKeyProvider;
import com.azure.cosmos.encryption.EncryptionItemRequestOptions;
import com.azure.cosmos.encryption.EncryptionOptions;
import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.encryption.WithEncryption;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;

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

        CosmosAsyncContainer container = client.getDatabase("myDb").getContainer("myCol");

        // Encryptor given a key provider can encrypt/decrypt data
        Encryptor encryptor =
            WithEncryption.createCosmosEncryptor(simpleDataEncryptionKeyProvider());

        EncryptionCosmosAsyncContainer encryptionContainer =
            WithEncryption.withEncryptor(container, encryptor);

        Pojo originalItem = new Pojo();
        originalItem.id = UUID.randomUUID().toString();
        originalItem.mypk = UUID.randomUUID().toString();
        originalItem.nonSensitive = UUID.randomUUID().toString();
        originalItem.sensitive1 = "this is a secret to be encrypted";
        originalItem.sensitive2 = "this is a another secret to be encrypted";

        // create an item and encrypt /sensitive1 and sensitive2
        EncryptionItemRequestOptions options = new EncryptionItemRequestOptions();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(ImmutableList.of("/sensitive1", "/sensitive2"));
        options.setEncryptionOptions(encryptionOptions);
        CosmosItemResponse<Pojo> response = encryptionContainer.createItem(originalItem, new PartitionKey(originalItem.mypk), options).block();

        // read and decrypt the item
        CosmosItemResponse<Pojo> readResponse = encryptionContainer.readItem(originalItem.id, new PartitionKey(originalItem.mypk), null, Pojo.class).block();
        Pojo readItem = readResponse.getItem();

        assert(originalItem.sensitive1.equals(readItem.sensitive1));
        assert(originalItem.sensitive2.equals(readItem.sensitive2));
    }

    private DataEncryptionKeyProvider simpleDataEncryptionKeyProvider() {
        // this is a naive data encryption key provider which always uses the same data encryption key in memory.
        // the user should implement DataEncryptionKeyProvider as per use case;
        // storing data encryption keys should happen on the app side.
        DataEncryptionKey key = createDataEncryptionKey();

        return new DataEncryptionKeyProvider() {
            @Override
            public Mono<DataEncryptionKey> getDataEncryptionKey(String id, String algorithm) {
                return Mono.just(key);
            }
        };
    }

    public static class Pojo {
        @JsonProperty
        private String id;
        @JsonProperty
        private String mypk;
        @JsonProperty
        private String nonSensitive;
        @JsonProperty
        private String sensitive1;
        @JsonProperty
        private String sensitive2;
    }

    private DataEncryptionKey createDataEncryptionKey() {
        byte[] key = DataEncryptionKey.generate("AEAes256CbcHmacSha256Randomized");
        return DataEncryptionKey.create(key, "AEAes256CbcHmacSha256Randomized");
    }
}

