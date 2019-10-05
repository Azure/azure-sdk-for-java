// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Used internally to represent a number component in the partition key of the Azure Cosmos DB database service.
 */
public class NumberPartitionKeyComponent implements IPartitionKeyComponent {

    public static final NumberPartitionKeyComponent Zero = new NumberPartitionKeyComponent(0);
    private final double value;

    public NumberPartitionKeyComponent(double value) {
        this.value = value;
    }

    private static byte[] doubleToByteArray(double d) {
        byte[] output = new byte[8];
        long lng = Double.doubleToLongBits(d);
        for (int i = 0; i < 8; i++) {
            output[i] = (byte) ((lng >> (i * 8)) & 0xff);
        }
        return output;
    }

    private static long EncodeDoubleAsUInt64(double value) {
        long rawLongBits = Double.doubleToRawLongBits(value);
        long mask = 0x8000000000000000L;
        return Long.compareUnsigned(rawLongBits, mask) < 0
                ? rawLongBits ^ mask
                : (~rawLongBits) + 1;
    }

    @Override
    public int CompareTo(IPartitionKeyComponent other) {
        NumberPartitionKeyComponent otherBool = Utils.as(other, NumberPartitionKeyComponent.class);
        if (otherBool == null) {
            throw new IllegalArgumentException("other");
        }

        return Double.compare(this.value, ((NumberPartitionKeyComponent) other).value);
    }

    @Override
    public int GetTypeOrdinal() {
        return PartitionKeyComponentType.NUMBER.type;
    }

    @Override
    public void JsonEncode(JsonGenerator writer) {
        try {
            writer.writeNumber(String.valueOf(value));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NUMBER.type);
            outputStream.write(doubleToByteArray(this.value));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashingV2(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NUMBER.type);
            outputStream.write(doubleToByteArray(this.value));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NUMBER.type);

            long payload = NumberPartitionKeyComponent.EncodeDoubleAsUInt64(this.value);

            // Encode first chunk with 8-bits of payload
            outputStream.write((byte) (payload >> (64 - 8)));
            payload <<= 8;

            // Encode remaining chunks with 7 bits of payload followed by single "1" bit each.
            byte byteToWrite = 0;
            boolean firstIteration = true;
            do {
                if (!firstIteration) {
                    outputStream.write(byteToWrite);
                } else {
                    firstIteration = false;
                }

                byteToWrite = (byte) ((payload >> (64 - 8)) | 0x01);
                payload <<= 7;
            } while (payload != 0);

            // Except for last chunk that ends with "0" bit.
            outputStream.write((byte) (byteToWrite & 0xFE));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IPartitionKeyComponent Truncate() {
        return this;
    }
}
