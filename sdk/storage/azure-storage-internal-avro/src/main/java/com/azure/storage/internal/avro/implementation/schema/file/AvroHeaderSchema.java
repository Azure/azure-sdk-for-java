package com.azure.storage.internal.avro.implementation.schema.file;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroParserState;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroType;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroFixedSchema;
import com.azure.storage.internal.avro.implementation.schema.complex.AvroMapSchema;
import com.azure.storage.internal.avro.implementation.util.AvroUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.util.Arrays;
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
public class AvroHeaderSchema extends AvroSchema<Object> {

    private final Consumer<AvroType> onParsingSchema;
    private final Consumer<byte[]> onSync;

    /**
     * Constructs a new AvroHeaderSchema.
     *
     * @param onParsingSchema The handler to store the objectType in the AvroParser
     * @param onSync The handler to store the sync marker in the AvroParser.
     * @param state The state of the parser.
     * @param onResult The result handler.
     */
    public AvroHeaderSchema(Consumer<AvroType> onParsingSchema, Consumer<byte[]> onSync,
        AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.onParsingSchema = onParsingSchema;
        this.onSync = onSync;
    }

    @Override
    public void add() {
        this.state.push(this);

        /* Read the magic bytes, call validateMagic. */
        AvroFixedSchema fixedSchema = new AvroFixedSchema(
            AvroConstants.MAGIC_BYTES.length,
            this.state,
            this::validateMagic
        );
        fixedSchema.add();
    }

    /**
     * Magic handler
     *
     * @param magic The magic bytes.
     */
    private void validateMagic(List<ByteBuffer> magic) {
        /* Validate the magic bytes. */
        byte[] init = AvroUtils.getBytes(magic);
        if (Arrays.equals(init, AvroConstants.MAGIC_BYTES)) {
            /* Read the metadata, then call onMetadata. */
            AvroMapSchema metadataSchema = new AvroMapSchema(
                new AvroType.AvroPrimitiveType("string"),
                this.state,
                this::onMetadata
            );
            metadataSchema.add();
        } else {
            throw new IllegalArgumentException("Invalid Avro file.");
        }
    }

    /**
     * Metadata handler.
     *
     * @param metadata The metadata
     * @see AvroType#getType(JsonNode)
     */
    private void onMetadata(Map<String, Object> metadata) {
        /* We do not support codec. */
        Object codecString = metadata.get(AvroConstants.CODEC_KEY);
        if (codecString != null && codecString.equals(AvroConstants.DEFLATE_CODEC)) {
            throw new RuntimeException("Deflate codec is not supported");
        }
        /* Get the schema and parse it, call the onParsingSchema handler. */
        String schemaString = metadata.get(AvroConstants.SCHEMA_KEY).toString();
        JsonNode schemaJson;
        try {
            schemaJson = new ObjectMapper().readTree(schemaString);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
        AvroType objectType = AvroType.getType(schemaJson);
        this.onParsingSchema.accept(objectType);

        /* Read the file sync marker, call onSyncMarker. */
        AvroFixedSchema fixedSchema = new AvroFixedSchema(
            AvroConstants.SYNC_MARKER_SIZE,
            this.state,
            this::onSyncMarker
        );
        fixedSchema.add();
    }

    /**
     * Sync maker handler.
     *
     * @param buffers The buffers that consist of the sync marker.
     */
    private void onSyncMarker(List<ByteBuffer> buffers) {
        /* Call the sync marker handler, then we're done. */
        byte[] sync = AvroUtils.getBytes(buffers);
        this.onSync.accept(sync);
        this.done = true;
        this.result = null;
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
