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

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

class StringPartitionKeyComponent implements IPartitionKeyComponent {

    public static final int MAX_STRING_CHARS = 100;
    public static final int MAX_STRING_BYTES_TO_APPEND = 100;
    private final String value;
    private final byte[] utf8Value;

    public StringPartitionKeyComponent(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value");
        }

        this.value = value;
        try {
            this.utf8Value = com.azure.data.cosmos.internal.Utils.getUTF8Bytes(value);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public int CompareTo(IPartitionKeyComponent other) {
        StringPartitionKeyComponent otherString = Utils.as(other, StringPartitionKeyComponent.class) ;
        if (otherString == null) {
            throw new IllegalArgumentException("other");
        }

        return this.value.compareTo(otherString.value);
    }

    @Override
    public int GetTypeOrdinal() {
        return PartitionKeyComponentType.STRING.type;
    }

    @Override
    public int hashCode() {
        // hashCode for hashmap dictionary, etc
        return value.hashCode();
    }

    public IPartitionKeyComponent Truncate() {
        if (this.value.length() > MAX_STRING_CHARS) {
            return new StringPartitionKeyComponent(this.value.substring(0, MAX_STRING_CHARS));
        }

        return this;
    }

    @Override
    public void JsonEncode(JsonGenerator writer) {
        try {
            writer.writeString(this.value);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.STRING.type);
            outputStream.write(utf8Value);
            outputStream.write((byte) 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashingV2(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.STRING.type);
            outputStream.write(utf8Value);
            outputStream.write((byte) 0xFF);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.STRING.type);
            boolean shortString = this.utf8Value.length <= MAX_STRING_BYTES_TO_APPEND;

            for (int index = 0; index < (shortString ? this.utf8Value.length : MAX_STRING_BYTES_TO_APPEND + 1); index++) {
                byte charByte = this.utf8Value[index];
                if (charByte < 0xFF) charByte++;
                outputStream.write(charByte);
            }

            if (shortString) {
                outputStream.write((byte) 0x00);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
