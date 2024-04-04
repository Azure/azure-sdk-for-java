// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import reactor.util.context.Context;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * An {@link InternalContext} implementation that holds four key-value pairs.
 */
final class InternalContext4 extends InternalContext {
    final Object key1;
    final Object value1;
    final Object key2;
    final Object value2;
    final Object key3;
    final Object value3;
    final Object key4;
    final Object value4;

    InternalContext4(Object key1, Object value1, Object key2, Object value2, Object key3, Object value3, Object key4,
        Object value4) {
        this.key1 = key1;
        this.value1 = value1;
        this.key2 = key2;
        this.value2 = value2;
        this.key3 = key3;
        this.value3 = value3;
        this.key4 = key4;
        this.value4 = value4;
    }

    @Override
    public Object getKey() {
        return key4;
    }

    @Override
    public Object getValue() {
        return value4;
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public InternalContext put(Object key, Object value) {
        return new InternalContextN(this, new InternalContext1(key, value));
    }

    @Override
    Object getInternal(Object key) {
        if (Objects.equals(key, key4)) {
            return value4;
        } else if (Objects.equals(key, key3)) {
            return value3;
        } else if (Objects.equals(key, key2)) {
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
        map.put(key3, value3);
        map.put(key4, value4);
    }

    @Override
    public Context putIntoReactorContext(Context reactorContext) {
        return reactorContext.putNonNull(key1, value1)
            .putNonNull(key2, value2)
            .putNonNull(key3, value3)
            .putNonNull(key4, value4);
    }

    @Override
    public InternalContext merge(InternalContext other) {
        if (other == null || other.size() == 0) {
            return this;
        } else {
            return new InternalContextN(this, other);
        }
    }
}
