// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Iso8601SerializerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    public static void setupClass() {
        MAPPER.registerModule(Iso8601DateSerializer.getModule());
    }

    @Test
    public void dateWithTimeZone() throws JsonProcessingException {
        SimpleDateFormat format = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date currentDate = new Date();
        String expectedDate = format.format(currentDate);
        String actualDate = MAPPER.writeValueAsString(currentDate);
        assertEquals(expectedDate, actualDate);
    }

    @Test
    public void dateWithoutTimeZone() throws JsonProcessingException {
        Date epochTime = Date.from(Instant.ofEpochSecond(new Date().toInstant().getEpochSecond()));
        Date expectedDate = new Date(epochTime.getYear(), epochTime.getMonth(), epochTime.getDate(),
            epochTime.getHours(), epochTime.getMinutes(), epochTime.getSeconds());
        SimpleDateFormat format = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String actualDate = MAPPER.writeValueAsString(epochTime);
        assertEquals(format.format(expectedDate), actualDate);
    }
}
