package com.azure.storage.internal.avro.implementation.schema.complex;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fixed instances are encoded using the number of bytes declared in the schema.
 * Wait for the cache to fill up, then get the bytes.
 * Note: We return a List of ByteBuffer since the number of bytes requested can be long and a single ByteBuffer can
 * only hold Integer.MAX bytes.
 */
public class AvroFixedSchema extends AvroSchema<List<ByteBuffer>> {

    private final long size;

    public AvroFixedSchema(long size, AvroParserState state, Consumer<List<ByteBuffer>> onResult) {
        super(state, onResult);
        this.size = size;
    }

    @Override
    public void add() {
        this.state.push(this);
    }

    @Override
    public void progress() {
        List<ByteBuffer> result = this.state.consume(size);
        this.done = true;
        this.result = result;
    }

    @Override
    public boolean canProgress() {
        return this.state.contains(this.size);
    }

}
