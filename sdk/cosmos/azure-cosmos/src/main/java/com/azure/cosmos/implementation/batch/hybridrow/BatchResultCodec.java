// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;

/** Decodes one result using the fixed Cosmos BatchResult HybridRow V1 schema. */
final class BatchResultCodec {
    private BatchResultCodec() {
    }

    static Result decode(byte[] row) {
        ByteBuf input = Unpooled.wrappedBuffer(row);
        HybridRowWireReader reader = new HybridRowWireReader(input);
        reader.expectByte(HybridRowBatchSchema.VERSION, "result version");
        reader.expectInt32(HybridRowBatchSchema.RESULT_SCHEMA_ID, "result schema");
        int presence = reader.readUnsignedByte();
        int required = HybridRowBatchSchema.ResultField.STATUS_CODE.presenceBit()
            | HybridRowBatchSchema.ResultField.SUB_STATUS_CODE.presenceBit();
        if ((presence & required) != required) {
            throw new CorruptedFrameException("Batch result is missing required fields");
        }
        Result result = new Result(reader.readInt32(), reader.readInt32());
        if ((presence & HybridRowBatchSchema.ResultField.ETAG.presenceBit()) != 0) {
            result.eTag = reader.readVariableString();
        }
        if ((presence & HybridRowBatchSchema.ResultField.RESOURCE_BODY.presenceBit()) != 0) {
            result.resourceBody = reader.readVariableBytes();
        }
        boolean retryAfterRead = false;
        boolean requestChargeRead = false;
        while (reader.readableBytes() != 0) {
            int typeCode = reader.readUnsignedByte();
            int pathToken = readPathToken(reader);
            HybridRowBatchSchema.ResultField field = HybridRowBatchSchema.ResultField.sparseFromPathToken(pathToken);
            HybridRowBatchSchema.SparseType type = HybridRowBatchSchema.SparseType.fromCode(typeCode);
            if (field == HybridRowBatchSchema.ResultField.RETRY_AFTER_MILLISECONDS
                && type == HybridRowBatchSchema.SparseType.UINT32) {
                if (retryAfterRead) {
                    throw new CorruptedFrameException("Duplicate BatchResult retry-after field");
                }
                result.retryAfterMilliseconds = reader.readUnsignedInt32();
                retryAfterRead = true;
            } else if (field == HybridRowBatchSchema.ResultField.REQUEST_CHARGE
                && type == HybridRowBatchSchema.SparseType.FLOAT64) {
                if (requestChargeRead) {
                    throw new CorruptedFrameException("Duplicate BatchResult request-charge field");
                }
                result.requestCharge = reader.readFloat64();
                requestChargeRead = true;
            } else {
                throw new CorruptedFrameException(
                    "Unsupported BatchResult sparse field token/type: " + pathToken + "/" + typeCode);
            }
        }
        return result;
    }

    private static int readPathToken(HybridRowWireReader reader) {
        int first = reader.readUnsignedByte();
        if ((first & 0x80) == 0) {
            return first;
        }
        int second = reader.readUnsignedByte();
        if ((second & 0x80) != 0) {
            throw new CorruptedFrameException("BatchResult path token is too large");
        }
        return (first & 0x7F) | (second << 7);
    }

    static final class Result {
        private final int statusCode;
        private final int subStatusCode;
        private String eTag;
        private byte[] resourceBody;
        private long retryAfterMilliseconds;
        private double requestCharge;

        private Result(int statusCode, int subStatusCode) {
            this.statusCode = statusCode;
            this.subStatusCode = subStatusCode;
        }

        int getStatusCode() { return statusCode; }
        int getSubStatusCode() { return subStatusCode; }
        String getETag() { return eTag; }
        byte[] getResourceBody() { return resourceBody == null ? null : resourceBody.clone(); }
        long getRetryAfterMilliseconds() { return retryAfterMilliseconds; }
        double getRequestCharge() { return requestCharge; }
    }
}
