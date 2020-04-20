package com.azure.storage.internal.avro.implementation;

import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.file.AvroBlockSchema;
import com.azure.storage.internal.avro.implementation.schema.file.AvroHeaderSchema;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a push based AvroParser that can parse from a reactive stream.
 *
 */
public class AvroParser {

    private AvroParserState state; /* State of the Avro Parser. */
    private byte[] syncMarker; /* Sync marker read in the header. */
    private AvroType fileType; /* Type of objects in this Avro file.  */
    private List<Object> records; /* Holds records collected so far. */

    public AvroParser() {
        this.state = new AvroParserState();
        this.records = new ArrayList<>(); /* Based off the file schema. */
        /* TODO (gapra): Make this a record schema since it essentially does the same stuff. */
        AvroHeaderSchema headerSchema = new AvroHeaderSchema(
            schema -> this.fileType = schema,
            syncMarker -> this.syncMarker = syncMarker,
            this.state,
            this::onHeader
        );
        headerSchema.add();
    }

    private void onHeader(Object ignore) {
        onBlock(ignore);
    }

    private void onBlock(Object ignore) {
        AvroBlockSchema blockSchema = new AvroBlockSchema(
            this.fileType,
            o -> this.records.add(o),
            this.syncMarker,
            this.state,
            this::onBlock
        );
        blockSchema.add();
    }

    /**
     * Parses as many objects as possible in the buffer.
     * Caches buffer remaining as part of state in case the entire buffer is not used.
     * @param buffer {@link ByteBuffer} that is part of an Avro file.
     * @return A reactive stream of Objects found in this
     */
    public Flux<Object> parse(ByteBuffer buffer) {
        /* Cache the buffer as part of state. */
        this.state.write(buffer);

        /* Keep consuming schemas while able to. */
        AvroSchema schema = this.state.peek();
        while (schema.canProgress()) {
            schema.progress();
            /* If schema is done, pop yourself off the stack and publish.
               It is important to publish after popping yourself off because the parent may need to complete/add
               other schemas as needed. */
            if (schema.isDone()) {
                this.state.pop(); /* */
                schema.publish();
            }
            schema = this.state.peek();
        }

        Flux<Object> result;
        if (this.records.isEmpty()) {
            result = Flux.empty();
        } else {
            result = Flux.fromIterable(this.records);
            this.records = new ArrayList<>();
        }

        return result;
    }

}
