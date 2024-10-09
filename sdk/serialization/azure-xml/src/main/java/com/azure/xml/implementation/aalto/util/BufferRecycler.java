// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

/**
 * This is a small utility class, whose main functionality is to allow
 * simple reuse of raw byte/char buffers. It is usually used through
 * <code>ThreadLocal</code> member of the owning class pointing to
 * instance of this class through a <code>SoftReference</code>. The
 * end result is a low-overhead GC-cleanable recycling: hopefully
 * ideal for use by stream readers.
 *<p>
 * Regarding implementation: the key design goal is simplicity; and to
 * that end, different types of buffers are handled separately. While
 * code may look inelegant as a result (would be cleaner to just
 * have generic char[]/byte[] buffer accessors), benefit is that
 * no data structures are needed, just simple references. As long
 * as usage pattern is well known (which it is, for stream readers)
 * this should be highly optimal and robust implementation.
 */
public final class BufferRecycler {
    private volatile char[] mSmallCBuffer = null; // temp buffers
    private volatile char[] mMediumCBuffer = null; // text collector
    private volatile char[] mFullCBuffer = null; // for actual parsing buffer

    private volatile byte[] mFullBBuffer = null;

    public BufferRecycler() {
    }

    // // // Char buffers:

    // // Small buffers, for temporary parsing

    public synchronized char[] getSmallCBuffer(int minSize) {
        char[] result = mSmallCBuffer;
        if (result != null && result.length >= minSize) {
            mSmallCBuffer = null;
            return result;
        }
        return null;
    }

    public synchronized void returnSmallCBuffer(char[] buffer) {
        mSmallCBuffer = buffer;
    }

    // // Medium buffers, for text output collection

    public synchronized char[] getMediumCBuffer(int minSize) {
        char[] result = mMediumCBuffer;
        if (result != null && result.length >= minSize) {
            mMediumCBuffer = null;
            return result;
        }
        return null;
    }

    public synchronized void returnMediumCBuffer(char[] buffer) {
        mMediumCBuffer = buffer;
    }

    // // Full buffers, for parser buffering

    public synchronized char[] getFullCBuffer(int minSize) {
        char[] result = mFullCBuffer;
        if (result != null && result.length >= minSize) {
            mFullCBuffer = null;
            return result;
        }
        return null;
    }

    public synchronized void returnFullCBuffer(char[] buffer) {
        mFullCBuffer = buffer;
    }

    // // // Byte buffers:

    // // Full byte buffers, for byte->char conversion (Readers)

    public synchronized byte[] getFullBBuffer(int minSize) {
        byte[] result = mFullBBuffer;
        if (result != null && result.length >= minSize) {
            mFullBBuffer = null;
            return result;
        }
        return null;
    }

    public synchronized void returnFullBBuffer(byte[] buffer) {
        mFullBBuffer = buffer;
    }
}
