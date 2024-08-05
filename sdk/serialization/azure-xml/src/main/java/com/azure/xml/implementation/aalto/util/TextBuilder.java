// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import com.azure.xml.implementation.stax2.typed.Base64Variant;
import com.azure.xml.implementation.stax2.typed.TypedArrayDecoder;
import com.azure.xml.implementation.stax2.typed.TypedXMLStreamException;

import com.azure.xml.implementation.stax2.ri.typed.CharArrayBase64Decoder;

import com.azure.xml.implementation.aalto.in.ReaderConfig;

/**
 * Class conceptually similar to {@link java.lang.StringBuilder}, but
 * that allows for bit more efficient building, using segmented internal
 * buffers, and direct access to these buffers.
 */
public final class TextBuilder {
    final static char[] sNoChars = new char[0];

    /**
     * Size of the first text segment buffer to allocate. Need not contain
     * the biggest segment, since new ones will get allocated as needed.
     * However, it's sensible to use something that often is big enough
     * to contain typical segments.
     */
    final static int DEF_INITIAL_BUFFER_SIZE = 500; // 1k

    final static int MAX_SEGMENT_LENGTH = 256 * 1024;

    final static int INT_SPACE = 0x0020;

    // // // Configuration:

    private final ReaderConfig _config;

    // // // Internal non-shared collector buffers:

    /**
     * List of segments prior to currently active segment.
     */
    private ArrayList<char[]> _segments;

    // // // Currently used segment; not (yet) contained in _segments

    /**
     * Amount of characters in segments in {@code _segments}
     */
    private int _segmentSize;

    private char[] _currentSegment;

    /**
     * Number of characters in currently active (last) segment
     */
    private int _currentSize;

    // // // Temporary caching for Objects to return

    /**
     * String that will be constructed when the whole contents are
     * needed; will be temporarily stored in case asked for again.
     */
    private String _resultString;

    private char[] _resultArray;

    /**
     * Indicator for length of data with <code>_resultArray</code>, iff
     * the primary indicator (_currentSize) is invalid (-1).
     */
    private int _resultLen;

    /*
    /**********************************************************************
    /* Support for decoding, for Typed Access API
    /**********************************************************************
     */

    private char[] _decodeBuffer;

    private int _decodePtr;

    private int _decodeEnd;

    /*
    /**********************************************************************
    /* Support for optimizating indentation segments:
    /**********************************************************************
     */

    /**
     * Marker to know if the contents currently stored were created
     * using "indentation detection". If so, it's known to be all
     * white space
     */
    private boolean _isIndentation = false;

    // // // Canonical indentation objects (up to 32 spaces, 8 tabs)

    public final static int MAX_INDENT_SPACES = 32;
    public final static int MAX_INDENT_TABS = 8;

    // Let's add one more space at the end, for safety...
    private final static String sIndSpaces =
    // 123456789012345678901234567890123
        "\n                                 ";
    private final static char[] sIndSpacesArray = sIndSpaces.toCharArray();
    private final static String[] sIndSpacesStrings = new String[sIndSpacesArray.length];

    private final static String sIndTabs =
    // 1 2 3 4 5 6 7 8 9
        "\n\t\t\t\t\t\t\t\t\t";
    private final static char[] sIndTabsArray = sIndTabs.toCharArray();
    private final static String[] sIndTabsStrings = new String[sIndTabsArray.length];

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private TextBuilder(ReaderConfig cfg) {
        _config = cfg;
    }

    public static TextBuilder createRecyclableBuffer(ReaderConfig cfg) {
        return new TextBuilder(cfg);
    }

    /**
     * Method called to indicate that the underlying buffers should now
     * be recycled if they haven't yet been recycled. Although caller
     * can still use this text buffer, it is not advisable to call this
     * method if that is likely, since next time a buffer is needed,
     * buffers need to reallocated.
     * Note: calling this method automatically also clears contents
     * of the buffer.
     */
    public void recycle(boolean force) {
        if (_config != null && _currentSegment != null) {
            if (force) {
                /* shouldn't call resetWithEmpty, as that would allocate
                 * initial buffer; but need to inline
                 */
                _resultString = null;
                _resultArray = null;
            } else {
                /* But if there's non-shared data (ie. buffer is still
                 * in use), can't return it yet:
                 */
                if ((_segmentSize + _currentSize) > 0) {
                    return;
                }
            }
            // If no data (or only shared data), can continue
            if (_segments != null && !_segments.isEmpty()) {
                // No need to use anything from list, curr segment not null
                _segments.clear();
                _segmentSize = 0;
            }
            char[] buf = _currentSegment;
            _currentSegment = null;
            _config.freeMediumCBuffer(buf);
        }
    }

    /**
     * Method called to clear out any content text buffer may have, and
     * initializes and returns the first segment to add characters to.
     */
    public char[] resetWithEmpty() {
        _resultString = null;
        _resultArray = null;
        _isIndentation = false;

        // And then reset internal input buffers, if necessary:
        if (_segments != null && !_segments.isEmpty()) {
            /* Since the current segment should be the biggest one
             * (as we allocate 50% bigger each time), let's retain it,
             * and clear others
             */
            _segments.clear();
            _segmentSize = 0;
        }
        _currentSize = 0;
        if (_currentSegment == null) {
            _currentSegment = allocBuffer(0);
        }
        return _currentSegment;
    }

    public void resetWithIndentation(int indCharCount, char indChar) {
        // First reset internal input buffers, if necessary:
        if (_segments != null && !_segments.isEmpty()) {
            _segments.clear();
            _segmentSize = 0;
        }
        _currentSize = -1;
        _isIndentation = true;

        String text;
        int strlen = indCharCount + 1;
        _resultLen = strlen;
        if (indChar == '\t') { // tabs?
            _resultArray = sIndTabsArray;
            text = sIndTabsStrings[indCharCount];
            if (text == null) {
                sIndTabsStrings[indCharCount] = text = sIndTabs.substring(0, strlen);
            }
        } else { // nope, spaces (should assert indChar?)
            _resultArray = sIndSpacesArray;
            text = sIndSpacesStrings[indCharCount];
            if (text == null) {
                sIndSpacesStrings[indCharCount] = text = sIndSpaces.substring(0, strlen);
            }
        }
        _resultString = text;
    }

    /**
     * Method called to initialize the buffer with just a single char
     */
    public void resetWithChar(char c) {
        _resultString = null;
        _resultArray = null;
        _isIndentation = false;

        // And then reset internal input buffers, if necessary:
        if (_segments != null && !_segments.isEmpty()) {
            _segments.clear();
            _segmentSize = 0;
        }
        _currentSize = 1;
        if (_currentSegment == null) {
            _currentSegment = allocBuffer(1);
        }
        _currentSegment[0] = c;
    }

    public void resetWithSurrogate(int c) {
        _resultString = null;
        _resultArray = null;
        _isIndentation = false;

        // And then reset internal input buffers, if necessary:
        if (_segments != null && !_segments.isEmpty()) {
            _segments.clear();
            _segmentSize = 0;
        }
        _currentSize = 2;
        if (_currentSegment == null) {
            _currentSegment = allocBuffer(2);
        }
        _currentSegment[0] = (char) (0xD800 | (c >> 10));
        _currentSegment[1] = (char) (0xDC00 | (c & 0x3FF));
    }

    public char[] getBufferWithoutReset() {
        return _currentSegment;
    }

    /*
    /**********************************************************************
    /* Accessors for implementing StAX interface:
    /**********************************************************************
     */

    /**
     * @return Number of characters currently stored by this collector
     */
    public int size() {
        int size = _currentSize;

        // Will be -1 only if we have shared white space
        if (size < 0) {
            return _resultLen;
        }
        return size + _segmentSize;
    }

    public char[] getTextBuffer() {
        // Does it fit in just one segment?
        if (_segments == null || _segments.isEmpty()) {
            // But is it whitespace, actually?
            if (_resultArray != null) {
                return _resultArray;
            }
            return _currentSegment;
        }
        // Nope, need to have/create a non-segmented array and return it
        return contentsAsArray();
    }

    /*
    /**********************************************************************
    /* Accessors for text contained
    /**********************************************************************
     */

    public String contentsAsString() {
        if (_resultString == null) {
            // Has array been requested? Can make a shortcut, if so:
            if (_resultArray != null) {
                _resultString = new String(_resultArray);
            } else {
                // Let's optimize common case: nothing in extra segments:
                int segLen = _segmentSize;
                int currLen = _currentSize;

                if (segLen == 0) {
                    _resultString = (currLen == 0) ? "" : new String(_currentSegment, 0, currLen);
                    return _resultString;
                }

                // Nope, need to combine:
                StringBuilder sb = new StringBuilder(segLen + currLen);
                // First stored segments
                if (_segments != null) {
                    for (char[] segment : _segments) {
                        sb.append(segment, 0, segment.length);
                    }
                }
                // And finally, current segment:
                sb.append(_currentSegment, 0, currLen);
                _resultString = sb.toString();
            }
        }
        return _resultString;
    }

    public char[] contentsAsArray() {
        char[] result = _resultArray;
        if (result == null) {
            _resultArray = result = buildResultArray();
        }
        return result;
    }

    public int contentsToArray(int srcStart, char[] dst, int dstStart, int len) {
        /* Could also check if we have array, but that'd only help with
         * brain dead clients that get full array first, then segments...
         * which hopefully aren't that common
         */
        // Copying from segmented array is bit more involved:
        int totalAmount = 0;
        if (_segments != null) {
            for (char[] chars : _segments) {
                int segLen = chars.length;
                int amount = segLen - srcStart;
                if (amount < 1) { // nothing from this segment?
                    srcStart -= segLen;
                    continue;
                }
                if (amount >= len) { // can get rest from this segment?
                    System.arraycopy(chars, srcStart, dst, dstStart, len);
                    return (totalAmount + len);
                }
                // Can get some from this segment, offset becomes zero:
                System.arraycopy(chars, srcStart, dst, dstStart, amount);
                totalAmount += amount;
                dstStart += amount;
                len -= amount;
                srcStart = 0;
            }
        }

        // Need to copy anything from last segment?
        if (len > 0) {
            int maxAmount = _currentSize - srcStart;
            if (len > maxAmount) {
                len = maxAmount;
            }
            if (len > 0) { // should always be true
                System.arraycopy(_currentSegment, srcStart, dst, dstStart, len);
                totalAmount += len;
            }
        }

        return totalAmount;
    }

    /**
     * Method that will stream contents of this buffer into specified
     * Writer.
     */
    public int rawContentsTo(Writer w) throws IOException {
        // Let's first see if we have created helper objects:
        if (_resultArray != null) {
            w.write(_resultArray);
            return _resultArray.length;
        }
        if (_resultString != null) {
            w.write(_resultString);
            return _resultString.length();
        }

        // Nope, need to do full segmented output
        int rlen = 0;
        if (_segments != null) {
            for (char[] segment : _segments) {
                w.write(segment);
                rlen += segment.length;
            }
        }
        if (_currentSize > 0) {
            w.write(_currentSegment, 0, _currentSize);
            rlen += _currentSize;
        }
        return rlen;
    }

    public boolean isAllWhitespace() {
        if (_isIndentation) {
            return true;
        }
        // Need to do full segmented output, otherwise
        if (_segments != null) {
            for (char[] segment : _segments) {
                for (char c : segment) {
                    if (c > 0x0020) {
                        return false;
                    }
                }
            }
        }

        char[] buf = _currentSegment;
        for (int i = 0, len = _currentSize; i < len; ++i) {
            if (buf[i] > 0x0020) {
                return false;
            }
        }
        return true;
    }

    /*
     * Method that can be used to check if the contents of the buffer end
     * in specified String.
     *
     * @return True if the textual content buffer contains ends with the
     *   specified String; false otherwise
    public boolean endsWith(String str)
    {
        int segIndex = (_segments == null) ? 0 : _segments.size();
        int inIndex = str.length() - 1;
        char[] buf = _currentSegment;
        int bufIndex = _currentSize-1;
    
        while (inIndex >= 0) {
            if (str.charAt(inIndex) != buf[bufIndex]) {
                return false;
            }
            if (--inIndex == 0) {
                break;
            }
            if (--bufIndex < 0) {
                if (--segIndex < 0) { // no more data?
                    return false;
                }
                buf = (char[]) _segments.get(segIndex);
                bufIndex = buf.length-1;
            }
        }
        return true;
    }
    */

    /**
     * Note: it is assumed that this method is not used often enough to
     * be a bottleneck, or for long segments. Based on this, it is optimized
     * for common simple cases where there is only one single character
     * segment to use; fallback for other cases is to create such segment.
     */
    public boolean equalsString(String str) {
        int expLen = str.length();

        // Otherwise, segments:
        if (expLen != size()) {
            return false;
        }
        char[] seg;
        if (_segments == null || _segments.isEmpty()) {
            // just one segment, still easy
            seg = _currentSegment;
        } else {
            /* Ok; this is the sub-optimal case. Could obviously juggle through
             * segments, but probably not worth the hassle, we seldom if ever
             * get here...
             */
            seg = contentsAsArray();
        }

        for (int i = 0; i < expLen; ++i) {
            if (seg[i] != str.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /*
    /**********************************************************************
    /* Methods for generating SAX events
    /**********************************************************************
     */

    /**
     * This is a specialized "accessor" method, which is basically
     * to fire SAX characters() events in an optimal way, based on
     * which internal buffers are being used
     */
    public void fireSaxCharacterEvents(ContentHandler h) throws SAXException {
        if (_resultArray != null) { // only happens for indentation
            h.characters(_resultArray, 0, _resultLen);
        } else {
            if (_segments != null) {
                for (char[] segment : _segments) {
                    h.characters(segment, 0, segment.length);
                }
            }
            if (_currentSize > 0) {
                h.characters(_currentSegment, 0, _currentSize);
            }
        }
    }

    public void fireSaxSpaceEvents(ContentHandler h) throws SAXException {
        if (_resultArray != null) { // only happens for indentation
            h.ignorableWhitespace(_resultArray, 0, _resultLen);
        } else {
            if (_segments != null) {
                for (char[] segment : _segments) {
                    h.ignorableWhitespace(segment, 0, segment.length);
                }
            }
            if (_currentSize > 0) {
                h.ignorableWhitespace(_currentSegment, 0, _currentSize);
            }
        }
    }

    public void fireSaxCommentEvent(LexicalHandler h) throws SAXException {
        // Comment can not be split, so may need to combine the array
        if (_resultArray != null) { // only happens for indentation
            h.comment(_resultArray, 0, _resultLen);
        } else if (_segments != null && !_segments.isEmpty()) {
            char[] ch = contentsAsArray();
            h.comment(ch, 0, ch.length);
        } else {
            h.comment(_currentSegment, 0, _currentSize);
        }
    }

    /*
    /**********************************************************************
    /* Support for validation
    /**********************************************************************
     */

    /*
    public void validateText(XMLValidator vld, boolean lastSegment)
        throws XMLValidationException
    {
        // Can either create a combine buffer, or construct
        // a String. While former could be more efficient, let's do latter
        // for now since current validator implementations work better
        // with Strings.
        vld.validateText(contentsAsString(), lastSegment);
    }
    */

    /*
    /**********************************************************************
    /* Public mutators:
    /**********************************************************************
     */

    public void append(char c) {
        _resultString = null;
        _resultArray = null;
        // Room in current segment?
        char[] curr = _currentSegment;
        if (_currentSize >= curr.length) {
            expand(1);
        }
        curr[_currentSize++] = c;
    }

    public void appendSurrogate(int surr) {
        append((char) (0xD800 | (surr >> 10)));
        append((char) (0xDC00 | (surr & 0x3FF)));
    }

    public void append(char[] c, int start, int len) {
        _resultString = null;
        _resultArray = null;

        // Room in current segment?
        char[] curr = _currentSegment;
        int max = curr.length - _currentSize;

        if (max >= len) {
            System.arraycopy(c, start, curr, _currentSize, len);
            _currentSize += len;
        } else {
            // No room for all, need to copy part(s):
            if (max > 0) {
                System.arraycopy(c, start, curr, _currentSize, max);
                start += max;
                len -= max;
            }
            /* And then allocate new segment; we are guaranteed to now
             * have enough room in segment.
             */
            expand(len); // note: curr != _currentSegment after this
            System.arraycopy(c, start, _currentSegment, 0, len);
            _currentSize = len;
        }
    }

    public void append(String str) {
        _resultString = null;
        _resultArray = null;

        int len = str.length();
        // Room in current segment?
        char[] curr = _currentSegment;
        int max = curr.length - _currentSize;
        if (max >= len) {
            str.getChars(0, len, curr, _currentSize);
            _currentSize += len;
        } else {
            // No room for all, need to copy part(s):
            if (max > 0) {
                str.getChars(0, max, curr, _currentSize);
                len -= max;
            }
            /* And then allocate new segment; we are guaranteed to now
             * have enough room in segment.
             */
            expand(len);
            str.getChars(max, max + len, _currentSegment, 0);
            _currentSize = len;
        }
    }

    /*
    /**********************************************************************
    /* Raw access, for high-performance use:
    /**********************************************************************
     */

    public int getCurrentLength() {
        return _currentSize;
    }

    public void setCurrentLength(int len) {
        _currentSize = len;
    }

    public char[] finishCurrentSegment() {
        if (_segments == null) {
            _segments = new ArrayList<>();
        }
        _segments.add(_currentSegment);
        int oldLen = _currentSegment.length;
        _segmentSize += oldLen;
        char[] curr = new char[calcNewSize(oldLen)];
        _currentSize = 0;
        _currentSegment = curr;
        return curr;
    }

    private int calcNewSize(int latestSize) {
        // Let's grow segments by 50%, when over 8k
        int incr = (latestSize < 8000) ? latestSize : (latestSize >> 1);
        int size = latestSize + incr;
        // but let's not create too big chunks
        return Math.min(size, MAX_SEGMENT_LENGTH);
    }

    /*
    /**********************************************************************
    /* Methods for implementing Typed Access API
    /**********************************************************************
     */

    /**
     * Method called by the stream reader to decode space-separated tokens
     * that are part of the current text event (contents of which
     * are stored within this buffer), using given decoder.
     */
    public int decodeElements(TypedArrayDecoder tad, boolean reset) throws TypedXMLStreamException {
        if (reset) {
            resetForDecode();
        }

        int ptr = _decodePtr;
        final char[] buf = _decodeBuffer;

        int count = 0;

        // And then let's decode
        int start = ptr;

        try {
            final int end = _decodeEnd;

            decode_loop: while (ptr < end) {
                // First, any space to skip?
                while (buf[ptr] <= INT_SPACE) {
                    if (++ptr >= end) {
                        break decode_loop;
                    }
                }
                // Then let's figure out non-space char (token)
                start = ptr;
                ++ptr;
                while (ptr < end && buf[ptr] > INT_SPACE) {
                    ++ptr;
                }
                ++count;
                int tokenEnd = ptr;
                ++ptr; // to skip trailing space (or, beyond end)
                // And there we have it
                if (tad.decodeValue(buf, start, tokenEnd)) {
                    break;
                }
                _decodePtr = ptr;
            }
            _decodePtr = ptr;
        } catch (IllegalArgumentException iae) {
            // Need to convert to a checked stream exception to return lexical
            // -1 to move it back after being advanced earlier (to skip trailing space)
            String lexical = new String(buf, start, (ptr - start - 1));
            throw new TypedXMLStreamException(lexical, iae.getMessage(), iae);
        }
        return count;
    }

    /**
     * Method called to initialize given base64 decoder with data
     * contained in this text buffer (for the current event).
     */
    public void resetForBinaryDecode(Base64Variant v, CharArrayBase64Decoder dec, boolean firstChunk) {
        // just one special case, indentation...
        if (_segments == null || _segments.isEmpty()) { // single segment
            if (_isIndentation) { // but special one, indent/ws
                dec.init(v, firstChunk, _resultArray, 0, _resultArray.length, null);
                return;
            }
        }
        dec.init(v, firstChunk, _currentSegment, 0, _currentSize, _segments);
    }

    private void resetForDecode() {
        /* This is very similar to getTextBuffer(), except
         * for assignment to _decodeXxx fields
         */
        _decodePtr = 0;
        if (_segments == null || _segments.isEmpty()) { // single segment
            if (_isIndentation) { // but special one, indent/ws
                _decodeBuffer = _resultArray;
                _decodeEnd = _resultArray.length;
            } else { // nope, just a regular buffer
                _decodeBuffer = _currentSegment;
                _decodeEnd = _currentSize;
            }
        } else {
            // Nope, need to have/create a non-segmented array and return it
            _decodeBuffer = contentsAsArray();
            _decodeEnd = _decodeBuffer.length;
        }
    }

    /*
    /**********************************************************************
    /* Standard methods:
    /**********************************************************************
     */

    /**
     * Note: calling this method may not be as efficient as calling
     * {@link #contentsAsString}, since it is guaranteed that resulting
     * String is NOT cached (to ensure we see no stale data)
     */
    @Override
    public String toString() {
        _resultString = null;
        _resultArray = null;
        return contentsAsString();
    }

    /*
    /**********************************************************************
    /* Internal methods:
    /**********************************************************************
     */

    private char[] allocBuffer(int minNeeded) {
        int size = Math.max(DEF_INITIAL_BUFFER_SIZE, minNeeded);
        char[] buf;
        if (_config != null) {
            buf = _config.allocMediumCBuffer(size);
            return buf;
        }
        return new char[size];
    }

    /**
     * Method called when current segment is full, to allocate new
     * segment.
     */
    private void expand(int roomNeeded) {
        // First, let's move current segment to segment list:
        if (_segments == null) {
            _segments = new ArrayList<>();
        }
        char[] curr = _currentSegment;
        _segments.add(curr);
        int oldLen = curr.length;
        _segmentSize += oldLen;
        int newSize = Math.max(roomNeeded, calcNewSize(oldLen));
        curr = new char[newSize];
        _currentSize = 0;
        _currentSegment = curr;
    }

    private char[] buildResultArray() {
        if (_resultString != null) { // Can take a shortcut...
            return _resultString.toCharArray();
        }
        char[] result;
        int size = size();
        if (size < 1) {
            return sNoChars;
        }
        int offset = 0;
        result = new char[size];
        if (_segments != null) {
            for (char[] curr : _segments) {
                int currLen = curr.length;
                System.arraycopy(curr, 0, result, offset, currLen);
                offset += currLen;
            }
        }
        System.arraycopy(_currentSegment, 0, result, offset, _currentSize);
        return result;
    }
}
