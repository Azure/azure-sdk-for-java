// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.io;

import com.azure.json.implementation.jackson.core.ErrorReportConfiguration;
import com.azure.json.implementation.jackson.core.JsonEncoding;
import com.azure.json.implementation.jackson.core.StreamReadConstraints;
import com.azure.json.implementation.jackson.core.StreamWriteConstraints;
import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import com.azure.json.implementation.jackson.core.util.ReadConstrainedTextBuffer;
import com.azure.json.implementation.jackson.core.util.TextBuffer;

/**
 * To limit number of configuration and state objects to pass, all
 * contextual objects that need to be passed by the factory to
 * readers and writers are combined under this object. One instance
 * is created for each reader and writer.
 *<p>
 * NOTE: non-final since 2.4, to allow sub-classing.
 */
public class IOContext implements AutoCloseable {
    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Reference to the source object, which can be used for displaying
     * location information
     */
    protected final ContentReference _contentReference;

    /**
     * Old, deprecated "raw" reference to input source.
     *
     * @deprecated Since 2.13, use {@link #_contentReference} instead
     */
    @Deprecated
    protected final Object _sourceRef;

    /**
     * Encoding used by the underlying stream, if known.
     */
    protected JsonEncoding _encoding;

    /**
     * Flag that indicates whether underlying input/output source/target
     * object is fully managed by the owner of this context (parser or
     * generator). If true, it is, and is to be closed by parser/generator;
     * if false, calling application has to do closing (unless auto-closing
     * feature is enabled for the parser/generator in question; in which
     * case it acts like the owner).
     */
    protected final boolean _managedResource;

    /*
    /**********************************************************************
    /* Buffer handling, recycling
    /**********************************************************************
     */

    /**
     * Recycler used for actual allocation/deallocation/reuse
     */
    protected final BufferRecycler _bufferRecycler;

    /**
     * Flag that indicates whether this context instance should release
     * configured {@code _bufferRecycler} or not: if it does, it needs to call
     * (via {@link BufferRecycler#releaseToPool()} when closed; if not,
     * should do nothing (recycler life-cycle is externally managed)
     *
     * @since 2.17
     */
    protected boolean _releaseRecycler = true;

    /**
     * @since 2.15
     */
    protected final StreamReadConstraints _streamReadConstraints;

    /**
     * @since 2.16
     */
    protected final StreamWriteConstraints _streamWriteConstraints;

    /**
     * @since 2.16
     */
    protected final ErrorReportConfiguration _errorReportConfiguration;

    /**
     * Reference to the allocated I/O buffer for low-level input reading,
     * if any allocated.
     */
    protected byte[] _readIOBuffer;

    /**
     * Reference to the allocated I/O buffer used for low-level
     * encoding-related buffering.
     */
    protected byte[] _writeEncodingBuffer;

    /**
     * Reference to the buffer allocated for temporary use with
     * base64 encoding or decoding.
     */
    protected byte[] _base64Buffer;

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

    /**
     * Reference temporary buffer Parser instances need if calling
     * app decides it wants to access name via 'getTextCharacters' method.
     * Regular text buffer can not be used as it may contain textual
     * representation of the value token.
     */
    protected char[] _nameCopyBuffer;

    private boolean _closed = false;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    /**
     * Main constructor to use.
     *
     * @param src constraints for streaming reads
     * @param swc constraints for streaming writes
     * @param br BufferRecycler to use, if any ({@code null} if none)
     * @param contentRef Input source reference for location reporting
     * @param managedResource Whether input source is managed (owned) by Jackson library
     * @param erc Error report configuration to use
     *
     * @since 2.16
     */
    public IOContext(StreamReadConstraints src, StreamWriteConstraints swc, ErrorReportConfiguration erc,
        BufferRecycler br, ContentReference contentRef, boolean managedResource) {
        _streamReadConstraints = src;
        _streamWriteConstraints = swc;
        _errorReportConfiguration = erc;
        _bufferRecycler = br;
        _contentReference = contentRef;
        _sourceRef = contentRef.getRawContent();
        _managedResource = managedResource;
    }

    /**
     * Deprecated legacy constructor.
     *
     * @param src constraints for streaming reads
     * @param br BufferRecycler to use, if any ({@code null} if none)
     * @param contentRef Input source reference for location reporting
     * @param managedResource Whether input source is managed (owned) by Jackson library
     *
     * @since 2.15
     * @deprecated Since 2.16. Use {@link #IOContext(StreamReadConstraints, StreamWriteConstraints, 
     * ErrorReportConfiguration, BufferRecycler, ContentReference, boolean)} instead.
     */
    @Deprecated
    public IOContext(StreamReadConstraints src, BufferRecycler br, ContentReference contentRef,
        boolean managedResource) {
        this(src, StreamWriteConstraints.defaults(), ErrorReportConfiguration.defaults(), br, contentRef,
            managedResource);
    }

    /**
     * Deprecated legacy constructor.
     *
     * @param br BufferRecycler to use, if any ({@code null} if none)
     * @param contentRef Input source reference for location reporting
     * @param managedResource Whether input source is managed (owned) by Jackson library
     *
     * @since 2.13
     * @deprecated Since 2.15. Use {@link #IOContext(StreamReadConstraints, StreamWriteConstraints, 
     * ErrorReportConfiguration, BufferRecycler, ContentReference, boolean)} instead.
     */
    @Deprecated // since 2.15
    public IOContext(BufferRecycler br, ContentReference contentRef, boolean managedResource) {
        this(StreamReadConstraints.defaults(), StreamWriteConstraints.defaults(), ErrorReportConfiguration.defaults(),
            br, contentRef, managedResource);
    }

    @Deprecated // since 2.13
    public IOContext(BufferRecycler br, Object rawContent, boolean managedResource) {
        this(br, ContentReference.rawReference(rawContent), managedResource);
    }

    /**
     * Method to call to prevent {@link #_bufferRecycler} release upon
     * {@link #close()}: called when {@link #_bufferRecycler} life-cycle is
     * externally managed.
     *
     * @since 2.17
     */
    public IOContext markBufferRecyclerReleased() {
        _releaseRecycler = false;
        return this;
    }

    /*
    /**********************************************************************
    /* Public API, accessors
    /**********************************************************************
     */

    /**
     * @return constraints for streaming reads
     * @since 2.15
     */
    public StreamReadConstraints streamReadConstraints() {
        return _streamReadConstraints;
    }

    /**
     * @return constraints for streaming writes
     * @since 2.16
     */
    public StreamWriteConstraints streamWriteConstraints() {
        return _streamWriteConstraints;
    }

    /**
     * @return Configured {@link ErrorReportConfiguration}, containing configured values for 
     * handling error reporting.
     *
     * @since 2.16
     */
    public ErrorReportConfiguration errorReportConfiguration() {
        return _errorReportConfiguration;
    }

    public void setEncoding(JsonEncoding enc) {
        _encoding = enc;
    }

    public IOContext withEncoding(JsonEncoding enc) {
        _encoding = enc;
        return this;
    }

    public JsonEncoding getEncoding() {
        return _encoding;
    }

    public boolean isResourceManaged() {
        return _managedResource;
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

    /**
     * @deprecated Since 2.13, use {@link #contentReference()} instead
     * @return "Raw" source reference
     */
    @Deprecated
    public Object getSourceReference() {
        return _sourceRef;
    }

    // @since 2.17
    public BufferRecycler bufferRecycler() {
        return _bufferRecycler;
    }

    /*
    /**********************************************************************
    /* Public API, buffer management
    /**********************************************************************
     */

    public TextBuffer constructTextBuffer() {
        return new TextBuffer(_bufferRecycler);
    }

    public TextBuffer constructReadConstrainedTextBuffer() {
        return new ReadConstrainedTextBuffer(_streamReadConstraints, _bufferRecycler);
    }

    /**
     * Method for recycling or allocation byte buffer of "read I/O" type.
     *<p>
     * Note: the method can only be called once during its life cycle.
     * This is to protect against accidental sharing.
     *
     * @return Allocated or recycled byte buffer
     */
    public byte[] allocReadIOBuffer() {
        _verifyAlloc(_readIOBuffer);
        return (_readIOBuffer = _bufferRecycler.allocByteBuffer(BufferRecycler.BYTE_READ_IO_BUFFER));
    }

    /**
     * Variant of {@link #allocReadIOBuffer()} that specifies smallest acceptable
     * buffer size.
     *
     * @param minSize Minimum size of the buffer to recycle or allocate
     *
     * @return Allocated or recycled byte buffer
     *
     * @since 2.4
     */
    public byte[] allocReadIOBuffer(int minSize) {
        _verifyAlloc(_readIOBuffer);
        return (_readIOBuffer = _bufferRecycler.allocByteBuffer(BufferRecycler.BYTE_READ_IO_BUFFER, minSize));
    }

    /**
     * Method for recycling or allocation byte buffer of "write encoding" type.
     *<p>
     * Note: the method can only be called once during its life cycle.
     * This is to protect against accidental sharing.
     *
     * @return Allocated or recycled byte buffer
     */
    public byte[] allocWriteEncodingBuffer() {
        _verifyAlloc(_writeEncodingBuffer);
        return (_writeEncodingBuffer = _bufferRecycler.allocByteBuffer(BufferRecycler.BYTE_WRITE_ENCODING_BUFFER));
    }

    /**
     * Variant of {@link #allocWriteEncodingBuffer()} that specifies smallest acceptable
     * buffer size.
     *
     * @param minSize Minimum size of the buffer to recycle or allocate
     *
     * @return Allocated or recycled byte buffer
     *
     * @since 2.4
     */
    public byte[] allocWriteEncodingBuffer(int minSize) {
        _verifyAlloc(_writeEncodingBuffer);
        return (_writeEncodingBuffer
            = _bufferRecycler.allocByteBuffer(BufferRecycler.BYTE_WRITE_ENCODING_BUFFER, minSize));
    }

    /**
     * Method for recycling or allocation byte buffer of "base 64 encode/decode" type.
     *<p>
     * Note: the method can only be called once during its life cycle.
     * This is to protect against accidental sharing.
     *
     * @return Allocated or recycled byte buffer
     */
    public byte[] allocBase64Buffer() {
        _verifyAlloc(_base64Buffer);
        return (_base64Buffer = _bufferRecycler.allocByteBuffer(BufferRecycler.BYTE_BASE64_CODEC_BUFFER));
    }

    /**
     * Variant of {@link #allocBase64Buffer()} that specifies smallest acceptable
     * buffer size.
     *
     * @param minSize Minimum size of the buffer to recycle or allocate
     *
     * @return Allocated or recycled byte buffer
     *
     * @since 2.9
     */
    public byte[] allocBase64Buffer(int minSize) {
        _verifyAlloc(_base64Buffer);
        return (_base64Buffer = _bufferRecycler.allocByteBuffer(BufferRecycler.BYTE_BASE64_CODEC_BUFFER, minSize));
    }

    public char[] allocTokenBuffer() {
        _verifyAlloc(_tokenCBuffer);
        return (_tokenCBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CHAR_TOKEN_BUFFER));
    }

    // @since 2.4
    public char[] allocTokenBuffer(int minSize) {
        _verifyAlloc(_tokenCBuffer);
        return (_tokenCBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CHAR_TOKEN_BUFFER, minSize));
    }

    public char[] allocConcatBuffer() {
        _verifyAlloc(_concatCBuffer);
        return (_concatCBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CHAR_CONCAT_BUFFER));
    }

    public char[] allocNameCopyBuffer(int minSize) {
        _verifyAlloc(_nameCopyBuffer);
        return (_nameCopyBuffer = _bufferRecycler.allocCharBuffer(BufferRecycler.CHAR_NAME_COPY_BUFFER, minSize));
    }

    /**
     * Method to call when all the processing buffers can be safely
     * recycled.
     *
     * @param buf Buffer instance to release (return for recycling)
     */
    public void releaseReadIOBuffer(byte[] buf) {
        if (buf != null) {
            // Let's do sanity checks to ensure once-and-only-once release,
            // as well as avoiding trying to release buffers not owned
            _verifyRelease(buf, _readIOBuffer);
            _readIOBuffer = null;
            _bufferRecycler.releaseByteBuffer(BufferRecycler.BYTE_READ_IO_BUFFER, buf);
        }
    }

    public void releaseWriteEncodingBuffer(byte[] buf) {
        if (buf != null) {
            // Let's do sanity checks to ensure once-and-only-once release,
            // as well as avoiding trying to release buffers not owned
            _verifyRelease(buf, _writeEncodingBuffer);
            _writeEncodingBuffer = null;
            _bufferRecycler.releaseByteBuffer(BufferRecycler.BYTE_WRITE_ENCODING_BUFFER, buf);
        }
    }

    public void releaseBase64Buffer(byte[] buf) {
        if (buf != null) { // sanity checks, release once-and-only-once, must be one owned
            _verifyRelease(buf, _base64Buffer);
            _base64Buffer = null;
            _bufferRecycler.releaseByteBuffer(BufferRecycler.BYTE_BASE64_CODEC_BUFFER, buf);
        }
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

    public void releaseNameCopyBuffer(char[] buf) {
        if (buf != null) {
            // 14-Jan-2014, tatu: Let's actually allow upgrade of the original buffer.
            _verifyRelease(buf, _nameCopyBuffer);
            _nameCopyBuffer = null;
            _bufferRecycler.releaseCharBuffer(BufferRecycler.CHAR_NAME_COPY_BUFFER, buf);
        }
    }

    /*
    /**********************************************************************
    /* Internal helpers
    /**********************************************************************
     */

    protected final void _verifyAlloc(Object buffer) {
        if (buffer != null) {
            throw new IllegalStateException("Trying to call same allocXxx() method second time");
        }
    }

    protected final void _verifyRelease(byte[] toRelease, byte[] src) {
        // 07-Mar-2016, tatu: As per [core#255], only prevent shrinking of buffer
        if ((toRelease != src) && (toRelease.length < src.length)) {
            throw wrongBuf();
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

    @Override
    public void close() {
        if (!_closed) {
            _closed = true;
            if (_releaseRecycler) {
                _releaseRecycler = false;
                _bufferRecycler.releaseToPool();
            }
        }
    }
}
