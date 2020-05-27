// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSimpleSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.file.AvroBlockSchema;
import com.azure.storage.internal.avro.implementation.schema.file.AvroHeaderSchema;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class that represents a push based AvroParser that can parse avro data from a reactive stream.
 *
 * The parser stores the {@link AvroParserState current state}, the sync marker (parsed from the header),
 * the file type (parsed from the header metadata), and the list of records collected so far.
 *
 * The {@link AvroParser#parse(ByteBuffer)} method accepts ByteBuffers as they are emitted from the stream.
 *
 * Header Block Block Block ....
 */
public class AvroParser {

    private final ClientLogger logger = new ClientLogger(AvroParser.class);

    /* State of the Avro Parser. */
    private AvroParserState state;

    /* The sync marker is read in the HeaderSchema.
       At the end of a BlockSchema, a sync marker is read and validated against this sync marker
       to enable detection of corrupt blocks. */
    private byte[] syncMarker;

    /* The type of objects in this Avro file is read and parsed from the metadata in the header.
       A BlockSchema uses this type to determine what type of Objects to read in the block. */
    private AvroType objectType;

    /* Holds records collected so far. */
    private List<Object> records;

    public AvroParser() {
        this.state = new AvroParserState();
        this.records = new ArrayList<>();

        /* Start off by adding the header schema to the stack so we can parse it. */
        AvroHeaderSchema headerSchema = new AvroHeaderSchema(
            this.state,
            this::onFilteredHeader
        );
        headerSchema.pushToStack();
    }

    /**
     * Header handler.
     *
     * @param header null
     */
    private void onFilteredHeader(Object header) {
        AvroSchema.checkType("header", header, Map.class);
        Map<?, ?> h = (Map<?, ?>) header;

        /* Store the type described by the metadata. */
        Object type = h.get(AvroConstants.META);
        AvroSchema.checkType("type", type, AvroType.class);
        this.objectType = (AvroType) type;

        /* Store the sync marker. */
        Object sync = h.get(AvroConstants.SYNC);
        AvroSchema.checkType("sync", sync, byte[].class);
        this.syncMarker = (byte[]) sync;

        /* On reading the header, read a block. */
        onBlock(null);
    }

    /**
     * Block handler.
     *
     * @param ignore null
     */
    private void onBlock(Object ignore) {
        /* On reading the block, read another block. */
        AvroBlockSchema blockSchema = new AvroBlockSchema(
            this.objectType,
            o -> this.records.add(o), /* Object result handler. */
            this.syncMarker,
            this.state,
            this::onBlock
        );
        blockSchema.pushToStack();
    }

    /**
     * Parses as many objects as possible in the buffer.
     *
     * @param buffer {@link ByteBuffer} that is part of an Avro file.
     * @return A reactive stream of Objects found in this buffer.
     */
    public Flux<Object> parse(ByteBuffer buffer) {
        /* Write a deep-copied buffer to the state's cache.
           As needed, bytes will be consumed from the cache by schemas.
           The bytes of a schema could be spread across any number of ByteBuffers in the stream, so we cache
           buffers until we are able to satisfy the schema at the top of the state's stack. */
        ByteBuffer allocatedBuffer = ByteBuffer.allocate(buffer.remaining());
        allocatedBuffer.put(buffer);
        allocatedBuffer.position(0);

        this.state.write(allocatedBuffer);

        /* The state also contains a stack of schemas required to parse the next object.
           The stack helps keep track of the schema currently being parsed and the parent schema that the schema is
           being parsed for. The respective schema files handle the logic of adding children schemas
           to the stack as necessary. */

        /* Keep progressing in parsing schemas while able to make progress. */
        AvroSchema schema = this.state.peekFromStack();
        while ((schema instanceof AvroCompositeSchema)
            || ((schema instanceof AvroSimpleSchema) && ((AvroSimpleSchema) schema).canProgress())) {
            if (schema instanceof AvroSimpleSchema) {
                ((AvroSimpleSchema) schema).progress();
            }
            /* If schema is done, pop yourself off the stack and publish.
               It is important to publish after popping yourself off because the parent may need to complete/add
               other schemas as needed. */
            if (schema.isDone()) {
                this.state.popOffStack();
                schema.publishResult();
            } else {
                if (schema instanceof AvroCompositeSchema) {
                    throw logger.logExceptionAsError(new IllegalStateException("Expected composite type to be done."));
                }
            }
            schema = this.state.peekFromStack();
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
