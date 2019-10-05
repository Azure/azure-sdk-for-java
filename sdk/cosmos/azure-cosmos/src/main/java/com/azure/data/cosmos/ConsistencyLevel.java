// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

/**
 * Represents the consistency levels supported for Cosmos DB client operations in the Azure Cosmos DB database service.
 * <p>
 * The requested ConsistencyLevel must match or be weaker than that provisioned for the database account. Consistency
 * levels by order of strength are STRONG, BOUNDED_STALENESS, SESSION and EVENTUAL.
 */
public enum ConsistencyLevel {

    /**
     * STRONG Consistency guarantees that read operations always return the value that was last written.
     */
    STRONG,

    /**
     * Bounded Staleness guarantees that reads are not too out-of-date. This can be configured based on number of
     * operations (MaxStalenessPrefix) or time (MaxStalenessIntervalInSeconds)
     */
    BOUNDED_STALENESS,

    /**
     * SESSION Consistency guarantees monotonic reads (you never read old data, then new, then old again), monotonic
     * writes (writes are ordered) and read your writes (your writes are immediately visible to your reads) within
     * any single session.
     */
    SESSION,

    /**
     * EVENTUAL Consistency guarantees that reads will return a subset of writes. ALL writes will be eventually be
     * available for reads.
     */
    EVENTUAL,

    /**
     * CONSISTENT_PREFIX Consistency guarantees that reads will return some prefix of all writes with no gaps. ALL writes
     * will be eventually be available for reads.
     */
    CONSISTENT_PREFIX;

    @Override
    public String toString() {
        return StringUtils.remove(WordUtils.capitalizeFully(this.name(), '_'), '_');        
    }
}
