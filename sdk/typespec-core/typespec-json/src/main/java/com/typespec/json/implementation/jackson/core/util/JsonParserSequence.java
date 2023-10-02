// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

import java.io.IOException;
import java.util.*;

import com.typespec.json.implementation.jackson.core.*;

/**
 * Helper class that can be used to sequence multiple physical
 * {@link JsonParser}s to create a single logical sequence of
 * tokens, as a single {@link JsonParser}.
 *<p>
 * Fairly simple use of {@link JsonParserDelegate}: only need
 * to override {@link #nextToken} to handle transition
 */
public class JsonParserSequence extends JsonParserDelegate
{
    /**
     * Parsers other than the first one (which is initially assigned
     * as delegate)
     */
    protected final JsonParser[] _parsers;

    /**
     * Configuration that determines whether state of parsers is first verified
     * to see if parser already points to a token (that is,
     * {@link JsonParser#hasCurrentToken()} returns <code>true</code>), and if so
     * that token is first return before {@link JsonParser#nextToken} is called.
     * If enabled, this check is made; if disabled, no check is made and
     * {@link JsonParser#nextToken} is always called for all parsers.
     *<p>
     * Default setting is <code>false</code> (for backwards-compatibility)
     * so that possible existing token is not considered for parsers.
     * 
     * @since 2.8
     */
    protected final boolean _checkForExistingToken;

    /**
     * Index of the next parser in {@link #_parsers}.
     */
    protected int _nextParserIndex;

    /**
     * Flag used to indicate that `JsonParser.nextToken()` should not be called,
     * due to parser already pointing to a token.
     *
     * @since 2.8
     */
    protected boolean _hasToken;

    /*
     *******************************************************
     * Construction
     *******************************************************
     */

    @Deprecated // since 2.8
    protected JsonParserSequence(JsonParser[] parsers) {
        this(false, parsers);
    }

    // @since 2.8
    protected JsonParserSequence(boolean checkForExistingToken, JsonParser[] parsers)
    {
        super(parsers[0]);
        _checkForExistingToken = checkForExistingToken;
        _hasToken = checkForExistingToken && delegate.hasCurrentToken();
        _parsers = parsers;
        _nextParserIndex = 1;
    }

    /**
     * Method that will construct a sequence (possibly a sequence) that
     * contains all given sub-parsers.
     * All parsers given are checked to see if they are sequences: and
     * if so, they will be "flattened", that is, contained parsers are
     * directly added in a new sequence instead of adding sequences
     * within sequences. This is done to minimize delegation depth,
     * ideally only having just a single level of delegation.
     *
     * @param checkForExistingToken Flag passed to be assigned as
     *   {@link #_checkForExistingToken} for resulting sequence
     * @param first First parser to traverse
     * @param second Second parser to traverse
     *
     * @return Sequence instance constructed
     */
    public static JsonParserSequence createFlattened(boolean checkForExistingToken,
            JsonParser first, JsonParser second)
    {
        if (!(first instanceof JsonParserSequence || second instanceof JsonParserSequence)) {
            return new JsonParserSequence(checkForExistingToken,
                    new JsonParser[] { first, second });
        }
        ArrayList<JsonParser> p = new ArrayList<JsonParser>();
        if (first instanceof JsonParserSequence) {
            ((JsonParserSequence) first).addFlattenedActiveParsers(p);
        } else {
            p.add(first);
        }
        if (second instanceof JsonParserSequence) {
            ((JsonParserSequence) second).addFlattenedActiveParsers(p);
        } else {
            p.add(second);
        }
        return new JsonParserSequence(checkForExistingToken,
                p.toArray(new JsonParser[p.size()]));
    }

    @Deprecated // since 2.8
    public static JsonParserSequence createFlattened(JsonParser first, JsonParser second) {
        return createFlattened(false, first, second);
    }
    
    @SuppressWarnings("resource")
    protected void addFlattenedActiveParsers(List<JsonParser> listToAddIn)
    {
        for (int i = _nextParserIndex-1, len = _parsers.length; i < len; ++i) {
            JsonParser p = _parsers[i];
            if (p instanceof JsonParserSequence) {
                ((JsonParserSequence) p).addFlattenedActiveParsers(listToAddIn);
            } else {
                listToAddIn.add(p);
            }
        }
    }

    /*
    /*******************************************************
    /* Overridden methods, needed: cases where default
    /* delegation does not work
    /*******************************************************
     */

    @Override
    public void close() throws IOException {
        do { delegate.close(); } while (switchToNext());
    }

    @Override
    public JsonToken nextToken() throws IOException
    {
        if (delegate == null) {
            return null;
        }
        if (_hasToken) {
            _hasToken = false;
           return delegate.currentToken();
        }
        JsonToken t = delegate.nextToken();
        if (t == null) {
            return switchAndReturnNext();
        }
        return t;
    }

    /**
     * Need to override, re-implement similar to how method defined in
     * {@link com.typespec.json.implementation.jackson.core.base.ParserMinimalBase}, to keep
     * state correct here.
     */
    @Override
    public JsonParser skipChildren() throws IOException
    {
        if ((delegate.currentToken() != JsonToken.START_OBJECT)
            && (delegate.currentToken() != JsonToken.START_ARRAY)) {
            return this;
        }
        int open = 1;

        // Since proper matching of start/end markers is handled
        // by nextToken(), we'll just count nesting levels here
        while (true) {
            JsonToken t = nextToken();
            if (t == null) { // not ideal but for now, just return
                return this;
            }
            if (t.isStructStart()) {
                ++open;
            } else if (t.isStructEnd()) {
                if (--open == 0) {
                    return this;
                }
            }
        }
    }

    /*
    /*******************************************************
    /* Additional extended API
    /*******************************************************
     */

    /**
     * Method that is most useful for debugging or testing;
     * returns actual number of underlying parsers sequence
     * was constructed with (nor just ones remaining active)
     *
     * @return Number of actual underlying parsers this sequence has
     */
    public int containedParsersCount() {
        return _parsers.length;
    }

    /*
    /*******************************************************
    /* Helper methods
    /*******************************************************
     */

    /**
     * Method that will switch active delegate parser from the current one
     * to the next parser in sequence, if there is another parser left:
     * if so, the next parser will become the active delegate parser.
     * 
     * @return True if switch succeeded; false otherwise
     *
     * @since 2.8
     */
    protected boolean switchToNext()
    {
        if (_nextParserIndex < _parsers.length) {
            delegate = _parsers[_nextParserIndex++];
            return true;
        }
        return false;
    }

    protected JsonToken switchAndReturnNext() throws IOException
    {
        while (_nextParserIndex < _parsers.length) {
            delegate = _parsers[_nextParserIndex++];
            if (_checkForExistingToken && delegate.hasCurrentToken()) {
                return delegate.getCurrentToken();
            }
            JsonToken t = delegate.nextToken();
            if (t != null) {
                return t;
            }
        }
        return null;
    }
}
