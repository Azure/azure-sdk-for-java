// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.io.InputDecorator;
import com.azure.json.implementation.jackson.core.io.OutputDecorator;
import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import com.azure.json.implementation.jackson.core.util.JsonGeneratorDecorator;
import com.azure.json.implementation.jackson.core.util.JsonRecyclerPools;
import com.azure.json.implementation.jackson.core.util.RecyclerPool;

import java.util.List;

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
     * Bitfield (set of flags) of all factory features that are enabled by default.
     */
    protected final static int DEFAULT_FACTORY_FEATURE_FLAGS = JsonFactory.Feature.collectDefaults();

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
     * Set of {@link com.azure.json.implementation.jackson.core.JsonFactory.Feature}s enabled,
     * as bitmask.
     */
    protected int _factoryFeatures;

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
    /* Other configuration
    /**********************************************************************
     */

    /**
     * @since 2.16
     */
    protected RecyclerPool<BufferRecycler> _recyclerPool;

    /**
     * Optional helper object that may decorate input sources, to do
     * additional processing on input during parsing.
     */
    protected InputDecorator _inputDecorator;

    /**
     * Optional helper object that may decorate output object, to do
     * additional processing on output during content generation.
     */
    protected OutputDecorator _outputDecorator;

    /**
     * {@link StreamReadConstraints} to use.
     *
     * @since 2.15
     */
    protected StreamReadConstraints _streamReadConstraints;

    /**
     * {@link StreamWriteConstraints} to use.
     *
     * @since 2.16
     */
    protected StreamWriteConstraints _streamWriteConstraints;

    /**
     * {@link ErrorReportConfiguration} to use.
     *
     * @since 2.16
     */
    protected ErrorReportConfiguration _errorReportConfiguration;

    /**
     * @since 2.16
     */
    protected List<JsonGeneratorDecorator> _generatorDecorators;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    protected TSFBuilder() {
        this(DEFAULT_FACTORY_FEATURE_FLAGS, DEFAULT_PARSER_FEATURE_FLAGS, DEFAULT_GENERATOR_FEATURE_FLAGS);
    }

    protected TSFBuilder(int factoryFeatures, int parserFeatures, int generatorFeatures) {
        _recyclerPool = JsonRecyclerPools.defaultPool();

        _factoryFeatures = factoryFeatures;
        _streamReadFeatures = parserFeatures;
        _streamWriteFeatures = generatorFeatures;

        _inputDecorator = null;
        _outputDecorator = null;
        _streamReadConstraints = StreamReadConstraints.defaults();
        _streamWriteConstraints = StreamWriteConstraints.defaults();
        _errorReportConfiguration = ErrorReportConfiguration.defaults();
        _generatorDecorators = null;
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
