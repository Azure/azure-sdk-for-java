// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PrimitiveJsonNodeMap implements Map<String, Object> {
    public final static String VALUE_KEY = "__primitive_json-node_value__";
    private final Object thisLock = new Object();
    private final JsonNode primitiveJsonNode;
    private volatile LinkedHashMap<String, Object> jsonNodeAsMap = null;

    public PrimitiveJsonNodeMap(JsonNode jsonNode) {
        checkNotNull(jsonNode, "Argument 'jsonNode' must not be null.");
        checkArgument(
            !jsonNode.isObject(),
            "Argument 'jsonNode' should not be an object - for objects use ObjectNodeMap.");

        this.primitiveJsonNode = jsonNode;
    }

    public JsonNode getPrimitiveJsonNode() {
        return this.primitiveJsonNode;
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

            this.jsonNodeAsMap = new LinkedHashMap<>();
            this.jsonNodeAsMap.put(VALUE_KEY, this.primitiveJsonNode);

            return this.jsonNodeAsMap;
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

        if (!(o instanceof PrimitiveJsonNodeMap)) {
            return false;
        }

        PrimitiveJsonNodeMap other = (PrimitiveJsonNodeMap) o;

        return this.primitiveJsonNode.equals(other.primitiveJsonNode);
    }

    @Override
    public int hashCode() {
        return this.primitiveJsonNode.hashCode();
    }
}
