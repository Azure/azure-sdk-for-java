// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FastBeanConstructor extends StdValueInstantiator {
    private final Supplier<?> optimizedDefaultConstructor;
    private final Function<Object[], Object> optimizedArgsConstructor;

    FastBeanConstructor(StdValueInstantiator delegate, Supplier<?> optimizedDefaultConstructor,
        Function<Object[], Object> optimizedArgsConstructor) {
        super(delegate);

        this.optimizedDefaultConstructor = optimizedDefaultConstructor;
        this.optimizedArgsConstructor = optimizedArgsConstructor;
    }

    @Override
    public boolean canCreateUsingDefault() {
        return optimizedDefaultConstructor != null || super.canCreateUsingDefault();
    }

    @Override
    public boolean canCreateFromObjectWith() {
        return optimizedArgsConstructor != null || super.canCreateFromObjectWith();
    }

    @Override
    public Object createUsingDefault(DeserializationContext ctxt) throws IOException {
        if (optimizedDefaultConstructor != null) {
            return optimizedDefaultConstructor.get();
        } else {
            return super.createUsingDefault(ctxt);
        }
    }

    @Override
    public Object createFromObjectWith(DeserializationContext ctxt, Object[] args) throws IOException {
        if (optimizedArgsConstructor != null) {
            return optimizedArgsConstructor.apply(args);
        } else {
            return super.createFromObjectWith(ctxt, args);
        }
    }
}
