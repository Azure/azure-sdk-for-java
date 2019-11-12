// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonAdapterTests {
    @Test
    public void emptyMap() throws IOException {
        final Map<String, String> map = new HashMap<>();
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithNullKey() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put(null, null);
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNullValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":null}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map = new HashMap<>();
        mapHolder.map.put("", "");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":\"\"}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNonEmptyValue() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put("", "test");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"\":\"test\"}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    private static class MapHolder {
        @JsonInclude(content = JsonInclude.Include.ALWAYS)
        private Map<String, String> map = new HashMap<>();

        public Map<String, String> map() {
            return map;
        }

        public void map(Map<String, String> map) {
            this.map = map;
        }
    }
}
