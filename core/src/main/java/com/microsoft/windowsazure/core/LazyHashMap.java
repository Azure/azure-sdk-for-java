/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LazyHashMap<K, V> extends HashMap<K, V> implements LazyCollection {
    private static final long serialVersionUID = 1L;
    
    private boolean initialized;
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public void clear() {
        initialized = true;
        super.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        initialized = true;
        return super.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        initialized = true;
        return super.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        initialized = true;
        return super.entrySet();
    }

    @Override
    public V get(Object key) {
        initialized = true;
        return super.get(key);
    }

    @Override
    public boolean isEmpty() {
        initialized = true;
        return super.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        initialized = true;
        return super.keySet();
    }

    @Override
    public V put(K key, V value) {
        initialized = true;
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        initialized = true;
        super.putAll(m);
    }

    @Override
    public V remove(Object key) {
        initialized = true;
        return super.remove(key);
    }

    @Override
    public int size() {
        initialized = true;
        return super.size();
    }

    @Override
    public Collection<V> values() {
        initialized = true;
        return super.values();
    }
}
