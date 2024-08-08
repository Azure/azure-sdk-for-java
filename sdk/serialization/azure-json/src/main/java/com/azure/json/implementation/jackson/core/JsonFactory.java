// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.io.ContentReference;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.json.ByteSourceJsonBootstrapper;
import com.azure.json.implementation.jackson.core.json.ReaderBasedJsonParser;
import com.azure.json.implementation.jackson.core.json.UTF8JsonGenerator;
import com.azure.json.implementation.jackson.core.json.WriterBasedJsonGenerator;
import com.azure.json.implementation.jackson.core.sym.ByteQuadsCanonicalizer;
import com.azure.json.implementation.jackson.core.sym.CharsToNameCanonicalizer;
import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import com.azure.json.implementation.jackson.core.util.JsonRecyclerPools;
import com.azure.json.implementation.jackson.core.util.RecyclerPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * The main factory class of Jackson package, used to configure and
 * construct
 * reader (aka parser, {@link JsonParser})
 * and
 * writer (aka generator, {@link JsonGenerator})
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
@SuppressWarnings("resource")
public class JsonFactory extends TokenStreamFactory implements java.io.Serializable {
    private static final long serialVersionUID = 2;

    /*
    /**********************************************************
    /* Constants
    /**********************************************************
     */

    /**
     * Bitfield (set of flags) of all parser features that are enabled
     * by default.
     */
    protected final static int DEFAULT_PARSER_FEATURE_FLAGS = JsonParser.Feature.collectDefaults();

    /**
     * @since 2.10
     */
    public final static char DEFAULT_QUOTE_CHAR = '"';

    /*
    /**********************************************************
    /* Buffer, symbol table management
    /**********************************************************
     */

    /**
     * Each factory comes equipped with a shared root symbol table.
     * It should not be linked back to the original blueprint, to
     * avoid contents from leaking between factories.
     *<p>
     * NOTE: non-final since 2.17 due to need to re-create if
     * {@link StreamReadConstraints} re-configured for factory.
     */
    protected transient CharsToNameCanonicalizer _rootCharSymbols;

    /**
     * Alternative to the basic symbol table, some stream-based
     * parsers use different name canonicalization method.
     *
     * @since 2.6
     */
    protected final transient ByteQuadsCanonicalizer _byteSymbolCanonicalizer = ByteQuadsCanonicalizer.createRoot();

    /*
    /**********************************************************
    /* Configuration, simple feature flags
    /**********************************************************
     */

    /**
     * Currently enabled parser features.
     */
    protected int _parserFeatures;

    /**
     * Currently enabled generator features.
     */
    protected int _generatorFeatures;

    /*
    /**********************************************************
    /* Construction
    /**********************************************************
     */

    /**
     * Constructor used by {@link JsonFactoryBuilder} for instantiation.
     *
     * @param b Builder that contains settings to use
     *
     * @since 2.10
     */
    public JsonFactory(JsonFactoryBuilder b) {
        // General
        _parserFeatures = b._streamReadFeatures;
        _generatorFeatures = b._streamWriteFeatures;

        _rootCharSymbols = CharsToNameCanonicalizer.createRoot(this);
    }

    /**
     * Main factory method to use for constructing {@link JsonFactory} instances with
     * different configuration: creates and returns a builder for collecting configuration
     * settings; instance created by calling {@code build()} after all configuration
     * set.
     *<p>
     * NOTE: signature unfortunately does not expose true implementation type; this
     * will be fixed in 3.0.
     *
     * @return Builder instance to use
     */
    public static TSFBuilder<?> builder() {
        return new JsonFactoryBuilder();
    }

    /*
    /**********************************************************************
    /* Constraints violation checking (2.15)
    /**********************************************************************
     */

    @Override
    public StreamReadConstraints streamReadConstraints() {
        return StreamReadConstraints.defaults();
    }

    /*
    /**********************************************************
    /* Parser factories, traditional (blocking) I/O sources
    /**********************************************************
     */

    /**
     * Method for constructing JSON parser instance to parse
     * the contents accessed via specified input stream.
     *<p>
     * The input stream will <b>not be owned</b> by
     * the parser, it will still be managed (i.e. closed if
     * end-of-stream is reacher, or parser close method called)
     * if (and only if) {@code com.azure.json.implementation.jackson.core.StreamReadFeature#AUTO_CLOSE_SOURCE}
     * is enabled.
     *<p>
     *
     * Note: no encoding argument is taken since it can always be
     * auto-detected as suggested by JSON RFC. Json specification
     * supports only UTF-8, UTF-16 and UTF-32 as valid encodings,
     * so auto-detection implemented only for this charsets.
     * For other charsets use {@link #createParser(java.io.Reader)}.
     *
     * @param in InputStream to use for reading JSON content to parse
     *
     * @since 2.1
     */
    @Override
    public JsonParser createParser(InputStream in) throws IOException {
        IOContext ctxt = _createContext(_createContentReference(in), false);
        return _createParser(in, ctxt);
    }

    /**
     * Method for constructing parser for parsing
     * the contents accessed via specified Reader.
     <p>
     * The read stream will <b>not be owned</b> by
     * the parser, it will still be managed (i.e. closed if
     * end-of-stream is reacher, or parser close method called)
     * if (and only if) {@code com.azure.json.implementation.jackson.core.StreamReadFeature#AUTO_CLOSE_SOURCE}
     * is enabled.
     *
     * @param r Reader to use for reading JSON content to parse
     *
     * @since 2.1
     */
    @Override
    public JsonParser createParser(Reader r) {
        // false -> we do NOT own Reader (did not create it)
        IOContext ctxt = _createContext(_createContentReference(r), false);
        return _createParser(r, ctxt);
    }

    /**
     * Method for constructing parser for parsing
     * the contents of given byte array.
     *
     * @since 2.1
     */
    @Override
    public JsonParser createParser(byte[] data) throws IOException {
        IOContext ctxt = _createContext(_createContentReference(data), true);
        return _createParser(data, data.length, ctxt);
    }

    /**
     * Method for constructing parser for parsing
     * contents of given String.
     *
     * @since 2.1
     */
    @Override
    public JsonParser createParser(String content) {
        final int strLen = content.length();
        // Actually, let's use this for medium-sized content, up to 64kB chunk (32kb char)
        if (strLen > 0x8000) {
            // easier to just wrap in a Reader than extend InputDecorator; or, if content
            // is too long for us to copy it over
            return createParser(new StringReader(content));
        }
        IOContext ctxt = _createContext(_createContentReference(content), true);
        char[] buf = ctxt.allocTokenBuffer(strLen);
        content.getChars(0, strLen, buf, 0);
        return _createParser(buf, strLen, ctxt);
    }

    /*
    /**********************************************************
    /* Generator factories
    /**********************************************************
     */

    /**
     * Convenience method for constructing generator that uses default
     * encoding of the format (UTF-8 for JSON and most other data formats).
     *<p>
     * Note: there are formats that use fixed encoding (like most binary data formats).
     *
     * @since 2.1
     */
    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        // false -> we won't manage the stream unless explicitly directed to
        IOContext ctxt = _createContext(_createContentReference(out), false);
        ctxt.setEncoding(JsonEncoding.UTF8);
        return _createUTF8Generator(out, ctxt);
    }

    /**
     * Method for constructing JSON generator for writing JSON content
     * using specified Writer.
     *<p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed,
     * so that generator will NOT close the Reader when
     * {@link JsonGenerator#close} is called (unless auto-closing
     * feature,
     * {@code com.azure.json.implementation.jackson.core.JsonGenerator.Feature#AUTO_CLOSE_TARGET} is enabled).
     * Using application needs to close it explicitly.
     *
     * @since 2.1
     *
     * @param w Writer to use for writing JSON content
     */
    @Override
    public JsonGenerator createGenerator(Writer w) {
        IOContext ctxt = _createContext(_createContentReference(w), false);
        return _createGenerator(w, ctxt);
    }

    /*
    /**********************************************************
    /* Factory methods used by factory for creating parser instances,
    /* overridable by sub-classes
    /**********************************************************
     */

    /**
     * Overridable factory method that actually instantiates desired parser
     * given {@link InputStream} and context object.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     *
     * @param in InputStream to use for reading content to parse
     * @param ctxt I/O context to use for parsing
     *
     * @throws IOException if parser initialization fails due to I/O (read) problem
     *
     * @return Parser constructed
     *
     * @since 2.1
     */
    protected JsonParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        try {
            return new ByteSourceJsonBootstrapper(ctxt, in).constructParser(_parserFeatures, _byteSymbolCanonicalizer,
                _rootCharSymbols);
        } catch (IOException | RuntimeException e) {
            // 10-Jun-2022, tatu: For [core#763] may need to close InputStream here
            if (ctxt.isResourceManaged()) {
                try {
                    in.close();
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                }
            }
            ctxt.close();
            throw e;
        }
    }

    /**
     * Overridable factory method that actually instantiates parser
     * using given {@link Reader} object for reading content.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     *
     * @param r Reader to use for reading content to parse
     * @param ctxt I/O context to use for parsing
     *
     * @return Actual parser to use
     *
     * @since 2.1
     */
    protected JsonParser _createParser(Reader r, IOContext ctxt) {
        return new ReaderBasedJsonParser(ctxt, _parserFeatures, r, _rootCharSymbols.makeChild());
    }

    /**
     * Overridable factory method that actually instantiates parser
     * using given <code>char[]</code> object for accessing content.
     *
     * @param data Buffer that contains content to parse
     * @param len Number of characters within buffer to parse
     * @param ctxt I/O context to use for parsing
     * @return Actual parser to use
     * @since 2.4
     */
    protected JsonParser _createParser(char[] data, int len, IOContext ctxt) {
        return new ReaderBasedJsonParser(ctxt, _parserFeatures, null, _rootCharSymbols.makeChild(), data, 0, len, true);
    }

    /**
     * Overridable factory method that actually instantiates parser
     * using given {@link Reader} object for reading content
     * passed as raw byte array.
     * <p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     *
     * @param data Buffer that contains content to parse
     * @param len Number of characters within buffer to parse
     * @param ctxt I/O context to use for parsing
     * @return Actual parser to use
     * @throws IOException if parser initialization fails due to I/O (read) problem
     */
    protected JsonParser _createParser(byte[] data, int len, IOContext ctxt) throws IOException {
        return new ByteSourceJsonBootstrapper(ctxt, data, 0, len).constructParser(_parserFeatures,
            _byteSymbolCanonicalizer, _rootCharSymbols);
    }

    /*
    /**********************************************************
    /* Factory methods used by factory for creating generator instances,
    /* overridable by sub-classes
    /**********************************************************
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

    /**
     * Overridable factory method that actually instantiates generator for
     * given {@link OutputStream} and context object, using UTF-8 encoding.
     *<p>
     * This method is specifically designed to remain
     * compatible between minor versions so that sub-classes can count
     * on it being called as expected. That is, it is part of official
     * interface from sub-class perspective, although not a public
     * method available to users of factory implementations.
     *
     * @param out OutputStream underlying writer to write generated content to
     * @param ctxt I/O context to use
     *
     * @return This factory instance (to allow call chaining)
     *
     */
    protected JsonGenerator _createUTF8Generator(OutputStream out, IOContext ctxt) {
        return new UTF8JsonGenerator(ctxt, _generatorFeatures, out);
    }

    /*
    /**********************************************************
    /* Internal factory methods, other
    /**********************************************************
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
        return _getRecyclerPool().acquireAndLinkPooled();
    }

    /**
     * Accessor for getting access to {@link RecyclerPool} for getting
     * {@link BufferRecycler} instance to use.
     *
     * @return RecyclerPool to use.
     *
     * @since 2.16
     */
    public RecyclerPool<BufferRecycler> _getRecyclerPool() {
        return JsonRecyclerPools.defaultPool();
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
    protected IOContext _createContext(ContentReference contentRef, boolean resourceManaged) {
        BufferRecycler br = null;
        boolean recyclerExternal = false;

        if (contentRef == null) {
            contentRef = ContentReference.unknown();
        } else {
            Object content = contentRef.getRawContent();
            // 18-Jan-2024, tatu: [core#1195] Let's see if we can reuse already allocated recycler
            //   (is the case when SegmentedStringWriter / ByteArrayBuilder passed)
            if (content instanceof BufferRecycler.Gettable) {
                br = ((BufferRecycler.Gettable) content).bufferRecycler();
                recyclerExternal = (br != null);
            }
        }
        if (br == null) {
            br = _getBufferRecycler();
        }
        IOContext ctxt = new IOContext(br, contentRef, resourceManaged);
        if (recyclerExternal) {
            ctxt.markBufferRecyclerReleased();
        }
        return ctxt;
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
        //    indicator of textual vs binary format:
        return ContentReference.construct(contentAccessor);
    }

}
