// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.sym;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import com.typespec.json.implementation.jackson.core.JsonFactory;
import com.typespec.json.implementation.jackson.core.util.InternCache;

/**
 * Replacement for <code>BytesToNameCanonicalizer</code> which aims at more localized
 * memory access due to flattening of name quad data.
 * Performance improvement modest for simple JSON document data binding (maybe 3%),
 * but should help more for larger symbol tables, or for binary formats like Smile.
 *<p>
 * Hash area is divided into 4 sections:
 *<ol>
 * <li>Primary area (1/2 of total size), direct match from hash (LSB)</li>
 * <li>Secondary area (1/4 of total size), match from {@code hash (LSB) >> 1}</li>
 * <li>Tertiary area (1/8 of total size), match from {@code hash (LSB) >> 2}</li>
 * <li>Spill-over area (remaining 1/8) with linear scan, insertion order</li>
 * <li></li>
 * </ol>
 * and within every area, entries are 4 {@code int}s, where 1 - 3 ints contain 1 - 12
 * UTF-8 encoded bytes of name (null-padded), and last int is offset in
 * {@code _names} that contains actual name Strings.
 *
 * @since 2.6
 */
@SuppressWarnings("fallthrough")
public final class ByteQuadsCanonicalizer
{
    /**
     * Initial size of the primary hash area. Each entry consumes 4 ints (16 bytes),
     * and secondary area is same as primary; so default size will use 2kB of memory
     * (plus 64x4 or 64x8 (256/512 bytes) for references to Strings, and Strings
     * themselves).
     */
    private static final int DEFAULT_T_SIZE = 64;
//    private static final int DEFAULT_T_SIZE = 256;

    /**
     * Let's not expand symbol tables past some maximum size;
     * with unique (~= random) names.
     * Size is in
     */
    private static final int MAX_T_SIZE = 0x10000; // 64k entries == 2M mem hash area

    /**
     * No point in trying to construct tiny tables, just need to resize soon.
     */
    private final static int MIN_HASH_SIZE = 16;

    /**
     * Let's only share reasonably sized symbol tables. Max size set to 3/4 of 8k;
     * this corresponds to 256k main hash index. This should allow for enough distinct
     * names for almost any case, while preventing ballooning for cases where names
     * are unique (or close thereof).
     */
    protected final static int MAX_ENTRIES_FOR_REUSE = 6000;

    /*
    /**********************************************************
    /* Linkage, needed for merging symbol tables
    /**********************************************************
     */

    /**
     * Reference to the root symbol table, for child tables, so
     * that they can merge table information back as necessary.
     */
    protected final ByteQuadsCanonicalizer _parent;

    /**
     * Member that is only used by the root table instance: root
     * passes immutable state info child instances, and children
     * may return new state if they add entries to the table.
     * Child tables do NOT use the reference.
     */
    protected final AtomicReference<TableInfo> _tableInfo;

    /**
     * Seed value we use as the base to make hash codes non-static between
     * different runs, but still stable for lifetime of a single symbol table
     * instance.
     * This is done for security reasons, to avoid potential DoS attack via
     * hash collisions.
     */
    protected final int _seed;

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Whether canonical symbol Strings are to be intern()ed before added
     * to the table or not.
     *<p>
     * NOTE: non-final to allow disabling intern()ing in case of excessive
     * collisions.
     */
    protected final boolean _intern;

    /**
     * Flag that indicates whether we should throw an exception if enough
     * hash collisions are detected (true); or just worked around (false).
     *
     * @since 2.4
     */
    protected final boolean _failOnDoS;

    /*
    /**********************************************************
    /* First, main hash area info
    /**********************************************************
     */

    /**
     * Primary hash information area: consists of <code>2 * _hashSize</code>
     * entries of 16 bytes (4 ints), arranged in a cascading lookup
     * structure (details of which may be tweaked depending on expected rates
     * of collisions).
     */
    protected int[] _hashArea;

    /**
     * Number of slots for primary entries within {@link #_hashArea}; which is
     * at most <code>1/8</code> of actual size of the underlying array (4-int slots,
     * primary covers only half of the area; plus, additional area for longer
     * symbols after hash area).
     */
    protected int _hashSize;

    /**
     * Offset within {@link #_hashArea} where secondary entries start
     */
    protected int _secondaryStart;

    /**
     * Offset within {@link #_hashArea} where tertiary entries start
     */
    protected int _tertiaryStart;

    /**
     * Constant that determines size of buckets for tertiary entries:
     * <code>1 &lt;&lt; _tertiaryShift</code> is the size, and shift value
     * is also used for translating from primary offset into
     * tertiary bucket (shift right by <code>4 + _tertiaryShift</code>).
     *<p>
     * Default value is 2, for buckets of 4 slots; grows bigger with
     * bigger table sizes.
     */
    protected int _tertiaryShift;

    /**
     * Total number of Strings in the symbol table; only used for child tables.
     */
    protected int _count;

    /**
     * Array that contains <code>String</code> instances matching
     * entries in {@link #_hashArea}.
     * Contains nulls for unused entries. Note that this size is twice
     * that of {@link #_hashArea}
     */
    protected String[] _names;

    /*
    /**********************************************************
    /* Then information on collisions etc
    /**********************************************************
     */

    /**
     * Pointer to the offset within spill-over area where there is room
     * for more spilled over entries (if any).
     * Spill over area is within fixed-size portion of {@link #_hashArea}.
     */
    protected int _spilloverEnd;

    /**
     * Offset within {@link #_hashArea} that follows main slots and contains
     * quads for longer names (13 bytes or longer), and points to the
     * first available int that may be used for appending quads of the next
     * long name.
     * Note that long name area follows immediately after the fixed-size
     * main hash area ({@link #_hashArea}).
     */
    protected int _longNameOffset;

    /*
    /**********************************************************
    /* Sharing, versioning
    /**********************************************************
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
    protected boolean _hashShared;

    /*
    /**********************************************************
    /* Life-cycle: constructors
    /**********************************************************
     */

    /**
     * Constructor used for creating per-{@code TokeanStreamFactory} "root"
     * symbol tables: ones used for merging and sharing common symbols
     *
     * @param sz Initial primary hash area size
     * @param seed Random seed valued used to make it more difficult to cause
     *   collisions (used for collision-based DoS attacks).
     */
    private ByteQuadsCanonicalizer(int sz, int seed)
    {
        // Settings to distinguish parent table: no parent
        _parent = null;
        _count = 0;

        // and mark as shared just in case to prevent modifications
        _hashShared = true;
        _seed = seed;
        _intern = false;
        _failOnDoS = true;
        // Sanity check: let's now allow hash sizes below certain minimum value
        if (sz < MIN_HASH_SIZE) {
            sz = MIN_HASH_SIZE;
        } else {
            // Also; size must be 2^N; otherwise hash algorithm won't
            // work... so let's just pad it up, if so
            if ((sz & (sz - 1)) != 0) { // only true if it's 2^N
                int curr = MIN_HASH_SIZE;
                while (curr < sz) {
                    curr += curr;
                }
                sz = curr;
            }
        }
        _tableInfo = new AtomicReference<TableInfo>(TableInfo.createInitial(sz));
    }

    /**
     * Constructor used when creating a child instance
     */
    private ByteQuadsCanonicalizer(ByteQuadsCanonicalizer parent, int seed,
            TableInfo state,
            boolean intern, boolean failOnDoS)
    {
        _parent = parent;
        _seed = seed;
        _intern = intern;
        _failOnDoS = failOnDoS;
        _tableInfo = null; // not used by child tables

        // Then copy shared state
        _count = state.count;
        _hashSize = state.size;
        _secondaryStart = _hashSize << 2; // right after primary area
        _tertiaryStart = _secondaryStart + (_secondaryStart >> 1); // right after secondary
        _tertiaryShift = state.tertiaryShift;

        _hashArea = state.mainHash;
        _names = state.names;

        _spilloverEnd = state.spilloverEnd;
        _longNameOffset = state.longNameOffset;

        // and then set other state to reflect sharing status
        _hashShared = true;
    }

    /**
     * Alternate constructor used in cases where a "placeholder" child
     * instance is needed when symbol table is not really used, but
     * caller needs a non-null placeholder to keep code functioning
     * with minimal awareness of distinction (all lookups fail to match
     * any name without error; add methods should NOT be called).
     *
     * @since 2.13
     */
    private ByteQuadsCanonicalizer(TableInfo state)
    {
        _parent = null;
        _seed = 0;
        _intern = false;
        _failOnDoS = true;
        _tableInfo = null; // not used by child tables

        // Then copy minimal pieces of shared state; only enough to guarantee
        // we will neither find anything nor fail -- primary hash is enough
        // for that purpose

        _count = -1;

        _hashArea = state.mainHash;
        _names = state.names;

        _hashSize = state.size;

        // But otherwise can just use markers towards end of table to
        // indicate error if access was attempted
        final int end = _hashArea.length;
        _secondaryStart = end;
        _tertiaryStart = end;
        _tertiaryShift = 1; //  bogus

        _spilloverEnd = end;
        _longNameOffset = end;

        // just in case something failed, to ensure copying would be done
        _hashShared = true;
    }

    /*
    /**********************************************************
    /* Life-cycle: factory methods, merging
    /**********************************************************
     */

    /**
     * Factory method to call to create a symbol table instance with a
     * randomized seed value.
     *
     * @return Root instance to use for constructing new child instances
     */
    public static ByteQuadsCanonicalizer createRoot() {
        // Need to use a variable seed, to thwart hash-collision based attacks.
        // 14-Feb-2017, tatu: Does this actually help?
        long now = System.currentTimeMillis();
        // ensure it's not 0; and might as well require to be odd so:
        int seed = (((int) now) + ((int) (now >>> 32))) | 1;
        return createRoot(seed);
    }

    // Factory method that should only be called from unit tests, where seed
    // value should remain the same.
    protected static ByteQuadsCanonicalizer createRoot(int seed) {
        return new ByteQuadsCanonicalizer(DEFAULT_T_SIZE, seed);
    }

    /**
     * Factory method used to create actual symbol table instance to
     * use for parsing.
     *
     * @param flags Bit flags of active {@link com.typespec.json.implementation.jackson.core.JsonFactory.Feature}s enabled.
     *
     * @return Actual canonicalizer instance that can be used by a parser
     */
    public ByteQuadsCanonicalizer makeChild(int flags) {
        return new ByteQuadsCanonicalizer(this, _seed,
                _tableInfo.get(),
                JsonFactory.Feature.INTERN_FIELD_NAMES.enabledIn(flags),
                JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.enabledIn(flags));
    }

    /**
     * Method similar to {@link #makeChild} but one that only creates real
     * instance of {@link com.typespec.json.implementation.jackson.core.JsonFactory.Feature#CANONICALIZE_FIELD_NAMES} is
     * enabled: otherwise a "bogus" instance is created.
     *
     * @param flags Bit flags of active {@link com.typespec.json.implementation.jackson.core.JsonFactory.Feature}s enabled.
     *
     * @return Actual canonicalizer instance that can be used by a parser if (and only if)
     *    canonicalization is enabled; otherwise a non-null "placeholder" instance.
     *
     * @since 2.13
     */
    public ByteQuadsCanonicalizer makeChildOrPlaceholder(int flags) {
        if (JsonFactory.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(flags)) {
            // inlined "makeChild()"
            return new ByteQuadsCanonicalizer(this, _seed,
                    _tableInfo.get(),
                    JsonFactory.Feature.INTERN_FIELD_NAMES.enabledIn(flags),
                    JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.enabledIn(flags));
        }
        return new ByteQuadsCanonicalizer(_tableInfo.get());
    }

    /**
     * Method called by the using code to indicate it is done with this instance.
     * This lets instance merge accumulated changes into parent (if need be),
     * safely and efficiently, and without calling code having to know about parent
     * information.
     */
    public void release()
    {
        // we will try to merge if child table has new entries
        // 28-Jul-2019, tatu: From [core#548]: do not share if immediate rehash needed
        if ((_parent != null) && maybeDirty()) {
            _parent.mergeChild(new TableInfo(this));
            // Let's also mark this instance as dirty, so that just in
            // case release was too early, there's no corruption of possibly shared data.
            _hashShared = true;
        }
    }

    private void mergeChild(TableInfo childState)
    {
        final int childCount = childState.count;
        TableInfo currState = _tableInfo.get();

        // Should usually grow; but occasionally could also shrink if (but only if)
        // collision list overflow ends up clearing some collision lists.
        if (childCount == currState.count) {
            return;
        }

        // One caveat: let's try to avoid problems with degenerate cases of documents with
        // generated "random" names: for these, symbol tables would bloat indefinitely.
        // One way to do this is to just purge tables if they grow
        // too large, and that's what we'll do here.
        if (childCount > MAX_ENTRIES_FOR_REUSE) {
            // At any rate, need to clean up the tables
            childState = TableInfo.createInitial(DEFAULT_T_SIZE);
        }
        _tableInfo.compareAndSet(currState, childState);
    }

    /*
    /**********************************************************
    /* API, accessors
    /**********************************************************
     */

    /**
     * @return Number of symbol entries contained by this canonicalizer instance
     */
    public int size()
    {
        if (_tableInfo != null) { // root table
            return _tableInfo.get().count;
        }
        // nope, child table
        return _count;
    }

    /**
     * @return number of primary slots table has currently
     */
    public int bucketCount() { return _hashSize; }

    /**
     * Method called to check to quickly see if a child symbol table
     * may have gotten additional entries. Used for checking to see
     * if a child table should be merged into shared table.
     *
     * @return Whether main hash area has been modified
     */
    public boolean maybeDirty() { return !_hashShared; }

    public int hashSeed() { return _seed; }

    /**
     * @return True for "real", canonicalizing child tables; false for
     *    root table as well as placeholder "child" tables.
     *
     * @since 2.13
     */
    public boolean isCanonicalizing() {
        // couple of options, but for now missing parent linkage simplest:
        return _parent != null;
    }

    /**
     * Method mostly needed by unit tests; calculates number of
     * entries that are in the primary slot set. These are
     * "perfect" entries, accessible with a single lookup
     *
     * @return Number of entries in the primary hash area
     */
    public int primaryCount()
    {
        int count = 0;
        for (int offset = 3, end = _secondaryStart; offset < end; offset += 4) {
            if (_hashArea[offset] != 0) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Method mostly needed by unit tests; calculates number of entries
     * in secondary buckets
     *
     * @return Number of entries in the secondary hash area
     */
    public int secondaryCount() {
        int count = 0;
        int offset = _secondaryStart + 3;
        for (int end = _tertiaryStart; offset < end; offset += 4) {
            if (_hashArea[offset] != 0) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Method mostly needed by unit tests; calculates number of entries
     * in tertiary buckets
     *
     * @return Number of entries in the tertiary hash area
     */
    public int tertiaryCount() {
        int count = 0;
        int offset = _tertiaryStart + 3; // to 1.5x, starting point of tertiary
        for (int end = offset + _hashSize; offset < end; offset += 4) {
            if (_hashArea[offset] != 0) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Method mostly needed by unit tests; calculates number of entries
     * in shared spill-over area
     *
     * @return Number of entries in the linear spill-over areay
     */
    public int spilloverCount() {
        // difference between spillover end, start, divided by 4 (four ints per slot)
        return (_spilloverEnd - _spilloverStart()) >> 2;
    }

    public int totalCount()
    {
        int count = 0;
        for (int offset = 3, end = (_hashSize << 3); offset < end; offset += 4) {
            if (_hashArea[offset] != 0) {
                ++count;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        int pri = primaryCount();
        int sec = secondaryCount();
        int tert = tertiaryCount();
        int spill = spilloverCount();
        int total = totalCount();
        return String.format("[%s: size=%d, hashSize=%d, %d/%d/%d/%d pri/sec/ter/spill (=%s), total:%d]",
                getClass().getName(), _count, _hashSize,
                pri, sec, tert, spill, (pri+sec+tert+spill), total);
    }

    /*
    /**********************************************************
    /* Public API, accessing symbols
    /**********************************************************
     */

    public String findName(int q1)
    {
        int offset = _calcOffset(calcHash(q1));
        // first: primary match?
        final int[] hashArea = _hashArea;

        int len = hashArea[offset+3];

        if (len == 1) {
            if (hashArea[offset] == q1) {
                return _names[offset >> 2];
            }
        } else if (len == 0) { // empty slot; unlikely but avoid further lookups if so
            return null;
        }
        // secondary? single slot shared by N/2 primaries
        int offset2 = _secondaryStart + ((offset >> 3) << 2);

        len = hashArea[offset2+3];

        if (len == 1) {
            if (hashArea[offset2] == q1) {
                return _names[offset2 >> 2];
            }
        } else if (len == 0) { // empty slot; unlikely but avoid further lookups if so
            return null;
        }

        // tertiary lookup & spillovers best to offline
        return _findSecondary(offset, q1);
    }

    public String findName(int q1, int q2)
    {
        int offset = _calcOffset(calcHash(q1, q2));

        final int[] hashArea = _hashArea;

        int len = hashArea[offset+3];

        if (len == 2) {
            if ((q1 == hashArea[offset]) && (q2 == hashArea[offset+1])) {
                return _names[offset >> 2];
            }
        } else if (len == 0) { // empty slot; unlikely but avoid further lookups if so
            return null;
        }
        // secondary?
        int offset2 = _secondaryStart + ((offset >> 3) << 2);

        len = hashArea[offset2+3];

        if (len == 2) {
            if ((q1 == hashArea[offset2]) && (q2 == hashArea[offset2+1])) {
                return _names[offset2 >> 2];
            }
        } else if (len == 0) { // empty slot? Short-circuit if no more spillovers
            return null;
        }
        return _findSecondary(offset, q1, q2);
    }

    public String findName(int q1, int q2, int q3)
    {
        int offset = _calcOffset(calcHash(q1, q2, q3));
        final int[] hashArea = _hashArea;
        int len = hashArea[offset+3];

        if (len == 3) {
            if ((q1 == hashArea[offset]) && (hashArea[offset+1] == q2) && (hashArea[offset+2] == q3)) {
                return _names[offset >> 2];
            }
        } else if (len == 0) { // empty slot; unlikely but avoid further lookups if so
            return null;
        }
        // secondary?
        int offset2 = _secondaryStart + ((offset >> 3) << 2);

        len = hashArea[offset2+3];

        if (len == 3) {
            if ((q1 == hashArea[offset2]) && (hashArea[offset2+1] == q2) && (hashArea[offset2+2] == q3)) {
                return _names[offset2 >> 2];
            }
        } else if (len == 0) { // empty slot? Short-circuit if no more spillovers
            return null;
        }
        return _findSecondary(offset, q1, q2, q3);
    }

    public String findName(int[] q, int qlen)
    {
        /* This version differs significantly, because longer names do not fit within cell.
         * Rather, they contain hash in main slot, and offset+length to extension area
         * that contains actual quads.
         */
        if (qlen < 4) { // another sanity check
            switch (qlen) {
            case 3:
                return findName(q[0], q[1], q[2]);
            case 2:
                return findName(q[0], q[1]);
            case 1:
                return findName(q[0]);
            default: // if 0 ever passed
                return "";
            }
        }
        final int hash = calcHash(q, qlen);
        int offset = _calcOffset(hash);

        final int[] hashArea = _hashArea;

        final int len = hashArea[offset+3];

        if ((hash == hashArea[offset]) && (len == qlen)) {
            // probable but not guaranteed: verify
            if (_verifyLongName(q, qlen, hashArea[offset+1])) {
                return _names[offset >> 2];
            }
        }
        if (len == 0) { // empty slot; unlikely but avoid further lookups if so
            return null;
        }
        // secondary?
        int offset2 = _secondaryStart + ((offset >> 3) << 2);

        final int len2 = hashArea[offset2+3];
        if ((hash == hashArea[offset2]) && (len2 == qlen)) {
            if (_verifyLongName(q, qlen, hashArea[offset2+1])) {
                return _names[offset2 >> 2];
            }
        }
        return _findSecondary(offset, hash, q, qlen);
    }

    private final int _calcOffset(int hash)
    {
        // NOTE: simple for initial impl, but we may want to interleave it a bit
        // in near future
        // So: first, hash into primary hash index
        int ix = hash & (_hashSize-1);
        // keeping in mind we have 4 ints per entry
        return (ix << 2);
    }

    /*
    /**********************************************************
    /* Access from spill-over areas
    /**********************************************************
     */

    private String _findSecondary(int origOffset, int q1)
    {
        // tertiary area division is dynamic. First; its size is N/4 compared to
        // primary hash size; and offsets are for 4 int slots. So to get to logical
        // index would shift by 4. But! Tertiary area is further split into buckets,
        // determined by shift value. And finally, from bucket back into physical offsets
        int offset = _tertiaryStart + ((origOffset >> (_tertiaryShift + 2)) << _tertiaryShift);
        final int[] hashArea = _hashArea;
        final int bucketSize = (1 << _tertiaryShift);
        for (int end = offset + bucketSize; offset < end; offset += 4) {
            int len = hashArea[offset+3];
            if ((q1 == hashArea[offset]) && (1 == len)) {
                return _names[offset >> 2];
            }
            if (len == 0) {
                return null;
            }
        }
        // but if tertiary full, check out spill-over area as last resort
        // shared spillover starts at 7/8 of the main hash area
        // (which is sized at 2 * _hashSize), so:
        for (offset = _spilloverStart(); offset < _spilloverEnd; offset += 4) {
            if ((q1 == hashArea[offset]) && (1 == hashArea[offset+3])) {
                return _names[offset >> 2];
            }
        }
        return null;
    }

    private String _findSecondary(int origOffset, int q1, int q2)
    {
        int offset = _tertiaryStart + ((origOffset >> (_tertiaryShift + 2)) << _tertiaryShift);
        final int[] hashArea = _hashArea;

        final int bucketSize = (1 << _tertiaryShift);
        for (int end = offset + bucketSize; offset < end; offset += 4) {
            int len = hashArea[offset+3];
            if ((q1 == hashArea[offset]) && (q2 == hashArea[offset+1]) && (2 == len)) {
                return _names[offset >> 2];
            }
            if (len == 0) {
                return null;
            }
        }
        for (offset = _spilloverStart(); offset < _spilloverEnd; offset += 4) {
            if ((q1 == hashArea[offset]) && (q2 == hashArea[offset+1]) && (2 == hashArea[offset+3])) {
                return _names[offset >> 2];
            }
        }
        return null;
    }

    private String _findSecondary(int origOffset, int q1, int q2, int q3)
    {
        int offset = _tertiaryStart + ((origOffset >> (_tertiaryShift + 2)) << _tertiaryShift);
        final int[] hashArea = _hashArea;

        final int bucketSize = (1 << _tertiaryShift);
        for (int end = offset + bucketSize; offset < end; offset += 4) {
            int len = hashArea[offset+3];
            if ((q1 == hashArea[offset]) && (q2 == hashArea[offset+1]) && (q3 == hashArea[offset+2]) && (3 == len)) {
                return _names[offset >> 2];
            }
            if (len == 0) {
                return null;
            }
        }
        for (offset = _spilloverStart(); offset < _spilloverEnd; offset += 4) {
            if ((q1 == hashArea[offset]) && (q2 == hashArea[offset+1]) && (q3 == hashArea[offset+2])
                    && (3 == hashArea[offset+3])) {
                return _names[offset >> 2];
            }
        }
        return null;
    }

    private String _findSecondary(int origOffset, int hash, int[] q, int qlen)
    {
        int offset = _tertiaryStart + ((origOffset >> (_tertiaryShift + 2)) << _tertiaryShift);
        final int[] hashArea = _hashArea;

        final int bucketSize = (1 << _tertiaryShift);
        for (int end = offset + bucketSize; offset < end; offset += 4) {
            int len = hashArea[offset+3];
            if ((hash == hashArea[offset]) && (qlen == len)) {
                if (_verifyLongName(q, qlen, hashArea[offset+1])) {
                    return _names[offset >> 2];
                }
            }
            if (len == 0) {
                return null;
            }
        }
        for (offset = _spilloverStart(); offset < _spilloverEnd; offset += 4) {
            if ((hash == hashArea[offset]) && (qlen == hashArea[offset+3])) {
                if (_verifyLongName(q, qlen, hashArea[offset+1])) {
                    return _names[offset >> 2];
                }
            }
        }
        return null;
    }

    private boolean _verifyLongName(int[] q, int qlen, int spillOffset)
    {
        final int[] hashArea = _hashArea;
        // spillOffset assumed to be physical index right into quad string
        int ix = 0;

        switch (qlen) {
        default:
            return _verifyLongName2(q, qlen, spillOffset);
        case 8:
            if (q[ix++] != hashArea[spillOffset++]) return false;
        case 7:
            if (q[ix++] != hashArea[spillOffset++]) return false;
        case 6:
            if (q[ix++] != hashArea[spillOffset++]) return false;
        case 5:
            if (q[ix++] != hashArea[spillOffset++]) return false;
        case 4: // always at least 4
            if (q[ix++] != hashArea[spillOffset++]) return false;
            if (q[ix++] != hashArea[spillOffset++]) return false;
            if (q[ix++] != hashArea[spillOffset++]) return false;
            if (q[ix++] != hashArea[spillOffset++]) return false;
        }
        return true;
    }

    private boolean _verifyLongName2(int[] q, int qlen, int spillOffset)
    {
        int ix = 0;
        do {
            if (q[ix++] != _hashArea[spillOffset++]) {
                return false;
            }
        } while (ix < qlen);
        return true;
    }

    /*
    /**********************************************************
    /* API, mutators
    /**********************************************************
     */

    public String addName(String name, int q1) {
        _verifySharing();
        if (_intern) {
            name = InternCache.instance.intern(name);
        }
        int offset = _findOffsetForAdd(calcHash(q1));
        _hashArea[offset] = q1;
        _hashArea[offset+3] = 1;
        _names[offset >> 2] = name;
        ++_count;
        return name;
    }

    public String addName(String name, int q1, int q2) {
        _verifySharing();
        if (_intern) {
            name = InternCache.instance.intern(name);
        }

        // 20-Mar-2021, tatu: For some reason, pre-2.13 there was logic
        //    to support "only one quad" case. Not sure why, does not make sense;
        //    removed from 2.13.0.
//        int hash = (q2 == 0) ? calcHash(q1) : calcHash(q1, q2);
        final int hash = calcHash(q1, q2);
        final int offset = _findOffsetForAdd(hash);
        _hashArea[offset] = q1;
        _hashArea[offset+1] = q2;
        _hashArea[offset+3] = 2;
        _names[offset >> 2] = name;
        ++_count;
        return name;
    }

    public String addName(String name, int q1, int q2, int q3) {
        _verifySharing();
        if (_intern) {
            name = InternCache.instance.intern(name);
        }
        int offset = _findOffsetForAdd(calcHash(q1, q2, q3));
        _hashArea[offset] = q1;
        _hashArea[offset+1] = q2;
        _hashArea[offset+2] = q3;
        _hashArea[offset+3] = 3;
        _names[offset >> 2] = name;
        ++_count;
        return name;
    }

    public String addName(String name, int[] q, int qlen)
    {
        _verifySharing();
        if (_intern) {
            name = InternCache.instance.intern(name);
        }
        int offset;

        switch (qlen) {
        case 1:
        {
                offset = _findOffsetForAdd(calcHash(q[0]));
                _hashArea[offset] = q[0];
                _hashArea[offset+3] = 1;
            }
            break;
        case 2:
            {
                offset = _findOffsetForAdd(calcHash(q[0], q[1]));
                _hashArea[offset] = q[0];
                _hashArea[offset+1] = q[1];
                _hashArea[offset+3] = 2;
            }
            break;
        case 3:
            {
                offset = _findOffsetForAdd(calcHash(q[0], q[1], q[2]));
                _hashArea[offset] = q[0];
                _hashArea[offset+1] = q[1];
                _hashArea[offset+2] = q[2];
                _hashArea[offset+3] = 3;
            }
            break;
        default:
            final int hash = calcHash(q, qlen);
            offset = _findOffsetForAdd(hash);

            _hashArea[offset] = hash;
            int longStart = _appendLongName(q, qlen);
            _hashArea[offset+1] = longStart;
            _hashArea[offset+3] = qlen;
        }
        // plus add the actual String
        _names[offset >> 2] = name;

        // and finally; see if we really should rehash.
        ++_count;
        return name;
    }

    private void _verifySharing()
    {
        if (_hashShared) {
            // 12-Mar-2021, tatu: prevent modifying of "placeholder" and
            //   parent tables
            if (_parent == null) {
                if (_count == 0) { // root
                    throw new IllegalStateException("Cannot add names to Root symbol table");
                }
                throw new IllegalStateException("Cannot add names to Placeholder symbol table");
            }

            _hashArea = Arrays.copyOf(_hashArea, _hashArea.length);
            _names = Arrays.copyOf(_names, _names.length);
            _hashShared = false;
        }
    }

    /**
     * Method called to find the location within hash table to add a new symbol in.
     */
    private int _findOffsetForAdd(int hash)
    {
        // first, check the primary: if slot found, no need for resize
        int offset = _calcOffset(hash);
        final int[] hashArea = _hashArea;
        if (hashArea[offset+3] == 0) {
//System.err.printf(" PRImary slot #%d, hash %X\n", (offset>>2), hash & 0x7F);
            return offset;
        }

        // Otherwise let's see if we are due resize():
        if (_checkNeedForRehash()) {
            return _resizeAndFindOffsetForAdd(hash);
        }

        // If not, proceed with secondary slot
        int offset2 = _secondaryStart + ((offset >> 3) << 2);
        if (hashArea[offset2+3] == 0) {
//System.err.printf(" SECondary slot #%d (start x%X), hash %X\n",(offset >> 3), _secondaryStart, (hash & 0x7F));
            return offset2;
        }
        // if not, tertiary?

        offset2 = _tertiaryStart + ((offset >> (_tertiaryShift + 2)) << _tertiaryShift);
        final int bucketSize = (1 << _tertiaryShift);
        for (int end = offset2 + bucketSize; offset2 < end; offset2 += 4) {
            if (hashArea[offset2+3] == 0) {
//System.err.printf(" TERtiary slot x%X (from x%X, start x%X), hash %X.\n", offset2, ((offset >> (_tertiaryShift + 2)) << _tertiaryShift), _tertiaryStart, (hash & 0x7F));
                return offset2;
            }
        }

        // and if even tertiary full, append at the end of spill area
        offset = _spilloverEnd;
        _spilloverEnd += 4;

//System.err.printf(" SPIll-over at x%X; start x%X; end x%X, hash %X\n", offset, _spilloverStart(), _hashArea.length, (hash & 0x7F));

        // one caveat: in the unlikely event if spill-over filling up,
        // check if that could be considered a DoS attack; handle appropriately
        // (NOTE: approximate for now; we could verify details if that becomes necessary)
        /* 31-Jul-2015, tatu: Note that spillover area does NOT end at end of array,
         *   since "long names" area follows. Instead, need to calculate from hash size.
         */
        final int end = (_hashSize << 3);
        if (_spilloverEnd >= end) {
            if (_failOnDoS) {
                _reportTooManyCollisions();
            }
            return _resizeAndFindOffsetForAdd(hash);
        }
        return offset;
    }

    // @since 2.10
    private int _resizeAndFindOffsetForAdd(int hash)
    {
        // First things first: we need to resize+rehash (or, if too big, nuke contents)
        rehash();

        // Copy of main _findOffsetForAdd except for checks to resize: can not be needed
        int offset = _calcOffset(hash);
        final int[] hashArea = _hashArea;
        if (hashArea[offset+3] == 0) {
            return offset;
        }
        int offset2 = _secondaryStart + ((offset >> 3) << 2);
        if (hashArea[offset2+3] == 0) {
            return offset2;
        }
        offset2 = _tertiaryStart + ((offset >> (_tertiaryShift + 2)) << _tertiaryShift);
        final int bucketSize = (1 << _tertiaryShift);
        for (int end = offset2 + bucketSize; offset2 < end; offset2 += 4) {
            if (hashArea[offset2+3] == 0) {
                return offset2;
            }
        }
        offset = _spilloverEnd;
        _spilloverEnd += 4;
        return offset;
    }

    // Helper method for checking if we should simply rehash() before add
    private boolean _checkNeedForRehash() {
        // Yes if above 80%, or above 50% AND have ~1% spill-overs
        if (_count > (_hashSize >> 1)) { // over 50%
            int spillCount = (_spilloverEnd - _spilloverStart()) >> 2;
            if ((spillCount > (1 + _count >> 7))
                    || (_count > (_hashSize * 0.80))) {
                return true;
            }
        }
        return false;
    }

    private int _appendLongName(int[] quads, int qlen)
    {
        int start = _longNameOffset;

        // note: at this point we must already be shared. But may not have enough space
        if ((start + qlen) > _hashArea.length) {
            // try to increment in reasonable chunks; at least space that we need
            int toAdd = (start + qlen) - _hashArea.length;
            // but at least 1/8 of regular hash area size or 16kB (whichever smaller)
            int minAdd = Math.min(4096, _hashSize);

            int newSize = _hashArea.length + Math.max(toAdd, minAdd);
            _hashArea = Arrays.copyOf(_hashArea, newSize);
        }
        System.arraycopy(quads, 0, _hashArea, start, qlen);
        _longNameOffset += qlen;
        return start;
    }

    /*
    /**********************************************************
    /* Hash calculation
    /**********************************************************
     */

    /* Note on hash calculation: we try to make it more difficult to
     * generate collisions automatically; part of this is to avoid
     * simple "multiply-add" algorithm (like JDK String.hashCode()),
     * and add bit of shifting. And other part is to make this
     * non-linear, at least for shorter symbols.
     */

    // JDK uses 31; other fine choices are 33 and 65599, let's use 33
    // as it seems to give fewest collisions for us
    // (see [http://www.cse.yorku.ca/~oz/hash.html] for details)
    private final static int MULT = 33;
    private final static int MULT2 = 65599;
    private final static int MULT3 = 31;

    public int calcHash(int q1)
    {
        int hash = q1 ^ _seed;
        /* 29-Mar-2015, tatu: Earlier used 15 + 9 right shifts, which worked ok
         *    except for one specific problem case: numbers. So needed to make sure
         *    that all 4 least-significant bits participate in hash. Couple of ways
         *    to work it out, but this is the simplest, fast and seems to do ok.
         */
        hash += (hash >>> 16); // to xor hi- and low- 16-bits
        hash ^= (hash << 3); // shuffle back a bit
        hash += (hash >>> 12); // and bit more
        return hash;
    }

    public int calcHash(int q1, int q2)
    {
        // For two quads, let's change algorithm a bit, to spice
        // things up (can do bit more processing anyway)
        int hash = q1;

        hash += (hash >>> 15); // try mixing first and second byte pairs first
        hash ^= (hash >>> 9); // as well as lowest 2 bytes
        hash += (q2 * MULT); // then add second quad
        hash ^= _seed;
        hash += (hash >>> 16); // and shuffle some more
        hash ^= (hash >>> 4);
        hash += (hash << 3);

        return hash;
    }

    public int calcHash(int q1, int q2, int q3)
    { // use same algorithm as multi-byte, tested to work well
        int hash = q1 ^ _seed;
        hash += (hash >>> 9);
        hash *= MULT3;
        hash += q2;
        hash *= MULT;
        hash += (hash >>> 15);
        hash ^= q3;
        // 26-Mar-2015, tatu: As per two-quad case, a short shift seems to help more here
        hash += (hash >>> 4);

        hash += (hash >>> 15);
        hash ^= (hash << 9);

        return hash;
    }

    public int calcHash(int[] q, int qlen)
    {
        if (qlen < 4) {
            throw new IllegalArgumentException();
        }
        /* And then change handling again for "multi-quad" case; mostly
         * to make calculation of collisions less fun. For example,
         * add seed bit later in the game, and switch plus/xor around,
         * use different shift lengths.
         */
        int hash = q[0] ^ _seed;
        hash += (hash >>> 9);
        hash += q[1];
        hash += (hash >>> 15);
        hash *= MULT;
        hash ^= q[2];
        hash += (hash >>> 4);

        for (int i = 3; i < qlen; ++i) {
            int next = q[i];
            next = next ^ (next >> 21);
            hash += next;
        }
        hash *= MULT2;

        // and finally shuffle some more once done
        hash += (hash >>> 19);
        hash ^= (hash << 5);
        return hash;
    }

    /*
    /**********************************************************
    /* Rehashing
    /**********************************************************
     */

    private void rehash()
    {
        // Note: since we'll make copies, no need to unshare, can just mark as such:
        _hashShared = false;

        // And then we can first deal with the main hash area. Since we are expanding
        // linearly (double up), we know there'll be no collisions during this phase.
        final int[] oldHashArea = _hashArea;
        final String[] oldNames = _names;
        final int oldSize = _hashSize;
        final int oldCount = _count;
        final int newSize = oldSize + oldSize;
        final int oldEnd = _spilloverEnd;

        /* 13-Mar-2010, tatu: Let's guard against OOME that could be caused by
         *    large documents with unique (or mostly so) names
         */
        if (newSize > MAX_T_SIZE) {
            nukeSymbols(true);
            return;
        }
        // double up main hash area, but do not expand long-name area:
        _hashArea = new int[oldHashArea.length + (oldSize<<3)];
        _hashSize = newSize;
        _secondaryStart = (newSize << 2); // 4 ints per entry
        _tertiaryStart = _secondaryStart + (_secondaryStart >> 1); // right after secondary
        _tertiaryShift = _calcTertiaryShift(newSize);

        // and simply double up name array
        _names = new String[oldNames.length << 1];
        nukeSymbols(false);

        // Plus we can scan only through the primary hash area, looking for non-empty
        // slots, without worrying about ordering. This should never reduce priority
        // of existing entries: primaries remain primaries; however, due to increased
        // space, secondaries may become primaries etc

        int copyCount = 0;
        int[] q = new int[16];
        for (int offset = 0, end = oldEnd; offset < end; offset += 4) {
            int len = oldHashArea[offset+3];
            if (len == 0) { // empty slot, skip
                continue;
            }
            ++copyCount;
            String name = oldNames[offset>>2];
            switch (len) {
            case 1:
                q[0] = oldHashArea[offset];
                addName(name, q, 1);
                break;
            case 2:
                q[0] = oldHashArea[offset];
                q[1] = oldHashArea[offset+1];
                addName(name, q, 2);
                break;
            case 3:
                q[0] = oldHashArea[offset];
                q[1] = oldHashArea[offset+1];
                q[2] = oldHashArea[offset+2];
                addName(name, q, 3);
                break;
            default:
                if (len > q.length) {
                    q = new int[len];
                }
                // #0 is hash, #1 offset
                int qoff = oldHashArea[offset+1];
                System.arraycopy(oldHashArea, qoff, q, 0, len);
                addName(name, q, len);
                break;
            }
        }

        // Sanity checks: since corruption difficult to detect, assert explicitly
        // with production code
        if (copyCount != oldCount) {
            throw new IllegalStateException("Failed rehash(): old count="+oldCount+", copyCount="+copyCount);
        }
    }

    /**
     * Helper method called to empty all shared symbols, but to leave
     * arrays allocated
     */
    private void nukeSymbols(boolean fill) {
        _count = 0;
        // reset spill-over to empty (starting at 7/8 of hash area)
        _spilloverEnd = _spilloverStart();
        // and long name area to empty, starting immediately after hash area
        _longNameOffset = _hashSize << 3;
        if (fill) {
            Arrays.fill(_hashArea, 0);
            Arrays.fill(_names, null);
        }
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    /**
     * Helper method that calculates start of the spillover area
     */
    private final int _spilloverStart() {
        // we'll need slot at 1.75x of hashSize, but with 4-ints per slot.
        // So basically multiply by 7
        int offset = _hashSize;
        return (offset << 3) - offset;
    }

    protected void _reportTooManyCollisions()
    {
        // First: do not fuzz about small symbol tables; may get balanced by doubling up
        if (_hashSize <= 1024) { // would have spill-over area of 128 entries
            return;
        }
        throw new IllegalStateException("Spill-over slots in symbol table with "+_count
                +" entries, hash area of "+_hashSize+" slots is now full (all "
                +(_hashSize >> 3)+" slots -- suspect a DoS attack based on hash collisions."
                +" You can disable the check via `JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW`");
    }

    static int _calcTertiaryShift(int primarySlots)
    {
        // first: we only get 1/4 of slots of primary, to divide
        int tertSlots = (primarySlots) >> 2;

        // default is for buckets of 4 slots (each 4 ints, i.e. 1 << 4)
        if (tertSlots < 64) {
            return 4;
        }
        if (tertSlots <= 256) { // buckets of 8 slots (up to 256 == 32 x 8)
            return 5;
        }
        if (tertSlots <= 1024) { // buckets of 16 slots (up to 1024 == 64 x 16)
            return 6;
        }
        // and biggest buckets have 32 slots
        return 7;
    }

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Immutable value class used for sharing information as efficiently
     * as possible, by only require synchronization of reference manipulation
     * but not access to contents.
     *
     * @since 2.1
     */
    private final static class TableInfo
    {
        public final int size;
        public final int count;
        public final int tertiaryShift;
        public final int[] mainHash;
        public final String[] names;
        public final int spilloverEnd;
        public final int longNameOffset;

        public TableInfo(int size, int count, int tertiaryShift,
                int[] mainHash, String[] names, int spilloverEnd, int longNameOffset)
        {
            this.size = size;
            this.count = count;
            this.tertiaryShift = tertiaryShift;
            this.mainHash = mainHash;
            this.names = names;
            this.spilloverEnd = spilloverEnd;
            this.longNameOffset = longNameOffset;
        }

        public TableInfo(ByteQuadsCanonicalizer src)
        {
            size = src._hashSize;
            count = src._count;
            tertiaryShift = src._tertiaryShift;
            mainHash = src._hashArea;
            names = src._names;
            spilloverEnd = src._spilloverEnd;
            longNameOffset = src._longNameOffset;
        }

        public static TableInfo createInitial(int sz) {
            int hashAreaSize = sz << 3;
            int tertShift = _calcTertiaryShift(sz);

            return new TableInfo(sz, // hashSize
                    0, // count
                    tertShift,
                    new int[hashAreaSize], // mainHash, 2x slots, 4 ints per slot
                    new String[sz << 1], // names == 2x slots
                    hashAreaSize - sz, // at 7/8 of the total area
                    hashAreaSize // longNameOffset, immediately after main hashes
            );
        }
    }
}
