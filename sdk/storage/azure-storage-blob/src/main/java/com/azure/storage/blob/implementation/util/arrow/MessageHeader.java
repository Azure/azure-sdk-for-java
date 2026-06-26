// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

/**
 * Discriminator values for the Arrow IPC {@code MessageHeader} union.
 * <p>
 * Only the subset that a ListBlobs response can contain is defined as active constants: {@link #SCHEMA} and
 * {@link #RECORD_BATCH} (a ListBlobs payload is a tabular result, i.e. a schema followed by record batches),
 * plus {@link #DICTIONARY_BATCH}, which the reader recognizes solely to reject it. The union's remaining members,
 * {@code TENSOR} (4) and {@code SPARSE_TENSOR} (5), are valid in the Arrow format but are never emitted for a
 * ListBlobs response, so they are intentionally kept commented out below rather than deleted. If a future service or
 * format change ever starts sending them, uncomment those constants and add the corresponding handling in
 * {@code BlobListArrowStreamReader}.
 */
public final class MessageHeader {
    private MessageHeader() {
    }

    /** No header. */
    public static final byte NONE = 0;
    /** A {@link Schema} header. */
    public static final byte SCHEMA = 1;
    /** A dictionary batch header. */
    public static final byte DICTIONARY_BATCH = 2;
    /** A {@link RecordBatch} header. */
    public static final byte RECORD_BATCH = 3;
    // TENSOR (4) and SPARSE_TENSOR (5) are omitted on purpose; see the class javadoc for why and how to revive them.
    // public static final byte TENSOR = 4;
    // public static final byte SPARSE_TENSOR = 5;
}
