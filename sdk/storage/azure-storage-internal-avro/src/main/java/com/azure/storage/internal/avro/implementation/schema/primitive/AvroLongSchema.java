package com.azure.storage.internal.avro.implementation.schema.primitive;

import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;

import java.util.function.Consumer;

/**
 * long values are written using variable-length zig-zag coding.
 *
 * The equivalent while loop version of the code would look like this.
 *
 * byte b = this.state.consume();
 * long n = b & 0x7F;
 * long shift = 7;
 * while ((b & 0x80) != 0) {
 *  b = this.state.consume();
 *  next |= (b & 0x7F) << shift;
 *  shift += 7;
 * }
 * long value = next;
 * return (n >> 1) ^ -(n & 1);
 */
public class AvroLongSchema extends AvroSchema<Long> {
    long n;
    long shift = 7;
    boolean first = true;
    int lastB;

    public AvroLongSchema(AvroParserState state, Consumer<Long> onResult){
        super(state, onResult);
    }

    @Override
    public void add() {
        state.push(this);
    }

    /**
     * Consuming bytes written into ParserState
     */
    @Override
    public void progress() {
        if(!first && (lastB & 0x80) == 0) {
            n = (n >> 1) ^ -(n & 1);
            this.done = true;
            this.result = n;
            return;
        }
        int b = this.state.consume() & 0xff;

        if (first) {
            n = b & 0x7F;
            first = false;
            lastB = b;
            if( (b & 0x80) != 0) {
                return;
            } else {
                n = (n >> 1) ^ -(n & 1);
                this.done = true;
                this.result = n;
            }
        } else {
            n |= (b & 0x7F) << shift;
            shift += 7;
            lastB = b;
        }
    }

    /* Can make progress. */
    @Override
    public boolean canProgress() {
        return this.state.contains(1L);
    }
}
