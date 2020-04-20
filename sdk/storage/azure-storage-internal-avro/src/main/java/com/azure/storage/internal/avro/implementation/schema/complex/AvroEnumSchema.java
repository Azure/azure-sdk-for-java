package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroIntegerSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.List;
import java.util.function.Consumer;

/**
 * An enum is encoded by a int, representing the zero-based position of the symbol in the schema.
 *
 * Add an IntegerSchema and convert the result into the Enum by indexing the values.
 */
public class AvroEnumSchema extends AvroSchema<String> {

    private final List<String> values;

    public AvroEnumSchema(List<String> symbols, AvroParserState state, Consumer<String> onResult) {
        super(state, onResult);
        this.values = symbols;
    }

    /**
     * Read the index.
     */
    @Override
    public void add() {
        this.state.push(this);
        AvroIntegerSchema indexSchema = new AvroIntegerSchema(
            this.state,
            this::onIndex
        );
        indexSchema.add();
    }

    /**
     * Once we read the index, we can figure out what the value is by indexing into the values, then we're done.
     * @param index The index.
     */
    private void onIndex(Integer index) {
        String result = this.values.get(index);
        this.done = true;
        this.result = result;
    }

    @Override
    public void progress() {
    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
