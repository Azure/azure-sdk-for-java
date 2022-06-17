// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the consistency levels supported for Azure Cosmos DB client operations in the Azure Cosmos DB service.
 * <p>
 * The requested ConsistencyLevel must match or be weaker than that provisioned for the database account. Consistency
 * levels by order of strength are STRONG, BOUNDED_STALENESS, SESSION and EVENTUAL.
 *
 * Refer to <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/consistency-levels">consistency level documentation</a> for additional details.
 */
public enum ConsistencyLevel {

    /**
     * STRONG Consistency guarantees that read operations always return the value that was last written.
     */
    STRONG("Strong"),

    /**
     * Bounded Staleness guarantees that reads are not too out-of-date. This can be configured based on number of
     * operations (MaxStalenessPrefix) or time (MaxStalenessIntervalInSeconds)
     */
    BOUNDED_STALENESS("BoundedStaleness"),

    /**
     * SESSION Consistency guarantees monotonic reads (you never read old data, then new, then old again), monotonic
     * writes (writes are ordered) and read your writes (your writes are immediately visible to your reads) within
     * any single session.
     */
    SESSION("Session"),

    /**
     * EVENTUAL Consistency guarantees that reads will return a subset of writes. ALL writes will be eventually be
     * available for reads.
     */
    EVENTUAL("Eventual"),

    /**
     * CONSISTENT_PREFIX Consistency guarantees that reads will return some prefix of all writes with no gaps. ALL
     * writes
     * will be eventually be available for reads.
     */
    CONSISTENT_PREFIX("ConsistentPrefix");

    private static final Map<String, ConsistencyLevel> consistencyLevelHashMap = new HashMap<>();

    static {
        for (ConsistencyLevel cl : ConsistencyLevel.values()) {
            consistencyLevelHashMap.put(cl.toString(), cl);
        }
    }

    private final String overWireValue;


    ConsistencyLevel(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    /**
     * Given the over wire version of ConsistencyLevel gives the corresponding enum or return null
     *
     * @param consistencyLevel String value of consistency level
     * @return ConsistencyLevel Enum consistency level
     */
    static ConsistencyLevel fromServiceSerializedFormat(String consistencyLevel) {
        // this is 100x faster than org.apache.commons.lang3.EnumUtils.getEnum(String)
        // for more detail refer to https://github.com/moderakh/azure-cosmosdb-benchmark
        return consistencyLevelHashMap.get(consistencyLevel);
    }

    @JsonValue
    @Override
    public String toString() {
        return this.overWireValue;
    }
}
