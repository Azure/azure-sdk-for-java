// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import reactor.util.context.Context;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * An {@link InternalContext} implementation that holds three key-value pairs.
 */
final class InternalContext3 implements InternalContext {
    final Object key1;
    final Object value1;
    final Object key2;
    final Object value2;
    final Object key3;
    final Object value3;

    InternalContext3(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3) {
        this.key1 = key1;
        this.value1 = value1;
        this.key2 = key2;
        this.value2 = value2;
        this.key3 = key3;
        this.value3 = value3;
    }

    @Override
    public Object getKey() {
        return key3;
    }

    @Override
    public Object getValue() {
        return value3;
    }

    @Override
    public int count() {
        return 3;
    }

    @Override
    public InternalContext addData(Object key, Object value) {
        return new InternalContext4(key1, value1, key2, value2, key3, value3, key, value);
    }

    @Override
    public Object getData(Object key) {
        if (Objects.equals(key, key3)) {
            return value3;
        } else if (Objects.equals(key, key2)) {
            return value2;
        } else if (Objects.equals(key, key1)) {
            return value1;
        }

        return null;
    }

    @Override
    public void getValues(LinkedHashMap<Object, Object> map) {
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
    }

    @Override
    public Context putIntoReactorContext(Context reactorContext) {
        return reactorContext.putNonNull(key1, value1).putNonNull(key2, value2).putNonNull(key3, value3);
    }

    @Override
    public InternalContext merge(InternalContext other) {
        if (other == null || other.count() == 0) {
            return this;
        } else if (other.count() == 1) {
            return new InternalContext4(key1, value1, key2, value2, key3, value3, other.getKey(), other.getValue());
        } else {
            return new InternalContextN(this, other);
        }
    }
}
