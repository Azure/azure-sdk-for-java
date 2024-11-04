// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import java.util.ArrayList;

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
            _currentSegment = allocBuffer();
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

    private char[] allocBuffer() {
        int size = Math.max(DEF_INITIAL_BUFFER_SIZE, 0);
        char[] buf;
        if (_config != null) {
            buf = _config.allocMediumCBuffer(size);
            return buf;
        }
        return new char[size];
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
