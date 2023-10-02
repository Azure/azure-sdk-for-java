// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.typespec.json.implementation.jackson.core;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.typespec.json.implementation.jackson.core.JsonParser.NumberType;
import com.typespec.json.implementation.jackson.core.io.CharacterEscapes;
import com.typespec.json.implementation.jackson.core.type.WritableTypeId;
import com.typespec.json.implementation.jackson.core.type.WritableTypeId.Inclusion;
import com.typespec.json.implementation.jackson.core.util.JacksonFeatureSet;
import com.typespec.json.implementation.jackson.core.util.VersionUtil;

import static com.typespec.json.implementation.jackson.core.JsonTokenId.*;

/**
 * Base class that defines public API for writing JSON content.
 * Instances are created using factory methods of
 * a {@link JsonFactory} instance.
 *
 * @author Tatu Saloranta
 */
public abstract class JsonGenerator
    implements Closeable, Flushable, Versioned
{
    /**
     * Default set of {@link StreamWriteCapability}ies that may be used as
     * basis for format-specific readers (or as bogus instance if non-null
     * set needs to be passed).
     *
     * @since 2.12
     */
    protected final static JacksonFeatureSet<StreamWriteCapability> DEFAULT_WRITE_CAPABILITIES
        = JacksonFeatureSet.fromDefaults(StreamWriteCapability.values());

    /**
     * Default set of {@link StreamWriteCapability}ies for typical textual formats,
     * to use either as-is, or as a base with possible differences.
     *
     * @since 2.12
     */
    protected final static JacksonFeatureSet<StreamWriteCapability> DEFAULT_TEXTUAL_WRITE_CAPABILITIES
        = DEFAULT_WRITE_CAPABILITIES.with(StreamWriteCapability.CAN_WRITE_FORMATTED_NUMBERS);

    /**
     * Default set of {@link StreamWriteCapability}ies for typical binary formats,
     * to use either as-is, or as a base with possible differences.
     *
     * @since 2.12
     */
    protected final static JacksonFeatureSet<StreamWriteCapability> DEFAULT_BINARY_WRITE_CAPABILITIES
        = DEFAULT_WRITE_CAPABILITIES.with(StreamWriteCapability.CAN_WRITE_BINARY_NATIVELY);

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
         * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonWriteFeature#QUOTE_FIELD_NAMES} instead
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
         * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonWriteFeature#WRITE_NAN_AS_STRINGS} instead
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
         * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonWriteFeature#ESCAPE_NON_ASCII} instead
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
         * @deprecated Since 2.10 use {@link com.typespec.json.implementation.jackson.core.json.JsonWriteFeature#WRITE_NUMBERS_AS_STRINGS} instead
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
         * Feature that determines whether {@link JsonGenerator} will explicitly
         * check that no duplicate JSON Object field names are written.
         * If enabled, generator will check all names within context and report
         * duplicates by throwing a {@link JsonGenerationException}; if disabled,
         * no such checking will be done. Assumption in latter case is
         * that caller takes care of not trying to write duplicate names.
         *<p>
         * Note that enabling this feature will incur performance overhead
         * due to having to store and check additional information.
         *<p>
         * Feature is disabled by default.
         * 
         * @since 2.3
         */
        STRICT_DUPLICATE_DETECTION(false),
        
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
        ;

        private final boolean _defaultState;
        private final int _mask;
        
        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         *
         * @return Bit field of the features that are enabled by default
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
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }

        public boolean enabledByDefault() { return _defaultState; }

        // @since 2.3
        public boolean enabledIn(int flags) { return (flags & _mask) != 0; }

        public int getMask() { return _mask; }
    }

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Object that handles pretty-printing (usually additional
     * white space to make results more human-readable) during
     * output. If null, no pretty-printing is done.
     */
    protected PrettyPrinter _cfgPrettyPrinter;

    /*
    /**********************************************************************
    /* Construction, initialization
    /**********************************************************************
     */
    
    protected JsonGenerator() { }

    /**
     * Method that can be called to set or reset the object to
     * use for writing Java objects as JsonContent
     * (using method {@link #writeObject}).
     *
     * @param oc Codec to assign, if any; {@code null} if none
     *
     * @return This generator, to allow call chaining
     */
    public abstract JsonGenerator setCodec(ObjectCodec oc);

    /**
     * Method for accessing the object used for writing Java
     * object as JSON content
     * (using method {@link #writeObject}).
     *
     * @return Codec assigned to this generator, if any; {@code null} if none
     */
    public abstract ObjectCodec getCodec();

    /**
     * Accessor for finding out version of the bundle that provided this generator instance.
     *
     * @return Version of this generator (derived from version declared for
     *   {@code jackson-core} jar that contains the class
     */
    @Override
    public abstract Version version();

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
     * Method that can be used to get access to object that is used
     * as target for generated output; this is usually either
     * {@link OutputStream} or {@link Writer}, depending on what
     * generator was constructed with.
     * Note that returned value may be null in some cases; including
     * case where implementation does not want to exposed raw
     * source to caller.
     * In cases where output has been decorated, object returned here
     * is the decorated version; this allows some level of interaction
     * between users of generator and decorator object.
     *<p>
     * In general use of this accessor should be considered as
     * "last effort", i.e. only used if no other mechanism is applicable.
     *
     * @return Output target this generator was configured with
     */
    public Object getOutputTarget() {
        return null;
    }

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
        // TODO: implement directly in 2.14 or later, make getCurrentValue() call this
        return getCurrentValue();
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
        JsonStreamContext ctxt = getOutputContext();
        return (ctxt == null) ? null : ctxt.getCurrentValue();
    }

    // TODO: deprecate in 2.14 or later
    /**
     * Alias for {@link #assignCurrentValue}, to be deprecated in later
     * Jackson 2.x versions (and removed from Jackson 3.0).
     *
     * @param v Current value to assign for the current context of this generator
     */
    public void setCurrentValue(Object v) {
        JsonStreamContext ctxt = getOutputContext();
        if (ctxt != null) {
            ctxt.setCurrentValue(v);
        }
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
        if (state) enable(f); else disable(f);
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
     * Bulk access method for getting state of all standard (non-dataformat-specific)
     * {@link JsonGenerator.Feature}s.
     * 
     * @return Bit mask that defines current states of all standard {@link JsonGenerator.Feature}s.
     *
     * @since 2.3
     */
    public abstract int getFeatureMask();

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
     * @deprecated Since 2.7, use {@link #overrideStdFeatures(int, int)} instead -- remove from 2.9
     */
    @Deprecated
    public abstract JsonGenerator setFeatureMask(int values);

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
     * @return This generator, to allow call chaining
     * 
     * @since 2.6
     */
    public JsonGenerator overrideStdFeatures(int values, int mask) {
        int oldState = getFeatureMask();
        int newState = (oldState & ~mask) | (values & mask);
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
     * the generator implementation does not support any {@link FormatFeature}s.
     * 
     * @param values Bit mask of set/clear state for features to change
     * @param mask Bit mask of features to change
     *
     * @return This generator, to allow call chaining
     * 
     * @since 2.6
     */
    public JsonGenerator overrideFormatFeatures(int values, int mask) {
        // 08-Oct-2018, tatu: For 2.10 we actually do get `JsonWriteFeature`s, although they
        //    are (for 2.x only, not for 3.x) mapper to legacy settings. So do not throw exception:
//        throw new IllegalArgumentException("No FormatFeatures defined for generator of type "+getClass().getName());
        return this;
    }

    /*
    /**********************************************************************
    /* Public API, Schema configuration
    /**********************************************************************
     */

    /**
     * Method to call to make this generator use specified schema.
     * Method must be called before generating any content, right after instance
     * has been created.
     * Note that not all generators support schemas; and those that do usually only
     * accept specific types of schemas: ones defined for data format this generator
     * produces.
     *<p>
     * If generator does not support specified schema, {@link UnsupportedOperationException}
     * is thrown.
     * 
     * @param schema Schema to use
     * 
     * @throws UnsupportedOperationException if generator does not support schema
     */
    public void setSchema(FormatSchema schema) {
        throw new UnsupportedOperationException(String.format(
                "Generator of type %s does not support schema of type '%s'",
                getClass().getName(), schema.getSchemaType()));
    }

    /**
     * Method for accessing Schema that this generator uses, if any; {@code null} if none.
     * Default implementation returns null.
     *
     * @return Schema in use by this generator, if any; {@code null} if none
     */
    public FormatSchema getSchema() { return null; }

    /*
    /**********************************************************************
    /* Public API, other configuration
    /**********************************************************************
      */

    /**
     * Method for setting a custom pretty printer, which is usually
     * used to add indentation for improved human readability.
     * By default, generator does not do pretty printing.
     *<p>
     * To use the default pretty printer that comes with core
     * Jackson distribution, call {@link #useDefaultPrettyPrinter}
     * instead.
     *
     * @param pp {@code PrettyPrinter} to assign, if any; {@code null} if none
     *
     * @return This generator, to allow call chaining
     */
    public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
        _cfgPrettyPrinter = pp;
        return this;
    }

    /**
     * Accessor for checking whether this generator has a configured
     * {@link PrettyPrinter}; returns it if so, null if none configured.
     *
     * @return {@link PrettyPrinter} configured for this generator, if any; {@code null} if none
     */
    public PrettyPrinter getPrettyPrinter() {
        return _cfgPrettyPrinter;
    }
    
    /**
     * Convenience method for enabling pretty-printing using
     * the default pretty printer
     * ({@link com.typespec.json.implementation.jackson.core.util.DefaultPrettyPrinter}).
     *
     * @return This generator, to allow call chaining
     */
    public abstract JsonGenerator useDefaultPrettyPrinter();

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
    public JsonGenerator setHighestNonEscapedChar(int charCode) { return this; }

    /**
     * Accessor method for testing what is the highest unescaped character
     * configured for this generator. This may be either positive value
     * (when escaping configuration has been set and is in effect), or
     * 0 to indicate that no additional escaping is in effect.
     * Some generators may not support additional escaping: for example,
     * generators for binary formats that do not use escaping should
     * simply return 0.
     * 
     * @return Currently active limitation for highest non-escaped character,
     *   if defined; or 0 to indicate no additional escaping is performed.
     */
    public int getHighestEscapedChar() { return 0; }

    /**
     * Method for accessing custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     *
     * @return {@link CharacterEscapes} configured for this generator, if any; {@code null} if none
     */
    public CharacterEscapes getCharacterEscapes() { return null; }

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
    public JsonGenerator setCharacterEscapes(CharacterEscapes esc) { return this; }

    /**
     * Method that allows overriding String used for separating root-level
     * JSON values (default is single space character)
     *<p>
     * Default implementation throws {@link UnsupportedOperationException}.
     * 
     * @param sep Separator to use, if any; null means that no separator is
     *   automatically added
     *
     * @return This generator, to allow call chaining
     */
    public JsonGenerator setRootValueSeparator(SerializableString sep) {
        throw new UnsupportedOperationException();
    }

    /*
    /**********************************************************************
    /* Public API, output state access
    /**********************************************************************
     */

    /**
     * Method for verifying amount of content that is buffered by generator
     * but not yet flushed to the underlying target (stream, writer),
     * in units (byte, char) that the generator implementation uses for buffering;
     * or -1 if this information is not available.
     * Unit used is often the same as the unit of underlying target (that is,
     * `byte` for {@link java.io.OutputStream}, `char` for {@link java.io.Writer}),
     * but may differ if buffering is done before encoding.
     * Default JSON-backed implementations do use matching units.
     *<p>
     * Note: non-JSON implementations will be retrofitted for 2.6 and beyond;
     * please report if you see -1 (missing override)
     *
     * @return Amount of content buffered in internal units, if amount known and
     *    accessible; -1 if not accessible.
     *
     * @since 2.6
     */
    public int getOutputBuffered() {
        return -1;
    }

    /*
    /**********************************************************************
    /* Public API, capability introspection methods
    /**********************************************************************
     */

    /**
     * Method that can be used to verify that given schema can be used with
     * this generator (using {@link #setSchema}).
     * 
     * @param schema Schema to check
     * 
     * @return True if this generator can use given schema; false if not
     */
    public boolean canUseSchema(FormatSchema schema) { return false; }
    
    /**
     * Introspection method that may be called to see if the underlying
     * data format supports some kind of Object Ids natively (many do not;
     * for example, JSON doesn't).
     * This method <b>must</b> be called prior to calling
     * {@link #writeObjectId} or {@link #writeObjectRef}.
     *<p>
     * Default implementation returns false; overridden by data formats
     * that do support native Object Ids. Caller is expected to either
     * use a non-native notation (explicit property or such), or fail,
     * in case it can not use native object ids.
     *
     * @return {@code True} if this generator is capable of writing "native" Object Ids
     *   (which is typically determined by capabilities of the underlying format),
     *   {@code false} if not
     *
     * @since 2.3
     */
    public boolean canWriteObjectId() { return false; }

    /**
     * Introspection method that may be called to see if the underlying
     * data format supports some kind of Type Ids natively (many do not;
     * for example, JSON doesn't).
     * This method <b>must</b> be called prior to calling
     * {@link #writeTypeId}.
     *<p>
     * Default implementation returns false; overridden by data formats
     * that do support native Type Ids. Caller is expected to either
     * use a non-native notation (explicit property or such), or fail,
     * in case it can not use native type ids.
     * 
     * @return {@code True} if this generator is capable of writing "native" Type Ids
     *   (which is typically determined by capabilities of the underlying format),
     *   {@code false} if not
     *
     * @since 2.3
     */
    public boolean canWriteTypeId() { return false; }

    /**
     * Introspection method that may be called to see if the underlying
     * data format supports "native" binary data; that is, an efficient
     * output of binary content without encoding.
     *<p>
     * Default implementation returns false; overridden by data formats
     * that do support native binary content.
     *
     * @return {@code True} if this generator is capable of writing "raw" Binary
     *   Content
     *   (this is typically determined by capabilities of the underlying format);
     *   {@code false} if not
     *
     * @since 2.3
     */
    public boolean canWriteBinaryNatively() { return false; }
    
    /**
     * Introspection method to call to check whether it is ok to omit
     * writing of Object fields or not. Most formats do allow omission,
     * but certain positional formats (such as CSV) require output of
     * placeholders, even if no real values are to be emitted.
     *
     * @return {@code True} if this generator is allowed to only write values
     *   of some Object fields and omit the rest; {@code false} if not
     *
     * @since 2.3
     */
    public boolean canOmitFields() { return true; }

    /**
     * Introspection method to call to check whether it is possible
     * to write numbers using {@link #writeNumber(java.lang.String)}
     * using possible custom format, or not. Typically textual formats
     * allow this (and JSON specifically does), whereas binary formats
     * do not allow this (except by writing them as Strings).
     * Usual reason for calling this method is to check whether custom
     * formatting of numbers may be applied by higher-level code (databinding)
     * or not.
     *
     * @return {@code True} if this generator is capable of writing "formatted"
     *   numbers (and if so, need to be passed using
     *   {@link #writeNumber(String)}, that is, passed as {@code String});
     *   {@code false} if not
     *
     * @since 2.8
     */
    public boolean canWriteFormattedNumbers() { return false; }

    /**
     * Accessor for getting metadata on capabilities of this generator, based on
     * underlying data format being read (directly or indirectly).
     *
     * @return Set of write capabilities for content written using this generator
     *
     * @since 2.12
     */
    public JacksonFeatureSet<StreamWriteCapability> getWriteCapabilities() {
        return DEFAULT_WRITE_CAPABILITIES;
    }

    /*
    /**********************************************************************
    /* Public API, write methods, structural
    /**********************************************************************
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
     * Method for writing start marker of an Array value, similar
     * to {@link #writeStartArray()},
     * but also specifying how many
     * elements will be written for the array before calling
     * {@link #writeEndArray()}.
     *<p>
     * Default implementation simply calls {@link #writeStartArray()}.
     * 
     * @param size Number of elements this array will have: actual
     *   number of values written (before matching call to
     *   {@link #writeEndArray()} MUST match; generator MAY verify
     *   this is the case (and SHOULD if format itself encodes length)
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *   
     * @since 2.4
     *
     * @deprecated Since 2.12 Use {@link #writeStartArray(Object, int)} instead
     */
    @Deprecated
    public void writeStartArray(int size) throws IOException {
        writeStartArray();
    }

    /**
     * Method for writing start marker of an Array value, similar
     * to {@link #writeStartArray()},
     * but also specifying the "current value"
     * to assign to the new Array context being created.
     *
     * @param forValue "Current value" to assign for the Array context being created
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.10
     */
    public void writeStartArray(Object forValue) throws IOException {
        writeStartArray();
        setCurrentValue(forValue);
    }

    /**
     * Method for writing start marker of an Array value, similar
     * to {@link #writeStartArray()}, but also specifying the "current value"
     * to assign to the new Array context being created
     * as well as how many elements will be written for the array before calling
     * {@link #writeEndArray()}.
     *
     * @param forValue "Current value" to assign for the Array context being created
     * @param size Number of elements this Array will have: actual
     *   number of values written (before matching call to
     *   {@link #writeEndArray()} MUST match; generator MAY verify
     *   this is the case (and SHOULD if format itself encodes length)
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.10
     */
    public void writeStartArray(Object forValue, int size) throws IOException {
        writeStartArray(size);
        setCurrentValue(forValue);
    }

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
     * Method for writing starting marker of an Object value
     * to represent the given Java Object value.
     * Argument is offered as metadata, but more
     * importantly it should be assigned as the "current value"
     * for the Object content that gets constructed and initialized.
     *<p>
     * Object values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     *
     * @param forValue "Current value" to assign for the Object context being created
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.8
     */
    public void writeStartObject(Object forValue) throws IOException
    {
        writeStartObject();
        setCurrentValue(forValue);
    }

    /**
     * Method for writing starting marker of an Object value
     * to represent the given Java Object value.
     * Argument is offered as metadata, but more
     * importantly it should be assigned as the "current value"
     * for the Object content that gets constructed and initialized.
     * In addition, caller knows number of key/value pairs ("properties")
     * that will get written for the Object value: this is relevant for
     * some format backends (but not, as an example, for JSON).
     *<p>
     * Object values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     *
     * @param forValue "Current value" to assign for the Object context being created
     * @param size Number of key/value pairs this Object will have: actual
     *   number of entries written (before matching call to
     *   {@link #writeEndObject()} MUST match; generator MAY verify
     *   this is the case (and SHOULD if format itself encodes length)
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.10
     */
    public void writeStartObject(Object forValue, int size) throws IOException
    {
        writeStartObject();
        setCurrentValue(forValue);
    }

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

    /**
     * Method similar to {@link #writeFieldName(String)}, main difference
     * being that it may perform better as some of processing (such as
     * quoting of certain characters, or encoding into external encoding
     * if supported by generator) can be done just once and reused for
     * later calls.
     *<p>
     * Default implementation simple uses unprocessed name container in
     * serialized String; implementations are strongly encouraged to make
     * use of more efficient methods argument object has.
     *
     * @param name Field name to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeFieldName(SerializableString name) throws IOException;

    /**
     * Alternative to {@link #writeFieldName(String)} that may be used
     * in cases where property key is of numeric type; either where
     * underlying format supports such notion (some binary formats do,
     * unlike JSON), or for convenient conversion into String presentation.
     * Default implementation will simply convert id into <code>String</code>
     * and call {@link #writeFieldName(String)}.
     *
     * @param id Field id to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.8
     */
    public void writeFieldId(long id) throws IOException {
        writeFieldName(Long.toString(id));
    }

    /*
    /**********************************************************************
    /* Public API, write methods, scalar arrays (2.8)
    /**********************************************************************
     */

    /**
     * Value write method that can be called to write a single
     * array (sequence of {@link JsonToken#START_ARRAY}, zero or
     * more {@link JsonToken#VALUE_NUMBER_INT}, {@link JsonToken#END_ARRAY})
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.8
     */
    public void writeArray(int[] array, int offset, int length) throws IOException
    {
        if (array == null) {
            throw new IllegalArgumentException("null array");
        }
        _verifyOffsets(array.length, offset, length);
        writeStartArray(array, length);
        for (int i = offset, end = offset+length; i < end; ++i) {
            writeNumber(array[i]);
        }
        writeEndArray();
    }

    /**
     * Value write method that can be called to write a single
     * array (sequence of {@link JsonToken#START_ARRAY}, zero or
     * more {@link JsonToken#VALUE_NUMBER_INT}, {@link JsonToken#END_ARRAY})
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.8
     */
    public void writeArray(long[] array, int offset, int length) throws IOException
    {
        if (array == null) {
            throw new IllegalArgumentException("null array");
        }
        _verifyOffsets(array.length, offset, length);
        writeStartArray(array, length);
        for (int i = offset, end = offset+length; i < end; ++i) {
            writeNumber(array[i]);
        }
        writeEndArray();
    }

    /**
     * Value write method that can be called to write a single
     * array (sequence of {@link JsonToken#START_ARRAY}, zero or
     * more {@link JsonToken#VALUE_NUMBER_FLOAT}, {@link JsonToken#END_ARRAY})
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.8
     */
    public void writeArray(double[] array, int offset, int length) throws IOException
    {
        if (array == null) {
            throw new IllegalArgumentException("null array");
        }
        _verifyOffsets(array.length, offset, length);
        writeStartArray(array, length);
        for (int i = offset, end = offset+length; i < end; ++i) {
            writeNumber(array[i]);
        }
        writeEndArray();
    }

    /**
     * Value write method that can be called to write a single
     * array (sequence of {@link JsonToken#START_ARRAY}, zero or
     * more {@link JsonToken#VALUE_STRING}, {@link JsonToken#END_ARRAY})
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.11
     */
    public void writeArray(String[] array, int offset, int length) throws IOException
    {
        if (array == null) {
            throw new IllegalArgumentException("null array");
        }
        _verifyOffsets(array.length, offset, length);
        writeStartArray(array, length);
        for (int i = offset, end = offset+length; i < end; ++i) {
            writeString(array[i]);
        }
        writeEndArray();
    }

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

    /**
     * Method for outputting a String value. Depending on context
     * this means either array element, (object) field value or
     * a stand alone String; but in all cases, String will be
     * surrounded in double quotes, and contents will be properly
     * escaped as required by JSON specification.
     * If {@code len} is &lt; 0, then write all contents of the reader.
     * Otherwise, write only len characters.
     *<p>
     * Note: actual length of content available may exceed {@code len} but
     * can not be less than it: if not enough content available, a
     * {@link JsonGenerationException} will be thrown.
     *
     * @param reader Reader to use for reading Text value to write
     * @param len Maximum Length of Text value to read (in {@code char}s, non-negative)
     *    if known; {@code -1} to indicate "read and write it all"
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer; or if length ({@code len}) is specified but
     *    {@code reader} does not provide enough content
     *
     * @since 2.9
     */
    public void writeString(Reader reader, int len) throws IOException {
        // Implemented as "unsupported" for backwards compatibility
        _reportUnsupportedOperation();
    }

    /**
     * Method for outputting a String value. Depending on context
     * this means either array element, (object) field value or
     * a stand alone String; but in all cases, String will be
     * surrounded in double quotes, and contents will be properly
     * escaped as required by JSON specification.
     *
     * @param buffer Buffer that contains String value to write
     * @param offset Offset in {@code buffer} of the first character of String value to write
     * @param len Length of the String value (in characters) to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeString(char[] buffer, int offset, int len) throws IOException;

    /**
     * Method similar to {@link #writeString(String)}, but that takes
     * {@link SerializableString} which can make this potentially
     * more efficient to call as generator may be able to reuse
     * quoted and/or encoded representation.
     *<p>
     * Default implementation just calls {@link #writeString(String)};
     * sub-classes should override it with more efficient implementation
     * if possible.
     *
     * @param text Pre-encoded String value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeString(SerializableString text) throws IOException;

    /**
     * Method similar to {@link #writeString(String)} but that takes as
     * its input a UTF-8 encoded String that is to be output as-is, without additional
     * escaping (type of which depends on data format; backslashes for JSON).
     * However, quoting that data format requires (like double-quotes for JSON) will be added
     * around the value if and as necessary.
     *<p>
     * Note that some backends may choose not to support this method: for
     * example, if underlying destination is a {@link java.io.Writer}
     * using this method would require UTF-8 decoding.
     * If so, implementation may instead choose to throw a
     * {@link UnsupportedOperationException} due to ineffectiveness
     * of having to decode input.
     *
     * @param buffer Buffer that contains String value to write
     * @param offset Offset in {@code buffer} of the first byte of String value to write
     * @param len Length of the String value (in characters) to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeRawUTF8String(byte[] buffer, int offset, int len)
        throws IOException;

    /**
     * Method similar to {@link #writeString(String)} but that takes as its input
     * a UTF-8 encoded String which has <b>not</b> been escaped using whatever
     * escaping scheme data format requires (for JSON that is backslash-escaping
     * for control characters and double-quotes; for other formats something else).
     * This means that textual JSON backends need to check if value needs
     * JSON escaping, but otherwise can just be copied as is to output.
     * Also, quoting that data format requires (like double-quotes for JSON) will be added
     * around the value if and as necessary.
     *<p>
     * Note that some backends may choose not to support this method: for
     * example, if underlying destination is a {@link java.io.Writer}
     * using this method would require UTF-8 decoding.
     * In this case
     * generator implementation may instead choose to throw a
     * {@link UnsupportedOperationException} due to ineffectiveness
     * of having to decode input.
     *
     * @param buffer Buffer that contains String value to write
     * @param offset Offset in {@code buffer} of the first byte of String value to write
     * @param len Length of the String value (in characters) to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeUTF8String(byte[] buffer, int offset, int len)
        throws IOException;

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
     * @param c Character to included in output
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeRaw(char c) throws IOException;

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
     *<p>
     * The default implementation delegates to {@link #writeRaw(String)};
     * other backends that support raw inclusion of text are encouraged
     * to implement it in more efficient manner (especially if they
     * use UTF-8 encoding).
     *
     * @param raw Pre-encoded textual contents to included in output
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
//    public abstract void writeRaw(SerializableString raw) throws IOException;
    public void writeRaw(SerializableString raw) throws IOException {
        writeRaw(raw.getValue());
    }

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

    public abstract void writeRawValue(String text, int offset, int len) throws IOException;

    public abstract void writeRawValue(char[] text, int offset, int len) throws IOException;

    /**
     * Method similar to {@link #writeRawValue(String)}, but potentially more
     * efficient as it may be able to use pre-encoded content (similar to
     * {@link #writeRaw(SerializableString)}.
     *
     * @param raw Pre-encoded textual contents to included in output
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * 
     * @since 2.5
     */
    public void writeRawValue(SerializableString raw) throws IOException {
        writeRawValue(raw.getValue());
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
    public abstract void writeBinary(Base64Variant bv,
            byte[] data, int offset, int len) throws IOException;

    /**
     * Similar to {@link #writeBinary(Base64Variant,byte[],int,int)},
     * but default to using the Jackson default Base64 variant 
     * (which is {@link Base64Variants#MIME_NO_LINEFEEDS}).
     *
     * @param data Buffer that contains binary data to write
     * @param offset Offset in {@code data} of the first byte of data to write
     * @param len Length of data to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeBinary(byte[] data, int offset, int len) throws IOException {
        writeBinary(Base64Variants.getDefaultVariant(), data, offset, len);
    }

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

    /**
     * Similar to {@link #writeBinary(Base64Variant,InputStream,int)},
     * but assumes default to using the Jackson default Base64 variant 
     * (which is {@link Base64Variants#MIME_NO_LINEFEEDS}).
     * 
     * @param data InputStream to use for reading binary data to write.
     *    Will not be closed after successful write operation
     * @param dataLength (optional) number of bytes that will be available;
     *    or -1 to be indicate it is not known. Note that implementations
     *    need not support cases where length is not known in advance; this
     *    depends on underlying data format: JSON output does NOT require length,
     *    other formats may
     *
     * @return Number of bytes actually written
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public int writeBinary(InputStream data, int dataLength)
        throws IOException {
        return writeBinary(Base64Variants.getDefaultVariant(), data, dataLength);
    }
    
    /**
     * Method similar to {@link #writeBinary(Base64Variant,byte[],int,int)},
     * but where input is provided through a stream, allowing for incremental
     * writes without holding the whole input in memory.
     * 
     * @param bv Base64 variant to use
     * @param data InputStream to use for reading binary data to write.
     *    Will not be closed after successful write operation
     * @param dataLength (optional) number of bytes that will be available;
     *    or -1 to be indicate it is not known.
     *    If a positive length is given, <code>data</code> MUST provide at least
     *    that many bytes: if not, an exception will be thrown.
     *    Note that implementations
     *    need not support cases where length is not known in advance; this
     *    depends on underlying data format: JSON output does NOT require length,
     *    other formats may.
     * 
     * @return Number of bytes read from <code>data</code> and written as binary payload
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract int writeBinary(Base64Variant bv,
            InputStream data, int dataLength) throws IOException;

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
     *
     * @since 2.2
     */
    public void writeNumber(short v) throws IOException { writeNumber((int) v); }

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
    public abstract void writeNumber(BigInteger v) throws IOException;

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
    public abstract void writeNumber(BigDecimal v) throws IOException;

    /**
     * Write method that can be used for custom numeric types that can
     * not be (easily?) converted to "standard" Java number types.
     * Because numbers are not surrounded by double quotes, regular
     * {@link #writeString} method can not be used; nor
     * {@link #writeRaw} because that does not properly handle
     * value separators needed in Array or Object contexts.
     *<p>
     * Note: because of lack of type safety, some generator
     * implementations may not be able to implement this
     * method. For example, if a binary JSON format is used,
     * it may require type information for encoding; similarly
     * for generator-wrappers around Java objects or JSON nodes.
     * If implementation does not implement this method,
     * it needs to throw {@link UnsupportedOperationException}.
     *
     * @param encodedValue Textual (possibly format) number representation to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * @throws UnsupportedOperationException If underlying data format does not
     *   support numbers serialized textually AND if generator is not allowed
     *   to just output a String instead (Schema-based formats may require actual
     *   number, for example)
     */
    public abstract void writeNumber(String encodedValue) throws IOException;

    /**
     * Overloaded version of {@link #writeNumber(String)} with same semantics
     * but possibly more efficient operation.
     *
     * @param encodedValueBuffer Buffer that contains the textual number representation to write
     * @param offset Offset of the first character of value to write
     * @param len Length of the value (in characters) to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.11
     */
    public void writeNumber(char[] encodedValueBuffer, int offset, int len) throws IOException {
        writeNumber(new String(encodedValueBuffer, offset, len));
    }

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
     * Method that can be called on backends that support passing opaque native
     * values that some data formats support; not used with JSON backend,
     * more common with binary formats.
     *<p>
     * NOTE: this is NOT the method to call for serializing regular POJOs,
     * see {@link #writeObject} instead.
     *
     * @param object Native format-specific value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.8
     */
    public void writeEmbeddedObject(Object object) throws IOException {
        // 01-Sep-2016, tatu: As per [core#318], handle small number of cases
        if (object == null) {
            writeNull();
            return;
        }
        if (object instanceof byte[]) {
            writeBinary((byte[]) object);
            return;
        }
        throw new JsonGenerationException("No native support for writing embedded objects of type "
                +object.getClass().getName(),
                this);
    }

    /*
    /**********************************************************************
    /* Public API, write methods, Native Ids (type, object)
    /**********************************************************************
     */

    /**
     * Method that can be called to output so-called native Object Id.
     * Note that it may only be called after ensuring this is legal
     * (with {@link #canWriteObjectId()}), as not all data formats
     * have native type id support; and some may only allow them in
     * certain positions or locations.
     * If output is not allowed by the data format in this position,
     * a {@link JsonGenerationException} will be thrown.
     *
     * @param id Native Object Id to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * @throws JsonGenerationException if Object ID output is not allowed
     *   (either at all, or specifically in this position in output)
     *
     * @since 2.3
     */
    public void writeObjectId(Object id) throws IOException {
        throw new JsonGenerationException("No native support for writing Object Ids", this);
    }

    /**
     * Method that can be called to output references to native Object Ids.
     * Note that it may only be called after ensuring this is legal
     * (with {@link #canWriteObjectId()}), as not all data formats
     * have native type id support; and some may only allow them in
     * certain positions or locations.
     * If output is not allowed by the data format in this position,
     * a {@link JsonGenerationException} will be thrown.
     *
     * @param referenced Referenced value, for which Object Id is expected to be written
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * @throws JsonGenerationException if Object ID output is not allowed
     *   (either at all, or specifically in this position in output)
     */
    public void writeObjectRef(Object referenced) throws IOException {
        throw new JsonGenerationException("No native support for writing Object Ids", this);
    }
    
    /**
     * Method that can be called to output so-called native Type Id.
     * Note that it may only be called after ensuring this is legal
     * (with {@link #canWriteTypeId()}), as not all data formats
     * have native type id support; and some may only allow them in
     * certain positions or locations.
     * If output is not allowed by the data format in this position,
     * a {@link JsonGenerationException} will be thrown.
     *
     * @param id Native Type Id to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * @throws JsonGenerationException if Type ID output is not allowed
     *   (either at all, or specifically in this position in output)
     *
     * @since 2.3
     */
    public void writeTypeId(Object id) throws IOException {
        throw new JsonGenerationException("No native support for writing Type Ids", this);
    }

    /**
     * Replacement method for {@link #writeTypeId(Object)} which is called
     * regardless of whether format has native type ids. If it does have native
     * type ids, those are to be used (if configuration allows this), if not,
     * structural type id inclusion is to be used. For JSON, for example, no
     * native type ids exist and structural inclusion is always used.
     *<p>
     * NOTE: databind may choose to skip calling this method for some special cases
     * (and instead included type id via regular write methods and/or {@link #writeTypeId}
     * -- this is discouraged, but not illegal, and may be necessary as a work-around
     * in some cases.
     *
     * @param typeIdDef Full Type Id definition
     *
     * @return {@link WritableTypeId} for caller to retain and pass to matching
     *   {@link #writeTypeSuffix} call
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * @throws JsonGenerationException if Type ID output is not allowed
     *   (either at all, or specifically in this position in output)
     *
     * @since 2.9
     */
    public WritableTypeId writeTypePrefix(WritableTypeId typeIdDef) throws IOException
    {
        Object id = typeIdDef.id;

        final JsonToken valueShape = typeIdDef.valueShape;
        if (canWriteTypeId()) {
            typeIdDef.wrapperWritten = false;
            // just rely on native type output method (sub-classes likely to override)
            writeTypeId(id);
        } else {
            // No native type id; write wrappers
            // Normally we only support String type ids (non-String reserved for native type ids)
            String idStr = (id instanceof String) ? (String) id : String.valueOf(id);
            typeIdDef.wrapperWritten = true;

            Inclusion incl = typeIdDef.include;
            // first: can not output "as property" if value not Object; if so, must do "as array"
            if ((valueShape != JsonToken.START_OBJECT)
                    && incl.requiresObjectContext()) {
                typeIdDef.include = incl = WritableTypeId.Inclusion.WRAPPER_ARRAY;
            }
            
            switch (incl) {
            case PARENT_PROPERTY:
                // nothing to do here, as it has to be written in suffix...
                break;
            case PAYLOAD_PROPERTY:
                // only output as native type id; otherwise caller must handle using some
                // other mechanism, so...
                break;
            case METADATA_PROPERTY:
                // must have Object context by now, so simply write as field name
                // Note, too, that it's bit tricky, since we must print START_OBJECT that is part
                // of value first -- and then NOT output it later on: hence return "early"
                writeStartObject(typeIdDef.forValue);
                writeStringField(typeIdDef.asProperty, idStr);
                return typeIdDef;

            case WRAPPER_OBJECT:
                // NOTE: this is wrapper, not directly related to value to output, so don't pass
                writeStartObject();
                writeFieldName(idStr);
                break;
            case WRAPPER_ARRAY:
            default: // should never occur but translate as "as-array"
                writeStartArray(); // wrapper, not actual array object to write
                writeString(idStr);
            }
        }
        // and finally possible start marker for value itself:
        if (valueShape == JsonToken.START_OBJECT) {
            writeStartObject(typeIdDef.forValue);
        } else if (valueShape == JsonToken.START_ARRAY) {
            // should we now set the current object?
            writeStartArray();
        }
        return typeIdDef;
    }

    /**
     * Method to call along with {@link #writeTypePrefix}, but after actual value
     * that has type id has been completely written. This allows post-processing
     * for some cases (for example if the actual Type Id is written at the END of
     * the value, not before or at the beginning).
     *
     * @param typeIdDef Value returned by the earlier matching call to {@link #writeTypePrefix(WritableTypeId)}
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * @throws JsonGenerationException if Type ID output is not allowed
     *   (either at all, or specifically in this position in output)
     *
     * @return Argument {@code typeIdDef}, possibly modified
     *
     * @since 2.9
     */
    public WritableTypeId writeTypeSuffix(WritableTypeId typeIdDef) throws IOException
    {
        final JsonToken valueShape = typeIdDef.valueShape;
        // First: does value need closing?
        if (valueShape == JsonToken.START_OBJECT) {
            writeEndObject();
        } else if (valueShape == JsonToken.START_ARRAY) {
            writeEndArray();
        }

        if (typeIdDef.wrapperWritten) {
            switch (typeIdDef.include) {
            case WRAPPER_ARRAY:
                writeEndArray();
                break;
            case PARENT_PROPERTY:
                // unusually, need to output AFTER value. And no real wrapper...
                {
                    Object id = typeIdDef.id;
                    String idStr = (id instanceof String) ? (String) id : String.valueOf(id);
                    writeStringField(typeIdDef.asProperty, idStr);
                }
                break;
            case METADATA_PROPERTY:
            case PAYLOAD_PROPERTY:
                // no actual wrapper; included within Object itself
                break;
            case WRAPPER_OBJECT:
            default: // should never occur but...
                writeEndObject();
                break;
            }
        }
        return typeIdDef;
    }

    /*
    /**********************************************************************
    /* Public API, write methods, serializing Java objects
    /**********************************************************************
     */

    /**
     * Method for writing given Java object (POJO) as JSON.
     * Exactly how the object gets written depends on object
     * in question (and on codec, its configuration); for
     * typical POJOs it will result in JSON Object, but for others JSON
     * Array, or String or numeric value (and for nulls, JSON
     * null literal).
     * <b>NOTE</b>: generator must have its {@code ObjectCodec}
     * set to non-null value; for generators created by a mapping
     * factory this is the case, for others not.
     *
     * @param pojo Java value (usually POJO) to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.13 (to eventually replace {@link #writeObject(Object)}
     */
    public void writePOJO(Object pojo) throws IOException {
        writeObject(pojo);
    }

    // TODO: deprecate in 2.14 or later
    /**
     * Older alias for {@link #writePOJO(Object)}
     *
     * @param pojo Java value (usually POJO) to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeObject(Object pojo) throws IOException;

    /**
     * Method for writing given JSON tree (expressed as a tree
     * where given JsonNode is the root) using this generator.
     * This will generally just call
     * {@link #writeObject} with given node, but is added
     * for convenience and to make code more explicit in cases
     * where it deals specifically with trees.
     *
     * @param rootNode {@link TreeNode} to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public abstract void writeTree(TreeNode rootNode) throws IOException;

    /*
    /**********************************************************************
    /* Public API, convenience field write methods
    /**********************************************************************
     */

    // 04-Oct-2019, tatu: Reminder: these could be defined final to
    //    remember NOT to override in delegating sub-classes -- but
    //    not final in 2.x to reduce compatibility issues

    /**
     * Convenience method for outputting a field entry ("member")
     * that contains specified data in base64-encoded form.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeBinary(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param data Binary data for the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        writeFieldName(fieldName);
        writeBinary(data);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has a boolean value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeBoolean(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Boolean value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        writeFieldName(fieldName);
        writeBoolean(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has JSON literal value null. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNull();
     *</pre>
     *
     * @param fieldName Name of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeNullField(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeNull();
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has a String value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeString(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value String value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeStringField(String fieldName, String value) throws IOException {
        writeFieldName(fieldName);
        writeString(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Numeric value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.11
     */
    public void writeNumberField(String fieldName, short value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Numeric value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeNumberField(String fieldName, int value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Numeric value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeNumberField(String fieldName, long value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Numeric value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     *
     * @since 2.11
     */
    public void writeNumberField(String fieldName, BigInteger value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Numeric value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeNumberField(String fieldName, float value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value. Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Numeric value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeNumberField(String fieldName, double value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has the specified numeric value.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeNumber(value);
     *</pre>
     *
     * @param fieldName Name of the field to write
     * @param value Numeric value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeNumberField(String fieldName, BigDecimal value) throws IOException {
        writeFieldName(fieldName);
        writeNumber(value);
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * (that will contain a JSON Array value), and the START_ARRAY marker.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeStartArray();
     *</pre>
     *<p>
     * Note: caller still has to take care to close the array
     * (by calling {#link #writeEndArray}) after writing all values
     * of the value Array.
     *
     * @param fieldName Name of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeArrayFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartArray();
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * (that will contain an Object value), and the START_OBJECT marker.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeStartObject();
     *</pre>
     *<p>
     * Note: caller still has to take care to close the Object
     * (by calling {#link #writeEndObject}) after writing all
     * entries of the value Object.
     *
     * @param fieldName Name of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeObjectFieldStart(String fieldName) throws IOException {
        writeFieldName(fieldName);
        writeStartObject();
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has contents of specific Java object as its value.
     * Equivalent to:
     *<pre>
     *  writeFieldName(fieldName);
     *  writeObject(pojo);
     *</pre>
     *<p>
     * NOTE: actual serialization of POJO value requires assigned {@code ObjectCodec}
     * and will delegate to that (usually {@code ObjectMapper} of databind layer)
     *
     * @param fieldName Name of the field to write
     * @param pojo POJO value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writePOJOField(String fieldName, Object pojo) throws IOException {
        writeObjectField(fieldName, pojo);
    }

    // TODO: deprecate in 2.14 or later
    /**
     * Older alias for {@link #writePOJOField}
     *
     * @param fieldName Name of the field to write
     * @param pojo POJO value of the field to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void writeObjectField(String fieldName, Object pojo) throws IOException {
        writeFieldName(fieldName);
        writeObject(pojo);
    }

    // // // But this method does need to be delegate so...
    
    /**
     * Method called to indicate that a property in this position was
     * skipped. It is usually only called for generators that return
     * <code>false</code> from {@link #canOmitFields()}.
     *<p>
     * Default implementation does nothing.
     *
     * @param fieldName Name of the field omitted
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     * 
     * @since 2.3
     */
    public void writeOmittedField(String fieldName) throws IOException { }

    /*
    /**********************************************************************
    /* Public API, copy-through methods
    /**********************************************************************
     */

    /**
     * Method for copying contents of the current event that
     * the given parser instance points to.
     * Note that the method <b>will not</b> copy any other events,
     * such as events contained within JSON Array or Object structures.
     *<p>
     * Calling this method will not advance the given
     * parser, although it may cause parser to internally process
     * more data (if it lazy loads contents of value events, for example)
     *
     * @param p Parser that points to event (token) to copy
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void copyCurrentEvent(JsonParser p) throws IOException
    {
        JsonToken t = p.currentToken();
        final int token = (t == null) ? ID_NOT_AVAILABLE : t.id();
        switch (token) {
        case ID_NOT_AVAILABLE:
            _reportError("No current event to copy");
            break; // never gets here
        case ID_START_OBJECT:
            writeStartObject();
            break;
        case ID_END_OBJECT:
            writeEndObject();
            break;
        case ID_START_ARRAY:
            writeStartArray();
            break;
        case ID_END_ARRAY:
            writeEndArray();
            break;
        case ID_FIELD_NAME:
            writeFieldName(p.getCurrentName());
            break;
        case ID_STRING:
            if (p.hasTextCharacters()) {
                writeString(p.getTextCharacters(), p.getTextOffset(), p.getTextLength());
            } else {
                writeString(p.getText());
            }
            break;
        case ID_NUMBER_INT:
        {
            NumberType n = p.getNumberType();
            if (n == NumberType.INT) {
                writeNumber(p.getIntValue());
            } else if (n == NumberType.BIG_INTEGER) {
                writeNumber(p.getBigIntegerValue());
            } else {
                writeNumber(p.getLongValue());
            }
            break;
        }
        case ID_NUMBER_FLOAT:
        {
            NumberType n = p.getNumberType();
            if (n == NumberType.BIG_DECIMAL) {
                writeNumber(p.getDecimalValue());
            } else if (n == NumberType.FLOAT) {
                writeNumber(p.getFloatValue());
            } else {
                writeNumber(p.getDoubleValue());
            }
            break;
        }
        case ID_TRUE:
            writeBoolean(true);
            break;
        case ID_FALSE:
            writeBoolean(false);
            break;
        case ID_NULL:
            writeNull();
            break;
        case ID_EMBEDDED_OBJECT:
            writeObject(p.getEmbeddedObject());
            break;
        default:
            throw new IllegalStateException("Internal error: unknown current token, "+t);
        }
    }

    /**
     * Method for copying contents of the current event
     * <b>and following events that it encloses</b>
     * the given parser instance points to.
     *<p>
     * So what constitutes enclosing? Here is the list of
     * events that have associated enclosed events that will
     * get copied:
     *<ul>
     * <li>{@link JsonToken#START_OBJECT}:
     *   all events up to and including matching (closing)
     *   {@link JsonToken#END_OBJECT} will be copied
     *  </li>
     * <li>{@link JsonToken#START_ARRAY}
     *   all events up to and including matching (closing)
     *   {@link JsonToken#END_ARRAY} will be copied
     *  </li>
     * <li>{@link JsonToken#FIELD_NAME} the logical value (which
     *   can consist of a single scalar value; or a sequence of related
     *   events for structured types (JSON Arrays, Objects)) will
     *   be copied along with the name itself. So essentially the
     *   whole <b>field entry</b> (name and value) will be copied.
     *  </li>
     *</ul>
     *<p>
     * After calling this method, parser will point to the
     * <b>last event</b> that was copied. This will either be
     * the event parser already pointed to (if there were no
     * enclosed events), or the last enclosed event copied.
     *
     * @param p Parser that points to the value to copy
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    public void copyCurrentStructure(JsonParser p) throws IOException
    {
        JsonToken t = p.currentToken();
        // Let's handle field-name separately first
        int id = (t == null) ? ID_NOT_AVAILABLE : t.id();
        if (id == ID_FIELD_NAME) {
            writeFieldName(p.getCurrentName());
            t = p.nextToken();
            id = (t == null) ? ID_NOT_AVAILABLE : t.id();
            // fall-through to copy the associated value
        }
        switch (id) {
        case ID_START_OBJECT:
            writeStartObject();
            _copyCurrentContents(p);
            return;
        case ID_START_ARRAY:
            writeStartArray();
            _copyCurrentContents(p);
            return;

        default:
            copyCurrentEvent(p);
        }
    }

    // @since 2.10
    protected void _copyCurrentContents(JsonParser p) throws IOException
    {
        int depth = 1;
        JsonToken t;

        // Mostly copied from `copyCurrentEvent()`, but with added nesting counts
        while ((t = p.nextToken()) != null) {
            switch (t.id()) {
            case ID_FIELD_NAME:
                writeFieldName(p.getCurrentName());
                break;

            case ID_START_ARRAY:
                writeStartArray();
                ++depth;
                break;

            case ID_START_OBJECT:
                writeStartObject();
                ++depth;
                break;

            case ID_END_ARRAY:
                writeEndArray();
                if (--depth == 0) {
                    return;
                }
                break;
            case ID_END_OBJECT:
                writeEndObject();
                if (--depth == 0) {
                    return;
                }
                break;

            case ID_STRING:
                if (p.hasTextCharacters()) {
                    writeString(p.getTextCharacters(), p.getTextOffset(), p.getTextLength());
                } else {
                    writeString(p.getText());
                }
                break;
            case ID_NUMBER_INT:
            {
                NumberType n = p.getNumberType();
                if (n == NumberType.INT) {
                    writeNumber(p.getIntValue());
                } else if (n == NumberType.BIG_INTEGER) {
                    writeNumber(p.getBigIntegerValue());
                } else {
                    writeNumber(p.getLongValue());
                }
                break;
            }
            case ID_NUMBER_FLOAT:
            {
                NumberType n = p.getNumberType();
                if (n == NumberType.BIG_DECIMAL) {
                    writeNumber(p.getDecimalValue());
                } else if (n == NumberType.FLOAT) {
                    writeNumber(p.getFloatValue());
                } else {
                    writeNumber(p.getDoubleValue());
                }
                break;
            }
            case ID_TRUE:
                writeBoolean(true);
                break;
            case ID_FALSE:
                writeBoolean(false);
                break;
            case ID_NULL:
                writeNull();
                break;
            case ID_EMBEDDED_OBJECT:
                writeObject(p.getEmbeddedObject());
                break;
            default:
                throw new IllegalStateException("Internal error: unknown current token, "+t);
            }
        }
    }

    /*
    /**********************************************************************
    /* Public API, buffer handling
    /**********************************************************************
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
    /* Helper methods for sub-classes
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
        throw new JsonGenerationException(msg, this);
    }

    protected final void _throwInternal() { VersionUtil.throwInternal(); }

    protected void _reportUnsupportedOperation() {
        throw new UnsupportedOperationException("Operation not supported by generator of type "+getClass().getName());
    }

    // @since 2.8
    protected final void _verifyOffsets(int arrayLength, int offset, int length)
    {
        if ((offset < 0) || (offset + length) > arrayLength) {
            throw new IllegalArgumentException(String.format(
                    "invalid argument(s) (offset=%d, length=%d) for input array of %d element",
                    offset, length, arrayLength));
        }
    }

    /**
     * Helper method to try to call appropriate write method for given
     * untyped Object. At this point, no structural conversions should be done,
     * only simple basic types are to be coerced as necessary.
     *
     * @param value Value to write
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
    protected void _writeSimpleObject(Object value) throws IOException
    {
        // 31-Dec-2009, tatu: Actually, we could just handle some basic
        //    types even without codec. This can improve interoperability,
        //    and specifically help with TokenBuffer.
        if (value == null) {
            writeNull();
            return;
        }
        if (value instanceof String) {
            writeString((String) value);
            return;
        }
        if (value instanceof Number) {
            Number n = (Number) value;
            if (n instanceof Integer) {
                writeNumber(n.intValue());
                return;
            } else if (n instanceof Long) {
                writeNumber(n.longValue());
                return;
            } else if (n instanceof Double) {
                writeNumber(n.doubleValue());
                return;
            } else if (n instanceof Float) {
                writeNumber(n.floatValue());
                return;
            } else if (n instanceof Short) {
                writeNumber(n.shortValue());
                return;
            } else if (n instanceof Byte) {
                writeNumber(n.byteValue());
                return;
            } else if (n instanceof BigInteger) {
                writeNumber((BigInteger) n);
                return;
            } else if (n instanceof BigDecimal) {
                writeNumber((BigDecimal) n);
                return;

            // then Atomic types
            } else if (n instanceof AtomicInteger) {
                writeNumber(((AtomicInteger) n).get());
                return;
            } else if (n instanceof AtomicLong) {
                writeNumber(((AtomicLong) n).get());
                return;
            }
        } else if (value instanceof byte[]) {
            writeBinary((byte[]) value);
            return;
        } else if (value instanceof Boolean) {
            writeBoolean((Boolean) value);
            return;
        } else if (value instanceof AtomicBoolean) {
            writeBoolean(((AtomicBoolean) value).get());
            return;
        }
        throw new IllegalStateException("No ObjectCodec defined for the generator, can only serialize simple wrapper types (type passed "
                +value.getClass().getName()+")");
    }    
}
