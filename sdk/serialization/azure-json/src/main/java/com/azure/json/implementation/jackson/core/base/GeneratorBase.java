// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.base;

import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.JsonStreamContext;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.UTF8Writer;
import com.azure.json.implementation.jackson.core.json.JsonWriteContext;

import java.io.IOException;

/**
 * This base class implements part of API that a JSON generator exposes
 * to applications, adds shared internal methods that sub-classes
 * can use and adds some abstract methods sub-classes must implement.
 */
public abstract class GeneratorBase extends JsonGenerator {
    public final static int SURR1_FIRST = 0xD800;
    public final static int SURR1_LAST = 0xDBFF;
    public final static int SURR2_FIRST = 0xDC00;
    public final static int SURR2_LAST = 0xDFFF;

    // // // Constants for validation messages (since 2.6)

    protected final static String WRITE_BINARY = "write a binary value";
    protected final static String WRITE_BOOLEAN = "write a boolean value";
    protected final static String WRITE_NULL = "write a null";
    protected final static String WRITE_NUMBER = "write a number";
    protected final static String WRITE_STRING = "write a string";

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link com.azure.json.implementation.jackson.core.JsonGenerator.Feature}s
     * are enabled.
     */
    protected int _features;

    // since 2.16
    protected final IOContext _ioContext;

    /*
    /**********************************************************
    /* State
    /**********************************************************
     */

    /**
     * Object that keeps track of the current contextual state
     * of the generator.
     */
    protected JsonWriteContext _writeContext;

    /**
     * Flag that indicates whether generator is closed or not. Gets
     * set when it is closed by an explicit call
     * ({@link #close}).
     */
    protected boolean _closed;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    // @since 2.16
    @SuppressWarnings("deprecation")
    protected GeneratorBase(int features, IOContext ioContext) {
        super();
        _features = features;
        _ioContext = ioContext;
        _writeContext = JsonWriteContext.createRootContext();
    }

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    @Override
    public final boolean isEnabled(Feature f) {
        return (_features & f.getMask()) != 0;
    }

    //public JsonGenerator configure(Feature f, boolean state) { }

    @SuppressWarnings("deprecation")
    @Override
    public JsonGenerator enable(Feature f) {
        final int mask = f.getMask();
        _features |= mask;
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public JsonGenerator disable(Feature f) {
        final int mask = f.getMask();
        _features &= ~mask;
        return this;
    }

    /*
    /**********************************************************
    /* Public API, accessors
    /**********************************************************
     */

    /**
     * Note: type was co-variant until Jackson 2.7; reverted back to
     * base type in 2.8 to allow for overriding by subtypes that use
     * custom context type.
     */
    @Override
    public JsonStreamContext getOutputContext() {
        return _writeContext;
    }

    @Override
    public void writeRawValue(String text) throws IOException {
        _verifyValueWrite("write raw value");
        writeRaw(text);
    }

    /*
    /**********************************************************
    /* Public API, low-level output handling
    /**********************************************************
     */

    @Override
    public abstract void flush() throws IOException;

    @Override
    public void close() throws IOException {
        if (!_closed) {
            if (_ioContext != null) {
                _ioContext.close();
            }
            _closed = true;
        }
    }

    /*
    /**********************************************************
    /* Package methods for this, sub-classes
    /**********************************************************
     */

    /**
     * Method called to release any buffers generator may be holding,
     * once generator is being closed.
     */
    protected abstract void _releaseBuffers();

    /**
     * Method called before trying to write a value (scalar or structured),
     * to verify that this is legal in current output state, as well as to
     * output separators if and as necessary.
     *
     * @param typeMsg Additional message used for generating exception message
     *   if value output is NOT legal in current generator output state.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    protected abstract void _verifyValueWrite(String typeMsg) throws IOException;

    /*
    /**********************************************************
    /* UTF-8 related helper method(s)
    /**********************************************************
     */

    // @since 2.5
    protected final int _decodeSurrogate(int surr1, int surr2) throws IOException {
        // First is known to be valid, but how about the other?
        if (surr2 < SURR2_FIRST || surr2 > SURR2_LAST) {
            String msg = String.format("Incomplete surrogate pair: first char 0x%04X, second 0x%04X", surr1, surr2);
            _reportError(msg);
        }
        return (surr1 << 10) + surr2 + UTF8Writer.SURROGATE_BASE;
    }

    /*
    /**********************************************************************
    /* Helper methods for validating parameters
    /**********************************************************************
     */

    // @since 2.14
    protected void _checkRangeBoundsForByteArray(byte[] data, int offset, int len) throws IOException {
        if (data == null) {
            _reportError("Invalid `byte[]` argument: `null`");
        }
        final int dataLen = data.length;
        final int end = offset + len;

        // Note: we are checking that:
        //
        // !(offset < 0)
        // !(len < 0)
        // !((offset + len) < 0) // int overflow!
        // !((offset + len) > dataLen) == !((datalen - (offset+len)) < 0)

        // All can be optimized by OR'ing and checking for negative:
        int anyNegs = offset | len | end | (dataLen - end);
        if (anyNegs < 0) {
            _reportError(String.format("Invalid 'offset' (%d) and/or 'len' (%d) arguments for `byte[]` of length %d",
                offset, len, dataLen));
        }
    }

    // @since 2.14
    protected void _checkRangeBoundsForCharArray(char[] data, int offset, int len) throws IOException {
        if (data == null) {
            _reportError("Invalid `char[]` argument: `null`");
        }
        final int dataLen = data.length;
        final int end = offset + len;
        // Note: we are checking same things as with other bounds-checks
        int anyNegs = offset | len | end | (dataLen - end);
        if (anyNegs < 0) {
            _reportError(String.format("Invalid 'offset' (%d) and/or 'len' (%d) arguments for `char[]` of length %d",
                offset, len, dataLen));
        }
    }

    // @since 2.14
    protected void _checkRangeBoundsForString(String data, int offset, int len) throws IOException {
        if (data == null) {
            _reportError("Invalid `String` argument: `null`");
        }
        final int dataLen = data.length();
        final int end = offset + len;
        // Note: we are checking same things as with other bounds-checks
        int anyNegs = offset | len | end | (dataLen - end);
        if (anyNegs < 0) {
            _reportError(String.format("Invalid 'offset' (%d) and/or 'len' (%d) arguments for `String` of length %d",
                offset, len, dataLen));
        }
    }
}
