// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.sym;

import java.util.Arrays;

/**
 * Generic implementation of PName used for "long" names, where long
 * means that its byte (UTF-8) representation is 13 bytes or more.
 */
@SuppressWarnings("fallthrough")
public final class NameN extends Name
{
    private final int q1, q2, q3, q4; // first four quads
    private final int qlen; // total number of quads (4 + q.length)
    private final int[] q;

    NameN(String name, int hash, int q1, int q2, int q3, int q4,
            int[] quads, int quadLen) {
        super(name, hash);
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
        q = quads;
        qlen = quadLen;
    }

    public static NameN construct(String name, int hash, int[] q, int qlen)
    {
        /* We have specialized implementations for shorter
         * names, so let's not allow runt instances here
         */
        if (qlen < 4) {
            throw new IllegalArgumentException();
        }
        int q1 = q[0];
        int q2 = q[1];
        int q3 = q[2];
        int q4 = q[3];

        int rem = qlen - 4;

        int[] buf;

        if (rem > 0) {
            buf = Arrays.copyOfRange(q, 4, qlen);
        } else {
            buf = null;
        }
        return new NameN(name, hash, q1, q2, q3, q4, buf, qlen);

    }

    // Implies quad length == 1, never matches
    @Override
    public boolean equals(int quad) { return false; }

    // Implies quad length == 2, never matches
    @Override
    public boolean equals(int quad1, int quad2) { return false; }

    // Implies quad length == 3, never matches
    @Override
    public boolean equals(int quad1, int quad2, int quad3) { return false; }

    @Override
    public boolean equals(int[] quads, int len) {
        if (len != qlen) { return false; }

        // Will always have >= 4 quads, can unroll
        if (quads[0] != q1) return false;
        if (quads[1] != q2) return false;
        if (quads[2] != q3) return false;
        if (quads[3] != q4) return false;

        switch (len) {
        default:
            return _equals2(quads);
        case 8:
            if (quads[7] != q[3]) return false;
        case 7:
            if (quads[6] != q[2]) return false;
        case 6:
            if (quads[5] != q[1]) return false;
        case 5:
            if (quads[4] != q[0]) return false;
        case 4:
        }
        return true;
    }

    private final boolean _equals2(int[] quads)
    {
        final int end = qlen-4;
        for (int i = 0; i < end; ++i) {
            if (quads[i+4] != q[i]) {
                return false;
            }
        }
        return true;
    }
}
