// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.encryption.models.SqlQuerySpecWithEncryption;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchItemRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import io.netty.handler.codec.http.HttpResponseStatus;
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
    private CosmosEncryptionClient cosmosEncryptionClient;
    private CosmosEncryptionContainer cosmosEncryptionContainer;

    @Factory(dataProvider = "clientBuilders")
    public EncryptionSyncApiCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        EncryptionAsyncApiCrudTest.TestEncryptionKeyStoreProvider encryptionKeyStoreProvider =
            new EncryptionAsyncApiCrudTest.TestEncryptionKeyStoreProvider();
        this.cosmosEncryptionClient = CosmosEncryptionClient.createCosmosEncryptionClient(this.client,
            encryptionKeyStoreProvider);
        this.cosmosEncryptionContainer = getSharedSyncEncryptionContainer(this.cosmosEncryptionClient);
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

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void batchExecution() {
        String itemId= UUID.randomUUID().toString();
        EncryptionPojo createPojo = getItem(itemId);
        EncryptionPojo replacePojo =  getItem(itemId);
        replacePojo.setSensitiveString("ReplacedSensitiveString");
        CosmosBatch cosmosBatch = CosmosBatch.createCosmosBatch(new PartitionKey(itemId));
        cosmosBatch.createItemOperation(createPojo);
        cosmosBatch.replaceItemOperation(itemId, replacePojo);
        cosmosBatch.upsertItemOperation(createPojo);
        cosmosBatch.readItemOperation(itemId);
        cosmosBatch.deleteItemOperation(itemId);

        CosmosBatchResponse batchResponse = this.cosmosEncryptionContainer.executeCosmosBatch(cosmosBatch);
        assertThat(batchResponse.getResults().size()).isEqualTo(5);
        assertThat(batchResponse.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getResults().get(2).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getResults().get(3).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getResults().get(4).getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
        validateResponse(batchResponse.getResults().get(0).getItem(EncryptionPojo.class), createPojo);
        validateResponse(batchResponse.getResults().get(1).getItem(EncryptionPojo.class), replacePojo);
        validateResponse(batchResponse.getResults().get(2).getItem(EncryptionPojo.class), createPojo);
        validateResponse(batchResponse.getResults().get(3).getItem(EncryptionPojo.class), createPojo);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void batchExecutionWithOptionsApi() {
        String itemId= UUID.randomUUID().toString();
        EncryptionPojo createPojo = getItem(itemId);
        EncryptionPojo replacePojo =  getItem(itemId);
        replacePojo.setSensitiveString("ReplacedSensitiveString");
        CosmosBatch cosmosBatch = CosmosBatch.createCosmosBatch(new PartitionKey(itemId));
        CosmosBatchItemRequestOptions cosmosBatchItemRequestOptions = new CosmosBatchItemRequestOptions();

        cosmosBatch.createItemOperation(createPojo, cosmosBatchItemRequestOptions);
        cosmosBatch.replaceItemOperation(itemId, replacePojo,cosmosBatchItemRequestOptions);
        cosmosBatch.upsertItemOperation(createPojo, cosmosBatchItemRequestOptions);
        cosmosBatch.readItemOperation(itemId, cosmosBatchItemRequestOptions);
        cosmosBatch.deleteItemOperation(itemId, cosmosBatchItemRequestOptions);

        CosmosBatchResponse batchResponse = this.cosmosEncryptionContainer.executeCosmosBatch(cosmosBatch);
        assertThat(batchResponse.getResults().size()).isEqualTo(5);
        assertThat(batchResponse.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
        assertThat(batchResponse.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getResults().get(2).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getResults().get(3).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        assertThat(batchResponse.getResults().get(4).getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
        validateResponse(batchResponse.getResults().get(0).getItem(EncryptionPojo.class), createPojo);
        validateResponse(batchResponse.getResults().get(1).getItem(EncryptionPojo.class), replacePojo);
        validateResponse(batchResponse.getResults().get(2).getItem(EncryptionPojo.class), createPojo);
        validateResponse(batchResponse.getResults().get(3).getItem(EncryptionPojo.class), createPojo);
    }
}
