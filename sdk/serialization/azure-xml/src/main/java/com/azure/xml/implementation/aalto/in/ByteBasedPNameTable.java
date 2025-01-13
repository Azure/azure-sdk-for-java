// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Aalto XML processor
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

import com.azure.xml.implementation.aalto.util.NameTable;

/**
 * This is a symbol table implementation used for storing byte-based
 * <code>PNames</code>, specifically, instances of ({@link ByteBasedPName}).
 */
public final class ByteBasedPNameTable extends NameTable {
    final static int MIN_HASH_SIZE = 16;

    final static int INITIAL_COLLISION_LEN = 32;

    /**
     * Bucket index is 8 bits, and value 0 is reserved to represent
     * 'empty' status.
     */
    final static int LAST_VALID_BUCKET = 0xFE;

    /*
    /**********************************************************************
    /* Main table state
    /**********************************************************************
     */

    // // // First, global information

    /**
     * Total number of PNames in the symbol table
     */
    private int mCount;

    // // // Then information regarding primary hash array and its
    // // // matching PName array

    /**
     * Mask used to truncate 32-bit hash value to current hash array
     * size; essentially, hash array size - 1 (since hash array sizes
     * are 2^N).
     */
    private int mMainHashMask;

    /**
     * Array of 2^N size, which contains combination
     * of 24-bits of hash (0 to indicate 'empty' slot),
     * and 8-bit collision bucket index (0 to indicate empty
     * collision bucket chain; otherwise subtract one from index)
     */
    private int[] mMainHash;

    /**
     * Array that contains <code>PName</code> instances matching
     * entries in <code>mMainHash</code>. Contains nulls for unused
     * entries.
     */
    private ByteBasedPName[] mMainNames;

    // // // Then the collision/spill-over area info

    /**
     * Array of heads of collision bucket chains; size dynamically
     */
    private Bucket[] mCollList;

    /**
     * Total number of PNames in collision buckets (included in
     * <code>mCount</code> along with primary entries)
     */
    private int mCollCount;

    /**
     * Index of the first unused collision bucket entry (== size of
     * the used portion of collision list): less than
     * or equal to 0xFF (255), since max number of entries is 255
     * (8-bit, minus 0 used as 'empty' marker)
     */
    private int mCollEnd;

    // // // Info regarding pending rehashing...

    /**
     * This flag is set if, after adding a new entry, it is deemed
     * that a rehash is warranted if any more entries are to be added.
     */
    private transient boolean mNeedRehash;

    /*
    /**********************************************************************
    /* Sharing, versioning
    /**********************************************************************
     */

    // // // Which of the buffers may be shared (and are copy-on-write)?

    /**
     * Flag that indicates whether underlying data structures for
     * the main hash area are shared or not. If they are, then they
     * need to be handled in copy-on-write way, i.e. if they need
     * to be modified, a copy needs to be made first; at this point
     * it will not be shared any more, and can be modified.
     *<p>
     * This flag needs to be checked both when adding new main entries,
     * and when adding new collision list queues (i.e. creating a new
     * collision list head entry)
     */
    private boolean mMainHashShared;

    private boolean mMainNamesShared;

    /**
     * Flag that indicates whether underlying data structures for
     * the collision list are shared or not. If they are, then they
     * need to be handled in copy-on-write way, i.e. if they need
     * to be modified, a copy needs to be made first; at this point
     * it will not be shared any more, and can be modified.
     *<p>
     * This flag needs to be checked when adding new collision entries.
     */
    private boolean mCollListShared;

    /*
    /**********************************************************************
    /* Construction, merging
    /**********************************************************************
     */

    public ByteBasedPNameTable(int hashSize) {
        // Sanity check: let's now allow hash sizes below certain min. value
        if (hashSize < MIN_HASH_SIZE) {
            hashSize = MIN_HASH_SIZE;
        } else {
            /* Also; size must be 2^N; otherwise hash algorithm won't
             * work... so let's just pad it up, if so
             */
            if ((hashSize & (hashSize - 1)) != 0) { // only true if it's 2^N
                int curr = MIN_HASH_SIZE;
                while (curr < hashSize) {
                    curr += curr;
                }
                //System.out.println("WARNING: hashSize "+hashSize+" illegal; padding up to "+curr);
                hashSize = curr;
            }
        }

        mCount = 0;
        mMainHashShared = false;
        mMainNamesShared = false;
        mMainHashMask = hashSize - 1;
        mMainHash = new int[hashSize];
        mMainNames = new ByteBasedPName[hashSize];

        mCollListShared = true; // just since it'll need to be allocated
        mCollList = null;
        mCollEnd = 0;

        mNeedRehash = false;
    }

    /**
     * Constructor used when creating a child instance
     */
    ByteBasedPNameTable(ByteBasedPNameTable parent) {
        // First, let's copy the state as is:
        mCount = parent.mCount;
        mMainHashMask = parent.mMainHashMask;
        mMainHash = parent.mMainHash;
        mMainNames = parent.mMainNames;
        mCollList = parent.mCollList;
        mCollCount = parent.mCollCount;
        mCollEnd = parent.mCollEnd;
        mNeedRehash = false;

        // And consider all shared, so far:
        mMainHashShared = true;
        mMainNamesShared = true;
        mCollListShared = true;
    }

    public void mergeFromChild(ByteBasedPNameTable child) {
        // Only makes sense if child has more entries
        if (child.mCount <= mCount) {
            return;
        }
        //System.out.print("["+mCount+"->"+child.mCount+"/"+mMainHash.length+"]");

        mCount = child.mCount;
        mMainHashMask = child.mMainHashMask;
        mMainHash = child.mMainHash;
        mMainNames = child.mMainNames;
        mCollList = child.mCollList;
        mCollCount = child.mCollCount;
        mCollEnd = child.mCollEnd;

        /* Plus, as an added safety measure, let's mark child buffers
         * as shared, just in case it might still be used:
         */
        child.markAsShared();
    }

    public void markAsShared() {
        mMainHashShared = true;
        mMainNamesShared = true;
        mCollListShared = true;
    }

    /*
    /**********************************************************************
    /* API, accessors
    /**********************************************************************
     */

    @Override
    public int size() {
        return mCount;
    }

    /**
     * Method called to check to quickly see if a child symbol table
     * may have gotten additional entries. Used for checking to see
     * if a child table should be merged into shared table.
     */
    @Override
    public boolean maybeDirty() {
        return !mMainHashShared;
    }

    /**
     * Finds and returns name matching the specified symbol, if such
     * name already exists in the table; or if not, creates name object,
     * adds to the table, and returns it.
     *<p>
     * Note: separate methods to optimize common case of relatively
     * short element/attribute names (8 or less ascii characters)
     *
     * @param firstQuad int32 containing first 4 bytes of the pname;
     *   if the whole name less than 4 bytes, padded with zero bytes
     *   in front (zero MSBs, ie. right aligned)
     * @param secondQuad int32 containing bytes 5 through 8 of the
     *   pname; if less than 8 bytes, padded with up to 4 zero bytes
     *   in front (zero MSBs, ie. right aligned)
     *
     * @return PName matching the symbol passed (or constructed for
     *   it)
     */
    public ByteBasedPName findSymbol(int hash, int firstQuad, int secondQuad) {

        int ix = (hash & mMainHashMask);
        int val = mMainHash[ix];

        /* High 24 bits of the value are low 24 bits of hash (low 8 bits
         * are bucket index)... match?
         */
        if ((((val >> 8) ^ hash) << 8) == 0) { // match
            // Ok, but do we have an actual match?
            ByteBasedPName pname = mMainNames[ix];
            if (pname == null) { // main slot empty; can't find
                return null;
            }
            if (pname.equals(firstQuad, secondQuad)) {
                return pname;
            }
        } else if (val == 0) { // empty slot? no match
            return null;
        }
        // Maybe a spill-over?
        val &= 0xFF;
        if (val > 0) { // 0 means 'empty'
            val -= 1; // to convert from 1-based to 0...
            Bucket bucket = mCollList[val];
            if (bucket != null) {
                return bucket.find(hash, firstQuad, secondQuad);
            }
        }
        // Nope, no match whatsoever
        return null;
    }

    /**
     * Finds and returns name matching the specified symbol, if such
     * name already exists in the table; or if not, creates name object,
     * adds to the table, and returns it.
     *<p>
     * Note: this is the general purpose method that can be called for
     * names of any length. However, if name is less than 9 bytes long,
     * it is preferable to call the version optimized for short
     * names.
     *
     * @param quads Array of int32s, each of which contain 4 bytes of
     *   encoded name
     * @param qlen Number of int32s, starting from index 0, in quads
     *   parameter
     *
     * @return PName matching the symbol passed (or constructed for
     *   it)
     */
    public ByteBasedPName findSymbol(int hash, int[] quads, int qlen) {
        if (qlen < 3) { // another sanity check
            return findSymbol(hash, quads[0], (qlen < 2) ? 0 : quads[1]);
        }
        // (for rest of comments regarding logic, see method above)
        int ix = (hash & mMainHashMask);
        int val = mMainHash[ix];
        if ((((val >> 8) ^ hash) << 8) == 0) {
            ByteBasedPName pname = mMainNames[ix];
            if (pname == null) { // main slot empty; no collision list then either
                return null;
            }
            if (pname.equals(quads, qlen)) { // should be match, let's verify
                return pname;
            }
        } else if (val == 0) { // empty slot? no match
            return null;
        }
        val &= 0xFF;
        if (val > 0) { // 0 means 'empty'
            val -= 1; // to convert from 1-based to 0...
            Bucket bucket = mCollList[val];
            if (bucket != null) {
                return bucket.find(hash, quads, qlen);
            }
        }
        return null;
    }

    /*
    /**********************************************************************
    /* API, mutators
    /**********************************************************************
     */

    public ByteBasedPName addSymbol(int hash, String symbolStr, int colonIx, int[] quads, int qlen) {
        ByteBasedPName symbol
            = ByteBasedPNameFactory.getInstance().constructPName(hash, symbolStr, colonIx, quads, qlen);
        doAddSymbol(hash, symbol);
        return symbol;
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    public static int calcHash(int firstQuad) {
        int hash = firstQuad * 31;
        hash ^= (hash >>> 16); // to xor hi- and low- 16-bits
        hash ^= (hash >>> 8); // as well as lowest 2 bytes
        return hash;
    }

    public static int calcHash(int firstQuad, int secondQuad) {
        int hash = (firstQuad * 31) + secondQuad;
        hash ^= (hash >>> 16); // to xor hi- and low- 16-bits
        hash ^= (hash >>> 8); // as well as lowest 2 bytes
        return hash;
    }

    public static int calcHash(int[] quads, int qlen) {
        int hash = quads[0];
        for (int i = 1; i < qlen; ++i) {
            hash = (hash * 31) + quads[i];
        }
        hash ^= (hash >>> 16); // to xor hi- and low- 16-bits
        hash ^= (hash >>> 8); // as well as lowest 2 bytes
        return hash;
    }

    /*
    /**********************************************************************
    /* Standard methods
    /**********************************************************************
     */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[PNameTable, size: ");
        sb.append(mCount);
        sb.append('/');
        sb.append(mMainHash.length);
        sb.append(", ");
        sb.append(mCollCount);
        sb.append(" coll; avg length: ");

        /* Average length: minimum of 1 for all (1 == primary hit);
         * and then 1 per each traversal for collisions/buckets
         */
        //int maxDist = 1;
        int pathCount = mCount;
        for (int i = 0; i < mCollEnd; ++i) {
            int spillLen = mCollList[i].length();
            for (int j = 1; j <= spillLen; ++j) {
                pathCount += j;
            }
        }
        double avgLength;

        if (mCount == 0) {
            avgLength = 0.0;
        } else {
            avgLength = (double) pathCount / (double) mCount;
        }
        // let's round up a bit (two 2 decimal places)
        //avgLength -= (avgLength % 0.01);

        sb.append(avgLength);
        sb.append(']');
        return sb.toString();
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private void doAddSymbol(int hash, ByteBasedPName symbol) {
        if (mMainHashShared) { // always have to modify main entry
            unshareMain();
        }
        // First, do we need to rehash?
        if (mNeedRehash) {
            rehash();
        }

        ++mCount;
        //System.err.println("DEBUG: adding '"+symbol+"' (hash 0x"+Integer.toHexString(hash)+"), size: "+mCount);

        /* Ok, enough about set up: now we need to find the slot to add
         * symbol in:
         */
        int ix = (hash & mMainHashMask);
        if (mMainNames[ix] == null) { // primary empty?
            mMainHash[ix] = (hash << 8);
            if (mMainNamesShared) {
                unshareNames();
            }
            mMainNames[ix] = symbol;
        } else { // nope, it's a collision, need to spill over
            /* How about spill-over area... do we already know the bucket
             * (is the case if it's not the first collision)
             */
            if (mCollListShared) {
                unshareCollision(); // also allocates if list was null
            }
            ++mCollCount;
            int entryValue = mMainHash[ix];
            int bucket = entryValue & 0xFF;
            if (bucket == 0) { // first spill over?
                if (mCollEnd <= LAST_VALID_BUCKET) { // yup, still unshared bucket
                    bucket = mCollEnd;
                    ++mCollEnd;
                    // need to expand?
                    if (bucket >= mCollList.length) {
                        expandCollision();
                    }
                } else { // nope, have to share... let's find shortest?
                    bucket = findBestBucket();
                }
                // Need to mark the entry... and the spill index is 1-based
                mMainHash[ix] = (entryValue & ~0xFF) | (bucket + 1);
            } else {
                --bucket; // 1-based index in value
            }

            // And then just need to link the new bucket entry in
            mCollList[bucket] = new Bucket(symbol, mCollList[bucket]);
        }

        /* Ok. Now, do we need a rehash next time? Need to have at least
         * 50% fill rate no matter what:
         */
        {
            int hashSize = mMainHash.length;
            if (mCount > (hashSize >> 1)) {
                int hashQuarter = (hashSize >> 2);
                /* And either strictly above 75% (the usual) or
                 * just 50%, and collision count >= 25% of total hash size
                 */
                if (mCount > (hashSize - hashQuarter)) {
                    mNeedRehash = true;
                } else if (mCollCount >= hashQuarter) {
                    mNeedRehash = true;
                }
            }
        }
    }

    private void rehash() {
        mNeedRehash = false;
        // Note: since we'll make copies, no need to unshare, can just mark as such:
        mMainNamesShared = false;

        /* And then we can first deal with the main hash area. Since we
         * are expanding linearly (double up), we know there'll be no
         * collisions during this phase.
         */
        int symbolsSeen = 0; // let's do a sanity check
        int[] oldMainHash = mMainHash;
        int len = oldMainHash.length;
        mMainHash = new int[len + len];
        mMainHashMask = (len + len - 1);
        ByteBasedPName[] oldNames = mMainNames;
        mMainNames = new ByteBasedPName[len + len];
        for (int i = 0; i < len; ++i) {
            ByteBasedPName symbol = oldNames[i];
            if (symbol != null) {
                ++symbolsSeen;
                int hash = symbol.hashCode();
                int ix = (hash & mMainHashMask);
                mMainNames[ix] = symbol;
                mMainHash[ix] = hash << 8; // will clear spill index
            }
        }

        /* And then the spill area. This may cause collisions, although
         * not necessarily as many as there were earlier. Let's allocate
         * same amount of space, however
         */
        int oldEnd = mCollEnd;
        if (oldEnd == 0) { // no prior collisions...
            return;
        }

        mCollCount = 0;
        mCollEnd = 0;
        mCollListShared = false;

        Bucket[] oldBuckets = mCollList;
        mCollList = new Bucket[oldBuckets.length];
        for (int i = 0; i < oldEnd; ++i) {
            for (Bucket curr = oldBuckets[i]; curr != null; curr = curr.mNext) {
                ++symbolsSeen;
                ByteBasedPName symbol = curr.mName;
                int hash = symbol.hashCode();
                int ix = (hash & mMainHashMask);
                int val = mMainHash[ix];
                if (mMainNames[ix] == null) { // no primary entry?
                    mMainHash[ix] = (hash << 8);
                    mMainNames[ix] = symbol;
                } else { // nope, it's a collision, need to spill over
                    ++mCollCount;
                    int bucket = val & 0xFF;
                    if (bucket == 0) { // first spill over?
                        if (mCollEnd <= LAST_VALID_BUCKET) { // yup, still unshared bucket
                            bucket = mCollEnd;
                            ++mCollEnd;
                            // need to expand?
                            if (bucket >= mCollList.length) {
                                expandCollision();
                            }
                        } else { // nope, have to share... let's find shortest?
                            bucket = findBestBucket();
                        }
                        // Need to mark the entry... and the spill index is 1-based
                        mMainHash[ix] = (val & ~0xFF) | (bucket + 1);
                    } else {
                        --bucket; // 1-based index in value
                    }
                    // And then just need to link the new bucket entry in
                    mCollList[bucket] = new Bucket(symbol, mCollList[bucket]);
                }
            } // for (... buckets in the chain ...)
        } // for (... list of bucket heads ... )

        if (symbolsSeen != mCount) { // sanity check
            throw new Error("Internal error: count after rehash " + symbolsSeen + "; should be " + mCount);
        }
    }

    /**
     * Method called to find the best bucket to spill a PName over to:
     * usually the first bucket that has only one entry, but in general
     * first one of the buckets with least number of entries
     */
    private int findBestBucket() {
        Bucket[] buckets = mCollList;
        int bestCount = Integer.MAX_VALUE;
        int bestIx = -1;

        for (int i = 0, len = mCollEnd; i < len; ++i) {
            int count = buckets[i].length();
            if (count < bestCount) {
                if (count == 1) { // best possible
                    return i;
                }
                bestCount = count;
                bestIx = i;
            }
        }
        return bestIx;
    }

    /**
     * Method that needs to be called, if the main hash structure
     * is (may be) shared. This happens every time something is added,
     * even if addition is to the collision list (since collision list
     * index comes from lowest 8 bits of the primary hash entry)
     */
    private void unshareMain() {
        int[] old = mMainHash;
        int len = mMainHash.length;

        mMainHash = new int[len];
        System.arraycopy(old, 0, mMainHash, 0, len);
        mMainHashShared = false;
    }

    private void unshareCollision() {
        Bucket[] old = mCollList;
        if (old == null) {
            mCollList = new Bucket[INITIAL_COLLISION_LEN];
        } else {
            int len = old.length;
            mCollList = new Bucket[len];
            System.arraycopy(old, 0, mCollList, 0, len);
        }
        mCollListShared = false;
    }

    private void unshareNames() {
        ByteBasedPName[] old = mMainNames;
        int len = old.length;
        mMainNames = new ByteBasedPName[len];
        System.arraycopy(old, 0, mMainNames, 0, len);
        mMainNamesShared = false;
    }

    private void expandCollision() {
        Bucket[] old = mCollList;
        int len = old.length;
        mCollList = new Bucket[len + len];
        System.arraycopy(old, 0, mCollList, 0, len);
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

    final static class Bucket {
        final ByteBasedPName mName;
        final Bucket mNext;

        Bucket(ByteBasedPName name, Bucket next) {
            mName = name;
            mNext = next;
        }

        public int length() {
            int len = 1;
            for (Bucket curr = mNext; curr != null; curr = curr.mNext) {
                ++len;
            }
            return len;
        }

        public ByteBasedPName find(int hash, int firstQuad, int secondQuad) {
            if (mName.hashEquals(hash, firstQuad, secondQuad)) {
                return mName;
            }
            for (Bucket curr = mNext; curr != null; curr = curr.mNext) {
                ByteBasedPName currName = curr.mName;
                if (currName.hashEquals(hash, firstQuad, secondQuad)) {
                    return currName;
                }
            }
            return null;
        }

        public ByteBasedPName find(int hash, int[] quads, int qlen) {
            if (mName.hashEquals(hash, quads, qlen)) {
                return mName;
            }
            for (Bucket curr = mNext; curr != null; curr = curr.mNext) {
                ByteBasedPName currName = curr.mName;
                if (currName.hashEquals(hash, quads, qlen)) {
                    return currName;
                }
            }
            return null;
        }
    }
}
