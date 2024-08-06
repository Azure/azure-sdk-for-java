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

    /**
     * Set of feature masks related to features that need updates of other
     * local configuration or state.
     *
     * @since 2.5
     */
    @SuppressWarnings("deprecation")
    protected final static int DERIVED_FEATURES_MASK
        = Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask();

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

    /**
     * Flag set to indicate that implicit conversion from number
     * to JSON String is needed (as per
     * {@link com.azure.json.implementation.jackson.core.json.JsonWriteFeature#WRITE_NUMBERS_AS_STRINGS}).
     */
    protected boolean _cfgNumbersAsStrings;

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

    @Deprecated // since 2.16
    protected GeneratorBase(int features) {
        this(features, (IOContext) null);
    }

    // @since 2.16
    @SuppressWarnings("deprecation")
    protected GeneratorBase(int features, IOContext ioContext) {
        super();
        _features = features;
        _ioContext = ioContext;
        _writeContext = JsonWriteContext.createRootContext();
        _cfgNumbersAsStrings = Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(features);
    }

    // @since 2.5
    @Deprecated // since 2.16
    protected GeneratorBase(int features, JsonWriteContext ctxt) {
        this(features, null, ctxt);
    }

    // @since 2.16
    @SuppressWarnings("deprecation")
    protected GeneratorBase(int features, IOContext ioContext, JsonWriteContext jsonWriteContext) {
        super();
        _features = features;
        _ioContext = ioContext;
        _writeContext = jsonWriteContext;
        _cfgNumbersAsStrings = Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(features);
    }

    // Overridden from JsonGenerator for direct context access:
    @Override
    public Object currentValue() {
        return _writeContext.getCurrentValue();
    }

    @Override
    // Overridden from JsonGenerator for direct context access:
    public void assignCurrentValue(Object v) {
        if (_writeContext != null) {
            _writeContext.setCurrentValue(v);
        }
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
        if ((mask & DERIVED_FEATURES_MASK) != 0) {
            // why not switch? Requires addition of a generated class, alas
            if (f == Feature.WRITE_NUMBERS_AS_STRINGS) {
                _cfgNumbersAsStrings = true;
            } else if (f == Feature.ESCAPE_NON_ASCII) {
                setHighestNonEscapedChar(127);
            }
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public JsonGenerator disable(Feature f) {
        final int mask = f.getMask();
        _features &= ~mask;
        if ((mask & DERIVED_FEATURES_MASK) != 0) {
            if (f == Feature.WRITE_NUMBERS_AS_STRINGS) {
                _cfgNumbersAsStrings = false;
            } else if (f == Feature.ESCAPE_NON_ASCII) {
                setHighestNonEscapedChar(0);
            }
        }
        return this;
    }

    @Override
    @Deprecated
    public JsonGenerator setFeatureMask(int newMask) {
        int changed = newMask ^ _features;
        _features = newMask;
        if (changed != 0) {
            _checkStdFeatureChanges(newMask, changed);
        }
        return this;
    }

    /**
     * Helper method called to verify changes to standard features.
     *
     * @param newFeatureFlags Bitflag of standard features after they were changed
     * @param changedFeatures Bitflag of standard features for which setting
     *    did change
     *
     * @since 2.7
     */
    @SuppressWarnings("deprecation")
    protected void _checkStdFeatureChanges(int newFeatureFlags, int changedFeatures) {
        if ((changedFeatures & DERIVED_FEATURES_MASK) == 0) {
            return;
        }
        _cfgNumbersAsStrings = Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(newFeatureFlags);
        if (Feature.ESCAPE_NON_ASCII.enabledIn(changedFeatures)) {
            if (Feature.ESCAPE_NON_ASCII.enabledIn(newFeatureFlags)) {
                setHighestNonEscapedChar(127);
            } else {
                setHighestNonEscapedChar(0);
            }
        }
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
    /* Public API, write methods, primitive
    /**********************************************************
     */

    // Not implemented at this level, added as placeholders

    /*
    public abstract void writeNumber(int i)
    public abstract void writeNumber(long l)
    public abstract void writeNumber(double d)
    public abstract void writeNumber(float f)
    public abstract void writeNumber(BigDecimal dec)
    public abstract void writeBoolean(boolean state)
    public abstract void writeNull()
    */

    /*
    /**********************************************************
    /* Public API, write methods, POJOs, trees
    /**********************************************************
     */

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

    @Override
    public boolean isClosed() {
        return _closed;
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
