// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class UndefinedPartitionKeyComponent implements IPartitionKeyComponent {

    public static final UndefinedPartitionKeyComponent VALUE = new UndefinedPartitionKeyComponent();

    @Override
    public int CompareTo(IPartitionKeyComponent other) {
        UndefinedPartitionKeyComponent otherUndefined = Utils.as(other, UndefinedPartitionKeyComponent.class);
        if (otherUndefined == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int GetTypeOrdinal() {
        return PartitionKeyComponentType.UNDEFINED.type;
    }

    @Override
    public void JsonEncode(JsonGenerator writer) {
        try {
            writer.writeStartObject();
            writer.writeEndObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.UNDEFINED.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashingV2(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.UNDEFINED.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.UNDEFINED.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IPartitionKeyComponent Truncate() {
        return this;
    }
}
