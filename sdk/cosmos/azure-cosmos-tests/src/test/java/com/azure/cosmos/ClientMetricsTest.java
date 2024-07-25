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
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.apachecommons.math.util.Pair;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
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
import com.azure.cosmos.models.CosmosItemIdentity;
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
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedResponse;
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
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.SkipException;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClientMetricsTest extends BatchTestBase {

    private CosmosClient client;
    private CosmosContainer container;
    private String databaseId;
    private String containerId;
    private String preferredRegion;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public ClientMetricsTest(CosmosClientBuilder clientBuilder) {

        super(clientBuilder);
    }

    private EnumSet<MetricCategory> getEffectiveMetricCategories() {
        return ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .getMetricCategories(this.client.asyncClient().getClientTelemetryConfig());
    }

    public MeterRegistry beforeTest(CosmosMetricCategory... metricCategories) {
        return beforeTest(null, metricCategories);
    }

    public MeterRegistry beforeTest(
        CosmosDiagnosticsThresholds thresholds,
        CosmosMetricCategory... metricCategories) {
        assertThat(this.client).isNull();

        MeterRegistry meterRegistry = ConsoleLoggingRegistryFactory.create(1);

         CosmosMicrometerMetricsOptions inputMetricsOptions = new CosmosMicrometerMetricsOptions()
            .meterRegistry(meterRegistry)
            .setMetricCategories(metricCategories)
            .configureDefaultTagNames(
                CosmosMetricTagName.DEFAULT,
                CosmosMetricTagName.PARTITION_ID,
                CosmosMetricTagName.REPLICA_ID,
                CosmosMetricTagName.OPERATION_SUB_STATUS_CODE,
                CosmosMetricTagName.PARTITION_KEY_RANGE_ID);

        CosmosClientTelemetryConfig inputClientTelemetryConfig = new CosmosClientTelemetryConfig()
            .metricsOptions(inputMetricsOptions);


        if (thresholds != null) {
            inputClientTelemetryConfig.diagnosticsThresholds(thresholds);
            inputMetricsOptions.applyDiagnosticThresholdsForTransportLevelMeters(true);
        }

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

        List<String> writeRegions = this.getAvailableWriteRegionNames(rxDocumentClient);
        assertThat(writeRegions).isNotNull().isNotEmpty();
        this.preferredRegion = writeRegions.iterator().next();

        if (databaseId == null) {
            CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
            this.databaseId = asyncContainer.getDatabase().getId();
            this.containerId = asyncContainer.getId();
        }

        container = client.getDatabase(databaseId).getContainer(containerId);
        return meterRegistry;
    }

    public Pair<MeterRegistry, CosmosMicrometerMetricsOptions> beforeTestInputMetrics(
        CosmosDiagnosticsThresholds thresholds,
        CosmosMetricCategory... metricCategories) {
        assertThat(this.client).isNull();

        MeterRegistry meterRegistry = ConsoleLoggingRegistryFactory.create(1);

        CosmosMicrometerMetricsOptions inputMetricsOptions = new CosmosMicrometerMetricsOptions()
            .meterRegistry(meterRegistry)
            .setMetricCategories(metricCategories)
            .configureDefaultTagNames(
                CosmosMetricTagName.DEFAULT,
                CosmosMetricTagName.PARTITION_ID,
                CosmosMetricTagName.REPLICA_ID,
                CosmosMetricTagName.OPERATION_SUB_STATUS_CODE,
                CosmosMetricTagName.PARTITION_KEY_RANGE_ID);

        CosmosClientTelemetryConfig inputClientTelemetryConfig = new CosmosClientTelemetryConfig()
            .metricsOptions(inputMetricsOptions);


        if (thresholds != null) {
            inputClientTelemetryConfig.diagnosticsThresholds(thresholds);
            inputMetricsOptions.applyDiagnosticThresholdsForTransportLevelMeters(true);
        }

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

        List<String> writeRegions = this.getAvailableWriteRegionNames(rxDocumentClient);
        assertThat(writeRegions).isNotNull().isNotEmpty();
        this.preferredRegion = writeRegions.iterator().next();

        if (databaseId == null) {
            CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
            this.databaseId = asyncContainer.getDatabase().getId();
            this.containerId = asyncContainer.getId();
        }

        container = client.getDatabase(databaseId).getContainer(containerId);
        return new Pair<>(meterRegistry, inputMetricsOptions);
    }

    public void afterTest(MeterRegistry meterRegistry) {
        this.container = null;
        CosmosClient clientSnapshot = this.client;
        if (clientSnapshot != null) {
            this.client.close();
        }
        this.client = null;

        if (meterRegistry != null) {
            meterRegistry.clear();
            meterRegistry.close();
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void maxValueExceedingDefinedLimitStillWorksWithoutException() throws Exception {

        // Expected behavior is that higher values than the expected max value can still be recorded
        // it would only result in getting less accurate "estimates" for percentile histograms

        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);

        try {
            Tag dummyOperationTag = Tag.of(TagName.Operation.toString(), "TestDummy");
            Timer latencyMeter = Timer
                .builder("cosmos.client.op.latency")
                .description("Operation latency")
                .maximumExpectedValue(Duration.ofSeconds(300))
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(Collections.singleton(dummyOperationTag))
                .register(meterRegistry);
            latencyMeter.record(Duration.ofSeconds(600));

            Meter requestLatencyMeter = this.assertMetrics(
                "cosmos.client.op.latency",
                true,
                dummyOperationTag,
                meterRegistry);

            List<Measurement> measurements = new ArrayList<>();
            requestLatencyMeter.measure().forEach(measurements::add);

            int expectedMeasurementCount = 3;
            if (measurements.size() < expectedMeasurementCount) {
                logger.error("Size should have been 3 but was {}", measurements.size());
                for (int i = 0; i < measurements.size(); i++) {
                    Measurement m = measurements.get(i);
                    logger.error(
                        "{}: {}",
                        i,
                        m);
                }
            }

            assertThat(measurements.size()).isGreaterThanOrEqualTo(expectedMeasurementCount);

            assertThat(measurements.get(0).getStatistic().getTagValueRepresentation()).isEqualTo("count");
            assertThat(measurements.get(0).getValue()).isEqualTo(1);
            assertThat(measurements.get(1).getStatistic().getTagValueRepresentation()).isEqualTo("total");
            assertThat(measurements.get(1).getValue()).isEqualTo(600 * 1000); // transform into milliseconds
            assertThat(measurements.get(2).getStatistic().getTagValueRepresentation()).isEqualTo("max");
            assertThat(measurements.get(2).getValue()).isEqualTo(600 * 1000); // transform into milliseconds
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void createItem() throws Exception {
        boolean[] disableLatencyMeterTestCases = { false, true };

        for (boolean disableLatencyMeter: disableLatencyMeterTestCases) {

           Pair<MeterRegistry, CosmosMicrometerMetricsOptions> entry = this.beforeTestInputMetrics(null, CosmosMetricCategory.DEFAULT);

            if (disableLatencyMeter) {
                entry.getValue()
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
                this.assertMetrics("cosmos.client.op.latency", !disableLatencyMeter, expectedOperationTag, entry.getKey());
                Tag expectedSubStatusCodeOperationTag = Tag.of(TagName.OperationSubStatusCode.toString(), "0");
                this.assertMetrics("cosmos.client.op.latency", !disableLatencyMeter, expectedSubStatusCodeOperationTag, entry.getKey());

                // Calls meter is never disabled - should always show up
                this.assertMetrics("cosmos.client.op.calls", true, expectedOperationTag, entry.getKey());

                if (!disableLatencyMeter) {
                    Tag expectedRequestTag = Tag.of(TagName.RequestStatusCode.toString(), "201/0");
                    this.validateMetrics(
                        expectedOperationTag,
                        expectedRequestTag,
                        0,
                        300,
                        entry.getKey()
                    );

                    // also ensure the replicaId dimension is populated for DIRECT mode
                    if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT) {
                        Meter foundMeter = this.assertMetrics(
                            "cosmos.client.req.rntbd.latency",
                            true,
                            expectedRequestTag,
                            entry.getKey());
                        assertThat(foundMeter).isNotNull();
                        boolean replicaIdDimensionExists = foundMeter
                            .getId()
                            .getTags()
                            .stream()
                            .anyMatch(tag -> tag.getKey().equals(TagName.ReplicaId.toString()) &&
                                !tag.getValue().equals("NONE"));
                        assertThat(replicaIdDimensionExists)
                            .isEqualTo(true);
                    }

                    this.validateMetrics(
                        Tag.of(
                            TagName.Operation.toString(), "Document/Create"),
                        Tag.of(TagName.RequestOperationType.toString(), "Document/Create"),
                        0,
                        300,
                       entry.getKey()
                    );
                }

            } finally {
                this.afterTest(entry.getKey());
            }
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void createItemWithAllMetrics() throws Exception {

        boolean[] suppressConsistencyLevelTagTestCases = { false, true };

        for (boolean suppressConsistencyLevelTag: suppressConsistencyLevelTagTestCases) {

            Pair<MeterRegistry, CosmosMicrometerMetricsOptions> entry = this.beforeTestInputMetrics(null, CosmosMetricCategory.ALL);
            entry.getValue()
                .configureDefaultTagNames(CosmosMetricTagName.ALL);
            MeterRegistry meterRegistry = entry.getKey();

            if (suppressConsistencyLevelTag) {
                entry.getValue()
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
                    0,
                    300,
                    meterRegistry
                );

                this.validateMetrics(
                    Tag.of(
                        TagName.Operation.toString(), "Document/Create"),
                    Tag.of(TagName.RequestOperationType.toString(), "Document/Create"),
                    0,
                    300,
                    meterRegistry
                );

                String expectedConsistencyLevel = ImplementationBridgeHelpers
                    .CosmosAsyncClientHelper
                    .getCosmosAsyncClientAccessor()
                    .getEffectiveConsistencyLevel(
                        client.asyncClient(),
                        OperationType.Create,
                        null)
                    .toString();
                Tag expectedConsistencyTag = Tag.of(TagName.ConsistencyLevel.toString(), expectedConsistencyLevel);
                this.assertMetrics(
                    "cosmos.client.op.latency",
                    !suppressConsistencyLevelTag,
                    expectedConsistencyTag,
                    meterRegistry);

                this.assertMetrics(
                    "cosmos.client.op.calls",
                    true,
                    expectedConsistencyTag,
                    meterRegistry);

            } finally {
                this.afterTest(meterRegistry);
            }
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                new PartitionKey(properties.get("mypk")),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class);
            validateItemResponse(properties, readResponse1);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                0,
                500,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Read"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Read"),
                0,
                500,
                meterRegistry
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection_QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", false, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag, meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readManySingleItem() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            List<CosmosItemIdentity> tuplesToBeRead = new ArrayList<>();
            tuplesToBeRead.add(new CosmosItemIdentity(
                new PartitionKey(properties.get("mypk")),
                properties.getId()
            ));

            FeedResponse<InternalObjectNode> readManyResponse = container.readMany(
                tuplesToBeRead,
                new CosmosReadManyRequestOptions(),
                InternalObjectNode.class);
            validateReadManyFeedResponse(Arrays.asList(properties), readManyResponse);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                0,
                500,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/readMany"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Read"),
                0,
                500,
                meterRegistry
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection_QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", false, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag, meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readManyMultipleItems() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);

        List<InternalObjectNode> createdDocs = new ArrayList<>();
        List<CosmosItemIdentity> tuplesToBeRead = new ArrayList<>();
        try {
            for (int i = 0; i < 20; i++) {
                InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
                container.createItem(properties);
                createdDocs.add(properties);
                tuplesToBeRead.add(new CosmosItemIdentity(
                    new PartitionKey(properties.get("mypk")),
                    properties.getId()
                ));
            }


            FeedResponse<InternalObjectNode> readManyResponse = container.readMany(
                tuplesToBeRead,
                new CosmosReadManyRequestOptions(),
                InternalObjectNode.class);
            validateReadManyFeedResponse(createdDocs, readManyResponse);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                0,
                500,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/readMany"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                0,
                500,
                meterRegistry
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection_QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", false, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag, meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    private void runReadItemTestWithThresholds(
        CosmosDiagnosticsThresholds thresholds,
        boolean expectRequestMetrics
    ) {
        MeterRegistry meterRegistry = this.beforeTest(thresholds, CosmosMetricCategory.DEFAULT);
        try {

            if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
                throw new SkipException("Test case only relevant for direct model.");
            }

            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                new PartitionKey(properties.get("mypk")),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class);
            validateItemResponse(properties, readResponse1);

            CosmosDiagnosticsThresholds maxThresholds = new CosmosDiagnosticsThresholds()
                .setPointOperationLatencyThreshold(Duration.ofDays(1));

            Tag operationTag = Tag.of(TagName.OperationStatusCode.toString(), "200");
            Tag expectedSubStatusCodeOperationTag = Tag.of(TagName.OperationSubStatusCode.toString(), "0");
            Tag requestTag = Tag.of(TagName.RequestStatusCode.toString(), "200/0");
            this.assertMetrics("cosmos.client.op.latency", true, operationTag, meterRegistry);
            this.assertMetrics("cosmos.client.op.latency", true, expectedSubStatusCodeOperationTag, meterRegistry);
            this.assertMetrics("cosmos.client.op.calls", true, operationTag, meterRegistry);
            this.assertMetrics("cosmos.client.op.calls", true, expectedSubStatusCodeOperationTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd.latency", expectRequestMetrics, requestTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd.backendLatency", expectRequestMetrics, requestTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd.requests", expectRequestMetrics, requestTag, meterRegistry);
            Meter reportedRntbdRequestCharge =
                this.assertMetrics("cosmos.client.req.rntbd.RUs", expectRequestMetrics, requestTag, meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readItemWithThresholdsApplied() throws Exception {
        CosmosDiagnosticsThresholds maxThresholds = new CosmosDiagnosticsThresholds()
            .setPointOperationLatencyThreshold(Duration.ofDays(1));
        CosmosDiagnosticsThresholds minThresholds = new CosmosDiagnosticsThresholds()
            .setPointOperationLatencyThreshold(Duration.ZERO);

        runReadItemTestWithThresholds(maxThresholds, false);
        runReadItemTestWithThresholds(minThresholds, true);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void replaceItem() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

            validateItemResponse(properties, itemResponse);
            String newPropValue = UUID.randomUUID().toString();
            properties.set("newProp", newPropValue, CosmosItemSerializer.DEFAULT_SERIALIZER);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            ModelBridgeInternal.setPartitionKey(options, new PartitionKey(properties.get("mypk")));
            // replace document
            CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(properties,
                properties.getId(),
                new PartitionKey(properties.get("mypk")),
                options);
            assertThat(BridgeInternal.getProperties(replace).get("newProp")).isEqualTo(newPropValue);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                0,
                1000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Replace"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Replace"),
                0,
                1000,
                meterRegistry
            );
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();

            CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
                new PartitionKey(properties.get("mypk")),
                options);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(204);

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "204"),
                Tag.of(TagName.RequestStatusCode.toString(), "204/0"),
                0,
                1000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Delete"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Delete"),
                0,
                1000,
                meterRegistry
            );
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);
            assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

            // draining the iterator - metrics will only be emitted at the end
            feedResponseIterator3.stream().collect(Collectors.toList());

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                0,
                3000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                0,
                10000,
                meterRegistry
            );

            this.validateItemCountMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                meterRegistry
            );

            this.validateRequestActualItemCountMetrics(
                meterRegistry,
                Tag.of(
                    TagName.Operation.toString(),
                    "Document/ReadFeed/readAllItems." + container.getId()));

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.gw.requests", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.gw.RUs", false, queryPlanTag,meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readAllItemsWithDetailMetrics() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(
            CosmosMetricCategory.DEFAULT,
            CosmosMetricCategory.OPERATION_DETAILS,
            CosmosMetricCategory.REQUEST_DETAILS);
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 = container
                .readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);
            assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

            // draining the iterator - metrics will only be emitted at the end
            feedResponseIterator3.stream().collect(Collectors.toList());

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                0,
                10000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                0,
                10000,
                meterRegistry
            );

            this.validateItemCountMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                meterRegistry
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.gw.requests", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.gw.RUs", false, queryPlanTag, meterRegistry);

            this.assertMetrics("cosmos.client.req.gw.timeline", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.op.maxItemCount", true, meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void readAllItemsWithDetailMetricsWithExplicitPageSize() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(
            CosmosMetricCategory.DEFAULT,
            CosmosMetricCategory.OPERATION_DETAILS,
            CosmosMetricCategory.REQUEST_DETAILS);
        try {
            InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(properties);

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 = new CosmosPagedIterable<>(
                container.asyncContainer.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class),
                10);
            assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

            // draining the iterator - metrics will only be emitted at the end
            feedResponseIterator3.stream().collect(Collectors.toList());

            this.validateMetrics(
                Tag.of(TagName.OperationStatusCode.toString(), "200"),
                Tag.of(TagName.RequestStatusCode.toString(), "200/0"),
                0,
                10000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                0,
                10000,
                meterRegistry
            );

            this.validateItemCountMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/ReadFeed/readAllItems." + container.getId()),
                meterRegistry
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.gw.requests", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.gw.RUs", false, queryPlanTag, meterRegistry);

            this.assertMetrics("cosmos.client.req.gw.timeline", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.op.maxItemCount", true, meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void queryItems() throws Exception {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.ALL);
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
                0,
                100000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/queryItems." + container.getId()),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Query"),
                0,
                100000,
                meterRegistry
            );

            this.validateItemCountMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/queryItems." + container.getId()),
                meterRegistry
            );

            this.validateRequestActualItemCountMetrics(
                meterRegistry,
                Tag.of(
                    TagName.Operation.toString(), "Document/Query/queryItems." + container.getId())
            );

            Tag queryPlanTag = Tag.of(TagName.RequestOperationType.toString(), "DocumentCollection/QueryPlan");
            this.assertMetrics("cosmos.client.req.gw", true, queryPlanTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd", false, queryPlanTag, meterRegistry);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 10)
    public void itemPatchSuccess() {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                0,
                3000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Patch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Patch"),
                0,
                3000,
                meterRegistry
            );
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void createItem_withBulk() {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                0,
                10000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Batch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Batch"),
                0,
                10000,
                meterRegistry
            );

            this.validateRequestActualItemCountMetrics(
                meterRegistry,
                Tag.of(TagName.Operation.toString(), "Document/Batch"),
                Tag.of(TagName.PartitionKeyRangeId.toString(), "0"),
                Tag.of(TagName.PartitionKeyRangeId.toString(), "1"));
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void batchMultipleItemExecution() {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.DEFAULT);
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
                0,
                3000,
                meterRegistry
            );

            this.validateMetrics(
                Tag.of(
                    TagName.Operation.toString(), "Document/Batch"),
                Tag.of(TagName.RequestOperationType.toString(), "Document/Batch"),
                0,
                3000,
                meterRegistry
            );
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForDefault() {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.fromString("DeFAult"));
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
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForDefaultPlusDetails() {
        MeterRegistry meterRegistry = this.beforeTest(
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
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesInvalidCategory() {
        String badCategoryName = "InvalidCategory";
        MeterRegistry meterRegistry = null;
        try {
             meterRegistry = this.beforeTest(
                CosmosMetricCategory.DEFAULT,
                CosmosMetricCategory.fromString(badCategoryName));

            fail("Should have thrown exception");
        } catch (IllegalArgumentException argError) {
            assertThat(argError.getMessage()).contains(badCategoryName);
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForAll() {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.ALL);
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
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void endpointMetricsAreDurable() throws IllegalAccessException {
        MeterRegistry meterRegistry = this.beforeTest(CosmosMetricCategory.ALL);
        try {
            if (client.asyncClient().getConnectionPolicy().getConnectionMode() != ConnectionMode.DIRECT) {
                return;
            }

            RntbdTransportClient transportClient = (RntbdTransportClient) ReflectionUtils.getTransportClient(client);
            RntbdServiceEndpoint.Provider endpointProvider =
                (RntbdServiceEndpoint.Provider) ReflectionUtils.getRntbdEndpointProvider(transportClient);
            ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor =
                    ReflectionUtils.getProactiveOpenConnectionsProcessor(transportClient);
            AddressSelector addressSelector = (AddressSelector) FieldUtils.readField(transportClient, "addressSelector", true);

            String address = "https://localhost:12345";
            RntbdEndpoint firstEndpoint = endpointProvider.createIfAbsent(URI.create(address), new Uri(address), proactiveOpenConnectionsProcessor, Configs.getMinConnectionPoolSizePerEndpoint(), addressSelector);
            RntbdDurableEndpointMetrics firstDurableMetricsInstance = firstEndpoint.durableEndpointMetrics();
            firstEndpoint.close();
            assertThat(firstEndpoint.durableEndpointMetrics().getEndpoint()).isNull();

            RntbdEndpoint secondEndpoint = endpointProvider.createIfAbsent(URI.create(address), new Uri(address), proactiveOpenConnectionsProcessor, Configs.getMinConnectionPoolSizePerEndpoint(), addressSelector);

            // ensure metrics are durable across multiple endpoint instances
            assertThat(firstEndpoint).isNotSameAs(secondEndpoint);
            assertThat(firstDurableMetricsInstance).isSameAs(secondEndpoint.durableEndpointMetrics());
        } finally {
            this.afterTest(meterRegistry);
        }
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void effectiveMetricCategoriesForAllLatebound() {
        Pair<MeterRegistry, CosmosMicrometerMetricsOptions> entry = this.beforeTestInputMetrics(null, CosmosMetricCategory.DEFAULT);
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
            entry.getValue()
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
            this.afterTest(entry.getKey());
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void invalidMeterNameThrows() {
        try {
            CosmosMetricName.fromString("InvalidMeterName");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("InvalidMeterName");
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void invalidMeterCategoryThrows() {
        try {
            CosmosMetricCategory.fromString("InvalidMeterCategory");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("InvalidMeterCategory");
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
    public void invalidMeterTagNameThrows() {
        try {
            CosmosMetricTagName.fromString("InvalidMeterTagName");
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("InvalidMeterTagName");
        }
    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
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
        assertThat(CosmosMetricTagName.fromString("partitionID"))
            .isSameAs(CosmosMetricTagName.PARTITION_ID);
        assertThat(CosmosMetricTagName.fromString("REPLICAid"))
            .isSameAs(CosmosMetricTagName.REPLICA_ID);

    }

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
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

    @Test(groups = {"fast"}, timeOut = TIMEOUT)
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

        assertThat(CosmosMetricName.fromString("cosmos.client.req.reqPaylOADSize"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_SIZE_REQUEST);
        assertThat(CosmosMetricName.fromString("cosmos.client.req.rspPayloadSIZE"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_SIZE_RESPONSE);

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
        assertThat(CosmosMetricName.fromString("cosmos.client.Req.rntbd.actualItemCount"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_DIRECT_ACTUAL_ITEM_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.req.rntbd.actualITemCount"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_DIRECT_ACTUAL_ITEM_COUNT);

        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.gw.LAtency"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY);
        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.gw.RUS"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE);
        assertThat(CosmosMetricName.fromString("cosmos.CLIENT.req.gw.ReQUEsts"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS);
        assertThat(CosmosMetricName.fromString("cosmos.client.req.gw.tiMELine"))
            .isSameAs(CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE);
        assertThat(CosmosMetricName.fromString("cosmos.client.Req.gw.actualItemCount"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_ACTUAL_ITEM_COUNT);
        assertThat(CosmosMetricName.fromString("cosmos.client.req.gw.actualITemCount"))
            .isSameAs(CosmosMetricName.REQUEST_SUMMARY_GATEWAY_ACTUAL_ITEM_COUNT);

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

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void metricConfigsThroughSystemProperty() {
        System.setProperty(
            "COSMOS.METRICS_CONFIG",
            "{\"metricCategories\":\"[OperationDetails]\","
                + "\"tagNames\":\"[PartitionId]\","
                + "\"sampleRate\":0.5,"
                + "\"percentiles\":[0.90,0.99],"
                + "\"enableHistograms\":false,"
                + "\"applyDiagnosticThresholdsForTransportLevelMeters\":true}");

        CosmosClientBuilder testClientBuilder = new CosmosClientBuilder();
        CosmosClientTelemetryConfig clientTelemetryConfig = ReflectionUtils.getClientTelemetryConfig(testClientBuilder);
        EnumSet<MetricCategory> effectiveMetricsCategory = ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .getMetricCategories(clientTelemetryConfig);
        assertThat(effectiveMetricsCategory).containsAll(MetricCategory.MINIMAL_CATEGORIES);
        assertThat(effectiveMetricsCategory).contains(MetricCategory.OperationDetails);

        EnumSet<TagName> effectiveTagNames =
            ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor()
                .getMetricTagNames(clientTelemetryConfig);
        assertThat(effectiveTagNames).containsAll(TagName.MINIMUM_TAGS);
        assertThat(effectiveTagNames).contains(TagName.PartitionId);

        double sampleRate = ImplementationBridgeHelpers
            .CosmosClientTelemetryConfigHelper
            .getCosmosClientTelemetryConfigAccessor()
            .getSamplingRate(clientTelemetryConfig);
        assertThat(sampleRate).isEqualTo(0.5);

        double[] percentiles =
            ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor()
                .getDefaultPercentiles(clientTelemetryConfig);
        assertThat(percentiles).contains(0.90, 0.99);

        boolean publishHistograms =
            ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor()
                .shouldPublishHistograms(clientTelemetryConfig);
        assertThat(publishHistograms).isFalse();

        boolean applyDiagnosticThresholdsForTransportLevelMeters =
            ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor()
                .shouldApplyDiagnosticThresholdsForTransportLevelMeters(clientTelemetryConfig);
        assertThat(applyDiagnosticThresholdsForTransportLevelMeters).isTrue();
        System.clearProperty("COSMOS.METRICS_CONFIG");
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

    private void validateReadManyFeedResponse(
        List<InternalObjectNode> createdDocs,
        FeedResponse<InternalObjectNode> readManyResponse) {
        // Basic validation
        assertThat(readManyResponse).isNotNull();
        assertThat(readManyResponse.getResults()).isNotNull();
        List<InternalObjectNode> docsFromResponse = readManyResponse.getResults();
        assertThat(docsFromResponse).hasSize(createdDocs.size());
        for (InternalObjectNode doc: createdDocs) {
            assertThat(docsFromResponse.stream().anyMatch(r -> r.getId() != null && r.getId().equals(doc.getId())));
        }
    }

    private void validateItemCountMetrics(Tag expectedOperationTag, MeterRegistry meterRegistry) {
        if (this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)) {
            this.assertMetrics("cosmos.client.op.maxItemCount", true, expectedOperationTag, meterRegistry);
            this.assertMetrics("cosmos.client.op.actualItemCount", true, expectedOperationTag, meterRegistry);
        }
    }

    private void validateRequestActualItemCountMetrics(MeterRegistry meterRegistry, Tag... expectedRequestTags) {
        if (this.getEffectiveMetricCategories().contains(MetricCategory.RequestSummary)) {
            if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT) {
                for (Tag expectedRequestTag : expectedRequestTags) {
                    this.assertMetrics("cosmos.client.req.rntbd.actualItemCount", true, expectedRequestTag, meterRegistry);
                }
            } else {
                for (Tag expectedRequestTag : expectedRequestTags) {
                    this.assertMetrics("cosmos.client.req.gw.actualItemCount", true, expectedRequestTag, meterRegistry);
                }
            }
        }
    }

    private void validateReasonableRUs(Meter reportedRequestChargeMeter, int expectedMinRu, int expectedMaxRu) {
        List<Measurement> measurements = new ArrayList<>();
        reportedRequestChargeMeter.measure().forEach(measurements::add);
        logger.info("RequestedRequestChargeMeter: {} {}", reportedRequestChargeMeter, reportedRequestChargeMeter.getId());
        assertThat(measurements.size()).isGreaterThan(0);
        for (int i = 0; i < measurements.size(); i++) {

            assertThat(measurements.get(i).getValue()).isGreaterThanOrEqualTo(expectedMinRu);
            assertThat(measurements.get(i).getValue()).isLessThanOrEqualTo(expectedMaxRu);
        }
    }

    private void validateMetrics(Tag expectedOperationTag, Tag expectedRequestTag, int minRu, int maxRu, MeterRegistry meterRegistry) {
        this.assertMetrics("cosmos.client.op.latency", true, expectedOperationTag, meterRegistry);
        this.assertMetrics("cosmos.client.op.calls", true, expectedOperationTag, meterRegistry);

        if (expectedOperationTag.getKey() == "OperationStatusCode" &&
            ("200".equals(expectedOperationTag.getValue()) || "201".equals(expectedOperationTag.getValue()))) {

            Tag expectedSubStatusCodeOperationTag = Tag.of(TagName.OperationSubStatusCode.toString(), "0");
            this.assertMetrics("cosmos.client.op.latency", true, expectedSubStatusCodeOperationTag, meterRegistry);
            this.assertMetrics("cosmos.client.op.calls", true, expectedSubStatusCodeOperationTag, meterRegistry);
        }
        Meter reportedOpRequestCharge = this.assertMetrics(
            "cosmos.client.op.RUs", true, expectedOperationTag, meterRegistry);
        validateReasonableRUs(reportedOpRequestCharge, minRu, maxRu);

        if (this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)) {
            this.assertMetrics("cosmos.client.op.regionsContacted", true, expectedOperationTag, meterRegistry);

            this.assertMetrics(
                "cosmos.client.op.regionsContacted",
                true,
                Tag.of(TagName.RegionName.toString(), this.preferredRegion.toLowerCase(Locale.ROOT)), meterRegistry);
        }

        if (this.getEffectiveMetricCategories().contains(MetricCategory.RequestSummary)) {
            this.assertMetrics(
                "cosmos.client.req.reqPayloadSize",
                true,
                expectedOperationTag,
                meterRegistry);

            this.assertMetrics(
                "cosmos.client.req.rspPayloadSize",
                true,
                expectedOperationTag,
                meterRegistry);
        }

        if (this.client.asyncClient().getConnectionPolicy().getConnectionMode() == ConnectionMode.DIRECT) {
            this.assertMetrics("cosmos.client.req.rntbd.latency", true, expectedRequestTag, meterRegistry);
            this.assertMetrics(
                "cosmos.client.req.rntbd.latency",
                true,
                Tag.of(TagName.RegionName.toString(), this.preferredRegion.toLowerCase(Locale.ROOT)), meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd.backendLatency", true, expectedRequestTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.rntbd.requests", true, expectedRequestTag, meterRegistry);
            Meter reportedRntbdRequestCharge =
                this.assertMetrics("cosmos.client.req.rntbd.RUs", true, expectedRequestTag, meterRegistry);
            validateReasonableRUs(reportedRntbdRequestCharge, minRu, maxRu);

            if (this.getEffectiveMetricCategories().contains(MetricCategory.RequestDetails)) {
                this.assertMetrics("cosmos.client.req.rntbd.timeline", true, expectedRequestTag, meterRegistry);
            }
        } else {
            this.assertMetrics("cosmos.client.req.gw.latency", true, expectedRequestTag, meterRegistry);

            if (this.getEffectiveMetricCategories().contains(MetricCategory.OperationDetails)) {
                this.assertMetrics(
                    "cosmos.client.req.gw.latency",
                    true,
                    Tag.of(TagName.RegionName.toString(), this.preferredRegion.toLowerCase(Locale.ROOT)), meterRegistry);
            }
            this.assertMetrics("cosmos.client.req.gw.backendLatency", false, expectedRequestTag, meterRegistry);
            this.assertMetrics("cosmos.client.req.gw.requests", true, expectedRequestTag, meterRegistry);
            Meter reportedGatewayRequestCharge =
                this.assertMetrics("cosmos.client.req.gw.RUs", true, expectedRequestTag, meterRegistry);
            validateReasonableRUs(reportedGatewayRequestCharge, minRu, maxRu);

            if (this.getEffectiveMetricCategories().contains(MetricCategory.RequestDetails)) {
                this.assertMetrics("cosmos.client.req.gw.timeline", true, expectedRequestTag, meterRegistry);
            }
        }
    }

    private Meter assertMetrics(String prefix, boolean expectedToFind, MeterRegistry meterRegistry) {
        return assertMetrics(prefix, expectedToFind, null, meterRegistry);
    }

    private Meter assertMetrics(String prefix, boolean expectedToFind, Tag withTag, MeterRegistry meterRegistry) {
        assertThat(meterRegistry).isNotNull();
        assertThat(meterRegistry.getMeters()).isNotNull();
        List<Meter> meters = meterRegistry.getMeters().stream().collect(Collectors.toList());

        if (expectedToFind) {
            assertThat(meters.size()).isGreaterThan(0);
            // Makes sure that we stay compatible with prometheus which expects that all the same tags are present for a meter
            assertTagInAllMeters(meters, prefix);
        }

        List<Meter> meterPrefixMatches = meters
            .stream()
            .filter(meter -> meter.getId().getName().startsWith(prefix))
            .collect(Collectors.toList());

        List<Meter> meterMatches = meterPrefixMatches
            .stream()
            .filter(meter -> (withTag == null || meter.getId().getTags().contains(withTag)) &&
                meter.measure().iterator().next().getValue() > 0)
            .collect(Collectors.toList());

        if (expectedToFind) {
            if (meterMatches.size() == 0) {

                String message = String.format(
                    "No meter found for the expected constraints - prefix '%s', withTag '%s'",
                    prefix,
                    withTag);

                logger.error(message);

                logger.info("Meters matching the prefix");
                meterPrefixMatches.forEach(meter ->
                    logger.info("{} has measurements {}", meter.getId(), meter.measure().iterator().hasNext()));


                fail(message);
            }

            if (meterMatches.size() > 1) {
                StringBuilder sb = new StringBuilder();
                final AtomicReference<Meter> exactMatchMeter = new AtomicReference<>(null);
                meterMatches.forEach(m -> {
                    if (exactMatchMeter.get() == null && m.getId().getName().equals(prefix)) {
                        exactMatchMeter.set(m);
                    }

                    String message = String.format(
                        "Found more than one meter '%s' for prefix '%s' withTag '%s' --> '%s'",
                        m.getId(),
                        prefix,
                        withTag,
                        m);
                    sb.append(message);
                    sb.append(System.getProperty("line.separator"));
                    logger.info(message);
                });

                if (exactMatchMeter.get() != null) {
                    logger.info("Found exact match {}", exactMatchMeter);
                    return exactMatchMeter.get();
                }
            }

            return meterMatches.get(0);
        } else {
            if (meterMatches.size() > 0) {
                StringBuilder sb = new StringBuilder();
                meterMatches.forEach(m -> {
                        String message = String.format(
                            "Found unexpected meter '%s' for prefix '%s' withTag '%s' --> '%s'",
                            m,
                            prefix,
                            withTag,
                            m);
                        sb.append(message);
                        sb.append(System.getProperty("line.separator"));
                        logger.error(message);
                    });


                fail(sb.toString());
            }
            assertThat(meterMatches.size()).isEqualTo(0);

            return null;
        }
    }

    private void assertTagInAllMeters(List<Meter> meters, String prefix) {
        List<Meter> meterMatches = meters
            .stream()
            .filter(meter -> meter.getId().getName().equals(prefix))
            .collect(Collectors.toList());
        if (meterMatches.size() >  0) {
            Set<String> possibleTags = new HashSet<>();
            for (Tag tag : meterMatches.get(0).getId().getTags()) {
                possibleTags.add(tag.getKey());
            }

            List<Meter> metersMissingTags = new ArrayList<>();
            List<Meter> metersTagPresent = meterMatches
                .stream()
                .filter(meter -> {
                    int numTags = 0;
                    for (Tag tag : meter.getId().getTags()) {
                        if (!possibleTags.contains(tag.getKey())) {
                            return false;
                        }
                        numTags++;
                    }
                    if (numTags != possibleTags.size()) {
                        metersMissingTags.add(meter);
                    }
                    return numTags == possibleTags.size();
                } )
                .collect(Collectors.toList());

            if (metersMissingTags.size() > 0) {
                System.out.println("There are meters missing tags with prefix " + prefix);
                System.out.println("All possible tags " + possibleTags);
                for (Meter meter : metersMissingTags) {
                    System.out.println("Meters missing tags " + meter.getId().getName() + "------" + meter.getId().getTags());
                }
            }
            assertThat(metersTagPresent.size()).isEqualTo(meterMatches.size());
        }
    }

    private List<String> getAvailableWriteRegionNames(RxDocumentClientImpl rxDocumentClient) {
        try {
            GlobalEndpointManager globalEndpointManager = ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
            LocationCache locationCache = ReflectionUtils.getLocationCache(globalEndpointManager);

            Field locationInfoField = LocationCache.class.getDeclaredField("locationInfo");
            locationInfoField.setAccessible(true);
            Object locationInfo = locationInfoField.get(locationCache);

            Class<?> DatabaseAccountLocationsInfoClass = Class.forName("com.azure.cosmos.implementation.routing" +
                ".LocationCache$DatabaseAccountLocationsInfo");
            Field availableWriteLocations = DatabaseAccountLocationsInfoClass.getDeclaredField(
                "availableWriteLocations");
            availableWriteLocations.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) availableWriteLocations.get(locationInfo);
            return list;

        } catch (Exception error) {
            fail(error.toString());

            return null;
        }
    }
}
