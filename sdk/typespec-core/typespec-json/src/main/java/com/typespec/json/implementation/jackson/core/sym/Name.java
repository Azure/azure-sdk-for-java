// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.sym;

/**
 * Base class for tokenized names (key strings in objects) that have
 * been tokenized from byte-based input sources (like
 * {@link java.io.InputStream}.
 *
 * @author Tatu Saloranta
 */
public abstract class Name
{
    protected final String _name;

    protected final int _hashCode;

    protected Name(String name, int hashCode) {
        _name = name;
        _hashCode = hashCode;
    }

    public String getName() { return _name; }

    /*
    /**********************************************************
    /* Methods for package/core parser
    /**********************************************************
     */

    public abstract boolean equals(int q1);

    public abstract boolean equals(int q1, int q2);

    public abstract boolean equals(int q1, int q2, int q3);

    public abstract boolean equals(int[] quads, int qlen);

    /*
    /**********************************************************
    /* Overridden standard methods
    /**********************************************************
     */

    @Override public String toString() { return _name; }

    @Override public final int hashCode() { return _hashCode; }

    @Override public boolean equals(Object o) {
        // Canonical instances, can usually just do identity comparison
        return (o == this);
    }
}
