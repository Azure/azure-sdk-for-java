// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.format;

import java.io.*;

import com.typespec.json.implementation.jackson.core.JsonFactory;

/**
 * Interface used to expose beginning of a data file to data format
 * detection code.
 */
public interface InputAccessor
{
    /**
     * Method to call to check if more input is available.
     * Since this may result in more content to be read (at least
     * one more byte), a {@link IOException} may get thrown.
     *
     * @return Whether there is at least one more input byte accessible
     *
     * @throws IOException If check for more content failed due to issue with
     *   underlying input abstraction
     */
    boolean hasMoreBytes() throws IOException;

    /**
     * Returns next byte available, if any; if no more bytes are
     * available, will throw {@link java.io.EOFException}.
     *
     * @return Next content byte, if available
     *
     * @throws IOException If called and there is no more content available
     */
    byte nextByte() throws IOException;

    /**
     * Method that can be called to reset accessor to read from beginning of input.
     */
    void reset();

    /*
    /**********************************************************
    /* Standard implementation
    /**********************************************************
     */

    /**
     * Basic implementation that reads data from given
     * {@link InputStream} and buffers it as necessary.
     */
    class Std implements InputAccessor
    {
        protected final InputStream _in;

        protected final byte[] _buffer;

        protected final int _bufferedStart;

        /**
         * End of valid bytes in the buffer (points to one past last valid)
         */
        protected int _bufferedEnd;
        
        /**
         * Pointer to next available buffered byte in {@link #_buffer}.
         */
        protected int _ptr;

        // Constructor used when content to check is available via
        // input stream and must be read.
        public Std(InputStream in, byte[] buffer)
        {
            _in = in;
            _buffer = buffer;
            _bufferedStart = 0;
            _ptr = 0;
            _bufferedEnd = 0;
        }

        // Constructor used when the full input (or at least enough leading bytes
        // of full input) is available.
        public Std(byte[] inputDocument) {
            this(inputDocument, 0, inputDocument.length);
        }

        // Constructor used when the full input (or at least enough leading bytes
        // of full input) is available.
        public Std(byte[] inputDocument, int start, int len)
        {
            _in = null;
            _buffer = inputDocument;
            _ptr = start;
            _bufferedStart = start;
            _bufferedEnd = start+len;
        }
        
        @Override
        public boolean hasMoreBytes() throws IOException
        {
            if (_ptr < _bufferedEnd) { // already got more
                return true;
            }
            if (_in == null) { // nowhere to read from
                return false;
            }
            int amount = _buffer.length - _ptr;
            if (amount < 1) { // can not load any more
                return false;
            }
            int count = _in.read(_buffer, _ptr, amount);
            if (count <= 0) { // EOF
                return false;
            }
            _bufferedEnd += count;
            return true;
        }

        @Override
        public byte nextByte() throws IOException
        {
            // should we just try loading more automatically?
            if (_ptr >= _bufferedEnd) {
                if (!hasMoreBytes()) {
                    throw new EOFException("Failed auto-detect: could not read more than "+_ptr+" bytes (max buffer size: "+_buffer.length+")");
                }
            }
            return _buffer[_ptr++];
        }

        @Override
        public void reset() {
            _ptr = _bufferedStart;
        }

        /*
        /**********************************************************
        /* Extended API for DataFormatDetector/Matcher
        /**********************************************************
         */

        public DataFormatMatcher createMatcher(JsonFactory match, MatchStrength matchStrength)
        {
            return new DataFormatMatcher(_in, _buffer, _bufferedStart, (_bufferedEnd - _bufferedStart),
                    match, matchStrength);
        }
    }
}
