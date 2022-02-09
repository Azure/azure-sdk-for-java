// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

/**
 * Custom serializer that serializes a list of items into line-delimited JSON format.
 */
public class NdJsonSerializer extends StdSerializer<List<?>> {

    /** NDJSON is JSON (non-pretty printed) with a new line delimiter after each line. */
    private static final String NEW_LINE_DELIMITER = System.lineSeparator();

    /** Classes serial ID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of the serializer.
     */
    public NdJsonSerializer() {
        super(List.class, false);
    }

    @Override
    public void serialize(final List<?> values, final JsonGenerator gen, final SerializerProvider provider)
        throws IOException {

        if (values == null) {
            return;
        }

        for (Object o : values) {
            gen.writeObject(o);
            gen.writeRawValue(NEW_LINE_DELIMITER);
        }
    }
}
