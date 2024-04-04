// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import reactor.util.context.Context;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * An {@link InternalContext} implementation that holds two key-value pairs.
 */
final class InternalContext2 extends InternalContext {
    final Object key1;
    final Object value1;
    final Object key2;
    final Object value2;

    InternalContext2(Object key1, Object value1, Object key2, Object value2) {
        this.key1 = key1;
        this.value1 = value1;
        this.key2 = key2;
        this.value2 = value2;
    }

    @Override
    public Object getKey() {
        return key2;
    }

    @Override
    public Object getValue() {
        return value2;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public InternalContext put(Object key, Object value) {
        return new InternalContext3(key1, value1, key2, value2, key, value);
    }

    @Override
    Object getInternal(Object key) {
        if (Objects.equals(key, key2)) {
            return value2;
        } else if (Objects.equals(key, key1)) {
            return value1;
        }

        return SENTINEL;
    }

    @Override
    public void getValues(LinkedHashMap<Object, Object> map) {
        map.put(key1, value1);
        map.put(key2, value2);
    }

    @Override
    public Context putIntoReactorContext(Context reactorContext) {
        return reactorContext.putNonNull(key1, value1).putNonNull(key2, value2);
    }

    @Override
    public InternalContext merge(InternalContext other) {
        if (other == null || other.size() == 0) {
            return this;
        } else if (other.size() == 1) {
            return new InternalContext3(key1, value1, key2, value2, other.getKey(), other.getValue());
        } else if (other.size() == 2) {
            InternalContext2 other2 = (InternalContext2) other;
            return new InternalContext4(key1, value1, key2, value2, other2.key1, other2.value1, other2.key2,
                other2.value2);
        } else {
            return new InternalContextN(this, other);
        }
    }
}
