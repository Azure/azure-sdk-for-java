// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

public class CommonsBridgeInternal {
    public static boolean isV2(PartitionKeyDefinition pkd) {
        return pkd.getVersion() != null && PartitionKeyDefinitionVersion.V2.val == pkd.getVersion().val;
    }

    public static void setV2(PartitionKeyDefinition pkd) {
        pkd.setVersion(PartitionKeyDefinitionVersion.V2);
    }

    /**
     * Gets the getPartitionKeyRangeId.
     *
     * @return the getPartitionKeyRangeId.
     */
    public static String partitionKeyRangeIdInternal(FeedOptions options) {
        return options.getPartitionKeyRangeIdInternal();
    }

    /**
     * Gets the getPartitionKeyRangeId.
     *
     * @return the getPartitionKeyRangeId.
     */
    public static String partitionKeyRangeIdInternal(ChangeFeedOptions options) {
        return options.getPartitionKeyRangeId();
    }

    /**
     * Sets the getPartitionKeyRangeId.
     *
     * @return the getPartitionKeyRangeId.
     */
    public static FeedOptions partitionKeyRangeIdInternal(FeedOptions options, String partitionKeyRangeId) {
        return options.setPartitionKeyRangeIdInternal(partitionKeyRangeId);
    }

    /**
     * Sets the getPartitionKeyRangeId.
     *
     * @return the getPartitionKeyRangeId.
     */
    public static ChangeFeedOptions partitionKeyRangeIdInternal(ChangeFeedOptions options, String partitionKeyRangeId) {
        return options.setPartitionKeyRangeId(partitionKeyRangeId);
    }
}
