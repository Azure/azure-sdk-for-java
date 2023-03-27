// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.messaging.webpubsub.client.models.EventHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class EventHandlerCollection {

    private final ConcurrentMap<String, List> collection = new ConcurrentHashMap<>();

    public <T> void addEventHandler(String type, EventHandler<T> eventHandler) {
        List<EventHandler<T>> listeners =
            collection.computeIfAbsent(type, k -> new CopyOnWriteArrayList<EventHandler<T>>());
        listeners.add(eventHandler);
    }

    public <T> void removeEventHandler(String type, EventHandler<T> eventHandler) {
        List<EventHandler<T>> listeners = collection.get(type);
        if (listeners != null) {
            listeners.remove(eventHandler);
        }
    }

    public <T> void fireEvent(String type, T event) {
        List<EventHandler<T>> listeners = collection.get(type);
        if (listeners != null) {
            for (EventHandler<T> listener : listeners) {
                listener.handle(event);
            }
        }
    }
}
