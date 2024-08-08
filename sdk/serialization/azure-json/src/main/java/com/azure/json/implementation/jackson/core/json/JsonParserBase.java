// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.*;
import com.azure.json.implementation.jackson.core.base.ParserBase;
import com.azure.json.implementation.jackson.core.io.CharTypes;
import com.azure.json.implementation.jackson.core.io.IOContext;

/**
 * Another intermediate base class, only used by actual JSON-backed parser
 * implementations.
 *
 * @since 2.17
 */
public abstract class JsonParserBase extends ParserBase {
    @SuppressWarnings("deprecation")
    protected final static int FEAT_MASK_TRAILING_COMMA = Feature.ALLOW_TRAILING_COMMA.getMask();
    @SuppressWarnings("deprecation")
    protected final static int FEAT_MASK_LEADING_ZEROS = Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
    @SuppressWarnings("deprecation")
    protected final static int FEAT_MASK_NON_NUM_NUMBERS = Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
    @SuppressWarnings("deprecation")
    protected final static int FEAT_MASK_ALLOW_MISSING = Feature.ALLOW_MISSING_VALUES.getMask();
    protected final static int FEAT_MASK_ALLOW_SINGLE_QUOTES = Feature.ALLOW_SINGLE_QUOTES.getMask();
    protected final static int FEAT_MASK_ALLOW_UNQUOTED_NAMES = Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
    protected final static int FEAT_MASK_ALLOW_JAVA_COMMENTS = Feature.ALLOW_COMMENTS.getMask();
    protected final static int FEAT_MASK_ALLOW_YAML_COMMENTS = Feature.ALLOW_YAML_COMMENTS.getMask();

    // Latin1 encoding is not supported, but we do use 8-bit subset for
    // pre-processing task, to simplify first pass, keep it fast.
    protected final static int[] INPUT_CODES_LATIN1 = CharTypes.getInputCodeLatin1();

    // This is the main input-code lookup table, fetched eagerly
    protected final static int[] INPUT_CODES_UTF8 = CharTypes.getInputCodeUtf8();

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    protected JsonParserBase(IOContext ioCtxt, int features) {
        super(ioCtxt, features);
    }

    /*
    /**********************************************************************
    /* Location handling
    /**********************************************************************
     */

    // First: override some methods as abstract to force definition by subclasses

    @Override
    public abstract JsonLocation currentLocation();

    @Override
    public abstract JsonLocation currentTokenLocation();

    @Override
    protected abstract JsonLocation _currentLocationMinusOne();
}
