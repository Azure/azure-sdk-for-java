// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
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
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdDurableEndpointMetrics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdMetricsCompletionRecorder;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestRecord;
import com.azure.cosmos.implementation.guava25.net.PercentEscaper;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.models.CosmosMetricName;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
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
    private static CosmosMeterOptions cpuOptions;
    private static CosmosMeterOptions memoryOptions;

    private static volatile DescendantValidationResult lastDescendantValidation = new DescendantValidationResult(Instant.MIN, true);

    private static final Object lockObject = new Object();
    private static final Tag QUERYPLAN_TAG = Tag.of(
        TagName.RequestOperationType.toString(),
        ResourceType.DocumentCollection + "/" + OperationType.QueryPlan);

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
                (meter) -> logger.trace(
                    "Meter '{}' added. Callstack: {}",
                    meter.getId().getName(),
                    convertStackTraceToString(new IllegalStateException("Dummy")))
            );
        }

        return registry;
    }

    public static void recordSystemUsage(
        float averageSystemCpuUsage,
        float freeMemoryAvailableInMB
    ) {
        if (compositeRegistry.getRegistries().isEmpty() || cpuOptions == null || memoryOptions == null) {
            return;
        }

        if (cpuOptions.isEnabled()) {
            DistributionSummary averageSystemCpuUsageMeter = DistributionSummary
                .builder(CosmosMetricName.SYSTEM_CPU.toString())
                .baseUnit("%")
                .description("Avg. System CPU load")
                .maximumExpectedValue(100d)
                .publishPercentiles(cpuOptions.getPercentiles())
                .publishPercentileHistogram(cpuOptions.isHistogramPublishingEnabled())
                .register(compositeRegistry);
            averageSystemCpuUsageMeter.record(averageSystemCpuUsage);
        }

        if (memoryOptions.isEnabled()) {
            DistributionSummary freeMemoryAvailableInMBMeter = DistributionSummary
                .builder(CosmosMetricName.SYSTEM_MEMORY_FREE.toString())
                .baseUnit("MB")
                .description("Free memory available")
                .publishPercentiles()
                .publishPercentileHistogram(false)
                .register(compositeRegistry);
            freeMemoryAvailableInMBMeter.record(freeMemoryAvailableInMB);
        }
    }

    public static void recordOperation(
        CosmosAsyncClient client,
        CosmosDiagnosticsContext diagnosticsContext
    ) {
        recordOperation(
            client,
            diagnosticsContext,
            diagnosticsContext.getStatusCode(),
            diagnosticsContext.getSubStatusCode(),
            diagnosticsContext.getMaxItemCount(),
            diagnosticsContext.getActualItemCount(),
            diagnosticsContext.getContainerName(),
            diagnosticsContext.getDatabaseName(),
            diagnosticsContext.getOperationType(),
            diagnosticsContext.isPointOperation(),
            diagnosticsContext.getResourceType(),
            diagnosticsContext.getEffectiveConsistencyLevel(),
            diagnosticsContext.getOperationId(),
            diagnosticsContext.getTotalRequestCharge(),
            diagnosticsContext.getDuration()
        );
    }

    private static boolean hasAnyActualMeterRegistry() {

        Instant nowSnapshot = Instant.now();
        DescendantValidationResult snapshot = lastDescendantValidation;
        if (nowSnapshot.isBefore(snapshot.getExpiration())) {
            return snapshot.getResult();
        }

        synchronized (lockObject) {
            snapshot = lastDescendantValidation;
            if (nowSnapshot.isBefore(snapshot.getExpiration())) {
                return snapshot.getResult();
            }

            DescendantValidationResult newResult = new DescendantValidationResult(
                nowSnapshot.plus(10, ChronoUnit.SECONDS),
                hasAnyActualMeterRegistryCore(compositeRegistry, 1)
            );

            lastDescendantValidation = newResult;
            return newResult.getResult();
        }
    }

    private static boolean hasAnyActualMeterRegistryCore(CompositeMeterRegistry compositeMeterRegistry, int depth) {

        if (depth > 100) {
            return true;
        }

        for (MeterRegistry registry : compositeMeterRegistry.getRegistries()) {
            if (registry instanceof CompositeMeterRegistry) {
                if (hasAnyActualMeterRegistryCore((CompositeMeterRegistry)registry, depth + 1)) {
                    return true;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    private static void recordOperation(
        CosmosAsyncClient client,
        CosmosDiagnosticsContext diagnosticsContext,
        int statusCode,
        int subStatusCode,
        Integer maxItemCount,
        Integer actualItemCount,
        String containerId,
        String databaseId,
        String operationType,
        boolean isPointOperation,
        String resourceType,
        ConsistencyLevel consistencyLevel,
        String operationId,
        float requestCharge,
        Duration latency
    ) {
        boolean isClientTelemetryMetricsEnabled = clientAccessor.shouldEnableEmptyPageDiagnostics(client);

        if (!hasAnyActualMeterRegistry() || !isClientTelemetryMetricsEnabled) {
            return;
        }

        Tag clientCorrelationTag = clientAccessor.getClientCorrelationTag(client);
        String accountTagValue = clientAccessor.getAccountTagValue(client);

        EnumSet<TagName> metricTagNames = clientAccessor.getMetricTagNames(client);
        EnumSet<MetricCategory> metricCategories = clientAccessor.getMetricCategories(client);

        Set<String> contactedRegions = Collections.emptySet();
        if (metricCategories.contains(MetricCategory.OperationDetails)) {
            contactedRegions = diagnosticsContext.getContactedRegionNames();
        }

        Tags operationTags = createOperationTags(
            metricTagNames,
            statusCode,
            subStatusCode,
            containerId,
            databaseId,
            operationType,
            resourceType,
            consistencyLevel,
            operationId,
            isPointOperation,
            contactedRegions,
            clientCorrelationTag,
            accountTagValue
        );

        OperationMetricProducer metricProducer = new OperationMetricProducer(metricCategories, metricTagNames, operationTags);
        metricProducer.recordOperation(
            client,
            requestCharge,
            latency,
            maxItemCount == null ? -1 : maxItemCount,
            actualItemCount == null ? -1: actualItemCount,
            diagnosticsContext,
            contactedRegions
        );
    }

    public static RntbdMetricsCompletionRecorder createRntbdMetrics(
        RntbdTransportClient client,
        RntbdEndpoint endpoint) {

        return new RntbdMetricsV2(compositeRegistry, client, endpoint);
    }

    public static synchronized void add(
        MeterRegistry registry,
        CosmosMeterOptions cpuOptions,
        CosmosMeterOptions memoryOptions) {
        if (registryRefCount
            .computeIfAbsent(registry, (meterRegistry) -> new AtomicLong(0))
            .incrementAndGet() == 1L) {

            System.out.println("Adding new meter registry, total registries  " + registryRefCount.size());
            ClientTelemetryMetrics
                .compositeRegistry
                .add(registry);

            // CPU and Memory signals are scoped system-wide - not for each client
            // technically multiple CosmosClients could have different configuration for system meter options
            // which isn't possible because it is a global system-wide metric
            // so using most intuitive compromise - last meter options wins
            ClientTelemetryMetrics.cpuOptions = cpuOptions;
            ClientTelemetryMetrics.memoryOptions = memoryOptions;

            // reset the cached flag whether any actual meter registry is available
            lastDescendantValidation = new DescendantValidationResult(Instant.MIN, true);
        } else {
            System.out.println("Adding new meter registry is skipped");
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

            // reset the cached flag whether any actual meter registry is available
            lastDescendantValidation = new DescendantValidationResult(Instant.MIN, true);
        }
    }

    public static String escape(String value) {
        return PERCENT_ESCAPER.escape(value);
    }

    private static Tags createOperationTags(
        EnumSet<TagName> metricTagNames,
        int statusCode,
        int subStatusCode,
        String containerId,
        String databaseId,
        String operationType,
        String resourceType,
        ConsistencyLevel consistencyLevel,
        String operationId,
        boolean isPointOperation,
        Set<String> contactedRegions,
        Tag clientCorrelationTag,
        String accountTagValue) {

        List<Tag> effectiveTags = new ArrayList<>();

        if (metricTagNames.contains(TagName.ClientCorrelationId)) {
            effectiveTags.add(clientCorrelationTag);
        }

        if (metricTagNames.contains(TagName.Container)) {
            String containerTagValue =
                escape(accountTagValue)
                + "/"
                + (databaseId != null ? escape(databaseId) : "NONE")
                + "/"
                + (containerId != null ? escape(containerId) : "NONE");

            effectiveTags.add(Tag.of(TagName.Container.toString(), containerTagValue));
        }

        if (metricTagNames.contains(TagName.Operation)) {
            String operationTagValue = !isPointOperation && !Strings.isNullOrWhiteSpace(operationId)
                ? resourceType + "/" + operationType + "/" + escape(operationId)
                : resourceType + "/" + operationType;

            effectiveTags.add(Tag.of(TagName.Operation.toString(), operationTagValue));
        }

        if (metricTagNames.contains(TagName.OperationStatusCode)) {
            effectiveTags.add(Tag.of(TagName.OperationStatusCode.toString(), String.valueOf(statusCode)));
        }

        if (metricTagNames.contains(TagName.OperationSubStatusCode)) {
            effectiveTags.add(Tag.of(TagName.OperationSubStatusCode.toString(), String.valueOf(subStatusCode)));
        }

        if (metricTagNames.contains(TagName.ConsistencyLevel)) {
            assert consistencyLevel != null : "ConsistencyLevel must never be null here.";
            effectiveTags.add(Tag.of(
                TagName.ConsistencyLevel.toString(),
                consistencyLevel.toString()
            ));
        }

        if (metricTagNames.contains(TagName.RegionName)) {
            effectiveTags.add(Tag.of(
                TagName.RegionName.toString(),
                contactedRegions != null && contactedRegions.size() > 0
                    ? String.join(", ", contactedRegions) : "NONE"
            ));
        }

        return Tags.of(effectiveTags);
    }

    private static Tags getEffectiveTags(Tags tags, CosmosMeterOptions meterOptions) {
        EnumSet<TagName> suppressedTags = meterOptions.getSuppressedTagNames();
        if (suppressedTags == null || suppressedTags.isEmpty()) {
            return tags;
        }

        HashSet<String> suppressedNames = new HashSet<>();
        for (TagName t: suppressedTags) {
            suppressedNames.add(t.name());
        }

        List<Tag> result = new ArrayList<>();
        for (Tag t: tags) {
            if (!suppressedNames.contains(t.getKey())) {
                result.add(t);
            }
        }

        return Tags.of(result);
    }

    private static class OperationMetricProducer {
        private final EnumSet<TagName> metricTagNames;
        private final EnumSet<MetricCategory> metricCategories;
        private final Tags operationTags;

        public OperationMetricProducer(EnumSet<MetricCategory> metricCategories, EnumSet<TagName> metricTagNames, Tags operationTags) {
            this.metricCategories = metricCategories;
            this.metricTagNames = metricTagNames;
            this.operationTags = operationTags;
        }

        public void recordOperation(
            CosmosAsyncClient cosmosAsyncClient,
            float requestCharge,
            Duration latency,
            int maxItemCount,
            int actualItemCount,
            CosmosDiagnosticsContext diagnosticsContext,
            Set<String> contactedRegions) {

            CosmosMeterOptions callsOptions = clientAccessor.getMeterOptions(
                cosmosAsyncClient,
                CosmosMetricName.OPERATION_SUMMARY_CALLS);

            if (callsOptions.isEnabled()) {
                Counter operationsCounter = Counter
                    .builder(callsOptions.getMeterName().toString())
                    .baseUnit("calls")
                    .description("Operation calls")
                    .tags(getEffectiveTags(operationTags, callsOptions))
                    .register(compositeRegistry);
                operationsCounter.increment();
            }

            CosmosMeterOptions requestChargeOptions = clientAccessor.getMeterOptions(
                cosmosAsyncClient,
                CosmosMetricName.OPERATION_SUMMARY_REQUEST_CHARGE);
            if (requestChargeOptions.isEnabled()) {
                DistributionSummary requestChargeMeter = DistributionSummary
                    .builder(requestChargeOptions.getMeterName().toString())
                    .baseUnit("RU (request unit)")
                    .description("Operation RU charge")
                    .maximumExpectedValue(100_000d)
                    .publishPercentiles(requestChargeOptions.getPercentiles())
                    .publishPercentileHistogram(requestChargeOptions.isHistogramPublishingEnabled())
                    .tags(getEffectiveTags(operationTags, requestChargeOptions))
                    .register(compositeRegistry);
                requestChargeMeter.record(Math.min(requestCharge, 100_000d));
            }

            if (this.metricCategories.contains(MetricCategory.OperationDetails)) {
                CosmosMeterOptions regionsOptions = clientAccessor.getMeterOptions(
                    cosmosAsyncClient,
                    CosmosMetricName.OPERATION_DETAILS_REGIONS_CONTACTED);
                if (regionsOptions.isEnabled()) {
                    DistributionSummary regionsContactedMeter = DistributionSummary
                        .builder(regionsOptions.getMeterName().toString())
                        .baseUnit("Regions contacted")
                        .description("Operation - regions contacted")
                        .maximumExpectedValue(100d)
                        .publishPercentiles()
                        .publishPercentileHistogram(false)
                        .tags(getEffectiveTags(operationTags, regionsOptions))
                        .register(compositeRegistry);
                    if (contactedRegions != null && contactedRegions.size() > 0) {
                        regionsContactedMeter.record(Math.min(contactedRegions.size(), 100d));
                    }
                }

                this.recordItemCounts(cosmosAsyncClient, maxItemCount, actualItemCount);
            }

            CosmosMeterOptions latencyOptions = clientAccessor.getMeterOptions(
                cosmosAsyncClient,
                CosmosMetricName.OPERATION_SUMMARY_LATENCY);
            if (latencyOptions.isEnabled()) {
                Timer latencyMeter = Timer
                    .builder(latencyOptions.getMeterName().toString())
                    .description("Operation latency")
                    .maximumExpectedValue(Duration.ofSeconds(300))
                    .publishPercentiles(latencyOptions.getPercentiles())
                    .publishPercentileHistogram(latencyOptions.isHistogramPublishingEnabled())
                    .tags(getEffectiveTags(operationTags, latencyOptions))
                    .register(compositeRegistry);
                latencyMeter.record(latency);
            }

            for (CosmosDiagnostics diagnostics: diagnosticsContext.getDiagnostics()) {
                Collection<ClientSideRequestStatistics> clientSideRequestStatistics =
                    diagnosticsAccessor.getClientSideRequestStatistics(diagnostics);

                if (clientSideRequestStatistics != null) {
                    for (ClientSideRequestStatistics requestStatistics : clientSideRequestStatistics) {

                        recordStoreResponseStatistics(
                            diagnosticsContext,
                            cosmosAsyncClient,
                            requestStatistics.getResponseStatisticsList(),
                            actualItemCount);
                        recordStoreResponseStatistics(
                            diagnosticsContext,
                            cosmosAsyncClient,
                            requestStatistics.getSupplementalResponseStatisticsList(),
                            -1);
                        recordGatewayStatistics(
                            diagnosticsContext,
                            cosmosAsyncClient,
                            requestStatistics.getDuration(),
                            requestStatistics.getGatewayStatisticsList(),
                            requestStatistics.getRequestPayloadSizeInBytes(),
                            actualItemCount);
                        recordAddressResolutionStatistics(
                            diagnosticsContext,
                            cosmosAsyncClient,
                            requestStatistics.getAddressResolutionStatistics());
                    }
                }

                FeedResponseDiagnostics feedDiagnostics = diagnosticsAccessor
                    .getFeedResponseDiagnostics(diagnostics);

                if (feedDiagnostics == null) {
                    continue;
                }

                QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnostics =
                    feedDiagnostics.getQueryPlanDiagnosticsContext();

                recordQueryPlanDiagnostics(diagnosticsContext, cosmosAsyncClient, queryPlanDiagnostics);
            }
        }

        private void recordQueryPlanDiagnostics(
            CosmosDiagnosticsContext ctx,
            CosmosAsyncClient cosmosAsyncClient,
            QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnostics
        ) {
            if (queryPlanDiagnostics == null || !this.metricCategories.contains(MetricCategory.RequestSummary)) {
                return;
            }

            Tags requestTags = operationTags.and(
                createQueryPlanTags(metricTagNames)
            );

            CosmosMeterOptions requestsOptions = clientAccessor.getMeterOptions(
                cosmosAsyncClient,
                CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS);
            if (requestsOptions.isEnabled() &&
                (!requestsOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                Counter requestCounter = Counter
                    .builder(requestsOptions.getMeterName().toString())
                    .baseUnit("requests")
                    .description("Gateway requests")
                    .tags(getEffectiveTags(requestTags, requestsOptions))
                    .register(compositeRegistry);
                requestCounter.increment();
            }

            Duration latency = queryPlanDiagnostics.getDuration();

            if (latency != null) {
                CosmosMeterOptions latencyOptions = clientAccessor.getMeterOptions(
                    cosmosAsyncClient,
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY);
                if (latencyOptions.isEnabled() &&
                    (!latencyOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    Timer requestLatencyMeter = Timer
                        .builder(latencyOptions.getMeterName().toString())
                        .description("Gateway Request latency")
                        .maximumExpectedValue(Duration.ofSeconds(300))
                        .publishPercentiles(latencyOptions.getPercentiles())
                        .publishPercentileHistogram(latencyOptions.isHistogramPublishingEnabled())
                        .tags(getEffectiveTags(requestTags, latencyOptions))
                        .register(compositeRegistry);
                    requestLatencyMeter.record(latency);
                }
            }

            recordRequestTimeline(
                ctx,
                cosmosAsyncClient,
                CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE,
                queryPlanDiagnostics.getRequestTimeline(), requestTags);
        }

        private void recordRequestPayloadSizes(
            CosmosDiagnosticsContext ctx,
            CosmosAsyncClient client,
            int requestPayloadSizeInBytes,
            int responsePayloadSizeInBytes
        ) {
            CosmosMeterOptions reqSizeOptions = clientAccessor.getMeterOptions(
                client,
                CosmosMetricName.REQUEST_SUMMARY_SIZE_REQUEST);
            if (reqSizeOptions.isEnabled() &&
                (!reqSizeOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                DistributionSummary requestPayloadSizeMeter = DistributionSummary
                    .builder(reqSizeOptions.getMeterName().toString())
                    .baseUnit("bytes")
                    .description("Request payload size in bytes")
                    .maximumExpectedValue(16d * 1024)
                    .publishPercentiles()
                    .publishPercentileHistogram(false)
                    .tags(getEffectiveTags(operationTags, reqSizeOptions))
                    .register(compositeRegistry);
                requestPayloadSizeMeter.record(requestPayloadSizeInBytes);
            }

            CosmosMeterOptions rspSizeOptions = clientAccessor.getMeterOptions(
                client,
                CosmosMetricName.REQUEST_SUMMARY_SIZE_RESPONSE);
            if (rspSizeOptions.isEnabled() &&
                (!rspSizeOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                DistributionSummary responsePayloadSizeMeter = DistributionSummary
                    .builder(rspSizeOptions.getMeterName().toString())
                    .baseUnit("bytes")
                    .description("Response payload size in bytes")
                    .maximumExpectedValue(16d * 1024)
                    .publishPercentiles()
                    .publishPercentileHistogram(false)
                    .tags(getEffectiveTags(operationTags, rspSizeOptions))
                    .register(compositeRegistry);
                responsePayloadSizeMeter.record(responsePayloadSizeInBytes);
            }
        }

        private void recordItemCounts(
            CosmosAsyncClient client,
            int maxItemCount,
            int actualItemCount
        ) {
            if (maxItemCount > 0 && this.metricCategories.contains(MetricCategory.OperationDetails)) {

                CosmosMeterOptions maxItemCountOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.OPERATION_DETAILS_MAX_ITEM_COUNT);
                if (maxItemCountOptions.isEnabled()) {
                    DistributionSummary maxItemCountMeter = DistributionSummary
                        .builder(maxItemCountOptions.getMeterName().toString())
                        .baseUnit("item count")
                        .description("Request max. item count")
                        .maximumExpectedValue(100_000d)
                        .publishPercentiles()
                        .publishPercentileHistogram(false)
                        .tags(getEffectiveTags(operationTags, maxItemCountOptions))
                        .register(compositeRegistry);
                    maxItemCountMeter.record(Math.max(0, Math.min(maxItemCount, 100_000d)));
                }

                CosmosMeterOptions actualItemCountOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.OPERATION_DETAILS_ACTUAL_ITEM_COUNT);
                if (actualItemCountOptions.isEnabled()) {
                    DistributionSummary actualItemCountMeter = DistributionSummary
                        .builder(actualItemCountOptions.getMeterName().toString())
                        .baseUnit("item count")
                        .description("Response actual item count")
                        .maximumExpectedValue(100_000d)
                        .publishPercentiles()
                        .publishPercentileHistogram(false)
                        .tags(getEffectiveTags(operationTags, actualItemCountOptions))
                        .register(compositeRegistry);
                    actualItemCountMeter.record(Math.max(0, Math.min(actualItemCount, 100_000d)));
                }
            }
        }

        private Tags createRequestTags(
            EnumSet<TagName> metricTagNames,
            String pkRangeId,
            int statusCode,
            int subStatusCode,
            String resourceType,
            String operationType,
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
                    statusCode + "/" + subStatusCode));
            }

            if (metricTagNames.contains(TagName.RequestOperationType)) {
                effectiveTags.add(Tag.of(
                    TagName.RequestOperationType.toString(),
                    resourceType + "/" + operationType));
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

            String effectiveServiceAddress = serviceAddress != null ? escape(serviceAddress) : "NONE";
            if (metricTagNames.contains(TagName.ServiceAddress)) {
                effectiveTags.add(Tag.of(
                    TagName.ServiceAddress.toString(),
                    effectiveServiceAddress));
            }

            boolean containsPartitionId = metricTagNames.contains(TagName.PartitionId);
            boolean containsReplicaId = metricTagNames.contains(TagName.ReplicaId);
            if (containsPartitionId || containsReplicaId) {

                String partitionId = "NONE";
                String replicaId = "NONE";

                String[] partitionAndReplicaId =
                    StoreResultDiagnostics.getPartitionAndReplicaId(effectiveServiceAddress);
                if (partitionAndReplicaId.length == 2) {
                    partitionId = partitionAndReplicaId[0];
                    replicaId = partitionAndReplicaId[1];
                }

                if (containsPartitionId) {
                    effectiveTags.add(Tag.of(
                        TagName.PartitionId.toString(),
                        partitionId));
                }

                if (containsReplicaId) {
                    effectiveTags.add(Tag.of(
                        TagName.ReplicaId.toString(),
                        replicaId));
                }
            }

            return Tags.of(effectiveTags);
        }

        private Tags createQueryPlanTags(
            EnumSet<TagName> metricTagNames
        ) {
            List<Tag> effectiveTags = new ArrayList<>();

            if (metricTagNames.contains(TagName.RequestOperationType)) {
                effectiveTags.add(QUERYPLAN_TAG);
            }
            if (metricTagNames.contains(TagName.RequestStatusCode)) {
                effectiveTags.add(Tag.of(TagName.RequestStatusCode.toString(),"NONE"));
            }
            if (metricTagNames.contains(TagName.PartitionKeyRangeId)) {
                effectiveTags.add(Tag.of(TagName.PartitionKeyRangeId.toString(),"NONE"));
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

        private void recordRntbdEndpointStatistics(
            CosmosAsyncClient client,
            RntbdEndpointStatistics endpointStatistics,
            Tags requestTags) {
            if (endpointStatistics == null || !this.metricCategories.contains(MetricCategory.Legacy)) {
                return;
            }

            CosmosMeterOptions acquiredOptions = clientAccessor.getMeterOptions(
                client,
                CosmosMetricName.LEGACY_DIRECT_ENDPOINT_STATISTICS_ACQUIRED);
            if (acquiredOptions.isEnabled()) {
                DistributionSummary acquiredChannelsMeter = DistributionSummary
                    .builder(acquiredOptions.getMeterName().toString())
                    .baseUnit("#")
                    .description("Endpoint statistics(acquired channels)")
                    .maximumExpectedValue(100_000d)
                    .publishPercentiles()
                    .publishPercentileHistogram(false)
                    .tags(getEffectiveTags(requestTags, acquiredOptions))
                    .register(compositeRegistry);

                acquiredChannelsMeter.record(endpointStatistics.getAcquiredChannels());
            }

            CosmosMeterOptions availableOptions = clientAccessor.getMeterOptions(
                client,
                CosmosMetricName.LEGACY_DIRECT_ENDPOINT_STATISTICS_AVAILABLE);
            if (availableOptions.isEnabled()) {
                DistributionSummary availableChannelsMeter = DistributionSummary
                    .builder(availableOptions.getMeterName().toString())
                    .baseUnit("#")
                    .description("Endpoint statistics(available channels)")
                    .maximumExpectedValue(100_000d)
                    .publishPercentiles()
                    .publishPercentileHistogram(false)
                    .tags(getEffectiveTags(requestTags, availableOptions))
                    .register(compositeRegistry);
                availableChannelsMeter.record(endpointStatistics.getAvailableChannels());
            }

            CosmosMeterOptions inflightOptions = clientAccessor.getMeterOptions(
                client,
                CosmosMetricName.LEGACY_DIRECT_ENDPOINT_STATISTICS_INFLIGHT);
            if (inflightOptions.isEnabled()) {
                DistributionSummary inflightRequestsMeter = DistributionSummary
                    .builder(inflightOptions.getMeterName().toString())
                    .baseUnit("#")
                    .description("Endpoint statistics(inflight requests)")
                    .tags(getEffectiveTags(requestTags, inflightOptions))
                    .maximumExpectedValue(1_000_000d)
                    .publishPercentiles(inflightOptions.getPercentiles())
                    .publishPercentileHistogram(inflightOptions.isHistogramPublishingEnabled())
                    .register(compositeRegistry);
                inflightRequestsMeter.record(endpointStatistics.getInflightRequests());
            }
        }

        private void recordRequestTimeline(
            CosmosDiagnosticsContext ctx,
            CosmosAsyncClient client,
            CosmosMetricName name,
            RequestTimeline requestTimeline,
            Tags requestTags) {

            if (requestTimeline == null || !this.metricCategories.contains(MetricCategory.RequestDetails)) {
                return;
            }

            CosmosMeterOptions timelineOptions = clientAccessor.getMeterOptions(
                client,
                name);
            if (!timelineOptions.isEnabled() ||
                (timelineOptions.isDiagnosticThresholdsFilteringEnabled() && !ctx.isThresholdViolated())) {
                return;
            }
            for (RequestTimeline.Event event : requestTimeline) {
                Duration duration = event.getDuration();
                if (duration == null || duration == Duration.ZERO) {
                    continue;
                }

                Timer eventMeter = Timer
                    .builder(timelineOptions.getMeterName().toString() + "." + escape(event.getName()))
                    .description("Request timeline (" + event.getName() + ")")
                    .maximumExpectedValue(Duration.ofSeconds(300))
                    .publishPercentiles(timelineOptions.getPercentiles())
                    .publishPercentileHistogram(timelineOptions.isHistogramPublishingEnabled())
                    .tags(getEffectiveTags(requestTags, timelineOptions))
                    .register(compositeRegistry);
                eventMeter.record(duration);
            }
        }

        private void recordStoreResponseStatistics(
            CosmosDiagnosticsContext ctx,
            CosmosAsyncClient client,
            Collection<ClientSideRequestStatistics.StoreResponseStatistics> storeResponseStatistics,
            int actualItemCount) {

            if (!this.metricCategories.contains(MetricCategory.RequestSummary)) {
                return;
            }

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
                        responseStatistics.getRequestResourceType().toString(),
                        responseStatistics.getRequestOperationType().toString(),
                        responseStatistics.getRegionName(),
                        storeResultDiagnostics.getStorePhysicalAddressEscapedAuthority(),
                        storeResultDiagnostics.getStorePhysicalAddressEscapedPath())
                );

                Double backendLatency = storeResultDiagnostics.getBackendLatencyInMs();

                if (backendLatency != null) {

                    CosmosMeterOptions beLatencyOptions = clientAccessor.getMeterOptions(
                        client,
                        CosmosMetricName.REQUEST_SUMMARY_DIRECT_BACKEND_LATENCY);
                    if (beLatencyOptions.isEnabled() &&
                        (!beLatencyOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                        DistributionSummary backendRequestLatencyMeter = DistributionSummary
                            .builder(beLatencyOptions.getMeterName().toString())
                            .baseUnit("ms")
                            .description("Backend service latency")
                            .maximumExpectedValue(6_000d)
                            .publishPercentiles(beLatencyOptions.getPercentiles())
                            .publishPercentileHistogram(beLatencyOptions.isHistogramPublishingEnabled())
                            .tags(getEffectiveTags(requestTags, beLatencyOptions))
                            .register(compositeRegistry);
                        backendRequestLatencyMeter.record(storeResultDiagnostics.getBackendLatencyInMs());
                    }
                }

                CosmosMeterOptions ruOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUEST_CHARGE);
                if (ruOptions.isEnabled() &&
                    (!ruOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    double requestCharge = storeResponseDiagnostics.getRequestCharge();
                    DistributionSummary requestChargeMeter = DistributionSummary
                        .builder(ruOptions.getMeterName().toString())
                        .baseUnit("RU (request unit)")
                        .description("RNTBD Request RU charge")
                        .maximumExpectedValue(100_000d)
                        .publishPercentiles(ruOptions.getPercentiles())
                        .publishPercentileHistogram(ruOptions.isHistogramPublishingEnabled())
                        .tags(getEffectiveTags(requestTags, ruOptions))
                        .register(compositeRegistry);
                    requestChargeMeter.record(Math.min(requestCharge, 100_000d));
                }

                CosmosMeterOptions latencyOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.REQUEST_SUMMARY_DIRECT_LATENCY);
                if (latencyOptions.isEnabled() &&
                    (!latencyOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    Duration latency = responseStatistics.getDuration();
                    if (latency != null) {
                        Timer requestLatencyMeter = Timer
                            .builder(latencyOptions.getMeterName().toString())
                            .description("RNTBD Request latency")
                            .maximumExpectedValue(Duration.ofSeconds(6))
                            .publishPercentiles(latencyOptions.getPercentiles())
                            .publishPercentileHistogram(latencyOptions.isHistogramPublishingEnabled())
                            .tags(getEffectiveTags(requestTags, latencyOptions))
                            .register(compositeRegistry);
                        requestLatencyMeter.record(latency);
                    }
                }

                CosmosMeterOptions reqOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUESTS);
                if (reqOptions.isEnabled() &&
                    (!reqOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    Counter requestCounter = Counter
                        .builder(reqOptions.getMeterName().toString())
                        .baseUnit("requests")
                        .description("RNTBD requests")
                        .tags(getEffectiveTags(requestTags, reqOptions))
                        .register(compositeRegistry);
                    requestCounter.increment();
                }

                CosmosMeterOptions actualItemCountOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.REQUEST_SUMMARY_DIRECT_ACTUAL_ITEM_COUNT);

                if (actualItemCountOptions.isEnabled()
                    && (!actualItemCountOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    DistributionSummary actualItemCountMeter = DistributionSummary
                        .builder(actualItemCountOptions.getMeterName().toString())
                        .baseUnit("item count")
                        .description("Rntbd response actual item count")
                        .maximumExpectedValue(100_000d)
                        .publishPercentiles()
                        .publishPercentileHistogram(false)
                        .tags(getEffectiveTags(requestTags, actualItemCountOptions))
                        .register(compositeRegistry);
                    actualItemCountMeter.record(Math.max(0, Math.min(actualItemCount, 100_000d)));
                }

                if (this.metricCategories.contains(MetricCategory.RequestDetails)) {
                    recordRequestTimeline(
                        ctx,
                        client,
                        CosmosMetricName.REQUEST_DETAILS_DIRECT_TIMELINE,
                        storeResponseDiagnostics.getRequestTimeline(), requestTags);
                }

                recordRequestPayloadSizes(
                    ctx,
                    client,
                    storeResponseDiagnostics.getRequestPayloadLength(),
                    storeResponseDiagnostics.getResponsePayloadLength()
                );

                recordRntbdEndpointStatistics(
                    client,
                    storeResponseDiagnostics.getRntbdEndpointStatistics(),
                    requestTags);
            }
        }

        private void recordGatewayStatistics(
            CosmosDiagnosticsContext ctx,
            CosmosAsyncClient client,
            Duration latency,
            List<ClientSideRequestStatistics.GatewayStatistics> gatewayStatisticsList,
            int requestPayloadSizeInBytes,
            int actualItemCount) {

            if (gatewayStatisticsList == null
                || gatewayStatisticsList.size() == 0
                || !this.metricCategories.contains(MetricCategory.RequestSummary)) {
                return;
            }

            EnumSet<TagName> metricTagNamesForGateway = metricTagNames.clone();
            metricTagNamesForGateway.remove(TagName.RegionName);
            metricTagNamesForGateway.remove(TagName.ServiceAddress);
            metricTagNamesForGateway.remove(TagName.ServiceEndpoint);
            metricTagNamesForGateway.remove(TagName.PartitionId);
            metricTagNamesForGateway.remove(TagName.ReplicaId);

            for (ClientSideRequestStatistics.GatewayStatistics gatewayStats : gatewayStatisticsList) {
                Tags requestTags = operationTags.and(
                    createRequestTags(
                        metricTagNamesForGateway,
                        gatewayStats.getPartitionKeyRangeId(),
                        gatewayStats.getStatusCode(),
                        gatewayStats.getSubStatusCode(),
                        gatewayStats.getResourceType().toString(),
                        gatewayStats.getOperationType().toString(),
                        null,
                        null,
                        null)
                );

                recordRequestPayloadSizes(
                    ctx,
                    client,
                    requestPayloadSizeInBytes,
                    gatewayStats.getResponsePayloadSizeInBytes()
                );

                CosmosMeterOptions reqOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS);
                if (reqOptions.isEnabled() &&
                    (!reqOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    Counter requestCounter = Counter
                        .builder(reqOptions.getMeterName().toString())
                        .baseUnit("requests")
                        .description("Gateway requests")
                        .tags(getEffectiveTags(requestTags, reqOptions))
                        .register(compositeRegistry);
                    requestCounter.increment();
                }

                CosmosMeterOptions ruOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE);
                if (ruOptions.isEnabled() &&
                    (!ruOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    double requestCharge = gatewayStats.getRequestCharge();
                    DistributionSummary requestChargeMeter = DistributionSummary
                        .builder(ruOptions.getMeterName().toString())
                        .baseUnit("RU (request unit)")
                        .description("Gateway Request RU charge")
                        .maximumExpectedValue(100_000d)
                        .publishPercentiles(ruOptions.getPercentiles())
                        .publishPercentileHistogram(ruOptions.isHistogramPublishingEnabled())
                        .tags(getEffectiveTags(requestTags, ruOptions))
                        .register(compositeRegistry);
                    requestChargeMeter.record(Math.min(requestCharge, 100_000d));
                }

                if (latency != null) {
                    CosmosMeterOptions latencyOptions = clientAccessor.getMeterOptions(
                        client,
                        CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY);
                    if (latencyOptions.isEnabled() &&
                        (!latencyOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                        Timer requestLatencyMeter = Timer
                            .builder(latencyOptions.getMeterName().toString())
                            .description("Gateway Request latency")
                            .maximumExpectedValue(Duration.ofSeconds(300))
                            .publishPercentiles(latencyOptions.getPercentiles())
                            .publishPercentileHistogram(latencyOptions.isHistogramPublishingEnabled())
                            .tags(getEffectiveTags(requestTags, latencyOptions))
                            .register(compositeRegistry);
                        requestLatencyMeter.record(latency);
                    }
                }

                CosmosMeterOptions actualItemCountOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.REQUEST_SUMMARY_GATEWAY_ACTUAL_ITEM_COUNT);

                if (actualItemCountOptions.isEnabled()
                    && (!actualItemCountOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    DistributionSummary actualItemCountMeter = DistributionSummary
                        .builder(actualItemCountOptions.getMeterName().toString())
                        .baseUnit("item count")
                        .description("Gateway response actual item count")
                        .maximumExpectedValue(100_000d)
                        .publishPercentiles()
                        .publishPercentileHistogram(false)
                        .tags(getEffectiveTags(requestTags, actualItemCountOptions))
                        .register(compositeRegistry);
                    actualItemCountMeter.record(Math.max(0, Math.min(actualItemCount, 100_000d)));
                }

                recordRequestTimeline(
                    ctx,
                    client,
                    CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE,
                    gatewayStats.getRequestTimeline(), requestTags);
            }
        }

        private void recordAddressResolutionStatistics(
            CosmosDiagnosticsContext ctx,
            CosmosAsyncClient client,
            Map<String, ClientSideRequestStatistics.AddressResolutionStatistics> addressResolutionStatisticsMap) {

            if (addressResolutionStatisticsMap == null
                || addressResolutionStatisticsMap.size() == 0
                || !this.metricCategories.contains(MetricCategory.AddressResolutions) ) {

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

                CosmosMeterOptions latencyOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.DIRECT_ADDRESS_RESOLUTION_LATENCY);
                if (latencyOptions.isEnabled() &&
                    (!latencyOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    Timer addressResolutionLatencyMeter = Timer
                        .builder(latencyOptions.getMeterName().toString())
                        .description("Address resolution latency")
                        .maximumExpectedValue(Duration.ofSeconds(6))
                        .publishPercentiles(latencyOptions.getPercentiles())
                        .publishPercentileHistogram(latencyOptions.isHistogramPublishingEnabled())
                        .tags(getEffectiveTags(addressResolutionTags, latencyOptions))
                        .register(compositeRegistry);
                    addressResolutionLatencyMeter.record(latency);
                }

                CosmosMeterOptions reqOptions = clientAccessor.getMeterOptions(
                    client,
                    CosmosMetricName.DIRECT_ADDRESS_RESOLUTION_REQUESTS);
                if (reqOptions.isEnabled() &&
                    (!reqOptions.isDiagnosticThresholdsFilteringEnabled() || ctx.isThresholdViolated())) {
                    Counter requestCounter = Counter
                        .builder(reqOptions.getMeterName().toString())
                        .baseUnit("requests")
                        .description("Address resolution requests")
                        .tags(getEffectiveTags(addressResolutionTags, reqOptions))
                        .register(compositeRegistry);
                    requestCounter.increment();
                }
            }
        }
    }

    private static class RntbdMetricsV2 implements RntbdMetricsCompletionRecorder {
        private final RntbdTransportClient client;
        private final Tags tags;
        private final MeterRegistry registry;

        private RntbdMetricsV2(MeterRegistry registry, RntbdTransportClient client, RntbdEndpoint endpoint) {
            this.tags = Tags.of(endpoint.clientMetricTag());
            this.client = client;
            this.registry = registry;
            if (this.client.getMetricCategories().contains(MetricCategory.DirectRequests)) {

                CosmosMeterOptions options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_REQUEST_CONCURRENT_COUNT);
                if (options.isEnabled()) {
                    Gauge.builder(options.getMeterName().toString(), endpoint, RntbdEndpoint::concurrentRequests)
                         .description("RNTBD concurrent requests (executing or queued request count)")
                         .tags(getEffectiveTags(tags, options))
                         .register(registry);
                }

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_REQUEST_QUEUED_COUNT);
                if (options.isEnabled()) {
                    Gauge.builder(options.getMeterName().toString(), endpoint, RntbdEndpoint::requestQueueLength)
                         .description("RNTBD queued request count")
                         .tags(getEffectiveTags(tags, options))
                         .register(registry);
                }
            }

            if (this.client.getMetricCategories().contains(MetricCategory.DirectEndpoints)) {
                CosmosMeterOptions options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_ENDPOINTS_COUNT);
                if (options.isEnabled()) {
                    Gauge.builder(options.getMeterName().toString(), client, RntbdTransportClient::endpointCount)
                         .description("RNTBD endpoint count")
                         .register(registry);
                }

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_ENDPOINTS_EVICTED);
                if (options.isEnabled()) {
                    FunctionCounter.builder(
                        options.getMeterName().toString(),
                        client,
                        RntbdTransportClient::endpointEvictionCount)
                                   .description("RNTBD endpoint eviction count")
                                   .register(registry);
                }
            }

            if (this.client.getMetricCategories().contains(MetricCategory.DirectChannels)) {
                CosmosMeterOptions options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_CHANNELS_ACQUIRED_COUNT);
                if (options.isEnabled()) {
                    FunctionCounter.builder(
                        options.getMeterName().toString(),
                        endpoint.durableEndpointMetrics(),
                        RntbdDurableEndpointMetrics::totalChannelsAcquiredMetric)
                                   .description("RNTBD acquired channel count")
                                   .tags(getEffectiveTags(tags, options))
                                   .register(registry);
                }

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_CHANNELS_CLOSED_COUNT);
                if (options.isEnabled()) {
                    FunctionCounter.builder(
                        options.getMeterName().toString(),
                        endpoint.durableEndpointMetrics(),
                        RntbdDurableEndpointMetrics::totalChannelsClosedMetric)
                                   .description("RNTBD closed channel count")
                                   .tags(getEffectiveTags(tags, options))
                                   .register(registry);
                }

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_CHANNELS_AVAILABLE_COUNT);
                if (options.isEnabled()) {
                    Gauge.builder(
                        options.getMeterName().toString(),
                        endpoint.durableEndpointMetrics(),
                        RntbdDurableEndpointMetrics::channelsAvailableMetric)
                         .description("RNTBD available channel count")
                         .tags(getEffectiveTags(tags, options))
                         .register(registry);
                }
            }
        }

        public void markComplete(RntbdRequestRecord requestRecord) {
            if (this.client.getMetricCategories().contains(MetricCategory.DirectRequests)) {

                Timer requests = null;
                Timer requestsSuccess = null;
                Timer requestsFailed = null;

                CosmosMeterOptions options = this.client
                    .getMeterOptions(CosmosMetricName.DIRECT_REQUEST_LATENCY);

                if (options.isEnabled()) {
                    requests = Timer
                        .builder(options.getMeterName().toString())
                        .description("RNTBD request latency")
                        .maximumExpectedValue(Duration.ofSeconds(300))
                        .publishPercentiles(options.getPercentiles())
                        .publishPercentileHistogram(options.isHistogramPublishingEnabled())
                        .tags(getEffectiveTags(this.tags, options))
                        .register(this.registry);
                }

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_REQUEST_LATENCY_FAILED);
                if (options.isEnabled()) {
                    requestsFailed = Timer
                        .builder(options.getMeterName().toString())
                        .description("RNTBD failed request latency")
                        .maximumExpectedValue(Duration.ofSeconds(300))
                        .publishPercentiles(options.getPercentiles())
                        .publishPercentileHistogram(options.isHistogramPublishingEnabled())
                        .tags(getEffectiveTags(tags, options))
                        .register(registry);
                }

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_REQUEST_LATENCY_SUCCESS);
                if (options.isEnabled()) {
                    requestsSuccess = Timer
                        .builder(options.getMeterName().toString())
                        .description("RNTBD successful request latency")
                        .maximumExpectedValue(Duration.ofSeconds(300))
                        .publishPercentiles(options.getPercentiles())
                        .publishPercentileHistogram(options.isHistogramPublishingEnabled())
                        .tags(getEffectiveTags(tags, options))
                        .register(registry);
                }

                requestRecord.stop(
                    requests,
                    requestRecord.isCompletedExceptionally() ? requestsFailed : requestsSuccess);

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_REQUEST_SIZE_REQUEST);
                if (options.isEnabled()) {
                    DistributionSummary requestSize = DistributionSummary.builder(options.getMeterName().toString())
                                                          .description("RNTBD request size (bytes)")
                                                          .baseUnit("bytes")
                                                          .tags(getEffectiveTags(tags, options))
                                                          .maximumExpectedValue(16_000_000d)
                                                          .publishPercentileHistogram(false)
                                                          .publishPercentiles()
                                                          .register(registry);
                    requestSize.record(requestRecord.requestLength());
                }

                options = client
                    .getMeterOptions(CosmosMetricName.DIRECT_REQUEST_SIZE_RESPONSE);
                if (options.isEnabled()) {
                    DistributionSummary responseSize = DistributionSummary.builder(options.getMeterName().toString())
                                                           .description("RNTBD response size (bytes)")
                                                           .baseUnit("bytes")
                                                           .tags(getEffectiveTags(tags, options))
                                                           .maximumExpectedValue(16_000_000d)
                                                           .publishPercentileHistogram(false)
                                                           .publishPercentiles()
                                                           .register(registry);

                    responseSize.record(requestRecord.responseLength());
                }
            } else {
                requestRecord.stop();
            }
        }
    }

    static class DescendantValidationResult {
        private final Instant expiration;
        private final boolean result;

        public DescendantValidationResult(Instant expiration, boolean result) {
            this.expiration = expiration;
            this.result = result;
        }

        public Instant getExpiration() {
            return this.expiration;
        }

        public boolean getResult() {
            return this.result;
        }
    }
}
