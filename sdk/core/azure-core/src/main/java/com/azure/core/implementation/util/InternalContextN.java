// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.CoreUtils;
import reactor.util.context.Context;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * An {@link InternalContext} implementation that holds N key-value pairs.
 */
final class InternalContextN implements InternalContext {
    private static final Object SENTINEL = new Object();

    private final InternalContext[] contexts;
    private final int count;

    private final Object key;
    private final Object value;

    InternalContextN(InternalContext... contexts) {
        this.contexts = contexts;

        int count = 0;
        for (InternalContext context : contexts) {
            count += context.count();
        }
        this.count = count;

        this.key = contexts[contexts.length - 1].getKey();
        this.value = contexts[contexts.length - 1].getValue();
    }

    private InternalContextN(int count, Object key, Object value, InternalContext[] contexts) {
        this.count = count;
        this.key = key;
        this.value = value;
        this.contexts = contexts;
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
    public int count() {
        return count;
    }

    @Override
    public InternalContext addData(Object key, Object value) {
        InternalContext last = contexts[contexts.length - 1];

        if (last.count() < 4) {
            // The last context can hold more data. Add the data to the last context.
            InternalContext[] newContexts = CoreUtils.clone(contexts);
            newContexts[contexts.length - 1] = last.addData(key, value);

            return new InternalContextN(count + 1, key, value, newContexts);
        } else {
            // The last context is full. Create a new InternalContext1 and add it to the array.
            InternalContext[] newContexts = new InternalContext[contexts.length + 1];
            System.arraycopy(contexts, 0, newContexts, 0, contexts.length);
            newContexts[contexts.length] = new InternalContext1(key, value);

            return new InternalContextN(count + 1, key, value, newContexts);
        }
    }

    @Override
    public Object getData(Object key) {
        Object data = SENTINEL;

        // Iterate in reverse order to get the most recent data first.
        for (int i = contexts.length - 1; i >= 0; i--) {
            data = contexts[i].getData(key);
            if (!Objects.equals(SENTINEL, data)) {
                return data;
            }
        }

        return null;
    }

    @Override
    public void getValues(LinkedHashMap<Object, Object> map) {
        for (InternalContext context : contexts) {
            context.getValues(map);
        }
    }

    @Override
    public Context putIntoReactorContext(Context reactorContext) {
        for (InternalContext context : contexts) {
            reactorContext = context.putIntoReactorContext(reactorContext);
        }

        return reactorContext;
    }

    @Override
    public InternalContext merge(InternalContext other) {
        if (other == null || other.count() == 0) {
            return this;
        }

        InternalContext last = contexts[contexts.length - 1];
        if (last.count() + other.count() <= 4) {
            // The other context can be merged into the last context without creating a new InternalContextN.
            // Merge the last context with the other context to reduce the number of array that have to be allocated.
            InternalContext[] newContexts = CoreUtils.clone(contexts);
            newContexts[contexts.length - 1] = last.merge(other);

            return new InternalContextN(count + other.count(), other.getKey(), other.getValue(), newContexts);
        } else {
            // The last context can't fit the other context. Create a new InternalContextN with a larger backing array.
            InternalContext[] newContexts = new InternalContext[contexts.length + 1];
            System.arraycopy(contexts, 0, newContexts, 0, contexts.length);
            newContexts[contexts.length] = other;

            return new InternalContextN(count + other.count(), other.getKey(), other.getValue(), newContexts);
        }
    }
}
