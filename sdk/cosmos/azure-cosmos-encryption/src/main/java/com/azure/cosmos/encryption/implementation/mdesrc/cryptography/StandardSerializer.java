/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;


abstract class StandardSerializer extends Serializer<Object> {}


class BooleanSerializer extends StandardSerializer {

    private static final byte[] f = new byte[] {0};
    private static final byte[] t = new byte[] {1};

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return (boolean) value ? t : f;
        return null;
    }

    @Override
    public Boolean deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0 || bytes.length > 1)
            return null;

        if (bytes[0] == 0)
            return false;
        else if (bytes[0] == 1)
            return true;
        else
            return null;
    }
}


class StringSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return ((String) value).getBytes(Charset.forName("UTF-16"));
        return null;
    }

    @Override
    public String deserialize(byte[] bytes) {
        if (null == bytes)
            return null;
        else
            return new String(bytes, Charset.forName("UTF-16"));
    }
}


class CharSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return ByteBuffer.allocate(Character.BYTES).putChar((char) value).array();
        return null;
    }

    @Override
    public Character deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return ByteBuffer.wrap(bytes).getChar();
    }
}


class IntegerSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return ByteBuffer.allocate(Integer.BYTES).putInt((int) value).array();
        return null;
    }

    @Override
    public Integer deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return ByteBuffer.wrap(bytes).getInt();
    }
}

class LongSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return ByteBuffer.allocate(Long.BYTES).putLong((long) value).array();
        return null;
    }

    @Override
    public Long deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return ByteBuffer.wrap(bytes).getLong();
    }
}

class DoubleSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return ByteBuffer.allocate(Double.BYTES).putDouble((double) value).array();
        return null;
    }

    @Override
    public Double deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return ByteBuffer.wrap(bytes).getDouble();
    }
}


class FloatSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return ByteBuffer.allocate(Float.BYTES).putFloat((float) value).array();
        return null;
    }

    @Override
    public Float deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return ByteBuffer.wrap(bytes).getFloat();
    }
}


class ByteSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null)
            return new byte[] {(Byte) value};
        return null;
    }

    @Override
    public Byte deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0 || bytes.length > 1)
            return null;
        else
            return bytes[0];
    }
}


class UuidSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null) {
            ByteBuffer b = ByteBuffer.wrap(new byte[16]);
            b.putLong(((UUID) value).getMostSignificantBits());
            b.putLong(((UUID) value).getLeastSignificantBits());
            return b.array();
        }
        return null;
    }

    @Override
    public UUID deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0)
            return null;
        else {
            ByteBuffer b = ByteBuffer.wrap(bytes);
            Long high = b.getLong();
            Long low = b.getLong();
            return new UUID(high, low);
        }
    }
}


class DateSerializer extends StandardSerializer {

    @Override
    public byte[] serialize(Object value) {
        if (value != null) {
            return ByteBuffer.allocate(Long.BYTES).putLong(((Date)value).getTime()).array();
        }
        return null;
    }

    @Override
    public Date deserialize(byte[] bytes) {
        if (null == bytes || bytes.length == 0)
            return null;
        else
            return new Date(ByteBuffer.wrap(bytes).getLong());
    }
}
