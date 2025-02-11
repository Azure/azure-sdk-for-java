// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package io.clientcore.core.serialization.json.implementation.jackson.core;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Base class that defines public API for writing JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
public abstract class JsonGenerator implements Closeable, Flushable {

    /**
     * Enumeration that defines all togglable features for generators.
     */
    public enum Feature {
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
         */
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

        public int getMask() {
            return _mask;
        }
    }

    /*
     * /**********************************************************
     * /* Minimal configuration state
     * /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link JsonParser.Feature}s
     * are enabled.
     */
    protected int _features;

    /*
     * /**********************************************************************
     * /* Construction, initialization
     * /**********************************************************************
     */

    protected JsonGenerator(int features) {
        _features = features;
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
    public abstract JsonStreamContext getOutputContext();

    /*
     * /**********************************************************************
     * /* Public API, Feature configuration
     * /**********************************************************************
     */

    /**
     * Method for configuring specified generator feature:
     * check {@link Feature} for list of available features.
     *
     * @param f Feature to configure
     * @param state Whether to enable or disable the feature
     * @return This generator, to allow call chaining
     */
    public final JsonGenerator configure(Feature f, boolean state) {
        if (state) {
            _features |= f.getMask();
        } else {
            _features &= ~f.getMask();
        }
        return this;
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
     * @param data Buffer that contains binary data to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeBinary(byte[] data) throws IOException;

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
     * reference to the resource such as File, but not stream). Otherwise
     * (not managing, feature not enabled), target is not closed.
     *
     * @throws IOException if there is either an underlying I/O problem
     */
    @Override
    public abstract void close() throws IOException;
}
