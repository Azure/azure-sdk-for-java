// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/*
 * Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.io.ContentReference;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.UTF8Writer;
import com.azure.json.implementation.jackson.core.json.ByteSourceJsonBootstrapper;
import com.azure.json.implementation.jackson.core.json.ReaderBasedJsonParser;
import com.azure.json.implementation.jackson.core.json.UTF8JsonGenerator;
import com.azure.json.implementation.jackson.core.json.WriterBasedJsonGenerator;
import com.azure.json.implementation.jackson.core.sym.ByteQuadsCanonicalizer;
import com.azure.json.implementation.jackson.core.sym.CharsToNameCanonicalizer;
import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import com.azure.json.implementation.jackson.core.util.BufferRecyclers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * The main factory class of Jackson package, used to configure and
 * construct reader (aka parser, {@link JsonParser})
 * and writer (aka generator, {@link JsonGenerator})
 * instances.
 *<p>
 * Factory instances are thread-safe and reusable after configuration
 * (if any). Typically applications and services use only a single
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
public class JsonFactory {

    /*
     * /**********************************************************
     * /* Buffer, symbol table management
     * /**********************************************************
     */

    /**
     * Each factory comes equipped with a shared root symbol table.
     * It should not be linked back to the original blueprint, to
     * avoid contents from leaking between factories.
     */
    protected final transient CharsToNameCanonicalizer _rootCharSymbols = CharsToNameCanonicalizer.createRoot();

    /**
     * Alternative to the basic symbol table, some stream-based
     * parsers use different name canonicalization method.
     *<p>
     * TODO: should clean up this; looks messy having 2 alternatives
     * with not very clear differences.
     *
     * @since 2.6
     */
    protected final transient ByteQuadsCanonicalizer _byteSymbolCanonicalizer = ByteQuadsCanonicalizer.createRoot();

    /*
     * /**********************************************************
     * /* Configuration, simple feature flags
     * /**********************************************************
     */

    /**
     * Currently enabled parser features.
     */
    private static final int _parserFeatures = JsonParser.Feature.collectDefaults();

    /**
     * Currently enabled generator features.
     */
    private static final int _generatorFeatures = JsonGenerator.Feature.collectDefaults();

    /*
     * /**********************************************************
     * /* Construction
     * /**********************************************************
     */

    /**
     * Constructor used when copy()ing a factory instance.
     *
     * @since 2.2.1
     */
    public JsonFactory() {
    }

    /*
     * /**********************************************************
     * /* Parser factories, traditional (blocking) I/O sources
     * /**********************************************************
     */

    /**
     * Method for constructing JSON parser instance to parse
     * the contents accessed via specified input stream.
     *<p>
     *
     * Note: no encoding argument is taken since it can always be
     * auto-detected as suggested by JSON RFC. Json specification
     * supports only UTF-8, UTF-16 and UTF-32 as valid encodings,
     * so auto-detection implemented only for this charsets.
     * For other charsets use {@link #createParser(Reader)}.
     *
     * @param in InputStream to use for reading JSON content to parse
     *
     * @since 2.1
     */
    public JsonParser createParser(InputStream in) throws IOException {
        return new ByteSourceJsonBootstrapper(_createContext(ContentReference.construct(in), false), in)
            .constructParser(_parserFeatures, _byteSymbolCanonicalizer, _rootCharSymbols);
    }

    /**
     * Method for constructing parser for parsing
     * the contents accessed via specified Reader.
     *
     * @param r Reader to use for reading JSON content to parse
     *
     * @since 2.1
     */
    public JsonParser createParser(Reader r) {
        // false -> we do NOT own Reader (did not create it)
        return new ReaderBasedJsonParser(_createContext(ContentReference.construct(r), false), _parserFeatures, r,
            _rootCharSymbols.makeChild());
    }

    /**
     * Method for constructing parser for parsing
     * the contents of given byte array.
     *
     * @since 2.1
     */
    public JsonParser createParser(byte[] data) throws IOException {
        IOContext ctxt = _createContext(ContentReference.construct(data), true);
        return new ByteSourceJsonBootstrapper(ctxt, data, 0, data.length).constructParser(_parserFeatures,
            _byteSymbolCanonicalizer, _rootCharSymbols);
    }

    /**
     * Method for constructing parser for parsing
     * contents of given String.
     *
     * @since 2.1
     */
    public JsonParser createParser(String content) {
        final int strLen = content.length();
        // Actually, let's use this for medium-sized content, up to 64kB chunk (32kb char)
        IOContext ctxt = _createContext(ContentReference.construct(content), true);
        char[] buf = ctxt.allocTokenBuffer(strLen);
        content.getChars(0, strLen, buf, 0);
        return new ReaderBasedJsonParser(ctxt, _parserFeatures, null, _rootCharSymbols.makeChild(), buf, 0, strLen,
            true);
    }

    /*
     * /**********************************************************
     * /* Generator factories
     * /**********************************************************
     */

    /**
     * Method for constructing JSON generator for writing JSON content
     * using specified output stream.
     * Encoding to use must be specified, and needs to be one of available
     * types (as per JSON specification).
     *<p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed,
     * so that generator will NOT close the output stream when
     * {@link JsonGenerator#close} is called (unless auto-closing
     * feature,
     * {@link JsonGenerator.Feature#AUTO_CLOSE_TARGET}
     * is enabled).
     * Using application needs to close it explicitly if this is the case.
     *<p>
     * Note: there are formats that use fixed encoding (like most binary data formats)
     * and that ignore passed in encoding.
     *
     * @param out OutputStream to use for writing JSON content
     * @param enc Character encoding to use
     *
     * @since 2.1
     */
    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        // false -> we won't manage the stream unless explicitly directed to
        IOContext ctxt = _createContext(ContentReference.construct(out), false);
        ctxt.setEncoding(enc);
        if (enc == JsonEncoding.UTF8) {
            return new UTF8JsonGenerator(ctxt, _generatorFeatures, out);
        }
        Writer w = _createWriter(out, enc, ctxt);
        return _createGenerator(w, ctxt);
    }

    /**
     * Convenience method for constructing generator that uses default
     * encoding of the format (UTF-8 for JSON and most other data formats).
     *<p>
     * Note: there are formats that use fixed encoding (like most binary data formats).
     *
     * @since 2.1
     */
    public JsonGenerator createGenerator(OutputStream out) throws IOException {
        return createGenerator(out, JsonEncoding.UTF8);
    }

    /**
     * Method for constructing JSON generator for writing JSON content
     * using specified Writer.
     *<p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed,
     * so that generator will NOT close the Reader when
     * {@link JsonGenerator#close} is called (unless auto-closing
     * feature,
     * {@link JsonGenerator.Feature#AUTO_CLOSE_TARGET} is enabled).
     * Using application needs to close it explicitly.
     *
     * @since 2.1
     *
     * @param w Writer to use for writing JSON content
     */
    public JsonGenerator createGenerator(Writer w) {
        return _createGenerator(w, _createContext(ContentReference.construct(w), false));
    }

    /*
     * /**********************************************************
     * /* Factory methods used by factory for creating generator instances,
     * /* overridable by sub-classes
     * /**********************************************************
     */

    /**
     * Overridable factory method that actually instantiates generator for
     * given {@link Writer} and context object.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     *
     * @param out Writer underlying writer to write generated content to
     * @param ctxt I/O context to use
     *
     * @return This factory instance (to allow call chaining)
     *
     */
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) {
        return new WriterBasedJsonGenerator(ctxt, _generatorFeatures, out);
    }

    protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException {
        // note: this should not get called any more (caller checks, dispatches)
        if (enc == JsonEncoding.UTF8) { // We have optimized writer for UTF-8
            return new UTF8Writer(ctxt, out);
        }
        // not optimal, but should do unless we really care about UTF-16/32 encoding speed
        return new OutputStreamWriter(out, enc.getJavaName());
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
    private static BufferRecycler _getBufferRecycler() {
        return BufferRecyclers.getBufferRecycler();
    }

    /**
     * Overridable factory method that actually instantiates desired
     * context object.
     *
     * @param contentRef Source/target reference to use for diagnostics, exception messages
     * @param resourceManaged Whether input/output buffer is managed by this factory or not
     *
     * @return I/O context created
     */
    private static IOContext _createContext(ContentReference contentRef, boolean resourceManaged) {
        // 21-Mar-2021, tatu: Bit of defensive coding for backwards compatibility
        return new IOContext(_getBufferRecycler(), contentRef, resourceManaged);
    }

}
