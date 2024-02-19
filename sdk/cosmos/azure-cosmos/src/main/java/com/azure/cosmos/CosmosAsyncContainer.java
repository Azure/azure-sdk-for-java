// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ChangeFeedOperationState;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ItemDeserializer;
import com.azure.cosmos.implementation.Offer;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyHelper;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.QueryFeedOperationState;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.WriteRetryPolicy;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.batch.BatchExecutor;
import com.azure.cosmos.implementation.batch.BulkExecutor;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.throughputControl.config.GlobalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.config.LocalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.config.ThroughputControlGroupFactory;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosBatchRequestOptions;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosConflictProperties;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchItemRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosReadManyRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Provides methods for reading, deleting, and replacing existing Containers.
 * Provides methods for interacting with child resources (Items, Scripts, Conflicts)
 */
public class CosmosAsyncContainer {
    private final static Logger logger = LoggerFactory.getLogger(CosmosAsyncContainer.class);
    private static final ImplementationBridgeHelpers.CosmosAsyncClientHelper.CosmosAsyncClientAccessor clientAccessor =
        ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();
    private static final ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor queryOptionsAccessor =
        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();

    private static final ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.CosmosItemRequestOptionsAccessor itemOptionsAccessor =
        ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.getCosmosItemRequestOptionsAccessor();

    private static final ImplementationBridgeHelpers.FeedResponseHelper.FeedResponseAccessor feedResponseAccessor =
        ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();
    private static final ImplementationBridgeHelpers.CosmosItemResponseHelper.CosmosItemResponseBuilderAccessor itemResponseAccessor =
        ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor();

    private final CosmosAsyncDatabase database;
    private final String id;
    private final String link;
    private final String replaceContainerSpanName;
    private final String deleteContainerSpanName;
    private final String replaceThroughputSpanName;
    private final String readThroughputSpanName;
    private final String readContainerSpanName;
    private final String readItemSpanName;
    private final String upsertItemSpanName;
    private final String deleteItemSpanName;
    private final String deleteAllItemsByPartitionKeySpanName;
    private final String replaceItemSpanName;
    private final String patchItemSpanName;
    private final String createItemSpanName;
    private final String readAllItemsSpanName;
    private final String readManyItemsSpanName;
    private final String readAllItemsOfLogicalPartitionSpanName;
    private final String queryItemsSpanName;
    private final String queryChangeFeedSpanName;
    private final String readAllConflictsSpanName;
    private final String queryConflictsSpanName;
    private final String batchSpanName;
    private final AtomicBoolean isInitialized;
    private CosmosAsyncScripts scripts;
    private IFaultInjectorProvider faultInjectorProvider;

    CosmosAsyncContainer(String id, CosmosAsyncDatabase database) {
        this.id = id;
        this.database = database;
        this.link = getParentLink() + "/" + getURIPathSegment() + "/" + getId();
        this.replaceContainerSpanName = "replaceContainer." + this.id;
        this.deleteContainerSpanName = "deleteContainer." + this.id;
        this.replaceThroughputSpanName = "replaceThroughput." + this.id;
        this.readThroughputSpanName = "readThroughput." + this.id;
        this.readContainerSpanName = "readContainer." + this.id;
        this.readItemSpanName = "readItem." + this.id;
        this.upsertItemSpanName = "upsertItem." + this.id;
        this.deleteItemSpanName = "deleteItem." + this.id;
        this.deleteAllItemsByPartitionKeySpanName = "deleteAllItemsByPartitionKey." + this.id;
        this.replaceItemSpanName = "replaceItem." + this.id;
        this.patchItemSpanName = "patchItem." + this.id;
        this.createItemSpanName = "createItem." + this.id;
        this.readAllItemsSpanName = "readAllItems." + this.id;
        this.readManyItemsSpanName = "readManyItems." + this.id;
        this.readAllItemsOfLogicalPartitionSpanName = "readAllItemsOfLogicalPartition." + this.id;
        this.queryItemsSpanName = "queryItems." + this.id;
        this.queryChangeFeedSpanName = "queryChangeFeed." + this.id;
        this.readAllConflictsSpanName = "readAllConflicts." + this.id;
        this.queryConflictsSpanName = "queryConflicts." + this.id;
        this.batchSpanName = "transactionalBatch." + this.id;
        this.isInitialized = new AtomicBoolean(false);
    }

    /**
     * Get the id of the {@link CosmosAsyncContainer}.
     *
     * @return the id of the {@link CosmosAsyncContainer}.
     */
    public String getId() {
        return id;
    }

    /**
     * Reads the current container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single Cosmos container response with
     * the read container or an error.
     */
    public Mono<CosmosContainerResponse> read() {
        return read(new CosmosContainerRequestOptions());
    }

    /**
     * Reads the current container while specifying additional options such as If-Match.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the read container. In case of failure the {@link Mono} will error.
     *
     * @param options the Cosmos container request options.
     * @return an {@link Mono} containing the single Cosmos container response with
     * the read container or an error.
     */
    public Mono<CosmosContainerResponse> read(CosmosContainerRequestOptions options) {
        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() : options;
        return withContext(context -> read(requestOptions, context));
    }

    /**
     * Deletes the container
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response for the
     * deleted database. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single Cosmos container response for
     * the deleted database or an error.
     */
    public Mono<CosmosContainerResponse> delete(CosmosContainerRequestOptions options) {
        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() : options;
        return withContext(context -> deleteInternal(requestOptions, context));
    }

    /**
     * Deletes the current container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response for the
     * deleted container. In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single Cosmos container response for
     * the deleted container or an error.
     */
    public Mono<CosmosContainerResponse> delete() {
        return delete(new CosmosContainerRequestOptions());
    }

    /**
     * Replaces the current container's properties.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the replaced container properties. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties
     * @return an {@link Mono} containing the single Cosmos container response with
     * the replaced container properties or an error.
     */
    public Mono<CosmosContainerResponse> replace(CosmosContainerProperties containerProperties) {
        return replace(containerProperties, null);
    }

    /**
     * Replaces the current container properties while using non-default request options.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single Cosmos container response with
     * the replaced container properties. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties the container properties
     * @param options the Cosmos container request options.
     * @return an {@link Mono} containing the single Cosmos container response with
     * the replaced container properties or an error.
     */
    public Mono<CosmosContainerResponse> replace(
        CosmosContainerProperties containerProperties,
        CosmosContainerRequestOptions options) {
        final CosmosContainerRequestOptions requestOptions = options == null ? new CosmosContainerRequestOptions() : options;
        return withContext(context -> replaceInternal(containerProperties, requestOptions, context));
    }

    /* CosmosAsyncItem operations */

    /**
     * Creates an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created Cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the Cosmos item represented as a POJO or Cosmos item object.
     * @return an {@link Mono} containing the single resource response with the
     * created Cosmos item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> createItem(T item) {
        return createItem(item, new CosmosItemRequestOptions());
    }

    /**
     * Creates an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * created Cosmos item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the Cosmos item represented as a POJO or Cosmos item object.
     * @param partitionKey the partition key.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the created Cosmos item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> createItem(
        T item,
        PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        return createItem(item, options);
    }

    /**
     * Creates a Cosmos item.
     *
     * @param <T> the type parameter.
     * @param item the item.
     * @param options the item request options.
     * @return an {@link Mono} containing the single resource response with the created Cosmos item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> createItem(T item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }

        final CosmosItemRequestOptions requestOptions = options;
        return withContext(context -> createItemInternal(item, requestOptions, context));
    }

    private static void mergeDiagnostics(CosmosException originalCosmosException, CosmosException readCosmosError) {
        checkNotNull(originalCosmosException, "Argument 'originalCosmosException' must not be null.");
        checkNotNull(readCosmosError, "Argument 'readCosmosError' must not be null.");

        CosmosDiagnostics readDiagnostics = readCosmosError.getDiagnostics();
        if (readDiagnostics != null && readDiagnostics.getClientSideRequestStatisticsRaw() != null) {
            CosmosDiagnostics originalDiagnostics = originalCosmosException.getDiagnostics();
            if (originalDiagnostics == null
                || originalDiagnostics.getClientSideRequestStatisticsRaw() == null) {

                originalCosmosException.setDiagnostics(readDiagnostics);
            } else {
                originalDiagnostics.clientSideRequestStatistics().recordContributingPointOperation(
                    readDiagnostics.getClientSideRequestStatisticsRaw()
                );
            }
        }
    }

    private static void mergeDiagnostics(
        ResourceResponse<Document> readResponse,
        CosmosException originalCosmosException) {

        CosmosDiagnostics responseDiagnostics = readResponse.getDiagnostics();
        if (responseDiagnostics != null &&
            responseDiagnostics.getClientSideRequestStatisticsRaw() != null) {

            CosmosDiagnostics errorDiagnostics = originalCosmosException.getDiagnostics();
            if (errorDiagnostics != null) {
                responseDiagnostics.clientSideRequestStatistics().recordContributingPointOperation(
                    errorDiagnostics.getClientSideRequestStatisticsRaw()
                );
            }

            readResponse.addRequestCharge(originalCosmosException.getRequestCharge());
        }
    }

    private <T> Mono<CosmosItemResponse<T>> replaceItemWithTrackingId(Class<T> itemType,
                                                                      String itemId,
                                                                      Document doc,
                                                                      RequestOptions requestOptions,
                                                                      String trackingId) {

        checkNotNull(trackingId, "Argument 'trackingId' must not be null.");
        return replaceItemInternalCore(itemType, itemId, doc, requestOptions, trackingId)
            .onErrorResume(throwable -> {
                Throwable error = throwable instanceof CompletionException ? throwable.getCause() : throwable;

                if (!(error instanceof CosmosException)) {

                    Exception nonCosmosException =
                        error instanceof Exception ? (Exception) error : new RuntimeException(error);
                    return Mono.error(nonCosmosException);
                }

                assert error instanceof CosmosException;
                CosmosException cosmosException = (CosmosException) error;

                if (cosmosException.getStatusCode() != HttpConstants.StatusCodes.PRECONDITION_FAILED) {
                    return Mono.error(cosmosException);
                }

                Mono<CosmosItemResponse<T>> readMono =
                    this.getDatabase().getDocClientWrapper()
                        .readDocument(getItemLink(itemId), requestOptions)
                        .map(response -> {
                            mergeDiagnostics(response, cosmosException);
                            return ModelBridgeInternal
                                .createCosmosAsyncItemResponse(response, itemType, getItemDeserializer());
                        })
                        .single();

                return readMono
                    .onErrorMap(readThrowable -> {
                        if (readThrowable instanceof CosmosException) {
                            mergeDiagnostics(cosmosException, (CosmosException)readThrowable);
                        }
                        return cosmosException;
                    })
                    .flatMap(readResponse -> {
                        if (readResponse.getStatusCode() == 200
                        && itemResponseAccessor.hasTrackingId(readResponse, trackingId)) {
                            return Mono.just(itemResponseAccessor.withRemappedStatusCode(
                                readResponse,
                                200,
                                cosmosException.getRequestCharge(),
                                this.isContentResponseOnWriteEffectivelyEnabled(requestOptions)));
                        }

                        return Mono.error(cosmosException);
                    });
            });
    }

    private <T> Mono<CosmosItemResponse<T>> createItemWithTrackingId(
        T item, RequestOptions options, String trackingId) {

        checkNotNull(trackingId, "Argument 'trackingId' must not be null.");

        return createItemInternalCore(item, options, trackingId)
            .onErrorResume(throwable -> {
                Throwable error = throwable instanceof CompletionException ? throwable.getCause() : throwable;

                if (!(error instanceof CosmosException)) {

                    Exception nonCosmosException =
                        error instanceof Exception ? (Exception) error : new RuntimeException(error);
                    return Mono.error(nonCosmosException);
                }

                assert error instanceof CosmosException;
                CosmosException cosmosException = (CosmosException) error;

                if (cosmosException.getStatusCode() != HttpConstants.StatusCodes.CONFLICT) {
                    return Mono.error(cosmosException);
                }

                InternalObjectNode internalObjectNode = InternalObjectNode.fromObjectToInternalObjectNode(item);
                String itemId = internalObjectNode.getId();

                RequestOptions readRequestOptions = new RequestOptions(options);
                readRequestOptions.setConsistencyLevel(null);

                @SuppressWarnings("unchecked")
                Class<T> itemType = (Class<T>) item.getClass();

                final AsyncDocumentClient clientWrapper = this.getDatabase().getDocClientWrapper();
                Mono<CosmosItemResponse<T>> readMono =
                    clientWrapper
                        .getCollectionCache()
                        .resolveByNameAsync(
                            null, this.getLinkWithoutTrailingSlash(), null)
                        .flatMap(collection -> {
                            if (collection == null) {
                                throw new IllegalStateException("Collection cannot be null");
                            }

                            PartitionKeyDefinition pkDef = collection.getPartitionKey();
                            PartitionKeyInternal partitionKeyInternal = PartitionKeyHelper
                                .extractPartitionKeyValueFromDocument(internalObjectNode, pkDef);
                            PartitionKey partitionKey = ImplementationBridgeHelpers
                                .PartitionKeyHelper
                                    .getPartitionKeyAccessor()
                                    .toPartitionKey(partitionKeyInternal);
                            readRequestOptions.setPartitionKey(partitionKey);

                            return clientWrapper.readDocument(getItemLink(itemId), readRequestOptions)
                                                .map(response -> {
                                                    mergeDiagnostics(response, cosmosException);
                                                    return ModelBridgeInternal
                                                        .createCosmosAsyncItemResponse(
                                                            response, itemType, getItemDeserializer());
                                                }).single();
                        });

                return readMono
                    .onErrorMap(readThrowable -> {
                        if (readThrowable instanceof CosmosException) {
                            mergeDiagnostics(cosmosException, (CosmosException)readThrowable);
                        }
                        return cosmosException;
                    })
                    .flatMap(readResponse -> {
                        if (readResponse.getStatusCode() == 200
                        && itemResponseAccessor.hasTrackingId(readResponse, trackingId)) {
                            return Mono.just(itemResponseAccessor.withRemappedStatusCode(
                                readResponse,
                                201,
                                cosmosException.getRequestCharge(),
                                this.isContentResponseOnWriteEffectivelyEnabled(options)));
                        }

                        return Mono.error(cosmosException);
                    });
            });
    }

    private boolean isContentResponseOnWriteEffectivelyEnabled(RequestOptions options) {
        Boolean requestOptionsContentResponseEnabled = null;
        if (options != null) {
            requestOptionsContentResponseEnabled = options.isContentResponseOnWriteEnabled();
        }

        return clientAccessor.isEffectiveContentResponseOnWriteEnabled(
            this.database.getClient(), requestOptionsContentResponseEnabled);
    }

    private <T> Mono<CosmosItemResponse<T>> createItemInternal(T item, CosmosItemRequestOptions options, Context context) {
        checkNotNull(options, "Argument 'options' must not be null.");

        WriteRetryPolicy nonIdempotentWriteRetryPolicy = itemOptionsAccessor
            .calculateAndGetEffectiveNonIdempotentRetriesEnabled(
                options,
                this.database.getClient().getNonIdempotentWriteRetryPolicy(),
                true);

        Mono<CosmosItemResponse<T>> responseMono;
        String trackingId = null;
        CosmosItemRequestOptions effectiveOptions = getEffectiveOptions(nonIdempotentWriteRetryPolicy, options);

        RequestOptions requestOptions =  ModelBridgeInternal.toRequestOptions(effectiveOptions);
        if (nonIdempotentWriteRetryPolicy.isEnabled() && nonIdempotentWriteRetryPolicy.useTrackingIdProperty()) {
            trackingId = UUID.randomUUID().toString();
            responseMono = createItemWithTrackingId(item, requestOptions, trackingId);
        } else {
            responseMono = createItemInternalCore(item, requestOptions, null);
        }

        CosmosAsyncClient client = database
            .getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosItemResponsePublisher(
                responseMono,
                context,
                this.createItemSpanName,
                getId(),
                database.getId(),
                database.getClient(),
                ModelBridgeInternal.getConsistencyLevel(effectiveOptions),
                OperationType.Create,
                ResourceType.Document,
                requestOptions,
                trackingId);
    }

    private <T> Mono<CosmosItemResponse<T>> createItemInternalCore(
        T item,
        RequestOptions requestOptions,
        String trackingId) {

        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        requestOptions.setTrackingId(trackingId);
        return database.getDocClientWrapper()
                   .createDocument(getLink(),
                                   item,
                                   requestOptions,
                                   true)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()))
                   .single();
    }

    /**
     * Upserts an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the item represented as a POJO or Item object to upsert.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item) {
        return upsertItem(item, new CosmosItemRequestOptions());
    }

    /**
     * Upserts an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the item represented as a POJO or Item object to upsert.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item, CosmosItemRequestOptions options) {
        final CosmosItemRequestOptions requestOptions = options == null ? new CosmosItemRequestOptions() : options;
        return withContext(context -> upsertItemInternal(item, requestOptions, context));
    }

    /**
     * Upserts an item.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the
     * upserted item. In case of failure the {@link Mono} will error.
     *
     * @param <T> the type parameter.
     * @param item the item represented as a POJO or Item object to upsert.
     * @param partitionKey the partition key.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the upserted item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> upsertItem(T item, PartitionKey partitionKey, CosmosItemRequestOptions options) {
        final CosmosItemRequestOptions requestOptions = options == null ? new CosmosItemRequestOptions() : options;
        ModelBridgeInternal.setPartitionKey(requestOptions, partitionKey);
        return withContext(context -> upsertItemInternal(item, requestOptions, context));
    }

    /**
     * Reads all the items in the current container.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read Cosmos items or an
     * error.
     */
    <T> CosmosPagedFlux<T> readAllItems(Class<T> classType) {
        return readAllItems(new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Reads all the items in the current container.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param options the feed options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read Cosmos items or an
     * error.
     */
    <T> CosmosPagedFlux<T> readAllItems(CosmosQueryRequestOptions options, Class<T> classType) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            CosmosAsyncClient client = this.getDatabase().getClient();
            CosmosQueryRequestOptions requestOptions = options != null ? options : new CosmosQueryRequestOptions();

            QueryFeedOperationState state = new QueryFeedOperationState(
                client,
                this.readAllItemsSpanName,
                database.getId(),
                this.getId(),
                ResourceType.Document,
                OperationType.ReadFeed,
                queryOptionsAccessor.getQueryNameOrDefault(requestOptions, this.readAllItemsSpanName),
                requestOptions,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return getDatabase()
                .getDocClientWrapper()
                .readDocuments(getLink(), state, classType)
                .map(response -> prepareFeedResponse(response, false));
        });
    }

    /**
     * Query for items in the current container.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.queryItems -->
     * <pre>
     * CosmosQueryRequestOptions options = new CosmosQueryRequestOptions&#40;&#41;;
     * String query = &quot;SELECT * FROM Passenger WHERE Passenger.departure IN &#40;'SEA', 'IND'&#41;&quot;;
     * cosmosAsyncContainer.queryItems&#40;query, options, Passenger.class&#41;
     *     .byPage&#40;&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Flux.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.queryItems -->
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param query the query.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, Class<T> classType) {
        return queryItemsInternal(new SqlQuerySpec(query), new CosmosQueryRequestOptions(), classType);
    }

    /**
     *  Best effort to initialize the container by warming up the caches and connections for the current read region.
     * <p>
     *  Depending on how many partitions the container has, the total time needed will also change. But generally you can use the following formula
     *  to get an estimated time:
     *  If it took 200ms to establish a connection, and you have 100 partitions in your container
     *  then it will take around (100 * 4 / CPUCores) * 200ms to open all connections after get the address list
     *
     *  <p>
     *  <br>NOTE: This API ideally should be called only once during application initialization before any workload.
     *  <br>In case of any transient error, caller should consume the error and continue the regular workload.
     *  </p>
     *
     *  @return Mono of Void.
     * @deprecated use {@link CosmosClientBuilder#openConnectionsAndInitCaches(CosmosContainerProactiveInitConfig)} instead.
     */
    @Deprecated
    public Mono<Void> openConnectionsAndInitCaches() {

        if (isInitialized.compareAndSet(false, true)) {

            CosmosContainerIdentity cosmosContainerIdentity = new CosmosContainerIdentity(this.database.getId(), this.id);
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig =
                new CosmosContainerProactiveInitConfigBuilder(Collections.singletonList(cosmosContainerIdentity))
                    .setProactiveConnectionRegionsCount(1)
                    .setMinConnectionPoolSizePerEndpointForContainer(cosmosContainerIdentity, Configs.getMinConnectionPoolSizePerEndpoint())
                    .build();

            return withContext(context -> openConnectionsAndInitCachesInternal(
                    proactiveContainerInitConfig
            )
            .collectList()
            .flatMap(openResult -> {
                logger.debug("OpenConnectionsAndInitCaches: {}", openResult);
                return Mono.empty();
            }));
        } else {
            logger.warn("OpenConnectionsAndInitCaches is already called once on Container {}, no operation will take place in this call", this.getId());
            return Mono.empty();
        }
    }

    /**
     *  Best effort to initialize the container by warming up the caches and connections to a specified no.
     *  of regions from the  preferred list of regions.
     * <p>
     *  Depending on how many partitions the container has, the total time needed will also change. But
     *  generally you can use the following formula to get an estimated time:
     *  If it took 200ms to establish a connection, and you have 100 partitions in your container
     *  then it will take around (100 * 4 / (10 * CPUCores)) * 200ms * RegionsWithProactiveConnections to open all
     *  connections after get the address list
     *
     *  <p>
     *  <br>NOTE: This API ideally should be called only once during application initialization before any workload.
     *  <br>In case of any transient error, caller should consume the error and continue the regular workload.
     *  </p>
     * <p>
     * In order to minimize latencies associated with warming up caches and opening connections
     * the no. of proactive connection regions cannot be more
     * than {@link CosmosContainerProactiveInitConfigBuilder#MAX_NO_OF_PROACTIVE_CONNECTION_REGIONS}.
     * </p>
     *
     * @param numProactiveConnectionRegions the no of regions to proactively connect to
     * @return Mono of Void.
     * @deprecated use {@link CosmosClientBuilder#openConnectionsAndInitCaches(CosmosContainerProactiveInitConfig)} instead.
     */
    @Deprecated
    public Mono<Void> openConnectionsAndInitCaches(int numProactiveConnectionRegions) {

        List<String> preferredRegions = clientAccessor.getPreferredRegions(this.database.getClient());
        boolean endpointDiscoveryEnabled = clientAccessor.isEndpointDiscoveryEnabled(this.database.getClient());

        checkArgument(numProactiveConnectionRegions > 0, "no. of proactive connection regions should be greater than 0");

        if (numProactiveConnectionRegions > 1) {
            checkArgument(
                endpointDiscoveryEnabled,
                "endpoint discovery should be enabled when no. " +
                    "of proactive regions is greater than 1");
            checkArgument(
                preferredRegions != null && preferredRegions.size() >= numProactiveConnectionRegions,
                "no. of proactive connection " +
                    "regions should be lesser than the no. of preferred regions.");
        }

        if (isInitialized.compareAndSet(false, true)) {

            CosmosContainerIdentity cosmosContainerIdentity = new CosmosContainerIdentity(database.getId(), this.id);
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig =
                new CosmosContainerProactiveInitConfigBuilder(Arrays.asList(cosmosContainerIdentity))
                    .setProactiveConnectionRegionsCount(numProactiveConnectionRegions)
                    .build();

            return withContext(context -> openConnectionsAndInitCachesInternal(
                    proactiveContainerInitConfig
            )
                .collectList()
                .flatMap(
                    openResult -> {
                        logger.debug("OpenConnectionsAndInitCaches: {}", openResult);
                        return Mono.empty();
                    }));
        } else {
            logger.warn(
                "OpenConnectionsAndInitCaches is already called once on Container {}, no operation will take place in this call",
                this.getId());
            return Mono.empty();
        }
    }

    /**
     * The internal implementation to try to initialize the container by warming up the caches and
     * connections for the first {@link CosmosContainerProactiveInitConfig#getProactiveConnectionRegionsCount()}
     * proactive connection regions
     *
     * @return a {@link String} type which represents the total no. of successful and failed
     * connection attempts for an endpoint
     */
    private Flux<Void> openConnectionsAndInitCachesInternal(
        CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {

        return this.database
            .getDocClientWrapper()
            .submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig)
            .doOnSubscribe(subscription -> {
                this.database.getDocClientWrapper().recordOpenConnectionsAndInitCachesStarted(proactiveContainerInitConfig.getCosmosContainerIdentities());
            })
            .doOnTerminate(() -> {
                this.database.getDocClientWrapper().recordOpenConnectionsAndInitCachesCompleted(proactiveContainerInitConfig.getCosmosContainerIdentities());
            });
    }

    /**
     * Query for items in the current container using a string.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.queryItems -->
     * <pre>
     * CosmosQueryRequestOptions options = new CosmosQueryRequestOptions&#40;&#41;;
     * String query = &quot;SELECT * FROM Passenger WHERE Passenger.departure IN &#40;'SEA', 'IND'&#41;&quot;;
     * cosmosAsyncContainer.queryItems&#40;query, options, Passenger.class&#41;
     *     .byPage&#40;&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Flux.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.queryItems -->
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param query the query.
     * @param options the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(String query, CosmosQueryRequestOptions options, Class<T> classType) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryItemsInternal(new SqlQuerySpec(query), options, classType);
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpec}.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.SqlQuerySpec.queryItems -->
     * <pre>
     * CosmosQueryRequestOptions options = new CosmosQueryRequestOptions&#40;&#41;;
     *
     * String query = &quot;SELECT * FROM Passenger p WHERE &#40;p.departure = &#64;departure&#41;&quot;;
     * List&lt;SqlParameter&gt; parameters = Collections.singletonList&#40;new SqlParameter&#40;&quot;&#64;departure&quot;, &quot;SEA&quot;&#41;&#41;;
     * SqlQuerySpec sqlQuerySpec = new SqlQuerySpec&#40;query, parameters&#41;;
     *
     * cosmosAsyncContainer.queryItems&#40;sqlQuerySpec, options, Passenger.class&#41;
     *     .byPage&#40;&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Flux.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.SqlQuerySpec.queryItems -->
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param querySpec the SQL query specification.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, Class<T> classType) {
        return queryItemsInternal(querySpec, new CosmosQueryRequestOptions(), classType);
    }

    /**
     * Query for items in the current container using a {@link SqlQuerySpec} and {@link CosmosQueryRequestOptions}.
     * <p>
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param querySpec the SQL query specification.
     * @param options the query request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained items or an
     * error.
     */
    public <T> CosmosPagedFlux<T> queryItems(SqlQuerySpec querySpec, CosmosQueryRequestOptions options, Class<T> classType) {
        if (options == null) {
            options = new CosmosQueryRequestOptions();
        }

        return queryItemsInternal(querySpec, options, classType);
    }

    <T> CosmosPagedFlux<T> queryItemsInternal(
        SqlQuerySpec sqlQuerySpec, CosmosQueryRequestOptions cosmosQueryRequestOptions, Class<T> classType) {
        if (cosmosQueryRequestOptions != null) {
            if (cosmosQueryRequestOptions.getPartitionKey() != null && cosmosQueryRequestOptions
                                                                           .getFeedRange() != null) {
                throw new IllegalArgumentException("Setting partitionKey and feedRange at the same time is not " +
                                                       "allowed");
            }
        }
        return UtilBridgeInternal.createCosmosPagedFlux(queryItemsInternalFunc(sqlQuerySpec, cosmosQueryRequestOptions, classType));
    }

    <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryItemsInternalFunc(
        SqlQuerySpec sqlQuerySpec, CosmosQueryRequestOptions cosmosQueryRequestOptions, Class<T> classType) {
        CosmosAsyncClient client = this.getDatabase().getClient();
        CosmosQueryRequestOptions options =
            cosmosQueryRequestOptions != null ? cosmosQueryRequestOptions : new CosmosQueryRequestOptions();

        Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> pagedFluxOptionsFluxFunction = (pagedFluxOptions -> {
            String spanName = this.queryItemsSpanName;

            QueryFeedOperationState state = new QueryFeedOperationState(
                client,
                spanName,
                database.getId(),
                this.getId(),
                ResourceType.Document,
                OperationType.Query,
                queryOptionsAccessor.getQueryNameOrDefault(options, spanName),
                options,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return getDatabase()
                        .getDocClientWrapper()
                        .queryDocuments(CosmosAsyncContainer.this.getLink(), sqlQuerySpec, state, classType)
                        .map(response -> prepareFeedResponse(response, false));
        });

        return pagedFluxOptionsFluxFunction;
    }

    <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryItemsInternalFunc(
        Mono<SqlQuerySpec> sqlQuerySpecMono, CosmosQueryRequestOptions cosmosQueryRequestOptions, Class<T> classType) {
        Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> pagedFluxOptionsFluxFunction = (pagedFluxOptions -> {
            CosmosAsyncClient client = this.getDatabase().getClient();
            CosmosQueryRequestOptions options =
                cosmosQueryRequestOptions != null ? cosmosQueryRequestOptions : new CosmosQueryRequestOptions();

            String spanName = this.queryItemsSpanName;

            QueryFeedOperationState state = new QueryFeedOperationState(
                client,
                spanName,
                database.getId(),
                this.getId(),
                ResourceType.Document,
                OperationType.Query,
                queryOptionsAccessor.getQueryNameOrDefault(options, spanName),
                options,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return sqlQuerySpecMono.flux()
                .flatMap(sqlQuerySpec -> getDatabase().getDocClientWrapper()
                    .queryDocuments(CosmosAsyncContainer.this.getLink(), sqlQuerySpec, state, classType))
                .map(response -> prepareFeedResponse(response, false));
        });

        return pagedFluxOptionsFluxFunction;
    }

    /**
     * Query for items in the change feed of the current container using the {@link CosmosChangeFeedRequestOptions}.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.queryChangeFeed -->
     * <pre>
     * CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
     *     .createForProcessingFromNow&#40;FeedRange.forFullRange&#40;&#41;&#41;
     *     .allVersionsAndDeletes&#40;&#41;;
     *
     * cosmosAsyncContainer.queryChangeFeed&#40;options, Passenger.class&#41;
     *     .byPage&#40;&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Flux.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.queryChangeFeed -->
     * After subscription the operation will be performed. The {@link Flux} will
     * contain one or several feed response of the obtained items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param options the change feed request options.
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained
     * items or an error.
     */
    public <T> CosmosPagedFlux<T> queryChangeFeed(CosmosChangeFeedRequestOptions options, Class<T> classType) {
        checkNotNull(options, "Argument 'options' must not be null.");
        checkNotNull(classType, "Argument 'classType' must not be null.");

        return queryChangeFeedInternal(options, classType);
    }

    <T> CosmosPagedFlux<T> queryChangeFeedInternal(
        CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions,
        Class<T> classType) {

        return UtilBridgeInternal.createCosmosPagedFlux(
            queryChangeFeedInternalFunc(cosmosChangeFeedRequestOptions, classType));
    }

    String getLinkWithoutTrailingSlash() {
        if (this.link.startsWith("/")) {
            return this.link.substring(1);
        }

        return this.link;
    }

    <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryChangeFeedInternalFunc(
        CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions,
        Class<T> classType) {

        checkNotNull(
            cosmosChangeFeedRequestOptions,
            "Argument 'cosmosChangeFeedRequestOptions' must not be null.");

        Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> pagedFluxOptionsFluxFunction = (pagedFluxOptions -> {

            checkNotNull(
                pagedFluxOptions,
                "Argument 'pagedFluxOptions' must not be null.");

            CosmosAsyncClient client = this.getDatabase().getClient();
            String spanName = this.queryChangeFeedSpanName;

            ChangeFeedOperationState state = new ChangeFeedOperationState(
                client,
                spanName,
                database.getId(),
                this.getId(),
                ResourceType.Document,
                OperationType.ReadFeed,
                spanName,
                cosmosChangeFeedRequestOptions,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            final AsyncDocumentClient clientWrapper = this.database.getDocClientWrapper();
            return clientWrapper
                .getCollectionCache()
                .resolveByNameAsync(
                    null,
                    this.getLinkWithoutTrailingSlash(),
                    null)
                .flatMapMany(
                    collection -> {
                        if (collection == null) {
                            throw new IllegalStateException("Collection cannot be null");
                        }

                        return clientWrapper
                            .queryDocumentChangeFeedFromPagedFlux(collection, state, classType)
                            .map(response -> prepareFeedResponse(response, true));
                    });
        });

        return pagedFluxOptionsFluxFunction;
    }

    private <T> FeedResponse<T> prepareFeedResponse(
        FeedResponse<T> response,
        boolean isChangeFeed) {

        boolean useEtagAsContinuation = isChangeFeed;
        boolean isNoChangesResponse = isChangeFeed ?
            ModelBridgeInternal.getNoChangesFromFeedResponse(response)
            : false;

        return BridgeInternal.createFeedResponseWithQueryMetrics(
            response.getResults(),
            response.getResponseHeaders(),
            ModelBridgeInternal.queryMetrics(response),
            ModelBridgeInternal.getQueryPlanDiagnosticsContext(response),
            useEtagAsContinuation,
            isNoChangesResponse,
            response.getCosmosDiagnostics());
    }

    /**
     * Executes the transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     *
     * @return A Mono response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then the Mono will return error instead of CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    public Mono<CosmosBatchResponse> executeCosmosBatch(CosmosBatch cosmosBatch) {
        return executeCosmosBatch(cosmosBatch, new CosmosBatchRequestOptions());
    }

    /**
     * Executes the transactional batch.
     *
     * @param cosmosBatch Batch having list of operation and partition key which will be executed by this container.
     * @param requestOptions Options that apply specifically to batch request.
     *
     * @return A Mono response which contains details of execution of the transactional batch.
     * <p>
     * If the transactional batch executes successfully, the value returned by {@link
     * CosmosBatchResponse#getStatusCode} on the response returned will be set to 200}.
     * <p>
     * If an operation within the transactional batch fails during execution, no changes from the batch will be
     * committed and the status of the failing operation is made available by {@link
     * CosmosBatchResponse#getStatusCode} or by the exception. To obtain information about the operations
     * that failed in case of some user error like conflict, not found etc, the response can be enumerated.
     * This returns {@link CosmosBatchOperationResult} instances corresponding to each operation in the
     * transactional batch in the order they were added to the transactional batch.
     * For a result corresponding to an operation within the transactional batch, use
     * {@link CosmosBatchOperationResult#getStatusCode}
     * to access the status of the operation. If the operation was not executed or it was aborted due to the failure of
     * another operation within the transactional batch, the value of this field will be 424;
     * for the operation that caused the batch to abort, the value of this field
     * will indicate the cause of failure.
     * <p>
     * If there are issues such as request timeouts, Gone, session not available, network failure
     * or if the service somehow returns 5xx then the Mono will return error instead of CosmosBatchResponse.
     * <p>
     * Use {@link CosmosBatchResponse#isSuccessStatusCode} on the response returned to ensure that the
     * transactional batch succeeded.
     */
    public Mono<CosmosBatchResponse> executeCosmosBatch(
        CosmosBatch cosmosBatch,
        CosmosBatchRequestOptions requestOptions) {

        if (requestOptions == null) {
            requestOptions = new CosmosBatchRequestOptions();
        }

        final CosmosBatchRequestOptions cosmosBatchRequestOptions = requestOptions;

        return withContext(context -> {
            final BatchExecutor executor = new BatchExecutor(this, cosmosBatch, cosmosBatchRequestOptions);
            final Mono<CosmosBatchResponse> responseMono = executor.executeAsync();

            RequestOptions requestOptionsInternal = ModelBridgeInternal.toRequestOptions(cosmosBatchRequestOptions);
            CosmosAsyncClient client = database
                .getClient();

            return client
                .getDiagnosticsProvider()
                .traceEnabledBatchResponsePublisher(
                    responseMono,
                    context,
                    this.batchSpanName,
                    database.getId(),
                    this.id,
                    client,
                    ImplementationBridgeHelpers
                        .CosmosBatchRequestOptionsHelper
                        .getCosmosBatchRequestOptionsAccessor()
                        .getConsistencyLevel(cosmosBatchRequestOptions),
                    OperationType.Batch,
                    ResourceType.Document,
                    requestOptionsInternal);
        });
    }

    /**
     * Executes flux of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     * @param operations Flux of operation which will be executed by this container.
     *
     * @return A Flux of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    public <TContext> Flux<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Flux<CosmosItemOperation> operations) {

        return this.executeBulkOperations(operations, new CosmosBulkExecutionOptions());
    }

    /**
     * Executes flux of operations in Bulk.
     *
     * @param <TContext> The context for the bulk processing.
     *
     * @param operations Flux of operation which will be executed by this container.
     * @param bulkOptions Options that apply for this Bulk request which specifies options regarding execution like
     *                    concurrency, batching size, interval and context.
     *
     * @return A Flux of {@link CosmosBulkOperationResponse} which contains operation and it's response or exception.
     * <p>
     *     To create a operation which can be executed here, use {@link com.azure.cosmos.models.CosmosBulkOperations}. For eg.
     *     for a upsert operation use {@link com.azure.cosmos.models.CosmosBulkOperations#getUpsertItemOperation(Object, PartitionKey)}
     * </p>
     * <p>
     *     We can get the corresponding operation using {@link CosmosBulkOperationResponse#getOperation()} and
     *     it's response using {@link CosmosBulkOperationResponse#getResponse()}. If the operation was executed
     *     successfully, the value returned by {@link com.azure.cosmos.models.CosmosBulkItemResponse#isSuccessStatusCode()} will be true. To get
     *     actual status use {@link com.azure.cosmos.models.CosmosBulkItemResponse#getStatusCode()}.
     * </p>
     * To check if the operation had any exception, use {@link CosmosBulkOperationResponse#getException()} to
     * get the exception.
     */
    public <TContext> Flux<CosmosBulkOperationResponse<TContext>> executeBulkOperations(
        Flux<CosmosItemOperation> operations,
        CosmosBulkExecutionOptions bulkOptions) {

        if (bulkOptions == null) {
            bulkOptions = new CosmosBulkExecutionOptions();
        }

        final CosmosBulkExecutionOptions cosmosBulkExecutionOptions = bulkOptions;

        return Flux.deferContextual(context -> {
            final BulkExecutor<TContext> executor = new BulkExecutor<>(this, operations, cosmosBulkExecutionOptions);

            return executor.execute().publishOn(CosmosSchedulers.BULK_EXECUTOR_BOUNDED_ELASTIC);
        });
    }

    /**
     * Reads an item by itemId.
     * <br/>
     * This operation is used to retrieve a single item from a container based on its unique identifier (ID) and partition key.
     * The readItem operation provides direct access to a specific item using its unique identifier, which consists of the item's ID and the partition key value. This operation is efficient for retrieving a known item by its ID and partition key without the need for complex querying.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain an item response with the read item.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readItem -->
     * <pre>
     * &#47;&#47; Read an item
     * cosmosAsyncContainer.readItem&#40;passenger.getId&#40;&#41;, new PartitionKey&#40;passenger.getId&#40;&#41;&#41;, Passenger.class&#41;
     *     .flatMap&#40;response -&gt; Mono.just&#40;response.getItem&#40;&#41;&#41;&#41;
     *     .subscribe&#40;passengerItem -&gt; System.out.println&#40;passengerItem&#41;, throwable -&gt; &#123;
     *         CosmosException cosmosException = &#40;CosmosException&#41; throwable;
     *         cosmosException.printStackTrace&#40;&#41;;
     *     &#125;&#41;;
     * &#47;&#47; ...
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readItem -->
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param itemType the item type.
     * @return an {@link Mono} containing the Cosmos item response with the read item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(String itemId, PartitionKey partitionKey, Class<T> itemType) {
        return readItem(itemId, partitionKey, ModelBridgeInternal.createCosmosItemRequestOptions(partitionKey), itemType);
    }

    /**
     * Reads an item by itemId using a configured {@link CosmosItemRequestOptions}.
     * <br/>
     * This operation is used to retrieve a single item from a container based on its unique identifier (ID) and partition key.
     * The readItem operation provides direct access to a specific item using its unique identifier, which consists of the item's ID and the partition key value. This operation is efficient for retrieving a known item by its ID and partition key without the need for complex querying.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a Cosmos item response with the read item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the request (Optional) {@link CosmosItemRequestOptions}.
     * @param itemType the item type.
     * @return an {@link Mono} containing the Cosmos item response with the read item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> readItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options, Class<T> itemType) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }

        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return withContext(context -> readItemInternal(itemId, requestOptions, itemType, context));
    }

    /**
     * Reads many documents.
     * Useful for reading many documents with a particular id and partition key in a single request.
     * If any document from the list is missing, no exception will be thrown.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readMany -->
     * <pre>
     * List&lt;CosmosItemIdentity&gt; itemIdentityList = new ArrayList&lt;&gt;&#40;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger1Id&#41;, passenger1Id&#41;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger2Id&#41;, passenger2Id&#41;&#41;;
     *
     * cosmosAsyncContainer.readMany&#40;itemIdentityList, Passenger.class&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Mono.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readMany -->
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param classType   class type
     * @return a Mono with feed response of cosmos items
     */
    public <T> Mono<FeedResponse<T>> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        Class<T> classType) {

        return this.readMany(itemIdentityList, new CosmosReadManyRequestOptions(), classType);
    }

    /**
     * Reads many documents.
     * Useful for reading many documents with a particular id and partition key in a single request.
     * If any document from the list is missing, no exception will be thrown.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readMany -->
     * <pre>
     * List&lt;CosmosItemIdentity&gt; itemIdentityList = new ArrayList&lt;&gt;&#40;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger1Id&#41;, passenger1Id&#41;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger2Id&#41;, passenger2Id&#41;&#41;;
     *
     * cosmosAsyncContainer.readMany&#40;itemIdentityList, Passenger.class&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Mono.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readMany -->
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param sessionToken the optional Session token - null if the read can be made without specific session token
     * @param classType   class type
     * @return a Mono with feed response of cosmos items or error
     */
    public <T> Mono<FeedResponse<T>> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        String sessionToken,
        Class<T> classType) {

        CosmosReadManyRequestOptions options = new CosmosReadManyRequestOptions();

        if (!StringUtils.isNotEmpty(sessionToken)) {
            options = options.setSessionToken(sessionToken);
        }

        return this.readMany(itemIdentityList, options, classType);
    }

    /**
     * Reads many documents.
     * Useful for reading many documents with a particular id and partition key in a single request.
     * If any document from the list is missing, no exception will be thrown.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readMany -->
     * <pre>
     * List&lt;CosmosItemIdentity&gt; itemIdentityList = new ArrayList&lt;&gt;&#40;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger1Id&#41;, passenger1Id&#41;&#41;;
     * itemIdentityList.add&#40;new CosmosItemIdentity&#40;new PartitionKey&#40;passenger2Id&#41;, passenger2Id&#41;&#41;;
     *
     * cosmosAsyncContainer.readMany&#40;itemIdentityList, Passenger.class&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Mono.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readMany -->
     * @param <T> the type parameter
     * @param itemIdentityList CosmosItem id and partition key tuple of items that that needs to be read
     * @param requestOptions the optional request option
     * @param classType   class type
     * @return a Mono with feed response of cosmos items or error
     */
    public <T> Mono<FeedResponse<T>> readMany(
        List<CosmosItemIdentity> itemIdentityList,
        CosmosReadManyRequestOptions requestOptions,
        Class<T> classType) {

        CosmosQueryRequestOptions queryRequestOptions = requestOptions == null
            ? new CosmosQueryRequestOptions()
            : queryOptionsAccessor.clone(requestOptions);
        queryRequestOptions.setMaxDegreeOfParallelism(-1);
        queryRequestOptions.setQueryName("readMany");

        CosmosAsyncClient client = this.getDatabase().getClient();
        CosmosPagedFluxOptions fluxOptions = new CosmosPagedFluxOptions();
        fluxOptions.setMaxItemCount(itemIdentityList != null ? itemIdentityList.size() : 0);
        QueryFeedOperationState state = new QueryFeedOperationState(
            client,
            this.readAllItemsSpanName,
            database.getId(),
            this.getId(),
            ResourceType.Document,
            OperationType.Query,
            queryOptionsAccessor.getQueryNameOrDefault(queryRequestOptions, this.readManyItemsSpanName),
            queryRequestOptions,
            fluxOptions
        );

        return CosmosBridgeInternal
            .getAsyncDocumentClient(this.getDatabase())
            .readMany(itemIdentityList, BridgeInternal.getLink(this), state, classType);
    }

    /**
     * Reads all the items of a logical partition
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readAllItems -->
     * <pre>
     * cosmosAsyncContainer
     *     .readAllItems&#40;new PartitionKey&#40;partitionKey&#41;, Passenger.class&#41;
     *     .byPage&#40;100&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Flux.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readAllItems -->
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed responses of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages
     * of the read Cosmos items or an error.
     */
    public <T> CosmosPagedFlux<T> readAllItems(
        PartitionKey partitionKey,
        Class<T> classType) {
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setPartitionKey(partitionKey);

        return this.readAllItems(partitionKey, queryRequestOptions, classType);
    }

    /**
     * Reads all the items of a logical partition
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readAllItems -->
     * <pre>
     * cosmosAsyncContainer
     *     .readAllItems&#40;new PartitionKey&#40;partitionKey&#41;, Passenger.class&#41;
     *     .byPage&#40;100&#41;
     *     .flatMap&#40;passengerFeedResponse -&gt; &#123;
     *         for &#40;Passenger passenger : passengerFeedResponse.getResults&#40;&#41;&#41; &#123;
     *             System.out.println&#40;passenger&#41;;
     *         &#125;
     *         return Flux.empty&#40;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readAllItems -->
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed responses of the read Cosmos items. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @param <T> the type parameter.
     * @param partitionKey the partition key value of the documents that need to be read
     * @param options the feed options (Optional).
     * @param classType the class type.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages
     * of the read Cosmos items or an error.
     */
    public <T> CosmosPagedFlux<T> readAllItems(
        PartitionKey partitionKey,
        CosmosQueryRequestOptions options,
        Class<T> classType) {
        CosmosAsyncClient client = this.getDatabase().getClient();
        final CosmosQueryRequestOptions requestOptions = options == null ? new CosmosQueryRequestOptions() : options;

        requestOptions.setPartitionKey(partitionKey);

        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {

            QueryFeedOperationState state = new QueryFeedOperationState(
                client,
                this.readAllItemsOfLogicalPartitionSpanName,
                database.getId(),
                this.getId(),
                ResourceType.Document,
                OperationType.ReadFeed,
                queryOptionsAccessor.getQueryNameOrDefault(requestOptions, this.readAllItemsOfLogicalPartitionSpanName),
                requestOptions,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return getDatabase()
                .getDocClientWrapper()
                .readAllDocuments(getLink(), partitionKey, state, classType)
                .map(response -> prepareFeedResponse(response, false));
        });
    }

    /**
     * Replaces an existing item in a container with a new item.
     * It performs a complete replacement of the item,
     * replacing all its properties with the properties of the new item
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.replaceItem -->
     * <pre>
     * cosmosAsyncContainer.replaceItem&#40;
     *         newPassenger,
     *         oldPassenger.getId&#40;&#41;,
     *         new PartitionKey&#40;oldPassenger.getId&#40;&#41;&#41;,
     *         new CosmosItemRequestOptions&#40;&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.println&#40;response&#41;;
     *     &#125;, throwable -&gt; &#123;
     *         throwable.printStackTrace&#40;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.replaceItem -->
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the replaced item.
     *
     * @param <T> the type parameter.
     * @param item the item to replace (containing the item id).
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @return an {@link Mono} containing the Cosmos item resource response with the replaced item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> replaceItem(T item, String itemId, PartitionKey partitionKey) {
        return replaceItem(item, itemId, partitionKey, new CosmosItemRequestOptions());
    }

    /**
     * Replaces an existing item in a container with a new item.
     * It performs a complete replacement of the item,
     * replacing all its properties with the properties of the new item
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.replaceItem -->
     * <pre>
     * cosmosAsyncContainer.replaceItem&#40;
     *         newPassenger,
     *         oldPassenger.getId&#40;&#41;,
     *         new PartitionKey&#40;oldPassenger.getId&#40;&#41;&#41;,
     *         new CosmosItemRequestOptions&#40;&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.println&#40;response&#41;;
     *     &#125;, throwable -&gt; &#123;
     *         throwable.printStackTrace&#40;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.replaceItem -->
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the replaced item.
     *
     * @param <T> the type parameter.
     * @param item the item to replace (containing the item id).
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param options the request comosItemRequestOptions (Optional).
     * @return an {@link Mono} containing the Cosmos item resource response with the replaced item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> replaceItem(
        T item, String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        Document doc = InternalObjectNode.fromObject(item);
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();
        final CosmosItemRequestOptions requestOptions = options;
        return withContext(context -> replaceItemInternal(itemType, itemId, doc, requestOptions, context));
    }

    /**
     * Run partial update that modifies specific properties or fields of the item without replacing the entire item.
     *
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.patchItem -->
     * <pre>
     * CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create&#40;&#41;;
     *
     * cosmosPatchOperations
     *     .add&#40;&quot;&#47;departure&quot;, &quot;SEA&quot;&#41;
     *     .increment&#40;&quot;&#47;trips&quot;, 1&#41;;
     *
     * cosmosAsyncContainer.patchItem&#40;
     *         passenger.getId&#40;&#41;,
     *         new PartitionKey&#40;passenger.getId&#40;&#41;&#41;,
     *         cosmosPatchOperations,
     *         Passenger.class&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.println&#40;response&#41;;
     *     &#125;, throwable -&gt; &#123;
     *         throwable.printStackTrace&#40;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.patchItem -->
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the patched item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param itemType the item type.
     *
     * @return an {@link Mono} containing the Cosmos item resource response with the patched item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        Class<T> itemType) {

        return patchItem(itemId, partitionKey, cosmosPatchOperations, new CosmosPatchItemRequestOptions(), itemType);
    }

    /**
     * Run partial update that modifies specific properties or fields of the item without replacing the entire item.
     *
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.patchItem -->
     * <pre>
     * CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create&#40;&#41;;
     *
     * cosmosPatchOperations
     *     .add&#40;&quot;&#47;departure&quot;, &quot;SEA&quot;&#41;
     *     .increment&#40;&quot;&#47;trips&quot;, 1&#41;;
     *
     * cosmosAsyncContainer.patchItem&#40;
     *         passenger.getId&#40;&#41;,
     *         new PartitionKey&#40;passenger.getId&#40;&#41;&#41;,
     *         cosmosPatchOperations,
     *         Passenger.class&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.println&#40;response&#41;;
     *     &#125;, throwable -&gt; &#123;
     *         throwable.printStackTrace&#40;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.patchItem -->
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response with the patched item.
     *
     * @param <T> the type parameter.
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @param cosmosPatchOperations Represents a container having list of operations to be sequentially applied to the referred Cosmos item.
     * @param options the request options.
     * @param itemType the item type.
     *
     * @return an {@link Mono} containing the Cosmos item resource response with the patched item or an error.
     */
    public <T> Mono<CosmosItemResponse<T>> patchItem(
        String itemId,
        PartitionKey partitionKey,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosPatchItemRequestOptions options,
        Class<T> itemType) {

        checkNotNull(itemId, "expected non-null itemId");
        checkNotNull(partitionKey, "expected non-null partitionKey for patchItem");
        checkNotNull(cosmosPatchOperations, "expected non-null cosmosPatchOperations");

        if (options == null) {
            options = new CosmosPatchItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);

        final CosmosPatchItemRequestOptions requestOptions = options;
        return withContext(context -> patchItemInternal(itemId, cosmosPatchOperations, requestOptions, context, itemType));
    }

    /**
     * Deletes an item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for the deleted item.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.deleteItem -->
     * <pre>
     *
     * cosmosAsyncContainer.deleteItem&#40;
     *     passenger.getId&#40;&#41;,
     *     new PartitionKey&#40;passenger.getId&#40;&#41;&#41;
     * &#41;.subscribe&#40;response -&gt; &#123;
     *     System.out.println&#40;response&#41;;
     * &#125;, throwable -&gt; &#123;
     *     CosmosException cosmosException = &#40;CosmosException&#41; throwable;
     *     cosmosException.printStackTrace&#40;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.deleteItem -->
     * @param itemId the item id.
     * @param partitionKey the partition key.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public Mono<CosmosItemResponse<Object>> deleteItem(String itemId, PartitionKey partitionKey) {
        return deleteItem(itemId, partitionKey, new CosmosItemRequestOptions());
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for the deleted item.
     *
     * @param itemId id of the item.
     * @param partitionKey partitionKey of the item.
     * @param options the request options (Optional).
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public Mono<CosmosItemResponse<Object>> deleteItem(
        String itemId, PartitionKey partitionKey,
        CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        final  CosmosItemRequestOptions finalOptions = options;
        return withContext(context -> deleteItemInternal(itemId, null, finalOptions, context));
    }

    /**
     * Deletes all items in the Container with the specified partitionKey value.
     * Starts an asynchronous Cosmos DB background operation which deletes all items in the Container with the specified value.
     * The asynchronous Cosmos DB background operation runs using a percentage of user RUs.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for all the deleted items.
     *
     * @param partitionKey partitionKey of the item.
     * @param options the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public Mono<CosmosItemResponse<Object>> deleteAllItemsByPartitionKey(PartitionKey partitionKey, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        ModelBridgeInternal.setPartitionKey(options, partitionKey);
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return withContext(context -> deleteAllItemsByPartitionKeyInternal(partitionKey, requestOptions, context));
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single Cosmos item response for the deleted item.
     *
     * @param <T> the type parameter.
     * @param item item to be deleted.
     * @param options the request options.
     * @return an {@link Mono} containing the Cosmos item resource response.
     */
    public <T> Mono<CosmosItemResponse<Object>> deleteItem(T item, CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        final CosmosItemRequestOptions finalOptions = options;
        InternalObjectNode internalObjectNode = InternalObjectNode.fromObjectToInternalObjectNode(item);
        return withContext(context -> deleteItemInternal(
            internalObjectNode.getId(), internalObjectNode, finalOptions, context));
    }

    private String getItemLink(String itemId) {
        String builder = this.getLink()
            + "/"
            + Paths.DOCUMENTS_PATH_SEGMENT
            + "/"
            + itemId;
        return builder;
    }

    /**
     * Gets a {@link CosmosAsyncScripts} using the current container as context.
     * <p>
     * This can be further used to perform various operations on Cosmos scripts.
     *
     * @return the {@link CosmosAsyncScripts}.
     */
    public CosmosAsyncScripts getScripts() {
        if (this.scripts == null) {
            this.scripts = new CosmosAsyncScripts(this);
        }
        return this.scripts;
    }

    /**
     * Lists all the conflicts in the current container.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readAllConflicts -->
     * <pre>
     * try &#123;
     *     cosmosAsyncContainer.readAllConflicts&#40;options&#41;.
     *         byPage&#40;100&#41;
     *         .subscribe&#40;response -&gt; &#123;
     *             for &#40;CosmosConflictProperties conflictProperties : response.getResults&#40;&#41;&#41; &#123;
     *                 System.out.println&#40;conflictProperties&#41;;
     *             &#125;
     *         &#125;, throwable -&gt; &#123;
     *             throwable.printStackTrace&#40;&#41;;
     *         &#125;&#41;;
     * &#125; catch &#40;CosmosException ce&#41; &#123;
     *     ce.printStackTrace&#40;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     e.printStackTrace&#40;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readAllConflicts -->
     * @param options the query request options
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> readAllConflicts(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            CosmosAsyncClient client = this.getDatabase().getClient();
            CosmosQueryRequestOptions nonNullOptions = options != null ? options : new CosmosQueryRequestOptions();

            QueryFeedOperationState state = new QueryFeedOperationState(
                client,
                this.readAllConflictsSpanName,
                database.getId(),
                this.getId(),
                ResourceType.Conflict,
                OperationType.ReadFeed,
                queryOptionsAccessor.getQueryNameOrDefault(nonNullOptions, this.readAllConflictsSpanName),
                nonNullOptions,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return database.getDocClientWrapper().readConflicts(getLink(), state)
                .map(response -> feedResponseAccessor.createFeedResponse(
                    ModelBridgeInternal.getCosmosConflictPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders(),
                    response.getCosmosDiagnostics()));
        });
    }

    /**
     * Queries all the conflicts in the current container.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.queryConflicts -->
     * <pre>
     * try &#123;
     *     cosmosAsyncContainer.queryConflicts&#40;query&#41;.
     *         byPage&#40;100&#41;
     *         .subscribe&#40;response -&gt; &#123;
     *             for &#40;CosmosConflictProperties conflictProperties : response.getResults&#40;&#41;&#41; &#123;
     *                 System.out.println&#40;conflictProperties&#41;;
     *             &#125;
     *         &#125;, throwable -&gt; &#123;
     *             throwable.printStackTrace&#40;&#41;;
     *         &#125;&#41;;
     * &#125; catch &#40;CosmosException ce&#41; &#123;
     *     ce.printStackTrace&#40;&#41;;
     * &#125; catch &#40;Exception e&#41; &#123;
     *     e.printStackTrace&#40;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.queryConflicts -->
     * @param query the query.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> queryConflicts(String query) {
        return queryConflicts(query, new CosmosQueryRequestOptions());
    }

    /**
     * Queries all the conflicts in the current container.
     *
     * @param query the query.
     * @param options the query request options (Optional).
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * obtained conflicts or an error.
     */
    public CosmosPagedFlux<CosmosConflictProperties> queryConflicts(String query, CosmosQueryRequestOptions options) {
        final CosmosQueryRequestOptions requestOptions = options == null ? new CosmosQueryRequestOptions() : options;
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            CosmosAsyncClient client = this.getDatabase().getClient();
            String operationId = queryOptionsAccessor.getQueryNameOrDefault(requestOptions, this.queryConflictsSpanName);

            QueryFeedOperationState state = new QueryFeedOperationState(
                client,
                this.queryConflictsSpanName,
                database.getId(),
                this.getId(),
                ResourceType.Conflict,
                OperationType.Query,
                queryOptionsAccessor.getQueryNameOrDefault(requestOptions, this.queryConflictsSpanName),
                requestOptions,
                pagedFluxOptions
            );

            pagedFluxOptions.setFeedOperationState(state);

            return database.getDocClientWrapper().queryConflicts(getLink(), query, state)
                .map(response -> feedResponseAccessor.createFeedResponse(
                    ModelBridgeInternal.getCosmosConflictPropertiesFromV2Results(response.getResults()),
                    response.getResponseHeaders(),
                    response.getCosmosDiagnostics()));
        });
    }

    /**
     * Gets a {@link CosmosAsyncConflict} object using current container for context.
     *
     * @param id the id of the Cosmos conflict.
     * @return a Cosmos conflict.
     */
    public CosmosAsyncConflict getConflict(String id) {
        return new CosmosAsyncConflict(id, this);
    }

    /**
     * Replace the throughput.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.replaceThroughput -->
     * <pre>
     * ThroughputProperties throughputProperties =
     *     ThroughputProperties.createAutoscaledThroughput&#40;1000&#41;;
     *
     * cosmosAsyncContainer.replaceThroughput&#40;throughputProperties&#41;
     *     .subscribe&#40;throughputResponse -&gt; &#123;
     *             System.out.println&#40;throughputResponse&#41;;
     *         &#125;,
     *         throwable -&gt; &#123;
     *             throwable.printStackTrace&#40;&#41;;
     *         &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.replaceThroughput -->
     * @param throughputProperties the throughput properties.
     * @return the mono containing throughput response.
     */
    public Mono<ThroughputResponse> replaceThroughput(ThroughputProperties throughputProperties) {
        return withContext(context -> replaceThroughputInternal(throughputProperties, context));
    }

    /**
     * Read the throughput provisioned for the current container.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.readThroughput -->
     * <pre>
     * Mono&lt;ThroughputResponse&gt; throughputResponseMono = cosmosAsyncContainer.readThroughput&#40;&#41;;
     * throughputResponseMono.subscribe&#40;throughputResponse -&gt; &#123;
     *     System.out.println&#40;throughputResponse&#41;;
     * &#125;, throwable -&gt; &#123;
     *     throwable.printStackTrace&#40;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.readThroughput -->
     * @return the mono containing throughput response.
     */
    public Mono<ThroughputResponse> readThroughput() {
        return withContext(context -> readThroughputInternal(context));
    }

    /**
     * Gets the parent {@link CosmosAsyncDatabase} for the current container.
     *
     * @return the {@link CosmosAsyncDatabase}.
     */
    public CosmosAsyncDatabase getDatabase() {
        return database;
    }

    String getURIPathSegment() {
        return Paths.COLLECTIONS_PATH_SEGMENT;
    }

    String getParentLink() {
        return database.getLink();
    }

    String getLink() {
        return this.link;
    }

    private Mono<CosmosItemResponse<Object>> deleteItemInternal(
        String itemId,
        InternalObjectNode internalObjectNode,
        CosmosItemRequestOptions options,
        Context context) {

        WriteRetryPolicy nonIdempotentWriteRetryPolicy = itemOptionsAccessor
            .calculateAndGetEffectiveNonIdempotentRetriesEnabled(
                options,
                this.database.getClient().getNonIdempotentWriteRetryPolicy(),
                true);

        CosmosItemRequestOptions effectiveOptions = getEffectiveOptions(nonIdempotentWriteRetryPolicy, options);

        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(effectiveOptions);

        return this.deleteItemInternalCore(itemId, internalObjectNode, requestOptions, context);
    }

    private Mono<CosmosItemResponse<Object>> deleteItemInternalCore(
        String itemId,
        InternalObjectNode internalObjectNode,
        RequestOptions requestOptions,
        Context context) {
        Mono<CosmosItemResponse<Object>> responseMono = this.getDatabase()
            .getDocClientWrapper()
            .deleteDocument(getItemLink(itemId), internalObjectNode, requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponseWithObjectType(response))
            .single();
        CosmosAsyncClient client = database.getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosItemResponsePublisher(
                responseMono,
                context,
                this.deleteItemSpanName,
                this.getId(),
                database.getId(),
                client,
                requestOptions.getConsistencyLevel(),
                OperationType.Delete,
                ResourceType.Document,
                requestOptions,
                null);
    }

    private Mono<CosmosItemResponse<Object>> deleteAllItemsByPartitionKeyInternal(
        PartitionKey partitionKey,
        RequestOptions requestOptions,
        Context context) {
        Mono<CosmosItemResponse<Object>> responseMono = this.getDatabase()
            .getDocClientWrapper()
            .deleteAllDocumentsByPartitionKey(getLink(), partitionKey, requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponseWithObjectType(response))
            .single();
        CosmosAsyncClient client = database.getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosItemResponsePublisher(
                responseMono,
                context,
                this.deleteAllItemsByPartitionKeySpanName,
                this.getId(),
                database.getId(),
                client,
                requestOptions.getConsistencyLevel(),
                OperationType.Delete,
                ResourceType.PartitionKey,
                requestOptions,
                null);
    }

    private <T> Mono<CosmosItemResponse<T>> replaceItemInternalCore(
        Class<T> itemType,
        String itemId,
        Document doc,
        RequestOptions requestOptions,
        String trackingId) {

        requestOptions.setTrackingId(trackingId);

        return this.getDatabase()
                   .getDocClientWrapper()
                   .replaceDocument(getItemLink(itemId), doc, requestOptions)
                   .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()))
                   .single();
    }

    private CosmosItemRequestOptions getEffectiveOptions(
        WriteRetryPolicy nonIdempotentWriteRetryPolicy,
        CosmosItemRequestOptions options) {

        CosmosItemRequestOptions effectiveOptions = itemOptionsAccessor.clone(options);
        effectiveOptions.setConsistencyLevel(null);
        if (nonIdempotentWriteRetryPolicy.isEnabled()) {
            itemOptionsAccessor
                .setNonIdempotentWriteRetryPolicy(
                    effectiveOptions,
                    true,
                    nonIdempotentWriteRetryPolicy.useTrackingIdProperty());
        } else {
            itemOptionsAccessor
                .setNonIdempotentWriteRetryPolicy(
                    effectiveOptions,
                    false,
                    false);
        }

        return effectiveOptions;
    }

    private <T> Mono<CosmosItemResponse<T>> replaceItemInternal(
        Class<T> itemType,
        String itemId,
        Document doc,
        CosmosItemRequestOptions options,
        Context context) {

        checkNotNull(options, "Argument 'options' must not be null.");

        WriteRetryPolicy nonIdempotentWriteRetryPolicy = itemOptionsAccessor
            .calculateAndGetEffectiveNonIdempotentRetriesEnabled(
                options,
                this.database.getClient().getNonIdempotentWriteRetryPolicy(),
                true);

        CosmosItemRequestOptions effectiveOptions = getEffectiveOptions(nonIdempotentWriteRetryPolicy, options);
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(effectiveOptions);

        Mono<CosmosItemResponse<T>> responseMono;
        String trackingId = null;
        if (nonIdempotentWriteRetryPolicy.isEnabled() && nonIdempotentWriteRetryPolicy.useTrackingIdProperty()) {
            trackingId = UUID.randomUUID().toString();
            responseMono = this.replaceItemWithTrackingId(itemType, itemId, doc, requestOptions, trackingId);
        } else {
            responseMono = this.replaceItemInternalCore(itemType, itemId, doc, requestOptions, null);
        }

        CosmosAsyncClient client = database
            .getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosItemResponsePublisher(
                responseMono,
                context,
                this.replaceItemSpanName,
                this.getId(),
                database.getId(),
                client,
                ModelBridgeInternal.getConsistencyLevel(effectiveOptions),
                OperationType.Replace,
                ResourceType.Document,
                requestOptions,
                trackingId);
    }

    private <T> Mono<CosmosItemResponse<T>> patchItemInternal(
        String itemId,
        CosmosPatchOperations cosmosPatchOperations,
        CosmosPatchItemRequestOptions options,
        Context context,
        Class<T> itemType) {

        WriteRetryPolicy nonIdempotentWriteRetryPolicy = itemOptionsAccessor
            .calculateAndGetEffectiveNonIdempotentRetriesEnabled(
                options,
                this.database.getClient().getNonIdempotentWriteRetryPolicy(),
                false);

        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        if (nonIdempotentWriteRetryPolicy.isEnabled()) {
            requestOptions.setNonIdempotentWriteRetriesEnabled(true);
        }

        Mono<CosmosItemResponse<T>> responseMono = this.getDatabase()
            .getDocClientWrapper()
            .patchDocument(getItemLink(itemId), cosmosPatchOperations, requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()));

        CosmosAsyncClient client = database
            .getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosItemResponsePublisher(
                responseMono,
                context,
                this.patchItemSpanName,
                this.getId(),
                database.getId(),
                client,
                ModelBridgeInternal.getConsistencyLevel(options),
                OperationType.Patch,
                ResourceType.Document,
                requestOptions,
                null);
    }

    private <T> Mono<CosmosItemResponse<T>> upsertItemInternal(T item, CosmosItemRequestOptions options, Context context) {
        @SuppressWarnings("unchecked")
        Class<T> itemType = (Class<T>) item.getClass();

        WriteRetryPolicy nonIdempotentWriteRetryPolicy = itemOptionsAccessor
            .calculateAndGetEffectiveNonIdempotentRetriesEnabled(
                options,
                this.database.getClient().getNonIdempotentWriteRetryPolicy(),
                true);

        CosmosItemRequestOptions effectiveOptions = getEffectiveOptions(nonIdempotentWriteRetryPolicy, options);

        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(effectiveOptions);

        Mono<CosmosItemResponse<T>> responseMono = this.getDatabase().getDocClientWrapper()
            .upsertDocument(this.getLink(), item,
                requestOptions,
                true)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(
                response, itemType, getItemDeserializer()))
            .single();
        CosmosAsyncClient client = database
            .getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosItemResponsePublisher(
                responseMono,
                context,
                this.upsertItemSpanName,
                this.getId(),
                database.getId(),
                client,
                ModelBridgeInternal.getConsistencyLevel(effectiveOptions),
                OperationType.Upsert,
                ResourceType.Document,
                requestOptions,
                null);
    }

    private <T> Mono<CosmosItemResponse<T>> readItemInternal(
        String itemId,
        RequestOptions requestOptions, Class<T> itemType,
        Context context) {
        Mono<CosmosItemResponse<T>> responseMono = this.getDatabase().getDocClientWrapper()
            .readDocument(getItemLink(itemId), requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosAsyncItemResponse(response, itemType, getItemDeserializer()))
            .single();
        CosmosAsyncClient client = database
            .getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosItemResponsePublisher(
                responseMono,
                context,
                this.readItemSpanName,
                this.getId(),
                database.getId(),
                client,
                requestOptions.getConsistencyLevel(),
                OperationType.Read,
                ResourceType.Document,
                requestOptions,
                null);
    }

    Mono<CosmosContainerResponse> read(CosmosContainerRequestOptions options, Context context) {
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        Mono<CosmosContainerResponse> responseMono = database
            .getDocClientWrapper()
            .readCollection(getLink(), requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosContainerResponse(response)).single();

        CosmosAsyncClient client = database
            .getClient();

        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosResponsePublisher(
                responseMono,
                context,
                this.readContainerSpanName,
                database.getId(),
                this.id,
                client,
                null,
                OperationType.Read,
                ResourceType.DocumentCollection,
                requestOptions);
    }

    private Mono<CosmosContainerResponse> deleteInternal(CosmosContainerRequestOptions options, Context context) {
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        Mono<CosmosContainerResponse> responseMono = database
            .getDocClientWrapper()
            .deleteCollection(getLink(), requestOptions)
            .map(response -> ModelBridgeInternal.createCosmosContainerResponse(response)).single();

        CosmosAsyncClient client = database
            .getClient();

        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosResponsePublisher(
                responseMono,
                context,
                this.deleteContainerSpanName,
                database.getId(),
                this.id,
                client,
                null,
                OperationType.Replace,
                ResourceType.DocumentCollection,
                requestOptions);
    }

    private Mono<CosmosContainerResponse> replaceInternal(CosmosContainerProperties containerProperties,
                                                               CosmosContainerRequestOptions options,
                                                               Context context) {
        Mono<CosmosContainerResponse> responseMono = database.getDocClientWrapper()
            .replaceCollection(ModelBridgeInternal.getV2Collection(containerProperties),
                ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosContainerResponse(response)).single();
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        CosmosAsyncClient client = database
            .getClient();
        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosResponsePublisher(
                responseMono,
                context,
                this.replaceContainerSpanName,
                database.getId(),
                containerProperties.getId(),
                client,
                null,
                OperationType.Replace,
                ResourceType.DocumentCollection,
                requestOptions);
    }

    private Mono<ThroughputResponse> readThroughputInternal(Context context) {
        Context nestedContext = context.addData(
            DiagnosticsProvider.COSMOS_CALL_DEPTH,
            DiagnosticsProvider.COSMOS_CALL_DEPTH_VAL);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        Mono<ThroughputResponse> responseMono = readThroughputInternal(this.read(options,
            nestedContext));

        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        CosmosAsyncClient client = database
            .getClient();

        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosResponsePublisher(
                responseMono,
                context,
                this.readThroughputSpanName,
                database.getId(),
                this.id,
                client,
                null,
                OperationType.Read,
                ResourceType.Offer,
                requestOptions);
    }

    private Mono<ThroughputResponse> readThroughputInternal(Mono<CosmosContainerResponse> responseMono) {

        QueryFeedOperationState state = new QueryFeedOperationState(
            this.database.getClient(),
            "readThroughputInternal",
            this.database.getId(),
            this.getId(),
            ResourceType.Offer,
            OperationType.Query,
            null,
            new CosmosQueryRequestOptions(),
            new CosmosPagedFluxOptions()
        );

        return responseMono
            .flatMap(response -> this.database.getDocClientWrapper()
                .queryOffers(database.getOfferQuerySpecFromResourceId(response.getProperties()
                        .getResourceId())
                    , state)
                .single()
                .flatMap(offerFeedResponse -> {
                    if (offerFeedResponse.getResults().isEmpty()) {
                        return Mono.error(BridgeInternal
                            .createCosmosException(
                                HttpConstants.StatusCodes.BADREQUEST,
                                "No offers found for the resource "
                                    + this.getId()));
                    }
                    return this.database.getDocClientWrapper()
                        .readOffer(offerFeedResponse.getResults()
                            .get(0)
                            .getSelfLink())
                        .single();
                })
                .map(ModelBridgeInternal::createThroughputRespose));
    }

    private Mono<ThroughputResponse> replaceThroughputInternal(ThroughputProperties throughputProperties,
                                                               Context context) {
        Context nestedContext = context.addData(
            DiagnosticsProvider.COSMOS_CALL_DEPTH,
            DiagnosticsProvider.COSMOS_CALL_DEPTH_VAL);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        Mono<ThroughputResponse> responseMono =
            replaceThroughputInternal(this.read(options, nestedContext),
                throughputProperties);

        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        CosmosAsyncClient client = database
            .getClient();

        return client
            .getDiagnosticsProvider()
            .traceEnabledCosmosResponsePublisher(
                responseMono,
                context,
                this.replaceThroughputSpanName,
                database.getId(),
                this.id,
                client,
                null,
                OperationType.Replace,
                ResourceType.Offer,
                requestOptions);
    }

    private Mono<ThroughputResponse> replaceThroughputInternal(Mono<CosmosContainerResponse> responseMono,
                                                               ThroughputProperties throughputProperties) {

        QueryFeedOperationState state = new QueryFeedOperationState(
            this.database.getClient(),
            "replaceThroughputInternal",
            this.database.getId(),
            this.getId(),
            ResourceType.Offer,
            OperationType.Query,
            null,
            new CosmosQueryRequestOptions(),
            new CosmosPagedFluxOptions()
        );

        return responseMono
            .flatMap(response -> this.database.getDocClientWrapper()
                .queryOffers(database.getOfferQuerySpecFromResourceId(response.getProperties()
                        .getResourceId())
                    , state)
                .single()
                .flatMap(offerFeedResponse -> {
                    if (offerFeedResponse.getResults().isEmpty()) {
                        return Mono.error(BridgeInternal
                            .createCosmosException(
                                HttpConstants.StatusCodes.BADREQUEST,
                                "No offers found for the " +
                                    "resource " + this.getId()));
                    }

                    Offer existingOffer = offerFeedResponse.getResults().get(0);
                    Offer updatedOffer =
                        ModelBridgeInternal.updateOfferFromProperties(existingOffer,
                            throughputProperties);
                    return this.database.getDocClientWrapper()
                        .replaceOffer(updatedOffer)
                        .single();
                }).map(ModelBridgeInternal::createThroughputRespose));
    }

    ItemDeserializer getItemDeserializer() {
        return getDatabase().getDocClientWrapper().getItemDeserializer();
    }

    /**
     * Obtains a list of {@link FeedRange} that can be used to parallelize Feed
     * operations.
     * <!-- src_embed com.azure.cosmos.CosmosAsyncContainer.getFeedRanges -->
     * <pre>
     * cosmosAsyncContainer.getFeedRanges&#40;&#41;
     *     .subscribe&#40;feedRanges -&gt; &#123;
     *         for &#40;FeedRange feedRange : feedRanges&#41; &#123;
     *             System.out.println&#40;&quot;Feed range: &quot; + feedRange&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.CosmosAsyncContainer.getFeedRanges -->
     * @return An unmodifiable list of {@link FeedRange}
     */
    public Mono<List<FeedRange>> getFeedRanges() {
        return this.getFeedRanges(true);
    }

    Mono<List<FeedRange>> getFeedRanges(boolean forceRefresh) {
        return this.getDatabase().getDocClientWrapper().getFeedRanges(getLink(), forceRefresh);
    }

    /**
     * Attempts to split a feedrange into {@lparamtargetedCountAfterAplit} sub ranges. This is a best
     * effort - it is possible that the list of feed ranges returned has less than {@lparamtargetedCountAfterAplit}
     * sub ranges
     * @param feedRange
     * @param targetedCountAfterSplit
     * @return list of feed ranges after attempted split operation
     */
    Mono<List<FeedRangeEpkImpl>> trySplitFeedRange(FeedRange feedRange, int targetedCountAfterSplit) {
        checkNotNull(feedRange, "Argument 'feedRange' must not be null.");

        final AsyncDocumentClient clientWrapper = this.database.getDocClientWrapper();
        Mono<Utils.ValueHolder<DocumentCollection>> getCollectionObservable = clientWrapper
            .getCollectionCache()
            .resolveByNameAsync(null, this.getLinkWithoutTrailingSlash(), null)
            .map(collection -> Utils.ValueHolder.initialize(collection));

        return FeedRangeInternal
            .convert(feedRange)
            .trySplit(
                clientWrapper.getPartitionKeyRangeCache(),
                null,
                getCollectionObservable,
                targetedCountAfterSplit
            );
    }

    Mono<Range<String>> getNormalizedEffectiveRange(FeedRange feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' must not be null.");

        final AsyncDocumentClient clientWrapper = this.database.getDocClientWrapper();
        Mono<Utils.ValueHolder<DocumentCollection>> getCollectionObservable = clientWrapper
            .getCollectionCache()
            .resolveByNameAsync(null, this.getLinkWithoutTrailingSlash(), null)
            .map(collection -> Utils.ValueHolder.initialize(collection));

        return FeedRangeInternal
            .convert(feedRange)
            .getNormalizedEffectiveRange(
                clientWrapper.getPartitionKeyRangeCache(),
                null,
                getCollectionObservable);
    }

     /**
     * Enable the throughput control group with local control mode.
     * <br/>
     * <!-- src_embed com.azure.cosmos.throughputControl.localControl -->
     * <pre>
     * ThroughputControlGroupConfig groupConfig =
     *     new ThroughputControlGroupConfigBuilder&#40;&#41;
     *         .groupName&#40;&quot;localControlGroup&quot;&#41;
     *         .targetThroughputThreshold&#40;0.1&#41;
     *         .build&#40;&#41;;
     *
     * container.enableLocalThroughputControlGroup&#40;groupConfig&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.throughputControl.localControl -->
     *
     * @param groupConfig A {@link ThroughputControlGroupConfig}.
     */
    public void enableLocalThroughputControlGroup(ThroughputControlGroupConfig groupConfig) {
        this.enableLocalThroughputControlGroup(groupConfig, null);
    }

    /***
     * Only used internally.
     *
     * @param groupConfig A {@link ThroughputControlGroupConfig}.
     * @param throughputQueryMono The throughput query mono.
     */
    void enableLocalThroughputControlGroup(
        ThroughputControlGroupConfig groupConfig,
        Mono<Integer> throughputQueryMono) {

        LocalThroughputControlGroup localControlGroup =
            ThroughputControlGroupFactory.createThroughputLocalControlGroup(groupConfig, this);
        this.database.getClient().enableThroughputControlGroup(localControlGroup, throughputQueryMono);
    }

    /**
     * Enable the throughput control group with global control mode.
     * The defined throughput limit will be shared across different clients.
     * <br/>
     * <!-- src_embed com.azure.cosmos.throughputControl.globalControl -->
     * <pre>
     * ThroughputControlGroupConfig groupConfig =
     *     new ThroughputControlGroupConfigBuilder&#40;&#41;
     *         .groupName&#40;&quot;localControlGroup&quot;&#41;
     *         .targetThroughputThreshold&#40;0.1&#41;
     *         .build&#40;&#41;;
     *
     * GlobalThroughputControlConfig globalControlConfig =
     *     this.client.createGlobalThroughputControlConfigBuilder&#40;database.getId&#40;&#41;, container.getId&#40;&#41;&#41;
     *         .setControlItemRenewInterval&#40;Duration.ofSeconds&#40;5&#41;&#41;
     *         .setControlItemExpireInterval&#40;Duration.ofSeconds&#40;10&#41;&#41;
     *         .build&#40;&#41;;
     *
     * container.enableGlobalThroughputControlGroup&#40;groupConfig, globalControlConfig&#41;;
     * </pre>
     * <!-- end com.azure.cosmos.throughputControl.globalControl -->
     *
     * @param groupConfig The throughput control group configuration, see {@link GlobalThroughputControlGroup}.
     * @param globalControlConfig The global throughput control configuration, see {@link GlobalThroughputControlConfig}.
     */
    public void enableGlobalThroughputControlGroup(
        ThroughputControlGroupConfig groupConfig,
        GlobalThroughputControlConfig globalControlConfig) {

        this.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig, null);
    }

    /***
     * Only used internally.
     * <br/>
     * @param groupConfig The throughput control group configuration, see {@link GlobalThroughputControlGroup}.
     * @param globalControlConfig The global throughput control configuration, see {@link GlobalThroughputControlConfig}.
     * @param throughputQueryMono The throughput query mono.
     */
    void enableGlobalThroughputControlGroup(
        ThroughputControlGroupConfig groupConfig,
        GlobalThroughputControlConfig globalControlConfig,
        Mono<Integer> throughputQueryMono) {

        GlobalThroughputControlGroup globalControlGroup =
            ThroughputControlGroupFactory.createThroughputGlobalControlGroup(groupConfig, globalControlConfig, this);

        this.database.getClient().enableThroughputControlGroup(globalControlGroup, throughputQueryMono);
    }

    void configureFaultInjectionProvider(IFaultInjectorProvider injectorProvider) {
        this.database.getClient().configureFaultInjectorProvider(injectorProvider);
    }

    synchronized IFaultInjectorProvider getOrConfigureFaultInjectorProvider(Callable<IFaultInjectorProvider> injectorProviderCallable) {
        checkNotNull(injectorProviderCallable, "Argument 'injectorProviderCallable' can not be null");

        try {
            if (this.faultInjectorProvider == null) {
                this.faultInjectorProvider = injectorProviderCallable.call();
                this.configureFaultInjectionProvider(this.faultInjectorProvider);
            }

            return this.faultInjectorProvider;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure fault injector provider " + e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosAsyncContainerHelper.setCosmosAsyncContainerAccessor(
            new ImplementationBridgeHelpers.CosmosAsyncContainerHelper.CosmosAsyncContainerAccessor() {
                @Override
                public <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryChangeFeedInternalFunc(
                    CosmosAsyncContainer cosmosAsyncContainer,
                    CosmosChangeFeedRequestOptions cosmosChangeFeedRequestOptions,
                    Class<T> classType) {
                    return cosmosAsyncContainer.queryChangeFeedInternalFunc(cosmosChangeFeedRequestOptions, classType);
                }

                @Override
                public void enableLocalThroughputControlGroup(
                    CosmosAsyncContainer cosmosAsyncContainer,
                    ThroughputControlGroupConfig groupConfig,
                    Mono<Integer> throughputQueryMono) {

                    cosmosAsyncContainer.enableLocalThroughputControlGroup(groupConfig, throughputQueryMono);
                }

                @Override
                public void enableGlobalThroughputControlGroup(
                    CosmosAsyncContainer cosmosAsyncContainer,
                    ThroughputControlGroupConfig groupConfig,
                    GlobalThroughputControlConfig globalControlConfig,
                    Mono<Integer> throughputQueryMono) {
                    cosmosAsyncContainer.enableGlobalThroughputControlGroup(groupConfig, globalControlConfig, throughputQueryMono);
                }

                @Override
                public IFaultInjectorProvider getOrConfigureFaultInjectorProvider(
                    CosmosAsyncContainer cosmosAsyncContainer,
                    Callable<IFaultInjectorProvider> injectorProviderCallable) {

                    return cosmosAsyncContainer.getOrConfigureFaultInjectorProvider(injectorProviderCallable);
                }

                @Override
                public <T> Mono<FeedResponse<T>> readMany(
                    CosmosAsyncContainer cosmosAsyncContainer,
                    List<CosmosItemIdentity> itemIdentityList,
                    CosmosReadManyRequestOptions requestOptions,
                    Class<T> classType) {

                    return cosmosAsyncContainer.readMany(itemIdentityList, requestOptions, classType);
                }

                @Override
                public <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryItemsInternalFunc(
                    CosmosAsyncContainer cosmosAsyncContainer,
                    SqlQuerySpec sqlQuerySpec,
                    CosmosQueryRequestOptions cosmosQueryRequestOptions,
                    Class<T> classType) {

                    return cosmosAsyncContainer.queryItemsInternalFunc(sqlQuerySpec, cosmosQueryRequestOptions, classType);
                }

                @Override
                public <T> Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> queryItemsInternalFuncWithMonoSqlQuerySpec(
                    CosmosAsyncContainer cosmosAsyncContainer,
                    Mono<SqlQuerySpec> sqlQuerySpecMono,
                    CosmosQueryRequestOptions cosmosQueryRequestOptions,
                    Class<T> classType) {
                    return cosmosAsyncContainer.queryItemsInternalFunc(sqlQuerySpecMono, cosmosQueryRequestOptions, classType);
                }

                @Override
                public Mono<List<FeedRange>> getFeedRanges(CosmosAsyncContainer cosmosAsyncContainer, boolean forceRefresh) {
                    return cosmosAsyncContainer.getFeedRanges(forceRefresh);
                }
            });
    }

    static { initialize(); }
}
