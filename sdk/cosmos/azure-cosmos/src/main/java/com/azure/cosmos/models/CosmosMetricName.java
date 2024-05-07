// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Names of Cosmos DB client-side meters
 */
public final class CosmosMetricName {
    private final String name;
    private final CosmosMetricCategory metricCategory;

    private CosmosMetricName(String name, CosmosMetricCategory metricCategory) {
        checkNotNull(name, "Argument 'name' must not be null.");
        checkNotNull(metricCategory, "Argument 'meterCategory' must not be null.");

        this.name = name;
        this.metricCategory = metricCategory;
    }


    /**
     * Number of operation calls (Counter)
     */
    public static final CosmosMetricName OPERATION_SUMMARY_CALLS = new CosmosMetricName(
        nameOf("op.calls"),
        CosmosMetricCategory.OPERATION_SUMMARY);

    /**
     * Total latency (across requests including retries) of the operation (Timer)
     */
    public static final CosmosMetricName OPERATION_SUMMARY_LATENCY = new CosmosMetricName(
        nameOf("op.latency"),
        CosmosMetricCategory.OPERATION_SUMMARY);

    /**
     * Request charge for the operation (DistributionSummary)
     */
    public static final CosmosMetricName OPERATION_SUMMARY_REQUEST_CHARGE = new CosmosMetricName(
        nameOf("op.RUs"),
        CosmosMetricCategory.OPERATION_SUMMARY);

    /**
     * Number of regions contacted for processing the operation (DistributionSummary)
     */
    public static final CosmosMetricName OPERATION_DETAILS_REGIONS_CONTACTED = new CosmosMetricName(
        nameOf("op.regionsContacted"),
        CosmosMetricCategory.OPERATION_DETAILS);

    /**
     * Actual item count - relevant for non-point-operations - indicating the actual number of
     * docs returned in the response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName OPERATION_DETAILS_ACTUAL_ITEM_COUNT = new CosmosMetricName(
        nameOf("op.actualItemCount"),
        CosmosMetricCategory.OPERATION_DETAILS);

    /**
     * Max. item count - relevant for non-point-operations - indicating the requested max. number of
     * docs returned in a single response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName OPERATION_DETAILS_MAX_ITEM_COUNT = new CosmosMetricName(
        nameOf("op.maxItemCount"),
        CosmosMetricCategory.OPERATION_DETAILS);

    /**
     * Number of requests (Counter)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_REQUESTS = new CosmosMetricName(
        nameOf("req.rntbd.requests"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Latency of the request (Timer)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_LATENCY = new CosmosMetricName(
        nameOf("req.rntbd.latency"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Backend-latency of the request (DistributionSummary)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_BACKEND_LATENCY = new CosmosMetricName(
        nameOf("req.rntbd.backendLatency"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Request charge for a request (DistributionSummary)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_REQUEST_CHARGE = new CosmosMetricName(
        nameOf("req.rntbd.RUs"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Actual item count - relevant for non-point-operations - indicating the actual number of
     * docs returned in the response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_DIRECT_ACTUAL_ITEM_COUNT = new CosmosMetricName(
        nameOf("req.rntbd.actualItemCount"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Number of requests (Counter)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_GATEWAY_REQUESTS = new CosmosMetricName(
        nameOf("req.gw.requests"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Latency of the request (Timer)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_GATEWAY_LATENCY =  new CosmosMetricName(
        nameOf("req.gw.latency"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Request charge for a request (DistributionSummary)
     */
    public static final CosmosMetricName REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE = new CosmosMetricName(
        nameOf("req.gw.RUs"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Actual item count - relevant for non-point-operations - indicating the actual number of
     * docs returned in the response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_GATEWAY_ACTUAL_ITEM_COUNT = new CosmosMetricName(
        nameOf("req.gw.actualItemCount"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     *  Size of the request payload (DistributionSummary)
     *  NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_SIZE_REQUEST = new CosmosMetricName(
        nameOf("req.reqPayloadSize"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     *  Size of the response payload (DistributionSummary)
     *  NOTE: No percentiles or histogram supported
     */
    public static final CosmosMetricName REQUEST_SUMMARY_SIZE_RESPONSE = new CosmosMetricName(
        nameOf("req.rspPayloadSize"),
        CosmosMetricCategory.REQUEST_SUMMARY);

    /**
     * Latency in different steps of the request pipeline (Timer)
     */
    public static final CosmosMetricName REQUEST_DETAILS_DIRECT_TIMELINE = new CosmosMetricName(
        nameOf("req.rntbd.timeline"),
        CosmosMetricCategory.REQUEST_DETAILS);

    /**
     * Latency in different steps of the request pipeline (Timer)
     */
    public static final CosmosMetricName REQUEST_DETAILS_GATEWAY_TIMELINE = new CosmosMetricName(
        nameOf("req.gw.timeline"),
        CosmosMetricCategory.REQUEST_DETAILS);

    /**
     * Number of acquired channels (new connections) for this endpoint (FunctionCounter)
     */
    public static final CosmosMetricName DIRECT_CHANNELS_ACQUIRED_COUNT = new CosmosMetricName(
        nameOf("rntbd.channels.acquired.count"),
        CosmosMetricCategory.DIRECT_CHANNELS);

    /**
     * Number of closed channels / connections for this endpoint (FunctionCounter)
     */
    public static final CosmosMetricName DIRECT_CHANNELS_CLOSED_COUNT = new CosmosMetricName(
        nameOf("rntbd.channels.closed.count"),
        CosmosMetricCategory.DIRECT_CHANNELS);

    /**
     * Snapshot of the number of available channels (active connections) for this endpoint (Gauge)
     */
    public static final CosmosMetricName DIRECT_CHANNELS_AVAILABLE_COUNT = new CosmosMetricName(
        nameOf("rntbd.channels.available.count"),
        CosmosMetricCategory.DIRECT_CHANNELS);

    /**
     * Snapshot of the number of endpoints (Gauge)
     */
    public static final CosmosMetricName DIRECT_ENDPOINTS_COUNT = new CosmosMetricName(
        nameOf("rntbd.endpoints.count"),
        CosmosMetricCategory.DIRECT_ENDPOINTS);

    /**
     * Number of evicted/closed endpoints (FunctionCounter)
     */
    public static final CosmosMetricName DIRECT_ENDPOINTS_EVICTED = new CosmosMetricName(
        nameOf("rntbd.endpoints.evicted"),
        CosmosMetricCategory.DIRECT_ENDPOINTS);

    /**
     * Number of RNTBD address resolution requests (Counter)
     */
    public static final CosmosMetricName DIRECT_ADDRESS_RESOLUTION_REQUESTS = new CosmosMetricName(
        nameOf("rntbd.addressResolution.requests"),
        CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS);

    /**
     * Latency of the RNTBD address resolution request (Timer)
     */
    public static final CosmosMetricName DIRECT_ADDRESS_RESOLUTION_LATENCY = new CosmosMetricName(
        nameOf("rntbd.addressResolution.latency"),
        CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS);

    /**
     * Latency of RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMetricName DIRECT_REQUEST_LATENCY = new CosmosMetricName(
        nameOf("rntbd.requests.latency"),
        CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Latency of failed RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMetricName DIRECT_REQUEST_LATENCY_FAILED = new CosmosMetricName(
        nameOf("rntbd.requests.failed.latency"),
        CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Latency of successful RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMetricName DIRECT_REQUEST_LATENCY_SUCCESS = new CosmosMetricName(
        nameOf("rntbd.requests.successful.latency"),
        CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Snapshot of number of concurrent RNTBD requests for this endpoint (Gauge)
     */
    public static final CosmosMetricName DIRECT_REQUEST_CONCURRENT_COUNT =  new CosmosMetricName(
        nameOf("rntbd.requests.concurrent.count"),
        CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Snapshot of number of queued RNTBD requests for this endpoint (Gauge)
     */
    public static final CosmosMetricName DIRECT_REQUEST_QUEUED_COUNT = new CosmosMetricName(
        nameOf("rntbd.requests.queued.count"),
        CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Size of the request payload (DistributionSummary)
     */
    public static final CosmosMetricName DIRECT_REQUEST_SIZE_REQUEST = new CosmosMetricName(
        nameOf("rntbd.req.reqSize"),
        CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Size of the response payload (DistributionSummary)
     */
    public static final CosmosMetricName DIRECT_REQUEST_SIZE_RESPONSE = new CosmosMetricName(
        nameOf("rntbd.req.rspSize"),
        CosmosMetricCategory.DIRECT_REQUESTS);

    /**
     * Avg. system-wide CPU load (DistributionSummary)
     */
    public static final CosmosMetricName SYSTEM_CPU = new CosmosMetricName(
        nameOf("system.avgCpuLoad"),
        CosmosMetricCategory.SYSTEM);

    /**
     * JVM's Free available memory (DistributionSummary)
     */
    public static final CosmosMetricName SYSTEM_MEMORY_FREE = new CosmosMetricName(
        nameOf("system.freeMemoryAvailable"),
        CosmosMetricCategory.SYSTEM);

    /**
     * Distribution summary over snapshot of acquired channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMetricName LEGACY_DIRECT_ENDPOINT_STATISTICS_ACQUIRED = new CosmosMetricName(
        nameOf("req.rntbd.stats.endpoint.acquiredChannels"),
        CosmosMetricCategory.LEGACY);

    /**
     * Distribution summary over snapshot of available channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMetricName LEGACY_DIRECT_ENDPOINT_STATISTICS_AVAILABLE = new CosmosMetricName(
        nameOf("req.rntbd.stats.endpoint.availableChannels"),
        CosmosMetricCategory.LEGACY);

    /**
     * Distribution summary over snapshot of inflight channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMetricName LEGACY_DIRECT_ENDPOINT_STATISTICS_INFLIGHT = new CosmosMetricName(
        nameOf("req.rntbd.stats.endpoint.inflightRequests"),
        CosmosMetricCategory.LEGACY);

    // NOTE - it is important to declare this field after all the Factory properties above
    // In java static fields are first set to default value - then in the order of the
    // declarations the assignment/initialization is executed.
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
        return this.metricCategory;
    }

    @Override
    @JsonValue
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(CosmosMetricName.class, this.name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!CosmosMetricName.class.isAssignableFrom(obj.getClass())) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (this.name == null) {
            return ((CosmosMetricName) obj).name == null;
        } else {
            return this.name.equals(((CosmosMetricName) obj).name);
        }
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
        map.put(nameOf("req.rntbd.actualitemcount"), CosmosMetricName.REQUEST_SUMMARY_DIRECT_ACTUAL_ITEM_COUNT);
        map.put(nameOf("req.gw.requests"), CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUESTS);
        map.put(nameOf("req.gw.latency"), CosmosMetricName.REQUEST_SUMMARY_GATEWAY_LATENCY);
        map.put(nameOf("req.gw.rus"), CosmosMetricName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE);
        map.put(nameOf("req.gw.actualitemcount"), CosmosMetricName.REQUEST_SUMMARY_GATEWAY_ACTUAL_ITEM_COUNT);
        map.put(nameOf("req.reqpayloadsize"), CosmosMetricName.REQUEST_SUMMARY_SIZE_REQUEST);
        map.put(nameOf("req.rsppayloadsize"), CosmosMetricName.REQUEST_SUMMARY_SIZE_RESPONSE);
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
        for (CosmosMetricName metric: meters.values()) {
            sj.add(metric.name);
        }

        return sj.toString();
    }

    private static String nameOf(final String member) {
        return "cosmos.client." + member;
    }
}
