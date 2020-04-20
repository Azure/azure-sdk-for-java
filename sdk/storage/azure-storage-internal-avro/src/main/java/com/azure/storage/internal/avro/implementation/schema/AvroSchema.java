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
 * An abstract class that represents an Avro schema that can return a generic result.
 * AvroSchemas get placed on the AvroParserState stack and the AvroParser makes progress parsing the schemas on the
 * stack as it is able to.
 *
 * @param <T> The result type of the schema.
 * @see AvroParser#parse(ByteBuffer)
 */
public abstract class AvroSchema<T> {

    protected final AvroParserState state;
    private final Consumer<T> onResult;
    protected boolean done = false;
    protected T result;

    /**
     * Constructs a new Schema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroSchema(AvroParserState state, Consumer<T> onResult) {
        this.state = state;
        this.onResult = onResult;
    }

    /**
     * Adds a schema to the state's stack.
     * For complex types, this includes adding the initial child schema to make progress.
     */
    public abstract void add();

    /**
     * Makes some progress in parsing the type.
     * For complex types, this is simply a no-op since primitive types do the job for them.
     */
    public abstract void progress();

    /**
     * @return Whether or not the schema is done. Also indicates that the result is ready.
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     * @return Whether or not progress can be made for this schema.
     * For complex types, this is always true since primitive types do the job for them.
     */
    public abstract boolean canProgress();

    /**
     * Calls the result handler.
     */
    public void publish() {
        this.onResult.accept(result);
    }

    /**
     * Gets the schema associated with the type.
     *
     * @param type The {@link AvroType type} that defines the schema.
     * @param state {@link AvroParserState}
     * @param onResult {@link Consumer}
     * @return {@link AvroSchema}
     */
    public static AvroSchema getSchema(AvroType type, AvroParserState state, Consumer onResult) {
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
                AvroType.AvroRecordType recordType = (AvroType.AvroRecordType) type;
                return new AvroRecordSchema(recordType.getName(), recordType.getFields(), state, onResult);
            }
            case ENUM: {
                AvroType.AvroEnumType enumType = (AvroType.AvroEnumType) type;
                return new AvroEnumSchema(enumType.getSymbols(), state, onResult);
            }
            case ARRAY: {
                AvroType.AvroArrayType arrayType = (AvroType.AvroArrayType) type;
                return new AvroArraySchema(arrayType.getItemType(), state, onResult);
            }
            case MAP: {
                AvroType.AvroMapType mapType = (AvroType.AvroMapType) type;
                return new AvroMapSchema(mapType.getValues(), state, onResult);
            }
            case UNION: {
                AvroType.AvroUnionType unionType = (AvroType.AvroUnionType) type;
                return new AvroUnionSchema(unionType.getTypes(), state, onResult);
            }
            case FIXED: {
                AvroType.AvroFixedType fixedType = (AvroType.AvroFixedType) type;
                return new AvroFixedSchema(fixedType.getSize(), state, onResult);
            }
            default:
                throw new RuntimeException("Unsupported type " + type.getType());
        }
    }

}
