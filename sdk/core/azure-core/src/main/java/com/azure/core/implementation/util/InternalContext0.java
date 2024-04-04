// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import reactor.util.context.Context;

import java.util.LinkedHashMap;

/**
 * An {@link InternalContext} implementation that holds no data.
 */
final class InternalContext0 implements InternalContext {
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
    public int count() {
        return 0;
    }

    @Override
    public InternalContext addData(Object key, Object value) {
        return new InternalContext1(key, value);
    }

    @Override
    public Object getData(Object key) {
        return SENTINEL;
    }

    @Override
    public void getValues(LinkedHashMap<Object, Object> map) {
    }

    @Override
    public Context putIntoReactorContext(Context reactorContext) {
        return reactorContext;
    }

    @Override
    public InternalContext merge(InternalContext other) {
        return (other == null) ? this : other;
    }
}
