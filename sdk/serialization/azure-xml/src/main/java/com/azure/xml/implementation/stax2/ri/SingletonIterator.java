// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.ri;

import java.util.Iterator;

/**
 * Simple read-only iterator that iterators over one specific item, passed
 * in as constructor argument.
 */
public class SingletonIterator<T> implements Iterator<T> {
    private final T _value;

    private boolean _done = false;

    @Deprecated // since 4.0; use SingletonIterator.create() instead
    public SingletonIterator(T value) {
        _value = value;
    }

    public static <T> SingletonIterator<T> create(T value) {
        return new SingletonIterator<>(value);
    }

    @Override
    public boolean hasNext() {
        return !_done;
    }

    @Override
    public T next() {
        if (_done) {
            throw new java.util.NoSuchElementException();
        }
        _done = true;
        return _value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Can not remove item from SingletonIterator.");
    }
}
