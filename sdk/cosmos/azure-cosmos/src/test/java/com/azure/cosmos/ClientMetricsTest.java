/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdDurableEndpointMetrics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdServiceEndpoint;
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
import com.azure.cosmos.models.CosmosMetricCategory;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosMetricTagName;
import com.azure.cosmos.models.CosmosMicrometerMeterOptions;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
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
    private CosmosClientTelemetryConfig inputClientTelemetryConfig;
    private CosmosMicrometerMetricsOptions inputMetricsOptions;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public ClientMetricsTest(CosmosClientBuilder clientBuilder) {

        super(clientBuilder);
    }

    private EnumSet<MetricCategory> getEffectiveMetricCategories() {
        return ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .getMetricCategories(this.inputClientTelemetryConfig);
    }

    public void beforeTest(CosmosMetricCategory... metricCategories) {
        assertThat(this.client).isNull();
        assertThat(this.meterRegistry).isNull();

        this.meterRegistry = ConsoleLoggingRegistryFactory.create(1);

        this.inputMetricsOptions = new CosmosMicrometerMetricsOptions()
            .meterRegistry(this.meterRegistry)
            .setMetricCategories(metricCategories);
        this.inputClientTelemetryConfig = new CosmosClientTelemetryConfig()
            .metricsOptions(this.inputMetricsOptions);

        this.client = getClientBuilder()
            .clientTelemetryConfig(inputClientTelemetryConfig)
            .buildClient();

        assertThat(
            ImplementationBridgeHelpers
                .CosmosAsyncClientHelper
                .getCosmosAsyncClientAccessor()
                .getMetricCategories(this.client.asyncClient())
        ).isSameAs(this.getEffectiveMetricCategories());

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

        this.beforeTest(CosmosMetricCategory.DEFAULT);

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
        boolean[] disableLatencyMeterTestCases = { false, true };

        for (boolean disableLatencyMeter: disableLatencyMeterTestCases) {

            this.beforeTest(CosmosMetricCategory.DEFAULT);

            if (disableLatencyMeter) {
                this.inputMetricsOptions
                    .configureMeter(
                        CosmosMetricName.fromString(CosmosMetricName.OPERATION_SUMMARY_LATENCY.toString().toUpperCase(Locale.ROOT)),
                        new CosmosMicrometerMeterOptions().setEnabled(false));
            }

            try {
                InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
                CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
                assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
                validateItemResponse(properties, itemResponse);

                properties = getDocumentDefinition(UUID.randomUUID().toString());
                CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
                validateItemResponse(properties, itemResponse1);

                Tag expectedOperationTag = Tag.of(TagName.OperationStatusCode.toString(), "201");
                // Latency meter can be disabled
                this.assertMetrics("cosmos.client.op.latency", !disableLatencyMeter, expectedOperationTag);

                // Calls meter is never disabled - should always show up
                this.assertMetrics("cosmos.client.op.calls", true, expectedOperationTag);

                if (!disableLatencyMeter) {
                    this.validateMetrics(
                        expectedOperationTag,
                        Tag.of(TagName.RequestStatusCode.toString(), "201/0"),
                        1,
                        300
                    );

                    this.validateMetrics(
                        Tag.of(
                            TagName.Operation.toString(), "Document/Create"),
                        Tag.of(TagName.RequestOperationType.toString(), "Document/Create"),
                        1,
                        300
                    );
                }

            } finally {
                this.afterTest();
            }
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItemWithAllMetrics() throws Exception {

        boolean[] suppressConsistencyLevelTagTestCases = { false, true };

        for (boolean suppressConsistencyLevelTag: suppressConsistencyLevelTagTestCases) {

            this.beforeTest(CosmosMetricCategory.ALL);
            this
                .inputMetricsOptions
                .configureDefaultTagNames(CosmosMetricTagName.ALL);

            if (suppressConsistencyLevelTag) {
                this
                    .inputMetricsOptions
                    .configureMeter(
                        CosmosMetricName.OPERATION_SUMMARY_LATENCY,
                        new CosmosMicrometerMeterOptions()
                            .suppressTagNames(CosmosMetricTagName.fromString("ConsistencyLevel"))
                            .enableHistograms(false)
                            .configurePercentiles(0.99, 0.999));
            }

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
                    300
                );

                this.validateMetrics(
                    Tag.of(
                        TagName.Operation.toString(), "Document/Create"),
                    Tag.of(TagName.RequestOperationType.toString(), "Document/Create"),
                    1,
                    300
                );

                Tag expectedConsistencyTag = Tag.of(TagName.ConsistencyLevel.toString(), "Session");
                this.assertMetrics(
                    "cosmos.client.op.latency",
                    !suppressConsistencyLevelTag,
                    expectedConsistencyTag);

                this.assertMetrics(
                    "cosmos.client.op.calls",
                    true,
                    expectedConsistencyTag);

            } finally {
                this.afterTest();
            }
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                500
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Read"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Read"),
                1,
                500
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
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                1000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Replace"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Replace"),
                1,
                1000
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                0,
                1000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Delete"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Delete"),
                0,
                1000
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception {
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                3000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                1,
                10000
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
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsWithDetailMetrics() throws Exception {
        this.beforeTest(
            CosmosMetricCategory.DEFAULT,
            CosmosMetricCategory.OPERATION_DETAILS,
            CosmosMetricCategory.REQUEST_DETAILS);
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
                10000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                1,
                10000
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
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                100000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/queryItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                1,
                100000
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
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                3000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Patch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Patch"),
                1,
                3000
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                10000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Batch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Batch"),
                1,
                10000
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void batchMultipleItemExecution() {
        this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                3000
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Batch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Batch"),
                1,
                3000
            );
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForDefault() {
        this.beforeTest(CosmosMetricCategory.fromString("DeFAult"));
        try {
            assertThat(this.getEffectiveMetricCategories().size()).isEqualTo(5);

            EnumSet<MetricCategory> clientMetricCategories = ImplementationBridgeHelpers
                .CosmosAsyncClientHelper
                .getCosmosAsyncClientAccessor()
                .getMetricCategories(client.asyncClient());
            assertThat(clientMetricCategories).isEqualTo(this.getEffectiveMetricCategories());

            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.RequestSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.OperationSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectChannels)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectRequests)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.System)).isEqualTo(true);
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForDefaultPlusDetails() {
        this.beforeTest(
            CosmosMetricCategory.DEFAULT,
            CosmosMetricCategory.fromString("RequestDetails"),
            CosmosMetricCategory.fromString("OperationDETAILS"));
        try {
            assertThat(this.getEffectiveMetricCategories().size()).isEqualTo(7);

            EnumSet<MetricCategory> clientMetricCategories = ImplementationBridgeHelpers
                .CosmosAsyncClientHelper
                .getCosmosAsyncClientAccessor()
                .getMetricCategories(client.asyncClient());
            assertThat(clientMetricCategories).isEqualTo(this.getEffectiveMetricCategories());

            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.RequestSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.RequestDetails)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.OperationSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectChannels)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectRequests)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.System)).isEqualTo(true);
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesInvalidCategory() {
        String badCategoryName = "InvalidCategory";
        try {
            this.beforeTest(
                CosmosMetricCategory.DEFAULT,
                CosmosMetricCategory.fromString(badCategoryName));

            fail("Should have thrown exception");
        } catch (IllegalArgumentException argError) {
            assertThat(argError.getMessage()).contains(badCategoryName);
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForAll() {
        this.beforeTest(CosmosMetricCategory.ALL);
        try {
            assertThat(this.getEffectiveMetricCategories().size()).isEqualTo(10);

            EnumSet<MetricCategory> clientMetricCategories = ImplementationBridgeHelpers
                .CosmosAsyncClientHelper
                .getCosmosAsyncClientAccessor()
                .getMetricCategories(client.asyncClient());
            assertThat(clientMetricCategories).isEqualTo(this.getEffectiveMetricCategories());

            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.RequestSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.RequestDetails)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.OperationSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectChannels)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectRequests)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectEndpoints)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.System)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.Legacy)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.AddressResolutions)).isEqualTo(true);
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void endpointMetricsAreDurable() {
        this.beforeTest(CosmosMetricCategory.ALL);
        try {
            if (client.asyncClient().getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
                return;
            }

            RntbdTransportClient transportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(client);
            RntbdServiceEndpoint.Provider endpointProvider =
                (RntbdServiceEndpoint.Provider) ReflectionUtils.getRntbdEndpointProvider(transportClient);

            String address = "https://localhost:12345";
            RntbdEndpoint firstEndpoint = endpointProvider.createIfAbsent(URI.create(address), URI.create(address), Configs.getMinChannelPoolPerEndpointAsInt());
            RntbdDurableEndpointMetrics firstDurableMetricsInstance = firstEndpoint.durableEndpointMetrics();
            firstEndpoint.close();
            assertThat(firstEndpoint.durableEndpointMetrics().getEndpoint()).isNull();

            RntbdEndpoint secondEndpoint = endpointProvider.createIfAbsent(URI.create(address), URI.create(address), Configs.getMinChannelPoolPerEndpointAsInt());

            // ensure metrics are durable across multiple endpoint instances
            assertThat(firstEndpoint).isNotSameAs(secondEndpoint);
            assertThat(firstDurableMetricsInstance).isSameAs(secondEndpoint.durableEndpointMetrics());
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForAllLatebound() {
        this.beforeTest(CosmosMetricCategory.DEFAULT);
        try {

            assertThat(this.getEffectiveMetricCategories().size()).isEqualTo(5);

            EnumSet<MetricCategory> clientMetricCategories = ImplementationBridgeHelpers
                .CosmosAsyncClientHelper
                .getCosmosAsyncClientAccessor()
                .getMetricCategories(client.asyncClient());
            assertThat(clientMetricCategories).isEqualTo(this.getEffectiveMetricCategories());

            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.RequestSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.OperationSummary)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectChannels)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.DirectRequests)).isEqualTo(true);
            assertThat(this.getEffectiveMetricCategories().contains(MetricCategory.System)).isEqualTo(true);
            
            // Now change the metricCategories on the config passed into the CosmosClientBuilder 
            // and validate that these changes take effect immediately on the client build via the builder
            this.inputMetricsOptions
                .setMetricCategories(CosmosMetricCategory.ALL)
                .removeMetricCategories(CosmosMetricCategory.OPERATION_DETAILS)
                .addMetricCategories(CosmosMetricCategory.OPERATION_DETAILS, CosmosMetricCategory.REQUEST_DETAILS)
                .configureDefaultPercentiles(0.9)
                .enableHistogramsByDefault(false)
                .setEnabled(true);
            
            assertThat(this.getEffectiveMetricCategories().size()).isEqualTo(10);

            clientMetricCategories = ImplementationBridgeHelpers
                .CosmosAsyncClientHelper
                .getCosmosAsyncClientAccessor()
                .getMetricCategories(client.asyncClient());
            assertThat(clientMetricCategories).isEqualTo(this.getEffectiveMetricCategories());
        } finally {
            this.afterTest();
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void invalidMeterNameThrows() {
        try {
            CosmosMetricName.fromString("InvalidMeterName");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("InvalidMeterName");
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void invalidMeterCategoryThrows() {
        try {
            CosmosMetricCategory.fromString("InvalidMeterCategory");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("InvalidMeterCategory");
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void invalidMeterTagNameThrows() {
        try {
            CosmosMetricTagName.fromString("InvalidMeterTagName");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("InvalidMeterTagName");
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void meterTagNameFromStringConversion() {
        assertThat(CosmosMetricTagName.fromString("aLl  "))
            .isSameAs(CosmosMetricTagName.ALL);
        assertThat(CosmosMetricTagName.fromString("Default"))
            .isSameAs(CosmosMetricTagName.DEFAULT);
        assertThat(CosmosMetricTagName.fromString("minimum"))
            .isSameAs(CosmosMetricTagName.MINIMUM);
        assertThat(CosmosMetricTagName.fromString("IsForceCollectionRoutingMapRefresh"))
            .isSameAs(CosmosMetricTagName.ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH);
        assertThat(CosmosMetricTagName.fromString("isForcerefresh"))
            .isSameAs(CosmosMetricTagName.ADDRESS_RESOLUTION_FORCED_REFRESH);
        assertThat(CosmosMetricTagName.fromString("ClientCorrelationID"))
            .isSameAs(CosmosMetricTagName.CLIENT_CORRELATION_ID);
        assertThat(CosmosMetricTagName.fromString("container"))
            .isSameAs(CosmosMetricTagName.CONTAINER);
        assertThat(CosmosMetricTagName.fromString(" ConsistencyLevel"))
            .isSameAs(CosmosMetricTagName.CONSISTENCY_LEVEL);
        assertThat(CosmosMetricTagName.fromString("operation"))
            .isSameAs(CosmosMetricTagName.OPERATION);
        assertThat(CosmosMetricTagName.fromString("OperationStatusCode"))
            .isSameAs(CosmosMetricTagName.OPERATION_STATUS_CODE);
        assertThat(CosmosMetricTagName.fromString("PartitionKeyRangeId"))
            .isSameAs(CosmosMetricTagName.PARTITION_KEY_RANGE_ID);
        assertThat(CosmosMetricTagName.fromString("regionname"))
            .isSameAs(CosmosMetricTagName.REGION_NAME);
        assertThat(CosmosMetricTagName.fromString("RequestOperationType"))
            .isSameAs(CosmosMetricTagName.REQUEST_OPERATION_TYPE);
        assertThat(CosmosMetricTagName.fromString("requestStatusCode"))
            .isSameAs(CosmosMetricTagName.REQUEST_STATUS_CODE);
        assertThat(CosmosMetricTagName.fromString("serviceaddress"))
            .isSameAs(CosmosMetricTagName.SERVICE_ADDRESS);
        assertThat(CosmosMetricTagName.fromString("serviceEndpoint"))
            .isSameAs(CosmosMetricTagName.SERVICE_ENDPOINT);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void meterCategoryFromStringConversion() {
        assertThat(CosmosMetricCategory.fromString("aLl  "))
            .isSameAs(CosmosMetricCategory.ALL);
        assertThat(CosmosMetricCategory.fromString("Default"))
            .isSameAs(CosmosMetricCategory.DEFAULT);
        assertThat(CosmosMetricCategory.fromString("minimum"))
            .isSameAs(CosmosMetricCategory.MINIMUM);
        assertThat(CosmosMetricCategory.fromString("operationsummary "))
            .isSameAs(CosmosMetricCategory.OPERATION_SUMMARY);
        assertThat(CosmosMetricCategory.fromString("operationDetails"))
            .isSameAs(CosmosMetricCategory.OPERATION_DETAILS);
        assertThat(CosmosMetricCategory.fromString("RequestSummary"))
            .isSameAs(CosmosMetricCategory.REQUEST_SUMMARY);
        assertThat(CosmosMetricCategory.fromString("RequestDetails"))
            .isSameAs(CosmosMetricCategory.REQUEST_DETAILS);
        assertThat(CosmosMetricCategory.fromString("DirectChannels"))
            .isSameAs(CosmosMetricCategory.DIRECT_CHANNELS);
        assertThat(CosmosMetricCategory.fromString("DirectRequests"))
            .isSameAs(CosmosMetricCategory.DIRECT_REQUESTS);
        assertThat(CosmosMetricCategory.fromString("DirectEndpoints"))
            .isSameAs(CosmosMetricCategory.DIRECT_ENDPOINTS);
        assertThat(CosmosMetricCategory.fromString("DirectAddressResolutions"))
            .isSameAs(CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS);
        assertThat(CosmosMetricCategory.fromString("system"))
            .isSameAs(CosmosMetricCategory.SYSTEM);
        assertThat(CosmosMetricCategory.fromString("Legacy"))
            .isSameAs(CosmosMetricCategory.LEGACY);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void meterNameFromStringConversion() {
        assertThat(CosmosMetricName.fromString("cosmos.client.op.laTency"))
            .isSameAs(CosmosMetricName.OPERATION_SUMMARY_LATENCY);
        assertThat(CosmosMetricName.fromString("cosmos.client.op.cAlls"))
            .isSameAs(CosmosMetricName.OPERATION_SUMMARY_CALLS);
        assertThat(CosmosMetricName.fromString("cosmos.client.op.rus"))
            .isSameAs(CosmosMetricName.OPERATION_SUMMARY_REQUEST_CHARGE);
        assertThat(CosmosMetricName.fromString("cosmos.client.OP.actualItemCount"))
            .isSameAs(CosmosMetricName.OPERATION_DETAILS_ACTUAL_ITEM_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.op.MAXItemCount"))
            .isSameAs(CosmosMetricName.OPERATION_DETAILS_MAX_ITEM_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.op.REGIONScontacted"))
            .isSameAs(CosmosMetricName.OPERATION_DETAILS_REGIONS_CONTACTED);

        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.rntbd.backendLatency"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_DIRECT_BACKEND_LATENCY);
        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.rntbd.LAtency"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_DIRECT_LATENCY);
        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.rntbd.RUS"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUEST_CHARGE);
        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.rntbd.ReQUEsts"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUESTS);
        assertThat(CosmosMetricName.fromString("cosmos.client.req.rntbd.TIMEline"))
            .isSameAs(CosmosMetricName.REQUEST_DETAILS_DIRECT_TIMELINE);

        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.gw.LAtency"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY);
        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.gw.RUS"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE);
        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.gw.ReQUEsts"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS);
        assertThat(CosmosMetricName.fromString("cosmos.client.req.gw.tiMELine"))
            .isSameAs(CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE);

        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.addressResolution.latency"))
            .isSameAs(CosmosMetricName.DIRECT_ADDRESS_RESOLUTION_LATENCY);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.addressResolution.requests"))
            .isSameAs(CosmosMetricName.DIRECT_ADDRESS_RESOLUTION_REQUESTS);

        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.channels.acquired.COUNT"))
            .isSameAs(CosmosMetricName.DIRECT_CHANNELS_ACQUIRED_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.channels.available.COUNT"))
            .isSameAs(CosmosMetricName.DIRECT_CHANNELS_AVAILABLE_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.channels.closed.COUNT"))
            .isSameAs(CosmosMetricName.DIRECT_CHANNELS_CLOSED_COUNT);

        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.endpoints.COUNT"))
            .isSameAs(CosmosMetricName.DIRECT_ENDPOINTS_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.endpoints.evicted"))
            .isSameAs(CosmosMetricName.DIRECT_ENDPOINTS_EVICTED);

        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.requests.concurrent.count"))
            .isSameAs(CosmosMetricName.DIRECT_REQUEST_CONCURRENT_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.requests.LAtency"))
            .isSameAs(CosmosMetricName.DIRECT_REQUEST_LATENCY);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.requests.FAIled.latency"))
            .isSameAs(CosmosMetricName.DIRECT_REQUEST_LATENCY_FAILED);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.requests.successful.latency"))
            .isSameAs(CosmosMetricName.DIRECT_REQUEST_LATENCY_SUCCESS);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.requests.queued.count"))
            .isSameAs(CosmosMetricName.DIRECT_REQUEST_QUEUED_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.req.RSPsize"))
            .isSameAs(CosmosMetricName.DIRECT_REQUEST_SIZE_RESPONSE);
        assertThat(CosmosMetricName.fromString("cosmos.client.RNTBD.req.reqsize"))
            .isSameAs(CosmosMetricName.DIRECT_REQUEST_SIZE_REQUEST);
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

    private void validateItemCountMetrics(Tag expectedOperationTag) {
        if (this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)) {
            this.assertMetrics("cosmos.client.op.maxItemCount", true, expectedOperationTag);
            this.assertMetrics("cosmos.client.op.actualItemCount", true, expectedOperationTag);
        }
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

        if (this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)) {
            this.assertMetrics("cosmos.client.op.regionsContacted", true, expectedOperationTag);

            this.assertMetrics(
                "cosmos.client.op.regionsContacted",
                true,
                Tag.of(TagName.RegionName.toString(), this.preferredRegion));
        }

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

            if (this.getEffectiveMetricCategories().contains(MetricCategory.RequestDetails)) {
                this.assertMetrics("cosmos.client.req.rntbd.timeline", true, expectedRequestTag);
            }
        } else {
            this.assertMetrics("cosmos.client.req.gw.latency", true, expectedRequestTag);

            if (this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)) {
                this.assertMetrics(
                    "cosmos.client.req.gw.latency",
                    true,
                    Tag.of(TagName.RegionName.toString(), this.preferredRegion));
            }
            this.assertMetrics("cosmos.client.req.gw.backendLatency", false, expectedRequestTag);
            this.assertMetrics("cosmos.client.req.gw.requests", true, expectedRequestTag);
            Meter reportedGatewayRequestCharge =
                this.assertMetrics("cosmos.client.req.gw.RUs", true, expectedRequestTag);
            validateReasonableRUs(reportedGatewayRequestCharge, minRu, maxRu);

            if (this.getEffectiveMetricCategories().contains(MetricCategory.RequestDetails)) {
                this.assertMetrics("cosmos.client.req.gw.timeline", true, expectedRequestTag);
            }

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
