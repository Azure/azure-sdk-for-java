// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

import com.azure.cosmos.models.CosmosItemOperationType;

import java.util.ArrayList;
import java.util.List;

/** Internal facade for the fixed Cosmos HybridRow batch wire format. */
public final class HybridRowBatchCodec {
    private HybridRowBatchCodec() {
    }

    public static byte[] encodeOperation(
        CosmosItemOperationType operationType,
        String partitionKey,
        String id,
        byte[] resourceBody,
        String indexingDirective,
        String ifMatch,
        String ifNoneMatch,
        boolean minimalReturn) {

        BatchOperationCodec.Options options = new BatchOperationCodec.Options(
            indexingDirective, ifMatch, ifNoneMatch, minimalReturn);
        return BatchOperationCodec.encode(new BatchOperationCodec.Operation(operationType)
            .partitionKey(partitionKey)
            .id(id)
            .resourceBody(resourceBody)
            .options(options));
    }

    public static byte[] encodeRecordIo(List<byte[]> operations) {
        return RecordIoCodec.encode(operations);
    }

    public static List<Result> decodeResponse(byte[] payload, int maxResults) {
        List<byte[]> rows = RecordIoCodec.decode(payload, maxResults);
        List<Result> results = new ArrayList<>(rows.size());
        for (byte[] row : rows) {
            results.add(new Result(BatchResultCodec.decode(row)));
        }
        return results;
    }

    public static final class Result {
        private final BatchResultCodec.Result result;

        private Result(BatchResultCodec.Result result) {
            this.result = result;
        }

        public int getStatusCode() { return result.getStatusCode(); }
        public int getSubStatusCode() { return result.getSubStatusCode(); }
        public String getETag() { return result.getETag(); }
        public byte[] getResourceBody() { return result.getResourceBody(); }
        public long getRetryAfterMilliseconds() { return result.getRetryAfterMilliseconds(); }
        public double getRequestCharge() { return result.getRequestCharge(); }
    }
}
