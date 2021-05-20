// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.encryption.models.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.models.CosmosEncryptionType;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers.CosmosClientHelper.CosmosClientAccessor;
import com.azure.cosmos.models.ClientEncryptionIncludedPath;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionSyncApiCrudTest extends TestSuiteBase {
    private CosmosClient client;
    private CosmosDatabase cosmosDatabase;
    private static final int TIMEOUT = 6000_000;
    private CosmosEncryptionClient cosmosEncryptionClient;
    private CosmosEncryptionDatabase cosmosEncryptionDatabase;
    private CosmosEncryptionContainer cosmosEncryptionContainer;
    private EncryptionKeyWrapMetadata metadata1;
    private EncryptionKeyWrapMetadata metadata2;
    private CosmosClientAccessor cosmosClientAccessor;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionSyncApiCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.cosmosClientAccessor = CosmosClientHelper.geCosmosClientAccessor();
        this.client = getClientBuilder().buildClient();
        this.cosmosDatabase =
            this.client.getDatabase(getSharedCosmosDatabase(this.cosmosClientAccessor.getCosmosAsyncClient(this.client)).getId());
        EncryptionAsyncApiCrudTest.TestEncryptionKeyStoreProvider encryptionKeyStoreProvider =
            new EncryptionAsyncApiCrudTest.TestEncryptionKeyStoreProvider();
        this.cosmosEncryptionClient = CosmosEncryptionClient.createCosmosEncryptionClient(this.client,
            encryptionKeyStoreProvider);
        this.cosmosEncryptionDatabase =
            cosmosEncryptionClient.getCosmosEncryptionDatabase(this.cosmosDatabase);

        metadata1 = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key1", "tempmetadata1");
        metadata2 = new EncryptionKeyWrapMetadata(encryptionKeyStoreProvider.getProviderName(), "key2", "tempmetadata2");
        this.cosmosEncryptionDatabase.createClientEncryptionKey("key3",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata1);
        this.cosmosEncryptionDatabase.createClientEncryptionKey("key4",
            CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256, metadata2);

        ClientEncryptionPolicy clientEncryptionPolicy = new ClientEncryptionPolicy(getPaths());
        String containerId = UUID.randomUUID().toString();
        CosmosContainerProperties properties = new CosmosContainerProperties(containerId, "/mypk");
        properties.setClientEncryptionPolicy(clientEncryptionPolicy);
        this.cosmosEncryptionDatabase.getCosmosDatabase().createContainer(properties);
        this.cosmosEncryptionContainer = this.cosmosEncryptionDatabase.getCosmosEncryptionAsyncContainer(containerId);
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() {
        EncryptionPojo properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, responseItem);

        EncryptionPojo readItem = this.cosmosEncryptionContainer.readItem(properties.getId(),
            new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions(), EncryptionPojo.class).getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, readItem);

        //Check for length support greater than 8000
        properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        String longString = "";
        for (int i = 0; i < 10000; i++) {
            longString += "a";
        }
        properties.setSensitiveString(longString);
        itemResponse = cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        responseItem = itemResponse.getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, responseItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void upsertItem_readItem() {
        EncryptionPojo properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.upsertItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, responseItem);

        EncryptionPojo readItem = this.cosmosEncryptionContainer.readItem(properties.getId(),
            new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions(), EncryptionPojo.class).getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, readItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItems() {
        EncryptionPojo properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, responseItem);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<EncryptionPojo> feedResponseIterator =
            this.cosmosEncryptionContainer.queryItems(querySpec, cosmosQueryRequestOptions,
                EncryptionPojo.class);
        List<EncryptionPojo> feedResponse = new ArrayList<>();
        feedResponseIterator.iterator().forEachRemaining(pojo -> {
            feedResponse.add(pojo);
        });
        assertThat(feedResponse.size()).isGreaterThanOrEqualTo(1);
        for (EncryptionPojo pojo : feedResponse) {
            if (pojo.getId().equals(properties.getId())) {
                EncryptionAsyncApiCrudTest.validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnEncryptedProperties() {
        EncryptionPojo properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, responseItem);

        String query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive =" +
            " " +
            "@nonSensitive and c.sensitiveLong = @sensitiveLong");
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        SqlParameter parameter1 = new SqlParameter("@nonSensitive", properties.getNonSensitive());
        querySpec.getParameters().add(parameter1);

        SqlParameter parameter2 = new SqlParameter("@sensitiveString", properties.getSensitiveString());
        SqlParameter parameter3 = new SqlParameter("@sensitiveLong", properties.getSensitiveLong());
        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(querySpec);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveString", parameter2);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveLong", parameter3);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedIterable<EncryptionPojo> feedResponseIterator =
            this.cosmosEncryptionContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                cosmosQueryRequestOptions, EncryptionPojo.class);
        List<EncryptionPojo> feedResponse = new ArrayList<>();
        feedResponseIterator.iterator().forEachRemaining(pojo -> {
            feedResponse.add(pojo);
        });
        assertThat(feedResponse.size()).isGreaterThanOrEqualTo(1);
        for (EncryptionPojo pojo : feedResponse) {
            if (pojo.getId().equals(properties.getId())) {
                EncryptionAsyncApiCrudTest.validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnRandomizedEncryption() {
        EncryptionPojo properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        EncryptionAsyncApiCrudTest.validateResponse(properties, responseItem);

        String query = String.format("SELECT * FROM c where c.sensitiveString = @sensitiveString and c.nonSensitive =" +
            " " +
            "@nonSensitive and c.sensitiveDouble = @sensitiveDouble");
        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        SqlParameter parameter1 = new SqlParameter("@nonSensitive", properties.getNonSensitive());
        querySpec.getParameters().add(parameter1);

        SqlParameter parameter2 = new SqlParameter("@sensitiveString", properties.getSensitiveString());
        SqlParameter parameter3 = new SqlParameter("@sensitiveDouble", properties.getSensitiveDouble());
        SqlQuerySpecWithEncryption sqlQuerySpecWithEncryption = new SqlQuerySpecWithEncryption(querySpec);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveString", parameter2);
        sqlQuerySpecWithEncryption.addEncryptionParameter("/sensitiveDouble", parameter3);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedIterable<EncryptionPojo> feedResponseIterator =
            this.cosmosEncryptionContainer.queryItemsOnEncryptedProperties(sqlQuerySpecWithEncryption,
                cosmosQueryRequestOptions, EncryptionPojo.class);
        try {
            List<EncryptionPojo> feedResponse = new ArrayList<>();
            feedResponseIterator.iterator().forEachRemaining(pojo -> {
                feedResponse.add(pojo);
            });
            fail("Query on randomized parameter should fail");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).contains("Path /sensitiveDouble cannot be used in the " +
                "query because of randomized encryption");
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() throws Exception {
        List<String> actualIds = new ArrayList<>();
        EncryptionPojo properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        this.cosmosEncryptionContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions());
        actualIds.add(properties.getId());
        properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        this.cosmosEncryptionContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions());
        actualIds.add(properties.getId());
        properties = EncryptionAsyncApiCrudTest.getItem(UUID.randomUUID().toString());
        this.cosmosEncryptionContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions());
        actualIds.add(properties.getId());

        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0),
            actualIds.get(1), actualIds.get(2));
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<EncryptionPojo> pojoCosmosPagedIterable =
            this.cosmosEncryptionContainer.queryItems(query, cosmosQueryRequestOptions, EncryptionPojo.class);

        do {
            Iterable<FeedResponse<EncryptionPojo>> feedResponseIterable =
                pojoCosmosPagedIterable.iterableByPage(continuationToken, 1);
            for (FeedResponse<EncryptionPojo> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while (continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);
    }

    private List<ClientEncryptionIncludedPath> getPaths() {
        ClientEncryptionIncludedPath includedPath1 = new ClientEncryptionIncludedPath();
        includedPath1.setClientEncryptionKeyId("key3");
        includedPath1.setPath("/sensitiveString");
        includedPath1.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath1.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath2 = new ClientEncryptionIncludedPath();
        includedPath2.setClientEncryptionKeyId("key4");
        includedPath2.setPath("/nonValidPath");
        includedPath2.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath2.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath3 = new ClientEncryptionIncludedPath();
        includedPath3.setClientEncryptionKeyId("key3");
        includedPath3.setPath("/sensitiveInt");
        includedPath3.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath3.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath4 = new ClientEncryptionIncludedPath();
        includedPath4.setClientEncryptionKeyId("key4");
        includedPath4.setPath("/sensitiveFloat");
        includedPath4.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath4.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath5 = new ClientEncryptionIncludedPath();
        includedPath5.setClientEncryptionKeyId("key3");
        includedPath5.setPath("/sensitiveLong");
        includedPath5.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath5.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath6 = new ClientEncryptionIncludedPath();
        includedPath6.setClientEncryptionKeyId("key4");
        includedPath6.setPath("/sensitiveDouble");
        includedPath6.setEncryptionType(CosmosEncryptionType.RANDOMIZED);
        includedPath6.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath7 = new ClientEncryptionIncludedPath();
        includedPath7.setClientEncryptionKeyId("key3");
        includedPath7.setPath("/sensitiveBoolean");
        includedPath7.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath7.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath8 = new ClientEncryptionIncludedPath();
        includedPath8.setClientEncryptionKeyId("key3");
        includedPath8.setPath("/sensitiveNestedPojo");
        includedPath8.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath8.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath9 = new ClientEncryptionIncludedPath();
        includedPath9.setClientEncryptionKeyId("key3");
        includedPath9.setPath("/sensitiveIntArray");
        includedPath9.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath9.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath10 = new ClientEncryptionIncludedPath();
        includedPath10.setClientEncryptionKeyId("key4");
        includedPath10.setPath("/sensitiveString3DArray");
        includedPath10.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath10.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath11 = new ClientEncryptionIncludedPath();
        includedPath11.setClientEncryptionKeyId("key3");
        includedPath11.setPath("/sensitiveStringArray");
        includedPath11.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath11.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath12 = new ClientEncryptionIncludedPath();
        includedPath12.setClientEncryptionKeyId("key3");
        includedPath12.setPath("/sensitiveChildPojoList");
        includedPath12.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath12.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        ClientEncryptionIncludedPath includedPath13 = new ClientEncryptionIncludedPath();
        includedPath13.setClientEncryptionKeyId("key3");
        includedPath13.setPath("/sensitiveChildPojo2DArray");
        includedPath13.setEncryptionType(CosmosEncryptionType.DETERMINISTIC);
        includedPath13.setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256);

        List<ClientEncryptionIncludedPath> paths = new ArrayList<>();
        paths.add(includedPath1);
        paths.add(includedPath2);
        paths.add(includedPath3);
        paths.add(includedPath4);
        paths.add(includedPath5);
        paths.add(includedPath6);
        paths.add(includedPath7);
        paths.add(includedPath8);
        paths.add(includedPath9);
        paths.add(includedPath10);
        paths.add(includedPath11);
        paths.add(includedPath12);
        paths.add(includedPath13);

        return paths;
    }
}
