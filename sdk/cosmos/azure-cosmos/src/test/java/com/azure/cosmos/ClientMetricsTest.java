/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosMicrometerMetricsOptions;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClientMetricsTest extends BatchTestBase {

    private CosmosClient client;
    private CosmosContainer container;
    private String databaseId;
    private String containerId;
    private MeterRegistry meterRegistry;
    private String preferredRegion;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public ClientMetricsTest(CosmosClientBuilder clientBuilder) {

        super(clientBuilder);
    }

    public void beforeTest() {
        assertThat(this.client).isNull();
        assertThat(this.meterRegistry).isNull();

        this.meterRegistry = ConsoleLoggingRegistryFactory.create(1);

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .metricsOptions(new CosmosMicrometerMetricsOptions().meterRegistry(this.meterRegistry));

        this.client = getClientBuilder()
            .clientTelemetryConfig(telemetryConfig)
            .buildClient();

        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(this.client.asyncClient());
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;

        Set<String> writeRegions = this.getAvailableRegionNames(rxDocumentClient, true);
        assertThat(writeRegions).isNotNull().isNotEmpty();
        this.preferredRegion = writeRegions.iterator().next();

        if (databaseId == null) {
            CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
            this.databaseId = asyncContainer.getDatabase().getId();
            this.containerId = asyncContainer.getId();
        }

        container = client.getDatabase(databaseId).getContainer(containerId);
    }

    public void afterTest() {
        this.container = null;
        CosmosClient clientSnapshot = this.client;
        if (clientSnapshot != null) {
            this.client.close();
        }
        this.client = null;

        MeterRegistry meterRegistrySnapshot = this.meterRegistry;
        if (meterRegistrySnapshot != null) {
            meterRegistrySnapshot.clear();
            meterRegistrySnapshot.close();
        }
        this.meterRegistry = null;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void maxValueExceedingDefinedLimitStillWorksWithoutException() throws Exception {

        // Expected behavior is that higher values than the expected max value can still be recorded
        // it would only result in getting less accurate "estimates" for percentile histograms

        this.beforeTest();

        try {
            Tag dummyOperationTag = Tag.of(TagName.Operation.toString(), "TestDummy");
            Timer latencyMeter = Timer
                .builder("cosmos.client.op.latency")
                .description("Operation latency")
                .maximumExpectedValue(Duration.ofSeconds(300))
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(Collections.singleton(dummyOperationTag))
                .register(this.meterRegistry);
            latencyMeter.record(Duration.ofSeconds(600));

            Meter requestLatencyMeter = this.assertMetrics(
                "cosmos.client.op.latency",
                true,
                dummyOperationTag);

            List<Measurement> measurements = new ArrayList<>();
            requestLatencyMeter.measure().forEach(measurements::add);

            assertThat(measurements.size()).isEqualTo(3);

            assertThat(measurements.get(0).getStatistic().getTagValueRepresentation()).isEqualTo("count");
            assertThat(measurements.get(0).getValue()).isEqualTo(1);
            assertThat(measurements.get(1).getStatistic().getTagValueRepresentation()).isEqualTo("total");
            assertThat(measurements.get(1).getValue()).isEqualTo(600 * 1000); // transform into milliseconds
            assertThat(measurements.get(2).getStatistic().getTagValueRepresentation()).isEqualTo("max");
            assertThat(measurements.get(2).getValue()).isEqualTo(600 * 1000); // transform into milliseconds
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
            assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
            validateItemResponse(properties, itemResponse);

            properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
            validateItemResponse(properties, itemResponse1);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "201"),
                Tag.of(TagName.RequestStatusCode.toString(), "201/0"),
                1,
                100
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Create"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Create"),
                1,
                100
            );

        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class);
            validateItemResponse(properties, readResponse1);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                1,
                50
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Read"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Read"),
                1,
                50
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection_QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", false, queryPlanTag);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag);
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

            validateItemResponse(properties, itemResponse);
            String newPropValue = UUID.randomUUID().toString();
            BridgeInternal.setProperty(properties, "newProp", newPropValue);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(options, new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")));
            // replace document
            CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(properties,
                properties.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                options);
            assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(BridgeInternal.getProperties(replace), "newProp")).isEqualTo(newPropValue);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                1,
                100
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Replace"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Replace"),
                1,
                100
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();

            CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                options);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "204"),
                Tag.of(TagName.RequestStatusCode.toString(), "204/0"),
                1,
                100
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Delete"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Delete"),
                1,
                100
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);
            assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                1,
                100
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                1,
                100
            );

            this.validateItemCountMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId())
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", true, queryPlanTag);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag);
            this.assertMetrics("cosmos.client.req.gw.requests", true, queryPlanTag);
            this.assertMetrics("cosmos.client.req.gw.RUs", false, queryPlanTag);
            this.assertMetrics("cosmos.client.req.gw.timeline", true, queryPlanTag);

            this.assertMetrics("cosmos.client.op.maxItemCount", true);
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItems() throws Exception {
        this.beforeTest();
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
                container.queryItems(query, cosmosQueryRequestOptions, InternalObjectNode.class);

            // Very basic validation
            assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

            SqlQuerySpec querySpec = new SqlQuerySpec(query);
            CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class);
            assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                1,
                10000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/queryItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                1,
                10000
            );

            this.validateItemCountMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/queryItems." + container.getId())
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", true, queryPlanTag);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag);
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 100)
    public void itemPatchSuccess() {
        this.beforeTest();
        try {
            PatchTest.ToDoActivity testItem = PatchTest.ToDoActivity.createRandomItem(this.container);
            PatchTest.ToDoActivity testItem1 = PatchTest.ToDoActivity.createRandomItem(this.container);
            PatchTest.ToDoActivity testItem2 = PatchTest.ToDoActivity.createRandomItem(this.container);

            int originalTaskNum = testItem.taskNum;
            int newTaskNum = originalTaskNum + 1;

            assertThat(testItem.children[1].status).isNull();

            com.azure.cosmos.models.CosmosPatchOperations cosmosPatchOperations = com.azure.cosmos.models.CosmosPatchOperations.create();
            cosmosPatchOperations.add("/children/0/CamelCase", "patched");
            cosmosPatchOperations.remove("/description");
            cosmosPatchOperations.replace("/taskNum", newTaskNum);
            cosmosPatchOperations.replace("/children/1", testItem1);
            cosmosPatchOperations.replace("/nestedChild", testItem2);
            cosmosPatchOperations.set("/valid", false);

            CosmosPatchItemRequestOptions options = new CosmosPatchItemRequestOptions();
            CosmosItemResponse<PatchTest.ToDoActivity> response = this.container.patchItem(
                testItem.id,
                new PartitionKey(testItem.status),
                cosmosPatchOperations,
                options,
                PatchTest.ToDoActivity.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());

            PatchTest.ToDoActivity patchedItem = response.getItem();
            assertThat(patchedItem).isNotNull();

            assertThat(patchedItem.children[0].camelCase).isEqualTo("patched");
            assertThat(patchedItem.description).isNull();
            assertThat(patchedItem.taskNum).isEqualTo(newTaskNum);
            assertThat(patchedItem.valid).isEqualTo(false);
            assertThat(patchedItem.children[1].id).isEqualTo(testItem1.id);
            assertThat(patchedItem.nestedChild.id).isEqualTo(testItem2.id);

            // read resource to validate the patch operation
            response = this.container.readItem(
                testItem.id,
                new PartitionKey(testItem.status),
                options, PatchTest.ToDoActivity.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(response.getItem()).isEqualTo(patchedItem);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                1,
                100
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Patch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Patch"),
                1,
                100
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        this.beforeTest();
        try {
            int totalRequest = 5;

            List<com.azure.cosmos.models.CosmosItemOperation> cosmosItemOperations = new ArrayList<>();
            for (int i = 0; i < totalRequest; i++) {
                String partitionKey = UUID.randomUUID().toString();
                BatchTestBase.TestDoc testDoc = this.populateTestDoc(partitionKey);
                cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(testDoc, new PartitionKey(partitionKey)));

                partitionKey = UUID.randomUUID().toString();
                BatchTestBase.EventDoc eventDoc = new BatchTestBase.EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", partitionKey);
                cosmosItemOperations.add(CosmosBulkOperations.getCreateItemOperation(eventDoc, new PartitionKey(partitionKey)));
            }

            CosmosBulkExecutionOptions cosmosBulkExecutionOptions = new CosmosBulkExecutionOptions();

            List<com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest>> bulkResponse = Lists.newArrayList(this.container
                .executeBulkOperations(cosmosItemOperations, cosmosBulkExecutionOptions));

            assertThat(bulkResponse.size()).isEqualTo(totalRequest * 2);

            for (com.azure.cosmos.models.CosmosBulkOperationResponse<CosmosBulkAsyncTest> cosmosBulkOperationResponse : bulkResponse) {
                com.azure.cosmos.models.CosmosBulkItemResponse cosmosBulkItemResponse = cosmosBulkOperationResponse.getResponse();

                assertThat(cosmosBulkItemResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isGreaterThan(0);
                assertThat(cosmosBulkItemResponse.getCosmosDiagnostics().toString()).isNotNull();
                assertThat(cosmosBulkItemResponse.getSessionToken()).isNotNull();
                assertThat(cosmosBulkItemResponse.getActivityId()).isNotNull();
                assertThat(cosmosBulkItemResponse.getRequestCharge()).isNotNull();
            }

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                1,
                100
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Batch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Batch"),
                1,
                100
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchMultipleItemExecution() {
        this.beforeTest();
        try {
            TestDoc firstDoc = this.populateTestDoc(this.partitionKey1);
            TestDoc replaceDoc = this.getTestDocCopy(firstDoc);
            replaceDoc.setCost(replaceDoc.getCost() + 1);

            EventDoc eventDoc1 = new EventDoc(UUID.randomUUID().toString(), 2, 4, "type1", this.partitionKey1);
            EventDoc readEventDoc = new EventDoc(UUID.randomUUID().toString(), 6, 14, "type2", this.partitionKey1);
            CosmosItemResponse<EventDoc> createResponse = container.createItem(readEventDoc, this.getPartitionKey(this.partitionKey1), null);
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());

            CosmosBatch batch = CosmosBatch.createCosmosBatch(this.getPartitionKey(this.partitionKey1));
            batch.createItemOperation(firstDoc);
            batch.createItemOperation(eventDoc1);
            batch.replaceItemOperation(replaceDoc.getId(), replaceDoc);
            batch.readItemOperation(readEventDoc.getId());

            CosmosBatchResponse batchResponse = container.executeCosmosBatch(batch);

            this.verifyBatchProcessed(batchResponse, 4);

            assertThat(batchResponse.getResults().get(0).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(batchResponse.getResults().get(0).getItem(TestDoc.class)).isEqualTo(firstDoc);

            assertThat(batchResponse.getResults().get(1).getStatusCode()).isEqualTo(HttpResponseStatus.CREATED.code());
            assertThat(batchResponse.getResults().get(1).getItem(EventDoc.class)).isEqualTo(eventDoc1);

            assertThat(batchResponse.getResults().get(2).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(batchResponse.getResults().get(2).getItem(TestDoc.class)).isEqualTo(replaceDoc);

            assertThat(batchResponse.getResults().get(3).getStatusCode()).isEqualTo(HttpResponseStatus.OK.code());
            assertThat(batchResponse.getResults().get(3).getItem(EventDoc.class)).isEqualTo(readEventDoc);

            // Ensure that the replace overwrote the doc from the first operation
            this.verifyByRead(container, replaceDoc);

            List<CosmosItemOperation> batchOperations = batch.getOperations();
            for (int index = 0; index < batchOperations.size(); index++) {
                assertThat(batchResponse.getResults().get(index).getOperation()).isEqualTo(batchOperations.get(index));
            }

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                1,
                100
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Batch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Batch"),
                1,
                100
            );
        } finally {
            this.afterTest();
        }
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        return
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                    + "}"
                , documentId, uuid));
    }

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateMetrics(int minRu, int maxRu) {
        this.assertMetrics("cosmos.client.op.latency", true);
        this.assertMetrics("cosmos.client.op.calls", true);
        Meter reportedOpRequestCharge = this.assertMetrics("cosmos.client.op.RUs", true);
        validateReasonableRUs(reportedOpRequestCharge, minRu, maxRu);
        this.assertMetrics("cosmos.client.op.regionsContacted", true);
        this.assertMetrics(
            "cosmos.client.op.regionsContacted",
            true,
            Tag.of(TagName.RegionName.toString(), this.preferredRegion));

        if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT) {
            this.assertMetrics("cosmos.client.req.rntbd.latency", true);
            this.assertMetrics(
                "cosmos.client.req.rntbd.latency",
                true,
                Tag.of(TagName.RegionName.toString(), this.preferredRegion));
            this.assertMetrics("cosmos.client.req.rntbd.backendLatency", true);
            this.assertMetrics("cosmos.client.req.rntbd.requests", true);
            Meter reportedRntbdRequestCharge =
                this.assertMetrics("cosmos.client.req.rntbd.RUs", true);
            validateReasonableRUs(reportedRntbdRequestCharge, minRu, maxRu);
            this.assertMetrics("cosmos.client.req.rntbd.timeline", true);
        } else {
            this.assertMetrics("cosmos.client.req.gw.latency", true);
            this.assertMetrics(
                "cosmos.client.req.gw.latency",
                true,
                Tag.of(TagName.RegionName.toString(), this.preferredRegion));
            this.assertMetrics("cosmos.client.req.gw.backendLatency", false);
            this.assertMetrics("cosmos.client.req.gw.requests", true);
            Meter reportedGatewayRequestCharge =
                this.assertMetrics("cosmos.client.req.gw.RUs", true);
            validateReasonableRUs(reportedGatewayRequestCharge, minRu, maxRu);
            this.assertMetrics("cosmos.client.req.gw.timeline", true);
            this.assertMetrics("cosmos.client.req.rntbd", false);
        }
    }

    private void validateItemCountMetrics(Tag expectedOperationTag) {
        this.assertMetrics("cosmos.client.op.maxItemCount", true, expectedOperationTag);
        this.assertMetrics("cosmos.client.op.actualItemCount", true, expectedOperationTag);
    }

    private void validateReasonableRUs(Meter reportedRequestChargeMeter, int expectedMinRu, int expectedMaxRu) {
        List<Measurement> measurements = new ArrayList<>();
        reportedRequestChargeMeter.measure().forEach(measurements::add);

        assertThat(measurements.size()).isGreaterThan(0);
        for (int i = 0; i < measurements.size(); i++) {
            assertThat(measurements.get(i).getValue()).isGreaterThanOrEqualTo(expectedMinRu);
            assertThat(measurements.get(i).getValue()).isLessThanOrEqualTo(expectedMaxRu);
        }
    }
    private void validateMetrics(Tag expectedOperationTag, Tag expectedRequestTag, int minRu, int maxRu) {
        this.assertMetrics("cosmos.client.op.latency", true, expectedOperationTag);
        this.assertMetrics("cosmos.client.op.calls", true, expectedOperationTag);
        Meter reportedOpRequestCharge = this.assertMetrics(
            "cosmos.client.op.RUs", true, expectedOperationTag);
        validateReasonableRUs(reportedOpRequestCharge, minRu, maxRu);

        this.assertMetrics("cosmos.client.op.regionsContacted", true, expectedOperationTag);

        this.assertMetrics(
            "cosmos.client.op.regionsContacted",
            true,
            Tag.of(TagName.RegionName.toString(), this.preferredRegion));

        if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT) {
            this.assertMetrics("cosmos.client.req.rntbd.latency", true, expectedRequestTag);
            this.assertMetrics(
                "cosmos.client.req.rntbd.latency",
                true,
                Tag.of(TagName.RegionName.toString(), this.preferredRegion));
            this.assertMetrics("cosmos.client.req.rntbd.backendLatency", true, expectedRequestTag);
            this.assertMetrics("cosmos.client.req.rntbd.requests", true, expectedRequestTag);
            Meter reportedRntbdRequestCharge =
                this.assertMetrics("cosmos.client.req.rntbd.RUs", true, expectedRequestTag);
            validateReasonableRUs(reportedRntbdRequestCharge, minRu, maxRu);
            this.assertMetrics("cosmos.client.req.rntbd.timeline", true, expectedRequestTag);
        } else {
            this.assertMetrics("cosmos.client.req.gw.latency", true, expectedRequestTag);
            this.assertMetrics(
                "cosmos.client.req.gw.latency",
                true,
                Tag.of(TagName.RegionName.toString(), this.preferredRegion));
            this.assertMetrics("cosmos.client.req.gw.backendLatency", false, expectedRequestTag);
            this.assertMetrics("cosmos.client.req.gw.requests", true, expectedRequestTag);
            Meter reportedGatewayRequestCharge =
                this.assertMetrics("cosmos.client.req.gw.RUs", true, expectedRequestTag);
            validateReasonableRUs(reportedGatewayRequestCharge, minRu, maxRu);
            this.assertMetrics("cosmos.client.req.gw.timeline", true, expectedRequestTag);
            this.assertMetrics("cosmos.client.req.rntbd", false);
        }
    }

    private Meter assertMetrics(String prefix, boolean expectedToFind) {
        return assertMetrics(prefix, expectedToFind, null);
    }

    private Meter assertMetrics(String prefix, boolean expectedToFind, Tag withTag) {
        assertThat(this.meterRegistry).isNotNull();
        assertThat(this.meterRegistry.getMeters()).isNotNull();
        List<Meter> meters = this.meterRegistry.getMeters().stream().collect(Collectors.toList());

        if (expectedToFind) {
            assertThat(meters.size()).isGreaterThan(0);
        }

        List<Meter> meterMatches = meters
            .stream()
            .filter(meter -> meter.getId().getName().startsWith(prefix) &&
                (withTag == null || meter.getId().getTags().contains(withTag)) &&
                meter.measure().iterator().next().getValue() > 0)
            .collect(Collectors.toList());

        if (expectedToFind) {
            assertThat(meterMatches.size()).isGreaterThan(0);

            return meterMatches.get(0);
        } else {
            if (meterMatches.size() > 0) {
                meterMatches.forEach(m ->
                    logger.error("Found unexpected meter {}", m.getId().getName()));
            }
            assertThat(meterMatches.size()).isEqualTo(0);

            return null;
        }
    }

    private Set<String> getAvailableRegionNames(RxDocumentClientImpl rxDocumentClient, boolean isWriteRegion) {
        try {
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            LocationCache locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);

            Field locationInfoField = LocationCache.class.getDeclaredField("locationInfo");
            locationInfoField.setAccessible(true);
            Object locationInfo = locationInfoField.get(locationCache);

            Class<?> DatabaseAccountLocationsInfoClass = Class.forName("com.azure.cosmos.implementation.routing" +
                ".LocationCache$DatabaseAccountLocationsInfo");

            if (isWriteRegion) {
                Field availableWriteEndpointByLocation = DatabaseAccountLocationsInfoClass.getDeclaredField(
                    "availableWriteEndpointByLocation");
                availableWriteEndpointByLocation.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, URI> map = (Map<String, URI>) availableWriteEndpointByLocation.get(locationInfo);
                return map.keySet();
            } else {
                Field availableReadEndpointByLocation = DatabaseAccountLocationsInfoClass.getDeclaredField(
                    "availableReadEndpointByLocation");
                availableReadEndpointByLocation.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, URI> map = (Map<String, URI>) availableReadEndpointByLocation.get(locationInfo);
                return map.keySet();
            }
        } catch (Exception error) {
            fail(error.toString());

            return null;
        }
    }
}
