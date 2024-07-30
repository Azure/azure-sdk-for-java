// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * An {@link InternalContext} implementation that holds N key-value pairs.
 */
final class InternalContextN extends InternalContext {
    private final InternalContext[] contexts;
    private final int count;

    private final Object key;
    private final Object value;

    InternalContextN(InternalContext... contexts) {
        this.contexts = contexts;

        int count = 0;
        for (InternalContext context : contexts) {
            count += context.size();
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
    public int size() {
        return count;
    }

    @Override
    public InternalContext put(Object key, Object value) {
        InternalContext last = contexts[contexts.length - 1];

        if (last.size() < 4) {
            // The last context can hold more data. Add the data to the last context.
            InternalContext[] newContexts = Arrays.copyOf(contexts, contexts.length);
            newContexts[contexts.length - 1] = last.put(key, value);

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
    Object getInternal(Object key) {
        Object data = SENTINEL;

        // Iterate in reverse order to get the most recent data first.
        for (int i = contexts.length - 1; i >= 0; i--) {
            data = contexts[i].getInternal(key);
            if (!Objects.equals(SENTINEL, data)) {
                return data;
            }
        }

        return data;
    }
}
