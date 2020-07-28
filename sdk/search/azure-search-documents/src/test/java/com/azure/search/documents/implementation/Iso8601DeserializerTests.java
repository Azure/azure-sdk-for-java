// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.azure.search.documents.TestHelpers.assertDateEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Iso8601DeserializerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Date EXPECTED_EPOCH = Date.from(Instant.ofEpochSecond(1569407400));

    @BeforeAll
    public static void setupClass() {
        MAPPER.registerModule(Iso8601DateSerializer.getModule());
    }

    @Test
    public void deserializesRawStringAsOffsetDateTime() throws Exception {
        String input = "\"2019-09-25T10:30:00.000Z\"";

        Date expected = new Date(EXPECTED_EPOCH.getYear(), EXPECTED_EPOCH.getMonth(), EXPECTED_EPOCH.getDate(),
            EXPECTED_EPOCH.getHours(), EXPECTED_EPOCH.getMinutes(), EXPECTED_EPOCH.getSeconds());
        Date actual = MAPPER.readValue(input, Date.class);

        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    public void deserializesPropertyAsOffsetDateTime() throws Exception {
        String input = "{\"date\":\"2019-09-25T10:30:00.000Z\"}";
        Date expected = new Date(EXPECTED_EPOCH.getYear(), EXPECTED_EPOCH.getMonth(), EXPECTED_EPOCH.getDate(),
            EXPECTED_EPOCH.getHours(), EXPECTED_EPOCH.getMinutes(), EXPECTED_EPOCH.getSeconds());

        TypeReference<HashMap<String, Date>> typeRef = new TypeReference<HashMap<String, Date>>() { };

        Map<String, Date> obj = MAPPER.readValue(input, typeRef);
        Date actual = obj.get("date");

        assertEquals(0, expected.compareTo(actual));
    }

    @Test
    public void deserializesListOfOffsetDateTime() throws Exception {
        String input = "[\"2019-09-25T10:30:00.000Z\",\"2010-02-10T10:30:00.000Z\"]";
        Date expectedEpoch2 = Date.from(Instant.ofEpochSecond(1265797800));
        Date expected1 = new Date(EXPECTED_EPOCH.getYear(), EXPECTED_EPOCH.getMonth(), EXPECTED_EPOCH.getDate(),
            EXPECTED_EPOCH.getHours(), EXPECTED_EPOCH.getMinutes(), EXPECTED_EPOCH.getSeconds());

        Date expected2 = new Date(expectedEpoch2.getYear(), expectedEpoch2.getMonth(), expectedEpoch2.getDate(),
            expectedEpoch2.getHours(), expectedEpoch2.getMinutes(), expectedEpoch2.getSeconds());

        TypeReference<List<Date>> typeRef = new TypeReference<List<Date>>() { };
        List<Date> obj = MAPPER.readValue(input, typeRef);

        Date actual1 = obj.get(0);
        Date actual2 = obj.get(1);

        assertDateEquals(expected1, actual1);
        assertDateEquals(expected2, actual2);
    }

    @Test
    public void deserializesListOfPartialOffsetDateTime() throws JsonProcessingException {
        String input = "[\"2019-09-25T10:30:00.000Z\",\"Feb 02 2010 10:30:00.000\"]";

        Date expected1 = new Date(EXPECTED_EPOCH.getYear(), EXPECTED_EPOCH.getMonth(), EXPECTED_EPOCH.getDate(),
            EXPECTED_EPOCH.getHours(), EXPECTED_EPOCH.getMinutes(), EXPECTED_EPOCH.getSeconds());

        TypeReference<List<Object>> typeRef = new TypeReference<List<Object>>() { };
        List<Object> obj = MAPPER.readValue(input, typeRef);

        Object actual1 = obj.get(0);
        Object actual2 = obj.get(1);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertEquals(format.format(expected1), actual1);
        assertEquals(String.class, actual1.getClass());
        assertEquals(String.class, actual2.getClass());
    }
}
