// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.util;

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
}
