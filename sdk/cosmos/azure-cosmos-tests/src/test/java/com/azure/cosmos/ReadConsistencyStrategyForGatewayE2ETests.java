// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ReadConsistencyStrategyForGatewayE2ETests extends TestSuiteBase {

    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private final GatewayVariant gatewayVariant;

    @Factory(dataProvider = "gatewayClientBuilders")
    public ReadConsistencyStrategyForGatewayE2ETests(CosmosClientBuilder clientBuilder, GatewayVariant gatewayVariant) {
        super(clientBuilder);
        this.gatewayVariant = gatewayVariant;
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        return this.gatewayVariant.name();
    }

    @DataProvider
    public static Object[][] gatewayClientBuilders() {
        List<Object[]> providers = new ArrayList<>();

        // Gateway V1 builders
        for (Object[] wrappedBuilders : clientBuildersWithGateway()) {
            providers.add(new Object[]{ wrappedBuilders[0], GatewayVariant.GATEWAY_V1 });
        }

        // Gateway V2 (HTTP/2) builders
        for (Object[] wrappedBuilders : clientBuildersWithGatewayAndHttp2()) {
            providers.add(new Object[]{ wrappedBuilders[0], GatewayVariant.GATEWAY_V2 });
        }

        return providers.toArray(new Object[0][]);
    }

    @BeforeClass(groups = {"multi-region", "thin-client-multi-region"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        this.client = getClientBuilder().buildAsyncClient();
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(this.client);
    }

    @AfterClass(groups = {"multi-region", "thin-client-multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(this.client);
    }

    @DataProvider(name = "nonDefaultReadConsistencyStrategies")
    public static Object[][] nonDefaultReadConsistencyStrategies() {
        return new Object[][] {
            { ReadConsistencyStrategy.EVENTUAL },
            { ReadConsistencyStrategy.SESSION },
            { ReadConsistencyStrategy.LATEST_COMMITTED },
            { ReadConsistencyStrategy.GLOBAL_STRONG },
        };
    }

    // --- Point read tests ---

    @Test(groups = {"multi-region", "thin-client-multi-region"}, dataProvider = "nonDefaultReadConsistencyStrategies", timeOut = TIMEOUT)
    public void readItem_withNonDefaultReadConsistencyStrategy_throwsIllegalArgumentException(
        ReadConsistencyStrategy strategy) {

        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(strategy);

        try {
            cosmosAsyncContainer
                .readItem(createdItem.getId(), new PartitionKey(createdItem.getMypk()), requestOptions, TestItem.class)
                .block();
            fail("Expected IllegalArgumentException for ReadConsistencyStrategy " + strategy + " in Gateway mode");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("ReadConsistencyStrategy");
            assertThat(e.getMessage()).contains("is not supported in Gateway mode");
        }
    }

    // --- Query tests ---

    @Test(groups = {"multi-region", "thin-client-multi-region"}, dataProvider = "nonDefaultReadConsistencyStrategies", timeOut = TIMEOUT)
    public void queryItems_withNonDefaultReadConsistencyStrategy_throwsIllegalArgumentException(
        ReadConsistencyStrategy strategy) {

        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions()
            .setReadConsistencyStrategy(strategy);

        String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());

        try {
            cosmosAsyncContainer
                .queryItems(query, requestOptions, TestItem.class)
                .byPage()
                .blockLast();
            fail("Expected IllegalArgumentException for ReadConsistencyStrategy " + strategy + " in Gateway mode");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("ReadConsistencyStrategy");
            assertThat(e.getMessage()).contains("is not supported in Gateway mode");
        }
    }

    // --- Change feed tests ---

    @Test(groups = {"multi-region", "thin-client-multi-region"}, dataProvider = "nonDefaultReadConsistencyStrategies", timeOut = TIMEOUT)
    public void queryChangeFeed_withNonDefaultReadConsistencyStrategy_throwsIllegalArgumentException(
        ReadConsistencyStrategy strategy) {

        String pk = UUID.randomUUID().toString();
        TestItem createdItem = new TestItem(UUID.randomUUID().toString(), pk, UUID.randomUUID().toString());
        cosmosAsyncContainer.createItem(createdItem).block();

        CosmosChangeFeedRequestOptions requestOptions =
            CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(pk)))
                .setReadConsistencyStrategy(strategy);

        try {
            cosmosAsyncContainer
                .queryChangeFeed(requestOptions, TestItem.class)
                .byPage()
                .blockLast();
            fail("Expected IllegalArgumentException for ReadConsistencyStrategy " + strategy + " in Gateway mode");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("ReadConsistencyStrategy");
            assertThat(e.getMessage()).contains("is not supported in Gateway mode");
        }
    }

    // --- Read many tests ---

    @Test(groups = {"multi-region", "thin-client-multi-region"}, dataProvider = "nonDefaultReadConsistencyStrategies", timeOut = TIMEOUT)
    public void readMany_withNonDefaultReadConsistencyStrategy_throwsIllegalArgumentException(
        ReadConsistencyStrategy strategy) {

        String pk = UUID.randomUUID().toString();
        TestItem item1 = new TestItem(UUID.randomUUID().toString(), pk, UUID.randomUUID().toString());
        cosmosAsyncContainer.createItem(item1).block();

        List<CosmosItemIdentity> identities = new ArrayList<>();
        identities.add(new CosmosItemIdentity(new PartitionKey(pk), item1.getId()));

        CosmosReadManyRequestOptions requestOptions = new CosmosReadManyRequestOptions()
            .setReadConsistencyStrategy(strategy);

        try {
            cosmosAsyncContainer
                .readMany(identities, requestOptions, TestItem.class)
                .block();
            fail("Expected IllegalArgumentException for ReadConsistencyStrategy " + strategy + " in Gateway mode");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("ReadConsistencyStrategy");
            assertThat(e.getMessage()).contains("is not supported in Gateway mode");
        }
    }

    // --- Positive test: DEFAULT strategy should be allowed ---

    @Test(groups = {"multi-region", "thin-client-multi-region"}, timeOut = TIMEOUT)
    public void readItem_withDefaultReadConsistencyStrategy_succeeds() {

        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.DEFAULT);

        // DEFAULT strategy should not throw
        cosmosAsyncContainer
            .readItem(createdItem.getId(), new PartitionKey(createdItem.getMypk()), requestOptions, TestItem.class)
            .block();
    }

    @Test(groups = {"multi-region", "thin-client-multi-region"}, timeOut = TIMEOUT)
    public void readItem_withNullReadConsistencyStrategy_succeeds() {

        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(null);

        // null strategy should not throw
        cosmosAsyncContainer
            .readItem(createdItem.getId(), new PartitionKey(createdItem.getMypk()), requestOptions, TestItem.class)
            .block();
    }

}
