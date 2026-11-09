// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class MinStringPartitionKeyComponent implements IPartitionKeyComponent {
    public static final MinStringPartitionKeyComponent VALUE = new MinStringPartitionKeyComponent();

    @Override
    public int compareTo(IPartitionKeyComponent other) {
        MinStringPartitionKeyComponent otherMinString = Utils.as(other, MinStringPartitionKeyComponent.class);
        if (otherMinString == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int getTypeOrdinal() {
        return PartitionKeyComponentType.MINSTRING.ordinal();
    }

    @Override
    public void jsonEncode(JsonGenerator writer) {
        PartitionKeyInternal.PartitionKeyInternalJsonSerializer.jsonEncode(this, writer);
    }

    @Override
    public void writeForHashing(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeForHashingV2(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void writeForBinaryEncoding(OutputStream outputStream) {
        try {
            outputStream.write((byte) PartitionKeyComponentType.MINSTRING.type);
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
        return MinString.VALUE;
    }

    private static final class MinString
    {
        public static final MinString VALUE = new MinString();
    }
}
