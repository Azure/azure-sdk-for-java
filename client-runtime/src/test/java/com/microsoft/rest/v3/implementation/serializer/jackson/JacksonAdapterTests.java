package com.microsoft.rest.v3.implementation.serializer.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.rest.v3.implementation.serializer.SerializerEncoding;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JacksonAdapterTests {
    @Test
    public void emptyMap() throws IOException {
        final Map<String,String> map = new HashMap<>();
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithNullKey() {
        final Map<String,String> map = new HashMap<>();
        map.put(null, null);
        final JacksonAdapter serializer = new JacksonAdapter();
        try {
            serializer.serialize(map, SerializerEncoding.JSON);
            fail();
        }
        catch (Exception e) {
            assertEquals(JsonMappingException.class, e.getClass());
            assertTrue(e.getMessage().contains("Null key for a Map not allowed in JSON"));
        }
    }

    @Test
    public void mapWithEmptyKeyAndNullValue() throws IOException {
        final Map<String,String> map = new HashMap<>();
        map.put("", null);
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"\":null}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final Map<String,String> map = new HashMap<>();
        map.put("", "");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"\":\"\"}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNonEmptyValue() throws IOException {
        final Map<String,String> map = new HashMap<>();
        map.put("", "test");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"\":\"test\"}", serializer.serialize(map, SerializerEncoding.JSON));
    }
}
