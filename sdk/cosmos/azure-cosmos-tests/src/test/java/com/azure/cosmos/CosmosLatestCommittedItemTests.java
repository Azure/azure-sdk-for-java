/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosOperationDetails;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.CosmosRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class CosmosLatestCommittedItemTests extends TestSuiteBase {
    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private CosmosClient client;
    private OverrideOption overrideOption;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuildersForLatestCommittedItemTests")
    public CosmosLatestCommittedItemTests(CosmosClientBuilder clientBuilder, OverrideOption overrideOption) {
        super(clientBuilder);

        this.overrideOption = overrideOption;
    }

    @Override
    public String resolveTestNameSuffix(Object[] row) {
        return this.overrideOption.name();
    }

    @DataProvider
    public static Object[][] clientBuildersForLatestCommittedItemTests() {
        List<CosmosClientBuilder> clientBuilders = new ArrayList<>();
        for (Object[] wrappedBuilders : clientBuildersWithDirect()) {
            clientBuilders.add((CosmosClientBuilder)wrappedBuilders[0]);
        }

        List<OverrideOption> overrideOptions = new ArrayList<>();
        overrideOptions.add(OverrideOption.REQUEST_OPTIONS);
        overrideOptions.add(OverrideOption.DYNAMIC_REQUEST_OPTIONS);
        overrideOptions.add(OverrideOption.CLIENT_LEVEL);

        List<Object[]> providers = new ArrayList<>();
        for (CosmosClientBuilder builder: clientBuilders) {
            for (OverrideOption overrideOption: overrideOptions) {
                Object[] configs = new Object[2];
                configs[1] = overrideOption;
                CosmosClientBuilder clonedBuilder = copyCosmosClientBuilder(builder);
                switch (overrideOption) {
                    case REQUEST_OPTIONS:
                        configs[0] = clonedBuilder;
                        break;
                    case CLIENT_LEVEL:
                        configs[0] = clonedBuilder.readConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);
                        break;
                    case DYNAMIC_REQUEST_OPTIONS:
                        CosmosOperationPolicy policy = new CosmosOperationPolicy() {
                            @Override
                            public void process(CosmosOperationDetails operationDetails) {
                                CosmosRequestContext requestContext = operationDetails.getRequestContext();
                                if (requestContext.getReadConsistencyStrategy() != null) {
                                    // NOTE - throwing here would not be done in any real application
                                    // just using it for test purposes here
                                    throw new IllegalStateException(
                                        "RequestOptions should not have read consistency strategy "
                                            + "set in this test variation.");
                                }
                                operationDetails.setRequestOptions(
                                    new CosmosRequestOptions()
                                        .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED));
                            }
                        };

                        configs[0] = clonedBuilder.addOperationPolicy(policy);
                        break;
                }

                providers.add(configs);
            }
        }

        Object[][] array = new Object[providers.size()][];
        return providers.toArray(array);
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"fast"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void queryItemsWithLatestCommitted() throws JsonProcessingException {
        this.ensureRelevantForLatestCommitted();

        String id = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(id, id);
        CosmosItemResponse<ObjectNode> createResponse = container.createItem(properties);
        assertThat(createResponse.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.DEFAULT);

        String query = String.format("SELECT * from c where c.id = '%s'", id);
        CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        if (this.overrideOption == OverrideOption.DYNAMIC_REQUEST_OPTIONS) {
            try {
                container
                    .queryItems(query, requestOptions, ObjectNode.class)
                    .iterableByPage(null)
                    .iterator();

                fail("Expected an IllegalStateException from the operationPolicy");
            } catch (IllegalStateException expectedError) {}
        }

        if (this.overrideOption != OverrideOption.REQUEST_OPTIONS) {
            // rely on client level or operation policy to force consistency strategy
            requestOptions.setReadConsistencyStrategy(null);
        }

        Iterator<FeedResponse<ObjectNode>> feedResponseIterator1 =
            container
                .queryItems(query, requestOptions, ObjectNode.class)
                .iterableByPage(null)
                .iterator();

        boolean hasNext = feedResponseIterator1.hasNext();

        // Very basic validation
        assertThat(hasNext).isTrue();
        FeedResponse<ObjectNode> response = feedResponseIterator1.next();
        CosmosDiagnosticsContext ctx = response.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        logger.info("Diagnostics context: {}", ctx.toJson());

        assertThat(ctx.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThat(ctx.getDiagnostics()).hasSize(2);
        List<CosmosDiagnostics> diagnosticsList = new ArrayList<>(ctx.getDiagnostics());
        CosmosDiagnostics queryPlanDiagnostics = diagnosticsList.get(0);
        List<ClientSideRequestStatistics> storeResultListQueryPlan =
            new ArrayList<>(queryPlanDiagnostics.getClientSideRequestStatistics());
        assertThat(storeResultListQueryPlan).hasSize(1);
        assertThat(storeResultListQueryPlan.get(0).getGatewayStatisticsList()).hasSize(1);
        assertThat(storeResultListQueryPlan.get(0).getGatewayStatisticsList().get(0).getOperationType())
            .isEqualTo(OperationType.QueryPlan);

        CosmosDiagnostics queryDiagnostics = diagnosticsList.get(1);
        List<ClientSideRequestStatistics> storeResultListQuery =
            new ArrayList<>(queryDiagnostics.getClientSideRequestStatistics());
        assertThat(storeResultListQuery).hasSize(2);
        ArrayList<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseList =
            new ArrayList<>(storeResultListQuery.get(0).getResponseStatisticsList());
        assertThat(storeResponseList).hasSize(2);
        assertThat(storeResponseList.get(0).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(0).getRequestOperationType()).isEqualTo(OperationType.Query);
        assertThat(storeResponseList.get(1).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(1).getRequestOperationType()).isEqualTo(OperationType.Query);

        storeResponseList = new ArrayList<>(storeResultListQuery.get(1).getResponseStatisticsList());
        assertThat(storeResponseList).hasSize(2);
        assertThat(storeResponseList.get(0).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(0).getRequestOperationType()).isEqualTo(OperationType.Query);
        assertThat(storeResponseList.get(1).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(1).getRequestOperationType()).isEqualTo(OperationType.Query);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readItemWithLatestCommitted() throws JsonProcessingException {
        this.ensureRelevantForLatestCommitted();

        String id = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(id, id);
        CosmosItemResponse<ObjectNode> createResponse = container.createItem(properties);
        assertThat(createResponse.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.DEFAULT);

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        if (this.overrideOption == OverrideOption.DYNAMIC_REQUEST_OPTIONS) {
            try {
                container
                    .readItem(id, new PartitionKey(id), requestOptions, ObjectNode.class);

                fail("Expected an IllegalStateException from the operationPolicy");
            } catch (IllegalStateException expectedError) {}
        }

        if (this.overrideOption != OverrideOption.REQUEST_OPTIONS) {
            // rely on client level or operation policy to force consistency strategy
            requestOptions.setReadConsistencyStrategy(null);
        }

        CosmosItemResponse<ObjectNode> response = container
            .readItem(id, new PartitionKey(id), requestOptions, ObjectNode.class);

        CosmosDiagnosticsContext ctx = response.getDiagnostics().getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        logger.info("Diagnostics context: {}", ctx.toJson());

        assertThat(ctx.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThat(ctx.getDiagnostics()).hasSize(1);
        List<CosmosDiagnostics> diagnosticsList = new ArrayList<>(ctx.getDiagnostics());
        CosmosDiagnostics pointReadDiagnostics = diagnosticsList.get(0);
        List<ClientSideRequestStatistics> storeResultListPointRead =
            new ArrayList<>(pointReadDiagnostics.getClientSideRequestStatistics());
        assertThat(storeResultListPointRead).hasSize(1);
        ArrayList<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseList =
            new ArrayList<>(storeResultListPointRead.get(0).getResponseStatisticsList());
        assertThat(storeResponseList).hasSize(2);
        assertThat(storeResponseList.get(0).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(0).getRequestOperationType()).isEqualTo(OperationType.Read);
        assertThat(storeResponseList.get(1).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(1).getRequestOperationType()).isEqualTo(OperationType.Read);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readManyWithLatestCommitted() throws JsonProcessingException {
        this.ensureRelevantForLatestCommitted();

        String pk = UUID.randomUUID().toString();
        String id1 = UUID.randomUUID().toString();
        ObjectNode properties1 = getDocumentDefinition(id1, pk);
        CosmosItemResponse<ObjectNode> createResponse = container.createItem(properties1);
        assertThat(createResponse.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.DEFAULT);

        String id2 = UUID.randomUUID().toString();
        ObjectNode properties2 = getDocumentDefinition(id2, pk);
        createResponse = container.createItem(properties2);
        assertThat(createResponse.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.DEFAULT);

        List<CosmosItemIdentity> identities = new ArrayList<>();
        identities.add(new CosmosItemIdentity(new PartitionKey(pk), id1));
        identities.add(new CosmosItemIdentity(new PartitionKey(pk), id2));

        CosmosReadManyRequestOptions requestOptions = new CosmosReadManyRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        if (this.overrideOption == OverrideOption.DYNAMIC_REQUEST_OPTIONS) {
            try {
                container
                    .readMany(identities, requestOptions, ObjectNode.class);

                fail("Expected an IllegalStateException from the operationPolicy");
            } catch (IllegalStateException expectedError) {}
        }

        if (this.overrideOption != OverrideOption.REQUEST_OPTIONS) {
            // rely on client level or operation policy to force consistency strategy
            requestOptions.setReadConsistencyStrategy(null);
        }

        FeedResponse<ObjectNode> response = container
            .readMany(identities, requestOptions, ObjectNode.class);

        CosmosDiagnosticsContext ctx = response.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        logger.info("Diagnostics context: {}", ctx.toJson());

        assertThat(ctx.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThat(ctx.getDiagnostics()).hasSize(1);
        List<CosmosDiagnostics> diagnosticsList = new ArrayList<>(ctx.getDiagnostics());
        CosmosDiagnostics pointReadDiagnostics = diagnosticsList.get(0);
        List<ClientSideRequestStatistics> storeResultListPointRead =
            new ArrayList<>(pointReadDiagnostics.getClientSideRequestStatistics());
        assertThat(storeResultListPointRead).hasSize(1);
        ArrayList<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseList =
            new ArrayList<>(storeResultListPointRead.get(0).getResponseStatisticsList());
        assertThat(storeResponseList).hasSize(2);
        assertThat(storeResponseList.get(0).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(0).getRequestOperationType()).isEqualTo(OperationType.Query);
        assertThat(storeResponseList.get(1).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(1).getRequestOperationType()).isEqualTo(OperationType.Query);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readManyWithLatestCommitted_SingleItem() throws JsonProcessingException {
        this.ensureRelevantForLatestCommitted();

        String pk = UUID.randomUUID().toString();
        String id1 = UUID.randomUUID().toString();
        ObjectNode properties1 = getDocumentDefinition(id1, pk);
        CosmosItemResponse<ObjectNode> createResponse = container.createItem(properties1);
        assertThat(createResponse.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.DEFAULT);

        List<CosmosItemIdentity> identities = new ArrayList<>();
        identities.add(new CosmosItemIdentity(new PartitionKey(pk), id1));

        CosmosReadManyRequestOptions requestOptions = new CosmosReadManyRequestOptions()
            .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        if (this.overrideOption == OverrideOption.DYNAMIC_REQUEST_OPTIONS) {
            try {
                container
                    .readMany(identities, requestOptions, ObjectNode.class);

                fail("Expected an IllegalStateException from the operationPolicy");
            } catch (IllegalStateException expectedError) {}
        }

        if (this.overrideOption != OverrideOption.REQUEST_OPTIONS) {
            // rely on client level or operation policy to force consistency strategy
            requestOptions.setReadConsistencyStrategy(null);
        }

        FeedResponse<ObjectNode> response = container
            .readMany(identities, requestOptions, ObjectNode.class);

        CosmosDiagnosticsContext ctx = response.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        logger.info("Diagnostics context: {}", ctx.toJson());

        assertThat(ctx.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThat(ctx.getDiagnostics()).hasSize(1);
        List<CosmosDiagnostics> diagnosticsList = new ArrayList<>(ctx.getDiagnostics());
        CosmosDiagnostics pointReadDiagnostics = diagnosticsList.get(0);
        List<ClientSideRequestStatistics> storeResultListPointRead =
            new ArrayList<>(pointReadDiagnostics.getClientSideRequestStatistics());
        assertThat(storeResultListPointRead).hasSize(1);
        ArrayList<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseList =
            new ArrayList<>(storeResultListPointRead.get(0).getResponseStatisticsList());
        assertThat(storeResponseList).hasSize(2);
        assertThat(storeResponseList.get(0).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(0).getRequestOperationType()).isEqualTo(OperationType.Read);
        assertThat(storeResponseList.get(1).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(1).getRequestOperationType()).isEqualTo(OperationType.Read);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void queryChangeFeedWithLatestCommitted() throws JsonProcessingException {
        this.ensureRelevantForLatestCommitted();

        String pk = UUID.randomUUID().toString();
        String id1 = UUID.randomUUID().toString();
        ObjectNode properties1 = getDocumentDefinition(id1, pk);
        CosmosItemResponse<ObjectNode> createResponse = container.createItem(properties1);
        assertThat(createResponse.getDiagnostics().getDiagnosticsContext().getEffectiveReadConsistencyStrategy())
            .isEqualTo(ReadConsistencyStrategy.DEFAULT);

        List<CosmosItemIdentity> identities = new ArrayList<>();
        identities.add(new CosmosItemIdentity(new PartitionKey(pk), id1));

        CosmosChangeFeedRequestOptions requestOptions =
            CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forLogicalPartition(new PartitionKey(pk)))
                .setReadConsistencyStrategy(ReadConsistencyStrategy.LATEST_COMMITTED);

        if (this.overrideOption == OverrideOption.DYNAMIC_REQUEST_OPTIONS) {
            try {
                container
                    .queryChangeFeed(requestOptions, ObjectNode.class)
                    .iterableByPage(null)
                    .iterator()
                    .next();

                fail("Expected an IllegalStateException from the operationPolicy");
            } catch (IllegalStateException expectedError) {}
        }

        if (this.overrideOption != OverrideOption.REQUEST_OPTIONS) {
            // rely on client level or operation policy to force consistency strategy
            requestOptions.setReadConsistencyStrategy(null);
        }

        FeedResponse<ObjectNode> response = container
            .queryChangeFeed(requestOptions, ObjectNode.class)
            .iterableByPage(null)
            .iterator()
            .next();

        CosmosDiagnosticsContext ctx = response.getCosmosDiagnostics().getDiagnosticsContext();
        assertThat(ctx).isNotNull();
        logger.info("Diagnostics context: {}", ctx.toJson());

        assertThat(ctx.getEffectiveReadConsistencyStrategy()).isEqualTo(ReadConsistencyStrategy.LATEST_COMMITTED);
        assertThat(ctx.getDiagnostics()).hasSize(1);
        List<CosmosDiagnostics> diagnosticsList = new ArrayList<>(ctx.getDiagnostics());
        CosmosDiagnostics changeFeedDiagnostics = diagnosticsList.get(0);
        List<ClientSideRequestStatistics> storeResultListChangeFeed =
            new ArrayList<>(changeFeedDiagnostics.getClientSideRequestStatistics());
        assertThat(storeResultListChangeFeed).hasSize(1);
        ArrayList<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseList =
            new ArrayList<>(storeResultListChangeFeed.get(0).getResponseStatisticsList());
        assertThat(storeResponseList).hasSize(2);
        assertThat(storeResponseList.get(0).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(0).getRequestOperationType()).isEqualTo(OperationType.ReadFeed);
        assertThat(storeResponseList.get(1).getRequestResourceType()).isEqualTo(ResourceType.Document);
        assertThat(storeResponseList.get(1).getRequestOperationType()).isEqualTo(OperationType.ReadFeed);
    }

    private void ensureRelevantForLatestCommitted() {
        if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("This test is only relevant for DIRECT mode");
        }

        ConsistencyLevel accountConsistencyLevel = ImplementationBridgeHelpers
            .CosmosAsyncClientHelper
            .getCosmosAsyncClientAccessor()
            .getEffectiveConsistencyLevel(this.client.asyncClient(), OperationType.Create, ConsistencyLevel.STRONG);

        if (accountConsistencyLevel != ConsistencyLevel.SESSION
            && accountConsistencyLevel != ConsistencyLevel.EVENTUAL) {

            throw new SkipException(
                "This test is only relevant for accounts with consistency level EVENTUAL or SESSION.");
        }
    }


    private ObjectNode getDocumentDefinition(String documentId, String pkId) throws JsonProcessingException {

        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , documentId, pkId);
        return
            OBJECT_MAPPER.readValue(json, ObjectNode.class);
    }

    private static enum OverrideOption {
        REQUEST_OPTIONS,
        DYNAMIC_REQUEST_OPTIONS,
        CLIENT_LEVEL
    }
}
