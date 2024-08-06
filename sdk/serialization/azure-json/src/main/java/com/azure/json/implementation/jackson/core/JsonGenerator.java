// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.exc.StreamWriteException;
import com.azure.json.implementation.jackson.core.io.CharacterEscapes;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;

/**
 * Base class that defines public API for writing JSON content.
 * Instances are created using factory methods of a
 * {@link JsonFactory} instance.
 *
 *
 * @author Tatu Saloranta
 */
public abstract class JsonGenerator implements Closeable, Flushable {

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
         * Feature that determines whether JSON Object field names are
         * quoted using double-quotes, as specified by JSON specification
         * or not. Ability to disable quoting was added to support use
         * cases where they are not usually expected, which most commonly
         * occurs when used straight from Javascript.
         *<p>
         * Feature is enabled by default (since it is required by JSON specification).
         *
         * @deprecated Since 2.10 use {@link com.azure.json.implementation.jackson.core.json.JsonWriteFeature#QUOTE_FIELD_NAMES} instead
         */
        @Deprecated
        QUOTE_FIELD_NAMES(true),

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
        QUOTE_NON_NUMERIC_NUMBERS(true),

        // // Character escaping features

        /**
         * Feature that specifies that all characters beyond 7-bit ASCII
         * range (i.e. code points of 128 and above) need to be output
         * using format-specific escapes (for JSON, backslash escapes),
         * if format uses escaping mechanisms (which is generally true
         * for textual formats but not for binary formats).
         *<p>
         * Note that this setting may not necessarily make sense for all
         * data formats (for example, binary formats typically do not use
         * any escaping mechanisms; and some textual formats do not have
         * general-purpose escaping); if so, settings is simply ignored.
         * Put another way, effects of this feature are data-format specific.
         *<p>
         * Feature is disabled by default.
         *
         * @deprecated Since 2.10 use {@link com.azure.json.implementation.jackson.core.json.JsonWriteFeature#ESCAPE_NON_ASCII} instead
         */
        @Deprecated
        ESCAPE_NON_ASCII(false),

        // // Datatype coercion features

        /**
         * Feature that forces all Java numbers to be written as Strings,
         * even if the underlying data format has non-textual representation
         * (which is the case for JSON as well as all binary formats).
         * Default state is 'false', meaning that Java numbers are to
         * be serialized using basic numeric serialization (as JSON
         * numbers, integral or floating point, for example).
         * If enabled, all such numeric values are instead written out as
         * textual values (which for JSON means quoted in double-quotes).
         *<p>
         * One use case is to avoid problems with Javascript limitations:
         * since Javascript standard specifies that all number handling
         * should be done using 64-bit IEEE 754 floating point values,
         * result being that some 64-bit integer values can not be
         * accurately represent (as mantissa is only 51 bit wide).
         *<p>
         * Feature is disabled by default.
         *
         * @deprecated Since 2.10 use {@link com.azure.json.implementation.jackson.core.json.JsonWriteFeature#WRITE_NUMBERS_AS_STRINGS} instead
         */
        @Deprecated
        WRITE_NUMBERS_AS_STRINGS(false),

        /**
         * Feature that determines whether {@link java.math.BigDecimal} entries are
         * serialized using {@link java.math.BigDecimal#toPlainString()} to prevent
         * values to be written using scientific notation.
         *<p>
         * NOTE: only affects generators that serialize {@link java.math.BigDecimal}s
         * using textual representation (textual formats but potentially some binary
         * formats).
         *<p>
         * Feature is disabled by default, so default output mode is used; this generally
         * depends on how {@link BigDecimal} has been created.
         *
         * @since 2.3
         */
        WRITE_BIGDECIMAL_AS_PLAIN(false),

        // // Schema/Validity support features

        /**
         * Feature that determines what to do if the underlying data format requires knowledge
         * of all properties to output, and if no definition is found for a property that
         * caller tries to write. If enabled, such properties will be quietly ignored;
         * if disabled, a {@link JsonProcessingException} will be thrown to indicate the
         * problem.
         * Typically most textual data formats do NOT require schema information (although
         * some do, such as CSV), whereas many binary data formats do require definitions
         * (such as Avro, protobuf), although not all (Smile, CBOR, BSON and MessagePack do not).
         *<p>
         * Note that support for this feature is implemented by individual data format
         * module, if (and only if) it makes sense for the format in question. For JSON,
         * for example, this feature has no effect as properties need not be pre-defined.
         *<p>
         * Feature is disabled by default, meaning that if the underlying data format
         * requires knowledge of all properties to output, attempts to write an unknown
         * property will result in a {@link JsonProcessingException}
         *
         * @since 2.5
         */
        IGNORE_UNKNOWN(false),

        // // Misc other

        /**
         * Alias for {@link com.azure.json.implementation.jackson.core.StreamWriteFeature#USE_FAST_DOUBLE_WRITER} instead
         *
         * @since 2.14
         * @deprecated Use {@link com.azure.json.implementation.jackson.core.StreamWriteFeature#USE_FAST_DOUBLE_WRITER} instead
         */
        @Deprecated
        USE_FAST_DOUBLE_WRITER(false),

        /**
         * Feature that specifies that hex values are encoded with capital letters.
         *<p>
         * Can be disabled to have a better possibility to compare between other Json
         * writer libraries, such as JSON.stringify from Javascript.
         *<p>
         * Feature is enabled by default.
         *
         * @since 2.14
         * @deprecated Use {@link com.azure.json.implementation.jackson.core.json.JsonWriteFeature#WRITE_HEX_UPPER_CASE} instead
         */
        @Deprecated
        WRITE_HEX_UPPER_CASE(true),

        /**
         * Feature that specifies whether {@link JsonGenerator} should escape forward slashes.
         * <p>
         * Feature is disabled by default for Jackson 2.x version, and enabled by default in Jackson 3.0.
         *
         * @since 2.17
         */
        ESCAPE_FORWARD_SLASHES(false);

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
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Construction, initialization
    /**********************************************************************
     */

    protected JsonGenerator() {
    }

    /**
     * Get the constraints to apply when performing streaming writes.
     *
     * @since 2.16
     */
    public StreamWriteConstraints streamWriteConstraints() {
        return StreamWriteConstraints.defaults();
    }

    /*
    /**********************************************************************
    /* Public API, state, output configuration access
    /**********************************************************************
     */

    /**
     * Accessor for context object that provides information about low-level
     * logical position withing output token stream.
     *
     * @return Stream output context ({@link JsonStreamContext}) associated with this generator
     */
    public abstract JsonStreamContext getOutputContext();

    /**
     * Helper method, usually equivalent to:
     *<code>
     *   getOutputContext().getCurrentValue();
     *</code>
     *<p>
     * Note that "current value" is NOT populated (or used) by Streaming parser or generators;
     * it is only used by higher-level data-binding functionality.
     * The reason it is included here is that it can be stored and accessed hierarchically,
     * and gets passed through data-binding.
     *
     * @return "Current value" associated with the current context (state) of this generator
     *
     * @since 2.13 (added as replacement for older {@link #getCurrentValue()}
     */
    public Object currentValue() {
        JsonStreamContext ctxt = getOutputContext();
        return (ctxt == null) ? null : ctxt.getCurrentValue();
    }

    /**
     * Helper method, usually equivalent to:
     *<code>
     *   getOutputContext().setCurrentValue(v);
     *</code>
     *
     * @param v Current value to assign for the current context of this generator
     *
     * @since 2.13 (added as replacement for older {@link #setCurrentValue}
     */
    public void assignCurrentValue(Object v) {
        JsonStreamContext ctxt = getOutputContext();
        if (ctxt != null) {
            ctxt.setCurrentValue(v);
        }
    }

    /**
     * Alias for {@link #currentValue()}, to be deprecated in later
     * Jackson 2.x versions (and removed from Jackson 3.0).
     *
     * @return Location of the last processed input unit (byte or character)
     *
     * @deprecated Since 2.17 use {@link #currentValue()} instead
     */
    @Deprecated
    public Object getCurrentValue() {
        return currentValue();
    }

    /**
     * Alias for {@link #assignCurrentValue}, to be deprecated in later
     * Jackson 2.x versions (and removed from Jackson 3.0).
     *
     * @param v Current value to assign for the current context of this generator
     *
     * @deprecated Since 2.17 use {@link #currentValue()} instead
     */
    @Deprecated
    public void setCurrentValue(Object v) {
        assignCurrentValue(v);
    }

    /*
    /**********************************************************************
    /* Public API, Feature configuration
    /**********************************************************************
     */

    /**
     * Method for enabling specified generator feature:
     * check {@link Feature} for list of available features.
     *
     * @param f Feature to enable
     *
     * @return This generator, to allow call chaining
     */
    public abstract JsonGenerator enable(Feature f);

    /**
     * Method for disabling specified feature
     * (check {@link Feature} for list of features)
     *
     * @param f Feature to disable
     *
     * @return This generator, to allow call chaining
     */
    public abstract JsonGenerator disable(Feature f);

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
        if (state)
            enable(f);
        else
            disable(f);
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
    public abstract boolean isEnabled(Feature f);

    /**
     * Method for checking whether given feature is enabled.
     * Check {@link Feature} for list of available features.
     *
     * @param f Feature to check
     *
     * @return True if specified feature is enabled; false if not
     *
     * @since 2.10
     */
    public boolean isEnabled(StreamWriteFeature f) {
        return isEnabled(f.mappedFeature());
    }

    /**
     * Bulk set method for (re)setting states of all standard {@link Feature}s
     *
     * @since 2.3
     *
     * @param values Bitmask that defines which {@link Feature}s are enabled
     *    and which disabled
     *
     * @return This generator, to allow call chaining
     *
     * @deprecated Since 2.7, use {@code #overrideStdFeatures(int, int)} instead -- remove from 2.9
     */
    @Deprecated
    public abstract JsonGenerator setFeatureMask(int values);

    /*
    /**********************************************************************
    /* Public API, Schema configuration
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Public API, other configuration
    /**********************************************************************
      */

    /**
     * Method that can be called to request that generator escapes
     * all character codes above specified code point (if positive value);
     * or, to not escape any characters except for ones that must be
     * escaped for the data format (if -1).
     * To force escaping of all non-ASCII characters, for example,
     * this method would be called with value of 127.
     *<p>
     * Note that generators are NOT required to support setting of value
     * higher than 127, because there are other ways to affect quoting
     * (or lack thereof) of character codes between 0 and 127.
     * Not all generators support concept of escaping, either; if so,
     * calling this method will have no effect.
     *<p>
     * Default implementation does nothing; sub-classes need to redefine
     * it according to rules of supported data format.
     *
     * @param charCode Either -1 to indicate that no additional escaping
     *   is to be done; or highest code point not to escape (meaning higher
     *   ones will be), if positive value.
     *
     * @return This generator, to allow call chaining
     */
    public JsonGenerator setHighestNonEscapedChar(int charCode) {
        return this;
    }

    /**
     * Method for defining custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     *<p>
     * Default implementation does nothing and simply returns this instance.
     *
     * @param esc {@link CharacterEscapes} to configure this generator to use, if any; {@code null} if none
     *
     * @return This generator, to allow call chaining
     */
    public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
        return this;
    }

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
    /**********************************************************************
    /* Public API, write methods, scalar arrays (2.8)
    /**********************************************************************
     */

    /*
    /**********************************************************************
    /* Public API, write methods, text/String values
    /**********************************************************************
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
    /**********************************************************************
    /* Public API, write methods, binary/raw content
    /**********************************************************************
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
    public abstract void writeRawValue(String text) throws IOException;

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
    /**********************************************************************
    /* Public API, write methods, numeric
    /**********************************************************************
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
    /**********************************************************************
    /* Public API, write methods, other value types
    /**********************************************************************
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

    /**
     * Method that can be called to determine whether this generator
     * is closed or not. If it is closed, no more output can be done.
     *
     * @return {@code True} if this generator instance has been closed
     */
    public abstract boolean isClosed();

    /*
    /**********************************************************************
    /* Closeable implementation
    /**********************************************************************
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
    public abstract void close() throws IOException;

    /*
    /**********************************************************************
    /* Helper methods for sub-classes, error reporting
    /**********************************************************************
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
        throw (JsonGenerationException) _constructWriteException(msg);
    }

    // @since 2.17
    protected StreamWriteException _constructWriteException(String msg) {
        return new JsonGenerationException(msg, this);
    }
}
