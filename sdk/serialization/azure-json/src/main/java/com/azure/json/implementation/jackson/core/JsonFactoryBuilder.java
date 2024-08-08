// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

/**
 * {@link com.azure.json.implementation.jackson.core.TSFBuilder}
 * implementation for constructing vanilla {@link JsonFactory}
 * instances for reading/writing JSON encoded content.
 *<p>
 * NOTE: as of Jackson 2.x, use of JSON-specific builder is bit cumbersome
 * since {@link JsonFactory} serves dual duty of base class AND actual
 * implementation for JSON backend. This will be fixed in Jackson 3.0.
 *
 * @since 2.10
 */
public class JsonFactoryBuilder extends TSFBuilder<JsonFactory> {
    public JsonFactoryBuilder() {
        super();
    }

    /*
    /**********************************************************
    /* Mutators
    /**********************************************************
     */

    // // // Accessors for JSON-specific settings

    @Override
    public JsonFactory build() {
        // 28-Dec-2017, tatu: No special settings beyond base class ones, so:
        return new JsonFactory(this);
    }
}
