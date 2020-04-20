package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.function.Consumer;

/**
 * A null is written as zero bytes.
 */
public class AvroNullSchema extends AvroSchema<AvroNullSchema.Null> {

    public AvroNullSchema(AvroParserState state, Consumer<Null> onResult) {
        super(state, onResult);
    }

    @Override
    public void add() {
        this.state.push(this);
    }

    @Override
    public void progress() {
        Null result = new Null();
        done = true;
        this.result = result;
    }

    @Override
    public boolean canProgress() {
        return true;
    }

    /* We use a custom type to return null since null cannot be emitted in a Flux.
       Users of the AvroParser must transform all NullSchema.Null objects to null if necessary. */
    public static class Null {
    }

}
