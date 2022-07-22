// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.ConsoleLoggingRegistryFactory;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionEvent;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import com.azure.cosmos.implementation.guava25.net.PercentEscaper;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
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
    private static final
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
            ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
    private static final PercentEscaper PERCENT_ESCAPER = new PercentEscaper("_-/", false);

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
            requestPayloadSizeInBytes = diagnosticsAccessor.getRequestPayloadSizeInBytes(cosmosDiagnostics);
        }

        EnumSet<TagName> metricTagNames = clientAccessor.getMetricTagNames(cosmosAsyncClient);

        Tags operationTags = createOperationTags(
            metricTagNames,
            cosmosAsyncClient,
            statusCode,
            responsePayloadSizeInBytes,
            containerId,
            databaseId,
            operationType,
            resourceType,
            consistencyLevel,
            operationId,
            isPointOperation,
            requestPayloadSizeInBytes
        );

        OperationMetricProducer metricProducer = new OperationMetricProducer(metricTagNames, operationTags);
        metricProducer.recordOperation(
            requestCharge,
            latency,
            maxItemCount,
            actualItemCount,
            cosmosDiagnostics
        );
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

    private static Tags createOperationTags(
        EnumSet<TagName> metricTagNames,
        CosmosAsyncClient cosmosAsyncClient,
        int statusCode,
        Integer responsePayloadSizeInBytes,
        String containerId,
        String databaseId,
        OperationType operationType,
        ResourceType resourceType,
        ConsistencyLevel consistencyLevel,
        String operationId,
        boolean isPointOperation,
        int requestPayloadSizeInBytes
    ) {
        List<Tag> effectiveTags = new ArrayList<>();

        if (metricTagNames.contains(TagName.ClientCorrelationId)) {
            effectiveTags.add(clientAccessor.getClientCorrelationTag(cosmosAsyncClient));
        }

        if (metricTagNames.contains(TagName.Container)) {
            String containerTagValue = String.format(
                "%s/%s/%s",
                escape(clientAccessor.getAccountTagValue(cosmosAsyncClient)),
                databaseId != null ? escape(databaseId) : "NONE",
                containerId != null ? escape(containerId) : "NONE"
            );

            effectiveTags.add(Tag.of(TagName.Container.toString(), containerTagValue));
        }

        if (metricTagNames.contains(TagName.Operation)) {
            String operationTagValue = !isPointOperation && Strings.isNullOrWhiteSpace(operationId)
                ? String.format("%s/%s", resourceType.toString(), operationType.toString())
                : String.format("%s/%s/%s", resourceType.toString(), operationType.toString(), escape(operationId));

            effectiveTags.add(Tag.of(TagName.Operation.toString(), operationTagValue));
        }

        if (metricTagNames.contains(TagName.OperationStatusCode)) {
            effectiveTags.add(Tag.of(TagName.OperationStatusCode.toString(), String.valueOf(statusCode)));
        }

        if (isPointOperation &&
            metricTagNames.contains(TagName.IsPayloadLargerThan1KB)) {

            effectiveTags.add(Tag.of(
                TagName.IsPayloadLargerThan1KB.toString(),
                String.valueOf(
                    Math.max(
                        requestPayloadSizeInBytes,
                        responsePayloadSizeInBytes
                    ) > ClientTelemetry.ONE_KB_TO_BYTES)
            ));
        }

        if (metricTagNames.contains(TagName.ConsistencyLevel)) {
            effectiveTags.add(Tag.of(
                TagName.ConsistencyLevel.toString(),
                consistencyLevel == null ?
                    BridgeInternal.getContextClient(cosmosAsyncClient).getConsistencyLevel().toString() :
                    consistencyLevel.toString()
            ));
        }

        return Tags.of(effectiveTags);
    }

    private static class OperationMetricProducer {
        private final EnumSet<TagName> metricTagNames;
        private final Tags operationTags;

        public OperationMetricProducer(EnumSet<TagName> metricTagNames, Tags operationTags) {
            this.metricTagNames = metricTagNames;
            this.operationTags = operationTags;
        }

        private void recordRequestPayloadSizes(
            int requestPayloadSizeInBytes,
            int responsePayloadSizeInBytes
        ) {
            DistributionSummary requestPayloadSizeMeter = DistributionSummary
                .builder(nameOf("request.requestPayloadSize"))
                .baseUnit("bytes")
                .description("Request payload size in bytes")
                .maximumExpectedValue(16d * 1024)
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(operationTags)
                .register(compositeRegistry);
            requestPayloadSizeMeter.record(requestPayloadSizeInBytes);

            DistributionSummary responsePayloadSizeMeter = DistributionSummary
                .builder(nameOf("request.responsePayloadSize"))
                .baseUnit("bytes")
                .description("Response payload size in bytes")
                .maximumExpectedValue(16d * 1024)
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(operationTags)
                .register(compositeRegistry);
            responsePayloadSizeMeter.record(responsePayloadSizeInBytes);
        }

        private void recordItemCounts(
            int maxItemCount,
            int actualItemCount
        ) {
            DistributionSummary maxItemCountMeter = DistributionSummary
                .builder(nameOf("operation.maxItemCount"))
                .baseUnit("item count")
                .description("Request max. item count")
                .maximumExpectedValue(1_000_000d)
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(operationTags)
                .register(compositeRegistry);
            maxItemCountMeter.record(Math.max(0, Math.min(maxItemCount, 1_000_000d)));

            DistributionSummary actualItemCountMeter = DistributionSummary
                .builder(nameOf("operation.actualItemCount"))
                .baseUnit("item count")
                .description("Response actual item count")
                .maximumExpectedValue(1_000_000d)
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(operationTags)
                .register(compositeRegistry);
            actualItemCountMeter.record(Math.max(0, Math.min(actualItemCount, 1_000_000d)));
        }

        private Tags createRequestTags(
            EnumSet<TagName> metricTagNames,
            String pkRangeId,
            int statusCode,
            int subStatusCode,
            ResourceType resourceType,
            OperationType operationType,
            String regionName,
            String serviceEndpoint,
            String serviceAddress
        ) {
            List<Tag> effectiveTags = new ArrayList<>();
            if (metricTagNames.contains(TagName.PartitionKeyRangeId)) {
                effectiveTags.add(Tag.of(
                    TagName.PartitionKeyRangeId.toString(),
                    pkRangeId != null ? escape(pkRangeId) : "NONE"));
            }

            if (metricTagNames.contains(TagName.RequestStatusCode)) {
                effectiveTags.add(Tag.of(
                    TagName.RequestStatusCode.toString(),
                    String.format("%d_%d", statusCode, subStatusCode)));
            }

            if (metricTagNames.contains(TagName.RequestOperationType)) {
                effectiveTags.add(Tag.of(
                    TagName.RequestOperationType.toString(),
                    String.format("%s_%s", resourceType.toString(), operationType.toString())));
            }

            if (metricTagNames.contains(TagName.RegionName)) {
                effectiveTags.add(Tag.of(
                    TagName.RegionName.toString(),
                    regionName != null ? escape(regionName) : "NONE"));
            }

            if (metricTagNames.contains(TagName.ServiceEndpoint)) {
                effectiveTags.add(Tag.of(
                    TagName.ServiceEndpoint.toString(),
                    serviceEndpoint != null ? escape(serviceEndpoint) : "NONE"));
            }

            if (metricTagNames.contains(TagName.ServiceAddress)) {
                effectiveTags.add(Tag.of(
                    TagName.ServiceAddress.toString(),
                    serviceAddress != null ? escape(serviceAddress) : "NONE"));
            }

            return Tags.of(effectiveTags);
        }

        private void recordRntbdChannelStatistics(
            int pendingRequestQueueSize,
            int channelTaskQueueSize,
            Tags requestTags) {

            DistributionSummary pendingRequestQueueSizeMeter = DistributionSummary
                .builder(nameOf("request.statistics.channel.pendingRequestQueueSize"))
                .baseUnit("#")
                .description("Channel statistics(Pending request queue size)")
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(requestTags)
                .register(compositeRegistry);
            pendingRequestQueueSizeMeter.record(pendingRequestQueueSize);

            DistributionSummary channelTaskQueueSizeMeter = DistributionSummary
                .builder(nameOf("request.statistics.channel.channelTaskQueueSize"))
                .baseUnit("#")
                .description("Channel statistics(Channel task queue size)")
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(requestTags)
                .register(compositeRegistry);
            channelTaskQueueSizeMeter.record(channelTaskQueueSize);
        }

        private void recordRntbdEndpointStatistics(RntbdEndpointStatistics endpointStatistics, Tags requestTags) {
            if (endpointStatistics == null) {
                return;
            }

            DistributionSummary acquiredChannelsMeter = DistributionSummary
                .builder(nameOf("request.statistics.endpoint.acquiredChannels"))
                .baseUnit("#")
                .description("Endpoint statistics(acquired channels)")
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(requestTags)
                .register(compositeRegistry);
            acquiredChannelsMeter.record(endpointStatistics.getAcquiredChannels());

            DistributionSummary availableChannelsMeter = DistributionSummary
                .builder(nameOf("request.statistics.endpoint.availableChannels"))
                .baseUnit("#")
                .description("Endpoint statistics(available channels)")
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(requestTags)
                .register(compositeRegistry);
            availableChannelsMeter.record(endpointStatistics.getAvailableChannels());

            DistributionSummary inflightRequestsMeter = DistributionSummary
                .builder(nameOf("request.statistics.endpoint.inflightRequests"))
                .baseUnit("#")
                .description("Endpoint statistics(inflight requests)")
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(requestTags)
                .register(compositeRegistry);
            inflightRequestsMeter.record(endpointStatistics.getInflightRequests());

            DistributionSummary executorTaskQueueSizeMeter = DistributionSummary
                .builder(nameOf("request.statistics.endpoint.executorTaskQueueSize"))
                .baseUnit("#")
                .description("Endpoint statistics(executor task queue size)")
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(requestTags)
                .register(compositeRegistry);
            executorTaskQueueSizeMeter.record(endpointStatistics.getExecutorTaskQueueSize());
        }

        private void recordRntbdChannelAcquisitionTimeline(
            RntbdChannelAcquisitionTimeline acquisitionTimeline,
            Tags requestTags) {

            if (acquisitionTimeline == null) {
                return;
            }

            for (RntbdChannelAcquisitionEvent acquisitionEvent :  acquisitionTimeline.getEvents()) {
                Duration duration = acquisitionEvent.getDuration();
                if (duration == null) {
                    continue;
                }

                String name = nameOf(
                    "request.channel.acquisition.timeline." +
                        escape(acquisitionEvent.getEventType().toString()));
                Timer acquisitionEventMeter = Timer
                    .builder(name)
                    .description(String.format("Channel acquisition timeline (%s)", name))
                    .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .tags(requestTags)
                    .register(compositeRegistry);
                acquisitionEventMeter.record(duration);
            }
        }

        private void recordRequestTimeline(RequestTimeline requestTimeline, Tags requestTags) {
            if (requestTimeline == null) {
                return;
            }

            for (RequestTimeline.Event event : requestTimeline) {
                Duration duration = event.getDuration();
                if (duration == null || duration == Duration.ZERO) {
                    continue;
                }

                Timer eventMeter = Timer
                    .builder(nameOf("request.timeline." + escape(event.getName())))
                    .description(String.format("Request timeline (%s)", event.getName()))
                    .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .tags(requestTags)
                    .register(compositeRegistry);
                eventMeter.record(duration);
            }
        }

        public void recordOperation(
            float requestCharge,
            Duration latency,
            int maxItemCount,
            int actualItemCount,
            CosmosDiagnostics diagnostics) {

            DistributionSummary requestChargeMeter = DistributionSummary
                .builder(nameOf("operation.requestCharge"))
                .baseUnit("RU (request unit)")
                .description("RU charge")
                .maximumExpectedValue(10_000_000d)
                .minimumExpectedValue(0d)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(operationTags)
                .register(compositeRegistry);
            requestChargeMeter.record(Math.max(requestCharge, 10_000_000d));

            Timer latencyMeter = Timer
                .builder(nameOf("operation.latency"))
                .description("Operation latency")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .publishPercentileHistogram()
                .tags(operationTags)
                .register(compositeRegistry);
            latencyMeter.record(latency);

            this.recordItemCounts(maxItemCount, actualItemCount);

            List<ClientSideRequestStatistics> clientSideRequestStatistics =
                diagnosticsAccessor.getClientSideRequestStatistics(diagnostics);

            if (clientSideRequestStatistics != null) {
                for (ClientSideRequestStatistics requestStatistics : clientSideRequestStatistics) {

                    for (ClientSideRequestStatistics.StoreResponseStatistics responseStatistics:
                        requestStatistics.getResponseStatisticsList()) {

                        StoreResultDiagnostics storeResultDiagnostics = responseStatistics.getStoreResult();
                        StoreResponseDiagnostics storeResponseDiagnostics =
                            storeResultDiagnostics.getStoreResponseDiagnostics();

                        Tags requestTags = operationTags.and(
                            createRequestTags(
                                metricTagNames,
                                storeResponseDiagnostics.getPartitionKeyRangeId(),
                                storeResponseDiagnostics.getStatusCode(),
                                storeResponseDiagnostics.getStatusCode(),
                                responseStatistics.getRequestResourceType(),
                                responseStatistics.getRequestOperationType(),
                                responseStatistics.getRegionName(),
                                storeResultDiagnostics.getStorePhysicalAddressEscapedAuthority(),
                                storeResultDiagnostics.getStorePhysicalAddressEscapedPath())
                        );

                        Timer backendRequestLatencyMeter = Timer
                            .builder(nameOf("request.backend.latency"))
                            .description("Backend Request latency")
                            .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                            .publishPercentileHistogram()
                            .tags(requestTags)
                            .register(compositeRegistry);
                        backendRequestLatencyMeter.record(latency);

                        recordRequestTimeline(storeResponseDiagnostics.getRequestTimeline(), requestTags);

                        recordRntbdChannelAcquisitionTimeline(
                            storeResponseDiagnostics.getChannelAcquisitionTimeline(),
                            requestTags);

                        recordRequestPayloadSizes(
                            storeResponseDiagnostics.getRequestPayloadLength(),
                            storeResponseDiagnostics.getResponsePayloadLength()
                        );

                        recordRntbdChannelStatistics(
                            storeResponseDiagnostics.getPendingRequestQueueSize(),
                            storeResponseDiagnostics.getRntbdChannelTaskQueueSize(),
                            requestTags
                        );

                        recordRntbdEndpointStatistics(
                            storeResponseDiagnostics.getRntbdEndpointStatistics(),
                            requestTags);
                    }
                }
            }
        }
    }
}
