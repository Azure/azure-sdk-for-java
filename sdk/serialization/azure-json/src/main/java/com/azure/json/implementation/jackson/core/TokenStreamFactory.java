// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */
package com.azure.json.implementation.jackson.core;

import java.io.*;
import java.net.URL;

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

    /*
    /**********************************************************************
    /* Format detection functionality
    /**********************************************************************
     */

    /**
     * Method that returns short textual id identifying format
     * this factory supports.
     *
     * @return Name of the format handled by parsers, generators this factory creates
     */
    public abstract String getFormatName();

    /*
    /**********************************************************************
    /* Configuration access
    /**********************************************************************
     */

    public abstract boolean isEnabled(JsonFactory.Feature f);

    public abstract boolean isEnabled(StreamReadFeature f);

    public abstract boolean isEnabled(StreamWriteFeature f);

    public abstract boolean isEnabled(JsonParser.Feature f);

    public abstract boolean isEnabled(JsonGenerator.Feature f);

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

    public abstract JsonParser createParser(byte[] data, int offset, int len) throws IOException;

    public abstract JsonParser createParser(File f) throws IOException;

    public abstract JsonParser createParser(InputStream in) throws IOException;

    public abstract JsonParser createParser(Reader r) throws IOException;

    public abstract JsonParser createParser(String content) throws IOException;

    public abstract JsonParser createParser(URL url) throws IOException;

    /*
    /**********************************************************************
    /* Factory methods, generators
    /**********************************************************************
     */

    public abstract JsonGenerator createGenerator(OutputStream out) throws IOException;

    public abstract JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException;

    public abstract JsonGenerator createGenerator(Writer w) throws IOException;

    /*
    /**********************************************************************
    /* Internal factory methods, other
    /**********************************************************************
     */

    /**
     * Helper method used for constructing an optimal stream for
     * parsers to use, when input is to be read from an URL.
     * This helps when reading file content via URL.
     *
     * @param url Source to read content to parse from
     *
     * @return InputStream constructed for given {@link URL}
     *
     * @throws IOException If there is a problem accessing content from specified {@link URL}
     */
    protected InputStream _optimizedStreamFromURL(URL url) throws IOException {
        if ("file".equals(url.getProtocol())) {
            /* Can not do this if the path refers
             * to a network drive on windows. This fixes the problem;
             * might not be needed on all platforms (NFS?), but should not
             * matter a lot: performance penalty of extra wrapping is more
             * relevant when accessing local file system.
             */
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                // [core#48]: Let's try to avoid probs with URL encoded stuff
                String path = url.getPath();
                if (path.indexOf('%') < 0) {
                    return new FileInputStream(url.getPath());

                }
                // otherwise, let's fall through and let URL decoder do its magic
            }
        }
        return url.openStream();
    }

    /**
     * Helper methods used for constructing an {@link InputStream} for
     * parsers to use, when input is to be read from given {@link File}.
     *
     * @param f File to open stream for
     *
     * @return {@link InputStream} constructed
     *
     * @throws IOException If there is a problem opening the stream
     *
     * @since 2.14
     */
    protected InputStream _fileInputStream(File f) throws IOException {
        return new FileInputStream(f);
    }

    /*
    /**********************************************************************
    /* Range check helper methods (2.14)
    /**********************************************************************
     */

    // @since 2.14
    protected void _checkRangeBoundsForByteArray(byte[] data, int offset, int len) throws IllegalArgumentException {
        if (data == null) {
            _reportRangeError("Invalid `byte[]` argument: `null`");
        }
        final int dataLen = data.length;
        final int end = offset + len;

        // Note: we are checking that:
        //
        // !(offset < 0)
        // !(len < 0)
        // !((offset + len) < 0) // int overflow!
        // !((offset + len) > dataLen) == !((datalen - (offset+len)) < 0)

        // All can be optimized by OR'ing and checking for negative:
        int anyNegs = offset | len | end | (dataLen - end);
        if (anyNegs < 0) {
            _reportRangeError(String.format(
                "Invalid 'offset' (%d) and/or 'len' (%d) arguments for `byte[]` of length %d", offset, len, dataLen));
        }
    }

    protected <T> T _reportRangeError(String msg) throws IllegalArgumentException {
        throw new IllegalArgumentException(msg);
    }
}
