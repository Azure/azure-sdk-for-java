// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;

import com.typespec.json.implementation.jackson.core.util.JacksonFeature;

/**
 * Token writer (generator) features not-specific to any particular format backend.
 * Eventual replacement for non-JSON-specific {@link com.typespec.json.implementation.jackson.core.JsonGenerator.Feature}s.
 *
 * @since 2.10
 */
public enum StreamWriteFeature
    implements JacksonFeature // since 2.12
{
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
    AUTO_CLOSE_TARGET(JsonGenerator.Feature.AUTO_CLOSE_TARGET),

    /**
     * Feature that determines what happens when the generator is
     * closed while there are still unmatched
     * {@link JsonToken#START_ARRAY} or {@link JsonToken#START_OBJECT}
     * entries in output content. If enabled, such Array(s) and/or
     * Object(s) are automatically closed (that is, matching END_ token write
     * call is made for all open scopes); if disabled, no additional
     * write calls are made.
     *<p>
     * Feature is enabled by default.
     */
    AUTO_CLOSE_CONTENT(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT),

    /**
     * Feature that specifies that calls to {@link JsonGenerator#flush} will cause
     * matching <code>flush()</code> to underlying {@link OutputStream}
     * or {@link Writer}; if disabled this will not be done.
     * Main reason to disable this feature is to prevent flushing at
     * generator level, if it is not possible to prevent method being
     * called by other code (like <code>ObjectMapper</code> or third
     * party libraries).
     *<p>
     * Feature is enabled by default.
     */
    FLUSH_PASSED_TO_STREAM(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM),

    // // Datatype coercion features

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
     */
    WRITE_BIGDECIMAL_AS_PLAIN(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN),

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
     */
    STRICT_DUPLICATE_DETECTION(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION),
    
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
     */
    IGNORE_UNKNOWN(JsonGenerator.Feature.IGNORE_UNKNOWN),
    ;

    /**
     * Whether feature is enabled or disabled by default.
     */
    private final boolean _defaultState;

    private final int _mask;

    /**
     * For backwards compatibility we may need to map to one of existing {@link JsonParser.Feature}s;
     * if so, this is the feature to enable/disable.
     */
    final private JsonGenerator.Feature _mappedFeature;

    private StreamWriteFeature(JsonGenerator.Feature mappedTo) {
        // only for 2.x, let's map everything to legacy feature:
        _mappedFeature = mappedTo;
        _mask = mappedTo.getMask();
        _defaultState = mappedTo.enabledByDefault();
    }

    /**
     * Method that calculates bit set (flags) of all features that
     * are enabled by default.
     *
     * @return Bit mask of all features that are enabled by default
     */
    public static int collectDefaults()
    {
        int flags = 0;
        for (StreamWriteFeature f : values()) {
            if (f.enabledByDefault()) {
                flags |= f.getMask();
            }
        }
        return flags;
    }

    @Override
    public boolean enabledByDefault() { return _defaultState; }
    @Override
    public boolean enabledIn(int flags) { return (flags & _mask) != 0; }
    @Override
    public int getMask() { return _mask; }

    public JsonGenerator.Feature mappedFeature() { return _mappedFeature; }
}
