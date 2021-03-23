// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of Iterator for GeoArray.
 *
 * @param <T> Type of the GeoArray.
 */
final class GeoArrayIterator<T> implements Iterator<T> {
    private final ClientLogger logger = new ClientLogger(GeoArrayIterator.class);

    private final GeoArray<T> container;

    private volatile AtomicInteger cursor;

    GeoArrayIterator(GeoArray<T> container) {
        this.container = container;
    }

    @Override
    public boolean hasNext() {
        return cursor.get() < container.size();
    }

    @Override
    public T next() {
        try {
            return container.get(cursor.getAndIncrement());
        } catch (IndexOutOfBoundsException ex) {
            throw logger.logExceptionAsError(new NoSuchElementException());
        }
    }
}
