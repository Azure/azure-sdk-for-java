// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkExecutionThresholdsState;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ImplementationBridgeHelpers {
    private final static Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);
    public static final class CosmosClientBuilderHelper {
        private static CosmosClientBuilderAccessor accessor;

        private CosmosClientBuilderHelper() {}
        static {
            ensureClassLoaded(CosmosClientBuilder.class);
        }

        public static void setCosmosClientBuilderAccessor(final CosmosClientBuilderAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosClientBuilder accessor already initialized!");
            }

            accessor = newAccessor;
        }

        static CosmosClientBuilderAccessor getCosmosClientBuilderAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosClientBuilder accessor is not initialized yet!");
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
                throw new IllegalStateException("PartitionKey accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static PartitionKeyAccessor getPartitionKeyAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("PartitionKey accessor is not initialized!");
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
            ensureClassLoaded(CosmosQueryRequestOptions.class);
        }

        public static void setCosmosQueryRequestOptionsAccessor(final CosmosQueryRequestOptionsAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosQueryRequestOptions accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosQueryRequestOptionsAccessor getCosmosQueryRequestOptionsAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosQueryRequestOptions accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosQueryRequestOptionsAccessor {
            void setOperationContext(CosmosQueryRequestOptions queryRequestOptions, OperationContextAndListenerTuple operationContext);
            OperationContextAndListenerTuple getOperationContext(CosmosQueryRequestOptions queryRequestOptions);
            CosmosQueryRequestOptions setHeader(CosmosQueryRequestOptions queryRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosQueryRequestOptions queryRequestOptions);
        }
    }

    public static final class CosmosItemRequestOptionsHelper {
        private static CosmosItemRequestOptionsAccessor accessor;

        private CosmosItemRequestOptionsHelper() {}
        static {
            ensureClassLoaded(CosmosItemRequestOptions.class);
        }

        public static void setCosmosItemRequestOptionsAccessor(final CosmosItemRequestOptionsAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosItemRequestOptions accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosItemRequestOptionsAccessor getCosmosItemRequestOptionsAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosItemRequestOptions accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosItemRequestOptionsAccessor {
            void setOperationContext(CosmosItemRequestOptions queryRequestOptions, OperationContextAndListenerTuple operationContext);
            OperationContextAndListenerTuple getOperationContext(CosmosItemRequestOptions queryRequestOptions);
            CosmosItemRequestOptions clone(CosmosItemRequestOptions options);
            CosmosItemRequestOptions setHeader(CosmosItemRequestOptions cosmosItemRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosItemRequestOptions cosmosItemRequestOptions);
        }
    }

    public static final class CosmosBulkExecutionOptionsHelper {
        private static CosmosBulkExecutionOptionsAccessor accessor;

        private CosmosBulkExecutionOptionsHelper() {}
        static {
            ensureClassLoaded(CosmosBulkExecutionOptions.class);
        }

        public static void setCosmosBulkExecutionOptionsAccessor(final CosmosBulkExecutionOptionsAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosBulkExecutionOptions accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosBulkExecutionOptionsAccessor getCosmosBulkExecutionOptionsAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosBulkExecutionOptions accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosBulkExecutionOptionsAccessor {
            void setOperationContext(
                CosmosBulkExecutionOptions options,
                OperationContextAndListenerTuple operationContext);

            OperationContextAndListenerTuple getOperationContext(CosmosBulkExecutionOptions options);

            <T> T getLegacyBatchScopedContext(CosmosBulkExecutionOptions options);
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
                throw new IllegalStateException("CosmosItemResponse accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosItemResponseBuilderAccessor getCosmosItemResponseBuilderAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosItemResponse accessor is not initialized yet!");
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

    public static final class CosmosClientHelper {
        private static CosmosClientAccessor accessor;

        private CosmosClientHelper() {
        }

        static {
            ensureClassLoaded(CosmosClient.class);
        }

        public static void setCosmosClientAccessor(final CosmosClientAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosClient accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosClientAccessor geCosmosClientAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosClient accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosClientAccessor {
            CosmosAsyncClient getCosmosAsyncClient(CosmosClient cosmosClient);
        }
    }

    public static final class CosmosContainerPropertiesHelper {
        private static CosmosContainerPropertiesAccessor accessor;

        private CosmosContainerPropertiesHelper() {
        }

        static {
            ensureClassLoaded(CosmosContainerProperties.class);
        }

        public static void setCosmosContainerPropertiesAccessor(final CosmosContainerPropertiesAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosContainerProperties already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosContainerPropertiesAccessor getCosmosContainerPropertiesAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosContainerProperties is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosContainerPropertiesAccessor {
            String getSelfLink(CosmosContainerProperties cosmosContainerProperties);
        }
    }

    public static final class CosmosPageFluxHelper {
        private static CosmosPageFluxAccessor accessor;

        private CosmosPageFluxHelper() {
        }

        static {
            ensureClassLoaded(CosmosContainerProperties.class);
        }

        public static <T> void setCosmosPageFluxAccessor(final CosmosPageFluxAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosPageFluxAccessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static <T> CosmosPageFluxAccessor getCosmosPageFluxAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosPageFluxAccessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosPageFluxAccessor {
            <T> CosmosPagedFlux<T> getCosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction);
        }
    }

    public static final class CosmosAsyncDatabaseHelper {
        private static CosmosAsyncDatabaseAccessor accessor;

        private CosmosAsyncDatabaseHelper() {
        }

        static {
            ensureClassLoaded(CosmosAsyncDatabase.class);
        }

        public static <T> void setCosmosAsyncDatabaseAccessor(final CosmosAsyncDatabaseAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosAsyncDatabaseAccessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static <T> CosmosAsyncDatabaseHelper.CosmosAsyncDatabaseAccessor getCosmosAsyncDatabaseAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosAsyncDatabaseAccessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosAsyncDatabaseAccessor {
            CosmosAsyncClient getCosmosAsyncClient(CosmosAsyncDatabase cosmosAsyncDatabase);
        }
    }

    public static final class CosmosBulkExecutionThresholdsStateHelper {
        private static CosmosBulkExecutionThresholdsStateAccessor accessor;

        private CosmosBulkExecutionThresholdsStateHelper() {
        }

        static {
            ensureClassLoaded(CosmosBulkExecutionThresholdsState.class);
        }

        public static void setBulkExecutionThresholdsAccessor(final CosmosBulkExecutionThresholdsStateAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("BulkExecutionThresholds accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosBulkExecutionThresholdsStateAccessor getBulkExecutionThresholdsAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("BulkExecutionThresholds accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosBulkExecutionThresholdsStateAccessor {
            ConcurrentMap<String, PartitionScopeThresholds> getPartitionScopeThresholds(
                CosmosBulkExecutionThresholdsState thresholds);
            CosmosBulkExecutionThresholdsState createWithPartitionScopeThresholds(
                ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds);
        }
    }

    public static final class CosmosDiagnosticsHelper {
        private static CosmosDiagnosticsAccessor accessor;

        private CosmosDiagnosticsHelper() {
        }

        static {
            ensureClassLoaded(CosmosDiagnostics.class);
        }

        public static void setCosmosDiagnosticsAccessor(final CosmosDiagnosticsAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosDiagnosticsAccessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosDiagnosticsAccessor getCosmosDiagnosticsAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosDiagnosticsAccessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosDiagnosticsAccessor {
            FeedResponseDiagnostics getFeedResponseDiagnostics(CosmosDiagnostics cosmosDiagnostics);
            AtomicBoolean isDiagnosticsCapturedInPagedFlux(CosmosDiagnostics cosmosDiagnostics);
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
