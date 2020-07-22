// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.serializer.gson.implementation;

import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;

/**
 * Utility type to configure JSON serialization behavior.
 */
public class SerializationUtil {
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    /**
     * Configures an {@link GsonBuilder} with custom behavior needed to work with the Azure Cognitive Search REST API.
     * @param builder The Gson builder.
     */
    public static GsonBuilder registerAdapter(GsonBuilder builder) {
        builder.setDateFormat(ISO_DATE_FORMAT);
        builder.registerTypeAdapter(LocalTime.class, new LocalTimeAdapter());
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        builder.registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter());
        builder.registerTypeAdapter(OffsetTime.class, new OffsetTimeAdapter());
        builder.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter());
        builder.registerTypeAdapter(Instant.class, new InstantAdapter());
        return builder;
    }
}
