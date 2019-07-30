// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdUUID {

    public static final UUID EMPTY = new UUID(0L, 0L);

    private RntbdUUID() {
    }

    /**
     * Decode a {@link UUID} as serialized by Microsoft APIs like {@code System.Guid.ToByteArray}
     *
     * @param bytes a {@link byte} array containing the serialized {@link UUID} to be decoded
     * @return a new {@link UUID}
     */
    public static UUID decode(final byte[] bytes) {
        return decode(Unpooled.wrappedBuffer(bytes));
    }

    /**
     * Decode a {@link UUID} as serialized by Microsoft APIs like {@code System.Guid.ToByteArray}
     *
     * @param in a {@link ByteBuf} containing the serialized {@link UUID} to be decoded
     * @return a new {@link UUID}
     */
    public static UUID decode(final ByteBuf in) {

        checkNotNull(in, "in");

        if (in.readableBytes() < 2 * Long.BYTES) {
            final String reason = String.format("invalid frame length: %d", in.readableBytes());
            throw new CorruptedFrameException(reason);
        }

        long mostSignificantBits = in.readUnsignedIntLE() << 32;

        mostSignificantBits |= (0x000000000000FFFFL & in.readShortLE()) << 16;
        mostSignificantBits |= (0x000000000000FFFFL & in.readShortLE());

        long leastSignificantBits = (0x000000000000FFFFL & in.readShortLE()) << (32 + 16);

        for (int shift = 32 + 8; shift >= 0; shift -= 8) {
            leastSignificantBits |= (0x00000000000000FFL & in.readByte()) << shift;
        }

        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Encodes a {@link UUID} as serialized by Microsoft APIs like {@code System.Guid.ToByteArray}
     *
     * @param uuid a {@link UUID} to be encoded
     * @return a new byte array containing the encoded
     */
    public static byte[] encode(final UUID uuid) {
        final byte[] bytes = new byte[2 * Integer.BYTES];
        encode(uuid, Unpooled.wrappedBuffer(bytes));
        return bytes;
    }

    /**
     * Encodes a {@link UUID} as serialized by Microsoft APIs like {@code System.Guid.ToByteArray}
     *
     * @param uuid a {@link UUID} to be encoded
     * @param out  an output {@link ByteBuf}
     */
    public static void encode(final UUID uuid, final ByteBuf out) {

        final long mostSignificantBits = uuid.getMostSignificantBits();

        out.writeIntLE((int)((mostSignificantBits & 0xFFFFFFFF00000000L) >>> 32));
        out.writeShortLE((short)((mostSignificantBits & 0x00000000FFFF0000L) >>> 16));
        out.writeShortLE((short)(mostSignificantBits & 0x000000000000FFFFL));

        final long leastSignificantBits = uuid.getLeastSignificantBits();

        out.writeShortLE((short)((leastSignificantBits & 0xFFFF000000000000L) >>> (32 + 16)));
        out.writeShort((short)((leastSignificantBits & 0x0000FFFF00000000L) >>> 32));
        out.writeInt((int)(leastSignificantBits & 0x00000000FFFFFFFFL));
    }
}
