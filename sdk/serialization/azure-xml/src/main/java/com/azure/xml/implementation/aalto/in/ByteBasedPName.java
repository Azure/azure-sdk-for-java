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
 * This intermediate abstract class defines more specialized API needed
 * by components like symbol tables, which need to provide efficient
 * access to byte-based PNames. Byte-based names can be used to directly
 * convert byte sequences to actual character-based names, without
 * intervening byte-to-character decoding phase.
 */
public abstract class ByteBasedPName extends PName {
    /**
     * Since the hash is calculated different from the way eventual
     * String's hash will be (bit faster, not significantly worse
     * hashing uniformness), we need to store that hash here.
     */
    protected final int mHash;

    protected ByteBasedPName(String pname, String prefix, String ln, int hash) {
        super(pname, prefix, ln);
        mHash = hash;
    }

    /*
    /**********************************************************************
    /* Specialized comparators and accessors
    /**********************************************************************
     */

    // From base class:

    @Override
    public abstract int getQuad(int index);

    @Override
    public abstract int sizeInQuads();

    // additional ones:

    public abstract boolean equals(int quad1, int quad2);

    public abstract boolean equals(int[] quads, int qlen);

    public abstract boolean hashEquals(int h, int quad1, int quad2);

    public abstract boolean hashEquals(int h, int[] quads, int qlen);

    /*
    /**********************************************************************
    /* Overridden standard methods
    /**********************************************************************
     */

    /**
     * Whether we should use internal hash, or the hash of prefixed
     * name string itself is an open question. For now, let's use
     * former.
     */
    @Override
    public int hashCode() {
        return mHash;
    }
}
