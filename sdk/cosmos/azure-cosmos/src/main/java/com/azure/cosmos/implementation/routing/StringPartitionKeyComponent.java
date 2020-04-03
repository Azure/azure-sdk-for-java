// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Utils;
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
        this.utf8Value = Utils.getUTF8Bytes(value);
    }

    @Override
    public int compareTo(IPartitionKeyComponent other) {
        StringPartitionKeyComponent otherString = Utils.as(other, StringPartitionKeyComponent.class) ;
        if (otherString == null) {
            throw new IllegalArgumentException("other");
        }

        return this.value.compareTo(otherString.value);
    }

    @Override
    public int getTypeOrdinal() {
        return PartitionKeyComponentType.STRING.type;
    }

    @Override
    public int hashCode() {
        // hashCode for hashmap dictionary, etc
        return value.hashCode();
    }

    public IPartitionKeyComponent truncate() {
        if (this.value.length() > MAX_STRING_CHARS) {
            return new StringPartitionKeyComponent(this.value.substring(0, MAX_STRING_CHARS));
        }

        return this;
    }

    @Override
    public void jsonEncode(JsonGenerator writer) {
        try {
            writer.writeString(this.value);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.STRING.type);
            outputStream.write(utf8Value);
            outputStream.write((byte) 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForHashingV2(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.STRING.type);
            outputStream.write(utf8Value);
            outputStream.write((byte) 0xFF);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForBinaryEncoding(OutputStream outputStream) {
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
