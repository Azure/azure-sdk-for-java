// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Names of Cosmos DB client-side meters
 */
public final class CosmosMetricName extends ExpandableStringEnum<CosmosMetricName> {
    private CosmosMetricCategory meterCategory;

    /**
     * Creates a new instance of {@link CosmosMetricName} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CosmosMetricName} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    CosmosMetricName() {
    }


    /**
     * Number of operation calls (Counter)
     */
    public static final CosmosMetricName OPERATION_SUMMARY_CALLS =
        fromString(nameOf("op.calls"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.OPERATION_SUMMARY);

    /**
     * Total latency (across requests including retries) of the operation (Timer)
     */
    public static final CosmosMetricName OPERATION_SUMMARY_LATENCY =
        fromString(nameOf("op.latency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.OPERATION_SUMMARY);

    /**
     * Request charge for the operation (DistributionSummary)
     */
    public static final CosmosMetricName OPERATION_SUMMARY_REQUEST_CHARGE =
        fromString(nameOf("op.RUs"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.OPERATION_SUMMARY);

    /**
     * Number of regions contacted for processing the operation (DistributionSummary)
     */
    public static final CosmosMetricName OPERATION_DETAILS_REGIONS_CONTACTED =
        fromString(nameOf("op.regionsContacted"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.OPERATION_DETAILS);

    /**
     * Actual item count - relevant for non-point-operations - indicating the actual number of
     * docs returned in the response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName OPERATION_DETAILS_ACTUAL_ITEM_COUNT =
        fromString(nameOf("op.actualItemCount"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.OPERATION_DETAILS);

    /**
     * Max. item count - relevant for non-point-operations - indicating the requested max. number of
     * docs returned in a single response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName OPERATION_DETAILS_MAX_ITEM_COUNT =
        fromString(nameOf("op.maxItemCount"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.OPERATION_DETAILS);

    /**
     * Number of requests (Counter)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_REQUESTS =
        fromString(nameOf("req.rntbd.requests"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Latency of the request (Timer)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_LATENCY =
        fromString(nameOf("req.rntbd.latency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Backend-latency of the request (DistributionSummary)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_BACKEND_LATENCY =
        fromString(nameOf("req.rntbd.backendLatency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Request charge for a request (DistributionSummary)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_REQUEST_CHARGE =
        fromString(nameOf("req.rntbd.RUs"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Number of requests (Counter)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_GATEWAY_REQUESTS =
        fromString(nameOf("req.gw.requests"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Latency of the request (Timer)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_GATEWAY_LATENCY =
        fromString(nameOf("req.gw.latency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Request charge for a request (DistributionSummary)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE =
        fromString(nameOf("req.gw.RUs"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     *  Size of the request payload (DistributionSummary)
     *  NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_SIZE_REQUEST =
        fromString(nameOf("req.reqPayloadSize"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     *  Size of the response payload (DistributionSummary)
     *  NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_SIZE_RESPONSE =
        fromString(nameOf("req.rspPayloadSize"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Latency in different steps of the request pipeline (Timer)
     */
    public static final CosmosMetricName REQUEST_DETAILS_DIRECT_TIMELINE =
        fromString(nameOf("req.rntbd.timeline"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_DETAILS);

    /**
     * Latency in different steps of the request pipeline (Timer)
     */
    public static final CosmosMetricName REQUEST_DETAILS_GATEWAY_TIMELINE =
        fromString(nameOf("req.gw.timeline"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.REQUEST_DETAILS);

    /**
     * Number of acquired channels (new connections) for this endpoint (FunctionCounter)
     */
    public static final CosmosMetricName DIRECT_CHANNELS_ACQUIRED_COUNT =
        fromString(nameOf("rntbd.channels.acquired.count"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_CHANNELS);

    /**
     * Number of closed channels / connections for this endpoint (FunctionCounter)
     */
    public static final CosmosMetricName DIRECT_CHANNELS_CLOSED_COUNT =
        fromString(nameOf("rntbd.channels.closed.count"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_CHANNELS);

    /**
     * Snapshot of the number of available channels (active connections) for this endpoint (Gauge)
     */
    public static final CosmosMetricName DIRECT_CHANNELS_AVAILABLE_COUNT =
        fromString(nameOf("rntbd.channels.available.count"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_CHANNELS);

    /**
     * Snapshot of the number of endpoints (Gauge)
     */
    public static final CosmosMetricName DIRECT_ENDPOINTS_COUNT =
        fromString(nameOf("rntbd.endpoints.count"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_ENDPOINTS);

    /**
     * Number of evicted/closed endpoints (FunctionCounter)
     */
    public static final CosmosMetricName DIRECT_ENDPOINTS_EVICTED =
        fromString(nameOf("rntbd.endpoints.evicted"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_ENDPOINTS);

    /**
     * Number of RNTBD address resolution requests (Counter)
     */
    public static final CosmosMetricName DIRECT_ADDRESS_RESOLUTION_REQUESTS =
        fromString(nameOf("rntbd.addressResolution.requests"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS);

    /**
     * Latency of the RNTBD address resolution request (Timer)
     */
    public static final CosmosMetricName DIRECT_ADDRESS_RESOLUTION_LATENCY =
        fromString(nameOf("rntbd.addressResolution.latency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS);

    /**
     * Latency of RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMetricName DIRECT_REQUEST_LATENCY =
        fromString(nameOf("rntbd.requests.latency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Latency of failed RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMetricName DIRECT_REQUEST_LATENCY_FAILED =
        fromString(nameOf("rntbd.requests.failed.latency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Latency of successful RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMetricName DIRECT_REQUEST_LATENCY_SUCCESS =
        fromString(nameOf("rntbd.requests.successful.latency"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Snapshot of number of concurrent RNTBD requests for this endpoint (Gauge)
     */
    public static final CosmosMetricName DIRECT_REQUEST_CONCURRENT_COUNT =
        fromString(nameOf("rntbd.requests.concurrent.count"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Snapshot of number of queued RNTBD requests for this endpoint (Gauge)
     */
    public static final CosmosMetricName DIRECT_REQUEST_QUEUED_COUNT =
        fromString(nameOf("rntbd.requests.queued.count"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Size of the request payload (DistributionSummary)
     */
    public static final CosmosMetricName DIRECT_REQUEST_SIZE_REQUEST =
        fromString(nameOf("rntbd.req.reqSize"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Size of the response payload (DistributionSummary)
     */
    public static final CosmosMetricName DIRECT_REQUEST_SIZE_RESPONSE =
        fromString(nameOf("rntbd.req.rspSize"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Avg. system-wide CPU load (DistributionSummary)
     */
    public static final CosmosMetricName SYSTEM_CPU =
        fromString(nameOf("system.avgCpuLoad"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.SYSTEM);

    /**
     * JVM's Free available memory (DistributionSummary)
     */
    public static final CosmosMetricName SYSTEM_MEMORY_FREE =
        fromString(nameOf("system.freeMemoryAvailable"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.SYSTEM);

    /**
     * Distribution summary over snapshot of acquired channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMetricName LEGACY_DIRECT_ENDPOINT_STATISTICS_ACQUIRED =
        fromString(nameOf("req.rntbd.stats.endpoint.acquiredChannels"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.LEGACY);

    /**
     * Distribution summary over snapshot of available channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMetricName LEGACY_DIRECT_ENDPOINT_STATISTICS_AVAILABLE =
        fromString(nameOf("req.rntbd.stats.endpoint.availableChannels"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.LEGACY);

    /**
     * Distribution summary over snapshot of inflight channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMetricName LEGACY_DIRECT_ENDPOINT_STATISTICS_INFLIGHT =
        fromString(nameOf("req.rntbd.stats.endpoint.inflightRequests"), CosmosMetricName.class)
            .setCategory(CosmosMetricCategory.LEGACY);

    private final static Map<String, CosmosMetricName> meters = createMeterNameMap();

    /**
     * Gets the corresponding metric category state from its string representation.
     *
     * @param name The name of the Cosmos metric category to convert.
     *
     * @return The corresponding Cosmos metric category.
     */
    public static CosmosMetricName fromString(String name) {
        checkNotNull(name, "Argument 'name' must not be null.");

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        CosmosMetricName meterName = meters.getOrDefault(normalizedName, null);

        if (meterName == null) {
            String errorMessage = String.format(
                "Argument 'name' has invalid value '%s' - valid values are: %s",
                name,
                getValidValues());

            throw new IllegalArgumentException(errorMessage);
        }

        return meterName;
    }

    /**
     * Gets the meter category of the meter
     * @return the category of the meter
     */
    public CosmosMetricCategory getCategory() {
        return this.meterCategory;
    }

    private static Map<String, CosmosMetricName> createMeterNameMap() {
        Map<String, CosmosMetricName> map = new HashMap<>();
        map.put(nameOf("op.latency"), CosmosMetricName.OPERATION_SUMMARY_LATENCY);
        map.put(nameOf("op.calls"), CosmosMetricName.OPERATION_SUMMARY_CALLS);
        map.put(nameOf("op.rus"), CosmosMetricName.OPERATION_SUMMARY_REQUEST_CHARGE);
        map.put(nameOf("op.maxitemcount"), CosmosMetricName.OPERATION_DETAILS_MAX_ITEM_COUNT);
        map.put(nameOf("op.actualitemcount"), CosmosMetricName.OPERATION_DETAILS_ACTUAL_ITEM_COUNT);
        map.put(nameOf("op.regionscontacted"), CosmosMetricName.OPERATION_DETAILS_REGIONS_CONTACTED);
        map.put(nameOf("req.rntbd.requests"), CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUESTS);
        map.put(nameOf("req.rntbd.latency"), CosmosMetricName.REQUEST_SUMMARY_DIRECT_LATENCY);
        map.put(nameOf("req.rntbd.backendlatency"), CosmosMetricName.REQUEST_SUMMARY_DIRECT_BACKEND_LATENCY);
        map.put(nameOf("req.rntbd.rus"), CosmosMetricName.REQUEST_SUMMARY_DIRECT_REQUEST_CHARGE);
        map.put(nameOf("req.gw.requests"), CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS);
        map.put(nameOf("req.gw.latency"), CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY);
        map.put(nameOf("req.gw.rus"), CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE);
        map.put(nameOf("req.reqPayloadSize"), CosmosMetricName.REQUEST_SUMMARY_SIZE_REQUEST);
        map.put(nameOf("req.rspPayloadSize"), CosmosMetricName.REQUEST_SUMMARY_SIZE_RESPONSE);
        map.put(nameOf("req.rntbd.timeline"), CosmosMetricName.REQUEST_DETAILS_DIRECT_TIMELINE);
        map.put(nameOf("req.gw.timeline"), CosmosMetricName.REQUEST_DETAILS_GATEWAY_TIMELINE);
        map.put(nameOf("rntbd.channels.acquired.count"), CosmosMetricName.DIRECT_CHANNELS_ACQUIRED_COUNT);
        map.put(nameOf("rntbd.channels.available.count"), CosmosMetricName.DIRECT_CHANNELS_AVAILABLE_COUNT);
        map.put(nameOf("rntbd.channels.closed.count"), CosmosMetricName.DIRECT_CHANNELS_CLOSED_COUNT);
        map.put(nameOf("rntbd.endpoints.count"), CosmosMetricName.DIRECT_ENDPOINTS_COUNT);
        map.put(nameOf("rntbd.endpoints.evicted"), CosmosMetricName.DIRECT_ENDPOINTS_EVICTED);
        map.put(nameOf("rntbd.addressresolution.requests"), CosmosMetricName.DIRECT_ADDRESS_RESOLUTION_REQUESTS);
        map.put(nameOf("rntbd.addressresolution.latency"), CosmosMetricName.DIRECT_ADDRESS_RESOLUTION_LATENCY);
        map.put(nameOf("rntbd.requests.latency"), CosmosMetricName.DIRECT_REQUEST_LATENCY);
        map.put(nameOf("rntbd.requests.failed.latency"), CosmosMetricName.DIRECT_REQUEST_LATENCY_FAILED);
        map.put(nameOf("rntbd.requests.successful.latency"), CosmosMetricName.DIRECT_REQUEST_LATENCY_SUCCESS);
        map.put(nameOf("rntbd.requests.concurrent.count"), CosmosMetricName.DIRECT_REQUEST_CONCURRENT_COUNT);
        map.put(nameOf("rntbd.requests.queued.count"), CosmosMetricName.DIRECT_REQUEST_QUEUED_COUNT);
        map.put(nameOf("rntbd.req.reqsize"), CosmosMetricName.DIRECT_REQUEST_SIZE_REQUEST);
        map.put(nameOf("rntbd.req.rspsize"), CosmosMetricName.DIRECT_REQUEST_SIZE_RESPONSE);
        map.put(nameOf("system.freememoryavailable"), CosmosMetricName.SYSTEM_MEMORY_FREE);
        map.put(nameOf("system.avgcpuload"), CosmosMetricName.SYSTEM_CPU);
        map.put(
            nameOf("req.rntbd.stats.endpoint.acquiredchannels"),
            CosmosMetricName.LEGACY_DIRECT_ENDPOINT_STATISTICS_ACQUIRED);
        map.put(
            nameOf("req.rntbd.stats.endpoint.availablechannels"),
            CosmosMetricName.LEGACY_DIRECT_ENDPOINT_STATISTICS_AVAILABLE);
        map.put(
            nameOf("req.rntbd.stats.endpoint.inflightrequests"),
            CosmosMetricName.LEGACY_DIRECT_ENDPOINT_STATISTICS_INFLIGHT);

        return Collections.unmodifiableMap(map);
    }

    private static String getValidValues() {
        StringJoiner sj = new StringJoiner(", ");
        for (CosmosMetricName c: CosmosMetricName.values(CosmosMetricName.class)) {
            sj.add(c.toString());
        }

        return sj.toString();
    }

    private CosmosMetricName setCategory(CosmosMetricCategory meterCategory) {
        this.meterCategory = meterCategory;
        return this;
    }

    private static String nameOf(final String member) {
        return "cosmos.client." + member;
    }
}
