// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.StreamWriteConstraints;
import com.azure.json.implementation.jackson.core.base.GeneratorBase;
import com.azure.json.implementation.jackson.core.io.CharTypes;
import com.azure.json.implementation.jackson.core.io.CharacterEscapes;
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

    /**
     * Definition of custom character escapes to use for generators created
     * by this factory, if any. If null, standard data format specific
     * escapes are used.
     */
    protected CharacterEscapes _characterEscapes;

    /*
    /**********************************************************
    /* Configuration, other
    /**********************************************************
     */

    /**
     * Flag that is set if quoting is not to be added around
     * JSON Object property names.
     *
     * @since 2.7
     */
    protected boolean _cfgUnqNames;

    /**
     * Write Hex values with uppercase letters
     *
     * @since 2.14
     */
    protected boolean _cfgWriteHexUppercase;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    @SuppressWarnings("deprecation")
    public JsonGeneratorImpl(IOContext ctxt, int features) {
        super(features, ctxt);
        _streamWriteConstraints = ctxt.streamWriteConstraints();
        if (Feature.ESCAPE_NON_ASCII.enabledIn(features)) {
            // inlined `setHighestNonEscapedChar()`
            _maximumNonEscapedChar = 127;
        }
        _cfgWriteHexUppercase = Feature.WRITE_HEX_UPPER_CASE.enabledIn(features);
        _cfgUnqNames = !Feature.QUOTE_FIELD_NAMES.enabledIn(features);
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
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = false;
        } else if (f == Feature.WRITE_HEX_UPPER_CASE) {
            _cfgWriteHexUppercase = true;
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public JsonGenerator disable(Feature f) {
        super.disable(f);
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = true;
        } else if (f == Feature.WRITE_HEX_UPPER_CASE) {
            _cfgWriteHexUppercase = false;
        }
        return this;
    }

    @Override
    public void setHighestNonEscapedChar(int charCode) {
        _maximumNonEscapedChar = Math.max(charCode, 0);
    }

    @Override
    public void setCharacterEscapes(CharacterEscapes esc) {
        _characterEscapes = esc;
        if (esc == null) { // revert to standard escapes
            _outputEscapes = sOutputEscapes;
        } else {
            _outputEscapes = esc.getEscapeCodesForAscii();
        }
    }

    protected void _reportCantWriteValueExpectName(String typeMsg) throws IOException {
        _reportError(
            String.format("Can not %s, expecting field name (context: %s)", typeMsg, _writeContext.typeDesc()));
    }
}
