// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * For issue [jackson-core#400] We keep a separate Set of all SoftReferences to BufferRecyclers
 * which are (also) referenced using `ThreadLocals`.
 * We do this to be able to release them (dereference) in `releaseBuffers()` and `shutdown()`
 * method to reduce heap consumption during hot reloading of services where otherwise
 * {@link ClassLoader} would have dangling reference via {@link ThreadLocal}s.
 * When gc clears a SoftReference, it puts it on a newly introduced referenceQueue.
 * We use this queue to release the inactive SoftReferences from the Set.
 *
 * @since 2.9.6
 */
class ThreadLocalBufferManager {

    /**
     * A set of all SoftReferences to all BufferRecyclers to be able to release them on shutdown.
     * 'All' means the ones created by this class, in this classloader.
     * There may be more from other classloaders.
     * We use a HashSet to have quick O(1) add and remove operations.
     *<p>
     * NOTE: assumption is that {@link SoftReference} has its {@code equals()} and
     * {@code hashCode()} implementations defined so that they use object identity, so
     * we do not need to use something like {@link IdentityHashMap}
     */
    private final Map<SoftReference<BufferRecycler>, Boolean> _trackedRecyclers = new ConcurrentHashMap<>();

    /**
     * Queue where gc will put just-cleared SoftReferences, previously referencing BufferRecyclers.
     * We use it to remove the cleared softRefs from the above set.
     */
    private final ReferenceQueue<BufferRecycler> _refQueue = new ReferenceQueue<>();

    /*
     * /**********************************************************
     * /* Public API
     * /**********************************************************
     */

    /**
     * Returns the lazily initialized singleton instance
     */
    public static ThreadLocalBufferManager instance() {
        return ThreadLocalBufferManagerHolder.manager;
    }

    public SoftReference<BufferRecycler> wrapAndTrack(BufferRecycler br) {
        SoftReference<BufferRecycler> newRef;
        newRef = new SoftReference<>(br, _refQueue);
        // also retain softRef to br in a set to be able to release it on shutdown
        _trackedRecyclers.put(newRef, true);
        // gc may have cleared one or more SoftRefs, clean them up to avoid a memleak
        removeSoftRefsClearedByGc();
        return newRef;
    }

    /*
     * /**********************************************************
     * /* Internal methods
     * /**********************************************************
     */

    /**
     * Remove cleared (inactive) SoftRefs from our set. Gc may have cleared one or more,
     * and made them inactive. We minimize contention by keeping synchronized sections short:
     * the poll/remove methods
     */
    private void removeSoftRefsClearedByGc() {
        SoftReference<?> clearedSoftRef;
        while ((clearedSoftRef = (SoftReference<?>) _refQueue.poll()) != null) {
            // uses reference-equality, quick, and O(1) removal by HashSet
            _trackedRecyclers.remove(clearedSoftRef);
        }
    }

    /**
     * ThreadLocalBufferManagerHolder uses the thread-safe initialize-on-demand, holder class idiom that implicitly
     * incorporates lazy initialization by declaring a static variable within a static Holder inner class
     */
    private static final class ThreadLocalBufferManagerHolder {
        static final ThreadLocalBufferManager manager = new ThreadLocalBufferManager();
    }
}
