// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;
import io.netty.handler.codec.http.HttpResponseStatus;

public final class PartitionKeyRangeBatchExecutionResult {

    private final Iterable<ItemBatchOperation<?>> operations;
    private final String partitionKeyRangeId;
    private final TransactionalBatchResponse serverResponse;

    public PartitionKeyRangeBatchExecutionResult(
        final String pkRangeId,
        final Iterable<ItemBatchOperation<?>> operations,
        final TransactionalBatchResponse serverResponse) {

        this.partitionKeyRangeId = pkRangeId;
        this.serverResponse = serverResponse;
        this.operations = operations;
    }

    public boolean isSplit() {
        TransactionalBatchResponse response = this.getServerResponse();

        return response != null && response.getResponseStatus() == HttpResponseStatus.GONE
            && (response.getSubStatusCode() == SubStatusCodes.COMPLETING_SPLIT
            || response.getSubStatusCode() == SubStatusCodes.COMPLETING_PARTITION_MIGRATION
            || response.getSubStatusCode() == SubStatusCodes.PARTITION_KEY_RANGE_GONE);
    }

    public Iterable<ItemBatchOperation<?>> getBatchOperations() {
        return operations;
    }

    public String getPartitionKeyRangeId() {
        return partitionKeyRangeId;
    }

    public TransactionalBatchResponse getServerResponse() {
        return serverResponse;
    }

}
