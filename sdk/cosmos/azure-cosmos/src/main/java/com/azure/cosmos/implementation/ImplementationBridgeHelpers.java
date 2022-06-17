// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
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
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
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

    private static void  initializeAllAccessors() {
        BridgeInternal.initializeAllAccessors();
        ModelBridgeInternal.initializeAllAccessors();
        UtilBridgeInternal.initializeAllAccessors();
    }

    public static final class CosmosClientBuilderHelper {
        private static final AtomicReference<CosmosClientBuilderAccessor> accessor = new AtomicReference<>();
        private static final AtomicBoolean cosmosClientBuilderClassLoaded = new AtomicBoolean(false);

        private CosmosClientBuilderHelper() {}

        public static void setCosmosClientBuilderAccessor(final CosmosClientBuilderAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosClientBuilderAccessor already initialized!");
            } else {
                logger.info("Setting CosmosClientBuilderAccessor...");
                cosmosClientBuilderClassLoaded.set(true);
            }
        }

        public static CosmosClientBuilderAccessor getCosmosClientBuilderAccessor() {
            if (!cosmosClientBuilderClassLoaded.get()) {
                logger.debug("Initializing CosmosClientBuilderAccessor...");
                initializeAllAccessors();
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
                logger.debug("PartitionKeyAccessor already initialized!");
            } else {
                logger.info("Setting PartitionKeyAccessor...");
                partitionKeyClassLoaded.set(true);
            }
        }

        public static PartitionKeyAccessor getPartitionKeyAccessor() {
            if (!partitionKeyClassLoaded.get()) {
                logger.debug("Initializing PartitionKeyAccessor...");
                initializeAllAccessors();
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
                logger.debug("DirectConnectionConfigAccessor already initialized!");
            } else {
                logger.info("Setting DirectConnectionConfigAccessor...");
                directConnectionConfigClassLoaded.set(true);
            }
        }

        public static DirectConnectionConfigAccessor getDirectConnectionConfigAccessor() {
            if (!directConnectionConfigClassLoaded.get()) {
                logger.debug("Initializing DirectConnectionConfigAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosQueryRequestOptionsAccessor already initialized!");
            } else {
                logger.info("Setting CosmosQueryRequestOptionsAccessor...");
                cosmosQueryRequestOptionsClassLoaded.set(true);
            }
        }

        public static CosmosQueryRequestOptionsAccessor getCosmosQueryRequestOptionsAccessor() {
            if (!cosmosQueryRequestOptionsClassLoaded.get()) {
                logger.debug("Initializing CosmosQueryRequestOptionsAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosChangeFeedRequestOptionsAccessor already initialized!");
            } else {
                logger.info("Setting CosmosChangeFeedRequestOptionsAccessor...");
                cosmosChangeFeedRequestOptionsClassLoaded.set(true);
            }
        }

        public static CosmosChangeFeedRequestOptionsAccessor getCosmosChangeFeedRequestOptionsAccessor() {
            if (!cosmosChangeFeedRequestOptionsClassLoaded.get()) {
                logger.debug("Initializing CosmosChangeFeedRequestOptionsAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosItemRequestOptionsAccessor already initialized!");
            } else {
                logger.info("Setting CosmosItemRequestOptionsAccessor...");
                cosmosItemRequestOptionsClassLoaded.set(true);
            }
        }

        public static CosmosItemRequestOptionsAccessor getCosmosItemRequestOptionsAccessor() {
            if (!cosmosItemRequestOptionsClassLoaded.get()) {
                logger.debug("Initializing CosmosItemRequestOptionsAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosBulkExecutionOptionsAccessor already initialized!");
            } else {
                logger.info("Setting CosmosBulkExecutionOptionsAccessor...");
                cosmosBulkExecutionOptionsClassLoaded.set(true);
            }
        }

        public static CosmosBulkExecutionOptionsAccessor getCosmosBulkExecutionOptionsAccessor() {
            if (!cosmosBulkExecutionOptionsClassLoaded.get()) {
                logger.debug("Initializing CosmosBulkExecutionOptionsAccessor...");
                initializeAllAccessors();
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

    public static final class CosmosClientHelper {
        private final static AtomicBoolean cosmosClientClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosClientAccessor> accessor = new AtomicReference<>();

        private CosmosClientHelper() {
        }

        public static void setCosmosClientAccessor(final CosmosClientAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosClientAccessor already initialized!");
            } else {
                logger.info("Setting CosmosClientAccessor...");
                cosmosClientClassLoaded.set(true);
            }
        }

        public static CosmosClientAccessor getCosmosClientAccessor() {
            if (!cosmosClientClassLoaded.get()) {
                logger.debug("Initializing CosmosClientAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosContainerPropertiesAccessor already initialized!");
            } else {
                logger.info("Setting CosmosContainerPropertiesAccessor...");
                cosmosContainerPropertiesClassLoaded.set(true);
            }
        }

        public static CosmosContainerPropertiesAccessor getCosmosContainerPropertiesAccessor() {
            if (!cosmosContainerPropertiesClassLoaded.get()) {
                logger.debug("Initializing CosmosContainerPropertiesAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosPageFluxAccessor already initialized!");
            } else {
                logger.info("Setting CosmosPageFluxAccessor...");
                cosmosPagedFluxClassLoaded.set(true);
            }
        }

        public static <T> CosmosPageFluxAccessor getCosmosPageFluxAccessor() {
            if (!cosmosPagedFluxClassLoaded.get()) {
                logger.debug("Initializing CosmosPageFluxAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosAsyncDatabaseAccessor already initialized!");
            } else {
                logger.info("Setting CosmosAsyncDatabaseAccessor...");
                cosmosAsyncDatabaseClassLoaded.set(true);
            }
        }

        public static <T> CosmosAsyncDatabaseAccessor getCosmosAsyncDatabaseAccessor() {
            if (!cosmosAsyncDatabaseClassLoaded.get()) {
                logger.debug("Initializing CosmosAsyncDatabaseAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosBulkExecutionThresholdsStateAccessor already initialized!");
            } else {
                logger.info("Setting CosmosBulkExecutionThresholdsStateAccessor...");
                cosmosBulkExecutionThresholdsStateClassLoaded.set(true);
            }
        }

        public static CosmosBulkExecutionThresholdsStateAccessor getBulkExecutionThresholdsAccessor() {
            if (!cosmosBulkExecutionThresholdsStateClassLoaded.get()) {
                logger.debug("Initializing CosmosBulkExecutionThresholdsStateAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosDiagnosticsAccessor already initialized!");
            } else {
                logger.info("Setting CosmosDiagnosticsAccessor...");
                cosmosDiagnosticsClassLoaded.set(true);
            }
        }

        public static CosmosDiagnosticsAccessor getCosmosDiagnosticsAccessor() {
            if (!cosmosDiagnosticsClassLoaded.get()) {
                logger.debug("Initializing CosmosDiagnosticsAccessor...");
                initializeAllAccessors();
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

        public static void setCosmosAsyncContainerAccessor(final CosmosAsyncContainerAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosAsyncContainerAccessor already initialized!");
            } else {
                logger.info("Setting CosmosAsyncContainerAccessor...");
                cosmosAsyncContainerClassLoaded.set(true);
            }
        }

        public static CosmosAsyncContainerAccessor getCosmosAsyncContainerAccessor() {
            if (!cosmosAsyncContainerClassLoaded.get()) {
                logger.debug("Initializing CosmosAsyncContainerAccessor...");
                initializeAllAccessors();
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

    public static final class CosmosBatchRequestOptionsHelper {
        private final static AtomicBoolean cosmosBatchRequestOptionsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosBatchRequestOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosBatchRequestOptionsHelper() {
        }

        public static CosmosBatchRequestOptionsAccessor getCosmosBatchRequestOptionsAccessor() {
            if (!cosmosBatchRequestOptionsClassLoaded.get()) {
                logger.debug("Initializing CosmosBatchRequestOptionsAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosBatchRequestOptionsAccessor already initialized!");
            } else {
                logger.info("Setting CosmosBatchRequestOptionsAccessor...");
                cosmosBatchRequestOptionsClassLoaded.set(true);
            }
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
            if (!cosmosBatchOperationResultClassLoaded.get()) {
                logger.debug("Initializing CosmosBatchOperationResultAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosBatchOperationResultAccessor already initialized!");
            } else {
                logger.info("Setting CosmosBatchOperationResultAccessor...");
                cosmosBatchOperationResultClassLoaded.set(true);
            }
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
            if (!cosmosPatchOperationsClassLoaded.get()) {
                logger.debug("Initializing CosmosPatchOperationsAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosPatchOperationsAccessor already initialized!");
            } else {
                logger.info("Setting CosmosPatchOperationsAccessor...");
                cosmosPatchOperationsClassLoaded.set(true);
            }
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
            if (!cosmosBatchClassLoaded.get()) {
                logger.debug("Initializing CosmosBatchAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosBatchAccessor already initialized!");
            } else {
                logger.info("Setting CosmosBatchAccessor...");
                cosmosBatchClassLoaded.set(true);
            }
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
            if (!cosmosBulkItemResponseClassLoaded.get()) {
                logger.debug("Initializing CosmosBulkItemResponseAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosBulkItemResponseAccessor already initialized!");
            } else {
                logger.info("Setting CosmosBulkItemResponseAccessor...");
                cosmosBulkItemResponseClassLoaded.set(true);
            }
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
            if (!cosmosBatchResponseClassLoaded.get()) {
                logger.debug("Initializing CosmosBatchResponseAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosBatchResponseAccessor already initialized!");
            } else {
                logger.info("Setting CosmosBatchResponseAccessor...");
                cosmosBatchResponseClassLoaded.set(true);
            }
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
            if (!cosmosAsyncClientEncryptionKeyClassLoaded.get()) {
                logger.debug("Initializing CosmosAsyncClientEncryptionKeyAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosAsyncClientEncryptionKeyAccessor already initialized!");
            } else {
                logger.info("Setting CosmosAsyncClientEncryptionKeyAccessor...");
                cosmosAsyncClientEncryptionKeyClassLoaded.set(true);
            }
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
            if (!cosmosExceptionClassLoaded.get()) {
                logger.debug("Initializing CosmosExceptionAccessor...");
                initializeAllAccessors();
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
                logger.debug("CosmosExceptionAccessor already initialized!");
            } else {
                logger.info("Setting CosmosExceptionAccessor...");
                cosmosExceptionClassLoaded.set(true);
            }
        }

        public interface CosmosExceptionAccessor {
            CosmosException createCosmosException(int statusCode, Exception innerException);
        }
    }
}
