// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
 * A class that represents a push based AvroParser that can parse avro data from a reactive stream.
 *
 * The parser stores the {@link AvroParserState current state}, the sync marker (parsed from the header),
 * the file type (parsed from the header metadata), and the list of records collected so far.
 *
 * For each ByteBuffer that is emitted from the stream, we first add it to the state's cache, while we can make
 * progress in parsing schema on the stack, we continue to progress in parsing schemas on the stack. When a schema
 * identifies itself as done, we first pop the schema off the stack, then publish the result by calling the schema's
 * result handler. The result handler will publish it to the location identified by the parent of the schema, and the
 * parent could add another item to the stack or determine that it is done.
 *
 */
public class AvroParser {

    private AvroParserState state; /* State of the Avro Parser. */
    private byte[] syncMarker; /* Sync marker read in the header. */
    private AvroType objectType; /* Type of objects in this Avro file. */
    private List<Object> records; /* Holds records collected so far. */

    public AvroParser() {
        this.state = new AvroParserState();
        this.records = new ArrayList<>();

        /* TODO (gapra): Make this a record schema since it essentially does the same stuff. */
        /* Start off by adding the header schema to the stack so we can parse it. */
        AvroHeaderSchema headerSchema = new AvroHeaderSchema(
            objectType -> this.objectType = objectType, /* Handler to store the objectType. */
            syncMarker -> this.syncMarker = syncMarker, /* Handler to store the sync marker. */
            this.state,
            this::onHeader
        );
        headerSchema.add();
    }

    /**
     * Header handler.
     *
     * @param ignore null
     */
    private void onHeader(Object ignore) {
        /* On reading the header, read a block. */
        onBlock(ignore);
    }

    /**
     * Block handler.
     *
     * @param ignore null
     */
    private void onBlock(Object ignore) {
        /* On reading the block, read a header. */
        AvroBlockSchema blockSchema = new AvroBlockSchema(
            this.objectType,
            o -> this.records.add(o), /* Object result handler. */
            this.syncMarker,
            this.state,
            this::onBlock
        );
        blockSchema.add();
    }

    /**
     * Parses as many objects as possible in the buffer.
     *
     * Caches buffer remaining as part of state in case the entire buffer is not used.
     * @param buffer {@link ByteBuffer} that is part of an Avro file.
     * @return A reactive stream of Objects found in this
     */
    public Flux<Object> parse(ByteBuffer buffer) {
        /* Cache the buffer as part of state. */
        this.state.write(buffer);

        /* Keep progressing in parsing schemas while able to make progress. */
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

        /* Convert the records collected so far into a Flux. */
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
