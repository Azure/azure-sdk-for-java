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
 *
 * Integer
 */
public class AvroEnumSchema extends AvroSchema<String> {

    private final List<String> values;

    /**
     * Constructs a new AvroEnumSchema.
     *
     * @param symbols The enum symbols.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroEnumSchema(List<String> symbols, AvroParserState state, Consumer<String> onResult) {
        super(state, onResult);
        this.values = symbols;
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
     *
     * @param index The index.
     */
    private void onIndex(Integer index) {
        if (index <= 0 || index >= this.values.size()) {
            throw new RuntimeException("Invalid index to parse enum");
        }
        /* Using the zero-based index, get the appropriate symbol, then we're done. */
        this.result = this.values.get(index);
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
