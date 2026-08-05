// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

import com.azure.cosmos.models.CosmosItemOperationType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

/** Encodes one operation using the fixed Cosmos BatchOperation HybridRow V1 schema. */
final class BatchOperationCodec {
    private BatchOperationCodec() {
    }

    static byte[] encode(Operation operation) {
        Objects.requireNonNull(operation, "operation");
        int capacity = 64 + length(operation.partitionKey) + length(operation.id)
            + length(operation.resourceBody) + operation.options.estimatedLength();
        ByteBuf output = Unpooled.buffer(capacity);
        HybridRowWireWriter writer = new HybridRowWireWriter(output);
        writer.writeByte(HybridRowBatchSchema.VERSION);
        writer.writeInt32(HybridRowBatchSchema.OPERATION_SCHEMA_ID);
        writer.writeByte(operation.presenceMask());
        writer.writeInt32(toWireOperation(operation.operationType));
        writer.writeInt32(HybridRowBatchSchema.DOCUMENT_RESOURCE_TYPE);
        writer.writeVariable(operation.partitionKey);
        writer.writeVariable(operation.id);
        writer.writeVariable(operation.resourceBody);
        writer.writeSparseString(HybridRowBatchSchema.OperationField.INDEXING_DIRECTIVE,
            operation.options.indexingDirective);
        writer.writeSparseString(HybridRowBatchSchema.OperationField.IF_MATCH, operation.options.ifMatch);
        writer.writeSparseString(HybridRowBatchSchema.OperationField.IF_NONE_MATCH, operation.options.ifNoneMatch);
        writer.writeSparseBoolean(HybridRowBatchSchema.OperationField.MINIMAL_RETURN,
            operation.options.minimalReturn);
        byte[] result = new byte[output.readableBytes()];
        output.readBytes(result);
        return result;
    }

    private static int toWireOperation(CosmosItemOperationType operationType) {
        switch (operationType) {
            case CREATE: return 0;
            case PATCH: return 1;
            case READ: return 2;
            case DELETE: return 4;
            case REPLACE: return 5;
            case UPSERT: return 20;
            default: throw new IllegalArgumentException("Unsupported batch operation: " + operationType);
        }
    }

    private static int length(String value) {
        return value == null ? 0 : value.length() * 3;
    }

    private static int length(byte[] value) {
        return value == null ? 0 : value.length;
    }

    static final class Operation {
        private final CosmosItemOperationType operationType;
        private String partitionKey;
        private String id;
        private byte[] resourceBody;
        private Options options = Options.NONE;

        Operation(CosmosItemOperationType operationType) {
            this.operationType = Objects.requireNonNull(operationType, "operationType");
        }

        Operation partitionKey(String value) {
            this.partitionKey = value;
            return this;
        }

        Operation id(String value) {
            this.id = value;
            return this;
        }

        Operation resourceBody(byte[] value) {
            this.resourceBody = value == null ? null : value.clone();
            return this;
        }

        Operation options(Options value) {
            this.options = Objects.requireNonNull(value, "options");
            return this;
        }

        private int presenceMask() {
            return HybridRowBatchSchema.OperationField.OPERATION_TYPE.presenceBit()
                | HybridRowBatchSchema.OperationField.RESOURCE_TYPE.presenceBit()
                | present(partitionKey, HybridRowBatchSchema.OperationField.PARTITION_KEY)
                | present(id, HybridRowBatchSchema.OperationField.ID)
                | present(resourceBody, HybridRowBatchSchema.OperationField.RESOURCE_BODY);
        }

        private static int present(Object value, HybridRowBatchSchema.OperationField field) {
            return value == null ? 0 : field.presenceBit();
        }
    }

    static final class Options {
        static final Options NONE = new Options(null, null, null, false);
        private final String indexingDirective;
        private final String ifMatch;
        private final String ifNoneMatch;
        private final boolean minimalReturn;

        Options(String indexingDirective, String ifMatch, String ifNoneMatch, boolean minimalReturn) {
            this.indexingDirective = indexingDirective;
            this.ifMatch = ifMatch;
            this.ifNoneMatch = ifNoneMatch;
            this.minimalReturn = minimalReturn;
        }

        private int estimatedLength() {
            return length(indexingDirective) + length(ifMatch) + length(ifNoneMatch) + 16;
        }
    }
}
