// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class InfinityPartitionKeyComponent implements IPartitionKeyComponent {
    @Override
    public int CompareTo(IPartitionKeyComponent other) {
        InfinityPartitionKeyComponent otherInfinity = Utils.as(other, InfinityPartitionKeyComponent.class);
        if (otherInfinity == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int GetTypeOrdinal() {
        return PartitionKeyComponentType.INFINITY.type;
    }

    @Override
    public void JsonEncode(JsonGenerator writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void WriteForHashing(OutputStream outputStream) {
        throw new IllegalStateException();
    }

    @Override
    public void WriteForHashingV2(OutputStream outputStream) {
        throw new IllegalStateException();
    }

    @Override
    public void WriteForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.INFINITY.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IPartitionKeyComponent Truncate() {
        return this;
    }
}
