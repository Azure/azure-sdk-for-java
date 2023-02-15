// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.cosmos.implementation.clienttelemetry.TagName;

import java.util.EnumSet;
import java.util.Locale;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class CosmosMeterTagName extends ExpandableStringEnum<CosmosMeterTagName> {

    private EnumSet<TagName> tagNames;

    /**
     * Creates a new instance of {@link CosmosMeterTagName} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link CosmosMeterTagName} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    CosmosMeterTagName() {
    }

    /**
     * All possible tags
     */
    public static final CosmosMeterTagName ALL = fromString("All", CosmosMeterTagName.class)
        .setTagNames(TagName.ALL_TAGS);

    /**
     * Default tags
     */
    public static final CosmosMeterTagName DEFAULT = fromString("Default", CosmosMeterTagName.class)
        .setTagNames(TagName.DEFAULT_TAGS);

    /**
     * Minimum tags that are required and cannot be disabled
     */
    public static final CosmosMeterTagName MINIMUM = fromString("Minimum", CosmosMeterTagName.class)
        .setTagNames(TagName.MINIMUM_TAGS);

    /**
     * Effective Consistency model
     * Applicable to operations and requests
     */
    public static final CosmosMeterTagName CONSISTENCY_LEVEL =
        fromString("ConsistencyLevel", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.ConsistencyLevel));

    /**
     * Container identifier
     * applicable to operations and requests
     */
    public static final CosmosMeterTagName CONTAINER =
        fromString("Container", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.Container));

    /**
     * The service endpoint (hostname + port)
     * Applicable to requests, direct channel, direct endpoint and direct requests
     */
    public static final CosmosMeterTagName SERVICE_ENDPOINT =
        fromString("ServiceEndpoint", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.ServiceEndpoint));

    /**
     * The service endpoint (hostname + port, partitionId, replicaId)
     * Applicable to requests
     */
    public static final CosmosMeterTagName SERVICE_ADDRESS=
        fromString("ServiceAddress", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.ServiceAddress));

    /**
     * The region names of the regions handling the operation/request
     * Applicable to requests and operations
     */
    public static final CosmosMeterTagName REGION_NAME =
        fromString("RegionName", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.RegionName));

    /**
     * Operation status code.
     * Applicable to operations
     */
    public static final CosmosMeterTagName OPERATION_STATUS_CODE =
        fromString("OperationStatusCode", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.OperationStatusCode));

    /**
     * Operation type
     * Applicable to operations
     */
    public static final CosmosMeterTagName OPERATION =
        fromString("Operation", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.Operation));


    /**
     * Request status code
     * Applicable to requests
     */
    public static final CosmosMeterTagName REQUEST_STATUS_CODE =
        fromString("RequestStatusCode", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.RequestStatusCode));

    /**
     * Request operation type
     * Applicable to requests
     */
    public static final CosmosMeterTagName REQUEST_OPERATION_TYPE =
        fromString("RequestOperationType", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.RequestOperationType));

    /**
     * An identifier for the instance of the Cosmos client
     * Applicable to all meters
     */
    public static final CosmosMeterTagName CLIENT_CORRELATION_ID =
        fromString("ClientCorrelationId", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.ClientCorrelationId));

    /**
     * An indicator whether an address resolution refresh requested a cache refresh
     * Applicable to address resolutions
     */
    public static final CosmosMeterTagName ADDRESS_RESOLUTION_FORCED_REFRESH =
        fromString("IsForceRefresh", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.IsForceRefresh));

    /**
     * An indicator whether an address resolution refresh requested a collection routing map
     * cache refresh
     * Applicable to address resolutions
     */
    public static final CosmosMeterTagName ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH =
        fromString("IsForceCollectionRoutingMapRefresh", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.IsForceCollectionRoutingMapRefresh));

    /**
     * A numeric identifier for a physical partition
     * Applicable to operations and requests
     */
    public static final CosmosMeterTagName PARTITION_KEY_RANGE_ID =
        fromString("PartitionKeyRangeId", CosmosMeterTagName.class)
            .setTagNames(EnumSet.of(TagName.PartitionKeyRangeId));

    /**
     * Gets the corresponding metric category state from its string representation.
     *
     * @param name The name of the Cosmos metric category to convert.
     *
     * @return The corresponding Cosmos metric category.
     */
    public static CosmosMeterTagName fromString(String name) {
        checkNotNull(name, "Argument 'name' must not be null.");

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        switch (normalizedName) {
            case "all": return CosmosMeterTagName.ALL;
            case "default": return CosmosMeterTagName.DEFAULT;
            case "minimum": return CosmosMeterTagName.MINIMUM;
            case "consistencylevel": return CosmosMeterTagName.CONSISTENCY_LEVEL;
            case "container": return CosmosMeterTagName.CONTAINER;
            case "serviceaddress": return CosmosMeterTagName.SERVICE_ADDRESS;
            case "serviceendpoint": return CosmosMeterTagName.SERVICE_ENDPOINT;
            case "regionname": return CosmosMeterTagName.REGION_NAME;
            case "operationstatuscode": return CosmosMeterTagName.OPERATION_STATUS_CODE;
            case "operation": return CosmosMeterTagName.OPERATION;
            case "requeststatuscode": return CosmosMeterTagName.REQUEST_STATUS_CODE;
            case "requestoperationtype": return CosmosMeterTagName.REQUEST_OPERATION_TYPE;
            case "clientcorrelationid": return CosmosMeterTagName.CLIENT_CORRELATION_ID;
            case "isforcerefresh": return CosmosMeterTagName.ADDRESS_RESOLUTION_FORCED_REFRESH;
            case "isforcecollectionroutingmaprefresh":
                return CosmosMeterTagName.ADDRESS_RESOLUTION_COLLECTION_MAP_REFRESH;
            case "partitionkeyrangeid": return CosmosMeterTagName.PARTITION_KEY_RANGE_ID;

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
        for (CosmosMeterTagName t: CosmosMeterTagName.values(CosmosMeterTagName.class)) {
            sj.add(t.toString());
        }

        return sj.toString();
    }

    private CosmosMeterTagName setTagNames(EnumSet<TagName> tagNames) {
        this.tagNames = tagNames;
        return this;
    }

    EnumSet<TagName> getTagNames() {
        return this.tagNames;
    }
}
