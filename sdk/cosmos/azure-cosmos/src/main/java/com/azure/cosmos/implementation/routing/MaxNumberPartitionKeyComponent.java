// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

class MaxNumberPartitionKeyComponent implements IPartitionKeyComponent {
    public static final MaxNumberPartitionKeyComponent VALUE = new MaxNumberPartitionKeyComponent();

    @Override
    public int compareTo(IPartitionKeyComponent other) {
        MaxNumberPartitionKeyComponent otherMaxNumber = Utils.as(other, MaxNumberPartitionKeyComponent.class);
        if (otherMaxNumber == null) {
            throw new IllegalArgumentException("other");
        }

        return 0;
    }

    @Override
    public int getTypeOrdinal() {
        return PartitionKeyComponentType.MAXNUMBER.ordinal();
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
            outputStream.write((byte) PartitionKeyComponentType.MAXNUMBER.type);
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
        return MaxNumber.VALUE;
    }

    private static class MaxNumber
    {
        public static final MaxNumber VALUE = new MaxNumber();
    }
}
