// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class JsonNodeMap implements Map<String, Object> {
    private final static ObjectMapper itemMapper = Utils.getSimpleObjectMapper();

    private final Object thisLock = new Object();
    private final JsonNode jsonNode;
    private volatile Map<String, Object> jsonNodeAsMap = null;

    public JsonNodeMap(JsonNode jsonNode) {
        checkNotNull(jsonNode, "Argument 'jsonNode' must not be null.");

        this.jsonNode = jsonNode;
    }

    public JsonNode getJsonNode() {
        return this.jsonNode;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ensureJsonNodeAsMap() {
        if (this.jsonNodeAsMap != null) {
            return this.jsonNodeAsMap;
        }

        synchronized (this.thisLock) {
            if (this.jsonNodeAsMap != null) {
                return this.jsonNodeAsMap;
            }

            return this.jsonNodeAsMap = itemMapper.convertValue(this.jsonNode, Map.class);
        }
    }

    @Override
    public int size() {
        return this.ensureJsonNodeAsMap().size();
    }

    @Override
    public boolean isEmpty() {
        return this.ensureJsonNodeAsMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.ensureJsonNodeAsMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.ensureJsonNodeAsMap().containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return this.ensureJsonNodeAsMap().get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return this.ensureJsonNodeAsMap().put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return this.ensureJsonNodeAsMap().remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        this.ensureJsonNodeAsMap().putAll(m);
    }

    @Override
    public void clear() {
        this.ensureJsonNodeAsMap().clear();
    }

    @Override
    public Set<String> keySet() {
        return this.ensureJsonNodeAsMap().keySet();
    }

    @Override
    public Collection<Object> values() {
        return this.ensureJsonNodeAsMap().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.ensureJsonNodeAsMap().entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof JsonNodeMap)) {
            return false;
        }

        JsonNodeMap other = (JsonNodeMap)o;

        return this.jsonNode.equals(other.jsonNode);
    }

    @Override
    public int hashCode() {
        return this.jsonNode.hashCode();
    }
}
