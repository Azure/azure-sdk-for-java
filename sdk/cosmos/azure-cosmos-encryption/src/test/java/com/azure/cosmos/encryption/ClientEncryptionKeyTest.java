// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClientEncryptionKeyTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;

    @Factory(dataProvider = "clientBuilders")
    public ClientEncryptionKeyTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        cosmosAsyncDatabase = getSharedCosmosDatabase(this.client);
        cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder().cosmosAsyncClient(this.client).keyEncryptionKeyResolver(
            new TestKeyEncryptionKeyResolver()).keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildAsyncClient();
        cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(cosmosAsyncDatabase);
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }


    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createClientEncryptionKey() {
        EncryptionKeyWrapMetadata metadata =
            new EncryptionKeyWrapMetadata(cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolverName(), "key1",
                "tempmetadata1", "RSA-OAEP");

        CosmosClientEncryptionKeyProperties clientEncryptionKey =
            cosmosEncryptionAsyncDatabase.createClientEncryptionKey("ClientEncryptionKeyTest1",
                CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata).block().getProperties();
        assertThat(clientEncryptionKey.getEncryptionKeyWrapMetadata()).isEqualTo(metadata);

        clientEncryptionKey =
            cosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey("ClientEncryptionKeyTest1", metadata).block().getProperties();
        assertThat(clientEncryptionKey.getEncryptionKeyWrapMetadata()).isEqualTo(metadata);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createClientEncryptionKeyWithException() {
        EncryptionKeyWrapMetadata metadata =
            new EncryptionKeyWrapMetadata("wrongName", "key1", "tempmetadata1", "RSA-OAEP");

        try {
            cosmosEncryptionAsyncDatabase.createClientEncryptionKey("ClientEncryptionKeyTest1",
                CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata).block().getProperties();
            fail("createClientEncryptionKey should fail as it has wrong encryptionKeyWrapMetadata type");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("The EncryptionKeyWrapMetadata Type value does not match with the " +
                "keyEncryptionKeyResolverName configured on the Client. Please refer to https://aka" +
                ".ms/CosmosClientEncryption for more details.");
        }

        try {
            cosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey("ClientEncryptionKeyTest1", metadata).block().getProperties();
            fail("rewrapClientEncryptionKey should fail as it has wrong encryptionKeyWrapMetadata type");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("The EncryptionKeyWrapMetadata Type value does not match with the " +
                "keyEncryptionKeyResolverName configured on the Client. Please refer to https://aka" +
                ".ms/CosmosClientEncryption for more details.");
        }

        metadata =
            new EncryptionKeyWrapMetadata(this.cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolverName(), "key1",
                "tempmetadata1", "WrongAlgoName");
        try {
            cosmosEncryptionAsyncDatabase.createClientEncryptionKey("ClientEncryptionKeyTest1",
                CosmosEncryptionAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.getName(), metadata).block().getProperties();
            fail("createClientEncryptionKey should fail as it has wrong encryptionKeyWrapMetadata algorithm");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid Key Encryption Key Algorithm in EncryptionKeyWrapMetadata " +
                "'WrongAlgoName'");
        }

        try {
            cosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey("ClientEncryptionKeyTest1", metadata).block().getProperties();
            fail("rewrapClientEncryptionKey should fail as it has wrong encryptionKeyWrapMetadata algorithm");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Invalid Key Encryption Key Algorithm in EncryptionKeyWrapMetadata " +
                "'WrongAlgoName'");
        }
    }
}
