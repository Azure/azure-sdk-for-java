package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.function.Consumer;

/**
 *  boolean is written as a single byte whose value is either 0 (false) or 1 (true).
 */
public class AvroBooleanSchema extends AvroSchema<Boolean> {

    public AvroBooleanSchema(AvroParserState state, Consumer<Boolean> onResult) {
        super(state, onResult);
    }

    @Override
    public void add() {
        this.state.push(this);
    }

    @Override
    public void progress() {
        byte b = this.state.consume();
        if (b == (byte) 0) {
            this.result = false;
        } else if (b == (byte) 1) {
            this.result = true;
        } else {
            throw new IllegalStateException(String.format("Boolean value expected, instead got %b", b));
        }
        this.done = true;
    }

    @Override
    public boolean canProgress() {
        return this.state.contains(AvroConstants.BOOL_SIZE);
    }
}
