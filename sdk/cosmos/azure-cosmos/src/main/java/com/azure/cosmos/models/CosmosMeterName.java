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

public final class CosmosMeterName extends ExpandableStringEnum<CosmosMeterName> {

    private final static Map<String, CosmosMeterName> meters = createMeterNameMap();

    private CosmosMeterCategory meterCategory;

    /**
     * Creates a new instance of {@link CosmosMeterName} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CosmosMeterName} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    CosmosMeterName() {
    }


    /**
     * Number of operation calls (Counter)
     */
    public static final CosmosMeterName OPERATION_SUMMARY_CALLS =
        fromString(nameOf("op.calls"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.OPERATION_SUMMARY);

    /**
     * Total latency (across requests including retries) of the operation (Timer)
     */
    public static final CosmosMeterName OPERATION_SUMMARY_LATENCY =
        fromString(nameOf("op.latency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.OPERATION_SUMMARY);

    /**
     * Request charge for the operation (DistributionSummary)
     */
    public static final CosmosMeterName OPERATION_SUMMARY_REQUEST_CHARGE =
        fromString(nameOf("op.RUs"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.OPERATION_SUMMARY);

    /**
     * Number of regions contacted for processing the operation (DistributionSummary)
     */
    public static final CosmosMeterName OPERATION_DETAILS_REGIONS_CONTACTED =
        fromString(nameOf("op.regionsContacted"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.OPERATION_DETAILS);

    /**
     * Actual item count - relevant for non-point-operations - indicating the actual number of
     * docs returned in the response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMeterName OPERATION_DETAILS_ACTUAL_ITEM_COUNT =
        fromString(nameOf("op.actualItemCount"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.OPERATION_DETAILS);

    /**
     * Max. item count - relevant for non-point-operations - indicating the requested max. number of
     * docs returned in a single response (DistributionSummary)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMeterName OPERATION_DETAILS_MAX_ITEM_COUNT =
        fromString(nameOf("op.maxItemCount"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.OPERATION_DETAILS);

    /**
     * Number of requests (Counter)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMeterName REQUEST_SUMMARY_DIRECT_REQUESTS =
        fromString(nameOf("req.rntbd.requests"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     * Latency of the request (Timer)
     */
    public static final CosmosMeterName REQUEST_SUMMARY_DIRECT_LATENCY =
        fromString(nameOf("req.rntbd.latency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     * Backend-latency of the request (DistributionSummary)
     */
    public static final CosmosMeterName REQUEST_SUMMARY_DIRECT_BACKEND_LATENCY =
        fromString(nameOf("req.rntbd.backendLatency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     * Request charge for a request (DistributionSummary)
     */
    public static final CosmosMeterName REQUEST_SUMMARY_DIRECT_REQUEST_CHARGE =
        fromString(nameOf("req.rntbd.RUs"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     * Number of requests (Counter)
     * NOTE: No percentiles or histogram supported
     */
    public static final CosmosMeterName REQUEST_SUMMARY_GATEWAY_REQUESTS =
        fromString(nameOf("req.gw.requests"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     * Latency of the request (Timer)
     */
    public static final CosmosMeterName REQUEST_SUMMARY_GATEWAY_LATENCY =
        fromString(nameOf("req.gw.latency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     * Request charge for a request (DistributionSummary)
     */
    public static final CosmosMeterName REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE =
        fromString(nameOf("req.gw.RUs"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     *  Size of the request payload (DistributionSummary)
     *  NOTE: No percentiles or histogram supported
     */
    public static final CosmosMeterName REQUEST_SUMMARY_SIZE_REQUEST =
        fromString(nameOf("req.reqPayloadSize"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     *  Size of the response payload (DistributionSummary)
     *  NOTE: No percentiles or histogram supported
     */
    public static final CosmosMeterName REQUEST_SUMMARY_SIZE_RESPONSE =
        fromString(nameOf("req.rspPayloadSize"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_SUMMARY);

    /**
     * Latency in different steps of the request pipeline (Timer)
     */
    public static final CosmosMeterName REQUEST_DETAILS_DIRECT_TIMELINE =
        fromString(nameOf("req.rntbd.timeline"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_DETAILS);

    /**
     * Latency in different steps of the request pipeline (Timer)
     */
    public static final CosmosMeterName REQUEST_DETAILS_GATEWAY_TIMELINE =
        fromString(nameOf("req.gw.timeline"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.REQUEST_DETAILS);

    /**
     * Number of acquired channels (new connections) for this endpoint (FunctionCounter)
     */
    public static final CosmosMeterName DIRECT_CHANNELS_ACQUIRED_COUNT =
        fromString(nameOf("rntbd.channels.acquired.count"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_CHANNELS);

    /**
     * Number of closed channels / connections for this endpoint (FunctionCounter)
     */
    public static final CosmosMeterName DIRECT_CHANNELS_CLOSED_COUNT =
        fromString(nameOf("rntbd.channels.closed.count"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_CHANNELS);

    /**
     * Snapshot of the number of available channels (active connections) for this endpoint (Gauge)
     */
    public static final CosmosMeterName DIRECT_CHANNELS_AVAILABLE_COUNT =
        fromString(nameOf("rntbd.channels.available.count"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_CHANNELS);

    /**
     * Snapshot of the number of endpoints (Gauge)
     */
    public static final CosmosMeterName DIRECT_ENDPOINTS_COUNT =
        fromString(nameOf("rntbd.endpoints.count"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_ENDPOINTS);

    /**
     * Number of evicted/closed endpoints (FunctionCounter)
     */
    public static final CosmosMeterName DIRECT_ENDPOINTS_EVICTED =
        fromString(nameOf("rntbd.endpoints.evicted"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_ENDPOINTS);

    /**
     * Number of RNTBD address resolution requests (Counter)
     */
    public static final CosmosMeterName DIRECT_ADDRESS_RESOLUTION_REQUESTS =
        fromString(nameOf("rntbd.addressResolution.requests"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_ADDRESS_RESOLUTIONS);

    /**
     * Latency of the RNTBD address resolution request (Timer)
     */
    public static final CosmosMeterName DIRECT_ADDRESS_RESOLUTION_LATENCY =
        fromString(nameOf("rntbd.addressResolution.latency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_ADDRESS_RESOLUTIONS);

    /**
     * Latency of RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMeterName DIRECT_REQUEST_LATENCY =
        fromString(nameOf("rntbd.requests.latency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_REQUESTS);

    /**
     * Latency of failed RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMeterName DIRECT_REQUEST_LATENCY_FAILED =
        fromString(nameOf("rntbd.requests.failed.latency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_REQUESTS);

    /**
     * Latency of successful RNTBD requests for this endpoint (Timer)
     */
    public static final CosmosMeterName DIRECT_REQUEST_LATENCY_SUCCESS =
        fromString(nameOf("rntbd.requests.successful.latency"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_REQUESTS);

    /**
     * Snapshot of number of concurrent RNTBD requests for this endpoint (Gauge)
     */
    public static final CosmosMeterName DIRECT_REQUEST_CONCURRENT_COUNT =
        fromString(nameOf("rntbd.requests.concurrent.count"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_REQUESTS);

    /**
     * Snapshot of number of queued RNTBD requests for this endpoint (Gauge)
     */
    public static final CosmosMeterName DIRECT_REQUEST_QUEUED_COUNT =
        fromString(nameOf("rntbd.requests.queued.count"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_REQUESTS);

    /**
     * Size of the request payload (DistributionSummary)
     */
    public static final CosmosMeterName DIRECT_REQUEST_SIZE_REQUEST =
        fromString(nameOf("rntbd.req.reqSize"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_REQUESTS);

    /**
     * Size of the response payload (DistributionSummary)
     */
    public static final CosmosMeterName DIRECT_REQUEST_SIZE_RESPONSE =
        fromString(nameOf("rntbd.req.rspSize"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.DIRECT_REQUESTS);

    /**
     * Avg. system-wide CPU load (DistributionSummary)
     */
    public static final CosmosMeterName SYSTEM_CPU =
        fromString(nameOf("system.avgCpuLoad"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.SYSTEM);

    /**
     * JVM's Free available memory (DistributionSummary)
     */
    public static final CosmosMeterName SYSTEM_MEMORY_FREE =
        fromString(nameOf("system.freeMemoryAvailable"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.SYSTEM);

    /**
     * Distribution summary over snapshot of acquired channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMeterName LEGACY_DIRECT_ENDPOINT_STATISTICS_ACQUIRED =
        fromString(nameOf("req.rntbd.stats.endpoint.acquiredChannels"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.LEGACY);

    /**
     * Distribution summary over snapshot of available channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMeterName LEGACY_DIRECT_ENDPOINT_STATISTICS_AVAILABLE =
        fromString(nameOf("req.rntbd.stats.endpoint.availableChannels"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.LEGACY);

    /**
     * Distribution summary over snapshot of inflight channels for the endpoint at time
     * of a request (DistributionSummary)
     */
    public static final CosmosMeterName LEGACY_DIRECT_ENDPOINT_STATISTICS_INFLIGHT =
        fromString(nameOf("req.rntbd.stats.endpoint.inflightRequests"), CosmosMeterName.class)
            .setCategory(CosmosMeterCategory.LEGACY);

    /**
     * Gets the corresponding metric category state from its string representation.
     *
     * @param name The name of the Cosmos metric category to convert.
     *
     * @return The corresponding Cosmos metric category.
     */
    public static CosmosMeterName fromString(String name) {
        checkNotNull(name, "Argument 'name' must not be null.");

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        CosmosMeterName meterName = meters.getOrDefault(normalizedName, null);

        if (meterName == null) {
            String errorMessage = String.format(
                "Argument 'name' has invalid value '%s' - valid values are: %s",
                name,
                getValidValues());

            throw new IllegalArgumentException(errorMessage);
        }

        return meterName;
    }

    private static Map<String, CosmosMeterName> createMeterNameMap() {
        Map<String, CosmosMeterName> map = new HashMap<>();
        map.put(nameOf("op.latency"), CosmosMeterName.OPERATION_SUMMARY_LATENCY);
        map.put(nameOf("op.calls"), CosmosMeterName.OPERATION_SUMMARY_CALLS);
        map.put(nameOf("op.rus"), CosmosMeterName.OPERATION_SUMMARY_REQUEST_CHARGE);
        map.put(nameOf("op.maxitemcount"), CosmosMeterName.OPERATION_DETAILS_MAX_ITEM_COUNT);
        map.put(nameOf("op.actualitemcount"), CosmosMeterName.OPERATION_DETAILS_ACTUAL_ITEM_COUNT);
        map.put(nameOf("op.regionscontacted"), CosmosMeterName.OPERATION_DETAILS_REGIONS_CONTACTED);
        map.put(nameOf("req.rntbd.requests"), CosmosMeterName.REQUEST_SUMMARY_DIRECT_REQUESTS);
        map.put(nameOf("req.rntbd.latency"), CosmosMeterName.REQUEST_SUMMARY_DIRECT_LATENCY);
        map.put(nameOf("req.rntbd.backendlatency"), CosmosMeterName.REQUEST_SUMMARY_DIRECT_BACKEND_LATENCY);
        map.put(nameOf("req.rntbd.rus"), CosmosMeterName.REQUEST_SUMMARY_DIRECT_REQUEST_CHARGE);
        map.put(nameOf("req.gw.requests"), CosmosMeterName.REQUEST_SUMMARY_GATEWAY_REQUESTS);
        map.put(nameOf("req.gw.latency"), CosmosMeterName.REQUEST_SUMMARY_GATEWAY_LATENCY);
        map.put(nameOf("req.gw.rus"), CosmosMeterName.REQUEST_SUMMARY_GATEWAY_REQUEST_CHARGE);
        map.put(nameOf("req.reqPayloadSize"), CosmosMeterName.REQUEST_SUMMARY_SIZE_REQUEST);
        map.put(nameOf("req.rspPayloadSize"), CosmosMeterName.REQUEST_SUMMARY_SIZE_RESPONSE);
        map.put(nameOf("req.rntbd.timeline"), CosmosMeterName.REQUEST_DETAILS_DIRECT_TIMELINE);
        map.put(nameOf("req.gw.timeline"), CosmosMeterName.REQUEST_DETAILS_GATEWAY_TIMELINE);
        map.put(nameOf("rntbd.channels.acquired.count"), CosmosMeterName.DIRECT_CHANNELS_ACQUIRED_COUNT);
        map.put(nameOf("rntbd.channels.available.count"), CosmosMeterName.DIRECT_CHANNELS_AVAILABLE_COUNT);
        map.put(nameOf("rntbd.channels.closed.count"), CosmosMeterName.DIRECT_CHANNELS_CLOSED_COUNT);
        map.put(nameOf("rntbd.endpoints.count"), CosmosMeterName.DIRECT_ENDPOINTS_COUNT);
        map.put(nameOf("rntbd.endpoints.evicted"), CosmosMeterName.DIRECT_ENDPOINTS_EVICTED);
        map.put(nameOf("rntbd.addressresolution.requests"), CosmosMeterName.DIRECT_ADDRESS_RESOLUTION_REQUESTS);
        map.put(nameOf("rntbd.addressresolution.latency"), CosmosMeterName.DIRECT_ADDRESS_RESOLUTION_LATENCY);
        map.put(nameOf("rntbd.requests.latency"), CosmosMeterName.DIRECT_REQUEST_LATENCY);
        map.put(nameOf("rntbd.requests.failed.latency"), CosmosMeterName.DIRECT_REQUEST_LATENCY_FAILED);
        map.put(nameOf("rntbd.requests.successful.latency"), CosmosMeterName.DIRECT_REQUEST_LATENCY_SUCCESS);
        map.put(nameOf("rntbd.requests.concurrent.count"), CosmosMeterName.DIRECT_REQUEST_CONCURRENT_COUNT);
        map.put(nameOf("rntbd.requests.queued.count"), CosmosMeterName.DIRECT_REQUEST_QUEUED_COUNT);
        map.put(nameOf("rntbd.req.reqsize"), CosmosMeterName.DIRECT_REQUEST_SIZE_REQUEST);
        map.put(nameOf("rntbd.req.rspsize"), CosmosMeterName.DIRECT_REQUEST_SIZE_RESPONSE);
        map.put(nameOf("system.freememoryavailable"), CosmosMeterName.SYSTEM_MEMORY_FREE);
        map.put(nameOf("system.avgcpuload"), CosmosMeterName.SYSTEM_CPU);
        map.put(
            nameOf("req.rntbd.stats.endpoint.acquiredchannels"),
            CosmosMeterName.LEGACY_DIRECT_ENDPOINT_STATISTICS_ACQUIRED);
        map.put(
            nameOf("req.rntbd.stats.endpoint.availablechannels"),
            CosmosMeterName.LEGACY_DIRECT_ENDPOINT_STATISTICS_AVAILABLE);
        map.put(
            nameOf("req.rntbd.stats.endpoint.inflightrequests"),
            CosmosMeterName.LEGACY_DIRECT_ENDPOINT_STATISTICS_INFLIGHT);

        return Collections.unmodifiableMap(map);
    }

    private static String getValidValues() {
        StringJoiner sj = new StringJoiner(", ");
        for (CosmosMeterName c: CosmosMeterName.values(CosmosMeterName.class)) {
            sj.add(c.toString());
        }

        return sj.toString();
    }

    private CosmosMeterName setCategory(CosmosMeterCategory meterCategory) {
        this.meterCategory = meterCategory;
        return this;
    }

    public CosmosMeterCategory getCategory() {
        return this.meterCategory;
    }

    private static String nameOf(final String member) {
        return "cosmos.client." + member;
    }
}
