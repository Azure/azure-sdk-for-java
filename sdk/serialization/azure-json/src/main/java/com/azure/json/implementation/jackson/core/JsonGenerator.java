// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.io.CharTypes;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.json.JsonWriteContext;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Base class that defines public API for writing JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
public abstract class JsonGenerator implements Closeable, Flushable {
    /*
     * /**********************************************************
     * /* Constants
     * /**********************************************************
     */

    /**
     * This is the default set of escape codes, over 7-bit ASCII range
     * (first 128 character codes), used for single-byte UTF-8 characters.
     */
    protected final static int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();

    public final static int SURR1_FIRST = 0xD800;
    public final static int SURR1_LAST = 0xDBFF;
    public final static int SURR2_FIRST = 0xDC00;
    public final static int SURR2_LAST = 0xDFFF;

    protected static final byte[] BYTES_SPACE = new byte[] { ' ' };

    // // // Constants for validation messages (since 2.6)

    protected final static String WRITE_BINARY = "write a binary value";
    protected final static String WRITE_BOOLEAN = "write a boolean value";
    protected final static String WRITE_NULL = "write a null";
    protected final static String WRITE_NUMBER = "write a number";
    protected final static String WRITE_STRING = "write a string";

    /**
     * Enumeration that defines all togglable features for generators.
     */
    public enum Feature {
        // // Low-level I/O / content features

        /**
         * Feature that determines whether generator will automatically
         * close underlying output target that is NOT owned by the
         * generator.
         * If disabled, calling application has to separately
         * close the underlying {@link OutputStream} and {@link Writer}
         * instances used to create the generator. If enabled, generator
         * will handle closing, as long as generator itself gets closed:
         * this happens when end-of-input is encountered, or generator
         * is closed by a call to {@link JsonGenerator#close}.
         *<p>
         * Feature is enabled by default.
         */
        AUTO_CLOSE_TARGET(true),

        /**
         * Feature that determines what happens when the generator is
         * closed while there are still unmatched
         * {@link JsonToken#START_ARRAY} or {@link JsonToken#START_OBJECT}
         * entries in output content. If enabled, such Array(s) and/or
         * Object(s) are automatically closed; if disabled, nothing
         * specific is done.
         *<p>
         * Feature is enabled by default.
         */
        AUTO_CLOSE_JSON_CONTENT(true),

        /**
         * Feature that specifies that calls to {@link #flush} will cause
         * matching <code>flush()</code> to underlying {@link OutputStream}
         * or {@link Writer}; if disabled this will not be done.
         * Main reason to disable this feature is to prevent flushing at
         * generator level, if it is not possible to prevent method being
         * called by other code (like <code>ObjectMapper</code> or third
         * party libraries).
         *<p>
         * Feature is enabled by default.
         */
        FLUSH_PASSED_TO_STREAM(true),

        // // Quoting-related features

        /**
         * Feature that determines whether "exceptional" (not real number)
         * float/double values are output as quoted strings.
         * The values checked are Double.Nan,
         * Double.POSITIVE_INFINITY and Double.NEGATIVE_INIFINTY (and
         * associated Float values).
         * If feature is disabled, these numbers are still output using
         * associated literal values, resulting in non-conformant
         * output.
         *<p>
         * Feature is enabled by default.
         *
         * @deprecated Since 2.10 use {@link com.azure.json.implementation.jackson.core.json.JsonWriteFeature#WRITE_NAN_AS_STRINGS} instead
         */
        @Deprecated
        QUOTE_NON_NUMERIC_NUMBERS(true);

        private final boolean _defaultState;
        private final int _mask;

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         *
         * @return Bit field of the features that are enabled by default
         */
        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }

        Feature(boolean defaultState) {
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }

        public boolean enabledByDefault() {
            return _defaultState;
        }

        // @since 2.3
        public boolean enabledIn(int flags) {
            return (flags & _mask) != 0;
        }

        public int getMask() {
            return _mask;
        }
    }

    /*
     * /**********************************************************
     * /* Configuration
     * /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link Feature}s
     * are enabled.
     */
    protected int _features;

    /*
     * /**********************************************************
     * /* State
     * /**********************************************************
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
     * /**********************************************************
     * /* Configuration, basic I/O
     * /**********************************************************
     */

    protected final IOContext _ioContext;

    /*
     * /**********************************************************************
     * /* Construction, initialization
     * /**********************************************************************
     */

    protected JsonGenerator(IOContext ctxt, int features) {
        _ioContext = ctxt;
        _features = features;
        _writeContext = JsonWriteContext.createRootContext();
    }

    /*
     * /**********************************************************************
     * /* Public API, state, output configuration access
     * /**********************************************************************
     */

    /**
     * Accessor for context object that provides information about low-level
     * logical position withing output token stream.
     *
     * @return Stream output context ({@link JsonStreamContext}) associated with this generator
     */
    public JsonStreamContext getOutputContext() {
        return _writeContext;
    }

    /*
     * /**********************************************************************
     * /* Public API, Feature configuration
     * /**********************************************************************
     */

    /**
     * Method for enabling or disabling specified feature:
     * check {@link Feature} for list of available features.
     *
     * @param f Feature to enable or disable
     * @param state Whether to enable ({@code true}) or disable ({@code false}) feature
     *
     * @return This generator, to allow call chaining
     */
    public final JsonGenerator configure(Feature f, boolean state) {
        final int mask = f.getMask();
        if (state)
            _features |= mask;
        else
            _features &= ~mask;
        return this;
    }

    /**
     * Method for checking whether given feature is enabled.
     * Check {@link Feature} for list of available features.
     *
     * @param f Feature to check
     *
     * @return True if specified feature is enabled; false if not
     */
    public final boolean isEnabled(Feature f) {
        return (_features & f.getMask()) != 0;
    }

    /*
     * /**********************************************************************
     * /* Public API, write methods, structural
     * /**********************************************************************
     */

    /**
     * Method for writing starting marker of a Array value
     * (for JSON this is character '['; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Array values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeStartArray() throws IOException;

    /**
     * Method for writing closing marker of a JSON Array value
     * (character ']'; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Marker can be written if the innermost structured type
     * is Array.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeEndArray() throws IOException;

    /**
     * Method for writing starting marker of an Object value
     * (character '{'; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Object values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeStartObject() throws IOException;

    /**
     * Method for writing closing marker of an Object value
     * (character '}'; plus possible white space decoration
     * if pretty-printing is enabled).
     *<p>
     * Marker can be written if the innermost structured type
     * is Object, and the last written event was either a
     * complete value, or START-OBJECT marker (see JSON specification
     * for more details).
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeEndObject() throws IOException;

    /**
     * Method for writing a field name (JSON String surrounded by
     * double quotes: syntactically identical to a JSON String value),
     * possibly decorated by white space if pretty-printing is enabled.
     *<p>
     * Field names can only be written in Object context (check out
     * JSON specification for details), when field name is expected
     * (field names alternate with values).
     *
     * @param name Field name to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeFieldName(String name) throws IOException;

    /*
     * /**********************************************************************
     * /* Public API, write methods, text/String values
     * /**********************************************************************
     */

    /**
     * Method for outputting a String value. Depending on context
     * this means either array element, (object) field value or
     * a stand alone String; but in all cases, String will be
     * surrounded in double quotes, and contents will be properly
     * escaped as required by JSON specification.
     *
     * @param text Text value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeString(String text) throws IOException;

    /*
     * /**********************************************************************
     * /* Public API, write methods, binary/raw content
     * /**********************************************************************
     */

    /**
     * Method that will force generator to copy
     * input text verbatim with <b>no</b> modifications (including
     * that no escaping is done and no separators are added even
     * if context [array, object] would otherwise require such).
     * If such separators are desired, use
     * {@link #writeRawValue(String)} instead.
     *<p>
     * Note that not all generator implementations necessarily support
     * such by-pass methods: those that do not will throw
     * {@link UnsupportedOperationException}.
     *
     * @param text Textual contents to include as-is in output.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeRaw(String text) throws IOException;

    /**
     * Method that will force generator to copy
     * input text verbatim with <b>no</b> modifications (including
     * that no escaping is done and no separators are added even
     * if context [array, object] would otherwise require such).
     * If such separators are desired, use
     * {@link #writeRawValue(String)} instead.
     *<p>
     * Note that not all generator implementations necessarily support
     * such by-pass methods: those that do not will throw
     * {@link UnsupportedOperationException}.
     *
     * @param text String that has contents to include as-is in output
     * @param offset Offset within {@code text} of the first character to output
     * @param len Length of content (from {@code text}, starting at offset {@code offset}) to output
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeRaw(String text, int offset, int len) throws IOException;

    /**
     * Method that will force generator to copy
     * input text verbatim with <b>no</b> modifications (including
     * that no escaping is done and no separators are added even
     * if context [array, object] would otherwise require such).
     * If such separators are desired, use
     * {@link #writeRawValue(String)} instead.
     *<p>
     * Note that not all generator implementations necessarily support
     * such by-pass methods: those that do not will throw
     * {@link UnsupportedOperationException}.
     *
     * @param text Buffer that has contents to include as-is in output
     * @param offset Offset within {@code text} of the first character to output
     * @param len Length of content (from {@code text}, starting at offset {@code offset}) to output
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeRaw(char[] text, int offset, int len) throws IOException;

    /**
     * Method that will force generator to copy
     * input text verbatim without any modifications, but assuming
     * it must constitute a single legal JSON value (number, string,
     * boolean, null, Array or List). Assuming this, proper separators
     * are added if and as needed (comma or colon), and generator
     * state updated to reflect this.
     *
     * @param text Textual contents to included in output
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeRawValue(String text) throws IOException {
        _verifyValueWrite("write raw value");
        writeRaw(text);
    }

    /**
     * Method that will output given chunk of binary data as base64
     * encoded, as a complete String value (surrounded by double quotes).
     * This method defaults
     *<p>
     * Note: because JSON Strings can not contain unescaped linefeeds,
     * if linefeeds are included (as per last argument), they must be
     * escaped. This adds overhead for decoding without improving
     * readability.
     * Alternatively if linefeeds are not included,
     * resulting String value may violate the requirement of base64
     * RFC which mandates line-length of 76 characters and use of
     * linefeeds. However, all {@link JsonParser} implementations
     * are required to accept such "long line base64"; as do
     * typical production-level base64 decoders.
     *
     * @param bv Base64 variant to use: defines details such as
     *   whether padding is used (and if so, using which character);
     *   what is the maximum line length before adding linefeed,
     *   and also the underlying alphabet to use.
     * @param data Buffer that contains binary data to write
     * @param offset Offset in {@code data} of the first byte of data to write
     * @param len Length of data to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException;

    /**
     * Similar to {@link #writeBinary(Base64Variant,byte[],int,int)},
     * but assumes default to using the Jackson default Base64 variant
     * (which is {@link Base64Variants#MIME_NO_LINEFEEDS}). Also
     * assumes that whole byte array is to be output.
     *
     * @param data Buffer that contains binary data to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeBinary(byte[] data) throws IOException {
        writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
    }

    /*
     * /**********************************************************************
     * /* Public API, write methods, numeric
     * /**********************************************************************
     */

    /**
     * Method for outputting given value as JSON number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     *
     * @param v Number value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeNumber(int v) throws IOException;

    /**
     * Method for outputting given value as JSON number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     *
     * @param v Number value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeNumber(long v) throws IOException;

    /**
     * Method for outputting indicate JSON numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     *
     * @param v Number value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeNumber(double v) throws IOException;

    /**
     * Method for outputting indicate JSON numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     *
     * @param v Number value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeNumber(float v) throws IOException;

    /*
     * /**********************************************************************
     * /* Public API, write methods, other value types
     * /**********************************************************************
     */

    /**
     * Method for outputting literal JSON boolean value (one of
     * Strings 'true' and 'false').
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     *
     * @param state Boolean value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeBoolean(boolean state) throws IOException;

    /**
     * Method for outputting literal JSON null value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * Additional white space may be added around the value
     * if pretty-printing is enabled.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeNull() throws IOException;

    /*
     * /**********************************************************************
     * /* Public API, buffer handling
     * /**********************************************************************
     */

    /**
     * Method called to flush any buffered content to the underlying
     * target (output stream, writer), and to flush the target itself
     * as well.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    @Override
    public abstract void flush() throws IOException;

    /*
     * /**********************************************************************
     * /* Closeable implementation
     * /**********************************************************************
     */

    /**
     * Method called to close this generator, so that no more content
     * can be written.
     *<p>
     * Whether the underlying target (stream, writer) gets closed depends
     * on whether this generator either manages the target (i.e. is the
     * only one with access to the target -- case if caller passes a
     * reference to the resource such as File, but not stream); or
     * has feature {@link Feature#AUTO_CLOSE_TARGET} enabled.
     * If either of above is true, the target is also closed. Otherwise
     * (not managing, feature not enabled), target is not closed.
     *
     * @throws IOException if there is either an underlying I/O problem
     */
    @Override
    public void close() throws IOException {
        _closed = true;
    }

    /*
     * /**********************************************************
     * /* Package methods for this, sub-classes
     * /**********************************************************
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
     * /**********************************************************************
     * /* Helper methods for sub-classes
     * /**********************************************************************
     */

    /**
     * Helper method used for constructing and throwing
     * {@link JsonGenerationException} with given base message.
     *<p>
     * Note that sub-classes may override this method to add more detail
     * or use a {@link JsonGenerationException} sub-class.
     *
     * @param msg Exception message to use
     *
     * @throws JsonGenerationException constructed
     */
    protected void _reportError(String msg) throws JsonGenerationException {
        throw new JsonGenerationException(msg, this);
    }

    protected void _reportCantWriteValueExpectName(String typeMsg) throws IOException {
        _reportError("Can not " + typeMsg + ", expecting field name (context: " + _writeContext.typeDesc() + ")");
    }

    /*
     * /**********************************************************
     * /* UTF-8 related helper method(s)
     * /**********************************************************
     */

    // @since 2.5
    protected final int _decodeSurrogate(int surr1, int surr2) throws IOException {
        // First is known to be valid, but how about the other?
        if (surr2 < SURR2_FIRST || surr2 > SURR2_LAST) {
            String msg = String.format("Incomplete surrogate pair: first char 0x%04X, second 0x%04X", surr1, surr2);
            _reportError(msg);
        }
        return 0x10000 + ((surr1 - SURR1_FIRST) << 10) + (surr2 - SURR2_FIRST);
    }
}
