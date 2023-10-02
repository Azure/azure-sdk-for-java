// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.typespec.json.implementation.jackson.core;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

import com.typespec.json.implementation.jackson.core.async.NonBlockingInputFeeder;
import com.typespec.json.implementation.jackson.core.exc.InputCoercionException;
import com.typespec.json.implementation.jackson.core.type.TypeReference;
import com.typespec.json.implementation.jackson.core.util.JacksonFeatureSet;
import com.typespec.json.implementation.jackson.core.util.RequestPayload;

/**
 * Base class that defines public API for reading JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
@SuppressWarnings("cast")
public abstract class JsonParser
    implements Closeable, Versioned
{
    private final static int MIN_BYTE_I = (int) Byte.MIN_VALUE;
    // as per [JACKSON-804], allow range up to and including 255
    private final static int MAX_BYTE_I = (int) 255;

    private final static int MIN_SHORT_I = (int) Short.MIN_VALUE;
    private final static int MAX_SHORT_I = (int) Short.MAX_VALUE;

    /**
     * Enumeration of possible "native" (optimal) types that can be
     * used for numbers.
     */
    public enum NumberType {
        INT, LONG, BIG_INTEGER, FLOAT, DOUBLE, BIG_DECIMAL
    };

    /**
     * Default set of {@link StreamReadCapability}ies that may be used as
     * basis for format-specific readers (or as bogus instance if non-null
     * set needs to be passed).
     *
     * @since 2.12
     */
    protected final static JacksonFeatureSet<StreamReadCapability> DEFAULT_READ_CAPABILITIES
        = JacksonFeatureSet.fromDefaults(StreamReadCapability.values());

    /**
     * Enumeration that defines all on/off features for parsers.
     */
    public enum Feature {
        // // // Low-level I/O handling features:

        /**
         * Feature that determines whether parser will automatically
         * close underlying input source that is NOT owned by the
         * parser. If disabled, calling application has to separately
         * close the underlying {@link InputStream} and {@link Reader}
         * instances used to create the parser. If enabled, parser
         * will handle closing, as long as parser itself gets closed:
         * this happens when end-of-input is encountered, or parser
         * is closed by a call to {@link JsonParser#close}.
         *<p>
         * Feature is enabled by default.
         */
        AUTO_CLOSE_SOURCE(true),

        // // // Support for non-standard data format constructs

        /**
         * Feature that determines whether parser will allow use
         * of Java/C++ style comments (both '/'+'*' and
         * '//' varieties) within parsed content or not.
         *<p>
         * Since JSON specification does not mention comments as legal
         * construct,
         * this is a non-standard feature; however, in the wild
         * this is extensively used. As such, feature is
         * <b>disabled by default</b> for parsers and must be
         * explicitly enabled.
         *<p>
         * NOTE: while not technically deprecated, since 2.10 recommended to use
         * {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_JAVA_COMMENTS} instead.
         */
        ALLOW_COMMENTS(false),

        /**
         * Feature that determines whether parser will allow use
         * of YAML comments, ones starting with '#' and continuing
         * until the end of the line. This commenting style is common
         * with scripting languages as well.
         *<p>
         * Since JSON specification does not mention comments as legal
         * construct,
         * this is a non-standard feature. As such, feature is
         * <b>disabled by default</b> for parsers and must be
         * explicitly enabled.
         *<p>
         * NOTE: while not technically deprecated, since 2.10 recommended to use
         * {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_YAML_COMMENTS} instead.
         */
        ALLOW_YAML_COMMENTS(false),

        /**
         * Feature that determines whether parser will allow use
         * of unquoted field names (which is allowed by Javascript,
         * but not by JSON specification).
         *<p>
         * Since JSON specification requires use of double quotes for
         * field names,
         * this is a non-standard feature, and as such disabled by default.
         *<p>
         * NOTE: while not technically deprecated, since 2.10 recommended to use
         * {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_UNQUOTED_FIELD_NAMES} instead.
         */
        ALLOW_UNQUOTED_FIELD_NAMES(false),

        /**
         * Feature that determines whether parser will allow use
         * of single quotes (apostrophe, character '\'') for
         * quoting Strings (names and String values). If so,
         * this is in addition to other acceptable markers.
         * but not by JSON specification).
         *<p>
         * Since JSON specification requires use of double quotes for
         * field names,
         * this is a non-standard feature, and as such disabled by default.
         *<p>
         * NOTE: while not technically deprecated, since 2.10 recommended to use
         * {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_SINGLE_QUOTES} instead.
         */
        ALLOW_SINGLE_QUOTES(false),

        /**
         * Feature that determines whether parser will allow
         * JSON Strings to contain unquoted control characters
         * (ASCII characters with value less than 32, including
         * tab and line feed characters) or not.
         * If feature is set false, an exception is thrown if such a
         * character is encountered.
         *<p>
         * Since JSON specification requires quoting for all control characters,
         * this is a non-standard feature, and as such disabled by default.
         *
         * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_UNESCAPED_CONTROL_CHARS} instead
         */
        @Deprecated
        ALLOW_UNQUOTED_CONTROL_CHARS(false),

        /**
         * Feature that can be enabled to accept quoting of all character
         * using backslash quoting mechanism: if not enabled, only characters
         * that are explicitly listed by JSON specification can be thus
         * escaped (see JSON spec for small list of these characters)
         *<p>
         * Since JSON specification requires quoting for all control characters,
         * this is a non-standard feature, and as such disabled by default.
         *
         * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER} instead
         */
        @Deprecated
        ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false),

        /**
         * Feature that determines whether parser will allow
         * JSON integral numbers to start with additional (ignorable)
         * zeroes (like: 000001). If enabled, no exception is thrown, and extra
         * nulls are silently ignored (and not included in textual representation
         * exposed via {@link JsonParser#getText}).
         *<p>
         * Since JSON specification does not allow leading zeroes,
         * this is a non-standard feature, and as such disabled by default.
         *
         * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_LEADING_ZEROS_FOR_NUMBERS} instead
         */
         @Deprecated
        ALLOW_NUMERIC_LEADING_ZEROS(false),

        /**
         * @deprecated Use {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS} instead
         */
        @Deprecated
        ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS(false),

        /**
         * Feature that allows parser to recognize set of
         * "Not-a-Number" (NaN) tokens as legal floating number
         * values (similar to how many other data formats and
         * programming language source code allows it).
         * Specific subset contains values that
         * <a href="http://www.w3.org/TR/xmlschema-2/">XML Schema</a>
         * (see section 3.2.4.1, Lexical Representation)
         * allows (tokens are quoted contents, not including quotes):
         *<ul>
         *  <li>"INF" (for positive infinity), as well as alias of "Infinity"
         *  <li>"-INF" (for negative infinity), alias "-Infinity"
         *  <li>"NaN" (for other not-a-numbers, like result of division by zero)
         *</ul>
         *<p>
         * Since JSON specification does not allow use of such values,
         * this is a non-standard feature, and as such disabled by default.
          *
          * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_NON_NUMERIC_NUMBERS} instead
          */
         @Deprecated
         ALLOW_NON_NUMERIC_NUMBERS(false),

         /**
          * Feature allows the support for "missing" values in a JSON array: missing
          * value meaning sequence of two commas, without value in-between but only
          * optional white space.
          * Enabling this feature will expose "missing" values as {@link JsonToken#VALUE_NULL}
          * tokens, which typically become Java nulls in arrays and {@link java.util.Collection}
          * in data-binding.
          * <p>
          * For example, enabling this feature will represent a JSON array <code>["value1",,"value3",]</code>
          * as <code>["value1", null, "value3", null]</code>
          * <p>
          * Since the JSON specification does not allow missing values this is a non-compliant JSON
          * feature and is disabled by default.
          *
          * @since 2.8
          *
          * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_MISSING_VALUES} instead
          */
         @Deprecated
         ALLOW_MISSING_VALUES(false),

         /**
          * Feature that determines whether {@link JsonParser} will allow for a single trailing
          * comma following the final value (in an Array) or member (in an Object). These commas
          * will simply be ignored.
          * <p>
          * For example, when this feature is enabled, <code>[true,true,]</code> is equivalent to
          * <code>[true, true]</code> and <code>{"a": true,}</code> is equivalent to
          * <code>{"a": true}</code>.
          * <p>
          * When combined with <code>ALLOW_MISSING_VALUES</code>, this feature takes priority, and
          * the final trailing comma in an array declaration does not imply a missing
          * (<code>null</code>) value. For example, when both <code>ALLOW_MISSING_VALUES</code>
          * and <code>ALLOW_TRAILING_COMMA</code> are enabled, <code>[true,true,]</code> is
          * equivalent to <code>[true, true]</code>, and <code>[true,true,,]</code> is equivalent to
          * <code>[true, true, null]</code>.
          * <p>
          * Since the JSON specification does not permit trailing commas, this is a non-standard
          * feature, and as such disabled by default.
          *
          * @since 2.9
          *
          * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonReadFeature#ALLOW_TRAILING_COMMA} instead
          */
         @Deprecated
         ALLOW_TRAILING_COMMA(false),

         // // // Validity checks

         /**
          * Feature that determines whether {@link JsonParser} will explicitly
          * check that no duplicate JSON Object field names are encountered.
          * If enabled, parser will check all names within context and report
          * duplicates by throwing a {@link JsonParseException}; if disabled,
          * parser will not do such checking. Assumption in latter case is
          * that caller takes care of handling duplicates at a higher level:
          * data-binding, for example, has features to specify detection to
          * be done there.
          *<p>
          * Note that enabling this feature will incur performance overhead
          * due to having to store and check additional information: this typically
          * adds 20-30% to execution time for basic parsing.
          *
          * @since 2.3
          */
         STRICT_DUPLICATE_DETECTION(false),

         /**
          * Feature that determines what to do if the underlying data format requires knowledge
          * of all properties to decode (usually via a Schema), and if no definition is
          * found for a property that input content contains.
          * Typically most textual data formats do NOT require schema information (although
          * some do, such as CSV), whereas many binary data formats do require definitions
          * (such as Avro, protobuf), although not all (Smile, CBOR, BSON and MessagePack do not).
          * Further note that some formats that do require schema information will not be able
          * to ignore undefined properties: for example, Avro is fully positional and there is
          * no possibility of undefined data. This leaves formats like Protobuf that have identifiers
          * that may or may not map; and as such Protobuf format does make use of this feature.
          *<p>
          * Note that support for this feature is implemented by individual data format
          * module, if (and only if) it makes sense for the format in question. For JSON,
          * for example, this feature has no effect as properties need not be pre-defined.
          *<p>
          * Feature is disabled by default, meaning that if the underlying data format
          * requires knowledge of all properties to output, attempts to read an unknown
          * property will result in a {@link JsonProcessingException}
          *
          * @since 2.6
          */
         IGNORE_UNDEFINED(false),

         // // // Other

         /**
          * Feature that determines whether {@link JsonLocation} instances should be constructed
          * with reference to source or not. If source reference is included, its type and contents
          * are included when `toString()` method is called (most notably when printing out parse
          * exception with that location information). If feature is disabled, no source reference
          * is passed and source is only indicated as "UNKNOWN".
          *<p>
          * Most common reason for disabling this feature is to avoid leaking information about
          * internal information; this may be done for security reasons.
          * Note that even if source reference is included, only parts of contents are usually
          * printed, and not the whole contents. Further, many source reference types can not
          * necessarily access contents (like streams), so only type is indicated, not contents.
          *<p>
          * Feature is enabled by default, meaning that "source reference" information is passed
          * and some or all of the source content may be included in {@link JsonLocation} information
          * constructed either when requested explicitly, or when needed for an exception.
          *
          * @since 2.9
          */
         INCLUDE_SOURCE_IN_LOCATION(true),

         ;

        /**
         * Whether feature is enabled or disabled by default.
         */
        private final boolean _defaultState;

        private final int _mask;

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         *
         * @return Bit mask of all features that are enabled by default
         */
        public static int collectDefaults()
        {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }

        private Feature(boolean defaultState) {
            _mask = (1 << ordinal());
            _defaultState = defaultState;
        }

        public boolean enabledByDefault() { return _defaultState; }

        public boolean enabledIn(int flags) { return (flags & _mask) != 0; }

        public int getMask() { return _mask; }
    }

    /*
    /**********************************************************
    /* Minimal configuration state
    /**********************************************************
     */

    /**
     * Bit flag composed of bits that indicate which
     * {@link com.typespec.json.implementation.jackson.core.JsonParser.Feature}s
     * are enabled.
     */
    protected int _features;

    /**
     * Optional container that holds the request payload which will be displayed on JSON parsing error.
     *
     * @since 2.8
     */
    protected transient RequestPayload _requestPayload;

    /*
    /**********************************************************
    /* Construction, configuration, initialization
    /**********************************************************
     */

    protected JsonParser() { }
    protected JsonParser(int features) { _features = features; }

    /**
     * Accessor for {@link ObjectCodec} associated with this
     * parser, if any. Codec is used by {@link #readValueAs(Class)}
     * method (and its variants).
     *
     * @return Codec assigned to this parser, if any; {@code null} if none
     */
    public abstract ObjectCodec getCodec();

    /**
     * Setter that allows defining {@link ObjectCodec} associated with this
     * parser, if any. Codec is used by {@link #readValueAs(Class)}
     * method (and its variants).
     *
     * @param oc Codec to assign, if any; {@code null} if none
     */
    public abstract void setCodec(ObjectCodec oc);

    /**
     * Method that can be used to get access to object that is used
     * to access input being parsed; this is usually either
     * {@link InputStream} or {@link Reader}, depending on what
     * parser was constructed with.
     * Note that returned value may be null in some cases; including
     * case where parser implementation does not want to exposed raw
     * source to caller.
     * In cases where input has been decorated, object returned here
     * is the decorated version; this allows some level of interaction
     * between users of parser and decorator object.
     *<p>
     * In general use of this accessor should be considered as
     * "last effort", i.e. only used if no other mechanism is applicable.
     *
     * @return Input source this parser was configured with
     */
    public Object getInputSource() { return null; }

    /**
     * Sets the payload to be passed if {@link JsonParseException} is thrown.
     *
     * @param payload Payload to pass
     *
     * @since 2.8
     */
    public void setRequestPayloadOnError(RequestPayload payload) {
        _requestPayload = payload;
    }

    /**
     * Sets the byte[] request payload and the charset
     *
     * @param payload Payload to pass
     * @param charset Character encoding for (lazily) decoding payload
     *
     * @since 2.8
     */
     public void setRequestPayloadOnError(byte[] payload, String charset) {
         _requestPayload = (payload == null) ? null : new RequestPayload(payload, charset);
     }

     /**
      * Sets the String request payload
      *
      * @param payload Payload to pass
      *
      * @since 2.8
      */
    public void setRequestPayloadOnError(String payload) {
        _requestPayload = (payload == null) ? null : new RequestPayload(payload);
    }

    /*
    /**********************************************************
    /* Format support
    /**********************************************************
     */

    /**
     * Method to call to make this parser use specified schema. Method must
     * be called before trying to parse any content, right after parser instance
     * has been created.
     * Note that not all parsers support schemas; and those that do usually only
     * accept specific types of schemas: ones defined for data format parser can read.
     *<p>
     * If parser does not support specified schema, {@link UnsupportedOperationException}
     * is thrown.
     *
     * @param schema Schema to use
     *
     * @throws UnsupportedOperationException if parser does not support schema
     */
    public void setSchema(FormatSchema schema) {
        throw new UnsupportedOperationException("Parser of type "+getClass().getName()+" does not support schema of type '"
                +schema.getSchemaType()+"'");
    }

    /**
     * Method for accessing Schema that this parser uses, if any.
     * Default implementation returns null.
     *
     * @return Schema in use by this parser, if any; {@code null} if none
     *
     * @since 2.1
     */
    public FormatSchema getSchema() { return null; }

    /**
     * Method that can be used to verify that given schema can be used with
     * this parser (using {@link #setSchema}).
     *
     * @param schema Schema to check
     *
     * @return True if this parser can use given schema; false if not
     */
    public boolean canUseSchema(FormatSchema schema) { return false; }

    /*
    /**********************************************************
    /* Capability introspection
    /**********************************************************
     */

    /**
     * Method that can be called to determine if a custom
     * {@link ObjectCodec} is needed for binding data parsed
     * using {@link JsonParser} constructed by this factory
     * (which typically also implies the same for serialization
     * with {@link JsonGenerator}).
     *
     * @return True if format-specific codec is needed with this parser; false if a general
     *   {@link ObjectCodec} is enough
     *
     * @since 2.1
     */
    public boolean requiresCustomCodec() { return false;}

    /**
     * Method that can be called to determine if this parser instance
     * uses non-blocking ("asynchronous") input access for decoding or not.
     * Access mode is determined by earlier calls via {@link JsonFactory};
     * it may not be changed after construction.
     *<p>
     * If non-blocking decoding is (@code true}, it is possible to call
     * {@link #getNonBlockingInputFeeder()} to obtain object to use
     * for feeding input; otherwise (<code>false</code> returned)
     * input is read by blocking
     *
     * @return True if this is a non-blocking ("asynchronous") parser
     *
     * @since 2.9
     */
    public boolean canParseAsync() { return false; }

    /**
     * Method that will either return a feeder instance (if parser uses
     * non-blocking, aka asynchronous access); or <code>null</code> for
     * parsers that use blocking I/O.
     *
     * @return Input feeder to use with non-blocking (async) parsing
     *
     * @since 2.9
     */
    public NonBlockingInputFeeder getNonBlockingInputFeeder() {
        return null;
    }

    /**
     * Accessor for getting metadata on capabilities of this parser, based on
     * underlying data format being read (directly or indirectly).
     *
     * @return Set of read capabilities for content to read via this parser
     *
     * @since 2.12
     */
    public JacksonFeatureSet<StreamReadCapability> getReadCapabilities() {
        return DEFAULT_READ_CAPABILITIES;
    }

    /*
    /**********************************************************
    /* Versioned
    /**********************************************************
     */

    /**
     * Accessor for getting version of the core package, given a parser instance.
     * Left for sub-classes to implement.
     *
     * @return Version of this generator (derived from version declared for
     *   {@code jackson-core} jar that contains the class
     */
    @Override
    public abstract Version version();

    /*
    /**********************************************************
    /* Closeable implementation
    /**********************************************************
     */

    /**
     * Closes the parser so that no further iteration or data access
     * can be made; will also close the underlying input source
     * if parser either <b>owns</b> the input source, or feature
     * {@link Feature#AUTO_CLOSE_SOURCE} is enabled.
     * Whether parser owns the input source depends on factory
     * method that was used to construct instance (so check
     * {@link com.typespec.json.implementation.jackson.core.JsonFactory} for details,
     * but the general
     * idea is that if caller passes in closable resource (such
     * as {@link InputStream} or {@link Reader}) parser does NOT
     * own the source; but if it passes a reference (such as
     * {@link java.io.File} or {@link java.net.URL} and creates
     * stream or reader it does own them.
     *
     * @throws IOException if there is either an underlying I/O problem
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Method that can be called to determine whether this parser
     * is closed or not. If it is closed, no new tokens can be
     * retrieved by calling {@link #nextToken} (and the underlying
     * stream may be closed). Closing may be due to an explicit
     * call to {@link #close} or because parser has encountered
     * end of input.
     *
     * @return {@code True} if this parser instance has been closed
     */
    public abstract boolean isClosed();

    /*
    /**********************************************************
    /* Public API, simple location, context accessors
    /**********************************************************
     */

    /**
     * Method that can be used to access current parsing context reader
     * is in. There are 3 different types: root, array and object contexts,
     * with slightly different available information. Contexts are
     * hierarchically nested, and can be used for example for figuring
     * out part of the input document that correspond to specific
     * array or object (for highlighting purposes, or error reporting).
     * Contexts can also be used for simple xpath-like matching of
     * input, if so desired.
     *
     * @return Stream input context ({@link JsonStreamContext}) associated with this parser
     */
    public abstract JsonStreamContext getParsingContext();

    /**
     * Method that returns location of the last processed input unit (character
     * or byte) from the input;
     * usually for error reporting purposes.
     *<p>
     * Note that the location is not guaranteed to be accurate (although most
     * implementation will try their best): some implementations may only
     * report specific boundary locations (start or end locations of tokens)
     * and others only return {@link JsonLocation#NA} due to not having access
     * to input location information (when delegating actual decoding work
     * to other library)
     *
     * @return Location of the last processed input unit (byte or character)
     *
     * @since 2.13
     */
    public JsonLocation currentLocation() {
        return getCurrentLocation();
    }

    /**
     * Method that return the <b>starting</b> location of the current
     * (most recently returned)
     * token; that is, the position of the first input unit (character or byte) from input
     * that starts the current token.
     *<p>
     * Note that the location is not guaranteed to be accurate (although most
     * implementation will try their best): some implementations may only
     * return {@link JsonLocation#NA} due to not having access
     * to input location information (when delegating actual decoding work
     * to other library)
     *
     * @return Starting location of the token parser currently points to
     *
     * @since 2.13 (will eventually replace {@link #getTokenLocation})
     */
    public JsonLocation currentTokenLocation() {
        return getTokenLocation();
    }

    // TODO: deprecate in 2.14 or later
    /**
     * Alias for {@link #currentLocation()}, to be deprecated in later
     * Jackson 2.x versions (and removed from Jackson 3.0).
     *
     * @return Location of the last processed input unit (byte or character)
     */
    public abstract JsonLocation getCurrentLocation();

    // TODO: deprecate in 2.14 or later
    /**
     * Alias for {@link #currentTokenLocation()}, to be deprecated in later
     * Jackson 2.x versions (and removed from Jackson 3.0).
     *
     * @return Starting location of the token parser currently points to
     */
    public abstract JsonLocation getTokenLocation();

    /**
     * Helper method, usually equivalent to:
     *<code>
     *   getParsingContext().getCurrentValue();
     *</code>
     *<p>
     * Note that "current value" is NOT populated (or used) by Streaming parser;
     * it is only used by higher-level data-binding functionality.
     * The reason it is included here is that it can be stored and accessed hierarchically,
     * and gets passed through data-binding.
     *
     * @return "Current value" associated with the current input context (state) of this parser
     *
     * @since 2.13 (added as replacement for older {@link #getCurrentValue()}
     */
    public Object currentValue() {
        // TODO: implement directly in 2.14 or later, make getCurrentValue() call this
        return getCurrentValue();
    }

    /**
     * Helper method, usually equivalent to:
     *<code>
     *   getParsingContext().setCurrentValue(v);
     *</code>
     *
     * @param v Current value to assign for the current input context of this parser
     *
     * @since 2.13 (added as replacement for older {@link #setCurrentValue}
     */
    public void assignCurrentValue(Object v) {
        // TODO: implement directly in 2.14 or later, make setCurrentValue() call this
        setCurrentValue(v);
    }

    // TODO: deprecate in 2.14 or later
    /**
     * Alias for {@link #currentValue()}, to be deprecated in later
     * Jackson 2.x versions (and removed from Jackson 3.0).
     *
     * @return Location of the last processed input unit (byte or character)
     */
    public Object getCurrentValue() {
        JsonStreamContext ctxt = getParsingContext();
        return (ctxt == null) ? null : ctxt.getCurrentValue();
    }

    // TODO: deprecate in 2.14 or later
    /**
     * Alias for {@link #assignCurrentValue}, to be deprecated in later
     * Jackson 2.x versions (and removed from Jackson 3.0).
     *
     * @param v Current value to assign for the current input context of this parser
     */
    public void setCurrentValue(Object v) {
        JsonStreamContext ctxt = getParsingContext();
        if (ctxt != null) {
            ctxt.setCurrentValue(v);
        }
    }

    /*
    /**********************************************************
    /* Buffer handling
    /**********************************************************
     */

    /**
     * Method that can be called to push back any content that
     * has been read but not consumed by the parser. This is usually
     * done after reading all content of interest using parser.
     * Content is released by writing it to given stream if possible;
     * if underlying input is byte-based it can released, if not (char-based)
     * it can not.
     *
     * @param out OutputStream to which buffered, undecoded content is written to
     *
     * @return -1 if the underlying content source is not byte based
     *    (that is, input can not be sent to {@link OutputStream};
     *    otherwise number of bytes released (0 if there was nothing to release)
     *
     * @throws IOException if write to stream threw exception
     */
    public int releaseBuffered(OutputStream out) throws IOException {
        return -1;
    }

    /**
     * Method that can be called to push back any content that
     * has been read but not consumed by the parser.
     * This is usually
     * done after reading all content of interest using parser.
     * Content is released by writing it to given writer if possible;
     * if underlying input is char-based it can released, if not (byte-based)
     * it can not.
     *
     * @param w Writer to which buffered but unprocessed content is written to
     *
     * @return -1 if the underlying content source is not char-based
     *    (that is, input can not be sent to {@link Writer};
     *    otherwise number of chars released (0 if there was nothing to release)
     *
     * @throws IOException if write using Writer threw exception
     */
    public int releaseBuffered(Writer w) throws IOException { return -1; }

    /*
    /***************************************************
    /* Public API, configuration
    /***************************************************
     */

    /**
     * Method for enabling specified parser feature
     * (check {@link Feature} for list of features)
     *
     * @param f Feature to enable
     *
     * @return This parser, to allow call chaining
     */
    public JsonParser enable(Feature f) {
        _features |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified  feature
     * (check {@link Feature} for list of features)
     *
     * @param f Feature to disable
     *
     * @return This parser, to allow call chaining
     */
    public JsonParser disable(Feature f) {
        _features &= ~f.getMask();
        return this;
    }

    /**
     * Method for enabling or disabling specified feature
     * (check {@link Feature} for list of features)
     *
     * @param f Feature to enable or disable
     * @param state Whether to enable feature ({@code true}) or disable ({@code false})
     *
     * @return This parser, to allow call chaining
     */
    public JsonParser configure(Feature f, boolean state) {
        if (state) enable(f); else disable(f);
        return this;
    }

    /**
     * Method for checking whether specified {@link Feature} is enabled.
     *
     * @param f Feature to check
     *
     * @return {@code True} if feature is enabled; {@code false} otherwise
     */
    public boolean isEnabled(Feature f) { return f.enabledIn(_features); }

    /**
     * Method for checking whether specified {@link Feature} is enabled.
     *
     * @param f Feature to check
     *
     * @return {@code True} if feature is enabled; {@code false} otherwise
     *
     * @since 2.10
     */
    public boolean isEnabled(StreamReadFeature f) { return f.mappedFeature().enabledIn(_features); }

    /**
     * Bulk access method for getting state of all standard {@link Feature}s.
     *
     * @return Bit mask that defines current states of all standard {@link Feature}s.
     *
     * @since 2.3
     */
    public int getFeatureMask() { return _features; }

    /**
     * Bulk set method for (re)setting states of all standard {@link Feature}s
     *
     * @param mask Bit mask that defines set of features to enable
     *
     * @return This parser, to allow call chaining
     *
     * @since 2.3
     * @deprecated Since 2.7, use {@link #overrideStdFeatures(int, int)} instead
     */
    @Deprecated
    public JsonParser setFeatureMask(int mask) {
        _features = mask;
        return this;
    }

    /**
     * Bulk set method for (re)setting states of features specified by <code>mask</code>.
     * Functionally equivalent to
     *<code>
     *    int oldState = getFeatureMask();
     *    int newState = (oldState &amp; ~mask) | (values &amp; mask);
     *    setFeatureMask(newState);
     *</code>
     * but preferred as this lets caller more efficiently specify actual changes made.
     *
     * @param values Bit mask of set/clear state for features to change
     * @param mask Bit mask of features to change
     *
     * @return This parser, to allow call chaining
     *
     * @since 2.6
     */
    public JsonParser overrideStdFeatures(int values, int mask) {
        int newState = (_features & ~mask) | (values & mask);
        return setFeatureMask(newState);
    }

    /**
     * Bulk access method for getting state of all {@link FormatFeature}s, format-specific
     * on/off configuration settings.
     *
     * @return Bit mask that defines current states of all standard {@link FormatFeature}s.
     *
     * @since 2.6
     */
    public int getFormatFeatures() {
        return 0;
    }

    /**
     * Bulk set method for (re)setting states of {@link FormatFeature}s,
     * by specifying values (set / clear) along with a mask, to determine
     * which features to change, if any.
     *<p>
     * Default implementation will simply throw an exception to indicate that
     * the parser implementation does not support any {@link FormatFeature}s.
     *
     * @param values Bit mask of set/clear state for features to change
     * @param mask Bit mask of features to change
     *
     * @return This parser, to allow call chaining
     *
     * @since 2.6
     */
    public JsonParser overrideFormatFeatures(int values, int mask) {
        // 08-Oct-2018, tatu: For 2.10 we actually do get `JsonReadFeature`s, although they
        //    are (for 2.x only, not for 3.x) mapper to legacy settings. So do not throw exception:
//        throw new IllegalArgumentException("No FormatFeatures defined for parser of type "+getClass().getName());
        return this;
    }

    /*
    /**********************************************************
    /* Public API, traversal
    /**********************************************************
     */

    /**
     * Main iteration method, which will advance stream enough
     * to determine type of the next token, if any. If none
     * remaining (stream has no content other than possible
     * white space before ending), null will be returned.
     *
     * @return Next token from the stream, if any found, or null
     *   to indicate end-of-input
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract JsonToken nextToken() throws IOException;

    /**
     * Iteration method that will advance stream enough
     * to determine type of the next token that is a value type
     * (including JSON Array and Object start/end markers).
     * Or put another way, nextToken() will be called once,
     * and if {@link JsonToken#FIELD_NAME} is returned, another
     * time to get the value for the field.
     * Method is most useful for iterating over value entries
     * of JSON objects; field name will still be available
     * by calling {@link #getCurrentName} when parser points to
     * the value.
     *
     * @return Next non-field-name token from the stream, if any found,
     *   or null to indicate end-of-input (or, for non-blocking
     *   parsers, {@link JsonToken#NOT_AVAILABLE} if no tokens were
     *   available yet)
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract JsonToken nextValue() throws IOException;

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * verifies whether it is {@link JsonToken#FIELD_NAME} with specified name
     * and returns result of that comparison.
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.FIELD_NAME) &amp;&amp; str.getValue().equals(getCurrentName());
     *</pre>
     * but may be faster for parser to verify, and can therefore be used if caller
     * expects to get such a property name from input next.
     *
     * @param str Property name to compare next token to (if next token is
     *   <code>JsonToken.FIELD_NAME</code>)
     *
     * @return {@code True} if parser advanced to {@code JsonToken.FIELD_NAME} with
     *    specified name; {@code false} otherwise (different token or non-matching name)
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public boolean nextFieldName(SerializableString str) throws IOException {
        return (nextToken() == JsonToken.FIELD_NAME) && str.getValue().equals(getCurrentName());
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * verifies whether it is {@link JsonToken#FIELD_NAME}; if it is,
     * returns same as {@link #getCurrentName()}, otherwise null.
     *
     * @return Name of the the {@code JsonToken.FIELD_NAME} parser advanced to, if any;
     *   {@code null} if next token is of some other type
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.5
     */
    public String nextFieldName() throws IOException {
        return (nextToken() == JsonToken.FIELD_NAME) ? getCurrentName() : null;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_STRING} returns contained String value;
     * otherwise returns null.
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.VALUE_STRING) ? getText() : null;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get a String value next from input.
     *
     * @return Text value of the {@code JsonToken.VALUE_STRING} token parser advanced
     *   to; or {@code null} if next token is of some other type
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public String nextTextValue() throws IOException {
        return (nextToken() == JsonToken.VALUE_STRING) ? getText() : null;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_NUMBER_INT} returns 32-bit int value;
     * otherwise returns specified default value
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getIntValue() : defaultValue;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get an int value next from input.
     *<p>
     * NOTE: value checks are performed similar to {@link #getIntValue()}
     *
     * @param defaultValue Value to return if next token is NOT of type {@code JsonToken.VALUE_NUMBER_INT}
     *
     * @return Integer ({@code int}) value of the {@code JsonToken.VALUE_NUMBER_INT} token parser advanced
     *   to; or {@code defaultValue} if next token is of some other type
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     * @throws InputCoercionException if integer number does not fit in Java {@code int}
     */
    public int nextIntValue(int defaultValue) throws IOException {
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getIntValue() : defaultValue;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_NUMBER_INT} returns 64-bit long value;
     * otherwise returns specified default value
     * It is functionally equivalent to:
     *<pre>
     *  return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getLongValue() : defaultValue;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get a long value next from input.
     *<p>
     * NOTE: value checks are performed similar to {@link #getLongValue()}
     *
     * @param defaultValue Value to return if next token is NOT of type {@code JsonToken.VALUE_NUMBER_INT}
     *
     * @return {@code long} value of the {@code JsonToken.VALUE_NUMBER_INT} token parser advanced
     *   to; or {@code defaultValue} if next token is of some other type
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     * @throws InputCoercionException if integer number does not fit in Java {@code long}
     */
    public long nextLongValue(long defaultValue) throws IOException {
        return (nextToken() == JsonToken.VALUE_NUMBER_INT) ? getLongValue() : defaultValue;
    }

    /**
     * Method that fetches next token (as if calling {@link #nextToken}) and
     * if it is {@link JsonToken#VALUE_TRUE} or {@link JsonToken#VALUE_FALSE}
     * returns matching Boolean value; otherwise return null.
     * It is functionally equivalent to:
     *<pre>
     *  JsonToken t = nextToken();
     *  if (t == JsonToken.VALUE_TRUE) return Boolean.TRUE;
     *  if (t == JsonToken.VALUE_FALSE) return Boolean.FALSE;
     *  return null;
     *</pre>
     * but may be faster for parser to process, and can therefore be used if caller
     * expects to get a Boolean value next from input.
     *
     * @return {@code Boolean} value of the {@code JsonToken.VALUE_TRUE} or {@code JsonToken.VALUE_FALSE}
     *   token parser advanced to; or {@code null} if next token is of some other type
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public Boolean nextBooleanValue() throws IOException {
        JsonToken t = nextToken();
        if (t == JsonToken.VALUE_TRUE) { return Boolean.TRUE; }
        if (t == JsonToken.VALUE_FALSE) { return Boolean.FALSE; }
        return null;
    }

    /**
     * Method that will skip all child tokens of an array or
     * object token that the parser currently points to,
     * iff stream points to
     * {@link JsonToken#START_OBJECT} or {@link JsonToken#START_ARRAY}.
     * If not, it will do nothing.
     * After skipping, stream will point to <b>matching</b>
     * {@link JsonToken#END_OBJECT} or {@link JsonToken#END_ARRAY}
     * (possibly skipping nested pairs of START/END OBJECT/ARRAY tokens
     * as well as value tokens).
     * The idea is that after calling this method, application
     * will call {@link #nextToken} to point to the next
     * available token, if any.
     *
     * @return This parser, to allow call chaining
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract JsonParser skipChildren() throws IOException;

    /**
     * Method that may be used to force full handling of the current token
     * so that even if lazy processing is enabled, the whole contents are
     * read for possible retrieval. This is usually used to ensure that
     * the token end location is available, as well as token contents
     * (similar to what calling, say {@link #getTextCharacters()}, would
     * achieve).
     *<p>
     * Note that for many dataformat implementations this method
     * will not do anything; this is the default implementation unless
     * overridden by sub-classes.
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.8
     */
    public void finishToken() throws IOException {
        ; // nothing
    }

    /*
    /**********************************************************
    /* Public API, simple token id/type access
    /**********************************************************
     */

    /**
     * Accessor to find which token parser currently points to, if any;
     * null will be returned if none.
     * If return value is non-null, data associated with the token
     * is available via other accessor methods.
     *
     * @return Type of the token this parser currently points to,
     *   if any: null before any tokens have been read, and
     *   after end-of-input has been encountered, as well as
     *   if the current token has been explicitly cleared.
     *
     * @since 2.8
     */
    public JsonToken currentToken() {
        return getCurrentToken();
    }

    /**
     * Method similar to {@link #getCurrentToken()} but that returns an
     * <code>int</code> instead of {@link JsonToken} (enum value).
     *<p>
     * Use of int directly is typically more efficient on switch statements,
     * so this method may be useful when building low-overhead codecs.
     * Note, however, that effect may not be big enough to matter: make sure
     * to profile performance before deciding to use this method.
     *
     * @since 2.8
     *
     * @return {@code int} matching one of constants from {@link JsonTokenId}.
     */
    public int currentTokenId() {
        return getCurrentTokenId();
    }

    // TODO: deprecate in 2.14 or later
    /**
     * Alias for {@link #currentToken()}, may be deprecated sometime after
     * Jackson 2.13 (will be removed from 3.0).
     *
     * @return Type of the token this parser currently points to,
     *   if any: null before any tokens have been read, and
     */
    public abstract JsonToken getCurrentToken();

    /**
     * Deprecated alias for {@link #currentTokenId()}.
     *
     * @return {@code int} matching one of constants from {@link JsonTokenId}.
     *
     * @deprecated Since 2.12 use {@link #currentTokenId} instead
     */
    @Deprecated
    public abstract int getCurrentTokenId();

    /**
     * Method for checking whether parser currently points to
     * a token (and data for that token is available).
     * Equivalent to check for <code>parser.getCurrentToken() != null</code>.
     *
     * @return True if the parser just returned a valid
     *   token via {@link #nextToken}; false otherwise (parser
     *   was just constructed, encountered end-of-input
     *   and returned null from {@link #nextToken}, or the token
     *   has been consumed)
     */
    public abstract boolean hasCurrentToken();

    /**
     * Method that is functionally equivalent to:
     *<code>
     *  return currentTokenId() == id
     *</code>
     * but may be more efficiently implemented.
     *<p>
     * Note that no traversal or conversion is performed; so in some
     * cases calling method like {@link #isExpectedStartArrayToken()}
     * is necessary instead.
     *
     * @param id Token id to match (from (@link JsonTokenId})
     *
     * @return {@code True} if the parser current points to specified token
     *
     * @since 2.5
     */
    public abstract boolean hasTokenId(int id);

    /**
     * Method that is functionally equivalent to:
     *<code>
     *  return currentToken() == t
     *</code>
     * but may be more efficiently implemented.
     *<p>
     * Note that no traversal or conversion is performed; so in some
     * cases calling method like {@link #isExpectedStartArrayToken()}
     * is necessary instead.
     *
     * @param t Token to match
     *
     * @return {@code True} if the parser current points to specified token
     *
     * @since 2.6
     */
    public abstract boolean hasToken(JsonToken t);

    /**
     * Specialized accessor that can be used to verify that the current
     * token indicates start array (usually meaning that current token
     * is {@link JsonToken#START_ARRAY}) when start array is expected.
     * For some specialized parsers this can return true for other cases
     * as well; this is usually done to emulate arrays in cases underlying
     * format is ambiguous (XML, for example, has no format-level difference
     * between Objects and Arrays; it just has elements).
     *<p>
     * Default implementation is equivalent to:
     *<pre>
     *   currentToken() == JsonToken.START_ARRAY
     *</pre>
     * but may be overridden by custom parser implementations.
     *
     * @return True if the current token can be considered as a
     *   start-array marker (such {@link JsonToken#START_ARRAY});
     *   {@code false} if not
     */
    public boolean isExpectedStartArrayToken() { return currentToken() == JsonToken.START_ARRAY; }

    /**
     * Similar to {@link #isExpectedStartArrayToken()}, but checks whether stream
     * currently points to {@link JsonToken#START_OBJECT}.
     *
     * @return True if the current token can be considered as a
     *   start-array marker (such {@link JsonToken#START_OBJECT});
     *   {@code false} if not
     *
     * @since 2.5
     */
    public boolean isExpectedStartObjectToken() { return currentToken() == JsonToken.START_OBJECT; }

    /**
     * Similar to {@link #isExpectedStartArrayToken()}, but checks whether stream
     * currently points to {@link JsonToken#VALUE_NUMBER_INT}.
     *<p>
     * The initial use case is for XML backend to efficiently (attempt to) coerce
     * textual content into numbers.
     *
     * @return True if the current token can be considered as a
     *   start-array marker (such {@link JsonToken#VALUE_NUMBER_INT});
     *   {@code false} if not
     *
     * @since 2.12
     */
    public boolean isExpectedNumberIntToken() { return currentToken() == JsonToken.VALUE_NUMBER_INT; }

    /**
     * Access for checking whether current token is a numeric value token, but
     * one that is of "not-a-number" (NaN) variety (including both "NaN" AND
     * positive/negative infinity!): not supported by all formats,
     * but often supported for {@link JsonToken#VALUE_NUMBER_FLOAT}.
     * NOTE: roughly equivalent to calling <code>!Double.isFinite()</code>
     * on value you would get from calling {@link #getDoubleValue()}.
     *
     * @return {@code True} if the current token is of type {@link JsonToken#VALUE_NUMBER_FLOAT}
     *   but represents a "Not a Number"; {@code false} for other tokens and regular
     *   floating-point numbers
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.9
     */
    public boolean isNaN() throws IOException {
        return false;
    }

    /*
    /**********************************************************
    /* Public API, token state overrides
    /**********************************************************
     */

    /**
     * Method called to "consume" the current token by effectively
     * removing it so that {@link #hasCurrentToken} returns false, and
     * {@link #getCurrentToken} null).
     * Cleared token value can still be accessed by calling
     * {@link #getLastClearedToken} (if absolutely needed), but
     * usually isn't.
     *<p>
     * Method was added to be used by the optional data binder, since
     * it has to be able to consume last token used for binding (so that
     * it will not be used again).
     */
    public abstract void clearCurrentToken();

    /**
     * Method that can be called to get the last token that was
     * cleared using {@link #clearCurrentToken}. This is not necessarily
     * the latest token read.
     * Will return null if no tokens have been cleared,
     * or if parser has been closed.
     *
     * @return Last cleared token, if any; {@code null} otherwise
     */
    public abstract JsonToken getLastClearedToken();

    /**
     * Method that can be used to change what is considered to be
     * the current (field) name.
     * May be needed to support non-JSON data formats or unusual binding
     * conventions; not needed for typical processing.
     *<p>
     * Note that use of this method should only be done as sort of last
     * resort, as it is a work-around for regular operation.
     *
     * @param name Name to use as the current name; may be null.
     */
    public abstract void overrideCurrentName(String name);

    /*
    /**********************************************************
    /* Public API, access to token information, text
    /**********************************************************
     */

    // TODO: deprecate in 2.14 or later
    /**
     * Alias of {@link #currentName()}.
     *
     * @return Name of the current field in the parsing context
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract String getCurrentName() throws IOException;

    /**
     * Method that can be called to get the name associated with
     * the current token: for {@link JsonToken#FIELD_NAME}s it will
     * be the same as what {@link #getText} returns;
     * for field values it will be preceding field name;
     * and for others (array values, root-level values) null.
     *
     * @return Name of the current field in the parsing context
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.10
     */
    public String currentName() throws IOException {
        return getCurrentName();
    }

    /**
     * Method for accessing textual representation of the current token;
     * if no current token (before first call to {@link #nextToken}, or
     * after encountering end-of-input), returns null.
     * Method can be called for any token type.
     *
     * @return Textual value associated with the current token (one returned
     *   by {@link #nextToken()} or other iteration methods)
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract String getText() throws IOException;

    /**
     * Method to read the textual representation of the current token in chunks and
     * pass it to the given Writer.
     * Conceptually same as calling:
     *<pre>
     *  writer.write(parser.getText());
     *</pre>
     * but should typically be more efficient as longer content does need to
     * be combined into a single <code>String</code> to return, and write
     * can occur directly from intermediate buffers Jackson uses.
     *
     * @param writer Writer to write textual content to
     *
     * @return The number of characters written to the Writer
     *
     * @throws IOException for low-level read issues or writes using passed
     *   {@code writer}, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.8
     */
    public int getText(Writer writer) throws IOException, UnsupportedOperationException
    {
        String str = getText();
        if (str == null) {
            return 0;
        }
        writer.write(str);
        return str.length();
    }

    /**
     * Method similar to {@link #getText}, but that will return
     * underlying (unmodifiable) character array that contains
     * textual value, instead of constructing a String object
     * to contain this information.
     * Note, however, that:
     *<ul>
     * <li>Textual contents are not guaranteed to start at
     *   index 0 (rather, call {@link #getTextOffset}) to
     *   know the actual offset
     *  </li>
     * <li>Length of textual contents may be less than the
     *  length of returned buffer: call {@link #getTextLength}
     *  for actual length of returned content.
     *  </li>
     * </ul>
     *<p>
     * Note that caller <b>MUST NOT</b> modify the returned
     * character array in any way -- doing so may corrupt
     * current parser state and render parser instance useless.
     *<p>
     * The only reason to call this method (over {@link #getText})
     * is to avoid construction of a String object (which
     * will make a copy of contents).
     *
     * @return Buffer that contains the current textual value (but not necessarily
     *    at offset 0, and not necessarily until the end of buffer)
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract char[] getTextCharacters() throws IOException;

    /**
     * Accessor used with {@link #getTextCharacters}, to know length
     * of String stored in returned buffer.
     *
     * @return Number of characters within buffer returned
     *   by {@link #getTextCharacters} that are part of
     *   textual content of the current token.
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract int getTextLength() throws IOException;

    /**
     * Accessor used with {@link #getTextCharacters}, to know offset
     * of the first text content character within buffer.
     *
     * @return Offset of the first character within buffer returned
     *   by {@link #getTextCharacters} that is part of
     *   textual content of the current token.
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract int getTextOffset() throws IOException;

    /**
     * Method that can be used to determine whether calling of
     * {@link #getTextCharacters} would be the most efficient
     * way to access textual content for the event parser currently
     * points to.
     *<p>
     * Default implementation simply returns false since only actual
     * implementation class has knowledge of its internal buffering
     * state.
     * Implementations are strongly encouraged to properly override
     * this method, to allow efficient copying of content by other
     * code.
     *
     * @return True if parser currently has character array that can
     *   be efficiently returned via {@link #getTextCharacters}; false
     *   means that it may or may not exist
     */
    public abstract boolean hasTextCharacters();

    /*
    /**********************************************************
    /* Public API, access to token information, numeric
    /**********************************************************
     */

    /**
     * Generic number value accessor method that will work for
     * all kinds of numeric values. It will return the optimal
     * (simplest/smallest possible) wrapper object that can
     * express the numeric value just parsed.
     *
     * @return Numeric value of the current token in its most optimal
     *   representation
     *
     * @throws IOException Problem with access: {@link JsonParseException} if
     *    the current token is not numeric, or if decoding of the value fails
     *    (invalid format for numbers); plain {@link IOException} if underlying
     *    content read fails (possible if values are extracted lazily)
     */
    public abstract Number getNumberValue() throws IOException;

    /**
     * Method similar to {@link #getNumberValue} with the difference that
     * for floating-point numbers value returned may be {@link BigDecimal}
     * if the underlying format does not store floating-point numbers using
     * native representation: for example, textual formats represent numbers
     * as Strings (which are 10-based), and conversion to {@link java.lang.Double}
     * is potentially lossy operation.
     *<p>
     * Default implementation simply returns {@link #getNumberValue()}
     *
     * @return Numeric value of the current token using most accurate representation
     *
     * @throws IOException Problem with access: {@link JsonParseException} if
     *    the current token is not numeric, or if decoding of the value fails
     *    (invalid format for numbers); plain {@link IOException} if underlying
     *    content read fails (possible if values are extracted lazily)
     *
     * @since 2.12
     */
    public Number getNumberValueExact() throws IOException {
        return getNumberValue();
    }

    /**
     * If current token is of type
     * {@link JsonToken#VALUE_NUMBER_INT} or
     * {@link JsonToken#VALUE_NUMBER_FLOAT}, returns
     * one of {@link NumberType} constants; otherwise returns null.
     *
     * @return Type of current number, if parser points to numeric token; {@code null} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract NumberType getNumberType() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a value of Java byte primitive type.
     * Note that in addition to "natural" input range of {@code [-128, 127]},
     * this also allows "unsigned 8-bit byte" values {@code [128, 255]}:
     * but for this range value will be translated by truncation, leading
     * to sign change.
     *<p>
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the resulting integer value falls outside range of
     * {@code [-128, 255]},
     * a {@link InputCoercionException}
     * will be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code byte} (if numeric token within
     *   range of {@code [-128, 255]}); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public byte getByteValue() throws IOException {
        int value = getIntValue();
        // So far so good: but does it fit?
        // [JACKSON-804]: Let's actually allow range of [-128, 255], as those are uniquely mapped
        //  (instead of just signed range of [-128, 127])
        if (value < MIN_BYTE_I || value > MAX_BYTE_I) {
            throw new InputCoercionException(this,
                    String.format("Numeric value (%s) out of range of Java byte", getText()),
                    JsonToken.VALUE_NUMBER_INT, Byte.TYPE);
        }
        return (byte) value;
    }

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a value of Java short primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the resulting integer value falls outside range of
     * Java short, a {@link InputCoercionException}
     * will be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code short} (if numeric token within
     *   Java 16-bit signed {@code short} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public short getShortValue() throws IOException
    {
        int value = getIntValue();
        if (value < MIN_SHORT_I || value > MAX_SHORT_I) {
            throw new InputCoercionException(this,
                    String.format("Numeric value (%s) out of range of Java short", getText()),
                    JsonToken.VALUE_NUMBER_INT, Short.TYPE);
        }
        return (short) value;
    }

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a value of Java int primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the resulting integer value falls outside range of
     * Java {@code int}, a {@link InputCoercionException}
     * may be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code int} (if numeric token within
     *   Java 32-bit signed {@code int} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract int getIntValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can be expressed as a Java long primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDoubleValue}
     * and then casting to int; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the token is an integer, but its value falls
     * outside of range of Java long, a {@link InputCoercionException}
     * may be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code long} (if numeric token within
     *   Java 32-bit signed {@code long} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract long getLongValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_INT} and
     * it can not be used as a Java long primitive type due to its
     * magnitude.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_FLOAT};
     * if so, it is equivalent to calling {@link #getDecimalValue}
     * and then constructing a {@link BigInteger} from that value.
     *
     * @return Current number value as {@link BigInteger} (if numeric token);
     *     otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract BigInteger getBigIntegerValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} and
     * it can be expressed as a Java float primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_INT};
     * if so, it is equivalent to calling {@link #getLongValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the value falls
     * outside of range of Java float, a {@link InputCoercionException}
     * will be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code float} (if numeric token within
     *   Java {@code float} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract float getFloatValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} and
     * it can be expressed as a Java double primitive type.
     * It can also be called for {@link JsonToken#VALUE_NUMBER_INT};
     * if so, it is equivalent to calling {@link #getLongValue}
     * and then casting; except for possible overflow/underflow
     * exception.
     *<p>
     * Note: if the value falls
     * outside of range of Java double, a {@link InputCoercionException}
     * will be thrown to indicate numeric overflow/underflow.
     *
     * @return Current number value as {@code double} (if numeric token within
     *   Java {@code double} range); otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract double getDoubleValue() throws IOException;

    /**
     * Numeric accessor that can be called when the current
     * token is of type {@link JsonToken#VALUE_NUMBER_FLOAT} or
     * {@link JsonToken#VALUE_NUMBER_INT}. No under/overflow exceptions
     * are ever thrown.
     *
     * @return Current number value as {@link BigDecimal} (if numeric token);
     *   otherwise exception thrown
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract BigDecimal getDecimalValue() throws IOException;

    /*
    /**********************************************************
    /* Public API, access to token information, other
    /**********************************************************
     */

    /**
     * Convenience accessor that can be called when the current
     * token is {@link JsonToken#VALUE_TRUE} or
     * {@link JsonToken#VALUE_FALSE}, to return matching {@code boolean}
     * value.
     * If the current token is of some other type, {@link JsonParseException}
     * will be thrown
     *
     * @return {@code True} if current token is {@code JsonToken.VALUE_TRUE},
     *   {@code false} if current token is {@code JsonToken.VALUE_FALSE};
     *   otherwise throws {@link JsonParseException}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public boolean getBooleanValue() throws IOException {
        JsonToken t = currentToken();
        if (t == JsonToken.VALUE_TRUE) return true;
        if (t == JsonToken.VALUE_FALSE) return false;
        throw new JsonParseException(this,
            String.format("Current token (%s) not of boolean type", t))
                .withRequestPayload(_requestPayload);
    }

    /**
     * Accessor that can be called if (and only if) the current token
     * is {@link JsonToken#VALUE_EMBEDDED_OBJECT}. For other token types,
     * null is returned.
     *<p>
     * Note: only some specialized parser implementations support
     * embedding of objects (usually ones that are facades on top
     * of non-streaming sources, such as object trees). One exception
     * is access to binary content (whether via base64 encoding or not)
     * which typically is accessible using this method, as well as
     * {@link #getBinaryValue()}.
     *
     * @return Embedded value (usually of "native" type supported by format)
     *   for the current token, if any; {@code null otherwise}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public Object getEmbeddedObject() throws IOException { return null; }

    /*
    /**********************************************************
    /* Public API, access to token information, binary
    /**********************************************************
     */

    /**
     * Method that can be used to read (and consume -- results
     * may not be accessible using other methods after the call)
     * base64-encoded binary data
     * included in the current textual JSON value.
     * It works similar to getting String value via {@link #getText}
     * and decoding result (except for decoding part),
     * but should be significantly more performant.
     *<p>
     * Note that non-decoded textual contents of the current token
     * are not guaranteed to be accessible after this method
     * is called. Current implementation, for example, clears up
     * textual content during decoding.
     * Decoded binary content, however, will be retained until
     * parser is advanced to the next event.
     *
     * @param bv Expected variant of base64 encoded
     *   content (see {@link Base64Variants} for definitions
     *   of "standard" variants).
     *
     * @return Decoded binary data
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public abstract byte[] getBinaryValue(Base64Variant bv) throws IOException;

    /**
     * Convenience alternative to {@link #getBinaryValue(Base64Variant)}
     * that defaults to using
     * {@link Base64Variants#getDefaultVariant} as the default encoding.
     *
     * @return Decoded binary data
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public byte[] getBinaryValue() throws IOException {
        return getBinaryValue(Base64Variants.getDefaultVariant());
    }

    /**
     * Method that can be used as an alternative to {@link #getBigIntegerValue()},
     * especially when value can be large. The main difference (beyond method
     * of returning content using {@link OutputStream} instead of as byte array)
     * is that content will NOT remain accessible after method returns: any content
     * processed will be consumed and is not buffered in any way. If caller needs
     * buffering, it has to implement it.
     *
     * @param out Output stream to use for passing decoded binary data
     *
     * @return Number of bytes that were decoded and written via {@link OutputStream}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.1
     */
    public int readBinaryValue(OutputStream out) throws IOException {
        return readBinaryValue(Base64Variants.getDefaultVariant(), out);
    }

    /**
     * Similar to {@link #readBinaryValue(OutputStream)} but allows explicitly
     * specifying base64 variant to use.
     *
     * @param bv base64 variant to use
     * @param out Output stream to use for passing decoded binary data
     *
     * @return Number of bytes that were decoded and written via {@link OutputStream}
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.1
     */
    public int readBinaryValue(Base64Variant bv, OutputStream out) throws IOException {
        _reportUnsupportedOperation();
        return 0; // never gets here
    }

    /*
    /**********************************************************
    /* Public API, access to token information, coercion/conversion
    /**********************************************************
     */

    /**
     * Method that will try to convert value of current token to a
     * Java {@code int} value.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured type
     * markers like start/end Object/Array)
     * default value of <b>0</b> will be returned; no exceptions are thrown.
     *
     * @return {@code int} value current token is converted to, if possible; exception thrown
     *    otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public int getValueAsInt() throws IOException {
        return getValueAsInt(0);
    }

    /**
     * Method that will try to convert value of current token to a
     * <b>int</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to an int (including structured type
     * markers like start/end Object/Array)
     * specified <b>def</b> will be returned; no exceptions are thrown.
     *
     * @param def Default value to return if conversion to {@code int} is not possible
     *
     * @return {@code int} value current token is converted to, if possible; {@code def} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public int getValueAsInt(int def) throws IOException { return def; }

    /**
     * Method that will try to convert value of current token to a
     * <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to a long (including structured type
     * markers like start/end Object/Array)
     * default value of <b>0L</b> will be returned; no exceptions are thrown.
     *
     * @return {@code long} value current token is converted to, if possible; exception thrown
     *    otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public long getValueAsLong() throws IOException {
        return getValueAsLong(0);
    }

    /**
     * Method that will try to convert value of current token to a
     * <b>long</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0 (false)
     * and 1 (true), and Strings are parsed using default Java language integer
     * parsing rules.
     *<p>
     * If representation can not be converted to a long (including structured type
     * markers like start/end Object/Array)
     * specified <b>def</b> will be returned; no exceptions are thrown.
     *
     * @param def Default value to return if conversion to {@code long} is not possible
     *
     * @return {@code long} value current token is converted to, if possible; {@code def} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public long getValueAsLong(long def) throws IOException {
        return def;
    }

    /**
     * Method that will try to convert value of current token to a Java
     * <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language floating
     * point parsing rules.
     *<p>
     * If representation can not be converted to a double (including structured types
     * like Objects and Arrays),
     * default value of <b>0.0</b> will be returned; no exceptions are thrown.
     *
     * @return {@code double} value current token is converted to, if possible; exception thrown
     *    otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public double getValueAsDouble() throws IOException {
        return getValueAsDouble(0.0);
    }

    /**
     * Method that will try to convert value of current token to a
     * Java <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language floating
     * point parsing rules.
     *<p>
     * If representation can not be converted to a double (including structured types
     * like Objects and Arrays),
     * specified <b>def</b> will be returned; no exceptions are thrown.
     *
     * @param def Default value to return if conversion to {@code double} is not possible
     *
     * @return {@code double} value current token is converted to, if possible; {@code def} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public double getValueAsDouble(double def) throws IOException {
        return def;
    }

    /**
     * Method that will try to convert value of current token to a
     * <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * default value of <b>false</b> will be returned; no exceptions are thrown.
     *
     * @return {@code boolean} value current token is converted to, if possible; exception thrown
     *    otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public boolean getValueAsBoolean() throws IOException {
        return getValueAsBoolean(false);
    }

    /**
     * Method that will try to convert value of current token to a
     * <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     *<p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * specified <b>def</b> will be returned; no exceptions are thrown.
     *
     * @param def Default value to return if conversion to {@code boolean} is not possible
     *
     * @return {@code boolean} value current token is converted to, if possible; {@code def} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     */
    public boolean getValueAsBoolean(boolean def) throws IOException {
        return def;
    }

    /**
     * Method that will try to convert value of current token to a
     * {@link java.lang.String}.
     * JSON Strings map naturally; scalar values get converted to
     * their textual representation.
     * If representation can not be converted to a String value (including structured types
     * like Objects and Arrays and {@code null} token), default value of
     * <b>null</b> will be returned; no exceptions are thrown.
     *
     * @return {@link String} value current token is converted to, if possible; {@code null} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.1
     */
    public String getValueAsString() throws IOException {
        return getValueAsString(null);
    }

    /**
     * Method that will try to convert value of current token to a
     * {@link java.lang.String}.
     * JSON Strings map naturally; scalar values get converted to
     * their textual representation.
     * If representation can not be converted to a String value (including structured types
     * like Objects and Arrays and {@code null} token), specified default value
     * will be returned; no exceptions are thrown.
     *
     * @param def Default value to return if conversion to {@code String} is not possible
     *
     * @return {@link String} value current token is converted to, if possible; {@code def} otherwise
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.1
     */
    public abstract String getValueAsString(String def) throws IOException;

    /*
    /**********************************************************
    /* Public API, Native Ids (type, object)
    /**********************************************************
     */

    /**
     * Introspection method that may be called to see if the underlying
     * data format supports some kind of Object Ids natively (many do not;
     * for example, JSON doesn't).
     *<p>
     * Default implementation returns true; overridden by data formats
     * that do support native Object Ids. Caller is expected to either
     * use a non-native notation (explicit property or such), or fail,
     * in case it can not use native object ids.
     *
     * @return {@code True} if the format being read supports native Object Ids;
     *    {@code false} if not
     *
     * @since 2.3
     */
    public boolean canReadObjectId() { return false; }

    /**
     * Introspection method that may be called to see if the underlying
     * data format supports some kind of Type Ids natively (many do not;
     * for example, JSON doesn't).
     *<p>
     * Default implementation returns true; overridden by data formats
     * that do support native Type Ids. Caller is expected to either
     * use a non-native notation (explicit property or such), or fail,
     * in case it can not use native type ids.
     *
     * @return {@code True} if the format being read supports native Type Ids;
     *    {@code false} if not
     *
     * @since 2.3
     */
    public boolean canReadTypeId() { return false; }

    /**
     * Method that can be called to check whether current token
     * (one that was just read) has an associated Object id, and if
     * so, return it.
     * Note that while typically caller should check with {@link #canReadObjectId}
     * first, it is not illegal to call this method even if that method returns
     * true; but if so, it will return null. This may be used to simplify calling
     * code.
     *<p>
     * Default implementation will simply return null.
     *
     * @return Native Object id associated with the current token, if any; {@code null} if none
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.3
     */
    public Object getObjectId() throws IOException { return null; }

    /**
     * Method that can be called to check whether current token
     * (one that was just read) has an associated type id, and if
     * so, return it.
     * Note that while typically caller should check with {@link #canReadTypeId}
     * first, it is not illegal to call this method even if that method returns
     * true; but if so, it will return null. This may be used to simplify calling
     * code.
     *<p>
     * Default implementation will simply return null.
     *
     * @return Native Type Id associated with the current token, if any; {@code null} if none
     *
     * @throws IOException for low-level read issues, or
     *   {@link JsonParseException} for decoding problems
     *
     * @since 2.3
     */
    public Object getTypeId() throws IOException { return null; }

    /*
    /**********************************************************
    /* Public API, optional data binding functionality
    /**********************************************************
     */

    /**
     * Method to deserialize JSON content into a non-container
     * type (it can be an array type, however): typically a bean, array
     * or a wrapper type (like {@link java.lang.Boolean}).
     * <b>Note</b>: method can only be called if the parser has
     * an object codec assigned; this is true for parsers constructed
     * by <code>MappingJsonFactory</code> (from "jackson-databind" jar)
     * but not for {@link JsonFactory} (unless its <code>setCodec</code>
     * method has been explicitly called).
     *<p>
     * This method may advance the event stream, for structured types
     * the current token will be the closing end marker (END_ARRAY,
     * END_OBJECT) of the bound structure. For non-structured Json types
     * (and for {@link JsonToken#VALUE_EMBEDDED_OBJECT})
     * stream is not advanced.
     *<p>
     * Note: this method should NOT be used if the result type is a
     * container ({@link java.util.Collection} or {@link java.util.Map}.
     * The reason is that due to type erasure, key and value types
     * can not be introspected when using this method.
     *
     * @param <T> Nominal type parameter for value type
     *
     * @param valueType Java type to read content as (passed to ObjectCodec that
     *    deserializes content)
     *
     * @return Java value read from content
     *
     * @throws IOException if there is either an underlying I/O problem or decoding
     *    issue at format layer
     */
    public <T> T readValueAs(Class<T> valueType) throws IOException {
        return _codec().readValue(this, valueType);
    }

    /**
     * Method to deserialize JSON content into a Java type, reference
     * to which is passed as argument. Type is passed using so-called
     * "super type token"
     * and specifically needs to be used if the root type is a
     * parameterized (generic) container type.
     * <b>Note</b>: method can only be called if the parser has
     * an object codec assigned; this is true for parsers constructed
     * by <code>MappingJsonFactory</code> (defined in 'jackson-databind' bundle)
     * but not for {@link JsonFactory} (unless its <code>setCodec</code>
     * method has been explicitly called).
     *<p>
     * This method may advance the event stream, for structured types
     * the current token will be the closing end marker (END_ARRAY,
     * END_OBJECT) of the bound structure. For non-structured Json types
     * (and for {@link JsonToken#VALUE_EMBEDDED_OBJECT})
     * stream is not advanced.
     *
     * @param <T> Nominal type parameter for value type
     *
     * @param valueTypeRef Java type to read content as (passed to ObjectCodec that
     *    deserializes content)
     *
     * @return Java value read from content
     *
     * @throws IOException if there is either an underlying I/O problem or decoding
     *    issue at format layer
     */
    @SuppressWarnings("unchecked")
    public <T> T readValueAs(TypeReference<?> valueTypeRef) throws IOException {
        return (T) _codec().readValue(this, valueTypeRef);
    }

    /**
     * Method for reading sequence of Objects from parser stream,
     * all with same specified value type.
     *
     * @param <T> Nominal type parameter for value type
     *
     * @param valueType Java type to read content as (passed to ObjectCodec that
     *    deserializes content)
     *
     * @return Iterator for reading multiple Java values from content
     *
     * @throws IOException if there is either an underlying I/O problem or decoding
     *    issue at format layer
     */
    public <T> Iterator<T> readValuesAs(Class<T> valueType) throws IOException {
        return _codec().readValues(this, valueType);
    }

    /**
     * Method for reading sequence of Objects from parser stream,
     * all with same specified value type.
     *
     * @param <T> Nominal type parameter for value type
     *
     * @param valueTypeRef Java type to read content as (passed to ObjectCodec that
     *    deserializes content)
     *
     * @return Iterator for reading multiple Java values from content
     *
     * @throws IOException if there is either an underlying I/O problem or decoding
     *    issue at format layer
     */
    public <T> Iterator<T> readValuesAs(TypeReference<T> valueTypeRef) throws IOException {
        return _codec().readValues(this, valueTypeRef);
    }

    /**
     * Method to deserialize JSON content into equivalent "tree model",
     * represented by root {@link TreeNode} of resulting model.
     * For JSON Arrays it will an array node (with child nodes),
     * for objects object node (with child nodes), and for other types
     * matching leaf node type. Empty or whitespace documents are null.
     *
     * @param <T> Nominal type parameter for result node type (to reduce need for casting)
     *
     * @return root of the document, or null if empty or whitespace.
     *
     * @throws IOException if there is either an underlying I/O problem or decoding
     *    issue at format layer
     */
    @SuppressWarnings("unchecked")
    public <T extends TreeNode> T readValueAsTree() throws IOException {
        return (T) _codec().readTree(this);
    }

    protected ObjectCodec _codec() {
        ObjectCodec c = getCodec();
        if (c == null) {
            throw new IllegalStateException("No ObjectCodec defined for parser, needed for deserialization");
        }
        return c;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Helper method for constructing {@link JsonParseException}s
     * based on current state of the parser
     *
     * @param msg Base exception message to construct exception with
     *
     * @return {@link JsonParseException} constructed
     */
    protected JsonParseException _constructError(String msg) {
        return new JsonParseException(this, msg)
            .withRequestPayload(_requestPayload);
    }

    /**
     * Helper method to call for operations that are not supported by
     * parser implementation.
     *
     * @since 2.1
     */
    protected void _reportUnsupportedOperation() {
        throw new UnsupportedOperationException("Operation not supported by parser of type "+getClass().getName());
    }

    /**
     * Helper method for constructing {@link JsonParseException}
     * based on current state of the parser.
     *
     * @param msg Base exception message to construct exception with
     *
     * @return Read exception (of type {@link JsonParseException}) constructed
     *
     * @since 2.13
     */
    protected JsonParseException _constructReadException(String msg) {
        // In 3.0 will be actual `StreamReadException`...
        return _constructError(msg);
    }

    protected JsonParseException _constructReadException(String msg, Object arg) {
        return _constructReadException(String.format(msg, arg));
    }

    protected JsonParseException _constructReadException(String msg, Object arg1, Object arg2) {
        return _constructReadException(String.format(msg, arg1, arg2));
    }

    protected JsonParseException _constructReadException(String msg,
            Object arg1, Object arg2, Object arg3) {
        return _constructReadException(String.format(msg, arg1, arg2, arg3));
    }

    /**
     * Helper method for constructing {@link JsonParseException}
     * based on current state of the parser and indicating that the given
     * {@link Throwable} is the root cause.
     *
     * @param msg Base exception message to construct exception with
     * @param t Root cause to assign
     *
     * @return Read exception (of type {@link JsonParseException}) constructed
     *
     * @since 2.13
     */
    protected JsonParseException _constructReadException(String msg, Throwable t) {
        JsonParseException e = new JsonParseException(this, msg, t);
        if (_requestPayload != null) {
            e = e.withRequestPayload(_requestPayload);
        }
        return e;
    }
}
