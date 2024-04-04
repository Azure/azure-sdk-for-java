// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.util;

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
}
