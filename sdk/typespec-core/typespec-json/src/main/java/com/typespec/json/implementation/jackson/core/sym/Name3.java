// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.sym;

/**
 * Specialized implementation of PName: can be used for short Strings
 * that consists of 9 to 12 bytes. It's the longest special purpose
 * implementaion; longer ones are expressed using {@link NameN}.
 */
public final class Name3 extends Name
{
    private final int q1,  q2, q3;

    Name3(String name, int hash, int i1, int i2, int i3) {
        super(name, hash);
        q1 = i1;
        q2 = i2;
        q3 = i3;
    }

    // Implies quad length == 1, never matches
    @Override
    public boolean equals(int quad) { return false; }

    // Implies quad length == 2, never matches
    @Override
    public boolean equals(int quad1, int quad2) { return false; }

    @Override
    public boolean equals(int quad1, int quad2, int quad3) {
        return (q1 == quad1) && (q2 == quad2) && (q3 == quad3);
    }

    @Override
    public boolean equals(int[] quads, int qlen) {
        return (qlen == 3) && (quads[0] == q1) && (quads[1] == q2) && (quads[2] == q3);
    }
}
