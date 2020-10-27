// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.Utils;

import java.nio.ByteBuffer;

class serializerDefaultMappingsImp implements SerializerDefaultMappings {
    @Override
    public boolean deserializeAsBoolean(byte[] bytes) {
        return bytes[0] == (byte) 1;
    }

    @Override
    public Double deserializeAsDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    @Override
    public String deserializeAsString(byte[] bytes) {
        return new String(bytes);
    }

    @Override
    public byte[] serializeBoolean(boolean val) {
        //TODO optimizat
        return val ? new byte[] { 1 } : new byte[] { 0 };
    }

    @Override
    public byte[] serializeDouble(double val) {
        byte[] buffer = new byte[8];
        ByteBuffer.wrap(buffer).putDouble(val);
        return buffer;
    }

    @Override
    public byte[] serializeString(String val) {
        return Utils.getUTF8Bytes(val);
    }
}
