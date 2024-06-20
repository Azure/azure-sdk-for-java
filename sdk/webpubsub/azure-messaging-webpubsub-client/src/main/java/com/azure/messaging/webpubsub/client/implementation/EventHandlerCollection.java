// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class EventHandlerCollection {

    private final ConcurrentMap<String, List> collection = new ConcurrentHashMap<>();

    public <T> void addEventHandler(String type, Consumer<T> eventHandler) {
        List<Consumer<T>> listeners = collection.computeIfAbsent(type, k -> new CopyOnWriteArrayList<Consumer<T>>());
        listeners.add(eventHandler);
    }

    public <T> void removeEventHandler(String type, Consumer<T> eventHandler) {
        List<Consumer<T>> listeners = collection.get(type);
        if (listeners != null) {
            listeners.remove(eventHandler);
        }
    }

    public <T> void fireEvent(String type, T event) {
        List<Consumer<T>> listeners = collection.get(type);
        if (listeners != null) {
            for (Consumer<T> listener : listeners) {
                listener.accept(event);
            }
        }
    }
}
