// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.guava25.base.Utf8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

enum RntbdTokenType {

    // All values are encoded as little endian byte sequences except for Guid
    // Guid values are serialized in Microsoft GUID byte order
    // Reference: GUID structure and System.Guid type

    Byte((byte)0x00, RntbdByte.codec),                // byte => byte
    UShort((byte)0x01, RntbdUnsignedShort.codec),     // short => int
    ULong((byte)0x02, RntbdUnsignedInteger.codec),    // int => long
    Long((byte)0x03, RntbdInteger.codec),             // int => int
    ULongLong((byte)0x04, RntbdLong.codec),           // long => long
    LongLong((byte)0x05, RntbdLong.codec),            // long => long

    Guid((byte)0x06, RntbdGuid.codec),                // byte[16] => UUID
    SmallString((byte)0x07, RntbdShortString.codec),  // (byte, byte[0..255]) => String
    String((byte)0x08, RntbdString.codec),            // (short, byte[0..64KiB]) => String
    ULongString((byte)0x09, RntbdLongString.codec),   // (int, byte[0..2GiB-1]) => String

    SmallBytes((byte)0x0A, RntbdShortBytes.codec),    // (byte, byte[0..255]) => byte[]
    Bytes((byte)0x0B, RntbdBytes.codec),              // (short, byte[0..64KiB]) => byte[]
    ULongBytes((byte)0x0C, RntbdLongBytes.codec),     // (int, byte[0..2GiB-1])    => byte[]

    Float((byte)0x0D, RntbdFloat.codec),              // float => float
    Double((byte)0x0E, RntbdDouble.codec),            // double => double

    Invalid((byte)0xFF, RntbdNone.codec);             // no data

    // region Implementation
    private static final RntbdTokenType[] allTokens = getAllTokens();

    private static  RntbdTokenType[] getAllTokens() {
        final int maxByteValue = 0xFF + 1;
        final RntbdTokenType[] allPossibleTokens = new RntbdTokenType[maxByteValue]; // one byte RNTBD limit
        for(int i = 0; i < maxByteValue; i++) {
            allPossibleTokens[i] = Invalid;
        }

        // Override with valid entries
        for (final RntbdTokenType tokenType : RntbdTokenType.values()) {
            if (tokenType.id != Invalid.id) { // byte (0xFF auto-translates to -1)
                allPossibleTokens[tokenType.id] = tokenType;
            }
        }

        return allPossibleTokens;
    }

    private Codec codec;
    private byte id;

    RntbdTokenType(final byte id, final Codec codec) {
        this.codec = codec;
        this.id = id;
    }

    public Codec codec() {
        return this.codec;
    }

    public static RntbdTokenType fromId(final byte value) {
        if (value == Invalid.id) {
            return Invalid;
        }

        return allTokens[value];
    }

    public byte id() {
        return this.id;
    }

    // endregion

    // region Types

    public interface Codec {

        int computeLength(Object value);

        Object convert(Object value);

        Object defaultValue();

        boolean isValid(Object value);

        Object read(ByteBuf in);

        ByteBuf readSlice(ByteBuf in);

        Class<?> valueType();

        void write(Object value, ByteBuf out);

        static void checkReadableBytes(final ByteBuf in, final long length, final long maxLength) {

            if (length > maxLength) {
                throw new CorruptedFrameException(
                    lenientFormat("value length (%s) is greater than maxLength (%s)", length, maxLength));
            }

            final int readableBytes = in.readableBytes();

            if (length != readableBytes) {
                throw new CorruptedFrameException(
                    lenientFormat("readableBytes (%s) does not match value length (%s)", readableBytes, length));
            }
        }
    }

    private static class RntbdByte implements Codec {

        public static final Codec codec = new RntbdByte();

        private RntbdByte() {
        }

        @Override
        public final int computeLength(final Object value) {
            return java.lang.Byte.BYTES;
        }

        @Override
        public final Object convert(final Object value) {

            assert this.isValid(value);

            if (value instanceof Number) {
                return ((Number)value).byteValue();
            }
            return (boolean)value ? (byte)0x01 : (byte)0x00;
        }

        @Override
        public final Object defaultValue() {
            return (byte)0;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof Number || value instanceof Boolean;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return in.readByte();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Byte.BYTES);
        }

        @Override
        public final Class<?> valueType() {
            return java.lang.Byte.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeByte(value instanceof Byte ? (byte)value : ((boolean)value ? 0x01 : 0x00));
        }
    }

    private static class RntbdBytes implements Codec {

        public static final Codec codec = new RntbdBytes();
        private static final byte[] defaultValue = {};

        private RntbdBytes() {
        }

        @Override
        public int computeLength(final Object value) {
            assert this.isValid(value);
            return Short.BYTES + ((byte[])value).length;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return value;
        }

        @Override
        public final Object defaultValue() {
            return defaultValue;
        }

        @Override
        public boolean isValid(final Object value) {
            return value instanceof byte[] && ((byte[])value).length < 0xFFFF;
        }

        @Override
        public Object read(final ByteBuf in) {
            final int length = in.readUnsignedShortLE();
            Codec.checkReadableBytes(in, length, 0xFFFF);
            return in.readBytes(length);
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            final int length = in.getUnsignedShortLE(in.readerIndex());
            return in.readSlice(Short.BYTES + length);
        }

        @Override
        public Class<?> valueType() {
            return Byte[].class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {

            assert this.isValid(value);

            final byte[] bytes = (byte[])value;
            final int length = bytes.length;

            if (length > 0xFFFF) {
                throw new IllegalStateException();
            }

            out.writeShortLE((short)length);
            out.writeBytes(bytes);
        }
    }

    private static class RntbdDouble implements Codec {

        public static final Codec codec = new RntbdDouble();

        private RntbdDouble() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Double.BYTES;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number)value).doubleValue();
        }

        @Override
        public final Object defaultValue() {
            return 0.0D;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return in.readDoubleLE();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Double.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Double.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeDoubleLE(((Number)value).doubleValue());
        }
    }

    private static class RntbdFloat implements Codec {

        public static final Codec codec = new RntbdFloat();

        private RntbdFloat() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Float.BYTES;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number)value).floatValue();
        }

        @Override
        public final Object defaultValue() {
            return 0.0F;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return in.readFloatLE();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Float.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Float.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeFloatLE(((Number)value).floatValue());
        }
    }

    private static class RntbdGuid implements Codec {

        public static final Codec codec = new RntbdGuid();

        private RntbdGuid() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return 2 * java.lang.Long.BYTES;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return value;
        }

        @Override
        public final Object defaultValue() {
            return RntbdUUID.EMPTY;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof UUID;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return RntbdUUID.decode(in);
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(2 * java.lang.Long.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return UUID.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            RntbdUUID.encode((UUID)value, out);
        }
    }

    private static class RntbdInteger implements Codec {

        public static final Codec codec = new RntbdInteger();

        private RntbdInteger() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return Integer.BYTES;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number)value).intValue();
        }

        @Override
        public final Object defaultValue() {
            return 0;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return in.readIntLE();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Integer.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Integer.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeIntLE(((Number)value).intValue());
        }
    }

    private static class RntbdLong implements Codec {

        public static final Codec codec = new RntbdLong();

        private RntbdLong() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Long.BYTES;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number)value).longValue();
        }

        @Override
        public final Object defaultValue() {
            return 0L;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return in.readLongLE();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Long.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Long.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeLongLE(((Number)value).longValue());
        }
    }

    private static class RntbdLongBytes extends RntbdBytes {

        public static final Codec codec = new RntbdLongBytes();

        private RntbdLongBytes() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return Integer.BYTES + ((byte[])value).length;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof byte[];
        }

        @Override
        public final Object read(final ByteBuf in) {
            final long length = in.readUnsignedIntLE();
            Codec.checkReadableBytes(in, length, Integer.MAX_VALUE);
            return in.readBytes((int)length);
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            final long length = in.getUnsignedIntLE(in.readerIndex());
            checkState(length <= Integer.MAX_VALUE);
            return in.readSlice(Integer.BYTES + (int)length);
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {

            assert this.isValid(value);

            final byte[] bytes = (byte[])value;
            out.writeIntLE(bytes.length);
            out.writeBytes(bytes);
        }
    }

    private static class RntbdLongString extends RntbdString {

        public static final Codec codec = new RntbdLongString();

        private RntbdLongString() {
        }

        @Override
        public final int computeLength(final Object value) {
            return Integer.BYTES + this.computeLength(value, Integer.MAX_VALUE);
        }

        @Override
        public final Object read(final ByteBuf in) {
            final long length = in.readUnsignedIntLE();
            Codec.checkReadableBytes(in, length, Integer.MAX_VALUE);
            return in.readCharSequence((int)length, StandardCharsets.UTF_8).toString();
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            final int length = this.computeLength(value, Integer.MAX_VALUE);
            out.writeIntLE(length);
            writeValue(out, value, length);
        }
    }

    private static class RntbdNone implements Codec {

        public static final Codec codec = new RntbdNone();

        @Override
        public final int computeLength(final Object value) {
            return 0;
        }

        @Override
        public final Object convert(final Object value) {
            return null;
        }

        @Override
        public final Object defaultValue() {
            return null;
        }

        @Override
        public final boolean isValid(final Object value) {
            return true;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return null;
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return null;
        }

        @Override
        public Class<?> valueType() {
            return null;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
        }
    }

    private static class RntbdShortBytes extends RntbdBytes {

        public static final Codec codec = new RntbdShortBytes();

        private RntbdShortBytes() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return java.lang.Byte.BYTES + ((byte[])value).length;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof byte[] && ((byte[])value).length <= 0xFF;
        }

        @Override
        public final Object read(final ByteBuf in) {

            final int length = in.readUnsignedByte();
            Codec.checkReadableBytes(in, length, 0xFF);
            final byte[] bytes = new byte[length];
            in.readBytes(bytes);

            return bytes;
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Byte.BYTES + in.getUnsignedByte(in.readerIndex()));
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {

            assert this.isValid(value);

            final byte[] bytes = (byte[])value;
            final int length = bytes.length;

            if (length > 0xFF) {
                throw new IllegalStateException();
            }

            out.writeByte((byte)length);
            out.writeBytes(bytes);
        }
    }

    private static class RntbdShortString extends RntbdString {

        public static final Codec codec = new RntbdShortString();

        private RntbdShortString() {
        }

        @Override
        public final int computeLength(final Object value) {
            return java.lang.Byte.BYTES + this.computeLength(value, 0xFF);
        }

        @Override
        public final Object read(final ByteBuf in) {
            final int length = in.readUnsignedByte();
            Codec.checkReadableBytes(in, length, 0xFF);
            return in.readCharSequence(length, StandardCharsets.UTF_8).toString();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(java.lang.Byte.BYTES + in.getUnsignedByte(in.readerIndex()));
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {

            final int length = this.computeLength(value, 0xFF);
            out.writeByte(length);
            writeValue(out, value, length);
        }
    }

    private static class RntbdString implements Codec {

        public static final Codec codec = new RntbdString();

        private RntbdString() {
        }

        @SuppressWarnings("UnstableApiUsage")
        final int computeLength(final Object value, final int maxLength) {

            assert this.isValid(value);
            final int length;

            if (value instanceof String) {

                final String string = (String)value;
                length = Utf8.encodedLength(string);

            } else {

                final byte[] string = (byte[])value;

                if (!Utf8.isWellFormed(string)) {
                    final String reason = lenientFormat("UTF-8 byte string is ill-formed: %s", ByteBufUtil.hexDump(string));
                    throw new CorruptedFrameException(reason);
                }

                length = string.length;
            }

            if (length > maxLength) {
                final String reason = lenientFormat("UTF-8 byte string exceeds %s bytes: %s bytes", maxLength, length);
                throw new CorruptedFrameException(reason);
            }

            return length;
        }

        @Override
        public int computeLength(final Object value) {
            return Short.BYTES + this.computeLength(value, 0xFFFF);
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return value instanceof String ? value : new String((byte[])value, StandardCharsets.UTF_8);
        }

        @Override
        public final Object defaultValue() {
            return "";
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof String || value instanceof byte[];
        }

        @Override
        public Object read(final ByteBuf in) {
            final int length = in.readUnsignedShortLE();
            Codec.checkReadableBytes(in, length, 0xFFFF);
            return in.readCharSequence(length, StandardCharsets.UTF_8).toString();
        }

        @Override
        public ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Short.BYTES + in.getUnsignedShortLE(in.readerIndex()));
        }

        @Override
        public Class<?> valueType() {
            return String.class;
        }

        @Override
        public void write(final Object value, final ByteBuf out) {

            final int length = this.computeLength(value, 0xFFFF);
            out.writeShortLE(length);
            writeValue(out, value, length);
        }

        static void writeValue(final ByteBuf out, final Object value, final int length) {

            final int start = out.writerIndex();

            if (value instanceof String) {
                out.writeCharSequence((String)value, StandardCharsets.UTF_8);
            } else {
                out.writeBytes((byte[])value);
            }

            assert out.writerIndex() - start == length;
        }
    }

    private static class RntbdUnsignedInteger implements Codec {

        public static final Codec codec = new RntbdUnsignedInteger();

        private RntbdUnsignedInteger() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return Integer.BYTES;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number)value).longValue() & 0xFFFFFFFFL;
        }

        @Override
        public final Object defaultValue() {
            return 0L;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return in.readUnsignedIntLE();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Integer.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Long.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeIntLE(((Number)value).intValue());
        }
    }

    private static class RntbdUnsignedShort implements Codec {

        public static final Codec codec = new RntbdUnsignedShort();

        private RntbdUnsignedShort() {
        }

        @Override
        public final int computeLength(final Object value) {
            assert this.isValid(value);
            return Short.BYTES;
        }

        @Override
        public final Object convert(final Object value) {
            assert this.isValid(value);
            return ((Number)value).intValue() & 0xFFFF;
        }

        @Override
        public final Object defaultValue() {
            return 0;
        }

        @Override
        public final boolean isValid(final Object value) {
            return value instanceof Number;
        }

        @Override
        public final Object read(final ByteBuf in) {
            return in.readUnsignedShortLE();
        }

        @Override
        public final ByteBuf readSlice(final ByteBuf in) {
            return in.readSlice(Short.BYTES);
        }

        @Override
        public Class<?> valueType() {
            return Integer.class;
        }

        @Override
        public final void write(final Object value, final ByteBuf out) {
            assert this.isValid(value);
            out.writeShortLE(((Number)value).shortValue());
        }
    }

    // endregion
}
