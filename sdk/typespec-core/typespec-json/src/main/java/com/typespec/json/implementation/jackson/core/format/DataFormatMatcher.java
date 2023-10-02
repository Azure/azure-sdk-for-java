// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.format;

import java.io.*;

import com.typespec.json.implementation.jackson.core.JsonFactory;
import com.typespec.json.implementation.jackson.core.JsonParser;
import com.typespec.json.implementation.jackson.core.io.MergedStream;

/**
 * Result object constructed by {@link DataFormatDetector} when requested
 * to detect format of given input data.
 */
public class DataFormatMatcher
{
    protected final InputStream _originalStream;

    /**
     * Content read during format matching process
     */
    protected final byte[] _bufferedData;

    /**
     * Pointer to the first byte in buffer available for reading
     */
    protected final int _bufferedStart;
    
    /**
     * Number of bytes available in buffer.
     */
    protected final int _bufferedLength;

    /**
     * Factory that produced sufficient match (if any)
     */
    protected final JsonFactory _match;

    /**
     * Strength of match with {@link #_match}
     */
    protected final MatchStrength _matchStrength;
    
    protected DataFormatMatcher(InputStream in, byte[] buffered,
            int bufferedStart, int bufferedLength,
            JsonFactory match, MatchStrength strength)
    {
        _originalStream = in;
        _bufferedData = buffered;
        _bufferedStart = bufferedStart;
        _bufferedLength = bufferedLength;
        _match = match;
        _matchStrength = strength;

        // can't have negative offset or length
        if ((bufferedStart | bufferedLength) < 0
                || (bufferedStart + bufferedLength) > buffered.length) {
            throw new IllegalArgumentException(String.format("Illegal start/length (%d/%d) wrt input array of %d bytes",
                    bufferedStart, bufferedLength, buffered.length));
        }
    }

    /*
    /**********************************************************
    /* Public API, simple accessors
    /**********************************************************
     */

    /**
     * Accessor to use to see if any formats matched well enough with
     * the input data.
     *
     * @return Whether format has a match
     */
    public boolean hasMatch() { return _match != null; }

    /**
     * Method for accessing strength of the match, if any; if no match,
     * will return {@link MatchStrength#INCONCLUSIVE}.
     *
     * @return Strength of match
     */
    public MatchStrength getMatchStrength() {
        return (_matchStrength == null) ? MatchStrength.INCONCLUSIVE : _matchStrength;
    }

    /**
     * Accessor for {@link JsonFactory} that represents format that data matched.
     *
     * @return Relevant {@link JsonFactory} to indicate matched format
     */
    public JsonFactory getMatch() { return _match; }

    /**
     * Accessor for getting brief textual name of matched format if any (null
     * if none). Equivalent to:
     *<pre>
     *   return hasMatch() ? getMatch().getFormatName() : null;
     *</pre>
     *
     * @return Name of the format that is acceptable match, if any; {@code null} if none
     */
    public String getMatchedFormatName() {
        return hasMatch() ? getMatch().getFormatName() : null;
    }
    
    /*
    /**********************************************************
    /* Public API, factory methods
    /**********************************************************
     */
    
    // Convenience method for trying to construct a {@link JsonParser} for
    // parsing content which is assumed to be in detected data format.
    // If no match was found, returns null.
    public JsonParser createParserWithMatch() throws IOException {
        if (_match == null) {
            return null;
        }
        if (_originalStream == null) {
            return _match.createParser(_bufferedData, _bufferedStart, _bufferedLength);
        }
        return _match.createParser(getDataStream());
    }
    
    /**
     * Method to use for accessing input for which format detection has been done.
     * This <b>must</b> be used instead of using stream passed to detector
     * unless given stream itself can do buffering.
     * Stream will return all content that was read during matching process, as well
     * as remaining contents of the underlying stream.
     *
     * @return InputStream to use for reading actual content using format detected
     */
    public InputStream getDataStream() {
        if (_originalStream == null) {
            return new ByteArrayInputStream(_bufferedData, _bufferedStart, _bufferedLength);
        }
        return new MergedStream(null, _originalStream, _bufferedData, _bufferedStart, _bufferedLength);
    }
}
