// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch.hybridrow;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;

/** Encodes and decodes the RecordIO envelope used by Cosmos batch requests. */
final class RecordIoCodec {
    private RecordIoCodec() {
    }

    static byte[] encode(List<byte[]> records) {
        Objects.requireNonNull(records, "records");
        int capacity = HybridRowBatchSchema.RECORD_IO_SEGMENT_LENGTH;
        for (byte[] record : records) {
            capacity = Math.addExact(capacity, Math.addExact(13, Objects.requireNonNull(record, "record").length));
        }
        ByteBuf output = Unpooled.buffer(capacity, capacity);
        HybridRowWireWriter writer = new HybridRowWireWriter(output);
        writer.writeByte(HybridRowBatchSchema.VERSION);
        writer.writeInt32(HybridRowBatchSchema.RECORD_IO_SEGMENT_SCHEMA_ID);
        writer.writeByte(1);
        writer.writeInt32(HybridRowBatchSchema.RECORD_IO_SEGMENT_LENGTH);
        for (byte[] record : records) {
            CRC32 crc = new CRC32();
            crc.update(record);
            writer.writeByte(HybridRowBatchSchema.VERSION);
            writer.writeInt32(HybridRowBatchSchema.RECORD_IO_RECORD_SCHEMA_ID);
            writer.writeInt32(record.length);
            writer.writeInt32((int) crc.getValue());
            output.writeBytes(record);
        }
        byte[] result = new byte[output.readableBytes()];
        output.readBytes(result);
        return result;
    }

    static List<byte[]> decode(byte[] payload, int maxRecords) {
        if (maxRecords < 0) {
            throw new IllegalArgumentException("maxRecords must be nonnegative");
        }
        if (payload == null || payload.length == 0) {
            return Collections.emptyList();
        }
        ByteBuf input = Unpooled.wrappedBuffer(payload);
        HybridRowWireReader reader = new HybridRowWireReader(input);
        reader.expectByte(HybridRowBatchSchema.VERSION, "segment version");
        reader.expectInt32(HybridRowBatchSchema.RECORD_IO_SEGMENT_SCHEMA_ID, "segment schema");
        reader.expectByte(1, "segment presence");
        reader.expectInt32(HybridRowBatchSchema.RECORD_IO_SEGMENT_LENGTH, "segment length");
        List<byte[]> records = new ArrayList<>();
        while (reader.readableBytes() != 0) {
            if (records.size() >= maxRecords) {
                throw new CorruptedFrameException("RecordIO record count exceeds request operation count");
            }
            reader.expectByte(HybridRowBatchSchema.VERSION, "record version");
            reader.expectInt32(HybridRowBatchSchema.RECORD_IO_RECORD_SCHEMA_ID, "record schema");
            int length = reader.readNonNegativeInt32("record length");
            long expectedCrc = reader.readUnsignedInt32();
            ByteBuf record = reader.readRetainedSlice(length);
            try {
                byte[] bytes = new byte[length];
                record.getBytes(record.readerIndex(), bytes);
                CRC32 crc = new CRC32();
                crc.update(bytes);
                if (crc.getValue() != expectedCrc) {
                    throw new CorruptedFrameException("RecordIO CRC mismatch");
                }
                records.add(bytes);
            } finally {
                record.release();
            }
        }
        return records;
    }
}
