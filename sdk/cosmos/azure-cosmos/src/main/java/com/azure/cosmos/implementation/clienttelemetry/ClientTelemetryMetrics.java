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
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.StoreResultDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionEvent;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdMetricsCompletionRecorder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.guava25.net.PercentEscaper;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
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
import java.util.Map;
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
            .publishPercentiles(0.95, 0.99)
            .publishPercentileHistogram(true)
            .register(compositeRegistry);
        averageSystemCpuUsageMeter.record(averageSystemCpuUsage);

        DistributionSummary freeMemoryAvailableInMBMeter = DistributionSummary
            .builder(nameOf("system.freeMemoryAvailable"))
            .baseUnit("MB")
            .description("Free memory available")
            .publishPercentiles()
            .publishPercentileHistogram(false)
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

        boolean isPointOperation = maxItemCount == null || maxItemCount < 0;

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
            maxItemCount == null ? -1 : maxItemCount,
            actualItemCount,
            cosmosDiagnostics
        );
    }

    public static RntbdMetricsCompletionRecorder createRntbdMetrics(
        RntbdTransportClient client,
        RntbdEndpoint endpoint) {

        return new RntbdMetricsV2(compositeRegistry, client, endpoint);
    }

    public static void add(MeterRegistry registry) {
        ClientTelemetryMetrics.compositeRegistry.add(registry);
    }

    public static String escape(String value) {
        return PERCENT_ESCAPER.escape(value);
    }

    private static String nameOf(final String member) {
        return "cosmos.client." + member;
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

        public void recordOperation(
            float requestCharge,
            Duration latency,
            int maxItemCount,
            int actualItemCount,
            CosmosDiagnostics diagnostics) {

            DistributionSummary requestChargeMeter = DistributionSummary
                .builder(nameOf("op.RUs"))
                .baseUnit("RU (request unit)")
                .description("Operation RU charge")
                .maximumExpectedValue(10_000_000d)
                .publishPercentiles()
                .publishPercentileHistogram(false)
                .tags(operationTags)
                .register(compositeRegistry);
            requestChargeMeter.record(Math.max(requestCharge, 10_000_000d));

            Timer latencyMeter = Timer
                .builder(nameOf("op.latency"))
                .description("Operation latency")
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(operationTags)
                .register(compositeRegistry);
            latencyMeter.record(latency);

            this.recordItemCounts(maxItemCount, actualItemCount);

            List<ClientSideRequestStatistics> clientSideRequestStatistics =
                diagnosticsAccessor.getClientSideRequestStatistics(diagnostics);

            if (clientSideRequestStatistics != null) {
                for (ClientSideRequestStatistics requestStatistics : clientSideRequestStatistics) {

                    recordStoreResponseStatistics(requestStatistics.getResponseStatisticsList());
                    recordStoreResponseStatistics(requestStatistics.getSupplementalResponseStatisticsList());
                    recordGatewayStatistics(requestStatistics.getGatewayStatistics());
                    recordAddressResolutionStatistics(requestStatistics.getAddressResolutionStatistics());
                }
            }
        }

        private void recordRequestPayloadSizes(
            int requestPayloadSizeInBytes,
            int responsePayloadSizeInBytes
        ) {
            DistributionSummary requestPayloadSizeMeter = DistributionSummary
                .builder(nameOf("req.reqPayloadSize"))
                .baseUnit("bytes")
                .description("Request payload size in bytes")
                .maximumExpectedValue(16d * 1024)
                .publishPercentiles()
                .publishPercentileHistogram(false)
                .tags(operationTags)
                .register(compositeRegistry);
            requestPayloadSizeMeter.record(requestPayloadSizeInBytes);

            DistributionSummary responsePayloadSizeMeter = DistributionSummary
                .builder(nameOf("req.rspPayloadSize"))
                .baseUnit("bytes")
                .description("Response payload size in bytes")
                .maximumExpectedValue(16d * 1024)
                .publishPercentiles()
                .publishPercentileHistogram(false)
                .tags(operationTags)
                .register(compositeRegistry);
            responsePayloadSizeMeter.record(responsePayloadSizeInBytes);
        }

        private void recordItemCounts(
            int maxItemCount,
            int actualItemCount
        ) {
            if (maxItemCount > 0) {
                DistributionSummary maxItemCountMeter = DistributionSummary
                    .builder(nameOf("op.maxItemCount"))
                    .baseUnit("item count")
                    .description("Request max. item count")
                    .maximumExpectedValue(1_000_000d)
                    .publishPercentiles()
                    .publishPercentileHistogram(false)
                    .tags(operationTags)
                    .register(compositeRegistry);
                maxItemCountMeter.record(Math.max(0, Math.min(maxItemCount, 1_000_000d)));

                DistributionSummary actualItemCountMeter = DistributionSummary
                    .builder(nameOf("op.actualItemCount"))
                    .baseUnit("item count")
                    .description("Response actual item count")
                    .maximumExpectedValue(1_000_000d)
                    .publishPercentiles()
                    .publishPercentileHistogram(false)
                    .tags(operationTags)
                    .register(compositeRegistry);
                actualItemCountMeter.record(Math.max(0, Math.min(actualItemCount, 1_000_000d)));
            }
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
                    Strings.isNullOrWhiteSpace(pkRangeId) ? "NONE" : escape(pkRangeId)));
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

        private Tags createAddressResolutionTags(
            EnumSet<TagName> metricTagNames,
            String serviceEndpoint,
            boolean isForceRefresh,
            boolean isForceCollectionRoutingMapRefresh
        ) {
            List<Tag> effectiveTags = new ArrayList<>();
            if (metricTagNames.contains(TagName.ServiceEndpoint)) {
                effectiveTags.add(Tag.of(
                    TagName.ServiceEndpoint.toString(),
                    serviceEndpoint != null ? escape(serviceEndpoint) : "NONE"));
            }

            if (metricTagNames.contains(TagName.IsForceRefresh)) {
                effectiveTags.add(Tag.of(
                    TagName.IsForceRefresh.toString(),
                    isForceRefresh ? "True" : "False"));
            }

            if (metricTagNames.contains(TagName.IsForceCollectionRoutingMapRefresh)) {
                effectiveTags.add(Tag.of(
                    TagName.IsForceCollectionRoutingMapRefresh.toString(),
                    isForceCollectionRoutingMapRefresh ? "True" : "False"));
            }

            return Tags.of(effectiveTags);
        }

        private void recordRntbdChannelStatistics(
            int pendingRequestQueueSize,
            int channelTaskQueueSize,
            Tags requestTags) {

            DistributionSummary pendingRequestQueueSizeMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.channel.pendingRequestQueueSize"))
                .baseUnit("#")
                .description("Channel statistics(Pending request queue size)")
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(requestTags)
                .register(compositeRegistry);
            pendingRequestQueueSizeMeter.record(pendingRequestQueueSize);

            DistributionSummary channelTaskQueueSizeMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.channel.channelTaskQueueSize"))
                .baseUnit("#")
                .description("Channel statistics(Channel task queue size)")
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(requestTags)
                .register(compositeRegistry);
            channelTaskQueueSizeMeter.record(channelTaskQueueSize);
        }

        private void recordRntbdEndpointStatistics(RntbdEndpointStatistics endpointStatistics, Tags requestTags) {
            if (endpointStatistics == null) {
                return;
            }

            DistributionSummary acquiredChannelsMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.endpoint.acquiredChannels"))
                .baseUnit("#")
                .description("Endpoint statistics(acquired channels)")
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(requestTags)
                .register(compositeRegistry);
            acquiredChannelsMeter.record(endpointStatistics.getAcquiredChannels());

            DistributionSummary availableChannelsMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.endpoint.availableChannels"))
                .baseUnit("#")
                .description("Endpoint statistics(available channels)")
                .publishPercentiles()
                .publishPercentileHistogram(false)
                .tags(requestTags)
                .register(compositeRegistry);
            availableChannelsMeter.record(endpointStatistics.getAvailableChannels());

            DistributionSummary inflightRequestsMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.endpoint.inflightRequests"))
                .baseUnit("#")
                .description("Endpoint statistics(inflight requests)")
                .tags(requestTags)
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .register(compositeRegistry);
            inflightRequestsMeter.record(endpointStatistics.getInflightRequests());

            DistributionSummary executorTaskQueueSizeMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.endpoint.executorTaskQueueSize"))
                .baseUnit("#")
                .description("Endpoint statistics(executor task queue size)")
                .publishPercentiles()
                .publishPercentileHistogram(false)
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

            for (RntbdChannelAcquisitionEvent acquisitionEvent : acquisitionTimeline.getEvents()) {
                Duration duration = acquisitionEvent.getDuration();
                if (duration == null) {
                    continue;
                }

                String name = nameOf(
                    "req.rntbd.channel.acquisition.timeline." +
                        escape(acquisitionEvent.getEventType().toString()));
                Timer acquisitionEventMeter = Timer
                    .builder(name)
                    .description(String.format("Channel acquisition timeline (%s)", name))
                    .publishPercentiles()
                    .publishPercentileHistogram(false)
                    .tags(requestTags)
                    .register(compositeRegistry);
                acquisitionEventMeter.record(duration);
            }
        }

        private void recordRequestTimeline(String prefix, RequestTimeline requestTimeline, Tags requestTags) {
            if (requestTimeline == null) {
                return;
            }

            for (RequestTimeline.Event event : requestTimeline) {
                Duration duration = event.getDuration();
                if (duration == null || duration == Duration.ZERO) {
                    continue;
                }

                Timer eventMeter = Timer
                    .builder(nameOf(prefix + escape(event.getName())))
                    .description(String.format("Request timeline (%s)", event.getName()))
                    .publishPercentiles(0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .tags(requestTags)
                    .register(compositeRegistry);
                eventMeter.record(duration);
            }
        }

        private void recordStoreResponseStatistics(
            List<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseStatistics) {

            for (ClientSideRequestStatistics.StoreResponseStatistics responseStatistics: storeResponseStatistics) {
                StoreResultDiagnostics storeResultDiagnostics = responseStatistics.getStoreResult();
                StoreResponseDiagnostics storeResponseDiagnostics =
                    storeResultDiagnostics.getStoreResponseDiagnostics();

                Tags requestTags = operationTags.and(
                    createRequestTags(
                        metricTagNames,
                        storeResponseDiagnostics.getPartitionKeyRangeId(),
                        storeResponseDiagnostics.getStatusCode(),
                        storeResponseDiagnostics.getSubStatusCode(),
                        responseStatistics.getRequestResourceType(),
                        responseStatistics.getRequestOperationType(),
                        responseStatistics.getRegionName(),
                        storeResultDiagnostics.getStorePhysicalAddressEscapedAuthority(),
                        storeResultDiagnostics.getStorePhysicalAddressEscapedPath())
                );

                DistributionSummary backendRequestLatencyMeter = DistributionSummary
                    .builder(nameOf("req.rntbd.BackendLatency"))
                    .baseUnit("ms")
                    .description("Backend Request latency")
                    .publishPercentiles(0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .tags(requestTags)
                    .register(compositeRegistry);
                backendRequestLatencyMeter.record(storeResultDiagnostics.getBackendLatencyInMs());

                recordRequestTimeline(
                    "req.rntbd.timeline.",
                    storeResponseDiagnostics.getRequestTimeline(), requestTags);

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

        private void recordGatewayStatistics(
            ClientSideRequestStatistics.GatewayStatistics gatewayStatistics) {

            if (gatewayStatistics == null) {
                return;
            }

            EnumSet<TagName> metricTagNamesForGateway = metricTagNames.clone();
            metricTagNamesForGateway.remove(TagName.RegionName);
            metricTagNamesForGateway.remove(TagName.ServiceAddress);
            metricTagNamesForGateway.remove(TagName.ServiceEndpoint);

            Tags requestTags = operationTags.and(
                createRequestTags(
                    metricTagNamesForGateway,
                    gatewayStatistics.getPartitionKeyRangeId(),
                    gatewayStatistics.getStatusCode(),
                    gatewayStatistics.getSubStatusCode(),
                    gatewayStatistics.getResourceType(),
                    gatewayStatistics.getOperationType(),
                    null,
                    null,
                    null)
            );

           recordRequestTimeline("req.gw.timeline.", gatewayStatistics.getRequestTimeline(), requestTags);
        }

        private void recordAddressResolutionStatistics(
            Map<String, ClientSideRequestStatistics.AddressResolutionStatistics> addressResolutionStatisticsMap) {

            if (addressResolutionStatisticsMap == null || addressResolutionStatisticsMap.size() == 0) {
                return;
            }

            for (ClientSideRequestStatistics.AddressResolutionStatistics addressResolutionStatistics
                : addressResolutionStatisticsMap.values()) {

                if (addressResolutionStatistics.isInflightRequest() ||
                    addressResolutionStatistics.getEndTimeUTC() == null) {

                    // skipping inflight or failed address resolution statistics
                    // capturing error count etc. won't make sense here - request diagnostic
                    // logs are the right way to debug those - not metrics
                    continue;
                }

                Tags addressResolutionTags = operationTags.and(
                    createAddressResolutionTags(
                        metricTagNames,
                        addressResolutionStatistics.getTargetEndpoint(),
                        addressResolutionStatistics.isForceRefresh(),
                        addressResolutionStatistics.isForceCollectionRoutingMapRefresh()
                    )
                );

                Duration latency = Duration.between(
                    addressResolutionStatistics.getStartTimeUTC(),
                    addressResolutionStatistics.getEndTimeUTC());

                Timer addressResolutionLatencyMeter = Timer
                    .builder(nameOf("req.addressResolution.latency"))
                    .description("Address resolution latency")
                    .publishPercentiles(0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .tags(addressResolutionTags)
                    .register(compositeRegistry);
                addressResolutionLatencyMeter.record(latency);
            }
        }
    }

    private static class RntbdMetricsV2 implements RntbdMetricsCompletionRecorder {
        private final DistributionSummary requestSize;
        private final Timer requests;
        private final Timer responseErrors;
        private final DistributionSummary responseSize;
        private final Timer responseSuccesses;

        private RntbdMetricsV2(MeterRegistry registry, RntbdTransportClient client, RntbdEndpoint endpoint) {
            Tags tags = Tags.of(endpoint.clientMetricTag());

            this.requests = Timer
                .builder(nameOf("rntbd.requests.latency"))
                .description("RNTBD request latency")
                .tags(tags)
                .publishPercentileHistogram(true)
                .publishPercentiles(0.95, 0.99)
                .register(registry);

            this.responseErrors = Timer
                .builder(nameOf("rntbd.requests.failed.latency"))
                .description("RNTBD failed request latency")
                .tags(tags)
                .publishPercentileHistogram(true)
                .publishPercentiles(0.95, 0.99)
                .register(registry);

            this.responseSuccesses = Timer
                .builder(nameOf("rntbd.requests.successful.latency"))
                .description("RNTBD successful request latency")
                .tags(tags)
                .register(registry);

            Gauge.builder(nameOf("rntbd.endpoints.count"), client, RntbdTransportClient::endpointCount)
                 .description("RNTBD endpoint count")
                 .register(registry);

            Gauge.builder(nameOf("rntbd.endpoints.evicted"), client, RntbdTransportClient::endpointEvictionCount)
                 .description("RNTBD endpoint eviction count")
                 .register(registry);

            Gauge.builder(nameOf("rntbd.requests.concurrent.count"), endpoint, RntbdEndpoint::concurrentRequests)
                 .description("RNTBD concurrent requests (executing or queued request count)")
                 .tags(tags)
                 .register(registry);

            Gauge.builder(nameOf("rntbd.requests.queued.count"), endpoint, RntbdEndpoint::requestQueueLength)
                 .description("RNTBD queued request count")
                 .tags(tags)
                 .register(registry);

            Gauge.builder(nameOf("rntbd.channels.acquired.count"), endpoint, RntbdEndpoint::channelsAcquiredMetric)
                 .description("RNTBD acquired channel count")
                 .tags(tags)
                 .register(registry);

            Gauge.builder(nameOf("rntbd.channels.available.count"), endpoint, RntbdEndpoint::channelsAvailableMetric)
                 .description("RNTBD available channel count")
                 .tags(tags)
                 .register(registry);

            this.requestSize = DistributionSummary.builder(nameOf("rntbd.req.reqSize"))
                                                  .description("RNTBD request size (bytes)")
                                                  .baseUnit("bytes")
                                                  .tags(tags)
                                                  .publishPercentileHistogram(true)
                                                  .publishPercentiles(0.9, 0.95, 0.99)
                                                  .register(registry);

            this.responseSize = DistributionSummary.builder(nameOf("rntbd.req.rspSize"))
                                                   .description("RNTBD response size (bytes)")
                                                   .baseUnit("bytes")
                                                   .tags(tags)
                                                   .publishPercentileHistogram(true)
                                                   .publishPercentiles(0.9, 0.95, 0.99)
                                                   .register(registry);
        }

        public void markComplete(RntbdRequestRecord requestRecord) {
            requestRecord.stop(this.requests, requestRecord.isCompletedExceptionally()
                ? this.responseErrors
                : this.responseSuccesses);
            this.requestSize.record(requestRecord.requestLength());
            this.responseSize.record(requestRecord.responseLength());
        }
    }
}
