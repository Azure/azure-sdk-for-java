// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {

    private static final long serialVersionUID = 1477047422582342157L;
    private static final DateTimeFormatter RESPONSE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss" + ".SSS").withLocale(Locale.US);

    public ZonedDateTimeSerializer() {
        super(ZonedDateTime.class);
    }

    @Override
    public void serialize(ZonedDateTime zonedDateTime,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(formatDateTime(zonedDateTime));
    }

    public static String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(RESPONSE_TIME_FORMATTER);
    }
}
