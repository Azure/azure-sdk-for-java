// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class NullPartitionKeyComponent implements IPartitionKeyComponent {

    public static final NullPartitionKeyComponent VALUE = new NullPartitionKeyComponent();

    @Override
    public int compareTo(IPartitionKeyComponent other) {
        NullPartitionKeyComponent otherMinString = Utils.as(other, NullPartitionKeyComponent.class);
        if (otherMinString == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int getTypeOrdinal() {
        return PartitionKeyComponentType.NULL.type;
    }

    @Override
    public void jsonEncode(JsonGenerator writer) {
        try {
            writer.writeObject(null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NULL.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForHashingV2(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NULL.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.NULL.type);
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
        return null;
    }
}
