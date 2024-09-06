// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.io.CharacterEscapes;
import com.azure.json.implementation.jackson.core.json.JsonReadFeature;
import com.azure.json.implementation.jackson.core.json.JsonWriteFeature;

/**
 * {@link TSFBuilder}
 * implementation for constructing vanilla {@link JsonFactory}
 * instances for reading/writing JSON encoded content.
 *<p>
 * NOTE: as of Jackson 2.x, use of JSON-specific builder is bit cumbersome
 * since {@link JsonFactory} serves dual duty of base class AND actual
 * implementation for JSON backend. This will be fixed in Jackson 3.0.
 *
 * @since 2.10
 */
public class JsonFactoryBuilder extends TSFBuilder<JsonFactory, JsonFactoryBuilder> {
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

    /*
     * /**********************************************************
     * /* Mutators
     * /**********************************************************
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

    // // // Accessors for JSON-specific settings

    @Override
    public JsonFactory build() {
        // 28-Dec-2017, tatu: No special settings beyond base class ones, so:
        return new JsonFactory(this);
    }
}
