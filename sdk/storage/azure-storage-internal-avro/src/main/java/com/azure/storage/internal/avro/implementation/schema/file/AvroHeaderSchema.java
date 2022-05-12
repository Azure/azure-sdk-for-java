// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema.file;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A file header consists of:
 * Four bytes, ASCII 'O', 'b', 'j', followed by 1.
 * file metadata, including the schema.
 * The 16-byte, randomly-generated sync marker for this file.
 *
 * File metadata is written as if defined by the following map schema:
 * {"type": "map", "values": "string"}
 *
 * Magic MapStringString SyncMarker
 */
public class AvroHeaderSchema extends AvroCompositeSchema {

    private final ClientLogger logger = new ClientLogger(AvroHeaderSchema.class);

    private static final String HEADER_SCHEMA =
        "{\"type\": \"record\", \"name\": \"org.apache.avro.file.Header\",\n"
            + " \"fields\" : [\n"
            + "   {\"name\": \"magic\", \"type\": {\"type\": \"fixed\", \"name\": \"Magic\", \"size\": 4}},\n"
            + "   {\"name\": \"meta\", \"type\": {\"type\": \"map\", \"values\": \"string\"}},\n"
            + "   {\"name\": \"sync\", \"type\": {\"type\": \"fixed\", \"name\": \"Sync\", \"size\": 16}}\n"
            + "  ]\n"
            + "}";

    /**
     * Constructs a new AvroHeaderSchema.
     *
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroHeaderSchema(AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
    }

    @Override
    public void pushToStack() {
        this.state.pushToStack(this);
        AvroType headerType = AvroType.getType(HEADER_SCHEMA);
        AvroSchema headerSchema = AvroSchema.getSchema(
            headerType,
            this.state,
            this::onHeader
        );
        headerSchema.pushToStack();
    }

    /**
     * Header handler.
     *
     * @param header The header.
     */
    private void onHeader(Object header) {
        checkType("header", header, Map.class);
        Map<?, ?> h = (Map<?, ?>) header;

        /* Validate the magic bytes. */
        validateMagic(h.get(AvroConstants.MAGIC));

        Map<String, Object> filteredHeader = new LinkedHashMap<>();
        /* Validate the meta, and parse the type from the meta. */
        filteredHeader.put(AvroConstants.META, parseMeta(h.get(AvroConstants.META)));
        /* Pass the sync marker on forward. */
        filteredHeader.put(AvroConstants.SYNC, convertSync(h.get(AvroConstants.SYNC)));

        this.result = filteredHeader;
        this.done = true;
    }

    /**
     * Validates the magic bytes.
     *
     * @param magic The magic bytes.
     */
    private void validateMagic(Object magic) {
        checkType("magic", magic, List.class);
        /* Validate the magic bytes. */
        byte[] init = AvroSchema.getBytes((List<?>) magic);
        for (int i = 0; i < AvroConstants.MAGIC_BYTES.size(); i++) {
            if (!AvroConstants.MAGIC_BYTES.get(i).equals(init[i])) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Invalid Avro file."));
            }
        }
    }

    /**
     * Parses the metadata.
     */
    private AvroType parseMeta(Object meta) {
        checkType("meta", meta, Map.class);
        Map<?, ?> m = (Map<?, ?>) meta;
        /* We do not support codec. */
        Object codecString = m.get(AvroConstants.CODEC_KEY);
        if (!(codecString == null || codecString.equals(AvroConstants.NULL_CODEC))) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Codec is not supported"));
        }
        /* Get the schema and parse it, call the onParsingSchema handler. */
        String schemaString = m.get(AvroConstants.SCHEMA_KEY).toString();
        return AvroType.getType(schemaString);
    }

    /**
     * Converts the sync marker.
     */
    private byte[] convertSync(Object sync) {
        checkType("sync", sync, List.class);
        return AvroSchema.getBytes((List<?>) sync);
    }
}
