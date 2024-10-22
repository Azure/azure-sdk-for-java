// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosMultiHashTest;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.FeedResponseValidator;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerQueryTest extends TestSuiteBase {
    private final static int TIMEOUT = 30000;
    private final String databaseId = CosmosDatabaseForTest.generateId();
    private List<CosmosAsyncContainer> createdContainers = new ArrayList<>();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @DataProvider(name = "queryContainerWithHierarchicalPartitionKeyDataProvider")
    public static Object[][] queryContainerWithHierarchicalPartitionKeyDataProvider() {
        return new Object[][]{
            { "Redmond", null, -1 },
            { "Redmond", "98052", -1 },
            { "Redmond", "98052", 1 },
        };
    }

    @Factory(dataProvider = "clientBuilders")
    public ContainerQueryTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryContainersWithFilter() throws Exception {

        String filterContainerId = createdContainers.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterContainerId);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 2;
        CosmosPagedFlux<CosmosContainerProperties> queryObservable = createdDatabase.queryContainers(query, options);

        List<CosmosAsyncContainer> expectedContainers = createdContainers.stream()
                .filter(c -> StringUtils.equals(filterContainerId, c.getId()) ).collect(Collectors.toList());

        assertThat(expectedContainers).isNotEmpty();

        int expectedPageSize = (expectedContainers.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .totalSize(expectedContainers.size())
                .exactlyContainsInAnyOrder(expectedContainers.stream().map(d -> d.read().block().getProperties().getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryAllContainers() throws Exception {

        String query = "SELECT * from c";

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        int maxItemCount = 2;
        CosmosPagedFlux<CosmosContainerProperties> queryObservable = createdDatabase.queryContainers(query, options);

        List<CosmosAsyncContainer> expectedContainers = createdContainers;

        assertThat(expectedContainers).isNotEmpty();

        int expectedPageSize = (expectedContainers.size() + maxItemCount - 1) / maxItemCount;

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .totalSize(expectedContainers.size())
                .exactlyContainsInAnyOrder(expectedContainers.stream().map(d -> d.read().block().getProperties().getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable.byPage(maxItemCount), validator, 10000);
    }

    @Test(groups = { "query" }, timeOut = TIMEOUT)
    public void queryContainers_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        CosmosPagedFlux<CosmosContainerProperties> queryObservable = createdDatabase.queryContainers(query, options);

        FeedResponseListValidator<CosmosContainerProperties> validator = new FeedResponseListValidator.Builder<CosmosContainerProperties>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<CosmosContainerProperties>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable.byPage(), validator);
    }

    // Currently no known issue with query and hpk but adding this to detect regressions just in case, since a fix was added
    // for queryChangeFeed with hpk
    @Test(groups = { "query" }, dataProvider = "queryContainerWithHierarchicalPartitionKeyDataProvider", timeOut = TIMEOUT)
    public void queryContainerWithHierarchicalPartitionKey(String pk1, String pk2, int pk3) {
        String testContainerId = UUID.randomUUID().toString();
        try {
            PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
            partitionKeyDefinition.setKind(PartitionKind.MULTI_HASH);
            partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
            ArrayList<String> paths = new ArrayList<>();
            paths.add("/city");
            paths.add("/zipcode");
            paths.add("/areaCode"); // expecting int value type
            partitionKeyDefinition.setPaths(paths);

            String query = "SELECT * from c";

            PartitionKeyBuilder partialPartitionKeyBuilder = new PartitionKeyBuilder();
            if (pk1 != null) {
                partialPartitionKeyBuilder.add(pk1);
            }
            if (pk2 != null) {
                partialPartitionKeyBuilder.add(pk2);
            }
            if (pk3 != -1) {
                partialPartitionKeyBuilder.add(pk3);
            }

            PartitionKey partialPartitionKey = partialPartitionKeyBuilder.build();
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setFeedRange(FeedRange.forLogicalPartition(partialPartitionKey));

            // create the container with hpk
            CosmosContainerProperties testContainerProperties = new CosmosContainerProperties(testContainerId, partitionKeyDefinition);
            CosmosAsyncContainer testContainer = createCollection(client, databaseId, testContainerProperties);

            CosmosMultiHashTest.CityItem cityItem = new CosmosMultiHashTest.CityItem(UUID.randomUUID().toString(), "Redmond", "98052", 1);
            testContainer.createItem(cityItem).block();

            List<CosmosMultiHashTest.CityItem> results = testContainer
                .queryItems(query, queryRequestOptions, CosmosMultiHashTest.CityItem.class) // this call should not fail with partial partition key in request options
                .collectList()
                .block();

            // some basic validation
            assertThat(results).isNotNull();
            assertThat(results.size()).isEqualTo(1);
            assertThat(results.get(0).getCity()).isEqualTo("Redmond");
            assertThat(results.get(0).getZipcode()).isEqualTo("98052");
            assertThat(results.get(0).getAreaCode()).isEqualTo(1);
        } finally {
            safeDeleteCollection(this.createdDatabase.getContainer(testContainerId));
        }
    }

    @BeforeClass(groups = { "query" }, timeOut = SETUP_TIMEOUT)
    public void before_ContainerQueryTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties container = new CosmosContainerProperties(UUID.randomUUID().toString(), partitionKeyDef);
        createdContainers.add(createCollection(client, databaseId, container));
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    private static InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "}"
            , uuid, uuid));
        return doc;
    }
}
