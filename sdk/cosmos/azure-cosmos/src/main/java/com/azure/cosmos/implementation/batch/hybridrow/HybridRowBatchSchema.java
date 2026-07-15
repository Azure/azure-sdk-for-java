// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

/** Wire constants for the fixed Cosmos batch HybridRow schemas. */
final class HybridRowBatchSchema {
    static final int VERSION = 0x81;
    static final int DOCUMENT_RESOURCE_TYPE = 2;
    static final int OPERATION_SCHEMA_ID = 2_145_473_648;
    static final int RESULT_SCHEMA_ID = 2_145_473_649;
    static final int RECORD_IO_SEGMENT_SCHEMA_ID = 2_147_473_648;
    static final int RECORD_IO_RECORD_SCHEMA_ID = 2_147_473_649;
    static final int RECORD_IO_SEGMENT_LENGTH = 10;

    private HybridRowBatchSchema() {
    }

    enum OperationField {
        OPERATION_TYPE(0, Storage.FIXED),
        RESOURCE_TYPE(1, Storage.FIXED),
        PARTITION_KEY(2, Storage.VARIABLE),
        EFFECTIVE_PARTITION_KEY(3, Storage.VARIABLE),
        ID(4, Storage.VARIABLE),
        BINARY_ID(5, Storage.VARIABLE),
        RESOURCE_BODY(6, Storage.VARIABLE),
        INDEXING_DIRECTIVE(8, Storage.SPARSE),
        IF_MATCH(9, Storage.SPARSE),
        IF_NONE_MATCH(10, Storage.SPARSE),
        TTL_SECONDS(11, Storage.SPARSE),
        MINIMAL_RETURN(12, Storage.SPARSE);

        private final int index;
        private final Storage storage;

        OperationField(int index, Storage storage) {
            this.index = index;
            this.storage = storage;
        }

        int presenceBit() {
            if (storage == Storage.SPARSE) {
                throw new IllegalStateException(name() + " is sparse");
            }
            return 1 << index;
        }

        int pathToken() {
            if (storage != Storage.SPARSE) {
                throw new IllegalStateException(name() + " is not sparse");
            }
            return index;
        }
    }

    enum ResultField {
        STATUS_CODE(0, Storage.FIXED),
        SUB_STATUS_CODE(1, Storage.FIXED),
        ETAG(2, Storage.VARIABLE),
        RESOURCE_BODY(3, Storage.VARIABLE),
        // Sparse tokens include the schema's root token at index zero.
        RETRY_AFTER_MILLISECONDS(5, Storage.SPARSE),
        REQUEST_CHARGE(6, Storage.SPARSE);

        private final int index;
        private final Storage storage;

        ResultField(int index, Storage storage) {
            this.index = index;
            this.storage = storage;
        }

        int presenceBit() {
            if (storage == Storage.SPARSE) {
                throw new IllegalStateException(name() + " is sparse");
            }
            return 1 << index;
        }

        static ResultField sparseFromPathToken(int token) {
            for (ResultField field : values()) {
                if (field.storage == Storage.SPARSE && field.index == token) {
                    return field;
                }
            }
            return null;
        }
    }

    enum SparseType {
        BOOLEAN_TRUE(0x03),
        UINT32(0x0B),
        FLOAT64(0x10),
        UTF8(0x14);

        private final int code;

        SparseType(int code) {
            this.code = code;
        }

        int code() {
            return code;
        }

        static SparseType fromCode(int code) {
            for (SparseType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }
    }

    private enum Storage {
        FIXED,
        VARIABLE,
        SPARSE
    }
}
