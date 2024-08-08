// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

import java.lang.ref.SoftReference;

/**
 * Helper entity used to control access to simple buffer recycling scheme used for
 * some encoding, decoding tasks.
 *
 * @see BufferRecycler
 *
 * @since 2.9.2
 *
 * @deprecated Since 2.16 recycling aspects are handled via {@link RecyclerPool}.
 */
@Deprecated
public class BufferRecyclers {
    /**
     * System property that is checked to see if recycled buffers (see {@link BufferRecycler})
     * should be tracked, for purpose of forcing release of all such buffers, typically
     * during major garbage-collection.
     *
     * @since 2.9.6
     */
    public final static String SYSTEM_PROPERTY_TRACK_REUSABLE_BUFFERS
        = "com.azure.json.implementation.jackson.core.util.BufferRecyclers.trackReusableBuffers";

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * Flag that indicates whether {@link BufferRecycler} instances should be tracked.
     */
    private final static ThreadLocalBufferManager _bufferRecyclerTracker;
    static {
        boolean trackReusableBuffers = false;
        try {
            trackReusableBuffers = "true".equals(System.getProperty(SYSTEM_PROPERTY_TRACK_REUSABLE_BUFFERS));
        } catch (SecurityException ignored) {
        }

        _bufferRecyclerTracker = trackReusableBuffers ? ThreadLocalBufferManager.instance() : null;
    }

    /*
    /**********************************************************
    /* BufferRecyclers for parsers, generators
    /**********************************************************
     */

    /**
     * This <code>ThreadLocal</code> contains a {@link java.lang.ref.SoftReference}
     * to a {@link BufferRecycler} used to provide a low-cost
     * buffer recycling between reader and writer instances.
     */
    final protected static ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef = new ThreadLocal<>();

    /**
     * Main accessor to call for accessing possibly recycled {@link BufferRecycler} instance.
     *
     * @return {@link BufferRecycler} to use
     *
     * @deprecated Since 2.16 should use {@link RecyclerPool} abstraction instead
     *   of calling static methods of this class
     */
    @Deprecated // since 2.16
    public static BufferRecycler getBufferRecycler() {
        SoftReference<BufferRecycler> ref = _recyclerRef.get();
        BufferRecycler br = (ref == null) ? null : ref.get();

        if (br == null) {
            br = new BufferRecycler();
            if (_bufferRecyclerTracker != null) {
                ref = _bufferRecyclerTracker.wrapAndTrack(br);
            } else {
                ref = new SoftReference<>(br);
            }
            _recyclerRef.set(ref);
        }
        return br;
    }
}
