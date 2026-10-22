// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;

import java.nio.ByteBuffer;

/**
 * Checks encoded value boundaries that Proton's decoder does not enforce, without interpreting message values.
 */
final class AmqpMessageValidator {
    private static final ClientLogger LOGGER = new ClientLogger(AmqpMessageValidator.class);
    // A section value is at depth 1, so 33 allows 32 nested levels. Described wrappers count as levels, so this is as
    // strict as Proton's default decode limit of 32 and sometimes stricter, and it applies on every Proton version.
    private static final int MAX_DEPTH = 33;
    private int remainingEntries = 1_000_000;

    private AmqpMessageValidator() {
    }

    static void validate(byte[] message) {
        AmqpMessageValidator validator = new AmqpMessageValidator();
        ByteBuffer buffer = ByteBuffer.wrap(message);
        while (buffer.hasRemaining()) {
            validator.readValue(buffer, 0);
        }
    }

    private void readValue(ByteBuffer buffer, int depth) {
        readValue(buffer, Byte.toUnsignedInt(buffer.get()), depth);
    }

    private void readValue(ByteBuffer buffer, int type, int depth) {
        if (depth > MAX_DEPTH) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("AMQP values cannot be nested more than 32 levels."));
        }
        if (type == 0) {
            readValue(buffer, depth + 1);
            readValue(buffer, depth + 1);
            return;
        }

        final int category = type >>> 4;
        if (category >= 4 && category <= 9) {
            skip(buffer, category == 4 ? 0 : 1 << (category - 5));
        } else if (category == 10 || category == 11) {
            skip(buffer, readSize(buffer, category == 10));
        } else if (type == 0xc0 || type == 0xc1 || type == 0xd0 || type == 0xd1) {
            readContainer(buffer, type, depth, false);
        } else if (type == 0xe0 || type == 0xf0) {
            readContainer(buffer, type, depth, true);
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid AMQP type code."));
        }
    }

    private void readContainer(ByteBuffer buffer, int type, int depth, boolean array) {
        final boolean small = (type & 0x10) == 0;
        final int size = readSize(buffer, small);
        if (size > buffer.remaining()) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("AMQP container size exceeds the available bytes."));
        }
        final ByteBuffer contents = buffer.slice();
        contents.limit(size);
        final int count = readSize(contents, small);
        // Arrays of constant encodings can declare billions of entries in a few bytes.
        if (count > remainingEntries) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("AMQP messages cannot contain more than 1,000,000 container entries."));
        }
        remainingEntries -= count;
        if (!array && (type & 1) != 0 && (count & 1) != 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("AMQP maps must contain key/value pairs."));
        }
        if (array) {
            int constructor = Byte.toUnsignedInt(contents.get());
            int constructorDepth = depth + 1;
            while (constructor == 0) {
                if (++constructorDepth > MAX_DEPTH) {
                    throw LOGGER.logExceptionAsError(
                        new IllegalArgumentException("AMQP values cannot be nested more than 32 levels."));
                }
                readValue(contents, constructorDepth);
                constructor = Byte.toUnsignedInt(contents.get());
            }
            for (int i = 0; i < count; i++) {
                readValue(contents, constructor, constructorDepth);
            }
        } else {
            // Each list or map entry must contain at least a type constructor.
            if (count > contents.remaining()) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("AMQP container count exceeds the available bytes."));
            }
            for (int i = 0; i < count; i++) {
                readValue(contents, depth + 1);
            }
        }
        if (contents.hasRemaining()) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("AMQP container size does not match its contents."));
        }
        skip(buffer, size);
    }

    private static int readSize(ByteBuffer buffer, boolean small) {
        int size = small ? Byte.toUnsignedInt(buffer.get()) : buffer.getInt();
        if (size < 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("AMQP value size or count exceeds the supported range."));
        }
        return size;
    }

    private static void skip(ByteBuffer buffer, int size) {
        if (size > buffer.remaining()) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("AMQP value size exceeds the available bytes."));
        }
        buffer.position(buffer.position() + size);
    }

}
