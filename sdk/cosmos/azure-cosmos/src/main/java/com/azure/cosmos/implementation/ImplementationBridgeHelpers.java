// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncClientEncryptionKey;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import com.azure.cosmos.implementation.patch.PatchOperation;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.spark.OperationContextAndListenerTuple;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkExecutionThresholdsState;
import com.azure.cosmos.models.CosmosBulkItemResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ImplementationBridgeHelpers {
    private final static Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);
    public static final class CosmosClientBuilderHelper {
        private static final AtomicReference<CosmosClientBuilderAccessor> accessor = new AtomicReference<>();
        private static final AtomicBoolean cosmosClientBuilderClassLoaded = new AtomicBoolean(false);

        private CosmosClientBuilderHelper() {}

        public static void setCosmosClientBuilderAccessor(final CosmosClientBuilderAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosClientBuilderAccessor already initialized!");
            }
            cosmosClientBuilderClassLoaded.set(true);
        }

        public static CosmosClientBuilderAccessor getCosmosClientBuilderAccessor() {
            if (cosmosClientBuilderClassLoaded.compareAndSet(false, true)) {
                CosmosClientBuilder.doNothingButEnsureLoadingClass();
            }

            CosmosClientBuilderAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosClientBuilderAccessor is not initialized yet!");
                System.exit(9700); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosClientBuilderAccessor {
            void setCosmosClientMetadataCachesSnapshot(CosmosClientBuilder builder,
                                                       CosmosClientMetadataCachesSnapshot metadataCache);

            CosmosClientMetadataCachesSnapshot getCosmosClientMetadataCachesSnapshot(CosmosClientBuilder builder);

            void setCosmosClientApiType(CosmosClientBuilder builder, ApiType apiType);

            ApiType getCosmosClientApiType(CosmosClientBuilder builder);

            ConnectionPolicy getConnectionPolicy(CosmosClientBuilder builder);

            Configs getConfigs(CosmosClientBuilder builder);

            ConsistencyLevel getConsistencyLevel(CosmosClientBuilder builder);
        }
    }

    public static final class PartitionKeyHelper {
        private final static AtomicBoolean partitionKeyClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<PartitionKeyAccessor> accessor = new AtomicReference<>();

        private PartitionKeyHelper() {}

        public static void setPartitionKeyAccessor(final PartitionKeyAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("PartitionKeyAccessor already initialized!");
            }
            partitionKeyClassLoaded.set(true);
        }

        public static PartitionKeyAccessor getPartitionKeyAccessor() {
            if (partitionKeyClassLoaded.compareAndSet(false, true)) {
                PartitionKey.doNothingButEnsureLoadingClass();
            }

            PartitionKeyAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("PartitionKeyAccessor is not initialized yet!");
                System.exit(9701); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface PartitionKeyAccessor {
            PartitionKey toPartitionKey(PartitionKeyInternal partitionKeyInternal);
        }
    }

    public static final class DirectConnectionConfigHelper {
        private final static AtomicBoolean directConnectionConfigClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<DirectConnectionConfigAccessor> accessor = new AtomicReference<>();

        private DirectConnectionConfigHelper() {}

        public static void setDirectConnectionConfigAccessor(final DirectConnectionConfigAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("DirectConnectionConfigAccessor already initialized!");
            }
            directConnectionConfigClassLoaded.set(true);
        }

        public static DirectConnectionConfigAccessor getDirectConnectionConfigAccessor() {
            if (directConnectionConfigClassLoaded.compareAndSet(false, true)) {
                DirectConnectionConfig.doNothingButEnsureLoadingClass();
            }

            DirectConnectionConfigAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("DirectConnectionConfigAccessor is not initialized yet!");
                System.exit(9702); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface DirectConnectionConfigAccessor {
            int getIoThreadCountPerCoreFactor(DirectConnectionConfig config);
            DirectConnectionConfig setIoThreadCountPerCoreFactor(
                DirectConnectionConfig config, int ioThreadCountPerCoreFactor);
            int getIoThreadPriority(DirectConnectionConfig config);
            DirectConnectionConfig setIoThreadPriority(
                DirectConnectionConfig config, int ioThreadPriority);
        }
    }

    public static final class CosmosQueryRequestOptionsHelper {
        private final static AtomicBoolean cosmosQueryRequestOptionsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosQueryRequestOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosQueryRequestOptionsHelper() {}

        public static void setCosmosQueryRequestOptionsAccessor(final CosmosQueryRequestOptionsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosQueryRequestOptionsAccessor already initialized!");
            }
            cosmosQueryRequestOptionsClassLoaded.set(true);
        }

        public static CosmosQueryRequestOptionsAccessor getCosmosQueryRequestOptionsAccessor() {
            if (cosmosQueryRequestOptionsClassLoaded.compareAndSet(false, true)) {
                CosmosQueryRequestOptions.doNothingButEnsureLoadingClass();
            }

            CosmosQueryRequestOptionsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosQueryRequestOptionsAccessor is not initialized yet!");
                System.exit(9703); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosQueryRequestOptionsAccessor {
            void setOperationContext(CosmosQueryRequestOptions queryRequestOptions, OperationContextAndListenerTuple operationContext);
            OperationContextAndListenerTuple getOperationContext(CosmosQueryRequestOptions queryRequestOptions);
            CosmosQueryRequestOptions setHeader(CosmosQueryRequestOptions queryRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosQueryRequestOptions queryRequestOptions);
            boolean isQueryPlanRetrievalDisallowed(CosmosQueryRequestOptions queryRequestOptions);
            CosmosQueryRequestOptions disallowQueryPlanRetrieval(CosmosQueryRequestOptions queryRequestOptions);
            UUID getCorrelationActivityId(CosmosQueryRequestOptions queryRequestOptions);
            CosmosQueryRequestOptions setCorrelationActivityId(CosmosQueryRequestOptions queryRequestOptions, UUID correlationActivityId);
            boolean isEmptyPageDiagnosticsEnabled(CosmosQueryRequestOptions queryRequestOptions);
            CosmosQueryRequestOptions setEmptyPageDiagnosticsEnabled(CosmosQueryRequestOptions queryRequestOptions, boolean emptyPageDiagnosticsEnabled);
            <T> Function<JsonNode, T> getItemFactoryMethod(CosmosQueryRequestOptions queryRequestOptions, Class<T> classOfT);
            CosmosQueryRequestOptions setItemFactoryMethod(CosmosQueryRequestOptions queryRequestOptions, Function<JsonNode, ?> factoryMethod);
        }
    }

    public static final class CosmosChangeFeedRequestOptionsHelper {
        private final static AtomicBoolean cosmosChangeFeedRequestOptionsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosChangeFeedRequestOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosChangeFeedRequestOptionsHelper() {}

        public static void setCosmosChangeFeedRequestOptionsAccessor(final CosmosChangeFeedRequestOptionsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosChangeFeedRequestOptionsAccessor already initialized!");
            }
            cosmosChangeFeedRequestOptionsClassLoaded.set(true);
        }

        public static CosmosChangeFeedRequestOptionsAccessor getCosmosChangeFeedRequestOptionsAccessor() {
            if (cosmosChangeFeedRequestOptionsClassLoaded.compareAndSet(false, true)) {
                CosmosChangeFeedRequestOptions.doNothingButEnsureLoadingClass();
            }

            CosmosChangeFeedRequestOptionsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosChangeFeedRequestOptionsAccessor is not initialized yet!");
                System.exit(9704); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosChangeFeedRequestOptionsAccessor {
            CosmosChangeFeedRequestOptions setHeader(CosmosChangeFeedRequestOptions changeFeedRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosChangeFeedRequestOptions changeFeedRequestOptions);
            void setOperationContext(CosmosChangeFeedRequestOptions changeFeedRequestOptions, OperationContextAndListenerTuple operationContext);
            OperationContextAndListenerTuple getOperationContext(CosmosChangeFeedRequestOptions changeFeedRequestOptions);
            <T> Function<JsonNode, T> getItemFactoryMethod(CosmosChangeFeedRequestOptions queryRequestOptions, Class<T> classOfT);
            CosmosChangeFeedRequestOptions setItemFactoryMethod(CosmosChangeFeedRequestOptions queryRequestOptions, Function<JsonNode, ?> factoryMethod);
        }
    }

    public static final class CosmosItemRequestOptionsHelper {
        private final static AtomicBoolean cosmosItemRequestOptionsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosItemRequestOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosItemRequestOptionsHelper() {}

        public static void setCosmosItemRequestOptionsAccessor(final CosmosItemRequestOptionsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosItemRequestOptionsAccessor already initialized!");
            }
            cosmosItemRequestOptionsClassLoaded.set(true);
        }

        public static CosmosItemRequestOptionsAccessor getCosmosItemRequestOptionsAccessor() {
            if (cosmosItemRequestOptionsClassLoaded.compareAndSet(false, true)) {
                CosmosItemRequestOptions.doNothingButEnsureLoadingClass();
            }

            CosmosItemRequestOptionsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosItemRequestOptionsAccessor is not initialized yet!");
                System.exit(9705); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
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
        private final static AtomicBoolean cosmosBulkExecutionOptionsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosBulkExecutionOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosBulkExecutionOptionsHelper() {}

        public static void setCosmosBulkExecutionOptionsAccessor(final CosmosBulkExecutionOptionsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosBulkExecutionOptionsAccessor already initialized!");
            }
            cosmosBulkExecutionOptionsClassLoaded.set(true);
        }

        public static CosmosBulkExecutionOptionsAccessor getCosmosBulkExecutionOptionsAccessor() {
            if (cosmosBulkExecutionOptionsClassLoaded.compareAndSet(false, true)) {
                CosmosBulkExecutionOptions.doNothingButEnsureLoadingClass();
            }

            CosmosBulkExecutionOptionsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosBulkExecutionOptionsAccessor is not initialized yet!");
                System.exit(9706); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
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

            CosmosBulkExecutionOptions setMaxMicroBatchSize(CosmosBulkExecutionOptions options, int maxMicroBatchSize);

            int getMaxMicroBatchConcurrency(CosmosBulkExecutionOptions options);

            Integer getMaxConcurrentCosmosPartitions(CosmosBulkExecutionOptions options);

            CosmosBulkExecutionOptions setMaxConcurrentCosmosPartitions(
                CosmosBulkExecutionOptions options, int mxConcurrentCosmosPartitions);

            Duration getMaxMicroBatchInterval(CosmosBulkExecutionOptions options);

            CosmosBulkExecutionOptions setHeader(CosmosBulkExecutionOptions cosmosBulkExecutionOptions,
                                                 String name, String value);

            Map<String, String> getHeader(CosmosBulkExecutionOptions cosmosBulkExecutionOptions);

            Map<String, String> getCustomOptions(CosmosBulkExecutionOptions cosmosBulkExecutionOptions);
        }
    }

    public static final class CosmosItemResponseHelper {
        private final static AtomicBoolean cosmosItemResponseClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosItemResponseBuilderAccessor> accessor = new AtomicReference<>();

        private CosmosItemResponseHelper() {
        }


        public static void setCosmosItemResponseBuilderAccessor(final CosmosItemResponseBuilderAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosItemResponseBuilderAccessor already initialized!");
            }
            cosmosItemResponseClassLoaded.set(true);
        }

        public static CosmosItemResponseBuilderAccessor getCosmosItemResponseBuilderAccessor() {
            if (cosmosItemResponseClassLoaded.compareAndSet(false, true)) {
                CosmosItemResponse.doNothingButEnsureLoadingClass();
            }

            CosmosItemResponseBuilderAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosItemResponseBuilderAccessor is not initialized yet!");
                System.exit(9707); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
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
        private final static AtomicBoolean cosmosClientClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosClientAccessor> accessor = new AtomicReference<>();

        private CosmosClientHelper() {
        }

        public static void setCosmosClientAccessor(final CosmosClientAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosClientAccessor already initialized!");
            }
            cosmosClientClassLoaded.set(true);
        }

        public static CosmosClientAccessor getCosmosClientAccessor() {
            if (cosmosClientClassLoaded.compareAndSet(false, true)) {
                CosmosClient.doNothingButEnsureLoadingClass();
            }

            CosmosClientAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosClientAccessor is not initialized yet!");
                System.exit(9708); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosClientAccessor {
            CosmosAsyncClient getCosmosAsyncClient(CosmosClient cosmosClient);
        }
    }

    public static final class CosmosContainerPropertiesHelper {
        private final static AtomicBoolean cosmosContainerPropertiesClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosContainerPropertiesAccessor> accessor = new AtomicReference<>();

        private CosmosContainerPropertiesHelper() {
        }

        public static void setCosmosContainerPropertiesAccessor(final CosmosContainerPropertiesAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosContainerPropertiesAccessor already initialized!");
            }
            cosmosContainerPropertiesClassLoaded.set(true);
        }

        public static CosmosContainerPropertiesAccessor getCosmosContainerPropertiesAccessor() {
            if (cosmosContainerPropertiesClassLoaded.compareAndSet(false, true)) {
                CosmosContainerProperties.doNothingButEnsureLoadingClass();
            }

            CosmosContainerPropertiesAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosContainerPropertiesAccessor is not initialized yet!");
                System.exit(9709); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosContainerPropertiesAccessor {
            String getSelfLink(CosmosContainerProperties cosmosContainerProperties);
            void setSelfLink(CosmosContainerProperties cosmosContainerProperties, String selfLink);
        }
    }

    public static final class CosmosPageFluxHelper {
        private final static AtomicBoolean cosmosPagedFluxClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosPageFluxAccessor> accessor = new AtomicReference<>();

        private CosmosPageFluxHelper() {
        }

        public static <T> void setCosmosPageFluxAccessor(final CosmosPageFluxAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosPageFluxAccessor already initialized!");
            }
            cosmosPagedFluxClassLoaded.set(true);
        }

        public static <T> CosmosPageFluxAccessor getCosmosPageFluxAccessor() {
            if (cosmosPagedFluxClassLoaded.compareAndSet(false, true)) {
                CosmosPagedFlux.doNothingButEnsureLoadingClass();
            }

            CosmosPageFluxAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosPageFluxAccessor is not initialized yet!");
                System.exit(9710); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosPageFluxAccessor {
            <T> CosmosPagedFlux<T> getCosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction);
        }
    }

    public static final class CosmosAsyncDatabaseHelper {
        private final static AtomicBoolean cosmosAsyncDatabaseClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosAsyncDatabaseAccessor> accessor = new AtomicReference<>();

        private CosmosAsyncDatabaseHelper() {
        }

        public static <T> void setCosmosAsyncDatabaseAccessor(final CosmosAsyncDatabaseAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosAsyncDatabaseAccessor already initialized!");
            }
            cosmosAsyncDatabaseClassLoaded.set(true);
        }

        public static <T> CosmosAsyncDatabaseAccessor getCosmosAsyncDatabaseAccessor() {
            if (cosmosAsyncDatabaseClassLoaded.compareAndSet(false, true)) {
                CosmosAsyncDatabase.doNothingButEnsureLoadingClass();
            }

            CosmosAsyncDatabaseAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosAsyncDatabaseAccessor is not initialized yet!");
                System.exit(9711); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosAsyncDatabaseAccessor {
            CosmosAsyncClient getCosmosAsyncClient(CosmosAsyncDatabase cosmosAsyncDatabase);
            String getLink(CosmosAsyncDatabase cosmosAsyncDatabase);
        }
    }

    public static final class CosmosBulkExecutionThresholdsStateHelper {
        private final static AtomicBoolean cosmosBulkExecutionThresholdsStateClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosBulkExecutionThresholdsStateAccessor> accessor = new AtomicReference<>();

        private CosmosBulkExecutionThresholdsStateHelper() {
        }

        public static void setBulkExecutionThresholdsAccessor(final CosmosBulkExecutionThresholdsStateAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosBulkExecutionThresholdsStateAccessor already initialized!");
            }
            cosmosBulkExecutionThresholdsStateClassLoaded.set(true);
        }

        public static CosmosBulkExecutionThresholdsStateAccessor getBulkExecutionThresholdsAccessor() {
            if (cosmosBulkExecutionThresholdsStateClassLoaded.compareAndSet(false, true)) {
                CosmosBulkExecutionThresholdsState.doNothingButEnsureLoadingClass();
            }

            CosmosBulkExecutionThresholdsStateAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosBulkExecutionThresholdsStateAccessor is not initialized yet!");
                System.exit(9712); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosBulkExecutionThresholdsStateAccessor {
            ConcurrentMap<String, PartitionScopeThresholds> getPartitionScopeThresholds(
                CosmosBulkExecutionThresholdsState thresholds);
            CosmosBulkExecutionThresholdsState createWithPartitionScopeThresholds(
                ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholds);
        }
    }

    public static final class CosmosDiagnosticsHelper {
        private final static AtomicBoolean cosmosDiagnosticsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosDiagnosticsAccessor> accessor = new AtomicReference<>();

        private CosmosDiagnosticsHelper() {
        }

        public static void setCosmosDiagnosticsAccessor(final CosmosDiagnosticsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosDiagnosticsAccessor already initialized!");
            }
            cosmosDiagnosticsClassLoaded.set(true);
        }

        public static CosmosDiagnosticsAccessor getCosmosDiagnosticsAccessor() {
            if (cosmosDiagnosticsClassLoaded.compareAndSet(false, true)) {
                CosmosDiagnostics.doNothingButEnsureLoadingClass();
            }

            CosmosDiagnosticsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosDiagnosticsAccessor is not initialized yet!");
                System.exit(9713); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosDiagnosticsAccessor {
            FeedResponseDiagnostics getFeedResponseDiagnostics(CosmosDiagnostics cosmosDiagnostics);
            AtomicBoolean isDiagnosticsCapturedInPagedFlux(CosmosDiagnostics cosmosDiagnostics);
        }
    }

    public static final class CosmosAsyncContainerHelper {
        private final static AtomicBoolean cosmosAsyncContainerClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosAsyncContainerAccessor> accessor = new AtomicReference<>();

        private CosmosAsyncContainerHelper() {
        }

        static {
            ensureClassLoaded(CosmosAsyncContainer.class);
        }

        public static void setCosmosAsyncContainerAccessor(final CosmosAsyncContainerAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosAsyncContainerAccessor already initialized!");
            }
            cosmosAsyncContainerClassLoaded.set(true);
        }

        public static CosmosAsyncContainerAccessor getCosmosAsyncContainerAccessor() {
            if (cosmosAsyncContainerClassLoaded.compareAndSet(false, true)) {
                CosmosAsyncContainer.doNothingButEnsureLoadingClass();
            }

            CosmosAsyncContainerAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosAsyncContainerAccessor is not initialized yet!");
                System.exit(9714); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface CosmosAsyncContainerAccessor {
            <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryChangeFeedInternalFunc(
                CosmosAsyncContainer cosmosAsyncContainer,
                CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions,
                Class<T> classType);
        }
    }

    public static final class FeedResponseHelper {
        private final static AtomicBoolean feedResponseClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<FeedResponseAccessor> accessor = new AtomicReference<>();

        private FeedResponseHelper() {
        }

        public static void setFeedResponseAccessor(final FeedResponseAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("FeedResponseAccessor already initialized!");
            }
            feedResponseClassLoaded.set(true);
        }

        public static FeedResponseAccessor getFeedResponseAccessor() {
            if (feedResponseClassLoaded.compareAndSet(false, true)) {
                FeedResponse.doNothingButEnsureLoadingClass();
            }

            FeedResponseAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("FeedResponseAccessor is not initialized yet!");
                System.exit(9715); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public interface FeedResponseAccessor {
            <T> boolean getNoChanges(FeedResponse<T> feedResponse);
            <TNew, T> FeedResponse<TNew> convertGenericType(FeedResponse<T> feedResponse, Function<T, TNew> conversion);
        }
    }

    public static final class CosmosBatchRequestOptionsHelper {
        private final static AtomicBoolean cosmosBatchRequestOptionsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosBatchRequestOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosBatchRequestOptionsHelper() {
        }

        public static CosmosBatchRequestOptionsAccessor getCosmosBatchRequestOptionsAccessor() {
            if (cosmosBatchRequestOptionsClassLoaded.compareAndSet(false, true)) {
                CosmosBatchRequestOptions.doNothingButEnsureLoadingClass();
            }

            CosmosBatchRequestOptionsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosBatchRequestOptionsAccessor is not initialized yet!");
                System.exit(9716); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosBatchRequestOptionsAccessor(final CosmosBatchRequestOptionsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosBatchRequestOptionsAccessor already initialized!");
            }
            cosmosBatchRequestOptionsClassLoaded.set(true);
        }

        public interface CosmosBatchRequestOptionsAccessor {
            ConsistencyLevel getConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions);
            CosmosBatchRequestOptions setConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions,
                                                          ConsistencyLevel consistencyLevel);
            CosmosBatchRequestOptions setHeader(CosmosBatchRequestOptions cosmosItemRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosBatchRequestOptions cosmosItemRequestOptions);
        }
    }

    public static final class CosmosBatchOperationResultHelper {
        private final static AtomicBoolean cosmosBatchOperationResultClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosBatchOperationResultAccessor> accessor = new AtomicReference<>();

        private CosmosBatchOperationResultHelper() {
        }

        public static CosmosBatchOperationResultAccessor getCosmosBatchOperationResultAccessor() {
            if (cosmosBatchOperationResultClassLoaded.compareAndSet(false, true)) {
                CosmosBatchOperationResult.doNothingButEnsureLoadingClass();
            }

            CosmosBatchOperationResultAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosBatchOperationResultAccessor is not initialized yet!");
                System.exit(9717); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosBatchOperationResultAccessor(final CosmosBatchOperationResultAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosBatchOperationResultAccessor already initialized!");
            }
            cosmosBatchOperationResultClassLoaded.set(true);
        }

        public interface CosmosBatchOperationResultAccessor {
            ObjectNode getResourceObject(CosmosBatchOperationResult cosmosBatchOperationResult);
            void setResourceObject(CosmosBatchOperationResult cosmosBatchOperationResult, ObjectNode objectNode);
        }
    }

    public static final class CosmosPatchOperationsHelper {
        private final static AtomicBoolean cosmosPatchOperationsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosPatchOperationsAccessor> accessor = new AtomicReference<>();

        private CosmosPatchOperationsHelper() {
        }

        public static CosmosPatchOperationsAccessor getCosmosPatchOperationsAccessor() {
            if (cosmosPatchOperationsClassLoaded.compareAndSet(false, true)) {
                CosmosPatchOperations.doNothingButEnsureLoadingClass();
            }

            CosmosPatchOperationsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosPatchOperationsAccessor is not initialized yet!");
                System.exit(9718); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosPatchOperationsAccessor(CosmosPatchOperationsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosPatchOperationsAccessor already initialized!");
            }
            cosmosPatchOperationsClassLoaded.set(true);
        }

        public interface CosmosPatchOperationsAccessor {
            List<PatchOperation> getPatchOperations(CosmosPatchOperations cosmosPatchOperations);
        }
    }

    public static final class CosmosBatchHelper {
        private static AtomicBoolean cosmosBatchClassLoaded = new AtomicBoolean(false);
        private static AtomicReference<CosmosBatchAccessor> accessor = new AtomicReference<>();

        private CosmosBatchHelper() {
        }

        public static CosmosBatchAccessor getCosmosBatchAccessor() {
            if (cosmosBatchClassLoaded.compareAndSet(false, true)) {
                CosmosBatch.doNothingButEnsureLoadingClass();
            }

            CosmosBatchAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosBatchAccessor is not initialized yet!");
                System.exit(9719); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosBatchAccessor(CosmosBatchAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosBatchAccessor already initialized!");
            }
            cosmosBatchClassLoaded.set(true);
        }

        public interface CosmosBatchAccessor {
            List<ItemBatchOperation<?>> getOperationsInternal(CosmosBatch cosmosBatch);
        }
    }

    public static final class CosmosBulkItemResponseHelper {
        private final static AtomicBoolean cosmosBulkItemResponseClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosBulkItemResponseAccessor> accessor = new AtomicReference<>();

        private CosmosBulkItemResponseHelper() {
        }

        public static CosmosBulkItemResponseAccessor getCosmosBulkItemResponseAccessor() {
            if (cosmosBulkItemResponseClassLoaded.compareAndSet(false, true)) {
                CosmosBulkItemResponse.doNothingButEnsureLoadingClass();
            }

            CosmosBulkItemResponseAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosBulkItemResponseAccessor is not initialized yet!");
                System.exit(9720); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosBulkItemResponseAccessor(CosmosBulkItemResponseAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosBulkItemResponseAccessor already initialized!");
            }
            cosmosBulkItemResponseClassLoaded.set(true);
        }

        public interface CosmosBulkItemResponseAccessor {
            ObjectNode getResourceObject(CosmosBulkItemResponse cosmosBulkItemResponse);

            void setResourceObject(CosmosBulkItemResponse cosmosBulkItemResponse,
                                   ObjectNode objectNode);
        }
    }

    public static final class CosmosBatchResponseHelper {
        private final static AtomicBoolean cosmosBatchResponseClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosBatchResponseAccessor> accessor = new AtomicReference<>();

        private CosmosBatchResponseHelper() {
        }

        public static CosmosBatchResponseAccessor getCosmosBatchResponseAccessor() {
            if (cosmosBatchResponseClassLoaded.compareAndSet(false, true)) {
                CosmosBatchResponse.doNothingButEnsureLoadingClass();
            }

            CosmosBatchResponseAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosBatchResponseAccessor is not initialized yet!");
                System.exit(9721); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosBatchResponseAccessor(final CosmosBatchResponseAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosBatchResponseAccessor already initialized!");
            }
            cosmosBatchResponseClassLoaded.set(true);
        }

        public interface CosmosBatchResponseAccessor {
            List<CosmosBatchOperationResult> getResults(CosmosBatchResponse cosmosBatchResponse);
        }
    }

    public static final class CosmosAsyncClientEncryptionKeyHelper {
        private final static AtomicBoolean cosmosAsyncClientEncryptionKeyClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosAsyncClientEncryptionKeyAccessor> accessor = new AtomicReference<>();

        private CosmosAsyncClientEncryptionKeyHelper() {
        }

        public static CosmosAsyncClientEncryptionKeyAccessor getCosmosAsyncClientEncryptionKeyAccessor() {
            if (cosmosAsyncClientEncryptionKeyClassLoaded.compareAndSet(false, true)) {
                CosmosAsyncClientEncryptionKey.doNothingButEnsureLoadingClass();
            }

            CosmosAsyncClientEncryptionKeyAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosAsyncClientEncryptionKeyAccessor is not initialized yet!");
                System.exit(9722); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosAsyncClientEncryptionKeyAccessor(final CosmosAsyncClientEncryptionKeyAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosAsyncClientEncryptionKeyAccessor already initialized!");
            }
            cosmosAsyncClientEncryptionKeyClassLoaded.set(true);
        }

        public interface CosmosAsyncClientEncryptionKeyAccessor {
            Mono<CosmosClientEncryptionKeyResponse> readClientEncryptionKey(CosmosAsyncClientEncryptionKey cosmosAsyncClientEncryptionKey,
                                                                            RequestOptions requestOptions);
        }
    }

    public static final class CosmosExceptionHelper {
        private final static AtomicBoolean cosmosExceptionClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosExceptionAccessor> accessor = new AtomicReference<>();

        private CosmosExceptionHelper() {
        }

        public static CosmosExceptionAccessor getCosmosExceptionAccessor() {
            if (cosmosExceptionClassLoaded.compareAndSet(false, true)) {
                CosmosException.doNothingButEnsureLoadingClass();
            }

            CosmosExceptionAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosExceptionAccessor is not initialized yet!");
                System.exit(9800); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setCosmosExceptionAccessor(final CosmosExceptionAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.warn("CosmosExceptionAccessor already initialized!");
            }
            cosmosExceptionClassLoaded.set(true);
        }

        public interface CosmosExceptionAccessor {
            CosmosException createCosmosException(int statusCode, Exception innerException);
        }
    }

    private static <T> void ensureClassLoaded(Class<T> classType) {
        try {
            try {
                ClassLoader classLoader = classType.getClassLoader();
                logger.info(
                        "Calling ensureClassLoaded for class {} with classLoader {}",
                        classType,
                        classLoader);
            } catch (Throwable e) {
                logger.warn("Failed to get class loader", e);
            }

            // ensures the class is loaded
            Class.forName(classType.getName());
        } catch (Throwable e) {
            logger.error("Can not load class {}", classType.getName(), e);
            System.exit(9801); // Using a unique status code here to help debug the issue.
        }
    }
}
