// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.sym;

import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicReference;

import com.typespec.json.implementation.jackson.core.JsonFactory;
import com.typespec.json.implementation.jackson.core.util.InternCache;

/**
 * This class is a kind of specialized type-safe Map, from char array to
 * String value. Specialization means that in addition to type-safety
 * and specific access patterns (key char array, Value optionally interned
 * String; values added on access if necessary), and that instances are
 * meant to be used concurrently, but by using well-defined mechanisms
 * to obtain such concurrently usable instances. Main use for the class
 * is to store symbol table information for things like compilers and
 * parsers; especially when number of symbols (keywords) is limited.
 *<p>
 * For optimal performance, usage pattern should be one where matches
 * should be very common (especially after "warm-up"), and as with most hash-based
 * maps/sets, that hash codes are uniformly distributed. Also, collisions
 * are slightly more expensive than with HashMap or HashSet, since hash codes
 * are not used in resolving collisions; that is, equals() comparison is
 * done with all symbols in same bucket index.<br>
 * Finally, rehashing is also more expensive, as hash codes are not
 * stored; rehashing requires all entries' hash codes to be recalculated.
 * Reason for not storing hash codes is reduced memory usage, hoping
 * for better memory locality.
 *<p>
 * Usual usage pattern is to create a single "master" instance, and either
 * use that instance in sequential fashion, or to create derived "child"
 * instances, which after use, are asked to return possible symbol additions
 * to master instance. In either case benefit is that symbol table gets
 * initialized so that further uses are more efficient, as eventually all
 * symbols needed will already be in symbol table. At that point no more
 * Symbol String allocations are needed, nor changes to symbol table itself.
 *<p>
 * Note that while individual SymbolTable instances are NOT thread-safe
 * (much like generic collection classes), concurrently used "child"
 * instances can be freely used without synchronization. However, using
 * master table concurrently with child instances can only be done if
 * access to master instance is read-only (i.e. no modifications done).
 */
public final class CharsToNameCanonicalizer
{
    /* If we use "multiply-add" based hash algorithm, this is the multiplier
     * we use.
     *<p>
     * Note that JDK uses 31; but it seems that 33 produces fewer collisions,
     * at least with tests we have.
     */
    public final static int HASH_MULT = 33;

    /**
     * Default initial table size. Shouldn't be miniscule (as there's
     * cost to both array realloc and rehashing), but let's keep
     * it reasonably small. For systems that properly 
     * reuse factories it doesn't matter either way; but when
     * recreating factories often, initial overhead may dominate.
     */
    private static final int DEFAULT_T_SIZE = 64;

    /**
     * Let's not expand symbol tables past some maximum size;
     * this should protected against OOMEs caused by large documents
     * with unique (~= random) names.
     */
    private static final int MAX_T_SIZE = 0x10000; // 64k entries == 256k mem

    /**
     * Let's only share reasonably sized symbol tables. Max size set to 3/4 of 16k;
     * this corresponds to 64k main hash index. This should allow for enough distinct
     * names for almost any case.
     */
    static final int MAX_ENTRIES_FOR_REUSE = 12000;

    /**
     * Also: to thwart attacks based on hash collisions (which may or may not
     * be cheap to calculate), we will need to detect "too long"
     * collision chains.
     * Started with static value of 100 entries for the longest legal chain,
     * but increased in Jackson 2.13 to 150 to work around specific test case.
     *<p>
     * Note: longest chain we have been able to produce without malicious
     * intent has been 38 (with "com.azure.json.implementation.jackson.core.main.TestWithTonsaSymbols");
     * our setting should be reasonable here.
     * 
     * @since 2.1 (changed in 2.13)
     */
    static final int MAX_COLL_CHAIN_LENGTH = 150;

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Sharing of learnt symbols is done by optional linking of symbol
     * table instances with their parents. When parent linkage is
     * defined, and child instance is released (call to <code>release</code>),
     * parent's shared tables may be updated from the child instance.
     */
    final protected CharsToNameCanonicalizer _parent;

    /**
     * Member that is only used by the root table instance: root
     * passes immutable state info child instances, and children
     * may return new state if they add entries to the table.
     * Child tables do NOT use the reference.
     */
    final protected AtomicReference<TableInfo> _tableInfo;

    /**
     * Seed value we use as the base to make hash codes non-static between
     * different runs, but still stable for lifetime of a single symbol table
     * instance.
     * This is done for security reasons, to avoid potential DoS attack via
     * hash collisions.
     * 
     * @since 2.1
     */
    final protected int _seed;

    final protected int _flags;

    /**
     * Whether any canonicalization should be attempted (whether using
     * intern or not.
     *<p>
     * NOTE: non-final since we may need to disable this with overflow.
     */
    protected boolean _canonicalize;

    /*
    /**********************************************************
    /* Actual symbol table data
    /**********************************************************
     */

    /**
     * Primary matching symbols; it's expected most match occur from
     * here.
     */
    protected String[] _symbols;

    /**
     * Overflow buckets; if primary doesn't match, lookup is done
     * from here.
     *<p>
     * Note: Number of buckets is half of number of symbol entries, on
     * assumption there's less need for buckets.
     */
    protected Bucket[] _buckets;

    /**
     * Current size (number of entries); needed to know if and when
     * rehash.
     */
    protected int _size;

    /**
     * Limit that indicates maximum size this instance can hold before
     * it needs to be expanded and rehashed. Calculated using fill
     * factor passed in to constructor.
     */
    protected int _sizeThreshold;

    /**
     * Mask used to get index from hash values; equal to
     * <code>_buckets.length - 1</code>, when _buckets.length is
     * a power of two.
     */
    protected int _indexMask;

    /**
     * We need to keep track of the longest collision list; this is needed
     * both to indicate problems with attacks and to allow flushing for
     * other cases.
     * 
     * @since 2.1
     */
    protected int _longestCollisionList;

    /*
    /**********************************************************
    /* State regarding shared arrays
    /**********************************************************
     */

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
    /* Bit of DoS detection goodness
    /**********************************************************
     */

    /**
     * Lazily constructed structure that is used to keep track of
     * collision buckets that have overflowed once: this is used
     * to detect likely attempts at denial-of-service attacks that
     * uses hash collisions.
     * 
     * @since 2.4
     */
    protected BitSet _overflows;

    /*
    /**********************************************************
    /* Life-cycle: constructors
    /**********************************************************
     */

    /**
     * Main method for constructing a root symbol table instance.
     */
    private CharsToNameCanonicalizer(int seed)
    {
        _parent = null;
        _seed = seed;
        
        // these settings don't really matter for the bootstrap instance
        _canonicalize = true;
        _flags = -1;
        // And we'll also set flags so no copying of buckets is needed:
        _hashShared = false; // doesn't really matter for root instance
        _longestCollisionList = 0;

        _tableInfo = new AtomicReference<TableInfo>(TableInfo.createInitial(DEFAULT_T_SIZE));
        // and actually do NOT assign buffers so we'll find if anyone tried to
        // use root instance
    }

    /**
     * Internal constructor used when creating child instances.
     */
    private CharsToNameCanonicalizer(CharsToNameCanonicalizer parent, int flags, int seed,
            TableInfo parentState)
    {
        _parent = parent;
        _seed = seed;
        _tableInfo = null; // not used by child tables
        _flags = flags;
        _canonicalize = JsonFactory.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(flags);

        // Then copy shared state
        _symbols = parentState.symbols;
        _buckets = parentState.buckets;

        _size = parentState.size;
        _longestCollisionList = parentState.longestCollisionList;

        // Hard-coded fill factor, 75%
        int arrayLen = (_symbols.length);
        _sizeThreshold = _thresholdSize(arrayLen);
        _indexMask =  (arrayLen - 1);

        // Need to make copies of arrays, if/when adding new entries
        _hashShared = true;
    }

    private static int _thresholdSize(int hashAreaSize) { return hashAreaSize - (hashAreaSize >> 2); }

    /*
    /**********************************************************
    /* Life-cycle: factory methods, merging
    /**********************************************************
     */

    /**
     * Method called to create root canonicalizer for a {@link com.typespec.json.implementation.jackson.core.JsonFactory}
     * instance. Root instance is never used directly; its main use is for
     * storing and sharing underlying symbol arrays as needed.
     *
     * @return Root instance to use for constructing new child instances 
     */
    public static CharsToNameCanonicalizer createRoot() {
        // Need to use a variable seed, to thwart hash-collision based attacks.
        // 14-Feb-2017, tatu: not sure it actually helps, at all, since it won't
        //   change mixing or any of the steps. Should likely just remove in future.
        long now = System.currentTimeMillis();
        // ensure it's not 0; and might as well require to be odd so:
        int seed = (((int) now) + ((int) (now >>> 32))) | 1;
        return createRoot(seed);
    }

    protected static CharsToNameCanonicalizer createRoot(int seed) {
        return new CharsToNameCanonicalizer(seed);
    }
    /**
     * "Factory" method; will create a new child instance of this symbol
     * table. It will be a copy-on-write instance, ie. it will only use
     * read-only copy of parent's data, but when changes are needed, a
     * copy will be created.
     *<p>
     * Note: while this method is synchronized, it is generally not
     * safe to both use makeChild/mergeChild, AND to use instance
     * actively. Instead, a separate 'root' instance should be used
     * on which only makeChild/mergeChild are called, but instance itself
     * is not used as a symbol table.
     *
     * @param flags Bit flags of active {@link com.typespec.json.implementation.jackson.core.JsonFactory.Feature}s enabled.
     *
     * @return Actual canonicalizer instance that can be used by a parser
     */
    public CharsToNameCanonicalizer makeChild(int flags) {
        return new CharsToNameCanonicalizer(this, flags, _seed, _tableInfo.get());
    }

    /**
     * Method called by the using code to indicate it is done with this instance.
     * This lets instance merge accumulated changes into parent (if need be),
     * safely and efficiently, and without calling code having to know about parent
     * information.
     */
    public void release() {
        // If nothing has been added, nothing to do
        if (!maybeDirty()) { return; }

        // we will try to merge if child table has new entries
        if (_parent != null && _canonicalize) { // canonicalize set to false if max size was reached
            _parent.mergeChild(new TableInfo(this));
            // Let's also mark this instance as dirty, so that just in
            // case release was too early, there's no corruption of possibly shared data.
            _hashShared = true;
        }
    }

    /**
     * Method that allows contents of child table to potentially be
     * "merged in" with contents of this symbol table.
     *<p>
     * Note that caller has to make sure symbol table passed in is
     * really a child or sibling of this symbol table.
     */
    private void mergeChild(TableInfo childState)
    {
        final int childCount = childState.size;
        TableInfo currState = _tableInfo.get();

        // Should usually grow; but occasionally could also shrink if (but only if)
        // collision list overflow ends up clearing some collision lists.
        if (childCount == currState.size) {
            return;
        }
        // One caveat: let's try to avoid problems with  degenerate cases of documents with
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
    /* Public API, generic accessors:
    /**********************************************************
     */

    /**
     * @return Number of symbol entries contained by this canonicalizer instance
     */
    public int size() {
        if (_tableInfo != null) { // root table
            return _tableInfo.get().size;
        }
        // nope, child table
        return _size;
    }

    /**
     * Method for checking number of primary hash buckets this symbol
     * table uses.
     * 
     * @return number of primary slots table has currently
     */
    public int bucketCount() {  return _symbols.length; }
    public boolean maybeDirty() { return !_hashShared; }
    public int hashSeed() { return _seed; }

    /**
     * Method mostly needed by unit tests; calculates number of
     * entries that are in collision list. Value can be at most
     * ({@link #size} - 1), but should usually be much lower, ideally 0.
     * 
     * @since 2.1
     *
     * @return Number of collisions in the primary hash area
     */
    public int collisionCount() {
        int count = 0;
        
        for (Bucket bucket : _buckets) {
            if (bucket != null) {
                count += bucket.length;
            }
        }
        return count;
    }

    /**
     * Method mostly needed by unit tests; calculates length of the
     * longest collision chain. This should typically be a low number,
     * but may be up to {@link #size} - 1 in the pathological case
     *
     * @return Length of the collision chain
     *
     * @since 2.1
     */
    public int maxCollisionLength() { return _longestCollisionList; }

    /*
    /**********************************************************
    /* Public API, accessing symbols:
    /**********************************************************
     */

    public String findSymbol(char[] buffer, int start, int len, int h)
    {
        if (len < 1) { // empty Strings are simplest to handle up front
            return "";
        }
        if (!_canonicalize) { // [JACKSON-259]
            return new String(buffer, start, len);
        }

        /* Related to problems with sub-standard hashing (somewhat
         * relevant for collision attacks too), let's try little
         * bit of shuffling to improve hash codes.
         * (note, however, that this can't help with full collisions)
         */
        int index = _hashToIndex(h);
        String sym = _symbols[index];

        // Optimal case; checking existing primary symbol for hash index:
        if (sym != null) {
            // Let's inline primary String equality checking:
            if (sym.length() == len) {
                int i = 0;
                while (sym.charAt(i) == buffer[start+i]) {
                    // Optimal case; primary match found
                    if (++i == len) {
                        return sym;
                    }
                }
            }
            Bucket b = _buckets[index>>1];
            if (b != null) {
                sym = b.has(buffer, start, len);
                if (sym != null) {
                    return sym;
                }
                sym = _findSymbol2(buffer, start, len, b.next);
                if (sym != null) {
                    return sym;
                }
            }
        }
        return _addSymbol(buffer, start, len, h, index);
    }

    private String _findSymbol2(char[] buffer, int start, int len, Bucket b) {
        while (b != null) {
            String sym = b.has(buffer, start, len);
            if (sym != null) {
                return sym;
            }
            b = b.next;
        }
        return null;
    }

    private String _addSymbol(char[] buffer, int start, int len, int h, int index)
    {
        if (_hashShared) { //need to do copy-on-write?
            copyArrays();
            _hashShared = false;
        } else if (_size >= _sizeThreshold) { // Need to expand?
            rehash();
            // Need to recalc hash; rare occurrence (index mask has been
             // recalculated as part of rehash)
            index = _hashToIndex(calcHash(buffer, start, len));
        }

        String newSymbol = new String(buffer, start, len);
        if (JsonFactory.Feature.INTERN_FIELD_NAMES.enabledIn(_flags)) {
            newSymbol = InternCache.instance.intern(newSymbol);
        }
        ++_size;
        // Ok; do we need to add primary entry, or a bucket?
        if (_symbols[index] == null) {
            _symbols[index] = newSymbol;
        } else {
            final int bix = (index >> 1);
            Bucket newB = new Bucket(newSymbol, _buckets[bix]);
            int collLen = newB.length;
            if (collLen > MAX_COLL_CHAIN_LENGTH) {
                // 23-May-2014, tatu: Instead of throwing an exception right away,
                //    let's handle in bit smarter way.
                _handleSpillOverflow(bix, newB, index);
            } else {
                _buckets[bix] = newB;
                _longestCollisionList = Math.max(collLen, _longestCollisionList);
            }
        }
        return newSymbol;
    }

    /**
     * Method called when an overflow bucket has hit the maximum expected length:
     * this may be a case of DoS attack. Deal with it based on settings by either
     * clearing up bucket (to avoid indefinite expansion) or throwing exception.
     * Currently the first overflow for any single bucket DOES NOT throw an exception,
     * only second time (per symbol table instance)
     */
    private void _handleSpillOverflow(int bucketIndex, Bucket newBucket, int mainIndex)
    {
        if (_overflows == null) {
            _overflows = new BitSet();
            _overflows.set(bucketIndex);
        } else {
            if (_overflows.get(bucketIndex)) {
                // Has happened once already for this bucket index, so probably not coincidental...
                if (JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.enabledIn(_flags)) {
                    _reportTooManyCollisions(MAX_COLL_CHAIN_LENGTH);
                }
                // but even if we don't fail, we will stop canonicalizing as safety measure
                // (so as not to cause problems with PermGen)
                _canonicalize = false;
            } else {
                _overflows.set(bucketIndex);
            }
        }

        // regardless, if we get this far, clear up the bucket, adjust size appropriately.
        _symbols[mainIndex] = newBucket.symbol;
        _buckets[bucketIndex] = null;
        // newBucket contains new symbol; but we will
        _size -= (newBucket.length);
        // we could calculate longest; but for now just mark as invalid
        _longestCollisionList = -1;
    }

    /**
     * Helper method that takes in a "raw" hash value, shuffles it as necessary,
     * and truncates to be used as the index.
     *
     * @param rawHash Raw hash value to use for calculating index
     *
     * @return Index value calculated
     */
    public int _hashToIndex(int rawHash) {
        // doing these seems to help a bit
        rawHash += (rawHash >>> 15);
        rawHash ^= (rawHash << 7);
        rawHash += (rawHash >>> 3);
        return (rawHash & _indexMask);
    }

    /**
     * Implementation of a hashing method for variable length
     * Strings. Most of the time intention is that this calculation
     * is done by caller during parsing, not here; however, sometimes
     * it needs to be done for parsed "String" too.
     *
     * @param buffer Input buffer that contains name to decode
     * @param start Pointer to the first character of the name
     * @param len Length of String; has to be at least 1 (caller guarantees)
     *
     * @return Hash code calculated
     */
    public int calcHash(char[] buffer, int start, int len) {
        int hash = _seed;
        for (int i = start, end = start+len; i < end; ++i) {
            hash = (hash * HASH_MULT) + (int) buffer[i];
        }
        // NOTE: shuffling, if any, is done in 'findSymbol()', not here:
        return (hash == 0) ? 1 : hash;
    }

    public int calcHash(String key)
    {
        final int len = key.length();
        
        int hash = _seed;
        for (int i = 0; i < len; ++i) {
            hash = (hash * HASH_MULT) + (int) key.charAt(i);
        }
        // NOTE: shuffling, if any, is done in 'findSymbol()', not here:
        return (hash == 0) ? 1 : hash;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Method called when copy-on-write is needed; generally when first
     * change is made to a derived symbol table.
     */
    private void copyArrays() {
        final String[] oldSyms = _symbols;
        _symbols = Arrays.copyOf(oldSyms, oldSyms.length);
        final Bucket[] oldBuckets = _buckets;
        _buckets = Arrays.copyOf(oldBuckets, oldBuckets.length);
    }

    /**
     * Method called when size (number of entries) of symbol table grows
     * so big that load factor is exceeded. Since size has to remain
     * power of two, arrays will then always be doubled. Main work
     * is really redistributing old entries into new String/Bucket
     * entries.
     */
    private void rehash() {
        final int size = _symbols.length;
        int newSize = size + size;

        /* 12-Mar-2010, tatu: Let's actually limit maximum size we are
         *    prepared to use, to guard against OOME in case of unbounded
         *    name sets (unique [non-repeating] names)
         */
        if (newSize > MAX_T_SIZE) {
            // If this happens, there's no point in either growing or shrinking hash areas.
            // Rather, let's just cut our losses and stop canonicalizing.
            _size = 0;
            _canonicalize = false;
            // in theory, could just leave these as null, but...
            _symbols = new String[DEFAULT_T_SIZE];
            _buckets = new Bucket[DEFAULT_T_SIZE>>1];
            _indexMask = DEFAULT_T_SIZE-1;
            _hashShared = false;
            return;
        }

        final String[] oldSyms = _symbols;
        final Bucket[] oldBuckets = _buckets;
        _symbols = new String[newSize];
        _buckets = new Bucket[newSize >> 1];
        // Let's update index mask, threshold, now (needed for rehashing)
        _indexMask = newSize - 1;
        _sizeThreshold = _thresholdSize(newSize);

        int count = 0; // let's do sanity check

        // Need to do two loops, unfortunately, since spill-over area is
        // only half the size:
        int maxColl = 0;
        for (int i = 0; i < size; ++i) {
            String symbol = oldSyms[i];
            if (symbol != null) {
                ++count;
                int index = _hashToIndex(calcHash(symbol));
                if (_symbols[index] == null) {
                    _symbols[index] = symbol;
                } else {
                    int bix = (index >> 1);
                    Bucket newB = new Bucket(symbol, _buckets[bix]);
                    _buckets[bix] = newB;
                    maxColl = Math.max(maxColl, newB.length);
                }
            }
        }

        final int bucketSize = (size >> 1);
        for (int i = 0; i < bucketSize; ++i) {
            Bucket b = oldBuckets[i];
            while (b != null) {
                ++count;
                String symbol = b.symbol;
                int index = _hashToIndex(calcHash(symbol));
                if (_symbols[index] == null) {
                    _symbols[index] = symbol;
                } else {
                    int bix = (index >> 1);
                    Bucket newB = new Bucket(symbol, _buckets[bix]);
                    _buckets[bix] = newB;
                    maxColl = Math.max(maxColl, newB.length);
                }
                b = b.next;
            }
        }
        _longestCollisionList = maxColl;
        _overflows = null;

        if (count != _size) {
            throw new IllegalStateException(String.format(
                    "Internal error on SymbolTable.rehash(): had %d entries; now have %d",
                    _size, count));
        }
    }

    /**
     * @param maxLen Maximum allowed length of collision chain
     *
     * @since 2.1
     */
    protected void _reportTooManyCollisions(int maxLen) {
        throw new IllegalStateException("Longest collision chain in symbol table (of size "+_size
                +") now exceeds maximum, "+maxLen+" -- suspect a DoS attack based on hash collisions");
    }

    // since 2.10, for tests only
    /**
     * Diagnostics method that will verify that internal data structures are consistent;
     * not meant as user-facing method but only for test suites and possible troubleshooting.
     *
     * @since 2.10
     */
    protected void verifyInternalConsistency() {
        int count = 0;
        final int size = _symbols.length;

        for (int i = 0; i < size; ++i) {
            String symbol = _symbols[i];
            if (symbol != null) {
                ++count;
            }
        }

        final int bucketSize = (size >> 1);
        for (int i = 0; i < bucketSize; ++i) {
            for (Bucket b = _buckets[i]; b != null; b = b.next) {
                ++count;
            }
        }
        if (count != _size) {
            throw new IllegalStateException(String.format("Internal error: expected internal size %d vs calculated count %d",
                    _size, count));
        }
    }

    // For debugging, comment out
    /*
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        int primaryCount = 0;
        for (String s : _symbols) {
            if (s != null) ++primaryCount;
        }
        
        sb.append("[BytesToNameCanonicalizer, size: ");
        sb.append(_size);
        sb.append('/');
        sb.append(_symbols.length);
        sb.append(", ");
        sb.append(primaryCount);
        sb.append('/');
        sb.append(_size - primaryCount);
        sb.append(" coll; avg length: ");

        // Average length: minimum of 1 for all (1 == primary hit);
        // and then 1 per each traversal for collisions/buckets
        //int maxDist = 1;
        int pathCount = _size;
        for (Bucket b : _buckets) {
            if (b != null) {
                int spillLen = b.length;
                for (int j = 1; j <= spillLen; ++j) {
                    pathCount += j;
                }
            }
        }
        double avgLength;

        if (_size == 0) {
            avgLength = 0.0;
        } else {
            avgLength = (double) pathCount / (double) _size;
        }
        // let's round up a bit (two 2 decimal places)
        //avgLength -= (avgLength % 0.01);

        sb.append(avgLength);
        sb.append(']');
        return sb.toString();
    }
*/

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * This class is a symbol table entry. Each entry acts as a node
     * in a linked list.
     */
    static final class Bucket
    {
        public final String symbol;
        public final Bucket next;
        public final int length;

        public Bucket(String s, Bucket n) {
            symbol = s;
            next = n;
            length = (n == null) ? 1 : n.length+1;
        }

        public String has(char[] buf, int start, int len) {
            if (symbol.length() != len) {
                return null;
            }
            int i = 0;
            do {
                if (symbol.charAt(i) != buf[start+i]) {
                    return null;
                }
            } while (++i < len);
            return symbol;
        }
    }

    /**
     * Immutable value class used for sharing information as efficiently
     * as possible, by only require synchronization of reference manipulation
     * but not access to contents.
     * 
     * @since 2.8.7
     */
    private final static class TableInfo
    {
        final int size;
        final int longestCollisionList;
        final String[] symbols;
        final Bucket[] buckets;

        public TableInfo(int size, int longestCollisionList,
                String[] symbols, Bucket[] buckets)
        {
            this.size = size;
            this.longestCollisionList = longestCollisionList;
            this.symbols = symbols;
            this.buckets = buckets;
        }

        public TableInfo(CharsToNameCanonicalizer src)
        {
            this.size = src._size;
            this.longestCollisionList = src._longestCollisionList;
            this.symbols = src._symbols;
            this.buckets = src._buckets;
        }

        public static TableInfo createInitial(int sz) {
            return new TableInfo(0,
                    0, // longestCollisionList
                    new String[sz], new Bucket[sz >> 1]);
        }
    }
}
