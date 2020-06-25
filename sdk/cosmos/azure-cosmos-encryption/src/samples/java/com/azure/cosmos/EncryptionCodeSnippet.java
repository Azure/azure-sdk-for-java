// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;
import com.azure.cosmos.implementation.encryption.api.EncryptionOptions;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Code snippets for {@link ChangeFeedProcessor}
 */
public class EncryptionCodeSnippet {

    public void encryptionSample() {
        CosmosClientBuilder builder = new CosmosClientBuilder();

        CosmosClient client = builder.key("key")
            .endpoint("endpoint")
            .dataEncryptionKeyProvider(naiveDataEncryptionKeyProvider())
            .buildClient();

        CosmosContainer container = client.getDatabase("myDb").getContainer("myCol");

        Pojo pojo = new Pojo();
        pojo.id = UUID.randomUUID().toString();
        pojo.mypk = UUID.randomUUID().toString();
        pojo.nonSensitive = UUID.randomUUID().toString();
        pojo.sensitive1 = "this is a secret to be encrypted";
        pojo.sensitive2 = "this is a another secret to be encrypted";

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        EncryptionOptions encryptionOptions = new EncryptionOptions();
        encryptionOptions.setPathsToEncrypt(ImmutableList.of("/sensitive1", "/sensitive2"));
        ModelBridgeInternal.setEncryptionOptions(options, encryptionOptions);

        CosmosItemResponse<Pojo> response = container.createItem(pojo, options);

        assert response.getItem().nonSensitive != null;
        assert response.getItem().sensitive1 == null;
        assert response.getItem().sensitive2 == null;


        CosmosItemResponse<Pojo> readResponse = container.readItem(pojo.id, new PartitionKey(pojo.mypk), Pojo.class);

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
        public String id;
        @JsonProperty
        public String mypk;
        @JsonProperty
        public String nonSensitive;
        @JsonProperty
        public String sensitive1;
        @JsonProperty
        public String sensitive2;
    }

    private DataEncryptionKey createDataEncryptionKey() {
        byte[] key = DataEncryptionKey.generate("AEAes256CbcHmacSha256Randomized");
        return DataEncryptionKey.create(key, "AEAes256CbcHmacSha256Randomized");
    }
}

