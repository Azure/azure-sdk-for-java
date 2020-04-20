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
 * {"type": "map", "values": "bytes"}
 */
public class AvroHeaderSchema extends AvroSchema<Object> {

    private final Consumer<AvroType> onParsingSchema;
    private final Consumer<byte[]> onSync;

    public AvroHeaderSchema(Consumer<AvroType> onParsingSchema, Consumer<byte[]> onSync,
        AvroParserState state, Consumer<Object> onResult) {
        super(state, onResult);
        this.onParsingSchema = onParsingSchema;
        this.onSync = onSync;
    }

    /**
     * Add a FixedSchema to read magic.
     */
    @Override
    public void add() {
        state.push(this);
        AvroFixedSchema fixedSchema = new AvroFixedSchema(
            AvroConstants.MAGIC_BYTES.length,
            this.state,
            this::validateMagic
        );
        fixedSchema.add();
    }

    /**
     * On reading magic, validate it, then read the metadata.
     * @param magic The magic bytes.
     */
    private void validateMagic(List<ByteBuffer> magic) {
        byte[] init = AvroUtils.getBytes(magic);
        if (Arrays.equals(init, AvroConstants.MAGIC_BYTES)) {
            AvroMapSchema metadataSchema = new AvroMapSchema(
                new AvroType.AvroPrimitiveType("string"),
                state,
                this::readMetadata);
            metadataSchema.add();
        } else {
            throw new IllegalArgumentException("Invalid Avro file.");
        }
    }

    /**
     * On reading metadata, parse the schema of the file and call the onParsingSchema handler, then read the sync
     * marker.
     * @param metadata The metadata
     * @see AvroType#getType(JsonNode)
     */
    private void readMetadata(Map<String, Object> metadata) {
        String schemaString = metadata.get(AvroConstants.SCHEMA_KEY).toString();
        JsonNode schemaJson = null;
        try {
            schemaJson = new ObjectMapper().readTree(schemaString);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
        AvroType fileSchema = AvroType.getType(schemaJson);
        this.onParsingSchema.accept(fileSchema);

        AvroFixedSchema fixedSchema = new AvroFixedSchema(
            AvroConstants.SYNC_MARKER_SIZE,
            state,
            this::onSyncMarker
        );
        fixedSchema.add();
    }

    /**
     * On reading the sync marker, call the onSync handler, then we're done.
     * @param buffers
     */
    private void onSyncMarker(List<ByteBuffer> buffers) {
        byte[] sync = AvroUtils.getBytes(buffers);
        this.onSync.accept(sync);
        this.done = true;
        this.result = null;
    }

    @Override
    public void progress() {
    }

    @Override
    public boolean canProgress() {
        return true;
    }
}
