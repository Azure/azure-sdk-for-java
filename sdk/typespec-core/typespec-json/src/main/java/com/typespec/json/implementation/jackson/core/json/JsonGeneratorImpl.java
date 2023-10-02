// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.json;

import java.io.IOException;

import com.typespec.json.implementation.jackson.core.*;
import com.typespec.json.implementation.jackson.core.base.GeneratorBase;
import com.typespec.json.implementation.jackson.core.io.CharTypes;
import com.typespec.json.implementation.jackson.core.io.CharacterEscapes;
import com.typespec.json.implementation.jackson.core.io.IOContext;
import com.typespec.json.implementation.jackson.core.util.DefaultPrettyPrinter;
import com.typespec.json.implementation.jackson.core.util.JacksonFeatureSet;
import com.typespec.json.implementation.jackson.core.util.VersionUtil;

/**
 * Intermediate base class shared by JSON-backed generators
 * like {@link UTF8JsonGenerator} and {@link WriterBasedJsonGenerator}.
 * 
 * @since 2.1
 */
public abstract class JsonGeneratorImpl extends GeneratorBase
{
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

    /**
     * Default capabilities for JSON generator implementations which do not
     * different from "general textual" defaults
     *
     * @since 2.12
     */
    protected final static JacksonFeatureSet<StreamWriteCapability> JSON_WRITE_CAPABILITIES
        = DEFAULT_TEXTUAL_WRITE_CAPABILITIES;

    /*
    /**********************************************************
    /* Configuration, basic I/O
    /**********************************************************
     */

    protected final IOContext _ioContext;

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
     * Separator to use, if any, between root-level values.
     * 
     * @since 2.1
     */
    protected SerializableString _rootValueSeparator
        = DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR;

    /**
     * Flag that is set if quoting is not to be added around
     * JSON Object property names.
     *
     * @since 2.7
     */
    protected boolean _cfgUnqNames;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    @SuppressWarnings("deprecation")
    public JsonGeneratorImpl(IOContext ctxt, int features, ObjectCodec codec)
    {
        super(features, codec);
        _ioContext = ctxt;
        if (Feature.ESCAPE_NON_ASCII.enabledIn(features)) {
            // inlined `setHighestNonEscapedChar()`
            _maximumNonEscapedChar = 127;
        }
        _cfgUnqNames = !Feature.QUOTE_FIELD_NAMES.enabledIn(features);
    }

    /*
    /**********************************************************
    /* Versioned
    /**********************************************************
     */

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
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
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public JsonGenerator disable(Feature f) {
        super.disable(f);
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = true;
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void _checkStdFeatureChanges(int newFeatureFlags, int changedFeatures) {
        super._checkStdFeatureChanges(newFeatureFlags, changedFeatures);
        _cfgUnqNames = !Feature.QUOTE_FIELD_NAMES.enabledIn(newFeatureFlags);
    }

    @Override
    public JsonGenerator setHighestNonEscapedChar(int charCode) {
        _maximumNonEscapedChar = (charCode < 0) ? 0 : charCode;
        return this;
    }

    @Override
    public int getHighestEscapedChar() {
        return _maximumNonEscapedChar;
    }

    @Override
    public JsonGenerator setCharacterEscapes(CharacterEscapes esc)
    {
        _characterEscapes = esc;
        if (esc == null) { // revert to standard escapes
            _outputEscapes = sOutputEscapes;
        } else {
            _outputEscapes = esc.getEscapeCodesForAscii();
        }
        return this;
    }

    /**
     * Method for accessing custom escapes factory uses for {@link JsonGenerator}s
     * it creates.
     */
    @Override
    public CharacterEscapes getCharacterEscapes() {
        return _characterEscapes;
    }
    
    @Override
    public JsonGenerator setRootValueSeparator(SerializableString sep) {
        _rootValueSeparator = sep;
        return this;
    }

    @Override
    public JacksonFeatureSet<StreamWriteCapability> getWriteCapabilities() {
        return JSON_WRITE_CAPABILITIES;
    }

    /*
    /**********************************************************
    /* Shared helper methods
    /**********************************************************
     */

    protected void _verifyPrettyValueWrite(String typeMsg, int status) throws IOException
    {
        // If we have a pretty printer, it knows what to do:
        switch (status) {
        case JsonWriteContext.STATUS_OK_AFTER_COMMA: // array
            _cfgPrettyPrinter.writeArrayValueSeparator(this);
            break;
        case JsonWriteContext.STATUS_OK_AFTER_COLON:
            _cfgPrettyPrinter.writeObjectFieldValueSeparator(this);
            break;
        case JsonWriteContext.STATUS_OK_AFTER_SPACE:
            _cfgPrettyPrinter.writeRootValueSeparator(this);
            break;
        case JsonWriteContext.STATUS_OK_AS_IS:
            // First entry, but of which context?
            if (_writeContext.inArray()) {
                _cfgPrettyPrinter.beforeArrayValues(this);
            } else if (_writeContext.inObject()) {
                _cfgPrettyPrinter.beforeObjectEntries(this);
            }
            break;
        case JsonWriteContext.STATUS_EXPECT_NAME:
            _reportCantWriteValueExpectName(typeMsg);
            break;
        default:
            _throwInternal();
            break;
        }
    }

    protected void _reportCantWriteValueExpectName(String typeMsg) throws IOException
    {
        _reportError(String.format("Can not %s, expecting field name (context: %s)",
                typeMsg, _writeContext.typeDesc()));
    }
}
