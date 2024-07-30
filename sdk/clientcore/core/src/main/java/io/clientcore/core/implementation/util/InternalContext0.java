// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.util;

/**
 * An {@link InternalContext} implementation that holds no data.
 */
final class InternalContext0 extends InternalContext {
    static final InternalContext INSTANCE = new InternalContext0();

    private InternalContext0() {
    }

    @Override
    public Object getKey() {
        return null;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public InternalContext put(Object key, Object value) {
        return new InternalContext1(key, value);
    }

    @Override
    Object getInternal(Object key) {
        return SENTINEL;
    }
}
