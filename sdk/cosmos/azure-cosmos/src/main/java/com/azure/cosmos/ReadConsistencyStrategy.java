// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the read consistency strategies supported by the Azure Cosmos DB service.
 * <p>
 * A {@code ReadConsistencyStrategy} (RCS) allows choosing the read consistency behavior independent
 * of the default consistency level configured for the database account. It can be set at the client
 * level via {@link CosmosClientBuilder#readConsistencyStrategy(ReadConsistencyStrategy)} or at
 * the request level via request options (e.g.,
 * {@link com.azure.cosmos.models.CosmosItemRequestOptions#setReadConsistencyStrategy(ReadConsistencyStrategy)}).
 * <p>
 * <b>Precedence:</b> When both {@link ConsistencyLevel} and RCS are set, RCS takes precedence
 * and {@code ConsistencyLevel} is ignored. When RCS is set to {@link #DEFAULT}, the configured
 * {@code ConsistencyLevel} (or the account default) applies normally.
 * <p>
 * Request-level RCS overrides client-level RCS.
 * <p>
 * RCS only applies to read operations on documents. Write operations always use {@link #DEFAULT}
 * regardless of the configured strategy.
 * <p>
 * Supported in all connection modes: Direct, Gateway, and thin client (Gateway V2).
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum ReadConsistencyStrategy {

    /**
     * Uses the default read behavior based on the {@link ConsistencyLevel} applied to the
     * operation, the client, or the account. When this value is set, {@code ReadConsistencyStrategy}
     * is effectively transparent.
     */
    DEFAULT("Default"),

    /**
     * Reads may return a subset of writes. All writes will eventually be available for reads.
     */
    EVENTUAL("Eventual"),

    /**
     * Guarantees monotonic reads, monotonic writes, and read-your-writes within a single session.
     */
    SESSION("Session"),

    /**
     * Reads the latest committed version from the preferred read region. The region may have
     * replication lag, but this strategy returns the most recent version that region has committed.
     */
    LATEST_COMMITTED("LatestCommitted"),

    /**
     * Reads the latest version across all regions. Because replication with global strong consistency
     * is synchronous, this guarantees the most recently written version is returned.
     *
     * <p>Only supported for single-master accounts with {@link ConsistencyLevel#STRONG} as the
     * default consistency level. A {@code BadRequestException} is thrown at request time if used
     * on an account that does not meet this requirement.
     */
    GLOBAL_STRONG("GlobalStrong");

    private static Map<String, ReadConsistencyStrategy> readConsistencyStrategyHashMap = new HashMap<>();

    static {
        for (ReadConsistencyStrategy readConsistencyStrategy : ReadConsistencyStrategy.values()) {
            readConsistencyStrategyHashMap.put(readConsistencyStrategy.toString(), readConsistencyStrategy);
        }
    }

    private final String overWireValue;

    ReadConsistencyStrategy(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    /**
     * Given the over wire version of ConsistencyLevel gives the corresponding enum or return null
     *
     * @param readConsistencyStrategy String value of read consistency strategy
     * @return ReadConsistencyStrategy Enum read consistency strategy
     */
    static ReadConsistencyStrategy fromServiceSerializedFormat(String readConsistencyStrategy) {
        // this is 100x faster than org.apache.commons.lang3.EnumUtils.getEnum(String)
        // for more detail refer to https://github.com/moderakh/azure-cosmosdb-benchmark
        return readConsistencyStrategyHashMap.get(readConsistencyStrategy);
    }

    @JsonValue
    @Override
    public String toString() {
        return this.overWireValue;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.ReadConsistencyStrategyHelper.setReadConsistencyStrategyAccessor(
            new ImplementationBridgeHelpers.ReadConsistencyStrategyHelper.ReadConsistencyStrategyAccessor() {

                @Override
                public ReadConsistencyStrategy createFromServiceSerializedFormat(String serviceSerializedFormat) {
                    return ReadConsistencyStrategy.fromServiceSerializedFormat(serviceSerializedFormat);
                }

                public ReadConsistencyStrategy getEffectiveReadConsistencyStrategy(
                    ResourceType resourceType,
                    OperationType operationType,
                    ReadConsistencyStrategy desiredReadConsistencyStrategyOfOperation,
                    ReadConsistencyStrategy clientLevelReadConsistencyStrategy) {

                    if (resourceType != ResourceType.Document) {
                        return ReadConsistencyStrategy.DEFAULT;
                    }

                    if (operationType.isWriteOperation()) {
                        return ReadConsistencyStrategy.DEFAULT;
                    }

                    if (desiredReadConsistencyStrategyOfOperation != null) {
                        return desiredReadConsistencyStrategyOfOperation;
                    }

                    if (clientLevelReadConsistencyStrategy != null) {
                        return clientLevelReadConsistencyStrategy;
                    }

                    return ReadConsistencyStrategy.DEFAULT;
                }
            }
        );
    }

    static { initialize(); }
}
