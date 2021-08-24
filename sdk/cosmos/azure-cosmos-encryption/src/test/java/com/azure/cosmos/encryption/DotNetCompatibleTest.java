// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.encryption.TestSuiteBase.TestEncryptionKeyStoreProvider;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.azure.cosmos.encryption.TestSuiteBase.getPaths;
import static com.azure.cosmos.encryption.TestSuiteBase.validateResponse;
import static org.assertj.core.api.Assertions.assertThat;

public class DotNetCompatibleTest extends TestSuiteBase {
    private static final int TIMEOUT = 6000_000;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase cosmosAsyncDatabase;
    private CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;
    private CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer;

    @Factory(dataProvider = "clientBuilders")
    public DotNetCompatibleTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() throws IOException {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildAsyncClient();
        CosmosDatabaseProperties properties = new CosmosDatabaseProperties(UUID.randomUUID().toString());
        this.client.createDatabase(properties).block();
        cosmosAsyncDatabase = this.client.getDatabase(properties.getId());

        CosmosClientEncryptionKeyProperties keyProperties1 = getEncryptionPropertiesFromJsonFile("src/test/resources" +
            "/dotnetEncryption/ClientEncryptionKey1.json");
        CosmosClientEncryptionKeyProperties keyProperties2 = getEncryptionPropertiesFromJsonFile("src/test/resources" +
            "/dotnetEncryption/ClientEncryptionKey2.json");

        cosmosAsyncDatabase.createClientEncryptionKey(keyProperties1).block();
        cosmosAsyncDatabase.createClientEncryptionKey(keyProperties2).block();

        TestEncryptionKeyStoreProvider encryptionKeyStoreProvider =
            new TestEncryptionKeyStoreProvider();
        cosmosEncryptionAsyncClient = CosmosEncryptionAsyncClient.createCosmosEncryptionAsyncClient(this.client,
            encryptionKeyStoreProvider);

        cosmosEncryptionAsyncDatabase =
            cosmosEncryptionAsyncClient.getCosmosEncryptionAsyncDatabase(cosmosAsyncDatabase.getId());

        ClientEncryptionPolicy clientEncryptionPolicy =
            new ClientEncryptionPolicy(getPaths());
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, "/mypk");
        containerProperties.setClientEncryptionPolicy(clientEncryptionPolicy);
        cosmosEncryptionAsyncDatabase.getCosmosAsyncDatabase().createContainer(containerProperties).block();
        cosmosEncryptionAsyncContainer = cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        this.cosmosAsyncDatabase.delete().block();
        safeClose(this.client);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() throws IOException {
        JsonNode dotNetEncryptedPocoJsonNode = MAPPER.readTree(new File("src/test/resources/dotnetEncryption" +
            "/EncryptedPOCO.json"));


        //storing .net encrypted json into database as it is
        PartitionKey partitionKey = new PartitionKey(dotNetEncryptedPocoJsonNode.get("mypk").asText());
        this.cosmosEncryptionAsyncContainer.getCosmosAsyncContainer().createItem(dotNetEncryptedPocoJsonNode,
            partitionKey, new CosmosItemRequestOptions()).block();

        //reading above saved .net encrypted json via java encryption library
        EncryptionPojo unencryptedPojo =
            this.cosmosEncryptionAsyncContainer.readItem(dotNetEncryptedPocoJsonNode.get("id").asText(), partitionKey
                , new CosmosItemRequestOptions(), EncryptionPojo.class).block().getItem();

        JsonNode dotNetPOCOJsonNode = MAPPER.readTree(new File("src/test/resources/dotnetEncryption/POCO.json"));
        EncryptionPojo unencryptedPoco = MAPPER.treeToValue(dotNetPOCOJsonNode, EncryptionPojo.class);

        //validating java decrypted pojo similar to original .net unencrypted poco
        validateResponse(unencryptedPojo, unencryptedPoco);

    }

    private CosmosClientEncryptionKeyProperties getEncryptionPropertiesFromJsonFile(String filePath) throws IOException {
        JsonNode node = MAPPER.readTree(new File(filePath));
        String clientEncryptionKey = node.get("id").asText();
        String algorithm = node.get("encryptionAlgorithmId").asText();
        byte[] wrappedDataEncryptionKey = MAPPER.treeToValue(node.get("wrappedDataEncryptionKey"), byte[].class);
        ObjectNode objectNode = (ObjectNode) node.get("keyWrapMetadata");
        EncryptionKeyWrapMetadata keyWrapMetadata = MAPPER.convertValue(objectNode, EncryptionKeyWrapMetadata.class);
        return new CosmosClientEncryptionKeyProperties(clientEncryptionKey, algorithm,
            wrappedDataEncryptionKey,
            keyWrapMetadata);
    }

}
