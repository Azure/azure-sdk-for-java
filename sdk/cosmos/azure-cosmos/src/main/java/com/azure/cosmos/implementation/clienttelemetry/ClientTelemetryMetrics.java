// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.guava25.net.PercentEscaper;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class ClientTelemetryMetrics {
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();
    private static final PercentEscaper PERCENT_ESCAPER = new PercentEscaper("_-", false);

    private static final Logger logger = LoggerFactory.getLogger(ClientTelemetryMetrics.class);
    private static final CompositeMeterRegistry compositeRegistry = new CompositeMeterRegistry();
    private static final AtomicLong staticCtorCallCount = new AtomicLong(0);

    static {
        if (staticCtorCallCount.incrementAndGet() == 1) {
            try {
                int step = Integer.getInteger("azure.cosmos.clientTelemetry.consoleLogging.step", 0);
                if (step > 0) {
                    ClientTelemetryMetrics.add(ConsoleLoggingRegistryFactory.create(step));
                }
            } catch (Throwable throwable) {
                logger.error("failed to initialize console logging registry due to ", throwable);
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                }
            }
        }
    }

    public static void recordSystemUsage(
        float averageSystemCpuUsage,
        float freeMemoryAvailableInMB
    ) {
        if (compositeRegistry.getRegistries().isEmpty()) {
            return;
        }

        DistributionSummary averageSystemCpuUsageMeter = DistributionSummary
            .builder(nameOf("system.avgCpuLoad"))
            .baseUnit("%")
            .description("Avg. System CPU load")
            .maximumExpectedValue(100d)
            .minimumExpectedValue(0d)
            .publishPercentileHistogram()
            .register(compositeRegistry);
        averageSystemCpuUsageMeter.record(averageSystemCpuUsage);

        DistributionSummary freeMemoryAvailableInMBMeter = DistributionSummary
            .builder(nameOf("system.freeMemoryAvailable"))
            .baseUnit("MB")
            .description("Free memory available")
            .publishPercentileHistogram()
            .register(compositeRegistry);
        freeMemoryAvailableInMBMeter.record(freeMemoryAvailableInMB);
    }

    public static void recordOperation(
        CosmosAsyncClient cosmosAsyncClient,
        CosmosDiagnostics cosmosDiagnostics,
        int statusCode,
        Integer responsePayloadSizeInBytes,
        Integer maxItemCount,
        Integer actualItemCount,
        String containerId,
        String databaseId,
        OperationType operationType,
        ResourceType resourceType,
        ConsistencyLevel consistencyLevel,
        String operationId,
        float requestCharge,
        Duration latency
    ) {
        if (compositeRegistry.getRegistries().isEmpty() ||
            !clientAccessor.isClientTelemetryMetricsEnabled(cosmosAsyncClient)) {
            return;
        }

        boolean isPointOperation = maxItemCount < 0;

        int requestPayloadSizeInBytes = 0;
        if (cosmosDiagnostics != null) {
            requestPayloadSizeInBytes = cosmosDiagnostics.getRequestPayloadSizeInBytes();
        }

        EnumSet<TagName> metricTagNames = clientAccessor.getMetricTagNames(cosmosAsyncClient);
        EnumSet<TagName> effectiveTagNames = EnumSet.noneOf(TagName.class);
        List<Tag> effectiveTags = new ArrayList<>();

        if (metricTagNames.contains(TagName.ClientCorrelationId)) {
            effectiveTags.add(clientAccessor.getClientCorrelationTag(cosmosAsyncClient));
            effectiveTagNames.add(TagName.ClientCorrelationId);
        }

        if (metricTagNames.contains(TagName.Account)) {
            effectiveTags.add(clientAccessor.getAccountTag(cosmosAsyncClient));
            effectiveTagNames.add(TagName.Account);
        }

        if (metricTagNames.contains(TagName.Database)) {
            effectiveTags.add(Tag.of(TagName.Database.toString(), escape(databaseId)));
            effectiveTagNames.add(TagName.Database);
        }

        if (metricTagNames.contains(TagName.Container)) {
            effectiveTags.add(Tag.of(TagName.Container.toString(), escape(containerId)));
            effectiveTagNames.add(TagName.Container);
        }

        if (metricTagNames.contains(TagName.ResourceType)) {
            effectiveTags.add(Tag.of(TagName.ResourceType.toString(), resourceType.toString()));
            effectiveTagNames.add(TagName.ResourceType);
        }

        if (metricTagNames.contains(TagName.OperationType)) {
            effectiveTags.add(Tag.of(TagName.OperationType.toString(), operationType.toString()));
            effectiveTagNames.add(TagName.OperationType);
        }

        if (metricTagNames.contains(TagName.StatusCode)) {
            effectiveTags.add(Tag.of(TagName.StatusCode.toString(), String.valueOf(statusCode)));
            effectiveTagNames.add(TagName.StatusCode);
        }

        if (!isPointOperation &&
            metricTagNames.contains(TagName.OperationId) &&
            !Strings.isNullOrWhiteSpace(operationId)) {

            effectiveTags.add(Tag.of(TagName.OperationId.toString(),operationId));
            effectiveTagNames.add(TagName.OperationId);
        }

        if (metricTagNames.contains(TagName.RegionsContacted)) {
            effectiveTags.add(
                Tag.of(
                    TagName.RegionsContacted.toString(),
                    escape(BridgeInternal.getRegionsContacted(cosmosDiagnostics).toString())
                )
            );
            effectiveTagNames.add(TagName.RegionsContacted);
        }

        if (isPointOperation &&
            metricTagNames.contains(TagName.IsPayloadLargerThan1KB)) {

            effectiveTags.add(
                Tag.of(
                    TagName.IsPayloadLargerThan1KB.toString(),
                    String.valueOf(
                        Math.max(
                            requestPayloadSizeInBytes,
                            responsePayloadSizeInBytes
                        ) > ClientTelemetry.ONE_KB_TO_BYTES)
                )
            );
            effectiveTagNames.add(TagName.IsPayloadLargerThan1KB);
        }

        if (metricTagNames.contains(TagName.ConsistencyLevel)) {
            effectiveTags.add(
                Tag.of(
                    TagName.ConsistencyLevel.toString(),
                    consistencyLevel == null ?
                        BridgeInternal.getContextClient(cosmosAsyncClient).getConsistencyLevel().toString() :
                        consistencyLevel.toString()
                )
            );
            effectiveTagNames.add(TagName.ConsistencyLevel);
        }

        DistributionSummary requestPayloadSizeMeter = DistributionSummary
            .builder(nameOf("request.payload"))
            .baseUnit("bytes")
            .description("Request payload size in bytes")
            .maximumExpectedValue(16d * 1024)
            .minimumExpectedValue(0d)
            .publishPercentiles(0.5, 0.9, 0.95, 0.99)
            .publishPercentileHistogram()
            .tags(effectiveTags)
            .register(compositeRegistry);
        requestPayloadSizeMeter.record(requestPayloadSizeInBytes);

        DistributionSummary responsePayloadSizeMeter = DistributionSummary
            .builder(nameOf("response.payload"))
            .baseUnit("bytes")
            .description("Response payload size in bytes")
            .maximumExpectedValue(16d * 1024)
            .minimumExpectedValue(0d)
            .publishPercentiles(0.5, 0.9, 0.95, 0.99)
            .publishPercentileHistogram()
            .tags(effectiveTags)
            .register(compositeRegistry);
        responsePayloadSizeMeter.record(responsePayloadSizeInBytes);

        if (!isPointOperation) {
            DistributionSummary maxItemCountMeter = DistributionSummary
                .builder(nameOf("request.maxItemCount"))
                .baseUnit("item count")
                .description("Request max. item count")
                .maximumExpectedValue(1_000_000d)
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(effectiveTags)
                .register(compositeRegistry);
            maxItemCountMeter.record(Math.max(maxItemCount, 1_000_000d));
        }

        if (actualItemCount >= 0) {
            DistributionSummary actualItemCountMeter = DistributionSummary
                .builder(nameOf("response.actualItemCount"))
                .baseUnit("item count")
                .description("Response actual item count")
                .maximumExpectedValue(1_000_000d)
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(effectiveTags)
                .register(compositeRegistry);
            actualItemCountMeter.record(Math.max(actualItemCount, 1_000_000d));
        }

        DistributionSummary requestChargeMeter = DistributionSummary
            .builder(nameOf("response.charge"))
            .baseUnit("RU (request unit)")
            .description("RU charge")
            .maximumExpectedValue(10_000_000d)
            .minimumExpectedValue(0d)
            .publishPercentiles(0.5, 0.9, 0.95, 0.99)
            .publishPercentileHistogram()
            .tags(effectiveTags)
            .register(compositeRegistry);
        requestChargeMeter.record(Math.max(requestCharge, 10_000_000d));

        Timer latencyMeter = Timer
            .builder(nameOf("latency"))
            .description("Latency")
            .publishPercentiles(0.5, 0.9, 0.95, 0.99)
            .publishPercentileHistogram()
            .tags(effectiveTags)
            .register(compositeRegistry);
        latencyMeter.record(latency);
    }

    public static void add(MeterRegistry registry) {
        ClientTelemetryMetrics.compositeRegistry.add(registry);
    }

    public static String escape(String value) {
        return PERCENT_ESCAPER.escape(value);
    }

    private static String nameOf(final String member) {
        return "com.azure.cosmos.telemetry.client." + member;
    }
}
