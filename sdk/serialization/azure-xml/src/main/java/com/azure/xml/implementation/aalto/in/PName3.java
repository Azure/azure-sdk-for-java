// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.in;

/**
 * Specialized implementation of PName: can be used for medium-sized Strings
 * that consists of at most 9-12 bytes. These are less frequent than
 * shorter variations, but still somewhat common.
 *<p>
 * The reason for such specialized classes is mostly space efficiency;
 * and to a lesser degree performance. Both are achieved for short
 * Strings by avoiding another level of indirection (via quad arrays)
 */
public final class PName3 extends ByteBasedPName {
    final int mQuad1;
    final int mQuad2;
    final int mQuad3;

    PName3(String pname, String prefix, String ln, int hash, int[] quads) {
        super(pname, prefix, ln, hash);
        mQuad1 = quads[0];
        mQuad2 = quads[1];
        mQuad3 = quads[2];
    }

    public PName3(String pname, String prefix, String ln, int hash, int q1, int q2, int q3) {
        super(pname, prefix, ln, hash);
        mQuad1 = q1;
        mQuad2 = q2;
        mQuad3 = q3;
    }

    @Override
    public PName createBoundName(NsBinding nsb) {
        PName3 newName = new PName3(_prefixedName, _prefix, _localName, mHash, mQuad1, mQuad2, mQuad3);
        newName._namespaceBinding = nsb;
        return newName;
    }

    @Override
    public boolean equals(int quad1, int quad2) {
        // Implies quad length < 3, never matches
        return false;
    }

    @Override
    public boolean equals(int[] quads, int qlen) {
        return (qlen == 3) && (quads[0] == mQuad1) && (quads[1] == mQuad2) && (quads[2] == mQuad3);
    }

    @Override
    public boolean hashEquals(int h, int quad1, int quad2) {
        // Implies quad length < 3, never matches
        return false;
    }

    @Override
    public boolean hashEquals(int h, int[] quads, int qlen) {
        return (h == mHash) && (qlen == 3) && (quads[0] == mQuad1) && (quads[1] == mQuad2) && (quads[2] == mQuad3);
    }

    @Override
    public int getQuad(int index) {
        if (index < 2) {
            return (index == 0) ? mQuad1 : mQuad2;
        }
        // Whatever would be returned for invalid index is arbitrary, so:
        return mQuad3;
    }

    @Override
    public int sizeInQuads() {
        return 3;
    }
}
