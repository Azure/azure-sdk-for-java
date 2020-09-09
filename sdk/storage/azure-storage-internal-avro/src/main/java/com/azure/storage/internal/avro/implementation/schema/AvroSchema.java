// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema;

import com.azure.storage.internal.avro.implementation.AvroParser;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroArraySchema;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroEnumSchema;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroFixedSchema;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroMapSchema;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroRecordSchema;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroUnionSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroBooleanSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroBytesSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroDoubleSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroFloatSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroIntegerSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroLongSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroStringSchema;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.ARRAY;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.BOOLEAN;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.BYTES;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.DOUBLE;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.ENUM;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.FIXED;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.FLOAT;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.INT;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.LONG;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.MAP;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.NULL;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.RECORD;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.STRING;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.UNION;

/**
 * An abstract class that represents an Avro schema that can return an Object result.
 * AvroSchemas get placed on the AvroParserState stack and the AvroParser makes progress parsing the schemas on the
 * stack as it is able to.
 *
 * @see AvroParser#parse(ByteBuffer)
 */
public abstract class AvroSchema {

    protected final AvroParserState state;
    private final Consumer<Object> onResult;
    protected boolean done = false;
    protected Object result;

    /**
     * Constructs a new Schema.
     *
     * @param state    The state of the parser.
     * @param onResult The result handler.
     */
    public AvroSchema(AvroParserState state, Consumer<Object> onResult) {
        this.state = state;
        this.onResult = onResult;
    }

    /**
     * Adds a schema to the state's stack.
     * For complex types, this includes adding the initial child schema to make progress.
     */
    public abstract void pushToStack();

    /**
     * @return Whether or not the schema is done. Also indicates that the result is ready.
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     * Calls the result handler.
     */
    public void publishResult() {
        this.onResult.accept(result);
    }

    /**
     * Gets the schema associated with the type.
     *
     * @param type     The {@link AvroType type} that defines the schema.
     * @param state    {@link AvroParserState}
     * @param onResult {@link Consumer}
     * @return {@link AvroSchema}
     * @see AvroType
     */
    public static AvroSchema getSchema(AvroType type, AvroParserState state, Consumer<Object> onResult) {
        switch (type.getType()) {
            case NULL:
                return new AvroNullSchema(state, onResult);
            case BOOLEAN:
                return new AvroBooleanSchema(state, onResult);
            case INT:
                return new AvroIntegerSchema(state, onResult);
            case LONG:
                return new AvroLongSchema(state, onResult);
            case FLOAT:
                return new AvroFloatSchema(state, onResult);
            case DOUBLE:
                return new AvroDoubleSchema(state, onResult);
            case BYTES:
                return new AvroBytesSchema(state, onResult);
            case STRING:
                return new AvroStringSchema(state, onResult);
            case RECORD: {
                checkType("type", type, AvroType.AvroRecordType.class);
                AvroType.AvroRecordType recordType = (AvroType.AvroRecordType) type;
                return new AvroRecordSchema(recordType.getName(), recordType.getFields(), state, onResult);
            }
            case ENUM: {
                checkType("type", type, AvroType.AvroEnumType.class);
                AvroType.AvroEnumType enumType = (AvroType.AvroEnumType) type;
                return new AvroEnumSchema(enumType.getSymbols(), state, onResult);
            }
            case ARRAY: {
                checkType("type", type, AvroType.AvroArrayType.class);
                AvroType.AvroArrayType arrayType = (AvroType.AvroArrayType) type;
                return new AvroArraySchema(arrayType.getItemType(), state, onResult);
            }
            case MAP: {
                checkType("type", type, AvroType.AvroMapType.class);
                AvroType.AvroMapType mapType = (AvroType.AvroMapType) type;
                return new AvroMapSchema(mapType.getValueType(), state, onResult);
            }
            case UNION: {
                checkType("type", type, AvroType.AvroUnionType.class);
                AvroType.AvroUnionType unionType = (AvroType.AvroUnionType) type;
                return new AvroUnionSchema(unionType.getTypes(), state, onResult);
            }
            case FIXED: {
                checkType("type", type, AvroType.AvroFixedType.class);
                AvroType.AvroFixedType fixedType = (AvroType.AvroFixedType) type;
                return new AvroFixedSchema(fixedType.getSize(), state, onResult);
            }
            default:
                throw new RuntimeException("Unsupported type " + type.getType());
        }
    }

    /**
     * Checks if the object matches the expected type.
     *
     * @param name         The name of the variable.
     * @param obj          The object.
     * @param expectedType The expected type.
     */
    public static void checkType(String name, Object obj, Class<?> expectedType) {
        if (!expectedType.isAssignableFrom(obj.getClass())) {
            throw new IllegalStateException(String.format(
                "Expected '%s' to be of type %s", name, expectedType.getSimpleName()));
        }
    }

    /**
     * Converts a List of ByteBuffers into a byte array.
     *
     * @param bytes The buffers to convert.
     * @return The byte array.
     */
    public static byte[] getBytes(List<?> bytes) {
        long longTotalBytes = bytes
            .stream()
            .mapToLong(buffer -> {
                checkType("buffer", buffer, ByteBuffer.class);
                return ((ByteBuffer) buffer).remaining();
            })
            .sum();

        if (longTotalBytes > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Bytes can not fit into a single array.");
        }

        int totalBytes = Math.toIntExact(longTotalBytes);

        byte[] ret = new byte[totalBytes];
        AtomicInteger offset = new AtomicInteger();
        bytes.forEach(buffer -> {
            checkType("buffer", buffer, ByteBuffer.class);
            ByteBuffer b = (ByteBuffer) buffer;
            int length = b.remaining();
            b.get(ret, offset.get(), length);
            offset.addAndGet(length);
        });

        return ret;
    }
}
