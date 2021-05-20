// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.rx.TestSuiteBase;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
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
    private EncryptionKeyStoreProvider encryptionKeyStoreProvider;

    @Factory(dataProvider = "clientBuilders")
    public ClientEncryptionKeyTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        encryptionKeyStoreProvider = new EncryptionAsyncApiCrudTest.TestEncryptionKeyStoreProvider();
        cosmosAsyncDatabase = getSharedCosmosDatabase(this.client);
        cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(this.client,
            encryptionKeyStoreProvider);
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
            new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key1", "tempmetadata1");

        CosmosClientEncryptionKeyProperties clientEncryptionKey =
            cosmosEncryptionAsyncDatabase.createClientEncryptionKey("ClientEncryptionKeyTest1",
                CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata).block().getProperties();
        assertThat(clientEncryptionKey.getEncryptionKeyWrapMetadata()).isEqualTo(metadata);

        clientEncryptionKey =
            cosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey("ClientEncryptionKeyTest1", metadata).block().getProperties();
        assertThat(clientEncryptionKey.getEncryptionKeyWrapMetadata()).isEqualTo(metadata);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createClientEncryptionKeyWithException() {
        EncryptionKeyWrapMetadata metadata =
            new EncryptionKeyWrapMetadata("wrongName", "key1", "tempmetadata1");

        try {
            cosmosEncryptionAsyncDatabase.createClientEncryptionKey("ClientEncryptionKeyTest1",
                CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata).block().getProperties();
            fail("createClientEncryptionKey should fail as it has wrong encryptionKeyWrapMetadata type");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("The EncryptionKeyWrapMetadata Type value does not match with the " +
                "ProviderName of EncryptionKeyStoreProvider configured on the Client. Please refer to https://aka" +
                ".ms/CosmosClientEncryption for more details.");
        }

        try {
            cosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey("ClientEncryptionKeyTest1", metadata).block().getProperties();

        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("The EncryptionKeyWrapMetadata Type value does not match with the " +
                "ProviderName of EncryptionKeyStoreProvider configured on the Client. Please refer to https://aka" +
                ".ms/CosmosClientEncryption for more details.");
        }
    }
}
