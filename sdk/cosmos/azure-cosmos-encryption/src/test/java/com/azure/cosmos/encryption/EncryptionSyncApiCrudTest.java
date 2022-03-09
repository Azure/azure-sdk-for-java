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
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.util.CosmosPagedIterable;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class EncryptionSyncApiCrudTest extends TestSuiteBase {
    private CosmosClient client;
    private CosmosEncryptionClient cosmosEncryptionClient;
    private CosmosEncryptionContainer cosmosEncryptionContainer;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public EncryptionSyncApiCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"encryption"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        EncryptionAsyncApiCrudTest.TestKeyEncryptionKeyResolver keyEncryptionKeyResolver =
            new EncryptionAsyncApiCrudTest.TestKeyEncryptionKeyResolver();
        this.cosmosEncryptionClient = new CosmosEncryptionClientBuilder().cosmosClient(this.client).keyEncryptionKeyResolver(
            keyEncryptionKeyResolver).keyEncryptionKeyResolverName("TEST_KEY_RESOLVER").buildClient();
        this.cosmosEncryptionContainer = getSharedSyncEncryptionContainer(this.cosmosEncryptionClient);
    }

    @AfterClass(groups = {"encryption"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void createItemEncrypt_readItemDecrypt() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        EncryptionPojo readItem = this.cosmosEncryptionContainer.readItem(properties.getId(),
            new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions(), EncryptionPojo.class).getItem();
        validateResponse(properties, readItem);

        //Check for length support greater than 8000
        properties = getItem(UUID.randomUUID().toString());
        String longString = "";
        for (int i = 0; i < 10000; i++) {
            longString += "a";
        }
        properties.setSensitiveString(longString);
        itemResponse = cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void upsertItem_readItem() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.upsertItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

        EncryptionPojo readItem = this.cosmosEncryptionContainer.readItem(properties.getId(),
            new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions(), EncryptionPojo.class).getItem();
        validateResponse(properties, readItem);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItems() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

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
                validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnEncryptedProperties() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

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
                validateResponse(pojo, responseItem);
            }
        }
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void queryItemsOnRandomizedEncryption() {
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties,
            new PartitionKey(properties.getMypk()), new CosmosItemRequestOptions());
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);

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
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        this.cosmosEncryptionContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions());
        actualIds.add(properties.getId());
        properties = getItem(UUID.randomUUID().toString());
        this.cosmosEncryptionContainer.createItem(properties, new PartitionKey(properties.getMypk()),
            new CosmosItemRequestOptions());
        actualIds.add(properties.getId());
        properties = getItem(UUID.randomUUID().toString());
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
    public void crudOnDifferentOverload() {
        List<EncryptionPojo> actualProperties = new ArrayList<>();
        // Read item
        EncryptionPojo properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse = this.cosmosEncryptionContainer.createItem(properties);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem = itemResponse.getItem();
        validateResponse(properties, responseItem);
        actualProperties.add(properties);

        properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> itemResponse1 = this.cosmosEncryptionContainer.createItem(properties, new CosmosItemRequestOptions());
        assertThat(itemResponse1.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem1 = itemResponse1.getItem();
        validateResponse(properties, responseItem1);
        actualProperties.add(properties);

        //Upsert Item
        properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> upsertResponse1 = this.cosmosEncryptionContainer.upsertItem(properties);
        assertThat(upsertResponse1.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem2 = upsertResponse1.getItem();
        validateResponse(properties, responseItem2);
        actualProperties.add(properties);

        properties = getItem(UUID.randomUUID().toString());
        CosmosItemResponse<EncryptionPojo> upsertResponse2 = this.cosmosEncryptionContainer.upsertItem(properties, new CosmosItemRequestOptions());
        assertThat(upsertResponse2.getRequestCharge()).isGreaterThan(0);
        EncryptionPojo responseItem3 = upsertResponse2.getItem();
        validateResponse(properties, responseItem3);
        actualProperties.add(properties);

        //Read Item
        EncryptionPojo readItem = this.cosmosEncryptionContainer.readItem(actualProperties.get(0).getId(),
            new PartitionKey(actualProperties.get(0).getMypk()), EncryptionPojo.class).getItem();
        validateResponse(actualProperties.get(0), readItem);

        //Query Item
        String query = String.format("SELECT * from c where c.id = '%s'", actualProperties.get(1).getId());

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        CosmosPagedIterable<EncryptionPojo> feedResponseIterator =
            this.cosmosEncryptionContainer.queryItems(query, cosmosQueryRequestOptions, EncryptionPojo.class);
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

        CosmosQueryRequestOptions cosmosQueryRequestOptions1 = new CosmosQueryRequestOptions();
        SqlQuerySpec querySpec = new SqlQuerySpec(query);

        CosmosPagedIterable<EncryptionPojo> feedResponseIterator2 =
            this.cosmosEncryptionContainer.queryItems(querySpec, cosmosQueryRequestOptions1, EncryptionPojo.class);
        List<EncryptionPojo> feedResponse2 = new ArrayList<>();
        feedResponseIterator2.iterator().forEachRemaining(pojo -> {
            feedResponse2.add(pojo);
        });
        assertThat(feedResponse2.size()).isGreaterThanOrEqualTo(1);
        for (EncryptionPojo pojo : feedResponse2) {
            if (pojo.getId().equals(properties.getId())) {
                EncryptionAsyncApiCrudTest.validateResponse(pojo, responseItem);
            }
        }

        //Replace Item
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        CosmosItemResponse<EncryptionPojo> replaceResponse =
            this.cosmosEncryptionContainer.replaceItem(actualProperties.get(2), actualProperties.get(2).getId(),
                new PartitionKey(actualProperties.get(2).getMypk()), requestOptions);
        assertThat(upsertResponse1.getRequestCharge()).isGreaterThan(0);
        responseItem = replaceResponse.getItem();
        validateResponse(actualProperties.get(2), responseItem);

        //Delete Item
        CosmosItemResponse<?> deleteResponse1 = this.cosmosEncryptionContainer.deleteItem(actualProperties.get(1).getId(),
            new PartitionKey(actualProperties.get(1).getMypk()), new CosmosItemRequestOptions());
        assertThat(deleteResponse1.getStatusCode()).isEqualTo(204);

        CosmosItemResponse<?> deleteResponse2 = this.cosmosEncryptionContainer.deleteItem(actualProperties.get(2),
            new CosmosItemRequestOptions());
        assertThat(deleteResponse2.getStatusCode()).isEqualTo(204);

        CosmosItemResponse<?> deleteResponse3 = this.cosmosEncryptionContainer.deleteAllItemsByPartitionKey(new PartitionKey(actualProperties.get(3).getMypk()),
            new CosmosItemRequestOptions());
        assertThat(deleteResponse3.getStatusCode()).isEqualTo(200);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void batchExecution() {
        String itemId = UUID.randomUUID().toString();
        EncryptionPojo createPojo = getItem(itemId);
        EncryptionPojo replacePojo = getItem(itemId);
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
        String itemId = UUID.randomUUID().toString();
        EncryptionPojo createPojo = getItem(itemId);
        EncryptionPojo replacePojo = getItem(itemId);
        replacePojo.setSensitiveString("ReplacedSensitiveString");
        CosmosBatch cosmosBatch = CosmosBatch.createCosmosBatch(new PartitionKey(itemId));
        CosmosBatchItemRequestOptions cosmosBatchItemRequestOptions = new CosmosBatchItemRequestOptions();

        cosmosBatch.createItemOperation(createPojo, cosmosBatchItemRequestOptions);
        cosmosBatch.replaceItemOperation(itemId, replacePojo, cosmosBatchItemRequestOptions);
        cosmosBatch.upsertItemOperation(createPojo, cosmosBatchItemRequestOptions);
        cosmosBatch.readItemOperation(itemId, cosmosBatchItemRequestOptions);
        cosmosBatch.deleteItemOperation(itemId, cosmosBatchItemRequestOptions);

        CosmosBatchResponse batchResponse = this.cosmosEncryptionContainer.executeCosmosBatch(cosmosBatch, new CosmosBatchRequestOptions());
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
    public void patchItem() {
        String itemId = UUID.randomUUID().toString();
        EncryptionPojo createPojo = getItem(itemId);
        CosmosItemResponse<EncryptionPojo> itemResponse = cosmosEncryptionContainer.createItem(createPojo,
            new PartitionKey(createPojo.getMypk()), new CosmosItemRequestOptions());

        int originalSensitiveInt = createPojo.getSensitiveInt();
        int newSensitiveInt = originalSensitiveInt + 1;

        String itemIdToReplace = UUID.randomUUID().toString();
        EncryptionPojo nestedEncryptionPojoToReplace = getItem(itemIdToReplace);
        nestedEncryptionPojoToReplace.setSensitiveString("testing");

        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();
        cosmosPatchOperations.add("/sensitiveString", "patched");
        cosmosPatchOperations.remove("/sensitiveDouble");
        cosmosPatchOperations.replace("/sensitiveInt", newSensitiveInt);
        cosmosPatchOperations.replace("/sensitiveNestedPojo", nestedEncryptionPojoToReplace);
        cosmosPatchOperations.set("/sensitiveBoolean", false);

        CosmosPatchItemRequestOptions options = new CosmosPatchItemRequestOptions();
        CosmosItemResponse<EncryptionPojo> response = this.cosmosEncryptionContainer.patchItem(
            createPojo.getId(),
            new PartitionKey(createPojo.getMypk()),
            cosmosPatchOperations,
            options,
            EncryptionPojo.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

        EncryptionPojo patchedItem = response.getItem();
        assertThat(patchedItem).isNotNull();

        assertThat(patchedItem.getSensitiveString()).isEqualTo("patched");
        assertThat(patchedItem.getSensitiveDouble()).isNull();
        assertThat(patchedItem.getSensitiveNestedPojo()).isNotNull();
        assertThat(patchedItem.getSensitiveInt()).isEqualTo(newSensitiveInt);
        assertThat(patchedItem.isSensitiveBoolean()).isEqualTo(false);

        response = this.cosmosEncryptionContainer.readItem(
            createPojo.getId(),
            new PartitionKey(createPojo.getMypk()),
            options,
            EncryptionPojo.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
        validateResponse(patchedItem, response.getItem());
    }

    private int getTotalRequest() {
        int countRequest = new Random().nextInt(100) + 120;
        logger.info("Total count of request for this test case: " + countRequest);

        return countRequest;
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void bulkExecution_createItem() {
        int totalRequest = getTotalRequest();
        Map<String, EncryptionPojo> idToItemMap = new HashMap<>();
        List<CosmosItemOperation> cosmosItemOperationsList = new ArrayList<>();
        for (int i = 0; i < totalRequest; i++) {
            String itemId = UUID.randomUUID().toString();
            EncryptionPojo createPojo = getItem(itemId);

            idToItemMap.put(itemId, createPojo);
            cosmosItemOperationsList.add(CosmosBulkOperations.getCreateItemOperation(createPojo, new PartitionKey(createPojo.getMypk())));
        }

        List<CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest>> bulkResponse = Lists.newArrayList(this.cosmosEncryptionContainer.
            executeBulkOperations(cosmosItemOperationsList));

        AtomicInteger processedDoc = new AtomicInteger(0);
        for (CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest> cosmosBulkOperationResponse : bulkResponse) {

            processedDoc.incrementAndGet();

            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            if (cosmosBulkOperationResponse.getException() != null) {
                logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                fail(cosmosBulkOperationResponse.getException().toString());
            }

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

            EncryptionPojo item = cosmosBulkItemResponse.getItem(EncryptionPojo.class);
            validateResponse(item, idToItemMap.get(item.getId()));
        }

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void bulkExecution_upsertItem() {
        int totalRequest = getTotalRequest();

        Map<String, EncryptionPojo> idToItemMap = new HashMap<>();
        List<CosmosItemOperation> cosmosItemOperationsList = new ArrayList<>();
        for (int i = 0; i < totalRequest; i++) {
            String itemId = UUID.randomUUID().toString();
            EncryptionPojo createPojo = getItem(itemId);

            idToItemMap.put(itemId, createPojo);
            cosmosItemOperationsList.add(CosmosBulkOperations.getUpsertItemOperation(createPojo, new PartitionKey(createPojo.getMypk())));
        }

        List<CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest>> bulkResponse = Lists.newArrayList(this.cosmosEncryptionContainer.
            executeBulkOperations(cosmosItemOperationsList, new CosmosBulkExecutionOptions()));

        AtomicInteger processedDoc = new AtomicInteger(0);
        for (CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest> cosmosBulkOperationResponse : bulkResponse) {

            processedDoc.incrementAndGet();

            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            if (cosmosBulkOperationResponse.getException() != null) {
                logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                fail(cosmosBulkOperationResponse.getException().toString());
            }

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

            EncryptionPojo item = cosmosBulkItemResponse.getItem(EncryptionPojo.class);
            validateResponse(item, idToItemMap.get(item.getId()));
        }
        ;

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void bulkExecution_deleteItem() {
        int totalRequest = Math.min(getTotalRequest(), 20);

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        for (int i = 0; i < totalRequest; i++) {
            String itemId = UUID.randomUUID().toString();
            EncryptionPojo createPojo = getItem(itemId);

            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(createPojo, new PartitionKey(createPojo.getMypk())));
        }

        createItemsAndVerify(cosmosItemOperations);

        List<CosmosItemOperation> deleteCosmosItemOperations = new ArrayList<>();
        for (CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            EncryptionPojo encryptionPojo = cosmosItemOperation.getItem();
            deleteCosmosItemOperations.add(CosmosBulkOperations.getDeleteItemOperation(encryptionPojo.getId(), cosmosItemOperation.getPartitionKeyValue()));
        }

        List<CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest>> bulkResponse = Lists.newArrayList(this.cosmosEncryptionContainer
            .executeBulkOperations(deleteCosmosItemOperations));

        AtomicInteger processedDoc = new AtomicInteger(0);
        for (CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest> cosmosBulkOperationResponse : bulkResponse) {

            processedDoc.incrementAndGet();

            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            if (cosmosBulkOperationResponse.getException() != null) {
                logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                fail(cosmosBulkOperationResponse.getException().toString());
            }

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.NO_CONTENT.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

        }
        ;

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    @Test(groups = {"encryption"}, timeOut = TIMEOUT)
    public void bulkExecution_readItem() {
        int totalRequest = getTotalRequest();

        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        Map<String, EncryptionPojo> idToItemMap = new HashMap<>();

        for (int i = 0; i < totalRequest; i++) {
            String itemId = UUID.randomUUID().toString();
            EncryptionPojo createPojo = getItem(itemId);

            idToItemMap.put(itemId, createPojo);
            cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(createPojo, new PartitionKey(createPojo.getMypk())));
        }

        createItemsAndVerify(cosmosItemOperations);

        List<CosmosItemOperation> readCosmosItemOperations = new ArrayList<>();
        for (CosmosItemOperation cosmosItemOperation : cosmosItemOperations) {
            EncryptionPojo encryptionPojo = cosmosItemOperation.getItem();
            readCosmosItemOperations.add(CosmosBulkOperations.getReadItemOperation(encryptionPojo.getId(), cosmosItemOperation.getPartitionKeyValue()));
        }

        List<CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest>> bulkResponse = Lists.newArrayList(this.cosmosEncryptionContainer
            .executeBulkOperations(readCosmosItemOperations));


        AtomicInteger processedDoc = new AtomicInteger(0);
        for (CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest> cosmosBulkOperationResponse : bulkResponse) {

            processedDoc.incrementAndGet();

            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            if (cosmosBulkOperationResponse.getException() != null) {
                logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                fail(cosmosBulkOperationResponse.getException().toString());
            }

            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

            EncryptionPojo item = cosmosBulkItemResponse.getItem(EncryptionPojo.class);
            validateResponse(item, idToItemMap.get(item.getId()));

        }
        ;

        assertThat(processedDoc.get()).isEqualTo(totalRequest);
    }

    private void createItemsAndVerify(List<CosmosItemOperation> cosmosItemOperations) {

        List<CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest>> createResponseFlux = Lists.newArrayList(this.cosmosEncryptionContainer.
            executeBulkOperations(cosmosItemOperations));

        Set<String> distinctIndex = new HashSet<>();
        AtomicInteger processedDoc = new AtomicInteger(0);

        for (CosmosBulkOperationResponse<EncryptionAsyncApiCrudTest> cosmosBulkOperationResponse : createResponseFlux) {
            processedDoc.incrementAndGet();
            CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();
            if (cosmosBulkOperationResponse.getException() != null) {
                logger.error("Bulk operation failed", cosmosBulkOperationResponse.getException());
                fail(cosmosBulkOperationResponse.getException().toString());
            }
            assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
            assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
            assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
            assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
            assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();

            // Using id as list index like we assigned
            EncryptionPojo encryptionPojo = cosmosBulkItemResponse.getItem(EncryptionPojo.class);
            distinctIndex.add(encryptionPojo.getId());

        }
        ;

        // Verify if all are distinct and count is equal to request count.
        assertThat(processedDoc.get()).isEqualTo(cosmosItemOperations.size());
        assertThat(distinctIndex.size()).isEqualTo(cosmosItemOperations.size());
    }
}
