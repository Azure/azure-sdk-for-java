// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.cosmos.implementation.clienttelemetry.TagName;

import java.util.EnumSet;
import java.util.Locale;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * The tag names that can be used for Cosmos client-side meters. Not all tag names are applicable for all meters, but
 * this identifier can be used to tune which tags to use for individual meters or also define the default tags that
 * should be used when no meter-specific suppression exists.
 */
public final class CosmosMetricTagName extends ExpandableStringEnum<CosmosMetricTagName> {

    private EnumSet<TagName> tagNames;

    /**
     * Creates a new instance of {@link CosmosMetricTagName} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CosmosMetricTagName} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    CosmosMetricTagName() {
    }

    /**
     * All possible tags
     */
    public static final CosmosMetricTagName ALL = fromString("All", CosmosMetricTagName.class)
        .setTagNames(TagName.ALL_TAGS);

    /**
     * Default tags
     */
    public static final CosmosMetricTagName DEFAULT = fromString("Default", CosmosMetricTagName.class)
        .setTagNames(TagName.DEFAULT_TAGS);

    /**
     * Minimum tags that are required and cannot be disabled
     */
    public static final CosmosMetricTagName MINIMUM = fromString("Minimum", CosmosMetricTagName.class)
        .setTagNames(TagName.MINIMUM_TAGS);

    /**
     * Effective Consistency model
     * Applicable to operations and requests
     */
    public static final CosmosMetricTagName CONSISTENCY_LEVEL =
        fromString("ConsistencyLevel", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.ConsistencyLevel));

    /**
     * Container identifier
     * applicable to operations and requests
     */
    public static final CosmosMetricTagName CONTAINER =
        fromString("Container", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.Container));

    /**
     * The service endpoint (hostname + port)
     * Applicable to requests, direct channel, direct endpoint and direct requests
     */
    public static final CosmosMetricTagName SERVICE_ENDPOINT =
        fromString("ServiceEndpoint", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.ServiceEndpoint));

    /**
     * The service endpoint (hostname + port, partitionId, replicaId)
     * Applicable to requests
     */
    public static final CosmosMetricTagName SERVICE_ADDRESS=
        fromString("ServiceAddress", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.ServiceAddress));

    /**
     * The region names of the regions handling the operation/request
     * Applicable to requests and operations
     */
    public static final CosmosMetricTagName REGION_NAME =
        fromString("RegionName", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.RegionName));

    /**
     * Operation status code.
     * Applicable to operations
     */
    public static final CosmosMetricTagName OPERATION_STATUS_CODE =
        fromString("OperationStatusCode", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.OperationStatusCode));

    /**
     * Operation type
     * Applicable to operations
     */
    public static final CosmosMetricTagName OPERATION =
        fromString("Operation", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.Operation));


    /**
     * Request status code
     * Applicable to requests
     */
    public static final CosmosMetricTagName REQUEST_STATUS_CODE =
        fromString("RequestStatusCode", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.RequestStatusCode));

    /**
     * Request operation type
     * Applicable to requests
     */
    public static final CosmosMetricTagName REQUEST_OPERATION_TYPE =
        fromString("RequestOperationType", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.RequestOperationType));

    /**
     * An identifier for the instance of the Cosmos client
     * Applicable to all meters
     */
    public static final CosmosMetricTagName CLIENT_CORRELATION_ID =
        fromString("ClientCorrelationId", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.ClientCorrelationId));

    /**
     * An indicator whether an address resolution refresh requested a cache refresh
     * Applicable to address resolutions
     */
    public static final CosmosMetricTagName ADDRESS_RESOLUTION_FORCED_REFRESH =
        fromString("IsForceRefresh", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.IsForceRefresh));

    /**
     * An indicator whether an address resolution refresh requested a collection routing map
     * cache refresh
     * Applicable to address resolutions
     */
    public static final CosmosMetricTagName ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH =
        fromString("IsForceCollectionRoutingMapRefresh", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.IsForceCollectionRoutingMapRefresh));

    /**
     * A numeric identifier for a physical partition
     * Applicable to operations and requests
     */
    public static final CosmosMetricTagName PARTITION_KEY_RANGE_ID =
        fromString("PartitionKeyRangeId", CosmosMetricTagName.class)
            .setTagNames(EnumSet.of(TagName.PartitionKeyRangeId));

    /**
     * Gets the corresponding metric category state from its string representation.
     *
     * @param name The name of the Cosmos metric category to convert.
     *
     * @return The corresponding Cosmos metric category.
     */
    public static CosmosMetricTagName fromString(String name) {
        checkNotNull(name, "Argument 'name' must not be null.");

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        switch (normalizedName) {
            case "all": return CosmosMetricTagName.ALL;
            case "default": return CosmosMetricTagName.DEFAULT;
            case "minimum": return CosmosMetricTagName.MINIMUM;
            case "consistencylevel": return CosmosMetricTagName.CONSISTENCY_LEVEL;
            case "container": return CosmosMetricTagName.CONTAINER;
            case "serviceaddress": return CosmosMetricTagName.SERVICE_ADDRESS;
            case "serviceendpoint": return CosmosMetricTagName.SERVICE_ENDPOINT;
            case "regionname": return CosmosMetricTagName.REGION_NAME;
            case "operationstatuscode": return CosmosMetricTagName.OPERATION_STATUS_CODE;
            case "operation": return CosmosMetricTagName.OPERATION;
            case "requeststatuscode": return CosmosMetricTagName.REQUEST_STATUS_CODE;
            case "requestoperationtype": return CosmosMetricTagName.REQUEST_OPERATION_TYPE;
            case "clientcorrelationid": return CosmosMetricTagName.CLIENT_CORRELATION_ID;
            case "isforcerefresh": return CosmosMetricTagName.ADDRESS_RESOLUTION_FORCED_REFRESH;
            case "isforcecollectionroutingmaprefresh":
                return CosmosMetricTagName.ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH;
            case "partitionkeyrangeid": return CosmosMetricTagName.PARTITION_KEY_RANGE_ID;

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
        for (CosmosMetricTagName t: CosmosMetricTagName.values(CosmosMetricTagName.class)) {
            sj.add(t.toString());
        }

        return sj.toString();
    }

    private CosmosMetricTagName setTagNames(EnumSet<TagName> tagNames) {
        this.tagNames = tagNames;
        return this;
    }

    EnumSet<TagName> getTagNames() {
        return this.tagNames;
    }
}
