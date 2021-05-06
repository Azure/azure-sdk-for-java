// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.spark.OperationContext;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImplementationBridgeHelpers {
    private static Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);
    public static final class CosmosClientBuilderHelper {
        private static CosmosClientBuilderAccessor accessor;

        private CosmosClientBuilderHelper() {}
        static {
            ensureClassLoaded(CosmosClientBuilder.class);
        }

        public static void setCosmosClientBuilderAccessor(final CosmosClientBuilderAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosClientBuilderHelper accessor already initialized!");
            }

            accessor = newAccessor;
        }

        static CosmosClientBuilderAccessor getCosmosClientBuilderAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosClientBuilderHelper accessor is not initialized yet!");
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
        static {
            ensureClassLoaded(PartitionKey.class);
        }
        private static PartitionKeyAccessor accessor;

        private PartitionKeyHelper() {}

        public static void setPartitionKeyAccessor(final PartitionKeyAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("PartitionKeyHelper accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static PartitionKeyAccessor getPartitionKeyAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("PartitionKeyHelper accessor is not initialized!");
            }

            return accessor;
        }

        public interface PartitionKeyAccessor {
            PartitionKey toPartitionKey(PartitionKeyInternal partitionKeyInternal);
        }
    }

    public static final class CosmosQueryRequestOptionsHelper {
        private static CosmosQueryRequestOptionsAccessor accessor;

        private CosmosQueryRequestOptionsHelper() {}
        static {
            ensureClassLoaded(CosmosQueryRequestOptionsHelper.class);
        }

        public static void setCosmosQueryRequestOptionsAccessor(final CosmosQueryRequestOptionsAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosQueryRequestOptionsHelper accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosQueryRequestOptionsAccessor getCosmosQueryRequestOptionsAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosQueryRequestOptionsHelper accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosQueryRequestOptionsAccessor {
            void setOperationContext(CosmosQueryRequestOptions queryRequestOptions, OperationContextAndListenerTuple operationContext);
            OperationContextAndListenerTuple getOperationContext(CosmosQueryRequestOptions queryRequestOptions);
        }
    }

    public static final class CosmosItemResponseHelper {
        private static CosmosItemResponseBuilderAccessor accessor;

        private CosmosItemResponseHelper() {
        }

        static {
            ensureClassLoaded(CosmosItemResponse.class);
        }

        public static void setCosmosItemResponseBuilderAccessor(final CosmosItemResponseBuilderAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosItemResponseBuilder accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosItemResponseBuilderAccessor getCosmosItemResponseBuilderAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosItemResponseBuilder accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosItemResponseBuilderAccessor {
            <T> CosmosItemResponse<T> createCosmosItemResponse(ResourceResponse<Document> response,
                                                               byte[] contentAsByteArray, Class<T> classType,
                                                               ItemDeserializer itemDeserializer);

            byte[] getByteArrayContent(CosmosItemResponse<byte[]> response);

            void setByteArrayContent(CosmosItemResponse<byte[]> response, byte[] content);

            ResourceResponse<Document> getResourceResponse(CosmosItemResponse<byte[]> response);
        }
    }

    private static <T> void ensureClassLoaded(Class<T> classType) {
        try {
            // ensures the class is loaded
            Class.forName(classType.getName());
        } catch (ClassNotFoundException e) {
            logger.error("cannot load class {}", classType.getName());
            throw new RuntimeException(e);
        }
    }
}
