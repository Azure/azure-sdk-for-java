// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the read consistency strategies supported by the Azure Cosmos DB service.
 * <p>
 * The requested read consistency strategy can be chosen independent of the consistency level
 * provisioned for the database account.
 * <p>
 * NOTE: The ReadConsistencyStrategy is currently only working when using direct mode
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum ReadConsistencyStrategy {

    /**
     * Use the default read behavior for the consistency level applied to the operation, the client or the account
     */
    DEFAULT("Default"),

    /**
     * Eventual Consistency guarantees that reads will return a subset of writes. All writes
     * will be eventually be available for reads.
     */
    EVENTUAL("Eventual"),

    /**
     * Session Consistency guarantees monotonic reads (you never read old data, then new, then old again), monotonic
     * writes (writes are ordered) and read your writes (your writes are immediately visible to your reads) within
     * any single session.
     */
    SESSION("Session"),

    /**
     * Will read the latest committed version from the region in preferred order (which means the read region might
     * have stale data) but this read strategy will return the latest committed version of that region
     */
    LATEST_COMMITTED("LatestCommitted"),

    /**
     * Will do a quorum read followed by GCLSN barrier requests if needed.
     *
     * NOTE: Only supported for single-master accounts Strong consistency enabled.
     */
    GLOBAL_STRONG("GlobalStrong");

    private static Map<String, ReadConsistencyStrategy> readConsistencyStrategyHashMap = new HashMap<>();

    static {
        for (ReadConsistencyStrategy rcs : ReadConsistencyStrategy.values()) {
            readConsistencyStrategyHashMap.put(rcs.toString(), rcs);
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
            }
        );
    }

    static { initialize(); }
}
