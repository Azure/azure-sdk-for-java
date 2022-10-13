// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
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
import com.azure.cosmos.implementation.query.QueryInfo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class ClientTelemetryMetrics {
    private static final Logger logger = LoggerFactory.getLogger(ClientTelemetryMetrics.class);
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();
    private static final
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
            ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
    private static final PercentEscaper PERCENT_ESCAPER = new PercentEscaper("_-/.", false);

    private static CompositeMeterRegistry compositeRegistry = createFreshRegistry();
    private static final ConcurrentHashMap<MeterRegistry, AtomicLong> registryRefCount = new ConcurrentHashMap<>();

    private static String convertStackTraceToString(Throwable throwable)
    {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw))
        {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
        catch (IOException ioe)
        {
            throw new IllegalStateException(ioe);
        }
    }

    private static CompositeMeterRegistry createFreshRegistry() {
        CompositeMeterRegistry registry = new CompositeMeterRegistry();
        if (logger.isTraceEnabled()) {
            registry.config().onMeterAdded(
                (meter) -> {
                    logger.trace(
                        "Meter '{}' added. Callstack: {}",
                        meter.getId().getName(),
                        convertStackTraceToString(new IllegalStateException("Dummy")));
                }
            );
        }

        return registry;
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

        EnumSet<TagName> metricTagNames = clientAccessor.getMetricTagNames(cosmosAsyncClient);

        Set<String> contactedRegions = cosmosDiagnostics.getContactedRegionNames();

        Tags operationTags = createOperationTags(
            metricTagNames,
            cosmosAsyncClient,
            statusCode,
            containerId,
            databaseId,
            operationType,
            resourceType,
            consistencyLevel,
            operationId,
            isPointOperation,
            contactedRegions
        );

        OperationMetricProducer metricProducer = new OperationMetricProducer(metricTagNames, operationTags);
        metricProducer.recordOperation(
            requestCharge,
            latency,
            maxItemCount == null ? -1 : maxItemCount,
            actualItemCount,
            cosmosDiagnostics,
            contactedRegions
        );
    }

    public static RntbdMetricsCompletionRecorder createRntbdMetrics(
        RntbdTransportClient client,
        RntbdEndpoint endpoint) {

        return new RntbdMetricsV2(compositeRegistry, client, endpoint);
    }

    public static synchronized void add(MeterRegistry registry) {
        if (registryRefCount
            .computeIfAbsent(registry, (meterRegistry) -> { return new AtomicLong(0); })
            .incrementAndGet() == 1L) {
            ClientTelemetryMetrics
                .compositeRegistry
                .add(registry);
        }
    }

    public static synchronized void remove(MeterRegistry registry) {
        if (registryRefCount
            .get(registry)
            .decrementAndGet() == 0L) {

            registry.clear();
            registry.close();

            ClientTelemetryMetrics
                .compositeRegistry
                .remove(registry);

            if (ClientTelemetryMetrics.compositeRegistry.getRegistries().isEmpty()) {
                ClientTelemetryMetrics.compositeRegistry = createFreshRegistry();
            }
        }
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
        String containerId,
        String databaseId,
        OperationType operationType,
        ResourceType resourceType,
        ConsistencyLevel consistencyLevel,
        String operationId,
        boolean isPointOperation,
        Set<String> contactedRegions) {
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
            String operationTagValue = !isPointOperation && !Strings.isNullOrWhiteSpace(operationId)
                ? String.format("%s/%s/%s", resourceType.toString(), operationType.toString(), escape(operationId))
                : String.format("%s/%s", resourceType.toString(), operationType.toString());

            effectiveTags.add(Tag.of(TagName.Operation.toString(), operationTagValue));
        }

        if (metricTagNames.contains(TagName.OperationStatusCode)) {
            effectiveTags.add(Tag.of(TagName.OperationStatusCode.toString(), String.valueOf(statusCode)));
        }

        if (metricTagNames.contains(TagName.ConsistencyLevel)) {
            effectiveTags.add(Tag.of(
                TagName.ConsistencyLevel.toString(),
                consistencyLevel == null ?
                    BridgeInternal.getContextClient(cosmosAsyncClient).getConsistencyLevel().toString() :
                    consistencyLevel.toString()
            ));
        }

        if (contactedRegions != null &&
            contactedRegions.size() > 0 &&
            metricTagNames.contains(TagName.RegionName)) {

            effectiveTags.add(Tag.of(
                TagName.RegionName.toString(),
                String.join(", ", contactedRegions)
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
            CosmosDiagnostics diagnostics,
            Set<String> contactedRegions) {

            Counter operationsCounter = Counter
                .builder(nameOf("op.calls"))
                .baseUnit("calls")
                .description("Operation calls")
                .tags(operationTags)
                .register(compositeRegistry);
            operationsCounter.increment();

            DistributionSummary requestChargeMeter = DistributionSummary
                .builder(nameOf("op.RUs"))
                .baseUnit("RU (request unit)")
                .description("Operation RU charge")
                .maximumExpectedValue(10_000_000d)
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(operationTags)
                .register(compositeRegistry);
            requestChargeMeter.record(Math.min(requestCharge, 10_000_000d));

            DistributionSummary regionsContactedMeter = DistributionSummary
                .builder(nameOf("op.regionsContacted"))
                .baseUnit("Regions contacted")
                .description("Operation - regions contacted")
                .maximumExpectedValue(100d)
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(operationTags)
                .register(compositeRegistry);
            if (contactedRegions != null && contactedRegions.size() > 0) {
                regionsContactedMeter.record(Math.min(contactedRegions.size(), 100d));
            }

            Timer latencyMeter = Timer
                .builder(nameOf("op.latency"))
                .description("Operation latency")
                .maximumExpectedValue(Duration.ofSeconds(300))
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
                    recordGatewayStatistics(requestStatistics.getDuration(), requestStatistics.getGatewayStatistics());
                    recordAddressResolutionStatistics(requestStatistics.getAddressResolutionStatistics());
                }
            }

            FeedResponseDiagnostics feedDiagnostics = diagnosticsAccessor
                .getFeedResponseDiagnostics(diagnostics);

            if (feedDiagnostics == null) {
                return;
            }

            QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnostics =
                feedDiagnostics.getQueryPlanDiagnosticsContext();

            recordQueryPlanDiagnostics(queryPlanDiagnostics);
        }

        private void recordQueryPlanDiagnostics(
            QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnostics
        ) {
            if (queryPlanDiagnostics == null) {
                return;
            }

            Tags requestTags = operationTags.and(
                createQueryPlanTags(metricTagNames)
            );

            Counter requestCounter = Counter
                .builder(nameOf("req.gw.requests"))
                .baseUnit("requests")
                .description("Gateway requests")
                .tags(requestTags)
                .register(compositeRegistry);
            requestCounter.increment();

            Duration latency = queryPlanDiagnostics.getDuration();

            if (latency != null) {
                Timer requestLatencyMeter = Timer
                    .builder(nameOf("req.gw.latency"))
                    .description("Gateway Request latency")
                    .maximumExpectedValue(Duration.ofSeconds(300))
                    .publishPercentiles(0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .tags(requestTags)
                    .register(compositeRegistry);
                requestLatencyMeter.record(latency);
            }

            recordRequestTimeline("req.gw.timeline.", queryPlanDiagnostics.getRequestTimeline(), requestTags);
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
                    String.format("%d/%d", statusCode, subStatusCode)));
            }

            if (metricTagNames.contains(TagName.RequestOperationType)) {
                effectiveTags.add(Tag.of(
                    TagName.RequestOperationType.toString(),
                    String.format("%s/%s", resourceType.toString(), operationType.toString())));
            }

            if (metricTagNames.contains(TagName.RegionName)) {
                effectiveTags.add(Tag.of(
                    TagName.RegionName.toString(),
                    regionName != null ? regionName : "NONE"));
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

        private Tags createQueryPlanTags(
            EnumSet<TagName> metricTagNames
        ) {
            List<Tag> effectiveTags = new ArrayList<>();

            if (metricTagNames.contains(TagName.RequestOperationType)) {
                effectiveTags.add(Tag.of(
                    TagName.RequestOperationType.toString(),
                    String.format(
                        "%s/%s",
                        ResourceType.DocumentCollection,
                        OperationType.QueryPlan)));
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

        private void recordRntbdEndpointStatistics(RntbdEndpointStatistics endpointStatistics, Tags requestTags) {
            if (endpointStatistics == null) {
                return;
            }

            DistributionSummary acquiredChannelsMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.endpoint.acquiredChannels"))
                .baseUnit("#")
                .description("Endpoint statistics(acquired channels)")
                .maximumExpectedValue(100_000d)
                .publishPercentiles()
                .publishPercentileHistogram(false)
                .tags(requestTags)
                .register(compositeRegistry);
            acquiredChannelsMeter.record(endpointStatistics.getAcquiredChannels());

            DistributionSummary availableChannelsMeter = DistributionSummary
                .builder(nameOf("req.rntbd.stats.endpoint.availableChannels"))
                .baseUnit("#")
                .description("Endpoint statistics(available channels)")
                .maximumExpectedValue(100_000d)
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
                .maximumExpectedValue(1_000_000d)
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .register(compositeRegistry);
            inflightRequestsMeter.record(endpointStatistics.getInflightRequests());
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
                    .maximumExpectedValue(Duration.ofSeconds(300))
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

                Double backendLatency = storeResultDiagnostics.getBackendLatencyInMs();

                if (backendLatency != null) {
                    DistributionSummary backendRequestLatencyMeter = DistributionSummary
                        .builder(nameOf("req.rntbd.backendLatency"))
                        .baseUnit("ms")
                        .description("Backend service latency")
                        .maximumExpectedValue(6_000d)
                        .publishPercentiles(0.95, 0.99)
                        .publishPercentileHistogram(true)
                        .tags(requestTags)
                        .register(compositeRegistry);
                    backendRequestLatencyMeter.record(storeResultDiagnostics.getBackendLatencyInMs());
                }

                double requestCharge = storeResponseDiagnostics.getRequestCharge();
                DistributionSummary requestChargeMeter = DistributionSummary
                    .builder(nameOf("req.rntbd.RUs"))
                    .baseUnit("RU (request unit)")
                    .description("RNTBD Request RU charge")
                    .maximumExpectedValue(1_000_000d)
                    .publishPercentiles(0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .tags(requestTags)
                    .register(compositeRegistry);
                requestChargeMeter.record(Math.min(requestCharge, 1_000_000d));

                Duration latency = responseStatistics.getDuration();
                if (latency != null) {
                    Timer requestLatencyMeter = Timer
                        .builder(nameOf("req.rntbd.latency"))
                        .description("RNTBD Request latency")
                        .maximumExpectedValue(Duration.ofSeconds(6))
                        .publishPercentiles(0.95, 0.99)
                        .publishPercentileHistogram(true)
                        .tags(requestTags)
                        .register(compositeRegistry);
                    requestLatencyMeter.record(latency);
                }

                Counter requestCounter = Counter
                    .builder(nameOf("req.rntbd.requests"))
                    .baseUnit("requests")
                    .description("RNTBD requests")
                    .tags(requestTags)
                    .register(compositeRegistry);
                requestCounter.increment();

                recordRequestTimeline(
                    "req.rntbd.timeline.",
                    storeResponseDiagnostics.getRequestTimeline(), requestTags);

                recordRequestPayloadSizes(
                    storeResponseDiagnostics.getRequestPayloadLength(),
                    storeResponseDiagnostics.getResponsePayloadLength()
                );

                recordRntbdEndpointStatistics(
                    storeResponseDiagnostics.getRntbdEndpointStatistics(),
                    requestTags);
            }
        }

        private void recordGatewayStatistics(
            Duration latency,
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

            Counter requestCounter = Counter
                .builder(nameOf("req.gw.requests"))
                .baseUnit("requests")
                .description("Gateway requests")
                .tags(requestTags)
                .register(compositeRegistry);
            requestCounter.increment();

            double requestCharge = gatewayStatistics.getRequestCharge();
            DistributionSummary requestChargeMeter = DistributionSummary
                .builder(nameOf("req.gw.RUs"))
                .baseUnit("RU (request unit)")
                .description("Gateway Request RU charge")
                .maximumExpectedValue(1_000_000d)
                .publishPercentiles(0.95, 0.99)
                .publishPercentileHistogram(true)
                .tags(requestTags)
                .register(compositeRegistry);
            requestChargeMeter.record(Math.min(requestCharge, 1_000_000d));

            if (latency != null) {
                Timer requestLatencyMeter = Timer
                    .builder(nameOf("req.gw.latency"))
                    .description("Gateway Request latency")
                    .maximumExpectedValue(Duration.ofSeconds(300))
                    .publishPercentiles(0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .tags(requestTags)
                    .register(compositeRegistry);
                requestLatencyMeter.record(latency);
            }

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
                    .builder(nameOf("rntbd.addressResolution.latency"))
                    .description("Address resolution latency")
                    .maximumExpectedValue(Duration.ofSeconds(6))
                    .publishPercentiles(0.95, 0.99)
                    .publishPercentileHistogram(true)
                    .tags(addressResolutionTags)
                    .register(compositeRegistry);
                addressResolutionLatencyMeter.record(latency);

                Counter requestCounter = Counter
                    .builder(nameOf("rntbd.addressResolution.requests"))
                    .baseUnit("requests")
                    .description("Address resolution requests")
                    .tags(addressResolutionTags)
                    .register(compositeRegistry);
                requestCounter.increment();
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
                .maximumExpectedValue(Duration.ofSeconds(300))
                .publishPercentileHistogram(true)
                .publishPercentiles(0.95, 0.99)
                .register(registry);

            this.responseErrors = Timer
                .builder(nameOf("rntbd.requests.failed.latency"))
                .description("RNTBD failed request latency")
                .tags(tags)
                .maximumExpectedValue(Duration.ofSeconds(300))
                .publishPercentileHistogram(true)
                .publishPercentiles(0.95, 0.99)
                .register(registry);

            this.responseSuccesses = Timer
                .builder(nameOf("rntbd.requests.successful.latency"))
                .description("RNTBD successful request latency")
                .tags(tags)
                .maximumExpectedValue(Duration.ofSeconds(300))
                .publishPercentileHistogram(true)
                .publishPercentiles(0.95, 0.99)
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
                                                  .maximumExpectedValue(16_000_000d)
                                                  .publishPercentileHistogram(false)
                                                  .publishPercentiles()
                                                  .register(registry);

            this.responseSize = DistributionSummary.builder(nameOf("rntbd.req.rspSize"))
                                                   .description("RNTBD response size (bytes)")
                                                   .baseUnit("bytes")
                                                   .tags(tags)
                                                   .maximumExpectedValue(16_000_000d)
                                                   .publishPercentileHistogram(false)
                                                   .publishPercentiles()
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
