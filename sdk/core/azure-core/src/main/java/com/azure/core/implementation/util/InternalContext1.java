// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import reactor.util.context.Context;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * An {@link InternalContext} that holds a single key-value pair.
 */
final class InternalContext1 extends InternalContext {
    final Object key;
    final Object value;

    InternalContext1(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public InternalContext put(Object key, Object value) {
        return new InternalContext2(this.key, this.value, key, value);
    }

    @Override
    Object getInternal(Object key) {
        return Objects.equals(this.key, key) ? value : SENTINEL;
    }

    @Override
    public void getValues(LinkedHashMap<Object, Object> map) {
        map.put(key, value);
    }

    @Override
    public Context putIntoReactorContext(Context reactorContext) {
        return reactorContext.putNonNull(key, value);
    }

    @Override
    public InternalContext merge(InternalContext other) {
        if (other == null || other.size() == 0) {
            return this;
        } else if (other.size() == 1) {
            return new InternalContext2(key, value, other.getKey(), other.getValue());
        } else if (other.size() == 2) {
            InternalContext2 other2 = (InternalContext2) other;
            return new InternalContext3(key, value, other2.key1, other2.value1, other2.key2, other2.value2);
        } else if (other.size() == 3) {
            InternalContext3 other3 = (InternalContext3) other;
            return new InternalContext4(key, value, other3.key1, other3.value1, other3.key2, other3.value2, other3.key3,
                other3.value3);
        } else {
            return new InternalContextN(this, other);
        }
    }
}
