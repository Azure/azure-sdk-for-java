// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.sym;

/**
 * Specialized implementation of PName: can be used for short Strings
 * that consists of 5 to 8 bytes. Usually this means relatively short
 * ascii-only names.
 *<p>
 * The reason for such specialized classes is mostly space efficiency;
 * and to a lesser degree performance. Both are achieved for short
 * Strings by avoiding another level of indirection (via quad arrays)
 */
public final class Name2 extends Name
{
    private final int q1,  q2;

    Name2(String name, int hash, int quad1, int quad2) {
        super(name, hash);
        q1 = quad1;
        q2 = quad2;
    }

    @Override
    public boolean equals(int quad) { return false; }

    @Override
    public boolean equals(int quad1, int quad2) { return (quad1 == q1) && (quad2 == q2); }

    @Override public boolean equals(int quad1, int quad2, int q3) { return false; }
    
    @Override
    public boolean equals(int[] quads, int qlen) { return (qlen == 2 && quads[0] == q1 && quads[1] == q2); }
}
