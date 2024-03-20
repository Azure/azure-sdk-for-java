// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class UndefinedPartitionKeyComponent implements IPartitionKeyComponent {

    public static final UndefinedPartitionKeyComponent VALUE = new UndefinedPartitionKeyComponent();

    @Override
    public int compareTo(IPartitionKeyComponent other) {
        UndefinedPartitionKeyComponent otherUndefined = Utils.as(other, UndefinedPartitionKeyComponent.class);
        if (otherUndefined == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int getTypeOrdinal() {
        return PartitionKeyComponentType.UNDEFINED.type;
    }

    @Override
    public void jsonEncode(JsonGenerator writer) {
        try {
            writer.writeStartObject();
            writer.writeEndObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.UNDEFINED.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForHashingV2(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.UNDEFINED.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.UNDEFINED.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IPartitionKeyComponent truncate() {
        return this;
    }

    @Override
    public Object toObject() {
        return Undefined.value();
    }
}
