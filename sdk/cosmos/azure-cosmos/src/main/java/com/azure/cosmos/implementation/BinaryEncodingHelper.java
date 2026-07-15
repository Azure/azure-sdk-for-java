// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.ConnectionMode;

public final class BinaryEncodingHelper {
    private BinaryEncodingHelper() {
    }

    static boolean canUseBinaryEncoding(
        ConnectionMode connectionMode,
        ResourceType resourceType,
        OperationType operationType,
        RequestOptions options) {

        return Configs.isBinaryEncodingEnabled()
            && connectionMode == ConnectionMode.DIRECT
            && resourceType == ResourceType.Document
            && isSupportedPointOperation(operationType)
            && !hasTriggers(options);
    }

    public static boolean canUseBinaryBatch(ConnectionMode connectionMode) {
        return Configs.isBinaryEncodingEnabled() && connectionMode == ConnectionMode.DIRECT;
    }

    public static boolean canUseBinaryQueryResponse(RxDocumentServiceRequest request) {
        if (!canUseBinaryDocumentResponse(request)) {
            return false;
        }
        OperationType operationType = request.getOperationType();
        return operationType == OperationType.Query || operationType == OperationType.SqlQuery;
    }

    public static boolean canUseBinaryChangeFeedResponse(RxDocumentServiceRequest request) {
        return canUseBinaryDocumentResponse(request)
            && request.getOperationType() == OperationType.ReadFeed
            && request.isChangeFeedRequest();
    }

    private static boolean canUseBinaryDocumentResponse(RxDocumentServiceRequest request) {
        return Configs.isBinaryEncodingEnabled()
            && request != null
            && !request.useThinClientMode
            && request.getResourceType() == ResourceType.Document;
    }

    private static boolean isSupportedPointOperation(OperationType operationType) {
        return operationType == OperationType.Create
            || operationType == OperationType.Upsert
            || operationType == OperationType.Replace
            || operationType == OperationType.Read
            || operationType == OperationType.Delete;
    }

    private static boolean hasTriggers(RequestOptions options) {
        return options != null
            && ((options.getPreTriggerInclude() != null && !options.getPreTriggerInclude().isEmpty())
            || (options.getPostTriggerInclude() != null && !options.getPostTriggerInclude().isEmpty()));
    }
}
