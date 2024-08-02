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

import com.azure.xml.implementation.aalto.impl.ErrorConsts;

/**
 * An alternate implementation of PName: instead of coming straight from
 * byte contents, it is actually just built from a character array.
 *<p>
 * Note: one unfortunate result of this being a somewhat different PName
 * is that equality comparison between this and other implementations will not
 * work as expected. As such, these should only be used as temporary names.
 */
public final class PNameC extends PName {
    /**
     * Since the hash may be calculated different from the way eventual
     * String's hash will be (right now it is not), we better store
     * "our" hash here.
     */
    protected final int mHash;

    public PNameC(String pname, String prefix, String ln, int hash) {
        super(pname, prefix, ln);
        mHash = hash;
    }

    @Override
    public PName createBoundName(NsBinding nsb) {
        PNameC newName = new PNameC(_prefixedName, _prefix, _localName, mHash);
        newName._namespaceBinding = nsb;
        return newName;
    }

    public static PNameC construct(String pname) {
        return construct(pname, calcHash(pname));
    }

    public static PNameC construct(String pname, int hash) {
        int colonIx = pname.indexOf(':');
        if (colonIx < 0) {
            return new PNameC(pname, null, pname, hash);
        }
        /* !!! TODO: cache prefix intern() calls, since they are bound
         * to cluster nicely (and quite often within same thread too)
         */
        return new PNameC(pname, pname.substring(0, colonIx).intern(), pname.substring(colonIx + 1).intern(), hash);
    }

    /*
    //////////////////////////////////////////////////////////
    // Sub-class API
    //////////////////////////////////////////////////////////
     */

    public boolean equalsPName(char[] buffer, int start, int len, int hash) {
        if (hash != mHash) {
            return false;
        }
        String pname = _prefixedName;
        int plen = pname.length();
        if (len != plen) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            if (buffer[start + i] != pname.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public int getCustomHash() {
        return mHash;
    }

    /**
     * Implementation of a hashing method for variable length
     * Strings. Most of the time intention is that this calculation
     * is done by caller during parsing, not here; however, sometimes
     * it needs to be done for parsed "String" too.
     *
     * @param len Length of String; has to be at least 1 (caller guarantees
     *   this pre-condition)
     */
    public static int calcHash(char[] buffer, int start, int len) {
        int hash = (int) buffer[0];
        for (int i = 1; i < len; ++i) {
            hash = (hash * 31) + (int) buffer[i];
        }
        return hash;
    }

    public static int calcHash(String key) {
        int hash = (int) key.charAt(0);
        for (int i = 1, len = key.length(); i < len; ++i) {
            hash = (hash * 31) + (int) key.charAt(i);

        }
        return hash;
    }

    /*
    //////////////////////////////////////////////////////////
    // Methods for package/core parser
    //////////////////////////////////////////////////////////
     */

    /**
     * This method should never get called on instances of this class,
     * so let's throw an exception if that does happen.
     */
    @Override
    public int sizeInQuads() {
        ErrorConsts.throwInternalError();
        return 0; // never gets here
    }

    @Override
    public int getFirstQuad() {
        ErrorConsts.throwInternalError();
        return 0; // never gets here
    }

    @Override
    public final int getLastQuad() {
        ErrorConsts.throwInternalError();
        return 0; // never gets here
    }

    @Override
    public int getQuad(int index) {
        ErrorConsts.throwInternalError();
        return 0; // never gets here
    }

    /*
    //////////////////////////////////////////////////////////
    // Redefined standard methods
    //////////////////////////////////////////////////////////
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
