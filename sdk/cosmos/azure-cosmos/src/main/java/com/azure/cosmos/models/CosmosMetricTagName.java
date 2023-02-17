// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

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
public final class CosmosMetricTagName {

    private final String name;
    private final EnumSet<TagName> tagNames;

    private CosmosMetricTagName(String name, EnumSet<TagName> tagNames) {
        checkNotNull(name, "Argument 'name' must not be null.");
        checkNotNull(tagNames, "Argument 'tagNames' must not be null.");

        this.name = name;
        this.tagNames = tagNames;
    }

    /**
     * All possible tags
     */
    public static final CosmosMetricTagName ALL = new CosmosMetricTagName(
        "All",
        TagName.ALL_TAGS);

    /**
     * Default tags
     */
    public static final CosmosMetricTagName DEFAULT =  new CosmosMetricTagName(
        "Default",
        TagName.DEFAULT_TAGS);

    /**
     * Minimum tags that are required and cannot be disabled
     */
    public static final CosmosMetricTagName MINIMUM = new CosmosMetricTagName(
        "Minimum",
        TagName.MINIMUM_TAGS);

    /**
     * Effective Consistency model
     * Applicable to operations and requests
     */
    public static final CosmosMetricTagName CONSISTENCY_LEVEL =new CosmosMetricTagName(
        "ConsistencyLevel",
        EnumSet.of(TagName.ConsistencyLevel));

    /**
     * Container identifier
     * applicable to operations and requests
     */
    public static final CosmosMetricTagName CONTAINER =new CosmosMetricTagName(
        "Container",
        EnumSet.of(TagName.Container));

    /**
     * The service endpoint (hostname + port)
     * Applicable to requests, direct channel, direct endpoint and direct requests
     */
    public static final CosmosMetricTagName SERVICE_ENDPOINT = new CosmosMetricTagName(
        "ServiceEndpoint",
        EnumSet.of(TagName.ServiceEndpoint));

    /**
     * The service endpoint (hostname + port, partitionId, replicaId)
     * Applicable to requests
     */
    public static final CosmosMetricTagName SERVICE_ADDRESS = new CosmosMetricTagName(
        "ServiceAddress",
        EnumSet.of(TagName.ServiceAddress));

    /**
     * The region names of the regions handling the operation/request
     * Applicable to requests and operations
     */
    public static final CosmosMetricTagName REGION_NAME = new CosmosMetricTagName(
        "RegionName",
        EnumSet.of(TagName.RegionName));

    /**
     * Operation status code.
     * Applicable to operations
     */
    public static final CosmosMetricTagName OPERATION_STATUS_CODE = new CosmosMetricTagName(
        "OperationStatusCode",
        EnumSet.of(TagName.OperationStatusCode));

    /**
     * Operation type
     * Applicable to operations
     */
    public static final CosmosMetricTagName OPERATION = new CosmosMetricTagName(
        "Operation",
        EnumSet.of(TagName.Operation));


    /**
     * Request status code
     * Applicable to requests
     */
    public static final CosmosMetricTagName REQUEST_STATUS_CODE = new CosmosMetricTagName(
        "RequestStatusCode",
        EnumSet.of(TagName.RequestStatusCode));

    /**
     * Request operation type
     * Applicable to requests
     */
    public static final CosmosMetricTagName REQUEST_OPERATION_TYPE = new CosmosMetricTagName(
        "RequestOperationType",
        EnumSet.of(TagName.RequestOperationType));

    /**
     * An identifier for the instance of the Cosmos client
     * Applicable to all meters
     */
    public static final CosmosMetricTagName CLIENT_CORRELATION_ID = new CosmosMetricTagName(
        "ClientCorrelationId",
        EnumSet.of(TagName.ClientCorrelationId));

    /**
     * An indicator whether an address resolution refresh requested a cache refresh
     * Applicable to address resolutions
     */
    public static final CosmosMetricTagName ADDRESS_RESOLUTION_FORCED_REFRESH = new CosmosMetricTagName(
        "IsForceRefresh",
        EnumSet.of(TagName.IsForceRefresh));

    /**
     * An indicator whether an address resolution refresh requested a collection routing map
     * cache refresh
     * Applicable to address resolutions
     */
    public static final CosmosMetricTagName ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH = new CosmosMetricTagName(
        "IsForceCollectionRoutingMapRefresh",
        EnumSet.of(TagName.IsForceCollectionRoutingMapRefresh));

    /**
     * A numeric identifier for a physical partition
     * Applicable to operations and requests
     */
    public static final CosmosMetricTagName PARTITION_KEY_RANGE_ID = new CosmosMetricTagName(
        "PartitionKeyRangeId",
        EnumSet.of(TagName.PartitionKeyRangeId));

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
        return new StringJoiner(", ")
            .add(CosmosMetricTagName.ALL.name)
            .add(CosmosMetricTagName.DEFAULT.name)
            .add(CosmosMetricTagName.MINIMUM.name)
            .add(CosmosMetricTagName.CONSISTENCY_LEVEL.name)
            .add(CosmosMetricTagName.CONTAINER.name)
            .add(CosmosMetricTagName.SERVICE_ADDRESS.name)
            .add(CosmosMetricTagName.SERVICE_ENDPOINT.name)
            .add(CosmosMetricTagName.REGION_NAME.name)
            .add(CosmosMetricTagName.OPERATION_STATUS_CODE.name)
            .add(CosmosMetricTagName.OPERATION.name)
            .add(CosmosMetricTagName.REQUEST_STATUS_CODE.name)
            .add(CosmosMetricTagName.REQUEST_OPERATION_TYPE.name)
            .add(CosmosMetricTagName.CLIENT_CORRELATION_ID.name)
            .add(CosmosMetricTagName.ADDRESS_RESOLUTION_FORCED_REFRESH.name)
            .add(CosmosMetricTagName.ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH.name)
            .add(CosmosMetricTagName.PARTITION_KEY_RANGE_ID.name)
            .toString();
    }

    EnumSet<TagName> getTagNames() {
        return this.tagNames;
    }
}
