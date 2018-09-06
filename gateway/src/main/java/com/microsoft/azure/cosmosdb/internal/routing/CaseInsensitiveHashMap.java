/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.routing;

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
