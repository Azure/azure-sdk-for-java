// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.typespec.json.implementation.jackson.core.io.NumberInput;

/**
 * TextBuffer is a class similar to {@link java.lang.StringBuffer}, with
 * following differences:
 *<ul>
 *  <li>TextBuffer uses segments character arrays, to avoid having
 *     to do additional array copies when array is not big enough.
 *     This means that only reallocating that is necessary is done only once:
 *     if and when caller
 *     wants to access contents in a linear array (char[], String).
 *    </li>
*  <li>TextBuffer can also be initialized in "shared mode", in which
*     it will just act as a wrapper to a single char array managed
*     by another object (like parser that owns it)
 *    </li>
 *  <li>TextBuffer is not synchronized.
 *    </li>
 * </ul>
 */
public final class TextBuffer
{
    final static char[] NO_CHARS = new char[0];

    /**
     * Let's start with sizable but not huge buffer, will grow as necessary
     *<p>
     * Reduced from 1000 down to 500 in 2.10.
     */
    final static int MIN_SEGMENT_LEN = 500;

    /**
     * Let's limit maximum segment length to something sensible.
     * For 2.10, let's limit to using 64kc chunks (128 kB) -- was 256kC/512kB up to 2.9
     */
    final static int MAX_SEGMENT_LEN = 0x10000;
    
    /*
    /**********************************************************
    /* Configuration:
    /**********************************************************
     */

    private final BufferRecycler _allocator;

    /*
    /**********************************************************
    /* Shared input buffers
    /**********************************************************
     */

    /**
     * Shared input buffer; stored here in case some input can be returned
     * as is, without being copied to collector's own buffers. Note that
     * this is read-only for this Object.
     */
    private char[] _inputBuffer;

    /**
     * Character offset of first char in input buffer; -1 to indicate
     * that input buffer currently does not contain any useful char data
     */
    private int _inputStart;

    private int _inputLen;

    /*
    /**********************************************************
    /* Aggregation segments (when not using input buf)
    /**********************************************************
     */

    /**
     * List of segments prior to currently active segment.
     */
    private ArrayList<char[]> _segments;

    /**
     * Flag that indicates whether _seqments is non-empty
     */
    private boolean _hasSegments;

    // // // Currently used segment; not (yet) contained in _seqments

    /**
     * Amount of characters in segments in {@link #_segments}
     */
    private int _segmentSize;

    private char[] _currentSegment;

    /**
     * Number of characters in currently active (last) segment
     */
    private int _currentSize;

    /*
    /**********************************************************
    /* Caching of results
    /**********************************************************
     */

    /**
     * String that will be constructed when the whole contents are
     * needed; will be temporarily stored in case asked for again.
     */
    private String _resultString;

    private char[] _resultArray;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public TextBuffer(BufferRecycler allocator) {
        _allocator = allocator;
    }

    // @since 2.10
    protected TextBuffer(BufferRecycler allocator, char[] initialSegment) {
        _allocator = allocator;
        _currentSegment = initialSegment;
        _currentSize = initialSegment.length;
        _inputStart = -1;
    }

    /**
     * Factory method for constructing an instance with no allocator, and
     * with initial full segment.
     *
     * @param initialSegment Initial, full segment to use for creating buffer (buffer
     *   {@link #size()} would return length of {@code initialSegment})
     *
     * @return TextBuffer constructed
     *
     * @since 2.10
     */
    public static TextBuffer fromInitial(char[] initialSegment) {
        return new TextBuffer(null, initialSegment);
    }

    /**
     * Method called to indicate that the underlying buffers should now
     * be recycled if they haven't yet been recycled. Although caller
     * can still use this text buffer, it is not advisable to call this
     * method if that is likely, since next time a buffer is needed,
     * buffers need to reallocated.
     *<p>
     * Note: since Jackson 2.11, calling this method will NOT clear already
     * aggregated contents (that is, {@code _currentSegment}, to retain
     * current token text if (but only if!) already aggregated.
     */
    public void releaseBuffers()
    {
        // inlined `resetWithEmpty()` (except leaving `_resultString` as-is
        {
            _inputStart = -1;
            _currentSize = 0;
            _inputLen = 0;

            _inputBuffer = null;
            // note: _resultString retained (see https://github.com/FasterXML/jackson-databind/issues/2635
            // for reason)
            _resultArray = null; // should this be retained too?

            if (_hasSegments) {
                clearSegments();
            }
        }

        if (_allocator != null) {
            if (_currentSegment != null) {
                // And then return that array
                char[] buf = _currentSegment;
                _currentSegment = null;
                _allocator.releaseCharBuffer(BufferRecycler.CHAR_TEXT_BUFFER, buf);
            }
        }
    }

    /**
     * Method called to clear out any content text buffer may have, and
     * initializes buffer to use non-shared data.
     */
    public void resetWithEmpty()
    {
        _inputStart = -1; // indicates shared buffer not used
        _currentSize = 0;
        _inputLen = 0;

        _inputBuffer = null;
        _resultString = null;
        _resultArray = null;

        // And then reset internal input buffers, if necessary:
        if (_hasSegments) {
            clearSegments();
        }
    }

    /**
     * Method for clearing out possibly existing content, and replacing them with
     * a single-character content (so {@link #size()} would return {@code 1})
     *
     * @param ch Character to set as the buffer contents
     *
     * @since 2.9
     */
    public void resetWith(char ch)
    {
        _inputStart = -1;
        _inputLen = 0;

        _resultString = null;
        _resultArray = null;

        if (_hasSegments) {
            clearSegments();
        } else if (_currentSegment == null) {
            _currentSegment = buf(1);
        }
        _currentSegment[0] = ch; // lgtm [java/dereferenced-value-may-be-null]
        _currentSize = _segmentSize = 1;
    }
    
    /**
     * Method called to initialize the buffer with a shared copy of data;
     * this means that buffer will just have pointers to actual data. It
     * also means that if anything is to be appended to the buffer, it
     * will first have to unshare it (make a local copy).
     *
     * @param buf Buffer that contains shared contents
     * @param offset Offset of the first content character in {@code buf}
     * @param len Length of content in {@code buf}
     */
    public void resetWithShared(char[] buf, int offset, int len)
    {
        // First, let's clear intermediate values, if any:
        _resultString = null;
        _resultArray = null;

        // Then let's mark things we need about input buffer
        _inputBuffer = buf;
        _inputStart = offset;
        _inputLen = len;

        // And then reset internal input buffers, if necessary:
        if (_hasSegments) {
            clearSegments();
        }
    }

    public void resetWithCopy(char[] buf, int offset, int len)
    {
        _inputBuffer = null;
        _inputStart = -1; // indicates shared buffer not used
        _inputLen = 0;

        _resultString = null;
        _resultArray = null;

        // And then reset internal input buffers, if necessary:
        if (_hasSegments) {
            clearSegments();
        } else if (_currentSegment == null) {
            _currentSegment = buf(len);
        }
        _currentSize = _segmentSize = 0;
        append(buf, offset, len);
    }

    // @since 2.9
    public void resetWithCopy(String text, int start, int len)
    {
        _inputBuffer = null;
        _inputStart = -1;
        _inputLen = 0;

        _resultString = null;
        _resultArray = null;

        if (_hasSegments) {
            clearSegments();
        } else if (_currentSegment == null) {
            _currentSegment = buf(len);
        }
        _currentSize = _segmentSize = 0;
        append(text, start, len);
    }

    public void resetWithString(String value)
    {
        _inputBuffer = null;
        _inputStart = -1;
        _inputLen = 0;

        _resultString = value;
        _resultArray = null;

        if (_hasSegments) {
            clearSegments();
        }
        _currentSize = 0;
        
    }

    /**
     * Method for accessing the currently active (last) content segment
     * without changing state of the buffer
     *
     * @return Currently active (last) content segment
     *
     * @since 2.9
     */
    public char[] getBufferWithoutReset() {
        return _currentSegment;
    }

    // Helper method used to find a buffer to use, ideally one
    // recycled earlier.
    private char[] buf(int needed)
    {
        if (_allocator != null) {
            return _allocator.allocCharBuffer(BufferRecycler.CHAR_TEXT_BUFFER, needed);
        }
        return new char[Math.max(needed, MIN_SEGMENT_LEN)];
    }

    private void clearSegments()
    {
        _hasSegments = false;
        /* Let's start using _last_ segment from list; for one, it's
         * the biggest one, and it's also most likely to be cached
         */
        /* 28-Aug-2009, tatu: Actually, the current segment should
         *   be the biggest one, already
         */
        //_currentSegment = _segments.get(_segments.size() - 1);
        _segments.clear();
        _currentSize = _segmentSize = 0;
    }

    /*
    /**********************************************************
    /* Accessors for implementing public interface
    /**********************************************************
     */

    /**
     * @return Number of characters currently stored in this buffer
     */
    public int size() {
        if (_inputStart >= 0) { // shared copy from input buf
            return _inputLen;
        }
        if (_resultArray != null) {
            return _resultArray.length;
        }
        if (_resultString != null) {
            return _resultString.length();
        }
        // local segmented buffers
        return _segmentSize + _currentSize;
    }

    public int getTextOffset() {
        /* Only shared input buffer can have non-zero offset; buffer
         * segments start at 0, and if we have to create a combo buffer,
         * that too will start from beginning of the buffer
         */
        return (_inputStart >= 0) ? _inputStart : 0;
    }

    /**
     * Method that can be used to check whether textual contents can
     * be efficiently accessed using {@link #getTextBuffer}.
     *
     * @return {@code True} if access via {@link #getTextBuffer()} would be efficient
     *   (that is, content already available as aggregated {@code char[]})
     */
    public boolean hasTextAsCharacters()
    {
        // if we have array in some form, sure
        if (_inputStart >= 0 || _resultArray != null)  return true;
        // not if we have String as value
        if (_resultString != null) return false;
        return true;
    }

    /**
     * Accessor that may be used to get the contents of this buffer as a single
     * {@code char[]} regardless of whether they were collected in a segmented
     * fashion or not: this typically require allocation of the result buffer.
     *
     * @return Aggregated {@code char[]} that contains all buffered content
     */
    public char[] getTextBuffer()
    {
        // Are we just using shared input buffer?
        if (_inputStart >= 0) return _inputBuffer;
        if (_resultArray != null)  return _resultArray;
        if (_resultString != null) {
            return (_resultArray = _resultString.toCharArray());
        }
        // Nope; but does it fit in just one segment?
        if (!_hasSegments) {
            return (_currentSegment == null) ? NO_CHARS : _currentSegment;
        }
        // Nope, need to have/create a non-segmented array and return it
        return contentsAsArray();
    }

    /*
    /**********************************************************
    /* Other accessors:
    /**********************************************************
     */

    /**
     * Accessor that may be used to get the contents of this buffer as a single
     * {@code String} regardless of whether they were collected in a segmented
     * fashion or not: this typically require construction of the result String.
     *
     * @return Aggregated buffered contents as a {@link java.lang.String}
     */
    public String contentsAsString()
    {
        if (_resultString == null) {
            // Has array been requested? Can make a shortcut, if so:
            if (_resultArray != null) {
                _resultString = new String(_resultArray);
            } else {
                // Do we use shared array?
                if (_inputStart >= 0) {
                    if (_inputLen < 1) {
                        return (_resultString = "");
                    }
                    _resultString = new String(_inputBuffer, _inputStart, _inputLen);
                } else { // nope... need to copy
                    // But first, let's see if we have just one buffer
                    int segLen = _segmentSize;
                    int currLen = _currentSize;
                    
                    if (segLen == 0) { // yup
                        _resultString = (currLen == 0) ? "" : new String(_currentSegment, 0, currLen);
                    } else { // no, need to combine
                        StringBuilder sb = new StringBuilder(segLen + currLen);
                        // First stored segments
                        if (_segments != null) {
                            for (int i = 0, len = _segments.size(); i < len; ++i) {
                                char[] curr = _segments.get(i);
                                sb.append(curr, 0, curr.length);
                            }
                        }
                        // And finally, current segment:
                        sb.append(_currentSegment, 0, _currentSize);
                        _resultString = sb.toString();
                    }
                }
            }
        }

        return _resultString;
    }
 
    public char[] contentsAsArray() {
        char[] result = _resultArray;
        if (result == null) {
            _resultArray = result = resultArray();
        }
        return result;
    }

    /**
     * Convenience method for converting contents of the buffer
     * into a {@link BigDecimal}.
     *
     * @return Buffered text value parsed as a {@link BigDecimal}, if possible
     *
     * @throws NumberFormatException if contents are not a valid Java number
     */
    public BigDecimal contentsAsDecimal() throws NumberFormatException
    {
        // Already got a pre-cut array?
        if (_resultArray != null) {
            return NumberInput.parseBigDecimal(_resultArray);
        }
        // Or a shared buffer?
        if ((_inputStart >= 0) && (_inputBuffer != null)) {
            return NumberInput.parseBigDecimal(_inputBuffer, _inputStart, _inputLen);
        }
        // Or if not, just a single buffer (the usual case)
        if ((_segmentSize == 0) && (_currentSegment != null)) {
            return NumberInput.parseBigDecimal(_currentSegment, 0, _currentSize);
        }
        // If not, let's just get it aggregated...
        return NumberInput.parseBigDecimal(contentsAsArray());
    }

    /**
     * Convenience method for converting contents of the buffer
     * into a Double value.
     *
     * @return Buffered text value parsed as a {@link Double}, if possible
     *
     * @throws NumberFormatException if contents are not a valid Java number
     */
    public double contentsAsDouble() throws NumberFormatException {
        return NumberInput.parseDouble(contentsAsString());
    }

    /**
     * Specialized convenience method that will decode a 32-bit int,
     * of at most 9 digits (and possible leading minus sign).
     *<p>
     * NOTE: method DOES NOT verify that the contents actually are a valid
     * Java {@code int} value (either regarding format, or value range): caller
     * MUST validate that.
     *
     * @param neg Whether contents start with a minus sign
     *
     * @return Buffered text value parsed as an {@code int} using
     *   {@link NumberInput#parseInt(String)} method (which does NOT validate input)
     *
     * @since 2.9
     */
    public int contentsAsInt(boolean neg) {
        if ((_inputStart >= 0) && (_inputBuffer != null)) {
            if (neg) {
                return -NumberInput.parseInt(_inputBuffer, _inputStart+1, _inputLen-1);
            }
            return NumberInput.parseInt(_inputBuffer, _inputStart, _inputLen);
        }
        if (neg) {
            return -NumberInput.parseInt(_currentSegment, 1, _currentSize-1);
        }
        return NumberInput.parseInt(_currentSegment, 0, _currentSize);
    }

    /**
     * Specialized convenience method that will decode a 64-bit int,
     * of at most 18 digits (and possible leading minus sign).
     *<p>
     * NOTE: method DOES NOT verify that the contents actually are a valid
     * Java {@code long} value (either regarding format, or value range): caller
     * MUST validate that.
     *
     * @param neg Whether contents start with a minus sign
     *
     * @return Buffered text value parsed as an {@code long} using
     *   {@link NumberInput#parseLong(String)} method (which does NOT validate input)
     *
     * @since 2.9
     */
    public long contentsAsLong(boolean neg) {
        if ((_inputStart >= 0) && (_inputBuffer != null)) {
            if (neg) {
                return -NumberInput.parseLong(_inputBuffer, _inputStart+1, _inputLen-1);
            }
            return NumberInput.parseLong(_inputBuffer, _inputStart, _inputLen);
        }
        if (neg) {
            return -NumberInput.parseLong(_currentSegment, 1, _currentSize-1);
        }
        return NumberInput.parseLong(_currentSegment, 0, _currentSize);
    }

    /**
     * Accessor that will write buffered contents using given {@link Writer}.
     *
     * @param w Writer to use for writing out buffered content
     *
     * @return Number of characters written (same as {@link #size()})
     *
     * @throws IOException If write using {@link Writer} parameter fails
     *
     * @since 2.8
     */
    public int contentsToWriter(Writer w) throws IOException
    {
        if (_resultArray != null) {
            w.write(_resultArray);
            return _resultArray.length;
        }
        if (_resultString != null) { // Can take a shortcut...
            w.write(_resultString);
            return _resultString.length();
        }
        // Do we use shared array?
        if (_inputStart >= 0) {
            final int len = _inputLen;
            if (len > 0) {
                w.write(_inputBuffer, _inputStart, len);
            }
            return len;
        }
        // nope, not shared
        int total = 0;
        if (_segments != null) {
            for (int i = 0, end = _segments.size(); i < end; ++i) {
                char[] curr = _segments.get(i);
                int currLen = curr.length;
                w.write(curr, 0, currLen);
                total += currLen;
            }
        }
        int len = _currentSize;
        if (len > 0) {
            w.write(_currentSegment, 0, len);
            total += len;
        }
        return total;
    }

    /*
    /**********************************************************
    /* Public mutators:
    /**********************************************************
     */

    /**
     * Method called to make sure that buffer is not using shared input
     * buffer; if it is, it will copy such contents to private buffer.
     */
    public void ensureNotShared() {
        if (_inputStart >= 0) {
            unshare(16);
        }
    }

    public void append(char c) {
        // Using shared buffer so far?
        if (_inputStart >= 0) {
            unshare(16);
        }
        _resultString = null;
        _resultArray = null;
        // Room in current segment?
        char[] curr = _currentSegment;
        if (_currentSize >= curr.length) {
            expand(1);
            curr = _currentSegment;
        }
        curr[_currentSize++] = c;
    }

    public void append(char[] c, int start, int len)
    {
        // Can't append to shared buf (sanity check)
        if (_inputStart >= 0) {
            unshare(len);
        }
        _resultString = null;
        _resultArray = null;

        // Room in current segment?
        char[] curr = _currentSegment;
        int max = curr.length - _currentSize;
            
        if (max >= len) {
            System.arraycopy(c, start, curr, _currentSize, len);
            _currentSize += len;
            return;
        }
        // No room for all, need to copy part(s):
        if (max > 0) {
            System.arraycopy(c, start, curr, _currentSize, max);
            start += max;
            len -= max;
        }
        // And then allocate new segment; we are guaranteed to now
        // have enough room in segment.
        do {
            expand(len);
            int amount = Math.min(_currentSegment.length, len);
            System.arraycopy(c, start, _currentSegment, 0, amount);
            _currentSize += amount;
            start += amount;
            len -= amount;
        } while (len > 0);
    }

    public void append(String str, int offset, int len)
    {
        // Can't append to shared buf (sanity check)
        if (_inputStart >= 0) {
            unshare(len);
        }
        _resultString = null;
        _resultArray = null;

        // Room in current segment?
        char[] curr = _currentSegment;
        int max = curr.length - _currentSize;
        if (max >= len) {
            str.getChars(offset, offset+len, curr, _currentSize);
            _currentSize += len;
            return;
        }
        // No room for all, need to copy part(s):
        if (max > 0) {
            str.getChars(offset, offset+max, curr, _currentSize);
            len -= max;
            offset += max;
        }
        // And then allocate new segment; we are guaranteed to now
        // have enough room in segment.
        do {
            expand(len);
            int amount = Math.min(_currentSegment.length, len);
            str.getChars(offset, offset+amount, _currentSegment, 0);
            _currentSize += amount;
            offset += amount;
            len -= amount;
        } while (len > 0);
    }

    /*
    /**********************************************************
    /* Raw access, for high-performance use:
    /**********************************************************
     */

    public char[] getCurrentSegment()
    {
        /* Since the intention of the caller is to directly add stuff into
         * buffers, we should NOT have anything in shared buffer... ie. may
         * need to unshare contents.
         */
        if (_inputStart >= 0) {
            unshare(1);
        } else {
            char[] curr = _currentSegment;
            if (curr == null) {
                _currentSegment = buf(0);
            } else if (_currentSize >= curr.length) {
                // Plus, we better have room for at least one more char
                expand(1);
            }
        }
        return _currentSegment;
    }

    public char[] emptyAndGetCurrentSegment()
    {
        // inlined 'resetWithEmpty()'
        _inputStart = -1; // indicates shared buffer not used
        _currentSize = 0;
        _inputLen = 0;

        _inputBuffer = null;
        _resultString = null;
        _resultArray = null;

        // And then reset internal input buffers, if necessary:
        if (_hasSegments) {
            clearSegments();
        }
        char[] curr = _currentSegment;
        if (curr == null) {
            _currentSegment = curr = buf(0);
        }
        return curr;
    }

    public int getCurrentSegmentSize() { return _currentSize; }
    public void setCurrentLength(int len) { _currentSize = len; }

    /**
     * Convenience method that finishes the current active content segment
     * (by specifying how many characters within consists of valid content)
     * and aggregates and returns resulting contents (similar to a call
     * to {@link #contentsAsString()}).
     *
     * @param len Length of content (in characters) of the current active segment
     *
     * @return String that contains all buffered content
     *
     * @since 2.6
     */
    public String setCurrentAndReturn(int len) {
        _currentSize = len;
        // We can simplify handling here compared to full `contentsAsString()`:
        if (_segmentSize > 0) { // longer text; call main method
            return contentsAsString();
        }
        // more common case: single segment
        int currLen = _currentSize;
        String str = (currLen == 0) ? "" : new String(_currentSegment, 0, currLen);
        _resultString = str;
        return str;
    }
    
    public char[] finishCurrentSegment() {
        if (_segments == null) {
            _segments = new ArrayList<char[]>();
        }
        _hasSegments = true;
        _segments.add(_currentSegment);
        int oldLen = _currentSegment.length;
        _segmentSize += oldLen;
        _currentSize = 0;

        // Let's grow segments by 50%
        int newLen = oldLen + (oldLen >> 1);
        if (newLen < MIN_SEGMENT_LEN) {
            newLen = MIN_SEGMENT_LEN;
        } else if (newLen > MAX_SEGMENT_LEN) {
            newLen = MAX_SEGMENT_LEN;
        }
        char[] curr = carr(newLen);
        _currentSegment = curr;
        return curr;
    }

    /**
     * Method called to expand size of the current segment, to
     * accommodate for more contiguous content. Usually only
     * used when parsing tokens like names if even then.
     * Method will both expand the segment and return it
     *
     * @return Expanded current segment
     */
    public char[] expandCurrentSegment()
    {
        final char[] curr = _currentSegment;
        // Let's grow by 50% by default
        final int len = curr.length;
        int newLen = len + (len >> 1);
        // but above intended maximum, slow to increase by 25%
        if (newLen > MAX_SEGMENT_LEN) {
            newLen = len + (len >> 2);
        }
        return (_currentSegment = Arrays.copyOf(curr, newLen));
    }

    /**
     * Method called to expand size of the current segment, to
     * accommodate for more contiguous content. Usually only
     * used when parsing tokens like names if even then.
     * 
     * @param minSize Required minimum strength of the current segment
     *
     * @return Expanded current segment
     *
     * @since 2.4
     */
    public char[] expandCurrentSegment(int minSize) {
        char[] curr = _currentSegment;
        if (curr.length >= minSize) return curr;
        _currentSegment = curr = Arrays.copyOf(curr, minSize);
        return curr;
    }

    /*
    /**********************************************************
    /* Standard methods:
    /**********************************************************
     */

    /**
     * Note: calling this method may not be as efficient as calling
     * {@link #contentsAsString}, since it's not guaranteed that resulting
     * String is cached.
     */
    @Override public String toString() { return contentsAsString(); }

    /*
    /**********************************************************
    /* Internal methods:
    /**********************************************************
     */

    /**
     * Method called if/when we need to append content when we have been
     * initialized to use shared buffer.
     */
    private void unshare(int needExtra)
    {
        int sharedLen = _inputLen;
        _inputLen = 0;
        char[] inputBuf = _inputBuffer;
        _inputBuffer = null;
        int start = _inputStart;
        _inputStart = -1;

        // Is buffer big enough, or do we need to reallocate?
        int needed = sharedLen+needExtra;
        if (_currentSegment == null || needed > _currentSegment.length) {
            _currentSegment = buf(needed);
        }
        if (sharedLen > 0) {
            System.arraycopy(inputBuf, start, _currentSegment, 0, sharedLen);
        }
        _segmentSize = 0;
        _currentSize = sharedLen;
    }

    // Method called when current segment is full, to allocate new segment.
    private void expand(int minNewSegmentSize)
    {
        // First, let's move current segment to segment list:
        if (_segments == null) {
            _segments = new ArrayList<char[]>();
        }
        char[] curr = _currentSegment;
        _hasSegments = true;
        _segments.add(curr);
        _segmentSize += curr.length;
        _currentSize = 0;
        int oldLen = curr.length;
        
        // Let's grow segments by 50% minimum
        int newLen = oldLen + (oldLen >> 1);
        if (newLen < MIN_SEGMENT_LEN) {
            newLen = MIN_SEGMENT_LEN;
        } else if (newLen > MAX_SEGMENT_LEN) {
            newLen = MAX_SEGMENT_LEN;
        }
        _currentSegment = carr(newLen);
    }

    private char[] resultArray()
    {
        if (_resultString != null) { // Can take a shortcut...
            return _resultString.toCharArray();
        }
        // Do we use shared array?
        if (_inputStart >= 0) {
            final int len = _inputLen;
            if (len < 1) {
                return NO_CHARS;
            }
            final int start = _inputStart;
            if (start == 0) {
                return Arrays.copyOf(_inputBuffer, len);
            }
            return Arrays.copyOfRange(_inputBuffer, start, start+len);
        }
        // nope, not shared
        int size = size();
        if (size < 1) {
            return NO_CHARS;
        }
        int offset = 0;
        final char[] result = carr(size);
        if (_segments != null) {
            for (int i = 0, len = _segments.size(); i < len; ++i) {
                char[] curr = _segments.get(i);
                int currLen = curr.length;
                System.arraycopy(curr, 0, result, offset, currLen);
                offset += currLen;
            }
        }
        System.arraycopy(_currentSegment, 0, result, offset, _currentSize);
        return result;
    }

    private char[] carr(int len) { return new char[len]; }
}
