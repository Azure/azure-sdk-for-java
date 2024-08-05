// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.azure.json.implementation.jackson.core.io.InputDecorator;
import com.azure.json.implementation.jackson.core.io.OutputDecorator;
import com.azure.json.implementation.jackson.core.json.JsonReadFeature;
import com.azure.json.implementation.jackson.core.json.JsonWriteFeature;
import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import com.azure.json.implementation.jackson.core.util.RecyclerPool;
import com.azure.json.implementation.jackson.core.util.JsonRecyclerPools;
import com.azure.json.implementation.jackson.core.util.JsonGeneratorDecorator;

/**
 * Since 2.10, Builder class is offered for creating token stream factories
 * with difference configurations: with 3.x they will be fully immutable.
 *
 * @since 2.10
 */
public abstract class TSFBuilder<F extends JsonFactory, B extends TSFBuilder<F, B>> {
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

    protected TSFBuilder(JsonFactory base) {
        this(base._factoryFeatures, base._parserFeatures, base._generatorFeatures);
        _inputDecorator = base._inputDecorator;
        _outputDecorator = base._outputDecorator;
        _streamReadConstraints = base._streamReadConstraints;
        _streamWriteConstraints = base._streamWriteConstraints;
        _errorReportConfiguration = base._errorReportConfiguration;
        _generatorDecorators = _copy(base._generatorDecorators);
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

    // @since 2.16
    protected static <T> List<T> _copy(List<T> src) {
        if (src == null) {
            return src;
        }
        return new ArrayList<>(src);
    }

    // // // Accessors

    public int factoryFeaturesMask() {
        return _factoryFeatures;
    }

    public int streamReadFeatures() {
        return _streamReadFeatures;
    }

    public int streamWriteFeatures() {
        return _streamWriteFeatures;
    }

    public RecyclerPool<BufferRecycler> recyclerPool() {
        return _recyclerPool;
    }

    public InputDecorator inputDecorator() {
        return _inputDecorator;
    }

    public OutputDecorator outputDecorator() {
        return _outputDecorator;
    }

    // // // Factory features

    public B enable(JsonFactory.Feature f) {
        _factoryFeatures |= f.getMask();
        return _this();
    }

    public B disable(JsonFactory.Feature f) {
        _factoryFeatures &= ~f.getMask();
        return _this();
    }

    public B configure(JsonFactory.Feature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    // // // StreamReadFeatures (replacement of non-json-specific parser features)

    public B enable(StreamReadFeature f) {
        _streamReadFeatures |= f.mappedFeature().getMask();
        return _this();
    }

    public B enable(StreamReadFeature first, StreamReadFeature... other) {
        _streamReadFeatures |= first.mappedFeature().getMask();
        for (StreamReadFeature f : other) {
            _streamReadFeatures |= f.mappedFeature().getMask();
        }
        return _this();
    }

    public B disable(StreamReadFeature f) {
        _streamReadFeatures &= ~f.mappedFeature().getMask();
        return _this();
    }

    public B disable(StreamReadFeature first, StreamReadFeature... other) {
        _streamReadFeatures &= ~first.mappedFeature().getMask();
        for (StreamReadFeature f : other) {
            _streamReadFeatures &= ~f.mappedFeature().getMask();
        }
        return _this();
    }

    public B configure(StreamReadFeature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    // // // StreamWriteFeatures (replacement of non-json-specific generator features)

    public B enable(StreamWriteFeature f) {
        _streamWriteFeatures |= f.mappedFeature().getMask();
        return _this();
    }

    public B enable(StreamWriteFeature first, StreamWriteFeature... other) {
        _streamWriteFeatures |= first.mappedFeature().getMask();
        for (StreamWriteFeature f : other) {
            _streamWriteFeatures |= f.mappedFeature().getMask();
        }
        return _this();
    }

    public B disable(StreamWriteFeature f) {
        _streamWriteFeatures &= ~f.mappedFeature().getMask();
        return _this();
    }

    public B disable(StreamWriteFeature first, StreamWriteFeature... other) {
        _streamWriteFeatures &= ~first.mappedFeature().getMask();
        for (StreamWriteFeature f : other) {
            _streamWriteFeatures &= ~f.mappedFeature().getMask();
        }
        return _this();
    }

    public B configure(StreamWriteFeature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    /* 26-Jun-2018, tatu: This should not be needed here, but due to 2.x limitations,
     *   we do need to include it or require casting.
     *   Specifically: since `JsonFactory` (and not `TokenStreamFactory`) is base class
     *   for all backends, it can not expose JSON-specific builder, but this.
     *   So let's select lesser evil(s).
     */

    // // // JSON-specific, reads

    public B enable(JsonReadFeature f) {
        return _failNonJSON(f);
    }

    public B enable(JsonReadFeature first, JsonReadFeature... other) {
        return _failNonJSON(first);
    }

    public B disable(JsonReadFeature f) {
        return _failNonJSON(f);
    }

    public B disable(JsonReadFeature first, JsonReadFeature... other) {
        return _failNonJSON(first);
    }

    public B configure(JsonReadFeature f, boolean state) {
        return _failNonJSON(f);
    }

    private B _failNonJSON(Object feature) {
        throw new IllegalArgumentException("Feature " + feature.getClass().getName() + "#" + feature.toString()
            + " not supported for non-JSON backend");
    }

    // // // JSON-specific, writes

    public B enable(JsonWriteFeature f) {
        return _failNonJSON(f);
    }

    public B enable(JsonWriteFeature first, JsonWriteFeature... other) {
        return _failNonJSON(first);
    }

    public B disable(JsonWriteFeature f) {
        return _failNonJSON(f);
    }

    public B disable(JsonWriteFeature first, JsonWriteFeature... other) {
        return _failNonJSON(first);
    }

    public B configure(JsonWriteFeature f, boolean state) {
        return _failNonJSON(f);
    }

    // // // Other configuration, helper objects

    /**
     * @param p RecyclerPool to use for buffer allocation
     *
     * @return this builder (for call chaining)
     *
     * @since 2.16
     */
    public B recyclerPool(RecyclerPool<BufferRecycler> p) {
        _recyclerPool = Objects.requireNonNull(p);
        return _this();
    }

    // // // Other configuration, decorators

    public B inputDecorator(InputDecorator dec) {
        _inputDecorator = dec;
        return _this();
    }

    public B outputDecorator(OutputDecorator dec) {
        _outputDecorator = dec;
        return _this();
    }

    public B addDecorator(JsonGeneratorDecorator decorator) {
        if (_generatorDecorators == null) {
            _generatorDecorators = new ArrayList<>();
        }
        _generatorDecorators.add(decorator);
        return _this();
    }

    // // // Other configuration, constraints

    /**
     * Sets the constraints for streaming reads.
     *
     * @param streamReadConstraints constraints for streaming reads
     * @return this builder (for call chaining)
     * @since 2.15
     */
    public B streamReadConstraints(StreamReadConstraints streamReadConstraints) {
        _streamReadConstraints = Objects.requireNonNull(streamReadConstraints);
        return _this();
    }

    /**
     * Sets the constraints for streaming writes.
     *
     * @param streamWriteConstraints constraints for streaming reads
     * @return this builder (for call chaining)
     * @since 2.16
     */
    public B streamWriteConstraints(StreamWriteConstraints streamWriteConstraints) {
        _streamWriteConstraints = Objects.requireNonNull(streamWriteConstraints);
        return _this();
    }

    /**
     * Sets the configuration for error reporting.
     *
     * @param errorReportConfiguration configuration values used for handling erroneous token inputs. 
     * @return this builder (for call chaining)
     * @since 2.16
     */
    public B errorReportConfiguration(ErrorReportConfiguration errorReportConfiguration) {
        _errorReportConfiguration = Objects.requireNonNull(errorReportConfiguration);
        return _this();
    }

    // // // Other methods

    /**
     * Method for constructing actual {@link TokenStreamFactory} instance, given
     * configuration.
     *
     * @return {@link TokenStreamFactory} build based on current configuration
     */
    public abstract F build();

    // silly convenience cast method we need
    @SuppressWarnings("unchecked")
    protected final B _this() {
        return (B) this;
    }

    // // // Support for subtypes

    protected void _legacyEnable(JsonParser.Feature f) {
        if (f != null) {
            _streamReadFeatures |= f.getMask();
        }
    }

    protected void _legacyDisable(JsonParser.Feature f) {
        if (f != null) {
            _streamReadFeatures &= ~f.getMask();
        }
    }

    protected void _legacyEnable(JsonGenerator.Feature f) {
        if (f != null) {
            _streamWriteFeatures |= f.getMask();
        }
    }

    protected void _legacyDisable(JsonGenerator.Feature f) {
        if (f != null) {
            _streamWriteFeatures &= ~f.getMask();
        }
    }
}
