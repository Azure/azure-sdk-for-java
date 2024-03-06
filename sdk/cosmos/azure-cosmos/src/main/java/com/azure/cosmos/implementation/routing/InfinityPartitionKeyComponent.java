// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class InfinityPartitionKeyComponent implements IPartitionKeyComponent {
    @Override
    public int compareTo(IPartitionKeyComponent other) {
        InfinityPartitionKeyComponent otherInfinity = Utils.as(other, InfinityPartitionKeyComponent.class);
        if (otherInfinity == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int getTypeOrdinal() {
        return PartitionKeyComponentType.INFINITY.type;
    }

    @Override
    public void jsonEncode(JsonGenerator writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeForHashing(OutputStream outputStream) {
        throw new IllegalStateException();
    }

    @Override
    public void writeForHashingV2(OutputStream outputStream) {
        throw new IllegalStateException();
    }

    @Override
    public void writeForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.INFINITY.type);
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
        throw new UnsupportedOperationException("toObject is not supported for InfinityPartitionKeyComponent");
    }
}
