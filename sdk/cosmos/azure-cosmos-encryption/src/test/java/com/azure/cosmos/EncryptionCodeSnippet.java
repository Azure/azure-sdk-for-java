// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.encryption.CosmosEncryptor;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.DataEncryptionKeyProvider;
import com.azure.cosmos.encryption.EncryptionItemRequestOptions;
import com.azure.cosmos.encryption.EncryptionOptions;
import com.azure.cosmos.encryption.WithEncryption;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;

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

        WithEncryption.withEncryptor(container, new CosmosEncryptor(naiveDataEncryptionKeyProvider()));

        Pojo pojo = new Pojo();
        pojo.id = UUID.randomUUID().toString();
        pojo.mypk = UUID.randomUUID().toString();
        pojo.nonSensitive = UUID.randomUUID().toString();
        pojo.sensitive1 = "this is a secret to be encrypted";
        pojo.sensitive2 = "this is a another secret to be encrypted";

        EncryptionItemRequestOptions options = new EncryptionItemRequestOptions();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(ImmutableList.of("/sensitive1", "/sensitive2"));
        options.setEncryptionOptions(encryptionOptions);

        CosmosItemResponse<Pojo> response = container.createItem(pojo, options).block();

        assert response.getItem().nonSensitive != null;
        assert response.getItem().sensitive1 == null;
        assert response.getItem().sensitive2 == null;


        CosmosItemResponse<Pojo> readResponse = container.readItem(pojo.id, new PartitionKey(pojo.mypk), Pojo.class).block();

        assert response.getItem().nonSensitive != null;
        assert response.getItem().sensitive1 != null;
        assert response.getItem().sensitive2 != null;
    }

    private DataEncryptionKeyProvider naiveDataEncryptionKeyProvider() {
        // this is a naive data encryption key provider which always uses the same data encryption key in memory.
        // the user should implement DataEncryptionKeyProvider as per use case;
        // storing data encryption keys should happen on the app side.
        DataEncryptionKey key = createDataEncryptionKey();

        return new DataEncryptionKeyProvider() {
            @Override
            public DataEncryptionKey getDataEncryptionKey(String id, String algorithm) {
                return key;
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

