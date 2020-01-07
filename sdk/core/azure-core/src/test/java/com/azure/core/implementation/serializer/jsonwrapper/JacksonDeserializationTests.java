// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer.jsonwrapper;

import com.azure.core.implementation.serializer.jsonwrapper.api.Config;
import com.azure.core.implementation.serializer.jsonwrapper.jacksonwrapper.JacksonDeserializer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JacksonDeserializationTests extends JsonDeserializationTests {

    @BeforeEach
    public void initialize() throws Exception {
        // createDeserializer
        jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        Assertions.assertNotNull(jsonApi);
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void deserializeDate() throws ParseException {
        String json = "{ \"date\" : \"1970-01-18T00:00:00Z\"}";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = dateFormat.parse("1970-01-18T00:00:00Z");

        jsonApi.configureTimezone();
        Entry entry = jsonApi.readString(json, Entry.class);
        Assertions.assertEquals(date, entry.date());
    }

    @Test
    public void convertMap2Object() {
        Map<String, Object> document = new HashMap<>();
        document.put("intValue", 1);
        document.put("stringValue", "one");

        Foo expected = new Foo();
        expected.setIntValue(1);
        expected.setStringValue("one");
        Foo actual = jsonApi.convertObjectToType(document, Foo.class);
        Assertions.assertEquals(expected, actual);
    }
}
