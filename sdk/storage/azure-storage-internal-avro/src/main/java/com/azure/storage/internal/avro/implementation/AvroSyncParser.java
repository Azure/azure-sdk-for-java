// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import com.azure.storage.internal.avro.implementation.schema.AvroCompositeSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.AvroSimpleSchema;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AvroSyncParser {
    private final AvroParserState state = new AvroParserState();
    private final List<AvroObject> objects = new ArrayList<>();

    /**
     * Parses the ByteBuffer and returns an iterable of AvroObjects.
     * @param buffer ByteBuffer that is part of an Avro file.
     * @return An iterable of AvroObject.
     */
    public Iterable<AvroObject> parse(ByteBuffer buffer) {
        state.write(buffer);

        while (!state.isStackEmpty()) {
            AvroSchema schema = state.peekFromStack();
            if (isSimpleSchema(schema)) {
                processSimpleSchema((AvroSimpleSchema) schema);
            } else if (isCompositeSchema(schema)) {
                // For composite schemas, we just ensure they're processed correctly in their own implementation
                schema.pushToStack();
            }
        }

        // Return or process the parsed objects
        return objects;
    }

    private boolean isSimpleSchema(AvroSchema schema) {
        return schema instanceof AvroSimpleSchema;
    }

    private boolean isCompositeSchema(AvroSchema schema) {
        return schema instanceof AvroCompositeSchema;
    }

    private void processSimpleSchema(AvroSimpleSchema schema) {
        while (schema.canProgress()) {
            schema.progress();
            if (schema.isDone()) {
                state.popOffStack();
                schema.publishResult();
                break;
            }
        }
    }
}
