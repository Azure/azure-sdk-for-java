// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri;

import java.util.Iterator;

/**
 * Simple implementation of "null iterator", iterator that has nothing to
 * iterate over.
 */
public final class EmptyIterator implements Iterator<Object> {
    final static Iterator<?> sInstance = new EmptyIterator();

    private EmptyIterator() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> getInstance() {
        return (Iterator<T>) sInstance;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        throw new java.util.NoSuchElementException();
    }

    @Override
    public void remove() {
        // The reason we do this is that we know for a fact that
        // it can not have been moved
        throw new IllegalStateException();
    }
}
