// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.AssertJUnit.fail;

public class EmulatorVNextWithHttpTest extends TestSuiteBase {
    private CosmosAsyncClient client;
    private CosmosAsyncContainer createdContainer;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public EmulatorVNextWithHttpTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "emulator-vnext" }, timeOut = SETUP_TIMEOUT)
    public void before_EmulatorWithHttpTest() {
        this.client =
            getClientBuilder()
                .gatewayMode()
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
        this.createdDatabase = getSharedCosmosDatabase(this.client);
        this.createdContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);
    }

    @AfterClass(groups = { "emulator-vnext" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeClose(this.client);
    }

    @Test(groups = { "emulator-vnext" }, timeOut = TIMEOUT)
    public void createSameDatabaseTwice() {
        try {
            this.client.createDatabase(this.createdDatabase.getId()).block();
            fail("Creating database with same name twice should have failed");
        } catch (CosmosException e) {
            if (e.getStatusCode() != HttpConstants.StatusCodes.CONFLICT) {
                fail("Creating database with same name twice should have resulted conflict error");
            }
        }
    }

    @Test(groups = { "emulator-vnext" }, timeOut = 4 * TIMEOUT)
    public void documentCrud() {
        // Currently emulator vnext only support gateway mode and limited set of features
        // https://review.learn.microsoft.com/en-us/azure/cosmos-db/emulator-linux?branch=release-ignite-2024-cosmos-db#feature-support

        // create item
        TestItem createdItem = TestItem.createNewItem();
        this.createdContainer.createItem(createdItem).block();

        // read item
        TestItem itemReadBack =
            this.createdContainer
                .readItem(
                    createdItem.getId(),
                    new PartitionKey(createdItem.getId()),
                    TestItem.class)
                .block()
                .getItem();
        validateItem(itemReadBack, createdItem);

        // query item
        String query = "select * from c";
        List<TestItem> queryResults = new ArrayList<>();
        this.createdContainer.queryItems(query, new CosmosQueryRequestOptions(), TestItem.class)
            .byPage()
            .doOnNext(feedResponse -> {
                queryResults.addAll(feedResponse.getResults());
            })
            .blockLast();
        assertThat(queryResults.size()).isOne();
        validateItem(queryResults.get(0), createdItem);

        // query items with sqlQuerySpec
        List<SqlParameter> sqlParameterList = new ArrayList<>();
        sqlParameterList.add(new SqlParameter("@ID", createdItem.getId()));
        List<TestItem> querySpecResult = new ArrayList<>();
        this.createdContainer
            .queryItems(
                new SqlQuerySpec("select * from c where c.id = @ID", sqlParameterList), TestItem.class)
            .byPage()
            .doOnNext(feedResponse -> {
                querySpecResult.addAll(feedResponse.getResults());
            })
            .blockLast();
        assertThat(querySpecResult.size()).isOne();
        validateItem(querySpecResult.get(0), createdItem);

        // query items with order by
        String orderByQuery = "select * from c order by c.intProp desc";
        List<TestItem> orderByQueryResult = new ArrayList<>();
        this.createdContainer
            .queryItems(orderByQuery, TestItem.class)
            .byPage()
            .doOnNext(feedResponse -> {
                orderByQueryResult.addAll(feedResponse.getResults());
            })
            .blockLast();
        assertThat(orderByQueryResult.size()).isOne();
        validateItem(orderByQueryResult.get(0), createdItem);

        // update item
        createdItem.setProp(UUID.randomUUID().toString());
        TestItem updatedItem = this.createdContainer.upsertItem(createdItem).block().getItem();
        validateItem(updatedItem, createdItem);

        // replace item
        createdItem.setProp(UUID.randomUUID().toString());
        TestItem replacedItem =
            this.createdContainer
                .replaceItem(
                    createdItem, createdItem.getId(),
                    new PartitionKey(createdItem.getId()))
                .block()
                .getItem();
        validateItem(replacedItem, createdItem);

        // read all items
        List<TestItem> allItemsResult = new ArrayList<>();
        this.createdContainer.readAllItems(new PartitionKey(createdItem.getId()), TestItem.class).byPage()
            .doOnNext(response -> {
                allItemsResult.addAll(response.getResults());
            }).blockLast();
        assertThat(allItemsResult.size()).isOne();
        validateItem(allItemsResult.get(0), createdItem);

        // bulk
        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
        cosmosItemOperations.add(CosmosBulkOperations.getReadItemOperation(createdItem.getId(), new PartitionKey(createdItem.getId())));
        List<TestItem> bulkReadResponse = new ArrayList<>();
        this.createdContainer.executeBulkOperations(Flux.fromIterable(cosmosItemOperations), new CosmosBulkExecutionOptions())
            .doOnNext(bulkResponse -> {
                bulkReadResponse.add(bulkResponse.getResponse().getItem(TestItem.class));
            })
            .blockLast();
        assertThat(bulkReadResponse.size()).isOne();
        validateItem(bulkReadResponse.get(0), createdItem);

        // batch
        CosmosBatch batchForRead = CosmosBatch.createCosmosBatch(new PartitionKey(createdItem.getId()));
        batchForRead.readItemOperation(createdItem.getId());
        CosmosBatchResponse cosmosBatchResponse = this.createdContainer.executeCosmosBatch(batchForRead).block();
        assertThat(cosmosBatchResponse.getResults().size()).isOne();
        validateItem(cosmosBatchResponse.getResults().get(0).getItem(TestItem.class), createdItem);

        // read many
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey(createdItem.getId()), createdItem.getId()));
        List<TestItem> readManyResults = new ArrayList<>();
        this.createdContainer
            .readMany(cosmosItemIdentities, TestItem.class)
            .doOnNext(feedResponse -> readManyResults.addAll(feedResponse.getResults()))
            .block();
        assertThat(readManyResults.size()).isOne();
        validateItem(readManyResults.get(0), createdItem);

        // read feed ranges
        List<FeedRange> feedRanges = this.createdContainer.getFeedRanges().block();
        assertThat(feedRanges.size()).isGreaterThanOrEqualTo(1);

        // delete item
        this.createdContainer.deleteItem(createdItem.getId(), new PartitionKey(createdItem.getId())).block();
    }

    private void validateItem(TestItem returnedItem, TestItem expectedItem) {
        assertThat(returnedItem).isNotNull();
        assertThat(expectedItem).isNotNull();
        assertThat(returnedItem.getId()).isEqualTo(expectedItem.getId());
        assertThat(returnedItem.getMypk()).isEqualTo(expectedItem.getMypk());
        assertThat(returnedItem.getProp()).isEqualTo(expectedItem.getProp());
    }

    private static class TestItem {
        private final static Random random = new Random();

        private String id;
        private String mypk;
        private String prop;
        private int intProp;

        public TestItem() {
        }

        public TestItem(String id, String mypk, String prop, int intProp) {
            this.id = id;
            this.mypk = mypk;
            this.prop = prop;
            this.intProp = intProp;
        }

        public static TestItem createNewItem() {
            return new TestItem(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                random.nextInt(10));
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getProp() {
            return prop;
        }

        public void setProp(String prop) {
            this.prop = prop;
        }

        public int getIntProp() {
            return intProp;
        }

        public void setIntProp(int intProp) {
            this.intProp = intProp;
        }
    }
}
