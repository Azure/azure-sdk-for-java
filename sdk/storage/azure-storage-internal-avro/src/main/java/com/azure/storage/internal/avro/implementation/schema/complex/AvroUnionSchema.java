package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroIntegerSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;

import java.util.List;
import java.util.function.Consumer;

/**
 * A union is encoded by first writing a long value indicating the zero-based position within the union of the
 * schema of its value. The value is then encoded per the indicated schema within the union.
 *
 * Add an IntegerSchema and figure out what schema to read, then read that type.
 */
public class AvroUnionSchema extends AvroSchema<Object> {

    private final List<AvroType> types;

    public AvroUnionSchema(List<AvroType> types, AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.types = types;
    }

    /**
     * Read the index.
     */
    @Override
    public void add() {
        state.push(this);
        AvroIntegerSchema indexSchema = new AvroIntegerSchema(
            this.state,
            this::onIndex
        );
        indexSchema.add();
    }

    /**
     * Once we read the index, we can figure out what type to read by indexing into the values, then we can
     * add the appropriate schema.
     * @param index The index.
     */
    private void onIndex(Integer index) {
        AvroType type = this.types.get(index);
        AvroSchema typeSchema = getSchema(
            type,
            this.state,
            this::onType
        );
        typeSchema.add();
    }

    /**
     * Once we read the type, then we're done.
     * @param value the value.
     */
    private void onType(Object value) {
        this.result = value;
        this.done = true;
    }

    @Override
    public void progress() {

    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
