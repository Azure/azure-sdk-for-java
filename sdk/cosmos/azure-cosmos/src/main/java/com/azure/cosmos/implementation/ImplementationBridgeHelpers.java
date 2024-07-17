// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncClientEncryptionKey;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosRegionSwitchHint;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.CosmosOperationPolicy;
import com.azure.cosmos.CosmosRequestContext;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GlobalThroughputControlConfig;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.batch.BulkExecutorDiagnosticsTracker;
import com.azure.cosmos.implementation.batch.ItemBatchOperation;
import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.CosmosMeterOptions;
import com.azure.cosmos.implementation.clienttelemetry.MetricCategory;
import com.azure.cosmos.implementation.clienttelemetry.TagName;
import com.azure.cosmos.implementation.directconnectivity.ContainerDirectConnectionMetadata;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelStatistics;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
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
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosMetricName;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PriorityLevel;
import com.azure.cosmos.models.CosmosOperationDetails;
import com.azure.cosmos.models.ShowQueryMode;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ImplementationBridgeHelpers {
    private final static Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);

    private static void  initializeAllAccessors() {
        ModelBridgeInternal.initializeAllAccessors();
        UtilBridgeInternal.initializeAllAccessors();
        BridgeInternal.initializeAllAccessors();
    }

    public static final class CosmosClientBuilderHelper {
        private static final AtomicReference<CosmosClientBuilderAccessor> accessor = new AtomicReference<>();
        private static final AtomicBoolean cosmosClientBuilderClassLoaded = new AtomicBoolean(false);

        private CosmosClientBuilderHelper() {}

        public static void setCosmosClientBuilderAccessor(final CosmosClientBuilderAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosClientBuilderAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosClientBuilderAccessor...");
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

            ConnectionPolicy buildConnectionPolicy(CosmosClientBuilder builder);

            Configs getConfigs(CosmosClientBuilder builder);

            ConsistencyLevel getConsistencyLevel(CosmosClientBuilder builder);

            String getEndpoint(CosmosClientBuilder builder);

            CosmosItemSerializer getDefaultCustomSerializer(CosmosClientBuilder builder);

            void setRegionScopedSessionCapturingEnabled(CosmosClientBuilder builder, boolean isRegionScopedSessionCapturingEnabled);

            boolean getRegionScopedSessionCapturingEnabled(CosmosClientBuilder builder);
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
                logger.debug("Setting PartitionKeyAccessor...");
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
            }

            return snapshot;
        }

        public interface PartitionKeyAccessor {
            PartitionKey toPartitionKey(PartitionKeyInternal partitionKeyInternal);
            PartitionKey toPartitionKey(List<Object> values, boolean strict);
            PartitionKeyInternal getPartitionKeyInternal(PartitionKey partitionKey);
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
                logger.debug("Setting DirectConnectionConfigAccessor...");
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
            DirectConnectionConfig setHealthCheckTimeoutDetectionEnabled(
                DirectConnectionConfig directConnectionConfig, boolean timeoutDetectionEnabled);
            boolean isHealthCheckTimeoutDetectionEnabled(DirectConnectionConfig directConnectionConfig);
            DirectConnectionConfig setMinConnectionPoolSizePerEndpoint(DirectConnectionConfig directConnectionConfig, int minConnectionPoolSizePerEndpoint);

            int getMinConnectionPoolSizePerEndpoint(DirectConnectionConfig directConnectionConfig);
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
                logger.debug("Setting CosmosQueryRequestOptionsAccessor...");
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
            }

            return snapshot;
        }

        public interface CosmosQueryRequestOptionsAccessor {
            CosmosQueryRequestOptionsBase<?> getImpl(CosmosQueryRequestOptions options);
            CosmosQueryRequestOptions clone(CosmosQueryRequestOptions toBeCloned);
            CosmosQueryRequestOptions clone(CosmosQueryRequestOptionsBase<?> toBeCloned);
            boolean isQueryPlanRetrievalDisallowed(CosmosQueryRequestOptions queryRequestOptions);
            CosmosQueryRequestOptions disallowQueryPlanRetrieval(CosmosQueryRequestOptions queryRequestOptions);
            boolean isEmptyPageDiagnosticsEnabled(CosmosQueryRequestOptions queryRequestOptions);
            String getQueryNameOrDefault(CosmosQueryRequestOptions queryRequestOptions, String defaultQueryName);
            RequestOptions toRequestOptions(CosmosQueryRequestOptions queryRequestOptions);
            List<CosmosDiagnostics> getCancelledRequestDiagnosticsTracker(CosmosQueryRequestOptions options);
            void setCancelledRequestDiagnosticsTracker(
                CosmosQueryRequestOptions options,
                List<CosmosDiagnostics> cancelledRequestDiagnosticsTracker);
            void setAllowEmptyPages(CosmosQueryRequestOptions options, boolean emptyPagesAllowed);

            boolean getAllowEmptyPages(CosmosQueryRequestOptions options);

            Integer getMaxItemCount(CosmosQueryRequestOptions options);

            String getRequestContinuation(CosmosQueryRequestOptions options);

            Integer getMaxItemCountForVectorSearch(CosmosQueryRequestOptions options);

            void setPartitionKeyDefinition(CosmosQueryRequestOptions options, PartitionKeyDefinition partitionKeyDefinition);

            PartitionKeyDefinition getPartitionKeyDefinition(CosmosQueryRequestOptions options);

            void setCollectionRid(CosmosQueryRequestOptions options, String collectionRid);

            String getCollectionRid(CosmosQueryRequestOptions options);
        }
    }

    public static final class CosmosReadManyRequestOptionsHelper {
        private final static AtomicBoolean cosmosReadManyRequestOptionsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosReadManyRequestOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosReadManyRequestOptionsHelper() {}

        public static void setCosmosReadManyRequestOptionsAccessor(final CosmosReadManyRequestOptionsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosReadManyRequestOptionsAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosReadManyRequestOptionsAccessor...");
                cosmosReadManyRequestOptionsClassLoaded.set(true);
            }
        }

        public static CosmosReadManyRequestOptionsAccessor getCosmosReadManyRequestOptionsAccessor() {
            if (!cosmosReadManyRequestOptionsClassLoaded.get()) {
                logger.debug("Initializing CosmosReadManyRequestOptionsAccessor...");
                initializeAllAccessors();
            }

            CosmosReadManyRequestOptionsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosReadManyRequestOptionsAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public interface CosmosReadManyRequestOptionsAccessor {
            CosmosQueryRequestOptionsBase<?> getImpl(CosmosReadManyRequestOptions options);
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
                logger.debug("Setting CosmosChangeFeedRequestOptionsAccessor...");
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
            }

            return snapshot;
        }

        public interface CosmosChangeFeedRequestOptionsAccessor {
            CosmosChangeFeedRequestOptions setHeader(CosmosChangeFeedRequestOptions changeFeedRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosChangeFeedRequestOptions changeFeedRequestOptions);
            CosmosChangeFeedRequestOptionsImpl getImpl(CosmosChangeFeedRequestOptions changeFeedRequestOptions);
            void setOperationContext(CosmosChangeFeedRequestOptions changeFeedRequestOptions, OperationContextAndListenerTuple operationContext);
            OperationContextAndListenerTuple getOperationContext(CosmosChangeFeedRequestOptions changeFeedRequestOptions);
            CosmosDiagnosticsThresholds getDiagnosticsThresholds(CosmosChangeFeedRequestOptions options);
            CosmosChangeFeedRequestOptions createForProcessingFromContinuation(String continuation, FeedRange targetRange, String continuationLsn);

            CosmosChangeFeedRequestOptions clone(CosmosChangeFeedRequestOptions toBeCloned);

            String getCollectionRid(CosmosChangeFeedRequestOptions changeFeedRequestOptions);

            void setCollectionRid(CosmosChangeFeedRequestOptions changeFeedRequestOptions, String collectionRid);

            PartitionKeyDefinition getPartitionKeyDefinition(CosmosChangeFeedRequestOptions changeFeedRequestOptions);

            void setPartitionKeyDefinition(CosmosChangeFeedRequestOptions changeFeedRequestOptions, PartitionKeyDefinition partitionKeyDefinition);
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
                logger.debug("Setting CosmosItemRequestOptionsAccessor...");
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
            }

            return snapshot;
        }

        public interface CosmosItemRequestOptionsAccessor {
            RequestOptions toRequestOptions(CosmosItemRequestOptions itemRequestOptions);
            void setOperationContext(CosmosItemRequestOptions queryRequestOptions, OperationContextAndListenerTuple operationContext);
            OperationContextAndListenerTuple getOperationContext(CosmosItemRequestOptions queryRequestOptions);
            CosmosItemRequestOptions clone(CosmosItemRequestOptions options);
            CosmosItemRequestOptions setHeader(CosmosItemRequestOptions cosmosItemRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosItemRequestOptions cosmosItemRequestOptions);
            CosmosDiagnosticsThresholds getDiagnosticsThresholds(CosmosItemRequestOptions cosmosItemRequestOptions);
            CosmosEndToEndOperationLatencyPolicyConfig getEndToEndOperationLatencyPolicyConfig(
                CosmosItemRequestOptions options);

            CosmosPatchItemRequestOptions clonePatchItemRequestOptions(CosmosPatchItemRequestOptions options);
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
                logger.debug("Setting CosmosBulkExecutionOptionsAccessor...");
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
            }

            return snapshot;
        }

        public interface CosmosBulkExecutionOptionsAccessor {

            void setOperationContext(CosmosBulkExecutionOptions options,
                                     OperationContextAndListenerTuple operationContextAndListenerTuple);

            OperationContextAndListenerTuple getOperationContext(CosmosBulkExecutionOptions options);

            Duration getMaxMicroBatchInterval(CosmosBulkExecutionOptions options);

            CosmosBulkExecutionOptions setTargetedMicroBatchRetryRate(
                CosmosBulkExecutionOptions options,
                double minRetryRate,
                double maxRetryRate);

            @SuppressWarnings({"unchecked"})
            <T> T getLegacyBatchScopedContext(CosmosBulkExecutionOptions options);

            double getMinTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options);

            double getMaxTargetedMicroBatchRetryRate(CosmosBulkExecutionOptions options);

            int getMaxMicroBatchPayloadSizeInBytes(CosmosBulkExecutionOptions options);

            CosmosBulkExecutionOptions setMaxMicroBatchPayloadSizeInBytes(
                CosmosBulkExecutionOptions options,
                int maxMicroBatchPayloadSizeInBytes);

            int getMaxMicroBatchConcurrency(CosmosBulkExecutionOptions options);

            Integer getMaxConcurrentCosmosPartitions(CosmosBulkExecutionOptions options);

            CosmosBulkExecutionOptions setMaxConcurrentCosmosPartitions(
                CosmosBulkExecutionOptions options, int mxConcurrentCosmosPartitions);

            CosmosBulkExecutionOptions setHeader(CosmosBulkExecutionOptions cosmosBulkExecutionOptions,
                                                 String name, String value);

            Map<String, String> getHeader(CosmosBulkExecutionOptions cosmosBulkExecutionOptions);

            Map<String, String> getCustomOptions(CosmosBulkExecutionOptions cosmosBulkExecutionOptions);

            int getMaxMicroBatchSize(CosmosBulkExecutionOptions cosmosBulkExecutionOptions);

            void setDiagnosticsTracker(CosmosBulkExecutionOptions cosmosBulkExecutionOptions, BulkExecutorDiagnosticsTracker tracker);

            BulkExecutorDiagnosticsTracker getDiagnosticsTracker(CosmosBulkExecutionOptions cosmosBulkExecutionOptions);

            CosmosBulkExecutionOptions setSchedulerOverride(CosmosBulkExecutionOptions cosmosBulkExecutionOptions, Scheduler customScheduler);

            CosmosBulkExecutionOptions clone(CosmosBulkExecutionOptions toBeCloned);
            CosmosBulkExecutionOptionsImpl getImpl(CosmosBulkExecutionOptions options);
        }
    }

    public static final class CosmosItemResponseHelper {
        private final static AtomicBoolean cosmosItemResponseClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosItemResponseBuilderAccessor> accessor = new AtomicReference<>();

        private CosmosItemResponseHelper() {
        }


        public static void setCosmosItemResponseBuilderAccessor(final CosmosItemResponseBuilderAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosItemResponseBuilderAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosItemResponseBuilderAccessor...");
                cosmosItemResponseClassLoaded.set(true);
            }
        }

        public static CosmosItemResponseBuilderAccessor getCosmosItemResponseBuilderAccessor() {
            if (!cosmosItemResponseClassLoaded.get()) {
                logger.debug("Initializing CosmosItemResponseBuilderAccessor...");
                initializeAllAccessors();
            }

            CosmosItemResponseBuilderAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosItemResponseBuilderAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public interface CosmosItemResponseBuilderAccessor {
            <T> CosmosItemResponse<T> createCosmosItemResponse(CosmosItemResponse<byte[]> response,
                                                               Class<T> classType,
                                                               CosmosItemSerializer serializer);

            <T> CosmosItemResponse<T> createCosmosItemResponse(ResourceResponse<Document> response,
                                                               Class<T> classType,
                                                               CosmosItemSerializer serializer);


            <T> CosmosItemResponse<T> withRemappedStatusCode(
                CosmosItemResponse<T> originalResponse,
                int newStatusCode,
                double additionalRequestCharge,
                boolean isContentResponseOnWriteEnabled);

            byte[] getByteArrayContent(CosmosItemResponse<byte[]> response);

            void setByteArrayContent(CosmosItemResponse<byte[]> response, Pair<byte[], JsonNode> content);

            ResourceResponse<Document> getResourceResponse(CosmosItemResponse<byte[]> response);

            boolean hasTrackingId(CosmosItemResponse<?> response, String candidate);
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
                logger.debug("Setting CosmosClientAccessor...");
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
                logger.debug("Setting CosmosContainerPropertiesAccessor...");
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
            }

            return snapshot;
        }

        public interface CosmosContainerPropertiesAccessor {
            CosmosContainerProperties create(DocumentCollection documentCollection);
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
                logger.debug("Setting CosmosPageFluxAccessor...");
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
                logger.debug("Setting CosmosAsyncDatabaseAccessor...");
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
                logger.debug("Setting CosmosBulkExecutionThresholdsStateAccessor...");
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

    public static final class CosmosOperationDetailsHelper {
        private final static AtomicBoolean cosmosOperationDetailsClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosOperationDetailsAccessor> accessor = new AtomicReference<>();

        private CosmosOperationDetailsHelper() {
        }

        public static void setCosmosOperationDetailsAccessor(final CosmosOperationDetailsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosOperationDetailsAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosOperationDetailsAccessor ...");
                cosmosOperationDetailsClassLoaded.set(true);
            }
        }

        public static CosmosOperationDetailsAccessor getCosmosOperationDetailsAccessor() {
            if (!cosmosOperationDetailsClassLoaded.get()) {
                logger.debug("Initializing CosmosOperationDetailsAccessor...");
                initializeAllAccessors();
            }

            CosmosOperationDetailsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosOperationDetailsAccessor is not initialized yet!");

            }

            return snapshot;
        }

        public interface CosmosOperationDetailsAccessor {
            CosmosOperationDetails create(OverridableRequestOptions requestOptions, CosmosDiagnosticsContext diagnosticsContext);
        }
    }

    public static final class CosmosRequestContextHelper {
        private final static AtomicBoolean cosmosRequestContextClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosRequestContextAccessor> accessor = new AtomicReference<>();

        private CosmosRequestContextHelper() {
        }

        public static void setCosmosRequestContextAccessor(final CosmosRequestContextAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosRequestContextAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosRequestContextAccessor ...");
                cosmosRequestContextClassLoaded.set(true);
            }
        }

        public static CosmosRequestContextAccessor getCosmosRequestContextAccessor() {
            if (!cosmosRequestContextClassLoaded.get()) {
                logger.debug("Initializing CosmosRequestContextAccessor...");
                initializeAllAccessors();
            }

            CosmosRequestContextAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosRequestContextAccessor is not initialized yet!");

            }

            return snapshot;
        }

        public interface CosmosRequestContextAccessor {
            CosmosRequestContext create(OverridableRequestOptions requestOptions);
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
                logger.debug("Setting CosmosDiagnosticsAccessor...");
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
            }

            return snapshot;
        }

        public interface CosmosDiagnosticsAccessor {
            FeedResponseDiagnostics getFeedResponseDiagnostics(CosmosDiagnostics cosmosDiagnostics);
            AtomicBoolean isDiagnosticsCapturedInPagedFlux(CosmosDiagnostics cosmosDiagnostics);
            Collection<ClientSideRequestStatistics> getClientSideRequestStatistics(CosmosDiagnostics cosmosDiagnostics);

            Collection<ClientSideRequestStatistics> getClientSideRequestStatisticsForQueryPipelineAggregations(CosmosDiagnostics cosmosDiagnostics);
            int getTotalResponsePayloadSizeInBytes(CosmosDiagnostics cosmosDiagnostics);
            int getRequestPayloadSizeInBytes(CosmosDiagnostics cosmosDiagnostics);
            ClientSideRequestStatistics getClientSideRequestStatisticsRaw(CosmosDiagnostics cosmosDiagnostics);
            void addClientSideDiagnosticsToFeed(
                CosmosDiagnostics cosmosDiagnostics,
                Collection<ClientSideRequestStatistics> requestStatistics);

            void setSamplingRateSnapshot(CosmosDiagnostics cosmosDiagnostics, double samplingRate);

            CosmosDiagnostics create(DiagnosticsClientContext clientContext, double samplingRate);
            void recordAddressResolutionEnd(
                RxDocumentServiceRequest request,
                String identifier,
                String errorMessage,
                long transportRequestId);

            boolean isNotEmpty(CosmosDiagnostics cosmosDiagnostics);

            void setDiagnosticsContext(CosmosDiagnostics cosmosDiagnostics, CosmosDiagnosticsContext ctx);

            URI getFirstContactedLocationEndpoint(CosmosDiagnostics cosmosDiagnostics);

            void mergeMetadataDiagnosticContext(CosmosDiagnostics cosmosDiagnostics, MetadataDiagnosticsContext otherMetadataDiagnosticsContext);

            void mergeSerializationDiagnosticContext(CosmosDiagnostics cosmosDiagnostics, SerializationDiagnosticsContext otherSerializationDiagnosticsContext);
        }
    }

    public static final class CosmosDiagnosticsContextHelper {
        private final static AtomicBoolean cosmosDiagnosticsContextClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosDiagnosticsContextAccessor> accessor = new AtomicReference<>();

        private CosmosDiagnosticsContextHelper() {
        }

        public static void setCosmosDiagnosticsContextAccessor(final CosmosDiagnosticsContextAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosDiagnosticsContextAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosDiagnosticsContextAccessor...");
                cosmosDiagnosticsContextClassLoaded.set(true);
            }
        }

        public static CosmosDiagnosticsContextAccessor getCosmosDiagnosticsContextAccessor() {
            if (!cosmosDiagnosticsContextClassLoaded.get()) {
                logger.debug("Initializing CosmosDiagnosticsAccessor...");
                initializeAllAccessors();
            }

            CosmosDiagnosticsContextAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosDiagnosticsAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public interface CosmosDiagnosticsContextAccessor {
            CosmosDiagnosticsContext create(
                String spanName,
                String account,
                String endpoint,
                String databaseId,
                String containerId,
                ResourceType resourceType,
                OperationType operationType,
                String operationId,
                ConsistencyLevel consistencyLevel,
                Integer maxItemCount,
                CosmosDiagnosticsThresholds thresholds,
                String trackingId,
                String connectionMode,
                String userAgent,
                Integer sequenceNumber,
                String queryStatement,
                OverridableRequestOptions requestOptions);

            OverridableRequestOptions getRequestOptions(CosmosDiagnosticsContext ctx);

            void setRequestOptions(CosmosDiagnosticsContext ctx, OverridableRequestOptions requestOptions);

            CosmosDiagnosticsSystemUsageSnapshot createSystemUsageSnapshot(
                String cpu,
                String used,
                String available,
                int cpuCount);

            void startOperation(CosmosDiagnosticsContext ctx);

            void recordOperation(
                CosmosDiagnosticsContext ctx,
                int statusCode,
                int subStatusCode,
                Integer actualItemCount,
                Double requestCharge,
                CosmosDiagnostics diagnostics,
                Throwable finalError);

            boolean endOperation(
                CosmosDiagnosticsContext ctx,
                int statusCode,
                int subStatusCode,
                Integer actualItemCount,
                Double requestCharge,
                CosmosDiagnostics diagnostics,
                Throwable finalError);

            void addRequestCharge(CosmosDiagnosticsContext ctx, float requestCharge);

            void addRequestSize(CosmosDiagnosticsContext ctx, int bytes);

            void addResponseSize(CosmosDiagnosticsContext ctx, int bytes);

            void addDiagnostics(CosmosDiagnosticsContext ctx, CosmosDiagnostics diagnostics);

            Collection<CosmosDiagnostics> getDiagnostics(CosmosDiagnosticsContext ctx);

            ResourceType getResourceType(CosmosDiagnosticsContext ctx);

            OperationType getOperationType(CosmosDiagnosticsContext ctx);

            String getEndpoint(CosmosDiagnosticsContext ctx);

            Collection<ClientSideRequestStatistics> getDistinctCombinedClientSideRequestStatistics(CosmosDiagnosticsContext ctx);

            String getSpanName(CosmosDiagnosticsContext ctx);

            void setSamplingRateSnapshot(CosmosDiagnosticsContext ctx, double samplingRate, boolean isSampledOut);

            Integer getSequenceNumber(CosmosDiagnosticsContext ctx);

            boolean isEmptyCompletion(CosmosDiagnosticsContext ctx);

            String getQueryStatement(CosmosDiagnosticsContext ctx);

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
                logger.debug("Setting CosmosAsyncContainerAccessor...");
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
            }

            return snapshot;
        }

        public interface CosmosAsyncContainerAccessor {
            <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryChangeFeedInternalFunc(
                CosmosAsyncContainer cosmosAsyncContainer,
                CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions,
                Class<T> classType);

            void enableLocalThroughputControlGroup(
                CosmosAsyncContainer cosmosAsyncContainer,
                ThroughputControlGroupConfig groupConfig,
                Mono<Integer> throughputQueryMono);

            void enableGlobalThroughputControlGroup(
                CosmosAsyncContainer cosmosAsyncContainer,
                ThroughputControlGroupConfig groupConfig,
                GlobalThroughputControlConfig globalControlConfig,
                Mono<Integer> throughputQueryMono);

            IFaultInjectorProvider getOrConfigureFaultInjectorProvider(
                CosmosAsyncContainer cosmosAsyncContainer,
                Callable<IFaultInjectorProvider> injectorProviderCallable);

            <T> Mono<FeedResponse<T>> readMany(
                CosmosAsyncContainer cosmosAsyncContainer,
                List<CosmosItemIdentity> itemIdentityList,
                CosmosReadManyRequestOptions requestOptions,
                Class<T> classType);

            <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryItemsInternalFunc(
                CosmosAsyncContainer cosmosAsyncContainer,
                SqlQuerySpec sqlQuerySpec,
                CosmosQueryRequestOptions cosmosQueryRequestOptions,
                Class<T> classType);

            <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryItemsInternalFuncWithMonoSqlQuerySpec(
                CosmosAsyncContainer cosmosAsyncContainer,
                Mono<SqlQuerySpec> sqlQuerySpecMono,
                CosmosQueryRequestOptions cosmosQueryRequestOptions,
                Class<T> classType);

            Mono<List<FeedRange>> getFeedRanges(CosmosAsyncContainer cosmosAsyncContainer, boolean forceRefresh);

            Mono<List<FeedRange>> trySplitFeedRange(
                CosmosAsyncContainer cosmosAsyncContainer,
                FeedRange feedRange,
                int targetedCountAfterSplit);

            String getLinkWithoutTrailingSlash(CosmosAsyncContainer cosmosAsyncContainer);
            Mono<Boolean> checkFeedRangeOverlapping(CosmosAsyncContainer container, FeedRange feedRange1, FeedRange feedRange2);
            Mono<List<FeedRange>> getOverlappingFeedRanges(CosmosAsyncContainer container, FeedRange feedRange, boolean forceRefresh);
            Mono<PartitionKeyDefinition> getPartitionKeyDefinition(CosmosAsyncContainer container);
        }
    }

    public static final class FeedResponseHelper {
        private final static AtomicBoolean feedResponseClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<FeedResponseAccessor> accessor = new AtomicReference<>();

        private FeedResponseHelper() {
        }

        public static void setFeedResponseAccessor(final FeedResponseAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("FeedResponseAccessor already initialized!");
            } else {
                logger.debug("Setting FeedResponseAccessor...");
                feedResponseClassLoaded.set(true);
            }
        }

        public static FeedResponseAccessor getFeedResponseAccessor() {
            if (!feedResponseClassLoaded.get()) {
                logger.debug("Initializing FeedResponseAccessor...");
                initializeAllAccessors();
            }

            FeedResponseAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("FeedResponseAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public interface FeedResponseAccessor {
            <T> FeedResponse<T> createFeedResponse(RxDocumentServiceResponse response,
                                                   CosmosItemSerializer itemSerializer,
                                                   Class<T> cls);

            <T> FeedResponse<T> createChangeFeedResponse(RxDocumentServiceResponse response,
                                                   CosmosItemSerializer itemSerializer,
                                                   Class<T> cls);

            <T> FeedResponse<T> createChangeFeedResponse(RxDocumentServiceResponse response,
                                                         CosmosItemSerializer itemSerializer,
                                                         Class<T> cls,
                                                         CosmosDiagnostics diagnostics);

            <T> boolean getNoChanges(FeedResponse<T> feedResponse);
            <TNew, T> FeedResponse<TNew> convertGenericType(FeedResponse<T> feedResponse, Function<T, TNew> conversion);
            <T> FeedResponse<T> createFeedResponse(
                List<T> results, Map<String, String> headers, CosmosDiagnostics diagnostics);
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
            }

            return snapshot;
        }

        public static void setCosmosBatchRequestOptionsAccessor(final CosmosBatchRequestOptionsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosBatchRequestOptionsAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosBatchRequestOptionsAccessor...");
                cosmosBatchRequestOptionsClassLoaded.set(true);
            }
        }

        public interface CosmosBatchRequestOptionsAccessor {
            ConsistencyLevel getConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions);
            CosmosBatchRequestOptions setConsistencyLevel(CosmosBatchRequestOptions cosmosBatchRequestOptions,
                                                          ConsistencyLevel consistencyLevel);
            CosmosBatchRequestOptions setHeader(CosmosBatchRequestOptions cosmosItemRequestOptions, String name, String value);
            Map<String, String> getHeader(CosmosBatchRequestOptions cosmosItemRequestOptions);
            CosmosBatchRequestOptions clone(CosmosBatchRequestOptions toBeCloned);
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
            }

            return snapshot;
        }

        public static void setCosmosBatchOperationResultAccessor(final CosmosBatchOperationResultAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosBatchOperationResultAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosBatchOperationResultAccessor...");
                cosmosBatchOperationResultClassLoaded.set(true);
            }
        }

        public interface CosmosBatchOperationResultAccessor {
            ObjectNode getResourceObject(CosmosBatchOperationResult cosmosBatchOperationResult);
            void setResourceObject(CosmosBatchOperationResult cosmosBatchOperationResult, ObjectNode objectNode);
            void setEffectiveItemSerializer(CosmosBatchOperationResult cosmosBatchOperationResult,
                                            CosmosItemSerializer effectiveItemSerializer);
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
            }

            return snapshot;
        }

        public static void setCosmosPatchOperationsAccessor(CosmosPatchOperationsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosPatchOperationsAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosPatchOperationsAccessor...");
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
            }

            return snapshot;
        }

        public static void setCosmosBatchAccessor(CosmosBatchAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosBatchAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosBatchAccessor...");
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
            }

            return snapshot;
        }

        public static void setCosmosBulkItemResponseAccessor(CosmosBulkItemResponseAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosBulkItemResponseAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosBulkItemResponseAccessor...");
                cosmosBulkItemResponseClassLoaded.set(true);
            }
        }

        public interface CosmosBulkItemResponseAccessor {
            ObjectNode getResourceObject(CosmosBulkItemResponse cosmosBulkItemResponse);

            void setResourceObject(CosmosBulkItemResponse cosmosBulkItemResponse,
                                   ObjectNode objectNode);

            void setEffectiveItemSerializer(CosmosBulkItemResponse cosmosBulkItemResponse,
                                            CosmosItemSerializer effectiveItemSerializer);
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
            }

            return snapshot;
        }

        public static void setCosmosBatchResponseAccessor(final CosmosBatchResponseAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosBatchResponseAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosBatchResponseAccessor...");
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
            }

            return snapshot;
        }

        public static void setCosmosAsyncClientEncryptionKeyAccessor(final CosmosAsyncClientEncryptionKeyAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosAsyncClientEncryptionKeyAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosAsyncClientEncryptionKeyAccessor...");
                cosmosAsyncClientEncryptionKeyClassLoaded.set(true);
            }
        }

        public interface CosmosAsyncClientEncryptionKeyAccessor {
            Mono<CosmosClientEncryptionKeyResponse> readClientEncryptionKey(CosmosAsyncClientEncryptionKey cosmosAsyncClientEncryptionKey,
                                                                            RequestOptions requestOptions);
        }
    }

    public static final class CosmosAsyncClientHelper {
        private static final AtomicReference<CosmosAsyncClientAccessor> accessor = new AtomicReference<>();
        private static final AtomicBoolean cosmosAsyncClientClassLoaded = new AtomicBoolean(false);

        private CosmosAsyncClientHelper() {}

        public static void setCosmosAsyncClientAccessor(final CosmosAsyncClientAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosAsyncClientAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosAsyncClientAccessor...");
                cosmosAsyncClientClassLoaded.set(true);
            }
        }

        public static CosmosAsyncClientAccessor getCosmosAsyncClientAccessor() {
            if (!cosmosAsyncClientClassLoaded.get()) {
                logger.debug("Initializing CosmosAsyncClientAccessor...");
                initializeAllAccessors();
            }

            CosmosAsyncClientAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosAsyncClientAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public interface CosmosAsyncClientAccessor {
            Tag getClientCorrelationTag(CosmosAsyncClient client);
            String getAccountTagValue(CosmosAsyncClient client);
            EnumSet<TagName> getMetricTagNames(CosmosAsyncClient client);
            EnumSet<MetricCategory> getMetricCategories(CosmosAsyncClient client);
            boolean shouldEnableEmptyPageDiagnostics(CosmosAsyncClient client);
            boolean isSendClientTelemetryToServiceEnabled(CosmosAsyncClient client);
            List<String> getPreferredRegions(CosmosAsyncClient client);
            boolean isEndpointDiscoveryEnabled(CosmosAsyncClient client);
            String getConnectionMode(CosmosAsyncClient client);
            String getUserAgent(CosmosAsyncClient client);
            CosmosMeterOptions getMeterOptions(CosmosAsyncClient client, CosmosMetricName name);
            boolean isEffectiveContentResponseOnWriteEnabled(
                CosmosAsyncClient client,
                Boolean requestOptionsContentResponseEnabled);

            ConsistencyLevel getEffectiveConsistencyLevel(
                CosmosAsyncClient client,
                OperationType operationType,
                ConsistencyLevel desiredConsistencyLevelOfOperation);

            CosmosDiagnosticsThresholds getEffectiveDiagnosticsThresholds(
                CosmosAsyncClient client,
                CosmosDiagnosticsThresholds operationLevelThresholds);

            DiagnosticsProvider getDiagnosticsProvider(CosmosAsyncClient client);

            List<CosmosOperationPolicy> getOperationPolicies(CosmosAsyncClient client);

            CosmosItemSerializer getEffectiveItemSerializer(
                CosmosAsyncClient client,
                CosmosItemSerializer requestOptionsItemSerializer);
        }
    }

    public static final class CosmosDiagnosticsThresholdsHelper {
        private static final AtomicReference<CosmosDiagnosticsThresholdsAccessor> accessor = new AtomicReference<>();
        private static final AtomicBoolean cosmosDiagnosticsThresholdsClassLoaded = new AtomicBoolean(false);

        private CosmosDiagnosticsThresholdsHelper() {}

        public static void setCosmosDiagnosticsThresholdsAccessor(final CosmosDiagnosticsThresholdsAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosDiagnosticsThresholds already initialized!");
            } else {
                logger.debug("Setting CosmosDiagnosticsThresholds...");
                cosmosDiagnosticsThresholdsClassLoaded.set(true);
            }
        }

        public static CosmosDiagnosticsThresholdsAccessor getCosmosAsyncClientAccessor() {
            if (!cosmosDiagnosticsThresholdsClassLoaded.get()) {
                logger.debug("Initializing CosmosDiagnosticsThresholds...");
                initializeAllAccessors();
            }

            CosmosDiagnosticsThresholdsAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosDiagnosticsThresholdsAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public interface CosmosDiagnosticsThresholdsAccessor {
            Duration getPointReadLatencyThreshold(CosmosDiagnosticsThresholds thresholds);
            Duration getNonPointReadLatencyThreshold(CosmosDiagnosticsThresholds thresholds);
            float getRequestChargeThreshold(CosmosDiagnosticsThresholds thresholds);
            int getPayloadSizeThreshold(CosmosDiagnosticsThresholds thresholds);
            boolean isFailureCondition(CosmosDiagnosticsThresholds thresholds, int statusCode, int subStatusCode);
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
            }

            return snapshot;
        }

        public static void setCosmosExceptionAccessor(final CosmosExceptionAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosExceptionAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosExceptionAccessor...");
                cosmosExceptionClassLoaded.set(true);
            }
        }

        public interface CosmosExceptionAccessor {
            CosmosException createCosmosException(int statusCode, Exception innerException);
            Map<String, Set<String>> getReplicaStatusList(CosmosException cosmosException);
            CosmosException setRntbdChannelStatistics(CosmosException cosmosException, RntbdChannelStatistics rntbdChannelStatistics);
            RntbdChannelStatistics getRntbdChannelStatistics(CosmosException cosmosException);

            void setFaultInjectionRuleId(CosmosException cosmosException, String faultInjectionRuleId);
            String getFaultInjectionRuleId(CosmosException cosmosException);

            void setFaultInjectionEvaluationResults(CosmosException cosmosException, List<String> faultInjectionRuleEvaluationResults);
            List<String> getFaultInjectionEvaluationResults(CosmosException cosmosException);
            void setRequestUri(CosmosException cosmosException, Uri requestUri);
            Uri getRequestUri(CosmosException cosmosException);
        }
    }

    public static final class CosmosClientTelemetryConfigHelper {
        private final static AtomicBoolean cosmosClientTelemetryClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<CosmosClientTelemetryConfigAccessor> accessor = new AtomicReference<>();

        private CosmosClientTelemetryConfigHelper() {
        }

        public static CosmosClientTelemetryConfigAccessor getCosmosClientTelemetryConfigAccessor() {
            if (!cosmosClientTelemetryClassLoaded.get()) {
                logger.debug("Initializing CosmosClientTelemetryConfigAccessor...");
                initializeAllAccessors();
            }

            CosmosClientTelemetryConfigAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosClientTelemetryConfigAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public static void setCosmosClientTelemetryConfigAccessor(
            final CosmosClientTelemetryConfigAccessor newAccessor) {

            assert(newAccessor != null);

            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosClientTelemetryConfigAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosClientTelemetryConfigAccessor...");
                cosmosClientTelemetryClassLoaded.set(true);
            }
        }

        public interface CosmosClientTelemetryConfigAccessor {
            Duration getHttpNetworkRequestTimeout(CosmosClientTelemetryConfig config);
            int getMaxConnectionPoolSize(CosmosClientTelemetryConfig config);
            Duration getIdleHttpConnectionTimeout(CosmosClientTelemetryConfig config);
            ProxyOptions getProxy(CosmosClientTelemetryConfig config);
            EnumSet<MetricCategory> getMetricCategories(CosmosClientTelemetryConfig config);
            EnumSet<TagName> getMetricTagNames(CosmosClientTelemetryConfig config);
            String getClientCorrelationId(CosmosClientTelemetryConfig config);
            MeterRegistry getClientMetricRegistry(CosmosClientTelemetryConfig config);
            Boolean isSendClientTelemetryToServiceEnabled(CosmosClientTelemetryConfig config);
            boolean isClientMetricsEnabled(CosmosClientTelemetryConfig config);
            void resetIsSendClientTelemetryToServiceEnabled(CosmosClientTelemetryConfig config);
            CosmosMeterOptions getMeterOptions(CosmosClientTelemetryConfig config, CosmosMetricName name);
            CosmosMeterOptions createDisabledMeterOptions(CosmosMetricName name);
            CosmosClientTelemetryConfig createSnapshot(
                CosmosClientTelemetryConfig config,
                boolean effectiveIsClientTelemetryEnabled);
            Collection<CosmosDiagnosticsHandler> getDiagnosticHandlers(CosmosClientTelemetryConfig config);
            void setAccountName(CosmosClientTelemetryConfig config, String accountName);
            String getAccountName(CosmosClientTelemetryConfig config);
            void setClientCorrelationTag(CosmosClientTelemetryConfig config, Tag clientCorrelationTag);
            Tag getClientCorrelationTag(CosmosClientTelemetryConfig config);
            void setClientTelemetry(CosmosClientTelemetryConfig config, ClientTelemetry clientTelemetry);
            ClientTelemetry getClientTelemetry(CosmosClientTelemetryConfig config);
            void addDiagnosticsHandler(CosmosClientTelemetryConfig config, CosmosDiagnosticsHandler handler);
            CosmosDiagnosticsThresholds getDiagnosticsThresholds(CosmosClientTelemetryConfig config);
            boolean isLegacyTracingEnabled(CosmosClientTelemetryConfig config);
            boolean isTransportLevelTracingEnabled(CosmosClientTelemetryConfig config);
            Tracer getOrCreateTracer(CosmosClientTelemetryConfig config);
            void setUseLegacyTracing(CosmosClientTelemetryConfig config, boolean useLegacyTracing);
            void setTracer(CosmosClientTelemetryConfig config, Tracer tracer);
            double getSamplingRate(CosmosClientTelemetryConfig config);
            ShowQueryMode showQueryMode(CosmosClientTelemetryConfig config);
            double[] getDefaultPercentiles(CosmosClientTelemetryConfig config);
            boolean shouldPublishHistograms(CosmosClientTelemetryConfig config);
            boolean shouldApplyDiagnosticThresholdsForTransportLevelMeters(CosmosClientTelemetryConfig config);
        }
    }

    public static final class PriorityLevelHelper {
        private final static AtomicBoolean priorityLevelClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<PriorityLevelAccessor> accessor = new AtomicReference<>();

        private PriorityLevelHelper() {
        }

        public static PriorityLevelAccessor getPriorityLevelAccessor() {
            if (!priorityLevelClassLoaded.get()) {
                logger.debug("Initializing PriorityLevelAccessor...");
                initializeAllAccessors();
            }

            PriorityLevelAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("PriorityLevelAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public static void setPriorityLevelAccessor(final PriorityLevelAccessor newAccessor) {

            assert(newAccessor != null);

            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("PriorityLevelAccessor already initialized!");
            } else {
                logger.debug("Setting PriorityLevelAccessor...");
                priorityLevelClassLoaded.set(true);
            }
        }

        public interface PriorityLevelAccessor {
            byte getPriorityValue(PriorityLevel level);
        }
    }

    public static final class CosmosContainerIdentityHelper {

        private static final AtomicBoolean cosmosContainerIdentityClassLoaded = new AtomicBoolean(false);
        private static final AtomicReference<CosmosContainerIdentityAccessor> accessor = new AtomicReference<>();

        private CosmosContainerIdentityHelper() {}

        public static CosmosContainerIdentityAccessor getCosmosContainerIdentityAccessor() {

            if (!cosmosContainerIdentityClassLoaded.get()) {
                logger.debug("Initializing CosmosContainerIdentityAccessor...");
                initializeAllAccessors();
            }

            CosmosContainerIdentityAccessor snapshot = accessor.get();

            if (snapshot == null) {
                logger.error("CosmosContainerIdentityAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public static void setCosmosContainerIdentityAccessor(final CosmosContainerIdentityAccessor newAccessor) {

            assert (newAccessor != null);

            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosContainerIdentityAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosContainerIdentityAccessor...");
                cosmosContainerIdentityClassLoaded.set(true);
            }
        }

        public interface CosmosContainerIdentityAccessor {
            String getDatabaseName(CosmosContainerIdentity cosmosContainerIdentity);
            String getContainerName(CosmosContainerIdentity cosmosContainerIdentity);
            String getContainerLink(CosmosContainerIdentity cosmosContainerIdentity);
        }
    }

    public static final class CosmosContainerProactiveInitConfigHelper {

        private static final AtomicBoolean cosmosContainerProactiveInitConfigClassLoaded = new AtomicBoolean(false);
        private static final AtomicReference<CosmosContainerProactiveInitConfigAccessor> accessor = new AtomicReference<>();

        private CosmosContainerProactiveInitConfigHelper() {}

        public static CosmosContainerProactiveInitConfigAccessor getCosmosContainerProactiveInitConfigAccessor() {

            if (!cosmosContainerProactiveInitConfigClassLoaded.get()) {
                logger.debug("Initializing CosmosContainerProactiveInitConfigAccessor...");
                initializeAllAccessors();
            }

            CosmosContainerProactiveInitConfigAccessor snapshot = accessor.get();

            if (snapshot == null) {
                logger.error("CosmosContainerProactiveInitConfigAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public static void setCosmosContainerProactiveInitConfigAccessor(final CosmosContainerProactiveInitConfigAccessor newAccessor) {

            assert (newAccessor != null);

            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosContainerProactiveInitConfigAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosContainerProactiveInitConfigAccessor...");
                cosmosContainerProactiveInitConfigClassLoaded.set(true);
            }
        }

        public interface CosmosContainerProactiveInitConfigAccessor {
            Map<CosmosContainerIdentity, ContainerDirectConnectionMetadata> getContainerPropertiesMap(CosmosContainerProactiveInitConfig cosmosContainerProactiveInitConfig);
        }
    }

    public static final class CosmosSessionRetryOptionsHelper {
        private static final AtomicBoolean cosmosSessionRetryOptionsClassLoaded = new AtomicBoolean(false);
        private static final AtomicReference<CosmosSessionRetryOptionsAccessor> accessor = new AtomicReference<>();

        private CosmosSessionRetryOptionsHelper() {}

        public static CosmosSessionRetryOptionsAccessor getCosmosSessionRetryOptionsAccessor() {

            if (!cosmosSessionRetryOptionsClassLoaded.get()) {
                logger.debug("Initializing cosmosSessionRetryOptionsAccessor...");
                initializeAllAccessors();
            }

            CosmosSessionRetryOptionsAccessor snapshot = accessor.get();

            if (snapshot == null) {
                logger.error("cosmosSessionRetryOptionsAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public static void setCosmosSessionRetryOptionsAccessor(final CosmosSessionRetryOptionsAccessor newAccessor) {

            assert (newAccessor != null);

            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosSessionRetryOptionsAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosSessionRetryOptionsAccessor...");
                cosmosSessionRetryOptionsClassLoaded.set(true);
            }
        }

        public interface CosmosSessionRetryOptionsAccessor {
            CosmosRegionSwitchHint getRegionSwitchHint(SessionRetryOptions sessionRetryOptions);
            Duration getMinInRegionRetryTime(SessionRetryOptions sessionRetryOptions);

            int getMaxInRegionRetryCount(SessionRetryOptions sessionRetryOptions);
        }
    }

    public static final class CosmosItemSerializerHelper {
        private static final AtomicReference<CosmosItemSerializerAccessor> accessor = new AtomicReference<>();
        private static final AtomicBoolean cosmosItemSerializerClassLoaded = new AtomicBoolean(false);

        private CosmosItemSerializerHelper() {}

        public static void setCosmosItemSerializerAccessor(final CosmosItemSerializerAccessor newAccessor) {
            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("CosmosItemSerializerAccessor already initialized!");
            } else {
                logger.debug("Setting CosmosItemSerializerAccessor...");
                cosmosItemSerializerClassLoaded.set(true);
            }
        }

        public static CosmosItemSerializerAccessor getCosmosItemSerializerAccessor() {
            if (!cosmosItemSerializerClassLoaded.get()) {
                logger.debug("Initializing CosmosItemSerializerAccessor...");
                initializeAllAccessors();
            }

            CosmosItemSerializerAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("CosmosItemSerializerAccessor is not initialized yet!");
            }

            return snapshot;
        }

        public interface CosmosItemSerializerAccessor {
            <T> Map<String, Object> serializeSafe(CosmosItemSerializer serializer, T item);

            <T> T deserializeSafe(CosmosItemSerializer serializer, Map<String, Object> jsonNodeMap, Class<T> classType);

            void setShouldWrapSerializationExceptions(
                CosmosItemSerializer serializer,
                boolean shouldWrapSerializationExceptions);
        }
    }
}
