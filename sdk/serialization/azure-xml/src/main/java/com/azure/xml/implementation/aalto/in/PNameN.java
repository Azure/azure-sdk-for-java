// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.aalto.in;

/**
 * General implementation of PName to be used with longer names (ones
 * that consist of more than 8 bytes).
 *<p>
 * The reason for such specialized classes is mostly space efficiency;
 * and to a lesser degree performance. Both are achieved for short
 * Strings by avoiding another level of indirection (via quad arrays)
 */
public final class PNameN extends ByteBasedPName {
    final int[] mQuads;
    final int mQuadLen;

    PNameN(String pname, String prefix, String ln, int hash, int[] quads, int quadLen) {
        super(pname, prefix, ln, hash);
        mQuads = quads;
        mQuadLen = quadLen;
    }

    @Override
    public PName createBoundName(NsBinding nsb) {
        PNameN newName = new PNameN(_prefixedName, _prefix, _localName, mHash, mQuads, mQuadLen);
        newName._namespaceBinding = nsb;
        return newName;
    }

    @Override
    public boolean equals(int quad1, int quad2) {
        // Unlikely to match... but:
        if (mQuadLen < 3) {
            if (mQuadLen == 1) {
                return (mQuads[0] == quad1) && (quad2 == 0);
            }
            return (mQuads[0] == quad1) && (mQuads[1] == quad2);
        }
        return false;
    }

    @Override
    public boolean equals(int[] quads, int qlen) {
        if (qlen == mQuadLen) {
            for (int i = 0; i < qlen; ++i) {
                if (quads[i] != mQuads[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hashEquals(int h, int quad1, int quad2) {
        if ((h == mHash) && (mQuadLen < 3)) {
            if (mQuadLen == 1) {
                return (mQuads[0] == quad1) && (quad2 == 0);
            }
            return (mQuads[0] == quad1) && (mQuads[1] == quad2);
        }
        return false;
    }

    @Override
    public boolean hashEquals(int h, int[] quads, int qlen) {
        if ((h == mHash) && (qlen == mQuadLen)) {
            for (int i = 0; i < qlen; ++i) {
                if (quads[i] != mQuads[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getQuad(int index) {
        return (index < mQuadLen) ? mQuads[index] : 0;
    }

    @Override
    public int sizeInQuads() {
        return mQuadLen;
    }
}
