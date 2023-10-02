// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.sym;

/**
 * Specialized implementation of PName: can be used for short Strings
 * that consists of at most 4 bytes. Usually this means short
 * ascii-only names.
 *<p>
 * The reason for such specialized classes is mostly space efficiency;
 * and to a lesser degree performance. Both are achieved for short
 * Strings by avoiding another level of indirection (via quad arrays)
 */
public final class Name1 extends Name
{
    private final static Name1 EMPTY = new Name1("", 0, 0);
    private final int q;

    Name1(String name, int hash, int quad) {
        super(name, hash);
        q = quad;
    }

    public static Name1 getEmptyName() { return EMPTY; }

    @Override public boolean equals(int quad) { return (quad == q); }
    @Override public boolean equals(int quad1, int quad2) { return (quad1 == q) && (quad2 == 0); }
    @Override public boolean equals(int q1, int q2, int q3) { return false; }

    @Override public boolean equals(int[] quads, int qlen) { return (qlen == 1 && quads[0] == q); }
}
