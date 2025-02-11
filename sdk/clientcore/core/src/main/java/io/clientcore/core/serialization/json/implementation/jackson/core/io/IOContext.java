// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.io;

import io.clientcore.core.serialization.json.implementation.jackson.core.util.BufferRecycler;
import io.clientcore.core.serialization.json.implementation.jackson.core.util.TextBuffer;

/**
 * To limit number of configuration and state objects to pass, all
 * contextual objects that need to be passed by the factory to
 * readers and writers are combined under this object. One instance
 * is created for each reader and writer.
 *<p>
 * NOTE: non-final since 2.4, to allow sub-classing.
 */
public class IOContext {
    /*
     * /**********************************************************************
     * /* Configuration
     * /**********************************************************************
     */

    /**
     * Reference to the source object, which can be used for displaying
     * location information
     */
    protected final ContentReference _contentReference;

    /*
     * /**********************************************************************
     * /* Buffer handling, recycling
     * /**********************************************************************
     */

    /**
     * Recycler used for actual allocation/deallocation/reuse
     */
    protected final BufferRecycler _bufferRecycler;

    /**
     * Reference to the buffer allocated for tokenization purposes,
     * in which character input is read, and from which it can be
     * further returned.
     */
    protected char[] _tokenCBuffer;

    /**
     * Reference to the buffer allocated for buffering it for
     * output, before being encoded: generally this means concatenating
     * output, then encoding when buffer fills up.
     */
    protected char[] _concatCBuffer;

    /*
     * /**********************************************************************
     * /* Life-cycle
     * /**********************************************************************
     */

    /**
     * Main constructor to use.
     *
     * @param br BufferRecycler to use, if any ({@code null} if none)
     * @param contentRef Input source reference for location reporting
     * @since 2.13
     */
    public IOContext(BufferRecycler br, ContentReference contentRef) {
        _bufferRecycler = br;
        _contentReference = contentRef;
    }

    /**
     * Accessor for getting (some) information about input source, mostly
     * usable for error reporting purposes.
     *
     * @return Reference to input source
     *
     * @since 2.13
     */
    public ContentReference contentReference() {
        return _contentReference;
    }

    /*
     * /**********************************************************************
     * /* Public API, buffer management
     * /**********************************************************************
     */

    public TextBuffer constructTextBuffer() {
        return new TextBuffer(_bufferRecycler);
    }

    public char[] allocTokenBuffer() {
        _verifyAlloc(_tokenCBuffer);
        return (_tokenCBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CHAR_TOKEN_BUFFER));
    }

    public char[] allocConcatBuffer() {
        _verifyAlloc(_concatCBuffer);
        return (_concatCBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CHAR_CONCAT_BUFFER));
    }

    public void releaseTokenBuffer(char[] buf) {
        if (buf != null) {
            _verifyRelease(buf, _tokenCBuffer);
            _tokenCBuffer = null;
            _bufferRecycler.releaseCharBuffer(BufferRecycler.CHAR_TOKEN_BUFFER, buf);
        }
    }

    public void releaseConcatBuffer(char[] buf) {
        if (buf != null) {
            // 14-Jan-2014, tatu: Let's actually allow upgrade of the original buffer.
            _verifyRelease(buf, _concatCBuffer);
            _concatCBuffer = null;
            _bufferRecycler.releaseCharBuffer(BufferRecycler.CHAR_CONCAT_BUFFER, buf);
        }
    }

    /*
     * /**********************************************************************
     * /* Internal helpers
     * /**********************************************************************
     */

    protected final void _verifyAlloc(Object buffer) {
        if (buffer != null) {
            throw new IllegalStateException("Trying to call same allocXxx() method second time");
        }
    }

    protected final void _verifyRelease(char[] toRelease, char[] src) {
        // 07-Mar-2016, tatu: As per [core#255], only prevent shrinking of buffer
        if ((toRelease != src) && (toRelease.length < src.length)) {
            throw wrongBuf();
        }
    }

    private IllegalArgumentException wrongBuf() {
        // sanity check failed; trying to return different, smaller buffer.
        return new IllegalArgumentException("Trying to release buffer smaller than original");
    }
}
