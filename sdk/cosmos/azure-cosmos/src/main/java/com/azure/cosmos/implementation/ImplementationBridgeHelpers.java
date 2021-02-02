// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.PartitionKey;

public class ImplementationBridgeHelpers {
    public static final class CosmosClientBuilderHelper {
        private static CosmosClientBuilderAccessor accessor;

        private CosmosClientBuilderHelper() {}

        public static void setCosmosClientBuilderAccessor(final CosmosClientBuilderAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("accessor already initialized!");
            }

            accessor = newAccessor;
        }

        static CosmosClientBuilderAccessor getCosmosClientBuilderAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosClientBuilderAccessor {
            void setCosmosClientMetadataCachesSnapshot(CosmosClientBuilder builder,
                                                       CosmosClientMetadataCachesSnapshot metadataCache);
            CosmosClientMetadataCachesSnapshot getCosmosClientMetadataCachesSnapshot(CosmosClientBuilder builder);

        }
    }

    public static final class PartitionKeyHelper {
        private static PartitionKeyAccessor accessor;

        private PartitionKeyHelper() {}

        public static void setPartitionKeyAccessor(final PartitionKeyAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static PartitionKeyAccessor getPartitionKeyAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface PartitionKeyAccessor {
            PartitionKey toPartitionKey(PartitionKeyInternal partitionKeyInternal);
        }
    }
}
