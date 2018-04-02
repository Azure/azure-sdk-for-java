/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.routing;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;

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
        BigInteger longValue = new BigInteger(Long.toBinaryString(rawLongBits), 2);
        BigInteger maskLongValue = new BigInteger(Long.toBinaryString(mask), 2);
        return (longValue.compareTo(maskLongValue) < 0)
                ? rawLongBits ^ mask
                : (~rawLongBits) + 1;
    }

    @Override
    public int CompareTo(IPartitionKeyComponent other) {
        if (!(other instanceof NumberPartitionKeyComponent)) {
            throw new IllegalArgumentException("other");
        }

        return Double.compare(this.value, ((NumberPartitionKeyComponent) other).value);
    }

    @Override
    public int GetTypeOrdinal() {
        return PartitionKeyComponentType.NUMBER.getValue();
    }

    @Override
    public void JsonEncode(JsonGenerator writer) {
        try {
            writer.writeRaw(String.valueOf(value));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NUMBER.getValue());
            outputStream.write(doubleToByteArray(this.value));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NUMBER.getValue());

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

                byteToWrite = (byte) ((payload >> (64 - 8)) | 1);
                payload <<= 7;
            } while (payload != 0);

            // Except for last chunk that ends with "0" bit.
            outputStream.write((byte) (byteToWrite & 0xFE));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
