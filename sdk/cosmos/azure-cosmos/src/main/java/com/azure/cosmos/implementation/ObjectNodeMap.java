// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.MapType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class ObjectNodeMap implements Map<String, Object> {
    private final static ObjectMapper itemMapper = Utils.getSimpleObjectMapper();
    public final static MapType JACKSON_MAP_TYPE = itemMapper.getTypeFactory().constructMapType(LinkedHashMap.class,
        String.class, Object.class);
    private final Object thisLock = new Object();
    private final ObjectNode jsonNode;
    private volatile LinkedHashMap<String, Object> jsonNodeAsMap = null;

    public ObjectNodeMap(ObjectNode jsonNode) {
        checkNotNull(jsonNode, "Argument 'jsonNode' must not be null.");

        this.jsonNode = jsonNode;
    }

    public ObjectNode getObjectNode() {
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

            return this.jsonNodeAsMap = itemMapper.convertValue(this.jsonNode, JACKSON_MAP_TYPE);
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

        if (!(o instanceof ObjectNodeMap)) {
            return false;
        }

        ObjectNodeMap other = (ObjectNodeMap)o;

        return this.jsonNode.equals(other.jsonNode);
    }

    @Override
    public int hashCode() {
        return this.jsonNode.hashCode();
    }
}
