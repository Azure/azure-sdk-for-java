// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

/**
 * Discriminator values for the Arrow IPC {@code MessageHeader} union.
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
    /** A tensor header. */
    public static final byte TENSOR = 4;
    /** A sparse tensor header. */
    public static final byte SPARSE_TENSOR = 5;
}

