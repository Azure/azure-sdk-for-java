// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Iso8601DeserializerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    public static void setupClass() {
        SimpleModule module = new SimpleModule();
        UntypedObjectDeserializer defaultDeserializer = new UntypedObjectDeserializer(null, null);
        module.addDeserializer(Object.class, new Iso8601DateDeserializer(defaultDeserializer));
        MAPPER.registerModule(module);
    }

    @Test
    public void deserializesRawStringAsOffsetDateTime() throws Exception {
        String input = "\"2019-09-25T10:30:00.000Z\"";

        OffsetDateTime expected = OffsetDateTime.of(2019, 9, 25,
            10, 30, 0, 0, ZoneOffset.UTC);
        Object actual = MAPPER.readValue(input, Object.class);

        assertEquals(expected, actual);
    }

    @Test
    public void deserializesPropertyAsOffsetDateTime() throws Exception {
        String input = "{\"date\":\"2019-09-25T10:30:00.000Z\"}";

        OffsetDateTime expected = OffsetDateTime.of(2019, 9, 25,
            10, 30, 0, 0, ZoneOffset.UTC);
        Map<?, ?> obj = MAPPER.readValue(input, Map.class);
        Object actual = obj.get("date");

        assertEquals(expected, actual);
    }

    @Test
    public void deserializesListOfOffsetDateTime() throws Exception {
        String input = "[\"1975-04-04T10:30:00.000Z\",\"2010-02-10T10:30:00.000Z\"]";

        OffsetDateTime expected1 = OffsetDateTime.of(1975, 4, 4,
            10, 30, 0, 0, ZoneOffset.UTC);
        OffsetDateTime expected2 = OffsetDateTime.of(2010, 2, 10,
            10, 30, 0, 0, ZoneOffset.UTC);

        List<?> obj = MAPPER.readValue(input, List.class);

        Object actual1 = obj.get(0);
        Object actual2 = obj.get(1);

        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
    }

    @Test
    public void deserializesListOfPartialOffsetDateTime() throws Exception {
        String input = "[\"1975-04-04T10:30:00.000Z\",\"Feb 02 2010 10:30:00.000\"]";

        OffsetDateTime expected1 = OffsetDateTime.of(1975, 4, 4,
            10, 30, 0, 0, ZoneOffset.UTC);

        List<?> obj = MAPPER.readValue(input, List.class);

        Object actual1 = obj.get(0);
        Object actual2 = obj.get(1);

        assertEquals(expected1, actual1);
        assertEquals(String.class, actual2.getClass());
    }
}
