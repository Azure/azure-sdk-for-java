// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import java.util.*;

/**
 * This class is used for canonicalization of namespace URIs.
 * It will act as a layer above String.intern(), trying to reduce
 * calls to somewhat slow intern() method, and to do that as efficiently
 * as possible considering that Strings in question are often
 * longer than names in xml documents.
 */
public final class UriCanonicalizer {
    private BoundedHashMap mURIs = null;

    public UriCanonicalizer() {
    }

    private void init() {
        mURIs = new BoundedHashMap();
    }

    public synchronized String canonicalizeURI(char[] ch, int len) {
        CanonicalKey key = new CanonicalKey(ch, len);
        if (mURIs == null) {
            init();
        } else {
            String result = mURIs.get(key);
            if (result != null) {
                return result;
            }
        }
        /* Key we have is not yet stable, as the underlying array
         * is shared and mutable. So:
         */
        key = key.safeClone();
        // Also, now we should intern() the URI
        String uri = new String(ch, 0, len).intern();
        mURIs.put(key, uri);
        return uri;
    }

    /*
    ///////////////////////////////////////////////////
    // Helper classes
    ///////////////////////////////////////////////////
     */

    /**
     * We'll use a bounded map, which should work well for most normal
     * cases, but avoid excesses for degenerate cases (unique URIs
     * used as idenfitiers etc).
     */
    @SuppressWarnings("serial")
    final static class BoundedHashMap extends LinkedHashMap<CanonicalKey, String> {
        /**
         * Let's create cache big enough to usually have enough space for
         * all/most entries for normal cases, but that won't grow
         * indefinitely for degenerate cases
         */
        private final static int DEFAULT_SIZE = 64;

        private final static int MAX_SIZE = (int) (1023 * 0.7f); // 4k primary hash

        public BoundedHashMap() {
            super(DEFAULT_SIZE, 0.7f, true);
        }

        @Override
        public boolean removeEldestEntry(Map.Entry<CanonicalKey, String> entry) {
            return (size() >= MAX_SIZE);
        }
    }

    final static class CanonicalKey {
        /**
         * Array containing characters of the canonicalized String.
         */
        final char[] mChars;

        /**
         * Length of canonicalized String
         */
        final int mLength;

        /**
         * Hash of the URI string, calculated using fast(er) hash
         * function (compared to regular String).
         */
        final int mHash;

        public CanonicalKey(char[] buffer, int len) {
            mChars = buffer;
            mLength = len;
            mHash = calcKeyHash(buffer, len);
        }

        public CanonicalKey(char[] buffer, int len, int hashCode) {
            mChars = buffer;
            mLength = len;
            mHash = hashCode;
        }

        public CanonicalKey safeClone() {
            char[] newBuf = new char[mLength];
            System.arraycopy(mChars, 0, newBuf, 0, mLength);
            return new CanonicalKey(newBuf, mLength, mHash);
        }

        public static int calcKeyHash(char[] buffer, int len) {
            /* Short URIs are not common, but if they were to
             * happen, let's just use regular String.hashCode();
             * it's good one, and for short strings, fast enough
             */
            if (len <= 8) { // we know it's at least one char, though
                int hash = buffer[0];
                // For these, let's use regular hashing method
                for (int i = 1; i < len; ++i) {
                    hash = (hash * 31) + buffer[i];
                }
                return hash;
            }

            /* Ok, longer. So first let's use length xored with first char;
             * usually first 4 will just be "http" anyways (and could
             * just be skipped for good?)
             */
            int hash = len ^ buffer[0];

            /* Otherwise, let's start with length, xor with first char,
             * then latter chars separated by larger and larger
             * spaces. The idea is to severely limit time needed
             * to calc hash code as URIs can get quite long.
             * But let's ignore last 4 chars, for now (we'll use them
             * all after the loop)
             */
            int ix = 2; // start from 3rd char (buffer[2])
            int dist = 2; // and skip 1 char first
            int end = (len - 4);

            while (ix < end) {
                hash = (hash * 31) + buffer[ix];
                ix += dist;
                ++dist; // will skip progressively longer spans
            }

            // And then last 4 chars...
            hash = (hash * 31) ^ (buffer[end] << 2) + buffer[end + 1];
            hash = (hash * 31) + (buffer[end + 2] << 2) ^ buffer[end + 3];
            return hash;
        }

        @Override
        public String toString() {
            return "{URI, hash: 0x" + Integer.toHexString(mHash) + "}";
        }

        @Override
        public int hashCode() {
            return mHash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (o.getClass() != getClass())
                return false;

            CanonicalKey other = (CanonicalKey) o;
            if (other.mLength != mLength)
                return false;
            for (int i = 0; i < mLength; ++i) {
                if (mChars[i] != other.mChars[i]) {
                    return false;
                }
            }
            return true;
        }

    }
}
