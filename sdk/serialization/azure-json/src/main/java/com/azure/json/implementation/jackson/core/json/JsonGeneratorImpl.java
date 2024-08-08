// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.StreamWriteConstraints;
import com.azure.json.implementation.jackson.core.base.GeneratorBase;
import com.azure.json.implementation.jackson.core.io.CharTypes;
import com.azure.json.implementation.jackson.core.io.IOContext;

import java.io.IOException;

/**
 * Intermediate base class shared by JSON-backed generators
 * like {@link UTF8JsonGenerator} and {@link WriterBasedJsonGenerator}.
 *
 * @since 2.1
 */
public abstract class JsonGeneratorImpl extends GeneratorBase {
    /*
    /**********************************************************
    /* Constants
    /**********************************************************
     */

    /**
     * This is the default set of escape codes, over 7-bit ASCII range
     * (first 128 character codes), used for single-byte UTF-8 characters.
     */
    protected final static int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();

    /*
    /**********************************************************
    /* Configuration, basic I/O
    /**********************************************************
     */

    /**
     * @since 2.16
     */
    protected final StreamWriteConstraints _streamWriteConstraints;

    /*
    /**********************************************************
    /* Configuration, output escaping
    /**********************************************************
     */

    /**
     * Currently active set of output escape code definitions (whether
     * and how to escape or not) for 7-bit ASCII range (first 128
     * character codes). Defined separately to make potentially
     * customizable
     */
    protected int[] _outputEscapes = sOutputEscapes;

    /**
     * Value between 128 (0x80) and 65535 (0xFFFF) that indicates highest
     * Unicode code point that will not need escaping; or 0 to indicate
     * that all characters can be represented without escaping.
     * Typically used to force escaping of some portion of character set;
     * for example to always escape non-ASCII characters (if value was 127).
     *<p>
     * NOTE: not all sub-classes make use of this setting.
     */
    protected int _maximumNonEscapedChar;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    @SuppressWarnings("deprecation")
    public JsonGeneratorImpl(IOContext ctxt, int features) {
        super(features, ctxt);
        _streamWriteConstraints = ctxt.streamWriteConstraints();
    }

    /*
    /**********************************************************************
    /* Constraints violation checking (2.16)
    /**********************************************************************
     */

    @Override
    public StreamWriteConstraints streamWriteConstraints() {
        return _streamWriteConstraints;
    }

    /*
    /**********************************************************
    /* Overridden configuration methods
    /**********************************************************
     */

    @SuppressWarnings("deprecation")
    @Override
    public JsonGenerator enable(Feature f) {
        super.enable(f);
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public JsonGenerator disable(Feature f) {
        super.disable(f);
        return this;
    }

    protected void _reportCantWriteValueExpectName(String typeMsg) throws IOException {
        _reportError(
            String.format("Can not %s, expecting field name (context: %s)", typeMsg, _writeContext.typeDesc()));
    }
}
