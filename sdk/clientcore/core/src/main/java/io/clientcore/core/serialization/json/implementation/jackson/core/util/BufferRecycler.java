// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.util;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * This is a small utility class, whose main functionality is to allow
 * simple reuse of raw byte/char buffers. It is usually used through
 * <code>ThreadLocal</code> member of the owning class pointing to
 * instance of this class through a <code>SoftReference</code>. The
 * end result is a low-overhead GC-cleanable recycling: hopefully
 * ideal for use by stream readers.
 *<p>
 * Rewritten in 2.10 to be thread-safe (see [jackson-core#479] for details),
 * to not rely on {@code ThreadLocal} access.
 */
public class BufferRecycler {

    /**
     * Buffer used for temporarily concatenating output; used for
     * example when requesting output as byte array.
     */
    public final static int BYTE_WRITE_CONCAT_BUFFER = 2;

    /**
     * Buffer used as input buffer for tokenization for character-based parsers.
     */
    public final static int CHAR_TOKEN_BUFFER = 0;

    /**
     * Buffer used by generators; for byte-backed generators for buffering of
     * {@link String} values to output (before encoding into UTF-8),
     * and for char-backed generators as actual concatenation buffer.
     */
    public final static int CHAR_CONCAT_BUFFER = 1;

    /**
     * Used through {@link TextBuffer}
     * when serializing (databind level {@code ObjectMapper} and
     * {@code ObjectWriter}). In both cases used as segments (and not for whole value),
     * but may result in retention of larger chunks for big content
     * (long text values during parsing; bigger output documents for generation).
     */
    public final static int CHAR_TEXT_BUFFER = 2;

    // Buffer lengths

    private final static int[] BYTE_BUFFER_LENGTHS = new int[] { 8000, 8000, 2000, 2000 };
    private final static int[] CHAR_BUFFER_LENGTHS = new int[] { 4000, 4000, 200, 200 };

    // Note: changed from simple array in 2.10:
    protected final AtomicReferenceArray<byte[]> _byteBuffers;

    // Note: changed from simple array in 2.10:
    protected final AtomicReferenceArray<char[]> _charBuffers;

    /*
     * /**********************************************************
     * /* Construction
     * /**********************************************************
     */

    /**
     * Default constructor used for creating instances of this default
     * implementation.
     */
    public BufferRecycler() {
        this(4, 4);
    }

    /**
     * Alternate constructor to be used by sub-classes, to allow customization
     * of number of low-level buffers in use.
     *
     * @param bbCount Number of {@code byte[]} buffers to allocate
     * @param cbCount Number of {@code char[]} buffers to allocate
     *
     * @since 2.4
     */
    protected BufferRecycler(int bbCount, int cbCount) {
        _byteBuffers = new AtomicReferenceArray<>(bbCount);
        _charBuffers = new AtomicReferenceArray<>(cbCount);
    }

    /*
     * /**********************************************************
     * /* Public API, byte buffers
     * /**********************************************************
     */

    /**
     * @param ix One of <code>READ_IO_BUFFER</code> constants.
     *
     * @return Buffer allocated (possibly recycled)
     */
    public final byte[] allocByteBuffer(int ix) {
        int minSize = BYTE_BUFFER_LENGTHS[ix];
        byte[] buffer = _byteBuffers.getAndSet(ix, null);
        if (buffer == null || buffer.length < minSize) {
            buffer = new byte[minSize];
        }
        return buffer;
    }

    public void releaseByteBuffer(int ix, byte[] buffer) {
        _byteBuffers.set(ix, buffer);
    }

    /*
     * /**********************************************************
     * /* Public API, char buffers
     * /**********************************************************
     */

    public final char[] allocCharBuffer(int ix) {
        return allocCharBuffer(ix, 0);
    }

    public char[] allocCharBuffer(int ix, int minSize) {
        final int DEF_SIZE = CHAR_BUFFER_LENGTHS[ix];
        if (minSize < DEF_SIZE) {
            minSize = DEF_SIZE;
        }
        char[] buffer = _charBuffers.getAndSet(ix, null);
        if (buffer == null || buffer.length < minSize) {
            buffer = new char[minSize];
        }
        return buffer;
    }

    public void releaseCharBuffer(int ix, char[] buffer) {
        _charBuffers.set(ix, buffer);
    }
}
