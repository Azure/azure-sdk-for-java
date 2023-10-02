// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import com.typespec.json.implementation.jackson.core.io.CharacterEscapes;
import com.typespec.json.implementation.jackson.core.io.SerializedString;
import com.typespec.json.implementation.jackson.core.json.JsonReadFeature;
import com.typespec.json.implementation.jackson.core.json.JsonWriteFeature;

/**
 * {@link com.typespec.json.implementation.jackson.core.TSFBuilder}
 * implementation for constructing vanilla {@link JsonFactory}
 * instances for reading/writing JSON encoded content.
 *<p>
 * NOTE: as of Jackson 2.x, use of JSON-specific builder is bit cumbersome
 * since {@link JsonFactory} serves dual duty of base class AND actual
 * implementation for JSON backend. This will be fixed in Jackson 3.0.
 *
 * @since 2.10
 */
public class JsonFactoryBuilder extends TSFBuilder<JsonFactory, JsonFactoryBuilder>
{
    protected CharacterEscapes _characterEscapes;

    protected SerializableString _rootValueSeparator;

    protected int _maximumNonEscapedChar;

    /**
     * Character used for quoting field names (if field name quoting has not
     * been disabled with {@link JsonWriteFeature#QUOTE_FIELD_NAMES})
     * and JSON String values.
     */
    protected char _quoteChar = JsonFactory.DEFAULT_QUOTE_CHAR;

    public JsonFactoryBuilder() {
        super();
        _rootValueSeparator = JsonFactory.DEFAULT_ROOT_VALUE_SEPARATOR;
        _maximumNonEscapedChar = 0;
    }

    public JsonFactoryBuilder(JsonFactory base) {
        super(base);
        _characterEscapes = base.getCharacterEscapes();
        _rootValueSeparator = base._rootValueSeparator;
        _maximumNonEscapedChar = base._maximumNonEscapedChar;
    }

    /*
    /**********************************************************
    /* Mutators
    /**********************************************************
     */

    // // // JSON-parsing features

    @Override
    public JsonFactoryBuilder enable(JsonReadFeature f) {
        _legacyEnable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder enable(JsonReadFeature first, JsonReadFeature... other) {
        _legacyEnable(first.mappedFeature());
        enable(first);
        for (JsonReadFeature f : other) {
            _legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonReadFeature f) {
        _legacyDisable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonReadFeature first, JsonReadFeature... other) {
        _legacyDisable(first.mappedFeature());
        for (JsonReadFeature f : other) {
            _legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder configure(JsonReadFeature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    // // // JSON-generating features

    @Override
    public JsonFactoryBuilder enable(JsonWriteFeature f) {
        JsonGenerator.Feature old = f.mappedFeature();
        if (old != null) {
            _legacyEnable(old);
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder enable(JsonWriteFeature first, JsonWriteFeature... other) {
        _legacyEnable(first.mappedFeature());
        for (JsonWriteFeature f : other) {
            _legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonWriteFeature f) {
        _legacyDisable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonWriteFeature first, JsonWriteFeature... other) {
        _legacyDisable(first.mappedFeature());
        for (JsonWriteFeature f : other) {
            _legacyDisable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder configure(JsonWriteFeature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    // // // JSON-specific helper objects, settings
    
    /**
     * Method for defining custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     *
     * @param esc CharacterEscapes to configure, if any; {@code null} if none
     *
     * @return This builder instance (to allow call chaining)
     */
    public JsonFactoryBuilder characterEscapes(CharacterEscapes esc) {
        _characterEscapes = esc;
        return this;
    }

    /**
     * Method that allows overriding String used for separating root-level
     * JSON values (default is single space character)
     * 
     * @param sep Separator to use, if any; null means that no separator is
     *   automatically added
     *
     * @return This builder instance (to allow call chaining)
     */
    public JsonFactoryBuilder rootValueSeparator(String sep) {
        _rootValueSeparator = (sep == null) ? null : new SerializedString(sep);
        return this;
    }

    /**
     * Method that allows overriding String used for separating root-level
     * JSON values (default is single space character)
     * 
     * @param sep Separator to use, if any; null means that no separator is
     *   automatically added
     *
     * @return This builder instance (to allow call chaining)
     */
    public JsonFactoryBuilder rootValueSeparator(SerializableString sep) {
        _rootValueSeparator = sep;
        return this;
    }

    /**
     * Method that allows specifying threshold beyond which all characters are
     * automatically escaped (without checking possible custom escaping settings
     * a la {@link #characterEscapes}: for example, to force escaping of all non-ASCII
     * characters (set to 127), or all non-Latin-1 character (set to 255).
     * Default setting is "disabled", specified by passing value of {@code 0} (or
     * negative numbers).
     *<p>
     * NOTE! Lowest legal value (aside from marker 0) is 127: for ASCII range, other checks apply
     * and this threshold is ignored. If value between [1, 126] is specified, 127 will be
     * used instead.
     * 
     * @param maxNonEscaped Highest character code that is NOT automatically escaped; if
     *    positive value above 0, or 0 to indicate that no automatic escaping is applied
     *    beside from what JSON specification requires (and possible custom escape settings).
     *    Values between 1 and 127 are all taken to behave as if 127 is specified: that is,
     *    no automatic escaping is applied in ASCII range.
     *
     * @return This builder instance (to allow call chaining)
     */
    public JsonFactoryBuilder highestNonEscapedChar(int maxNonEscaped) {
        _maximumNonEscapedChar = (maxNonEscaped <= 0) ? 0 : Math.max(127, maxNonEscaped);
        return this;
    }

    /**
     * Method that allows specifying an alternate
     * character used for quoting field names (if field name quoting has not
     * been disabled with {@link JsonWriteFeature#QUOTE_FIELD_NAMES})
     * and JSON String values.
     *<p>
     * Default value is double-quote ({@code "}); typical alternative is
     * single-quote/apostrophe ({@code '}).
     *
     * @param ch Character to use for quoting field names and JSON String values.
     *
     * @return This builder instance (to allow call chaining)
     */
    public JsonFactoryBuilder quoteChar(char ch) {
        // 12-Aug-2019, tatu: Due to implementation details, escaping characters beyond
        //    7-bit ASCII set has deep overhead so let's limit set. If we absolutely
        //    must it is possible of course, but leads to problems combining with
        //    custom escaping aspects.
        if (ch > 0x7F) {
            throw new IllegalArgumentException("Can only use Unicode characters up to 0x7F as quote characters");
        }
        _quoteChar = ch;
        return this;
    }

    // // // Accessors for JSON-specific settings
    
    public CharacterEscapes characterEscapes() { return _characterEscapes; }
    public SerializableString rootValueSeparator() { return _rootValueSeparator; }

    public int highestNonEscapedChar() { return _maximumNonEscapedChar; }

    public char quoteChar() { return _quoteChar; }

    @Override
    public JsonFactory build() {
        // 28-Dec-2017, tatu: No special settings beyond base class ones, so:
        return new JsonFactory(this);
    }
}
