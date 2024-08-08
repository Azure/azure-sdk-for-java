// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

/**
 * Since 2.10, Builder class is offered for creating token stream factories
 * with difference configurations: with 3.x they will be fully immutable.
 *
 * @since 2.10
 */
public abstract class TSFBuilder<F extends JsonFactory> {
    /*
    /**********************************************************************
    /* Constants
    /**********************************************************************
     */

    /**
     * Bitfield (set of flags) of all parser features that are enabled
     * by default.
     */
    protected final static int DEFAULT_PARSER_FEATURE_FLAGS = JsonParser.Feature.collectDefaults();

    /**
     * Bitfield (set of flags) of all generator features that are enabled
     * by default.
     */
    protected final static int DEFAULT_GENERATOR_FEATURE_FLAGS = JsonGenerator.Feature.collectDefaults();

    /*
    /**********************************************************************
    /* Configured features
    /**********************************************************************
     */

    /**
     * Set of {@link JsonParser.Feature}s enabled, as bitmask.
     */
    protected int _streamReadFeatures;

    /**
     * Set of {@link JsonGenerator.Feature}s enabled, as bitmask.
     */
    protected int _streamWriteFeatures;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    protected TSFBuilder() {
        _streamReadFeatures = DEFAULT_PARSER_FEATURE_FLAGS;
        _streamWriteFeatures = DEFAULT_GENERATOR_FEATURE_FLAGS;
    }

    // // // Other methods

    /**
     * Method for constructing actual {@link TokenStreamFactory} instance, given
     * configuration.
     *
     * @return {@link TokenStreamFactory} build based on current configuration
     */
    public abstract F build();
}
