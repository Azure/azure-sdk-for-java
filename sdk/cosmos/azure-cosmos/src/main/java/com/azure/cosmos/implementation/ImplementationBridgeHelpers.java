// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import com.azure.cosmos.implementation.patch.PatchOperation;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkExecutionThresholdsState;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
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

    public static final class CosmosChangeFeedRequestOptionsHelper {
        private static CosmosChangeFeedRequestOptionsAccessor accessor;

        private CosmosChangeFeedRequestOptionsHelper() {}
        static {
            ensureClassLoaded(CosmosChangeFeedRequestOptions.class);
        }

        public static void setCosmosChangeFeedRequestOptionsAccessor(final CosmosChangeFeedRequestOptionsAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosChangeFeedRequestOptions accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosChangeFeedRequestOptionsAccessor getCosmosChangeFeedRequestOptionsAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosChangeFeedRequestOptions accessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosChangeFeedRequestOptionsAccessor {
            CosmosChangeFeedRequestOptions setHeader(CosmosChangeFeedRequestOptions changeFeedRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosChangeFeedRequestOptions changeFeedRequestOptions);
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

            double getMinTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options);

            double getMaxTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options);

            CosmosBulkExecutionOptions setTargetedMicroBatchRetryRate(
                CosmosBulkExecutionOptions options,
                double minRetryRate,
                double maxRetryRate);

            int getMaxMicroBatchSize(CosmosBulkExecutionOptions options);

            int getMaxMicroBatchConcurrency(CosmosBulkExecutionOptions options);

            Duration getMaxMicroBatchInterval(CosmosBulkExecutionOptions options);
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

    public static final class CosmosAsyncContainerHelper {
        private static CosmosAsyncContainerAccessor accessor;

        private CosmosAsyncContainerHelper() {
        }

        static {
            ensureClassLoaded(CosmosAsyncContainer.class);
        }

        public static void setCosmosAsyncContainerAccessor(final CosmosAsyncContainerAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("CosmosAsyncContainerAccessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static CosmosAsyncContainerAccessor getCosmosAsyncContainerAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("CosmosAsyncContainerAccessor is not initialized yet!");
            }

            return accessor;
        }

        public interface CosmosAsyncContainerAccessor {
            <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryChangeFeedInternalFunc(
                CosmosAsyncContainer cosmosAsyncContainer,
                CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions,
                Class<T> classType);
        }
    }

    public static final class FeedResponseHelper {
        private static FeedResponseAccessor accessor;

        private FeedResponseHelper() {
        }

        static {
            ensureClassLoaded(FeedResponse.class);
        }

        public static void setFeedResponseAccessor(final FeedResponseAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("FeedResponseAccessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static FeedResponseAccessor getFeedResponseAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("FeedResponseAccessor is not initialized yet!");
            }

            return accessor;
        }

        public interface FeedResponseAccessor {
            <T> boolean getNoChanges(FeedResponse<T> feedResponse);
        }
    }

    public static final class CosmosBatchRequestOptionsHelper {
        private static CosmosBatchRequestOptionsAccessor accessor;

        private CosmosBatchRequestOptionsHelper() {
        }

        static {
            ensureClassLoaded(CosmosBatchRequestOptions.class);
        }

        public static CosmosBatchRequestOptionsAccessor getCosmosBatchRequestOptionsAccessor() {
            return accessor;
        }

        public static void setCosmosBatchRequestOptionsAccessor(CosmosBatchRequestOptionsAccessor accessor) {
            CosmosBatchRequestOptionsHelper.accessor = accessor;
        }

        public interface CosmosBatchRequestOptionsAccessor {
            ConsistencyLevel getConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions);
            CosmosBatchRequestOptions setConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions,
                                                          ConsistencyLevel consistencyLevel);
        }
    }

    public static final class CosmosBatchOperationResultHelper {
        private static CosmosBatchOperationResultAccessor accessor;

        private CosmosBatchOperationResultHelper() {
        }

        static {
            ensureClassLoaded(CosmosBatchRequestOptions.class);
        }

        public static CosmosBatchOperationResultAccessor getCosmosBatchOperationResultAccessor() {
            return accessor;
        }

        public static void setCosmosBatchOperationResultAccessor(CosmosBatchOperationResultAccessor accessor) {
            CosmosBatchOperationResultHelper.accessor = accessor;
        }

        public interface CosmosBatchOperationResultAccessor {
            ObjectNode getResourceObject(CosmosBatchOperationResult cosmosBatchOperationResult);
        }
    }

    public static final class CosmosPatchOperationsHelper {
        private static CosmosPatchOperationsAccessor accessor;

        private CosmosPatchOperationsHelper() {
        }

        static {
            ensureClassLoaded(CosmosPatchOperations.class);
        }

        public static CosmosPatchOperationsAccessor getCosmosPatchOperationsAccessor() {
            return accessor;
        }

        public static void setCosmosPatchOperationsAccessor(CosmosPatchOperationsAccessor accessor) {
            CosmosPatchOperationsHelper.accessor = accessor;
        }

        public interface CosmosPatchOperationsAccessor {
            List<PatchOperation> getPatchOperations(CosmosPatchOperations cosmosPatchOperations);
        }
    }

    public static final class CosmosBatchHelper {
        private static CosmosBatchAccessor accessor;

        private CosmosBatchHelper() {
        }

        static {
            ensureClassLoaded(CosmosBatch.class);
        }

        public static CosmosBatchAccessor getCosmosBatchAccessor() {
            return accessor;
        }

        public static void setCosmosBatchAccessor(CosmosBatchAccessor accessor) {
            CosmosBatchHelper.accessor = accessor;
        }

        public interface CosmosBatchAccessor {
            List<ItemBatchOperation<?>> getOperationsInternal(CosmosBatch cosmosBatch);
        }
    }

    public static final class CosmosBulkItemResponseHelper {
        private static CosmosBulkItemResponseAccessor accessor;

        private CosmosBulkItemResponseHelper() {
        }

        static {
            ensureClassLoaded(CosmosBulkItemResponse.class);
        }

        public static CosmosBulkItemResponseAccessor getCosmosBulkItemResponseAccessor() {
            return accessor;
        }

        public static void setCosmosBulkItemResponseAccessor(CosmosBulkItemResponseAccessor accessor) {
            CosmosBulkItemResponseHelper.accessor = accessor;
        }

        public interface CosmosBulkItemResponseAccessor {
            ObjectNode getResourceObject(CosmosBulkItemResponse cosmosBulkItemResponse);
        }
    }

    public static final class DeprecatedCosmosBulkItemResponseHelper {
        private static DeprecatedCosmosBulkItemResponseAccessor accessor;

        private DeprecatedCosmosBulkItemResponseHelper() {
        }

        static {
            ensureClassLoaded(com.azure.cosmos.CosmosBulkItemResponse.class);
        }

        public static DeprecatedCosmosBulkItemResponseAccessor getCosmosBulkItemResponseAccessor() {
            return accessor;
        }

        public static void setCosmosBulkItemResponseAccessor(DeprecatedCosmosBulkItemResponseAccessor accessor) {
            DeprecatedCosmosBulkItemResponseHelper.accessor = accessor;
        }

        public interface DeprecatedCosmosBulkItemResponseAccessor {
            ObjectNode getResourceObject(com.azure.cosmos.CosmosBulkItemResponse cosmosBulkItemResponse);
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
