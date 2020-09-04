// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.batch.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.directconnectivity.DirectBridgeInternal;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.netty.buffer.ByteBufUtil;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.OPERATION_CREATE;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.OPERATION_DELETE;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.OPERATION_READ;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.OPERATION_REPLACE;
import static com.azure.cosmos.batch.implementation.BatchRequestResponseConstant.OPERATION_UPSERT;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

/**
 * Util methods for batch requests/response.
 */
public class BatchExecUtils {

    /**
     * Converts the current {@link TransactionalBatchOperationResult transactional batch operation result} to a {@link
     * RxDocumentServiceResponse batch response message}.
     *
     * @param result a {@link TransactionalBatchOperationResult}
     * @return a new {@link RxDocumentServiceResponse batch response message}.
     */
    public static RxDocumentServiceResponse toResponseMessage(TransactionalBatchOperationResult<?> result) {

        Map<String, String> headers = getResponseHeaders(result);

        StoreResponse storeResponse = new StoreResponse(
            result.getResponseStatus(),
            new ArrayList<>(headers.entrySet()),
            result.getResourceObject() != null ? Utils.getUTF8BytesOrNull(result.getResourceObject().toString()) : null);


        if (result.getCosmosDiagnostics() != null) {
            DirectBridgeInternal.setCosmosDiagnostics(storeResponse, result.getCosmosDiagnostics());
        }

        return new RxDocumentServiceResponse(storeResponse);
    }

    public static Map<String, String> getResponseHeaders(TransactionalBatchOperationResult<?> result) {
        Map<String, String> headers = new HashMap<>();

        headers.put(HttpConstants.HttpHeaders.SUB_STATUS, String.valueOf(result.getSubStatusCode()));
        headers.put(HttpConstants.HttpHeaders.E_TAG, result.getETag());
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(result.getRequestCharge()));

        if (result.getRetryAfter() != null) {
            headers.put(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, String.valueOf(result.getRetryAfter().toMillis()));
        }

        return headers;
    }

    static Mono<DocumentCollection> getCollectionInfoAsync(AsyncDocumentClient documentClient, CosmosAsyncContainer container) {
        RxClientCollectionCache clientCollectionCache = documentClient.getCollectionCache();
        return clientCollectionCache
            .resolveByNameAsync(
                BridgeInternal.getMetaDataDiagnosticContext(BridgeInternal.createCosmosDiagnostics()),
                BridgeInternal.getLink(container),
                null);
    }

    public static void ensureValid(
        final List<ItemBatchOperation<?>> operations,
        final RequestOptions options) {

        final String errorMessage = BatchExecUtils.isValid(operations, options);
        checkArgument(errorMessage == null, errorMessage);
    }

    public static String getPartitionKeyRangeId(
        final PartitionKey key,
        final PartitionKeyDefinition keyDefinition,
        final CollectionRoutingMap collectionRoutingMap) {

        checkNotNull(key, "expected non-null key");
        checkNotNull(keyDefinition, "expected non-null keyDefinition");
        checkNotNull(collectionRoutingMap, "expected non-null collectionRoutingMap");

        String epkString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(
            BridgeInternal.getPartitionKeyInternal(key),
            keyDefinition);

        return collectionRoutingMap.getRangeByEffectivePartitionKey(epkString).getId();
    }

    public static String isValid(final List<ItemBatchOperation<?>> operations, final RequestOptions batchOptions) {

        String errorMessage = null;

        if (operations == null) {
            errorMessage = "expected non-null operations";
        }

        if (errorMessage == null && operations.size() == 0) {
            errorMessage = "expected operations.size > 0";
        }

        if (errorMessage == null && batchOptions != null) {
          if (batchOptions.getIfMatchETag() != null || batchOptions.getIfNoneMatchETag() != null) {
              errorMessage = "one or more request options provided on the batch request are not supported";
          }
        }

        if (errorMessage == null) {
            for (ItemBatchOperation<?> operation : operations) {

                final RequestOptions batchOperationOptions = operation.getRequestOptions();
                final Map<String, Object> batchOperationProperties = batchOperationOptions != null ? batchOperationOptions.getProperties() : null;

                if (batchOperationProperties != null
                    && (batchOperationProperties.containsKey(WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY)
                    || batchOperationProperties.containsKey(WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING)
                    || batchOperationProperties.containsKey(HttpConstants.HttpHeaders.PARTITION_KEY))) {

                    final String epkString = (String) batchOperationProperties.computeIfPresent(
                        WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING,
                        (k, v) -> v instanceof String ? v : null);

                    final byte[] epk = (byte[]) batchOperationProperties.computeIfPresent(
                        WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY,
                        (k, v) -> v instanceof byte[] ? v : null);

                    final String pkString = (String) batchOperationProperties.computeIfPresent(
                        HttpConstants.HttpHeaders.PARTITION_KEY,
                        (k, v) -> v instanceof String ? v : null);

                    if ((epk == null && pkString == null) || epkString == null) {
                        return lenientFormat(
                            "expected byte[] value for %s and string value for %s, not (%s, %s)",
                            WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY,
                            WFConstants.BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING,
                            epk == null
                                ? (pkString == null ? "null" : pkString)
                                : ByteBufUtil.hexDump(epk), epkString == null ? "null" : epkString);
                    }

                    if (operation.getPartitionKey() != null ) {
                        errorMessage = "partition key and effective partition key may not both be set.";
                    }
                }
            }
        }

        return errorMessage;
    }

    static String getStringOperationType(OperationType operationType) {
        switch (operationType) {
            case Create:
                return OPERATION_CREATE;
            case Delete:
                return OPERATION_DELETE;
            case Read:
                return OPERATION_READ;
            case Replace:
                return OPERATION_REPLACE;
            case Upsert:
                return OPERATION_UPSERT;
        }

        return null;
    }
}
