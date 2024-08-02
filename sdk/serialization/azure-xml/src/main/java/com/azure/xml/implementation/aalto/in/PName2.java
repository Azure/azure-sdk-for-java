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
 * Specialized implementation of PName: can be used for short Strings
 * that consists of at most 8 bytes. In general this means ascii-only
 * names (but possibly qualified by a short prefix)
 *<p>
 * The reason for such specialized classes is mostly space efficiency;
 * and to a lesser degree performance. Both are achieved for short
 * Strings by avoiding another level of indirection (via quad arrays)
 */
public final class PName2 extends ByteBasedPName {
    final int mQuad1;
    final int mQuad2;

    public PName2(String pname, String prefix, String ln, int hash, int quad1, int quad2) {
        super(pname, prefix, ln, hash);
        mQuad1 = quad1;
        mQuad2 = quad2;
    }

    @Override
    public PName createBoundName(NsBinding nsb) {
        PName2 newName = new PName2(_prefixedName, _prefix, _localName, mHash, mQuad1, mQuad2);
        newName._namespaceBinding = nsb;
        return newName;
    }

    @Override
    public boolean equals(int quad1, int quad2) {
        return (quad1 == mQuad1) && (quad2 == mQuad2);
    }

    @Override
    public boolean equals(int[] quads, int qlen) {
        return (qlen == 2) && (quads[0] == mQuad1) && (quads[1] == mQuad2);
    }

    @Override
    public boolean hashEquals(int h, int quad1, int quad2) {
        return (h == mHash) && (quad1 == mQuad1) && (quad2 == mQuad2);
    }

    @Override
    public boolean hashEquals(int h, int[] quads, int qlen) {
        return (h == mHash) && (qlen == 2) && (quads[0] == mQuad1) && (quads[1] == mQuad2);
    }

    @Override
    public int getFirstQuad() {
        return mQuad1;
    }

    @Override
    public int getLastQuad() {
        return mQuad2;
    }

    @Override
    public int getQuad(int index) {
        return (index == 0) ? mQuad1 : mQuad2;
    }

    @Override
    public int sizeInQuads() {
        return 2;
    }
}
