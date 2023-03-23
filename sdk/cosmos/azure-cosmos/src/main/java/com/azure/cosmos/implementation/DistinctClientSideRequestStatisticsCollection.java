// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DistinctClientSideRequestStatisticsCollection implements Queue<ClientSideRequestStatistics> {

    private final Queue<ClientSideRequestStatistics> queue = new ConcurrentLinkedQueue<>();
    private final Set<ClientSideRequestStatistics> set = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized int size() {
        return set.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public synchronized Iterator<ClientSideRequestStatistics> iterator() {
        return queue.iterator();
    }

    @Override
    public synchronized Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    @Override
    public synchronized boolean add(ClientSideRequestStatistics clientSideRequestStatistics) {
        if (clientSideRequestStatistics == null) {
            return true;
        }

        boolean isNew = set.add(clientSideRequestStatistics);

        if (isNew) {
            return queue.add(clientSideRequestStatistics);
        }
        return false;
    }

    @Override
    public synchronized boolean remove(Object o) {
        if (o == null) {
            return true;
        }

        if (set.remove((o))) {
            return queue.remove(o);
        }

        return false;
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        if (c == null || c.isEmpty()) {
            return true;
        }

        return set.containsAll(c);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends ClientSideRequestStatistics> c) {
        if (c == null || c.isEmpty()) {
            return true;
        }

        set.addAll(c);
        return queue.addAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        if (c == null || c.isEmpty()) {
            return true;
        }

        set.removeAll(c);
        return queue.removeAll(c);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        if (c == null || c.isEmpty()) {
            return true;
        }

        set.retainAll(c);
        return queue.retainAll(c);
    }

    @Override
    public synchronized void clear() {
        set.clear();
        queue.clear();
    }

    @Override
    public synchronized boolean offer(ClientSideRequestStatistics clientSideRequestStatistics) {
        if (clientSideRequestStatistics == null) {
            return true;
        }

        boolean isAccepted = set.add(clientSideRequestStatistics);
        if (isAccepted) {
            return queue.offer(clientSideRequestStatistics);
        }

        return false;
    }

    @Override
    public synchronized ClientSideRequestStatistics remove() {
        ClientSideRequestStatistics candidate = queue.remove();
        set.remove(candidate);
        return candidate;
    }

    @Override
    public synchronized ClientSideRequestStatistics poll() {
        ClientSideRequestStatistics candidate = queue.poll();
        if (candidate != null) {
            set.remove(candidate);
        }
        return candidate;
    }

    @Override
    public ClientSideRequestStatistics element() {
        return queue.element();
    }

    @Override
    public ClientSideRequestStatistics peek() {
        return queue.peek();
    }
}
