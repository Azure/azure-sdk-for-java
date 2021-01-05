// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosClientBuilder;

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
}
