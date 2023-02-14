// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.models.GeoObjectType;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;

import java.util.EnumSet;
import java.util.Locale;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Categories for Cosmos DB client-side metrics
 */
public class CosmosMetricCategory  extends ExpandableStringEnum<CosmosMetricCategory> {

    private EnumSet<MetricCategory> metricCategories;

    /**
     * Creates a new instance of {@link GeoObjectType} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link GeoObjectType} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    CosmosMetricCategory() {
    }

    /**
     * All metrics enabled
     */
    public static final CosmosMetricCategory ALL = fromString("All", CosmosMetricCategory.class)
        .setCategories(MetricCategory.ALL_CATEGORIES);

    /**
     * Default metrics (categories OperationSummary, RequestSummary, System, DirectChannels and DirectRequests) enabled.
     * These metrics provide good overview of end-to-end telemetry and help with triaging for most common issues
     */
    public static final CosmosMetricCategory DEFAULT = fromString("Default", CosmosMetricCategory.class)
        .setCategories(MetricCategory.DEFAULT_CATEGORIES);

    /**
     * Minimum set of metrics (categories OperationSummary and System) enabled.
     * These metrics provide a basic overview of end-to-end telemetry but won't be sufficient for triaging
     * most issues
     */
    public static final CosmosMetricCategory MINIMUM = fromString("Minimum", CosmosMetricCategory.class)
        .setCategories(EnumSet.of(MetricCategory.OperationSummary, MetricCategory.System));

    /**
     * The metrics in the OperationSummary category emit most important end-to-end metrics (like latency, request rate,
     * request charge, request- and response-payload size etc.) for SDK operations
     * These metrics are intended to visualize health state and impact - but alone not sufficient for triaging issues.
     */
    public static final CosmosMetricCategory OPERATION_SUMMARY =
        fromString("OperationSummary", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.OperationSummary));

    /**
     * The metrics in the OperationDetails category emit additional end-to-end metrics (like item count) for SDK
     * operations.
     */
    public static final CosmosMetricCategory OPERATION_DETAILS =
        fromString("OperationDetails", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.OperationDetails));

    /**
     * The metrics in the RequestSummary category emit most important end-to-end metrics (like latency, request rate,
     * request charge, request- and response-payload size etc.) for physical network requests - they have tags
     * allowing to distinguish by service endpoint in the backend as well as the client-machine. So, these metrics
     * can be very useful to triage whether impact (high latency, error rate) is skewed around certain client-machines
     * and/or backend service endpoints.
     */
    public static final CosmosMetricCategory REQUEST_SUMMARY =
        fromString("RequestSummary", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.RequestSummary));

    /**
     * The metrics in the RequestDetails category emit additional end-to-end metrics (like timeline metrics showing
     * where in the request pipeline latency was spent etc.) for physical network requests - they have tags
     * allowing to distinguish by service endpoint in the backend as well as the client-machine. So, these metrics
     * can be very useful to triage whether impact (high latency, error rate) is skewed around certain client-machines
     * and/or backend service endpoints.
     */
    public static final CosmosMetricCategory REQUEST_DETAILS =
        fromString("RequestDetails", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.RequestDetails));

    /**
     * The metrics in the AddressResolutions category emit metrics for calls made to get replica addresses for a
     * certain physical partition when using direct mode. A higher number of calls for a certain partition can
     * indicate either network/connectivity issues or the fact that at least one of the replica in this
     * partition has an issue.
     */
    public static final CosmosMetricCategory ADDRESS_RESOLUTIONS =
        fromString("AddressResolutions", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.AddressResolutions));

    /**
     * The metrics in the DirectChannels category emit metrics allowing to monitor connection handling by service
     * endpoint. These metrics can be used to identify how many connections to a certain endpoint have been
     * established, closed or are currently active. This information can help triaging whether there are any
     * connectivity/network issues for certain endpoints (high number of closed/re-opened connections).
     */
    public static final CosmosMetricCategory DIRECT_CHANNELS =
        fromString("DirectChannels", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.DirectChannels));

    /**
     * The metrics in the DirectEndpoints category emit metrics allowing to monitor state by service
     * endpoint. These metrics can be used to identify when a service endpoint was evicted (due to reaching
     * idle time threshold etc.). In most cases it should be sufficient to monitor DirectChannels instead.
     */
    public static final CosmosMetricCategory DIRECT_ENDPOINTS =
        fromString("DirectEndpoints", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.DirectEndpoints));

    /**
     * The metrics in the DirectRequests category emit metrics allowing to monitor requests by service
     * endpoint (request rate, error rate, latency etc.). These metrics can be used to triage whether high latency or
     * error rate is caused by a certain endpoint.
     */
    public static final CosmosMetricCategory DIRECT_REQUESTS =
        fromString("DirectRequests", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.DirectRequests));

    /**
     * The metrics in the Legacy category emit metrics that should not be used anymore and exist only for
     * backwards compatibility reasons.
     */
    public static final CosmosMetricCategory LEGACY =
        fromString("Legacy", CosmosMetricCategory.class)
            .setCategories(EnumSet.of(MetricCategory.Legacy));

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
            case "addressresolutions": return CosmosMetricCategory.ADDRESS_RESOLUTIONS;
            case "directchannels": return CosmosMetricCategory.DIRECT_CHANNELS;
            case "directendpoints": return CosmosMetricCategory.DIRECT_ENDPOINTS;
            case "directrequests": return CosmosMetricCategory.DIRECT_REQUESTS;
            case "legacy": return CosmosMetricCategory.LEGACY;

            default:
                String errorMessage = String.format(
                    "Argument 'name' has invalid value '%s' - valid values are: %s",
                    name,
                    getValidValues());

                throw new IllegalArgumentException(errorMessage);
        }
    }

    private static String getValidValues() {
        StringJoiner sj = new StringJoiner(", ");
        for (CosmosMetricCategory c: CosmosMetricCategory.values(CosmosMetricCategory.class)) {
            sj.add(c.toString());
        }

        return sj.toString();
    }

    private CosmosMetricCategory setCategories(EnumSet<MetricCategory> metricCategories) {
        this.metricCategories = metricCategories;
        return this;
    }

    EnumSet<MetricCategory> getCategories() {
        return this.metricCategories;
    }
}
