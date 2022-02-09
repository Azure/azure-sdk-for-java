// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosClientEncryptionKeyTest extends TestSuiteBase {
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosClientEncryptionKeyTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosDatabaseTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = {"emulator"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        for (String dbId : databases) {
            safeDeleteDatabase(client.getDatabase(dbId));
        }
        safeClose(client);
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void createClientEncryptionKey() {
        EncryptionKeyWrapMetadata encryptionKeyWrapMetadata = new EncryptionKeyWrapMetadata("key1", "tempmetadata1", "custom");
        byte[] key = decodeHexString(("34 62 52 77 f9 ee 11 9f 04 8c 6f 50 9c e4 c2 5b b3 39 f4 d0 4d c1 6a 32 fa 2b 3b aa " +
            "ae 1e d9 1c").replace(" ", ""));

        CosmosClientEncryptionKeyProperties cosmosClientEncryptionKeyProperties =
            new CosmosClientEncryptionKeyProperties("key1", "AEAD_AES_256_CBC_HMAC_SHA256", key,
                encryptionKeyWrapMetadata);
        CosmosClientEncryptionKeyResponse keyResponse =
            createdDatabase.createClientEncryptionKey(cosmosClientEncryptionKeyProperties).block();
        System.out.println("CosmosDatabaseTest.createDatabase_withClientEncryptionKey " + keyResponse.getProperties().getId());
        validateClientEncryptionKeyResponse(cosmosClientEncryptionKeyProperties, keyResponse.getProperties());

        CosmosAsyncClientEncryptionKey clientEncryptionKey = createdDatabase.getClientEncryptionKey("key1");
        keyResponse = clientEncryptionKey.read().block();
        validateClientEncryptionKeyResponse(cosmosClientEncryptionKeyProperties, keyResponse.getProperties());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void replaceClientEncryptionKey() {
        EncryptionKeyWrapMetadata encryptionKeyWrapMetadata = new EncryptionKeyWrapMetadata("custom", "key2", "tempmetadata1");
        byte[] key = decodeHexString(("34 62 52 77 f9 ee 11 9f 04 8c 6f 50 9c e4 c2 5b b3 39 f4 d0 4d c1 6a 32 fa 2b 3b aa " +
            "ae 1e d9 1c").replace(" ", ""));

        CosmosClientEncryptionKeyProperties cosmosClientEncryptionKeyProperties =
            new CosmosClientEncryptionKeyProperties("key2", "AEAD_AES_256_CBC_HMAC_SHA256", key,
                encryptionKeyWrapMetadata);
        CosmosClientEncryptionKeyResponse keyResponse =
            createdDatabase.createClientEncryptionKey(cosmosClientEncryptionKeyProperties).block();
        System.out.println("CosmosDatabaseTest.createDatabase_withClientEncryptionKey " + keyResponse.getProperties().getId());
        validateClientEncryptionKeyResponse(cosmosClientEncryptionKeyProperties, keyResponse.getProperties());

        CosmosAsyncClientEncryptionKey clientEncryptionKey = createdDatabase.getClientEncryptionKey("key2");

        encryptionKeyWrapMetadata = new EncryptionKeyWrapMetadata("custom", "key2", "tempmetadata2");
        cosmosClientEncryptionKeyProperties = keyResponse.getProperties();
        cosmosClientEncryptionKeyProperties.setEncryptionKeyWrapMetadata(encryptionKeyWrapMetadata);
        keyResponse = clientEncryptionKey.replace(cosmosClientEncryptionKeyProperties).block();
        validateClientEncryptionKeyResponse(cosmosClientEncryptionKeyProperties, keyResponse.getProperties());
    }

    @Test(groups = {"emulator"}, timeOut = TIMEOUT)
    public void queryClientEncryptionKeys() {
        EncryptionKeyWrapMetadata encryptionKeyWrapMetadata = new EncryptionKeyWrapMetadata("custom", "key3", "tempmetadata1");
        byte[] key = decodeHexString(("34 62 52 77 f9 ee 11 9f 04 8c 6f 50 9c e4 c2 5b b3 39 f4 d0 4d c1 6a 32 fa 2b 3b aa " +
            "ae 1e d9 1c").replace(" ", ""));

        CosmosClientEncryptionKeyProperties cosmosClientEncryptionKeyProperties =
            new CosmosClientEncryptionKeyProperties("key3", "AEAD_AES_256_CBC_HMAC_SHA256", key,
                encryptionKeyWrapMetadata);
        CosmosClientEncryptionKeyResponse keyResponse =
            createdDatabase.createClientEncryptionKey(cosmosClientEncryptionKeyProperties).block();
        System.out.println("CosmosDatabaseTest.createDatabase_withClientEncryptionKey " + keyResponse.getProperties().getId());
        validateClientEncryptionKeyResponse(cosmosClientEncryptionKeyProperties, keyResponse.getProperties());

        FeedResponse<CosmosClientEncryptionKeyProperties> feedResponse =
            createdDatabase.readAllClientEncryptionKeys().byPage().blockFirst();
        assertThat(feedResponse.getResults().size()).isGreaterThanOrEqualTo(1);
        for (CosmosClientEncryptionKeyProperties properties : feedResponse.getResults()) {
            if (properties.getId().equals("key3")) {
                validateClientEncryptionKeyResponse(cosmosClientEncryptionKeyProperties, properties);
            }
        }

        //TODO query not working for, error from BE service
//        feedResponse = createdDatabase.queryClientEncryptionKeys("Select * from c").byPage().blockFirst();
//        assertThat(feedResponse.getResults().size()).isGreaterThanOrEqualTo(1);
//        // ClientEncryptionKey
//        for (CosmosClientEncryptionKeyProperties properties : feedResponse.getResults()) {
//            if (properties.getId().equals("key3")) {
//                validateClientEncryptionKeyResponse(cosmosClientEncryptionKeyProperties, properties);
//            }
//        }
    }

    private void validateClientEncryptionKeyResponse(CosmosClientEncryptionKeyProperties keyProperties,
                                                     CosmosClientEncryptionKeyProperties createResponse) {
        assertThat(createResponse.getId()).isEqualTo(keyProperties.getId());
        assertThat(createResponse.getEncryptionAlgorithm()).isEqualTo(keyProperties.getEncryptionAlgorithm());
        assertThat(createResponse.getWrappedDataEncryptionKey()).isEqualTo(keyProperties.getWrappedDataEncryptionKey());
        assertThat(createResponse.getEncryptionKeyWrapMetadata()).isEqualTo(keyProperties.getEncryptionKeyWrapMetadata());
    }
}
