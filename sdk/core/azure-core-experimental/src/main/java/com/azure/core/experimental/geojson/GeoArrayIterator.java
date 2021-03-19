// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.NoSuchElementException;

final class GeoArrayIterator<T> implements Iterator<T> {
    private final ClientLogger logger = new ClientLogger(GeoArrayIterator.class);

    private final GeoArray<T> container;

    private transient int cursor;

    GeoArrayIterator(GeoArray<T> container) {
        this.container = container;
    }

    @Override
    public boolean hasNext() {
        return cursor < container.size();
    }

    @Override
    public T next() {
        try {
            int i = cursor;
            T value = container.get(i);
            cursor = i + 1;

            return value;
        } catch (IndexOutOfBoundsException ex) {
            throw logger.logExceptionAsError(new NoSuchElementException());
        }
    }
}
