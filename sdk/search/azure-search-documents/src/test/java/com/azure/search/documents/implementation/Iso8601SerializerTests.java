// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Iso8601SerializerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SERIALIZER_FORMAT = "\"%s\"";

    @BeforeAll
    public static void setupClass() {
        MAPPER.registerModule(Iso8601DateSerializer.getModule());
    }

    @Test
    public void serializeDateWithTimeZoneDateFormat() throws JsonProcessingException {
        Date currentDate = new Date();
        String expectedDate = currentDate.toInstant().atOffset(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String actualDate = MAPPER.writeValueAsString(currentDate);
        assertEquals(String.format(SERIALIZER_FORMAT, expectedDate), actualDate);
    }

    @Test
    public void serializeDate() throws JsonProcessingException {
        Date epochTime = Date.from(Instant.ofEpochSecond(new Date().toInstant().getEpochSecond()));
        Date expectedDate = new Date(epochTime.getYear(), epochTime.getMonth(), epochTime.getDate(),
            epochTime.getHours(), epochTime.getMinutes(), epochTime.getSeconds());
        String expectedDateString = expectedDate.toInstant().atOffset(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String actualDate = MAPPER.writeValueAsString(epochTime);
        assertEquals(String.format(SERIALIZER_FORMAT, expectedDateString), actualDate);
    }
}
