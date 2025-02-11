// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package io.clientcore.core.serialization.json.implementation.jackson.core;

import io.clientcore.core.serialization.json.implementation.jackson.core.io.ContentReference;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.IOContext;
import io.clientcore.core.serialization.json.implementation.jackson.core.json.ReaderBasedJsonParser;
import io.clientcore.core.serialization.json.implementation.jackson.core.json.WriterBasedJsonGenerator;
import io.clientcore.core.serialization.json.implementation.jackson.core.sym.CharsToNameCanonicalizer;
import io.clientcore.core.serialization.json.implementation.jackson.core.util.BufferRecycler;
import io.clientcore.core.serialization.json.implementation.jackson.core.util.BufferRecyclers;

import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

/**
 * The main factory class of Jackson package, used to configure and
 * construct reader (aka parser, {@link JsonParser})
 * and writer (aka generator, {@link JsonGenerator})
 * instances.
 *<p>
 * Factory instances are thread-safe and reusable after configuration
 * (if any). Typically, applications and services use only a single
 * globally shared factory instance, unless they need differently
 * configured factories. Factory reuse is important if efficiency matters;
 * most recycling of expensive construct is done on per-factory basis.
 *<p>
 * Creation of a factory instance is a light-weight operation,
 * and since there is no need for pluggable alternative implementations
 * (as there is no "standard" JSON processor API to implement),
 * the default constructor is used for constructing factory
 * instances.
 *
 * @author Tatu Saloranta
 */
public class JsonFactory implements Serializable // since 2.1 (for Android, mostly)
{
    private static final long serialVersionUID = 2;

    /*
     * /**********************************************************
     * /* Constants
     * /**********************************************************
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

    /**
     * @since 2.10
     */
    public final static char DEFAULT_QUOTE_CHAR = '"';

    /*
     * /**********************************************************
     * /* Construction
     * /**********************************************************
     */

    /**
     * Default constructor used to create factory instances.
     * Creation of a factory instance is a light-weight operation,
     * but it is still a good idea to reuse limited number of
     * factory instances (and quite often just a single instance):
     * factories are used as context for storing some reused
     * processing objects (such as symbol tables parsers use)
     * and this reuse only works within context of a single
     * factory instance.
     */
    public JsonFactory() {
    }

    /*
     * /**********************************************************
     * /* Parser factories, traditional (blocking) I/O sources
     * /**********************************************************
     */

    /**
     * Method for constructing parser for parsing
     * the contents accessed via specified Reader.
     <p>
     * The read stream will <b>not be owned</b> by
     * the parser.
     *
     * @param r Reader to use for reading JSON content to parse
     *
     * @since 2.1
     */
    public JsonParser createParser(Reader r) {
        // false -> we do NOT own Reader (did not create it)
        IOContext ctxt = _createContext(_createContentReference(r));
        return new ReaderBasedJsonParser(ctxt, DEFAULT_PARSER_FEATURE_FLAGS, r,
            CharsToNameCanonicalizer.createRoot().makeChild());
    }

    /*
     * /**********************************************************
     * /* Generator factories
     * /**********************************************************
     */

    /**
     * Method for constructing JSON generator for writing JSON content
     * using specified Writer.
     *<p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed,
     * so that generator will NOT close the Reader when
     * {@link JsonGenerator#close} is called.
     * Using application needs to close it explicitly.
     *
     * @since 2.1
     *
     * @param w Writer to use for writing JSON content
     */
    public JsonGenerator createGenerator(Writer w) {
        IOContext ctxt = _createContext(_createContentReference(w));
        return new WriterBasedJsonGenerator(ctxt, DEFAULT_GENERATOR_FEATURE_FLAGS, w);
    }

    /*
     * /**********************************************************
     * /* Internal factory methods, other
     * /**********************************************************
     */

    /**
     * Method used by factory to create buffer recycler instances
     * for parsers and generators.
     *<p>
     * Note: only public to give access for {@code ObjectMapper}
     *
     * @return Buffer recycler instance to use
     */
    public BufferRecycler _getBufferRecycler() {
        // 23-Apr-2015, tatu: Let's allow disabling of buffer recycling
        // scheme, for cases where it is considered harmful (possibly
        // on Android, for example)
        return BufferRecyclers.getBufferRecycler();
    }

    /**
     * Overridable factory method that actually instantiates desired
     * context object.
     *
     * @param contentRef Source/target reference to use for diagnostics, exception messages
     * @return I/O context created
     */
    protected IOContext _createContext(ContentReference contentRef) {
        // 21-Mar-2021, tatu: Bit of defensive coding for backwards compatibility
        if (contentRef == null) {
            contentRef = ContentReference.unknown();
        }
        return new IOContext(_getBufferRecycler(), contentRef);
    }

    /**
     * Overridable factory method for constructing {@link ContentReference}
     * to pass to parser or generator being created; used in cases where no offset
     * or length is applicable (either irrelevant, or full contents assumed).
     *
     * @param contentAccessor Access to underlying content; depends on source/target,
     *    as well as content representation
     *
     * @return Reference instance to use
     *
     * @since 2.13
     */
    protected ContentReference _createContentReference(Object contentAccessor) {
        // 21-Mar-2021, tatu: For now assume "canHandleBinaryNatively()" is reliable
        // indicator of textual vs binary format:
        return ContentReference.construct(contentAccessor);
    }
}
