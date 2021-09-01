// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class wraps a reactor context in the Azure-Core Context API, to avoid unnecessary copying
 */
public class ReactorToAzureContextWrapper extends Context {
    private static final ClientLogger LOGGER = new ClientLogger(ReactorToAzureContextWrapper.class);

    private final Map<String, String> contextAttributes;
    private final reactor.util.context.ContextView reactorContext;
    private final int size;

    /**
     * A wrapper class on top on reactor context, represent it as Azure Context type.
     *
     * @param reactorContext A reactor context object.
     */
    public ReactorToAzureContextWrapper(reactor.util.context.ContextView reactorContext) {
        this(null, reactorContext);
    }

    /**
     * A wrapper class on top on reactor context with additional attributes, represent it as Azure Context type.
     *
     * @param contextAttributes The attributes of context.
     * @param reactorContext A reactor context object.
     */
    public ReactorToAzureContextWrapper(Map<String, String> contextAttributes,
        reactor.util.context.ContextView reactorContext) {
        super();
        this.contextAttributes = contextAttributes;
        this.reactorContext = reactorContext;
        this.size = reactorContext.size() + (contextAttributes == null ? 0 : contextAttributes.size());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Optional<Object> getData(Object key) {
        if (key == null) {
            // going into the exception case
            return super.getData(null);
        }

        // Look through the reactor context first
        Optional<Object> value = reactorContext.getOrEmpty(key);
        if (value.isPresent()) {
            return value;
        }

        // then look through the map that we were given
        if (contextAttributes != null && contextAttributes.containsKey(key)) {
            return Optional.ofNullable(contextAttributes.get(key));
        }

        // otherwise defer up the context chain
        return super.getData(key);
    }

    @Override
    public Map<Object, Object> getValues() {
        return new AbstractMap<Object, Object>() {

            @Override
            public int size() {
                return size;
            }

            @Override
            public boolean isEmpty() {
                return size == 0;
            }

            @Override
            public boolean containsKey(Object key) {
                return (contextAttributes != null && contextAttributes.containsKey(key))
                           || reactorContext.hasKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
            }

            @Override
            public String get(final Object key) {
                if (reactorContext.hasKey(key)) {
                    return reactorContext.get(key);
                } else if (contextAttributes != null && contextAttributes.containsKey(key)) {
                    return contextAttributes.get(key);
                } else {
                    return null;
                }
            }

            @Override
            public String put(Object key, Object value) {
                throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
            }

            @Override
            public String remove(Object key) {
                throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
            }

            @Override
            public void putAll(Map<? extends Object, ? extends Object> m) {
                throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
            }

            @Override
            public void clear() {
                throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
            }

            @Override
            public Set<Entry<Object, Object>> entrySet() {
                return new AbstractSet<Entry<Object, Object>>() {
                    @Override
                    public Iterator<Entry<Object, Object>> iterator() {
                        return new Iterator<Entry<Object, Object>>() {
                            private final Iterator<Entry<Object, Object>> reactorIterator =
                                reactorContext.stream().iterator();

                            private final Iterator<Entry<String, String>> contextAttrsIterator =
                                contextAttributes == null
                                    ? Collections.emptyIterator()
                                    : contextAttributes.entrySet().iterator();

                            @Override
                            public boolean hasNext() {
                                return reactorIterator.hasNext() || contextAttrsIterator.hasNext();
                            }

                            @Override
                            @SuppressWarnings("unchecked")
                            public Entry<Object, Object> next() {
                                if (reactorIterator.hasNext()) {
                                    return reactorIterator.next();
                                }
                                return (Entry<Object, Object>) (Object) contextAttrsIterator.next();
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return size;
                    }
                };
            }
        };
    }
}
