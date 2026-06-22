// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util.arrow;

import com.google.flatbuffers.Table;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Accessor for the Arrow IPC {@code Message} table (root of every encapsulated IPC message).
 */
public final class Message extends Table {
    /**
     * Reads the {@code Message} located at the root offset of the supplied buffer.
     *
     * @param bb the little-endian buffer positioned at the start of the message.
     * @return the {@code Message} accessor.
     */
    public static Message getRootAsMessage(ByteBuffer bb) {
        return getRootAsMessage(bb, new Message());
    }

    /**
     * Reads the {@code Message} located at the root offset of the supplied buffer into {@code obj}.
     *
     * @param bb the buffer positioned at the start of the message.
     * @param obj the accessor instance to assign.
     * @return the assigned {@code Message} accessor.
     */
    public static Message getRootAsMessage(ByteBuffer bb, Message obj) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(bb.getInt(bb.position()) + bb.position(), bb);
    }

    /**
     * Positions this accessor at the given table offset.
     *
     * @param i the table offset.
     * @param bb the backing buffer.
     */
    public void __init(int i, ByteBuffer bb) {
        __reset(i, bb);
    }

    /**
     * Assigns this accessor to the given table offset.
     *
     * @param i the table offset.
     * @param bb the backing buffer.
     * @return this accessor.
     */
    public Message __assign(int i, ByteBuffer bb) {
        __init(i, bb);
        return this;
    }

    /**
     * Gets the discriminator identifying the type of the {@code header} union (see {@link MessageHeader}).
     *
     * @return the header union type, or {@code 0} when absent.
     */
    public byte headerType() {
        int o = __offset(6);
        return o != 0 ? bb.get(o + bb_pos) : 0;
    }

    /**
     * Resolves the {@code header} union value into the supplied accessor.
     *
     * @param obj the accessor to assign to the union value.
     * @return the assigned accessor, or {@code null} when the header is absent.
     */
    public Table header(Table obj) {
        int o = __offset(8);
        return o != 0 ? __union(obj, o + bb_pos) : null;
    }

    /**
     * Gets the length, in bytes, of the message body that follows the metadata.
     *
     * @return the body length, or {@code 0} when absent.
     */
    public long bodyLength() {
        int o = __offset(10);
        return o != 0 ? bb.getLong(o + bb_pos) : 0L;
    }
}

