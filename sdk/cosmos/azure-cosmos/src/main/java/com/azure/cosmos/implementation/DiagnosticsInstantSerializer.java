// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Provides a serialization for instant using ISO-8601 representation.
 * e.g., such as '2011-12-03T10:15:30Z'.
 *
 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_INSTANT
 */
public class DiagnosticsInstantSerializer extends StdSerializer<Instant> {
    private static final long serialVersionUID = 1477047422582342157L;

    public DiagnosticsInstantSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant instant,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(fromInstant(instant));
    }

    public static String fromInstant(Instant instant) {
        if (instant == null) {
            return null;
        }

        return instant.toString();
    }
}
