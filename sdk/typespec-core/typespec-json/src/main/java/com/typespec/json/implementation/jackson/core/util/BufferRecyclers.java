// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

import java.lang.ref.SoftReference;

import com.typespec.json.implementation.jackson.core.io.JsonStringEncoder;

/**
 * Helper entity used to control access to simple buffer recyling scheme used for
 * some encoding, decoding tasks.
 *
 * @see BufferRecycler
 *
 * @since 2.9.2
 */
public class BufferRecyclers
{
    /**
     * System property that is checked to see if recycled buffers (see {@link BufferRecycler})
     * should be tracked, for purpose of forcing release of all such buffers, typically
     * during major classloading.
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
        } catch (SecurityException e) { }

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
    final protected static ThreadLocal<SoftReference<BufferRecycler>> _recyclerRef
        = new ThreadLocal<SoftReference<BufferRecycler>>();

    /**
     * Main accessor to call for accessing possibly recycled {@link BufferRecycler} instance.
     *
     * @return {@link BufferRecycler} to use
     */
    public static BufferRecycler getBufferRecycler()
    {
        SoftReference<BufferRecycler> ref = _recyclerRef.get();
        BufferRecycler br = (ref == null) ? null : ref.get();

        if (br == null) {
            br = new BufferRecycler();
            if (_bufferRecyclerTracker != null) {
                ref = _bufferRecyclerTracker.wrapAndTrack(br);
            } else {
                ref = new SoftReference<BufferRecycler>(br);
            }
            _recyclerRef.set(ref);
        }
        return br;
    }

    /**
     * Specialized method that will release all recycled {@link BufferRecycler} if
     * (and only if) recycler tracking has been enabled
     * (see {@link #SYSTEM_PROPERTY_TRACK_REUSABLE_BUFFERS}).
     * This method is usually called on shutdown of the container like Application Server
     * to ensure that no references are reachable via {@link ThreadLocal}s as this may cause
     * unintentional retention of sizable amounts of memory. It may also be called regularly
     * if GC for some reason does not clear up {@link SoftReference}s aggressively enough.
     *
     * @return Number of buffers released, if tracking enabled (zero or more); -1 if tracking not enabled.
     *
     * @since 2.9.6
     */
    public static int releaseBuffers() {
        if (_bufferRecyclerTracker != null) {
            return _bufferRecyclerTracker.releaseBuffers();
        }
        return -1;
    }

    /*
    /**********************************************************************
    /* Obsolete things re-introduced in 2.12.5 after accidental direct
    /* removal from 2.10.0
    /**********************************************************************
     */

    /**
     * Not to be used any more: call {@link JsonStringEncoder#getInstance()} instead.
     *
     * @return {@code JsonStringEncoder} instance to use.
     *
     * @deprecated Since 2.10 (note: was accidentally removed but reintroduced as deprecated
     *    in 2.12.5, to be removed from 3.0)
     */
    @Deprecated
    public static JsonStringEncoder getJsonStringEncoder() {
        return JsonStringEncoder.getInstance();
    }

    /**
     * Not to be used any more: call {@link JsonStringEncoder#getInstance()} (and then
     * {@code encodeAsUTF8()}) instead.
     *
     * @param text String to encode
     * @return String encoded as UTF-8 bytes.
     *
     * @deprecated Since 2.10 (note: was accidentally removed but reintroduced as deprecated
     *    in 2.12.5, to be removed from 3.0)
     */
    @Deprecated
    public static byte[] encodeAsUTF8(String text) {
        return JsonStringEncoder.getInstance().encodeAsUTF8(text);
    }

    /**
     * Not to be used any more: call {@link JsonStringEncoder#getInstance()} (and then
     * {@code quoteAsString()}) instead.
     *
     * @param rawText String to quote
     *
     * @return Quoted text as {@code char[]}
     *
     * @deprecated Since 2.10 (note: was accidentally removed but reintroduced as deprecated
     *    in 2.12.5, to be removed from 3.0)
     */
    @Deprecated
    public static char[] quoteAsJsonText(String rawText) {
        return JsonStringEncoder.getInstance().quoteAsString(rawText);
    }

    /**
     * Not to be used any more: call {@link JsonStringEncoder#getInstance()} (and then
     * {@code quoteAsString()}) instead.
     *
     * @param input Textual content to quote
     * @param output Builder to append quoted content
     *
     * @deprecated Since 2.10 (note: was accidentally removed but reintroduced as deprecated
     *    in 2.12.5, to be removed from 3.0)
     */
    @Deprecated
    public static void quoteAsJsonText(CharSequence input, StringBuilder output) {
        JsonStringEncoder.getInstance().quoteAsString(input, output);
    }

    /**
     * Not to be used any more: call {@link JsonStringEncoder#getInstance()} (and then
     * {@code quoteAsUTF8()}) instead.
     *
     * @param rawText String to quote
     *
     * @return Quoted text as {@code byte[]}
     *
     * @deprecated Since 2.10 (note: was accidentally removed but reintroduced as deprecated
     *    in 2.12.5, to be removed from 3.0)
     */
    @Deprecated
    public static byte[] quoteAsJsonUTF8(String rawText) {
        return JsonStringEncoder.getInstance().quoteAsUTF8(rawText);
    }
}
