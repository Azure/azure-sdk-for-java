// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.util.arrow.Endianness;
import com.azure.storage.blob.implementation.util.arrow.MessageHeader;
import com.azure.storage.blob.implementation.util.arrow.TimeUnit;
import com.azure.storage.blob.implementation.util.arrow.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Fidelity tests that pin the vendored Arrow FlatBuffer metadata constants
 * (in {@code com.azure.storage.blob.implementation.util.arrow}) to the values defined by the official
 * {@code org.apache.arrow.flatbuf} classes shipped in {@code arrow-format} (pulled in transitively at test scope via
 * {@code arrow-vector}).
 * <p>
 * {@link BlobListArrowStreamReaderTests} validates decoding <em>behavior</em> against payloads written by the real
 * Arrow writer, but only exercises the enum values that a representative ListBlobs response happens to use. These tests
 * close that gap by asserting <em>every</em> enum ordinal and name our reader relies on matches Apache Arrow exactly,
 * including values that live on error/rejection paths (for example dictionary batches). If a future {@code arrow}
 * version bump renumbers or extends one of these enums, these tests fail loudly so the vendored copy can be reviewed.
 */
public class BlobListArrowFlatbufConstantsTest {

    @Test
    public void messageHeaderUnionMatchesArrow() {
        assertEquals(org.apache.arrow.flatbuf.MessageHeader.NONE, MessageHeader.NONE);
        assertEquals(org.apache.arrow.flatbuf.MessageHeader.Schema, MessageHeader.SCHEMA);
        assertEquals(org.apache.arrow.flatbuf.MessageHeader.DictionaryBatch, MessageHeader.DICTIONARY_BATCH);
        assertEquals(org.apache.arrow.flatbuf.MessageHeader.RecordBatch, MessageHeader.RECORD_BATCH);
        assertEquals(org.apache.arrow.flatbuf.MessageHeader.Tensor, MessageHeader.TENSOR);
        assertEquals(org.apache.arrow.flatbuf.MessageHeader.SparseTensor, MessageHeader.SPARSE_TENSOR);
    }

    @Test
    public void endiannessMatchesArrow() {
        assertEquals(org.apache.arrow.flatbuf.Endianness.Little, Endianness.LITTLE);
        assertEquals(org.apache.arrow.flatbuf.Endianness.Big, Endianness.BIG);
    }

    @Test
    public void timeUnitMatchesArrow() {
        assertEquals(org.apache.arrow.flatbuf.TimeUnit.SECOND, TimeUnit.SECOND);
        assertEquals(org.apache.arrow.flatbuf.TimeUnit.MILLISECOND, TimeUnit.MILLISECOND);
        assertEquals(org.apache.arrow.flatbuf.TimeUnit.MICROSECOND, TimeUnit.MICROSECOND);
        assertEquals(org.apache.arrow.flatbuf.TimeUnit.NANOSECOND, TimeUnit.NANOSECOND);

        // The vendored name table must match Arrow's, element-for-element and in length.
        assertEquals(org.apache.arrow.flatbuf.TimeUnit.names.length, namesLength(TimeUnit::name),
            "Arrow TimeUnit enum changed size; review the vendored TimeUnit.");
        for (int i = 0; i < org.apache.arrow.flatbuf.TimeUnit.names.length; i++) {
            assertEquals(org.apache.arrow.flatbuf.TimeUnit.name(i), TimeUnit.name(i),
                "TimeUnit name mismatch at ordinal " + i);
        }
    }

    @Test
    public void typeUnionOrdinalsMatchArrow() {
        assertEquals(org.apache.arrow.flatbuf.Type.NONE, Type.NONE);
        assertEquals(org.apache.arrow.flatbuf.Type.Null, Type.NULL);
        assertEquals(org.apache.arrow.flatbuf.Type.Int, Type.INT);
        assertEquals(org.apache.arrow.flatbuf.Type.FloatingPoint, Type.FLOATING_POINT);
        assertEquals(org.apache.arrow.flatbuf.Type.Binary, Type.BINARY);
        assertEquals(org.apache.arrow.flatbuf.Type.Utf8, Type.UTF8);
        assertEquals(org.apache.arrow.flatbuf.Type.Bool, Type.BOOL);
        assertEquals(org.apache.arrow.flatbuf.Type.Decimal, Type.DECIMAL);
        assertEquals(org.apache.arrow.flatbuf.Type.Date, Type.DATE);
        assertEquals(org.apache.arrow.flatbuf.Type.Time, Type.TIME);
        assertEquals(org.apache.arrow.flatbuf.Type.Timestamp, Type.TIMESTAMP);
        assertEquals(org.apache.arrow.flatbuf.Type.Interval, Type.INTERVAL);
        assertEquals(org.apache.arrow.flatbuf.Type.List, Type.LIST);
        assertEquals(org.apache.arrow.flatbuf.Type.Struct_, Type.STRUCT);
        assertEquals(org.apache.arrow.flatbuf.Type.Union, Type.UNION);
        assertEquals(org.apache.arrow.flatbuf.Type.FixedSizeBinary, Type.FIXED_SIZE_BINARY);
        assertEquals(org.apache.arrow.flatbuf.Type.FixedSizeList, Type.FIXED_SIZE_LIST);
        assertEquals(org.apache.arrow.flatbuf.Type.Map, Type.MAP);
        assertEquals(org.apache.arrow.flatbuf.Type.Duration, Type.DURATION);
        assertEquals(org.apache.arrow.flatbuf.Type.LargeBinary, Type.LARGE_BINARY);
        assertEquals(org.apache.arrow.flatbuf.Type.LargeUtf8, Type.LARGE_UTF8);
        assertEquals(org.apache.arrow.flatbuf.Type.LargeList, Type.LARGE_LIST);
        assertEquals(org.apache.arrow.flatbuf.Type.RunEndEncoded, Type.RUN_END_ENCODED);
        assertEquals(org.apache.arrow.flatbuf.Type.BinaryView, Type.BINARY_VIEW);
        assertEquals(org.apache.arrow.flatbuf.Type.Utf8View, Type.UTF8_VIEW);
        assertEquals(org.apache.arrow.flatbuf.Type.ListView, Type.LIST_VIEW);
        assertEquals(org.apache.arrow.flatbuf.Type.LargeListView, Type.LARGE_LIST_VIEW);
    }

    @Test
    public void typeUnionNamesMatchArrow() {
        // A length mismatch means Arrow added/removed a Type; the vendored Type (and reader's switch) must be reviewed.
        assertEquals(org.apache.arrow.flatbuf.Type.names.length, namesLength(Type::name),
            "Arrow Type enum changed size; review the vendored Type and the reader's type switch.");
        for (int i = 0; i < org.apache.arrow.flatbuf.Type.names.length; i++) {
            assertEquals(org.apache.arrow.flatbuf.Type.name(i), Type.name(i), "Type name mismatch at ordinal " + i);
        }
    }

    /** Counts a vendored name table's length by probing for its array bound. */
    private static int namesLength(java.util.function.IntFunction<String> nameFn) {
        int count = 0;
        while (true) {
            try {
                nameFn.apply(count);
                count++;
            } catch (ArrayIndexOutOfBoundsException e) {
                return count;
            }
        }
    }
}

