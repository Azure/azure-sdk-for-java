// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class MaxStringPartitionKeyComponent implements IPartitionKeyComponent {
    public static final MaxStringPartitionKeyComponent VALUE = new MaxStringPartitionKeyComponent();

    @Override
    public int compareTo(IPartitionKeyComponent other) {
        MaxStringPartitionKeyComponent otherMaxString = Utils.as(other, MaxStringPartitionKeyComponent.class);
        if (otherMaxString == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int getTypeOrdinal() {
        return PartitionKeyComponentType.MAXSTRING.ordinal();
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
            outputStream.write((byte) PartitionKeyComponentType.MAXSTRING.type);
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
        return MaxString.VALUE;
    }

    private static class MaxString
    {
        public final static MaxString VALUE = new MaxString();
    }
}
