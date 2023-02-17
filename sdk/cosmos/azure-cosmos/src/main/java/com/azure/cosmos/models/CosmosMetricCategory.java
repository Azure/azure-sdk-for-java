// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;

import java.util.EnumSet;
import java.util.Locale;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Categories for Cosmos DB client-side metrics
 */
public final class CosmosMetricCategory {

    private final String name;
    private final EnumSet<MetricCategory> metricCategories;

    private CosmosMetricCategory(String name, EnumSet<MetricCategory> metricCategories) {
        checkNotNull(name, "Argument 'name' must not be null.");
        checkNotNull(metricCategories, "Argument 'metricCategories' must not be null.");

        this.name = name;
        this.metricCategories = metricCategories;
    }

    /**
     * All metrics enabled
     */
    public static final CosmosMetricCategory ALL = new CosmosMetricCategory(
        "All",
        MetricCategory.ALL_CATEGORIES);

    /**
     * Default metrics (categories OperationSummary, RequestSummary, System, DirectChannels and DirectRequests) enabled.
     * These metrics provide good overview of end-to-end telemetry and help with triaging for most common issues
     */
    public static final CosmosMetricCategory DEFAULT = new CosmosMetricCategory(
        "Default",
        MetricCategory.DEFAULT_CATEGORIES);

    /**
     * Minimum set of metrics (categories OperationSummary and System) enabled.
     * These metrics provide a basic overview of end-to-end telemetry but won't be sufficient for triaging
     * most issues
     */
    public static final CosmosMetricCategory MINIMUM = new CosmosMetricCategory(
        "Minimum",
        EnumSet.of(MetricCategory.OperationSummary, MetricCategory.System));

    /**
     * The metrics in the OperationSummary category emit most important end-to-end metrics (like latency, request rate,
     * request charge, request- and response-payload size etc.) for SDK operations
     * These metrics are intended to visualize health state and impact - but alone not sufficient for triaging issues.
     */
    public static final CosmosMetricCategory OPERATION_SUMMARY = new CosmosMetricCategory(
        "OperationSummary",
        EnumSet.of(MetricCategory.OperationSummary));

    /**
     * The metrics in the OperationDetails category emit additional end-to-end metrics (like item count) for SDK
     * operations.
     */
    public static final CosmosMetricCategory OPERATION_DETAILS = new CosmosMetricCategory(
        "OperationDetails",
        EnumSet.of(MetricCategory.OperationDetails));

    /**
     * The metrics in the RequestSummary category emit most important end-to-end metrics (like latency, request rate,
     * request charge, request- and response-payload size etc.) for physical network requests - they have tags
     * allowing to distinguish by service endpoint in the backend as well as the client-machine. So, these metrics
     * can be very useful to triage whether impact (high latency, error rate) is skewed around certain client-machines
     * and/or backend service endpoints.
     */
    public static final CosmosMetricCategory REQUEST_SUMMARY = new CosmosMetricCategory(
        "RequestSummary",
        EnumSet.of(MetricCategory.RequestSummary));

    /**
     * The metrics in the RequestDetails category emit additional end-to-end metrics (like timeline metrics showing
     * where in the request pipeline latency was spent etc.) for physical network requests - they have tags
     * allowing to distinguish by service endpoint in the backend as well as the client-machine. So, these metrics
     * can be very useful to triage whether impact (high latency, error rate) is skewed around certain client-machines
     * and/or backend service endpoints.
     */
    public static final CosmosMetricCategory REQUEST_DETAILS = new CosmosMetricCategory(
        "RequestDetails",
        EnumSet.of(MetricCategory.RequestDetails));

    /**
     * The metrics in the AddressResolutions category emit metrics for calls made to get replica addresses for a
     * certain physical partition when using direct mode. A higher number of calls for a certain partition can
     * indicate either network/connectivity issues or the fact that at least one of the replica in this
     * partition has an issue.
     */
    public static final CosmosMetricCategory DIRECT_ADDRESS_RESOLUTIONS = new CosmosMetricCategory(
        "DirectAddressResolutions",
        EnumSet.of(MetricCategory.AddressResolutions));

    /**
     * The metrics in the DirectChannels category emit metrics allowing to monitor connection handling by service
     * endpoint. These metrics can be used to identify how many connections to a certain endpoint have been
     * established, closed or are currently active. This information can help triaging whether there are any
     * connectivity/network issues for certain endpoints (high number of closed/re-opened connections).
     */
    public static final CosmosMetricCategory DIRECT_CHANNELS = new CosmosMetricCategory(
        "DirectChannels",
        EnumSet.of(MetricCategory.DirectChannels));

    /**
     * The metrics in the DirectEndpoints category emit metrics allowing to monitor state by service
     * endpoint. These metrics can be used to identify when a service endpoint was evicted (due to reaching
     * idle time threshold etc.). In most cases it should be sufficient to monitor DirectChannels instead.
     */
    public static final CosmosMetricCategory DIRECT_ENDPOINTS = new CosmosMetricCategory(
        "DirectEndpoints",
        EnumSet.of(MetricCategory.DirectEndpoints));

    /**
     * The metrics in the DirectRequests category emit metrics allowing to monitor requests by service
     * endpoint (request rate, error rate, latency etc.). These metrics can be used to triage whether high latency or
     * error rate is caused by a certain endpoint.
     */
    public static final CosmosMetricCategory DIRECT_REQUESTS = new CosmosMetricCategory(
        "DirectRequests",
        EnumSet.of(MetricCategory.DirectRequests));

    /**
     * The metrics in the system category emit metrics that reflect system-wide CPU and memory usage based on the same
     * snapshots taken and logged in request diagnostics
     */
    public static final CosmosMetricCategory SYSTEM = new CosmosMetricCategory(
        "System",
        EnumSet.of(MetricCategory.System));

    /**
     * The metrics in the Legacy category emit metrics that should not be used anymore and exist only for
     * backwards compatibility reasons.
     */
    public static final CosmosMetricCategory LEGACY = new CosmosMetricCategory(
        "Legacy",
        EnumSet.of(MetricCategory.Legacy));

    /**
     * Gets the corresponding metric category state from its string representation.
     *
     * @param name The name of the Cosmos metric category to convert.
     *
     * @return The corresponding Cosmos metric category.
     */
    public static CosmosMetricCategory fromString(String name) {
        checkNotNull(name, "Argument 'name' must not be null.");

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        switch (normalizedName) {
            case "all": return CosmosMetricCategory.ALL;
            case "default": return CosmosMetricCategory.DEFAULT;
            case "minimum": return CosmosMetricCategory.MINIMUM;
            case "operationsummary": return CosmosMetricCategory.OPERATION_SUMMARY;
            case "operationdetails": return CosmosMetricCategory.OPERATION_DETAILS;
            case "requestsummary": return CosmosMetricCategory.REQUEST_SUMMARY;
            case "requestdetails": return CosmosMetricCategory.REQUEST_DETAILS;
            case "directaddressresolutions": return CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS;
            case "directchannels": return CosmosMetricCategory.DIRECT_CHANNELS;
            case "directendpoints": return CosmosMetricCategory.DIRECT_ENDPOINTS;
            case "directrequests": return CosmosMetricCategory.DIRECT_REQUESTS;
            case "legacy": return CosmosMetricCategory.LEGACY;
            case "system": return CosmosMetricCategory.SYSTEM;

            default:
                String errorMessage = String.format(
                    "Argument 'name' has invalid value '%s' - valid values are: %s",
                    name,
                    getValidValues());

                throw new IllegalArgumentException(errorMessage);
        }
    }

    private static String getValidValues() {
        return new StringJoiner(", ")
            .add(CosmosMetricCategory.ALL.name)
            .add(CosmosMetricCategory.DEFAULT.name)
            .add(CosmosMetricCategory.MINIMUM.name)
            .add(CosmosMetricCategory.OPERATION_SUMMARY.name)
            .add(CosmosMetricCategory.OPERATION_DETAILS.name)
            .add(CosmosMetricCategory.REQUEST_SUMMARY.name)
            .add(CosmosMetricCategory.REQUEST_DETAILS.name)
            .add(CosmosMetricCategory.DIRECT_ADDRESS_RESOLUTIONS.name)
            .add(CosmosMetricCategory.DIRECT_CHANNELS.name)
            .add(CosmosMetricCategory.DIRECT_ENDPOINTS.name)
            .add(CosmosMetricCategory.DIRECT_REQUESTS.name)
            .add(CosmosMetricCategory.LEGACY.name)
            .add(CosmosMetricCategory.SYSTEM.name)
            .toString();
    }

    EnumSet<MetricCategory> getCategories() {
        return this.metricCategories;
    }
}
