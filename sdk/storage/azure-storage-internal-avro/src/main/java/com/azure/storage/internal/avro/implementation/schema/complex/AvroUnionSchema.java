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
 *
 * Integer TypeSchema
 */
public class AvroUnionSchema extends AvroSchema<Object> {

    private final List<AvroType> types;

    /**
     * Constructs a new AvroUnionSchema.
     *
     * @param types The types the schema could be.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroUnionSchema(List<AvroType> types, AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.types = types;
    }

    @Override
    public void add() {
        this.state.push(this);

        /* Read the index, call onIndex. */
        AvroIntegerSchema indexSchema = new AvroIntegerSchema(
            this.state,
            this::onIndex
        );
        indexSchema.add();
    }

    /**
     * Index handler.
     * Once we read the index, we can figure out what type to read by indexing into the values, then we can
     * add the appropriate schema.
     * @param index The index.
     */
    private void onIndex(Integer index) {
        if (index <= 0 || index >= this.types.size()) {
            throw new RuntimeException("Invalid index to parse union");
        }
        /* Using the zero-based index, get the appropriate type. */
        AvroType type = this.types.get(index);

        /* Read the type, call onType. */
        AvroSchema typeSchema = getSchema(
            type,
            this.state,
            this::onType
        );
        typeSchema.add();
    }

    /**
     * Type handler.
     *
     * @param value the value.
     */
    private void onType(Object value) {
        /* Store the value, then we're done. */
        this.result = value;
        this.done = true;
    }

    @Override
    public void progress() {
        /* Progress is defined by progress on the sub-type schemas. */
    }

    @Override
    public boolean canProgress() {
        /* Can always make progress since it is defined by the progress on the sub-type schemas. */
        return true;
    }
}
