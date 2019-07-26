// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;


// TODO: commons-collections lib has CaseInsensitiveHashMap we should switch to that.
// https://commons.apache.org/proper/commons-collections/javadocs/api-3.2.2/org/apache/commons/collections/map/CaseInsensitiveMap.html
public class CaseInsensitiveHashMap<V> extends HashMap<String, V> {

    private static String safeToLower(String key) {
        return key != null ? key.toLowerCase() : null;
    }

    @Override
    public V get(Object key) {
        return super.get(safeToLower((String) key));
    }


    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        super.putAll(m);
    }

    @Override
    public V put(String key, V value) {
        return super.put(safeToLower(key), value);
    }

    @Override
    public V putIfAbsent(String key, V value) {
        return super.putIfAbsent(safeToLower(key), value);
    }

    @Override
    public V compute(String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
        return super.compute(safeToLower(key), remappingFunction);
    }

    @Override
    public V computeIfAbsent(String key, Function<? super String, ? extends V> mappingFunction) {
        return super.computeIfAbsent(safeToLower(key), mappingFunction);
    }

    @Override
    public V computeIfPresent(String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(safeToLower(key), remappingFunction);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(safeToLower((String) key));
    }
}
