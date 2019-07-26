// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

public class CommonsBridgeInternal {
    public static boolean isV2(PartitionKeyDefinition pkd) {
        return pkd.version() != null && PartitionKeyDefinitionVersion.V2.val == pkd.version().val;
    }

    public static void setV2(PartitionKeyDefinition pkd) {
        pkd.version(PartitionKeyDefinitionVersion.V2);
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static String partitionKeyRangeIdInternal(FeedOptions options) {
        return options.partitionKeyRangeIdInternal();
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static String partitionKeyRangeIdInternal(ChangeFeedOptions options) {
        return options.partitionKeyRangeId();
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static FeedOptions partitionKeyRangeIdInternal(FeedOptions options, String partitionKeyRangeId) {
        return options.partitionKeyRangeIdInternal(partitionKeyRangeId);
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public static ChangeFeedOptions partitionKeyRangeIdInternal(ChangeFeedOptions options, String partitionKeyRangeId) {
        return options.partitionKeyRangeId(partitionKeyRangeId);
    }
}
