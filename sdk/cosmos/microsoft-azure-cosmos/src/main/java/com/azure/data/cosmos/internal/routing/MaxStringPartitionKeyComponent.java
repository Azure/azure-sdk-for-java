// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.azure.data.cosmos.internal.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class MaxStringPartitionKeyComponent implements IPartitionKeyComponent {
    public static final MaxStringPartitionKeyComponent VALUE = new MaxStringPartitionKeyComponent();

    @Override
    public int CompareTo(IPartitionKeyComponent other) {
        MaxStringPartitionKeyComponent otherMaxString = Utils.as(other, MaxStringPartitionKeyComponent.class);
        if (otherMaxString == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int GetTypeOrdinal() {
        return PartitionKeyComponentType.MAXSTRING.ordinal();
    }

    @Override
    public void JsonEncode(JsonGenerator writer) {
        PartitionKeyInternal.PartitionKeyInternalJsonSerializer.jsonEncode(this, writer);
    }

    @Override
    public void WriteForHashing(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void WriteForHashingV2(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void WriteForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.MAXSTRING.type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IPartitionKeyComponent Truncate() {
        return this;
    }

}
