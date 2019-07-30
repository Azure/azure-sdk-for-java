// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class BoolPartitionKeyComponent implements IPartitionKeyComponent {

    private final boolean value;

    public BoolPartitionKeyComponent(boolean value) {
        this.value = value;
    }

    @Override
    public int CompareTo(IPartitionKeyComponent other) {
        BoolPartitionKeyComponent otherBool = Utils.as(other, BoolPartitionKeyComponent.class);
        if (otherBool == null) {
            throw new IllegalArgumentException("other");
        }

        return (int) Math.signum((this.value ? 1 : 0) - (otherBool.value ? 1 : 0));
    }

    @Override
    public int GetTypeOrdinal() {
        return this.value ? PartitionKeyComponentType.TRUE.type : PartitionKeyComponentType.FALSE.type;
    }

    @Override
    public void JsonEncode(JsonGenerator writer) {
        try {
            writer.writeBoolean(this.value);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashing(OutputStream outputStream) {
        try {
            outputStream.write((byte) (this.value ? PartitionKeyComponentType.TRUE.type
                    : PartitionKeyComponentType.FALSE.type));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForHashingV2(OutputStream outputStream) {
        try {
            outputStream.write((byte) (this.value ? PartitionKeyComponentType.TRUE.type
                    : PartitionKeyComponentType.FALSE.type));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void WriteForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) (this.value ? PartitionKeyComponentType.TRUE.type
                    : PartitionKeyComponentType.FALSE.type));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IPartitionKeyComponent Truncate() {
        return this;
    }
}
