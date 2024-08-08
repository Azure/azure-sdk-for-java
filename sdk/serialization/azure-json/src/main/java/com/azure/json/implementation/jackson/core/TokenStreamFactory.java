// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

import java.io.*;

/**
 * Intermediate base class for actual format-specific factories for constructing
 * parsers (reading) and generators (writing). Although full power will only be
 * available with Jackson 3, skeletal implementation added in 2.10 to help conversion
 * of code for 2.x to 3.x migration of projects depending on Jackson
 *
 * @since 2.10
 */
public abstract class TokenStreamFactory implements java.io.Serializable {
    private static final long serialVersionUID = 2;

    /*
    /**********************************************************************
    /* Capability introspection
    /**********************************************************************
     */

    /**
     * Introspection method that higher-level functionality may call
     * to see whether underlying data format can read and write binary
     * data natively; that is, embeded it as-is without using encodings
     * such as Base64.
     *<p>
     * Default implementation returns <code>false</code> as JSON does not
     * support native access: all binary content must use Base64 encoding.
     * Most binary formats (like Smile and Avro) support native binary content.
     *
     * @return Whether format supported by this factory
     *    supports native binary content
     */
    public abstract boolean canHandleBinaryNatively();

    /**
     * Method for getting bit set of all {@link JsonFactory.Feature}s enabled
     *
     * @return Bitset of enabled {@link JsonFactory.Feature}s.
     *
     * @since 2.16
     */
    public abstract int getFactoryFeatures();

    /*
    /**********************************************************************
    /* Constraints violation checking (2.15)
    /**********************************************************************
     */

    /**
     * Get the constraints to apply when performing streaming reads.
     *
     * @return Constraints to apply to reads done by {@link JsonParser}s constructed
     *   by this factory.
     *
     * @since 2.15
     */
    public abstract StreamReadConstraints streamReadConstraints();

    /*
    /**********************************************************************
    /* Factory methods, parsers
    /**********************************************************************
     */

    public abstract JsonParser createParser(byte[] data) throws IOException;

    public abstract JsonParser createParser(InputStream in) throws IOException;

    public abstract JsonParser createParser(Reader r) throws IOException;

    public abstract JsonParser createParser(String content) throws IOException;

    /*
    /**********************************************************************
    /* Factory methods, generators
    /**********************************************************************
     */

    public abstract JsonGenerator createGenerator(OutputStream out) throws IOException;

    public abstract JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException;

    public abstract JsonGenerator createGenerator(Writer w) throws IOException;
}
