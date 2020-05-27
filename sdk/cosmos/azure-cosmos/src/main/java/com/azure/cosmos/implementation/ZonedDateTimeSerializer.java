// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ZonedDateTimeSerializer extends StdSerializer<Instant> {

    private static final long serialVersionUID = 1477047422582342157L;
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss" + ".SSS").withLocale(Locale.US).withZone(ZoneOffset.UTC);

    public ZonedDateTimeSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant instant,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(formatDateTime(instant));
    }

    public static String formatDateTime(Instant dateTime) {
        if (dateTime == null) {
            return null;
        }
        return RESPONSE_TIME_FORMATTER.format(dateTime);
    }
}
