// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Simple read-only iterator that iterators over one specific item, passed
 * in as constructor argument.
 */
public final class SingletonIterator implements Iterator<String> {
    private final String _value;

    private boolean _done = false;

    public SingletonIterator(String value) {
        _value = value;
    }

    @Override
    public boolean hasNext() {
        return !_done;
    }

    @Override
    public String next() {
        if (_done) {
            throw new NoSuchElementException();
        }
        _done = true;
        return _value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
