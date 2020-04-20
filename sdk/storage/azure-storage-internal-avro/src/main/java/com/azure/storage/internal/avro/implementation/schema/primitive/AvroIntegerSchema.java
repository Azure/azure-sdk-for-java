package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.function.Consumer;

/**
 * int values are written using variable-length zig-zag coding.
 *
 * Since longs and ints share the same encoding, just add a LongSchema and convert the result into an Integer.
 */
public class AvroIntegerSchema extends AvroSchema<Integer> {

    public AvroIntegerSchema(AvroParserState state, Consumer<Integer> onResult){
        super(state, onResult);
    }

    /**
     * Read the long.
     */
    @Override
    public void add() {
        state.push(this);
        AvroLongSchema numSchema = new AvroLongSchema(
            state,
            this::onNum
        );
        numSchema.add();
    }

    /**
     * Once we read the Long, we can convert it to an Integer.
     * @param n The Long to convert.
     */
    private void onNum(Long n) {
        this.done = true;
        this.result = Math.toIntExact(n);
    }

    @Override
    public void progress() {
    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
