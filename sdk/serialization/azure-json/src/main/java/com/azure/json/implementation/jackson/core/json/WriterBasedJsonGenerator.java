// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.json;

import java.io.*;

import com.azure.json.implementation.jackson.core.*;
import com.azure.json.implementation.jackson.core.io.CharTypes;
import com.azure.json.implementation.jackson.core.io.CharacterEscapes;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.NumberOutput;

/**
 * {@link JsonGenerator} that outputs JSON content using a {@link java.io.Writer}
 * which handles character encoding.
 */
public class WriterBasedJsonGenerator extends JsonGeneratorImpl {
    protected final static int SHORT_WRITE = 32;

    protected final static char[] HEX_CHARS_UPPER = CharTypes.copyHexChars(true);

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected final Writer _writer;

    /**
     * Character used for quoting JSON Object property names
     * and String values.
     */
    protected char _quoteChar;

    /*
    /**********************************************************
    /* Output buffering
    /**********************************************************
     */

    /**
     * Intermediate buffer in which contents are buffered before
     * being written using {@link #_writer}.
     */
    protected char[] _outputBuffer;

    /**
     * Pointer to the first buffered character to output
     */
    protected int _outputHead;

    /**
     * Pointer to the position right beyond the last character to output
     * (end marker; may point to position right beyond the end of the buffer)
     */
    protected int _outputTail;

    /**
     * End marker of the output buffer; one past the last valid position
     * within the buffer.
     */
    protected int _outputEnd;

    /**
     * Short (14 char) temporary buffer allocated if needed, for constructing
     * escape sequences
     */
    protected char[] _entityBuffer;

    /**
     * Intermediate buffer in which characters of a String are copied
     * before being encoded.
     *
     * @since 2.10
     */
    protected char[] _copyBuffer;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    // @since 2.10
    public WriterBasedJsonGenerator(IOContext ctxt, int features, Writer w) {
        super(ctxt, features);
        _writer = w;
        _outputBuffer = ctxt.allocConcatBuffer();
        _outputEnd = _outputBuffer.length;
        _quoteChar = JsonFactory.DEFAULT_QUOTE_CHAR;
    }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    public void writeFieldName(String name) throws IOException {
        int status = _writeContext.writeFieldName(name);
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        _writeFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
    }

    protected final void _writeFieldName(String name, boolean commaBefore) throws IOException {
        // for fast+std case, need to output up to 2 chars, comma, dquote
        if ((_outputTail + 1) >= _outputEnd) {
            _flushBuffer();
        }
        if (commaBefore) {
            _outputBuffer[_outputTail++] = ',';
        }
        // Alternate mode, in which quoting of field names disabled?
        // we know there's room for at least one more char
        _outputBuffer[_outputTail++] = _quoteChar;
        // The beef:
        _writeString(name);
        // and closing quotes; need room for one more char:
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    /*
    /**********************************************************
    /* Output method implementations, structural
    /**********************************************************
     */

    @Override
    public void writeStartArray() throws IOException {
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext();
        streamWriteConstraints().validateNestingDepth(_writeContext.getNestingDepth());
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '[';
    }

    @Override
    public void writeEndArray() throws IOException {
        if (!_writeContext.inArray()) {
            _reportError("Current context not Array but " + _writeContext.typeDesc());
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = ']';
        _writeContext = _writeContext.clearAndGetParent();
    }

    @Override
    public void writeStartObject() throws IOException {
        _verifyValueWrite("start an object");
        _writeContext = _writeContext.createChildObjectContext();
        streamWriteConstraints().validateNestingDepth(_writeContext.getNestingDepth());
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '{';
    }

    @Override
    public void writeEndObject() throws IOException {
        if (!_writeContext.inObject()) {
            _reportError("Current context not Object but " + _writeContext.typeDesc());
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '}';
        _writeContext = _writeContext.clearAndGetParent();
    }

    /*
    /**********************************************************
    /* Output method implementations, textual
    /**********************************************************
     */

    @Override
    public void writeString(String text) throws IOException {
        _verifyValueWrite(WRITE_STRING);
        if (text == null) {
            _writeNull();
            return;
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        _writeString(text);
        // And finally, closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    /*
    /**********************************************************
    /* Output method implementations, unprocessed ("raw")
    /**********************************************************
     */

    @Override
    public void writeRaw(String text) throws IOException {
        // Nothing to check, can just output as is
        int len = text.length();
        int room = _outputEnd - _outputTail;

        if (room == 0) {
            _flushBuffer();
            room = _outputEnd - _outputTail;
        }
        // But would it nicely fit in? If yes, it's easy
        if (room >= len) {
            text.getChars(0, len, _outputBuffer, _outputTail);
            _outputTail += len;
        } else {
            writeRawLong(text);
        }
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        _checkRangeBoundsForString(text, offset, len);

        // Nothing to check, can just output as is
        int room = _outputEnd - _outputTail;

        if (room < len) {
            _flushBuffer();
            room = _outputEnd - _outputTail;
        }
        // But would it nicely fit in? If yes, it's easy
        if (room >= len) {
            text.getChars(offset, offset + len, _outputBuffer, _outputTail);
            _outputTail += len;
        } else {
            writeRawLong(text.substring(offset, offset + len));
        }
    }

    @Override
    public void writeRaw(char[] cbuf, int offset, int len) throws IOException {
        _checkRangeBoundsForCharArray(cbuf, offset, len);

        // Only worth buffering if it's a short write?
        if (len < SHORT_WRITE) {
            int room = _outputEnd - _outputTail;
            if (len > room) {
                _flushBuffer();
            }
            System.arraycopy(cbuf, offset, _outputBuffer, _outputTail, len);
            _outputTail += len;
            return;
        }
        // Otherwise, better just pass through:
        _flushBuffer();
        _writer.write(cbuf, offset, len);
    }

    private void writeRawLong(String text) throws IOException {
        int room = _outputEnd - _outputTail;
        // If not, need to do it by looping
        text.getChars(0, room, _outputBuffer, _outputTail);
        _outputTail += room;
        _flushBuffer();
        int offset = room;
        int len = text.length() - room;

        while (len > _outputEnd) {
            int amount = _outputEnd;
            text.getChars(offset, offset + amount, _outputBuffer, 0);
            _outputHead = 0;
            _outputTail = amount;
            _flushBuffer();
            offset += amount;
            len -= amount;
        }
        // And last piece (at most length of buffer)
        text.getChars(offset, offset + len, _outputBuffer, 0);
        _outputHead = 0;
        _outputTail = len;
    }

    /*
    /**********************************************************
    /* Output method implementations, base64-encoded binary
    /**********************************************************
     */

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
        _checkRangeBoundsForByteArray(data, offset, len);

        _verifyValueWrite(WRITE_BINARY);
        // Starting quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        _writeBinary(b64variant, data, offset, offset + len);
        // and closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    /*
    /**********************************************************
    /* Output method implementations, primitive
    /**********************************************************
     */

    @Override
    public void writeNumber(int i) throws IOException {
        _verifyValueWrite(WRITE_NUMBER);
        // up to 10 digits and possible minus sign
        if ((_outputTail + 11) >= _outputEnd) {
            _flushBuffer();
        }
        _outputTail = NumberOutput.outputInt(i, _outputBuffer, _outputTail);
    }

    @Override
    public void writeNumber(long l) throws IOException {
        _verifyValueWrite(WRITE_NUMBER);
        if ((_outputTail + 21) >= _outputEnd) {
            // up to 20 digits, minus sign
            _flushBuffer();
        }
        _outputTail = NumberOutput.outputLong(l, _outputBuffer, _outputTail);
    }

    // !!! 05-Aug-2008, tatus: Any ways to optimize these?

    @SuppressWarnings("deprecation")
    @Override
    public void writeNumber(double d) throws IOException {
        if ((NumberOutput.notFinite(d) && isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
            writeString(NumberOutput.toString(d));
            return;
        }
        // What is the max length for doubles? 40 chars?
        _verifyValueWrite(WRITE_NUMBER);
        writeRaw(NumberOutput.toString(d));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void writeNumber(float f) throws IOException {
        if ((NumberOutput.notFinite(f) && isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
            writeString(NumberOutput.toString(f));
            return;
        }
        // What is the max length for floats?
        _verifyValueWrite(WRITE_NUMBER);
        writeRaw(NumberOutput.toString(f));
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        _verifyValueWrite(WRITE_BOOLEAN);
        if ((_outputTail + 5) >= _outputEnd) {
            _flushBuffer();
        }
        int ptr = _outputTail;
        char[] buf = _outputBuffer;
        if (state) {
            buf[ptr] = 't';
            buf[++ptr] = 'r';
            buf[++ptr] = 'u';
            buf[++ptr] = 'e';
        } else {
            buf[ptr] = 'f';
            buf[++ptr] = 'a';
            buf[++ptr] = 'l';
            buf[++ptr] = 's';
            buf[++ptr] = 'e';
        }
        _outputTail = ptr + 1;
    }

    @Override
    public void writeNull() throws IOException {
        _verifyValueWrite(WRITE_NULL);
        _writeNull();
    }

    /*
    /**********************************************************
    /* Implementations for other methods
    /**********************************************************
     */

    @Override
    protected final void _verifyValueWrite(String typeMsg) throws IOException {
        final int status = _writeContext.writeValue();
        char c;
        switch (status) {
            case JsonWriteContext.STATUS_OK_AS_IS:
            default:
                return;

            case JsonWriteContext.STATUS_OK_AFTER_COMMA:
                c = ',';
                break;

            case JsonWriteContext.STATUS_OK_AFTER_COLON:
                c = ':';
                break;

            case JsonWriteContext.STATUS_OK_AFTER_SPACE: // root-value separator
                return;

            case JsonWriteContext.STATUS_EXPECT_NAME:
                _reportCantWriteValueExpectName(typeMsg);
                return;
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = c;
    }

    /*
    /**********************************************************
    /* Low-level output handling
    /**********************************************************
     */

    @Override
    public void flush() throws IOException {
        _flushBuffer();
        if (_writer != null) {
            _writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();

        // 05-Dec-2008, tatu: To add [JACKSON-27], need to close open scopes
        // First: let's see that we still have buffers...
        IOException flushFail = null;
        try {
            if (_outputBuffer != null) {
                while (true) {
                    JsonStreamContext ctxt = getOutputContext();
                    if (ctxt.inArray()) {
                        writeEndArray();
                    } else if (ctxt.inObject()) {
                        writeEndObject();
                    } else {
                        break;
                    }
                }
            }
            _flushBuffer();
        } catch (IOException e) {
            // 10-Jun-2022, tatu: [core#764] Need to avoid failing here; may
            //    still need to close the underlying output stream
            flushFail = e;
        }
        _outputHead = 0;
        _outputTail = 0;

        /* 25-Nov-2008, tatus: As per [JACKSON-16] we are not to call close()
         *   on the underlying Reader, unless we "own" it, or auto-closing
         *   feature is enabled.
         *   One downside: when using UTF8Writer, underlying buffer(s)
         *   may not be properly recycled if we don't close the writer.
         */
        if (_writer != null) {
            try {
                _writer.close();
            } catch (IOException | RuntimeException e) {
                if (flushFail != null) {
                    e.addSuppressed(flushFail);
                }
                throw e;
            }
        }
        // Internal buffer(s) generator has can now be released as well
        _releaseBuffers();

        if (flushFail != null) {
            throw flushFail;
        }
    }

    @Override
    protected void _releaseBuffers() {
        char[] buf = _outputBuffer;
        if (buf != null) {
            _outputBuffer = null;
            _ioContext.releaseConcatBuffer(buf);
        }
        buf = _copyBuffer;
        if (buf != null) {
            _copyBuffer = null;
            _ioContext.releaseNameCopyBuffer(buf);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing; text, default
    /**********************************************************
     */

    private void _writeString(String text) throws IOException {
        /* One check first: if String won't fit in the buffer, let's
         * segment writes. No point in extending buffer to huge sizes
         * (like if someone wants to include multi-megabyte base64
         * encoded stuff or such)
         */
        final int len = text.length();
        if (len > _outputEnd) { // Let's reserve space for entity at begin/end
            _writeLongString(text);
            return;
        }

        // Ok: we know String will fit in buffer ok
        // But do we need to flush first?
        if ((_outputTail + len) > _outputEnd) {
            _flushBuffer();
        }
        text.getChars(0, len, _outputBuffer, _outputTail);

        if (_maximumNonEscapedChar != 0) {
            _writeStringASCII(len, _maximumNonEscapedChar);
        } else {
            _writeString2(len);
        }
    }

    private void _writeString2(final int len) throws IOException {
        // And then we'll need to verify need for escaping etc:
        final int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;

        output_loop: while (_outputTail < end) {
            // Fast loop for chars not needing escaping
            while (true) {
                char c = _outputBuffer[_outputTail];
                if (c < escLen && escCodes[c] != 0) {
                    break;
                }
                if (++_outputTail >= end) {
                    break output_loop;
                }
            }

            // Ok, bumped into something that needs escaping.
            /* First things first: need to flush the buffer.
             * Inlined, as we don't want to lose tail pointer
             */
            int flushLen = (_outputTail - _outputHead);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, _outputHead, flushLen);
            }
            /* In any case, tail will be the new start, so hopefully
             * we have room now.
             */
            char c = _outputBuffer[_outputTail++];
            _prependOrWriteCharacterEscape(c, escCodes[c]);
        }
    }

    /**
     * Method called to write "long strings", strings whose length exceeds
     * output buffer length.
     */
    private void _writeLongString(String text) throws IOException {
        // First things first: let's flush the buffer to get some more room
        _flushBuffer();

        // Then we can write
        final int textLen = text.length();
        int offset = 0;
        do {
            int max = _outputEnd;
            int segmentLen = ((offset + max) > textLen) ? (textLen - offset) : max;
            text.getChars(offset, offset + segmentLen, _outputBuffer, 0);
            if (_maximumNonEscapedChar != 0) {
                _writeSegmentASCII(segmentLen, _maximumNonEscapedChar);
            } else {
                _writeSegment(segmentLen);
            }
            offset += segmentLen;
        } while (offset < textLen);
    }

    /**
     * Method called to output textual context which has been copied
     * to the output buffer prior to call. If any escaping is needed,
     * it will also be handled by the method.
     *<p>
     * Note: when called, textual content to write is within output
     * buffer, right after buffered content (if any). That's why only
     * length of that text is passed, as buffer and offset are implied.
     */
    private void _writeSegment(int end) throws IOException {
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;

        int ptr = 0;
        int start = ptr;

        while (ptr < end) {
            // Fast loop for chars not needing escaping
            char c;
            while (true) {
                c = _outputBuffer[ptr];
                if (c < escLen && escCodes[c] != 0) {
                    break;
                }
                if (++ptr >= end) {
                    break;
                }
            }

            // Ok, bumped into something that needs escaping.
            /* First things first: need to flush the buffer.
             * Inlined, as we don't want to lose tail pointer
             */
            int flushLen = (ptr - start);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break;
                }
            }
            ++ptr;
            // So; either try to prepend (most likely), or write directly:
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCodes[c]);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with additional escaping (ASCII or such)
    /**********************************************************
     */

    /* Same as "_writeString2()", except needs additional escaping
     * for subset of characters
     */
    private void _writeStringASCII(final int len, final int maxNonEscaped) throws IOException {
        // And then we'll need to verify need for escaping etc:
        int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);
        int escCode;

        output_loop: while (_outputTail < end) {
            char c;
            // Fast loop for chars not needing escaping
            while (true) {
                c = _outputBuffer[_outputTail];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                }
                if (++_outputTail >= end) {
                    break output_loop;
                }
            }
            int flushLen = (_outputTail - _outputHead);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, _outputHead, flushLen);
            }
            ++_outputTail;
            _prependOrWriteCharacterEscape(c, escCode);
        }
    }

    private void _writeSegmentASCII(int end, final int maxNonEscaped) throws IOException {
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped + 1);

        int ptr = 0;
        int escCode = 0;
        int start = ptr;

        while (ptr < end) {
            // Fast loop for chars not needing escaping
            char c;
            while (true) {
                c = _outputBuffer[ptr];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                }
                if (++ptr >= end) {
                    break;
                }
            }
            int flushLen = (ptr - start);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break;
                }
            }
            ++ptr;
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCode);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing; binary
    /**********************************************************
     */

    protected final void _writeBinary(Base64Variant b64variant, byte[] input, int inputPtr, final int inputEnd)
        throws IOException {
        // Encoding is by chunks of 3 input, 4 output chars, so:
        int safeInputEnd = inputEnd - 3;
        // Let's also reserve room for possible (and quoted) lf char each round
        int safeOutputEnd = _outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;

        // Ok, first we loop through all full triplets of data:
        while (inputPtr <= safeInputEnd) {
            if (_outputTail > safeOutputEnd) { // need to flush
                _flushBuffer();
            }
            // First, mash 3 bytes into lsb of 32-bit int
            int b24 = (input[inputPtr++]) << 8;
            b24 |= (input[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | ((input[inputPtr++]) & 0xFF);
            _outputTail = b64variant.encodeBase64Chunk(b24, _outputBuffer, _outputTail);
            if (--chunksBeforeLF <= 0) {
                // note: must quote in JSON value
                _outputBuffer[_outputTail++] = '\\';
                _outputBuffer[_outputTail++] = 'n';
                chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
            }
        }

        // And then we may have 1 or 2 leftover bytes to encode
        int inputLeft = inputEnd - inputPtr; // 0, 1 or 2
        if (inputLeft > 0) { // yes, but do we have room for output?
            if (_outputTail > safeOutputEnd) { // don't really need 6 bytes but...
                _flushBuffer();
            }
            int b24 = (input[inputPtr++]) << 16;
            if (inputLeft == 2) {
                b24 |= ((input[inputPtr++]) & 0xFF) << 8;
            }
            _outputTail = b64variant.encodeBase64Partial(b24, inputLeft, _outputBuffer, _outputTail);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, other
    /**********************************************************
     */

    private void _writeNull() throws IOException {
        if ((_outputTail + 4) >= _outputEnd) {
            _flushBuffer();
        }
        int ptr = _outputTail;
        char[] buf = _outputBuffer;
        buf[ptr] = 'n';
        buf[++ptr] = 'u';
        buf[++ptr] = 'l';
        buf[++ptr] = 'l';
        _outputTail = ptr + 1;
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, escapes
    /**********************************************************
     */

    /**
     * Method called to try to either prepend character escape at front of
     * given buffer; or if not possible, to write it out directly.
     * Uses head and tail pointers (and updates as necessary)
     */
    private void _prependOrWriteCharacterEscape(char ch, int escCode) throws IOException {
        if (escCode >= 0) { // \\N (2 char)
            if (_outputTail >= 2) { // fits, just prepend
                int ptr = _outputTail - 2;
                _outputHead = ptr;
                _outputBuffer[ptr++] = '\\';
                _outputBuffer[ptr] = (char) escCode;
                return;
            }
            // won't fit, write
            char[] buf = _entityBuffer;
            if (buf == null) {
                buf = _allocateEntityBuffer();
            }
            _outputHead = _outputTail;
            buf[1] = (char) escCode;
            _writer.write(buf, 0, 2);
            return;
        }

        if (_outputTail >= 6) { // fits, prepend to buffer
            char[] buf = _outputBuffer;
            int ptr = _outputTail - 6;
            _outputHead = ptr;
            buf[ptr] = '\\';
            buf[++ptr] = 'u';
            // We know it's a control char, so only the last 2 chars are non-0
            if (ch > 0xFF) { // beyond 8 bytes
                int hi = (ch >> 8) & 0xFF;
                buf[++ptr] = HEX_CHARS_UPPER[hi >> 4];
                buf[++ptr] = HEX_CHARS_UPPER[hi & 0xF];
                ch &= 0xFF;
            } else {
                buf[++ptr] = '0';
                buf[++ptr] = '0';
            }
            buf[++ptr] = HEX_CHARS_UPPER[ch >> 4];
            buf[++ptr] = HEX_CHARS_UPPER[ch & 0xF];
            return;
        }
        // won't fit, flush and write
        char[] buf = _entityBuffer;
        if (buf == null) {
            buf = _allocateEntityBuffer();
        }
        _outputHead = _outputTail;
        if (ch > 0xFF) { // beyond 8 bytes
            int hi = (ch >> 8) & 0xFF;
            int lo = ch & 0xFF;
            buf[10] = HEX_CHARS_UPPER[hi >> 4];
            buf[11] = HEX_CHARS_UPPER[hi & 0xF];
            buf[12] = HEX_CHARS_UPPER[lo >> 4];
            buf[13] = HEX_CHARS_UPPER[lo & 0xF];
            _writer.write(buf, 8, 6);
        } else { // We know it's a control char, so only the last 2 chars are non-0
            buf[6] = HEX_CHARS_UPPER[ch >> 4];
            buf[7] = HEX_CHARS_UPPER[ch & 0xF];
            _writer.write(buf, 2, 6);
        }
    }

    /**
     * Method called to try to either prepend character escape at front of
     * given buffer; or if not possible, to write it out directly.
     *
     * @return Pointer to start of prepended entity (if prepended); or 'ptr'
     *   if not.
     */
    private int _prependOrWriteCharacterEscape(char[] buffer, int ptr, int end, char ch, int escCode)
        throws IOException {
        if (escCode >= 0) { // \\N (2 char)
            if (ptr > 1 && ptr < end) { // fits, just prepend
                ptr -= 2;
                buffer[ptr] = '\\';
                buffer[ptr + 1] = (char) escCode;
            } else { // won't fit, write
                char[] ent = _entityBuffer;
                if (ent == null) {
                    ent = _allocateEntityBuffer();
                }
                ent[1] = (char) escCode;
                _writer.write(ent, 0, 2);
            }
            return ptr;
        }

        if (ptr > 5 && ptr < end) { // fits, prepend to buffer
            ptr -= 6;
            buffer[ptr++] = '\\';
            buffer[ptr++] = 'u';
            // We know it's a control char, so only the last 2 chars are non-0
            if (ch > 0xFF) { // beyond 8 bytes
                int hi = (ch >> 8) & 0xFF;
                buffer[ptr++] = HEX_CHARS_UPPER[hi >> 4];
                buffer[ptr++] = HEX_CHARS_UPPER[hi & 0xF];
                ch &= 0xFF;
            } else {
                buffer[ptr++] = '0';
                buffer[ptr++] = '0';
            }
            buffer[ptr++] = HEX_CHARS_UPPER[ch >> 4];
            buffer[ptr] = HEX_CHARS_UPPER[ch & 0xF];
            ptr -= 5;
        } else {
            // won't fit, flush and write
            char[] ent = _entityBuffer;
            if (ent == null) {
                ent = _allocateEntityBuffer();
            }
            _outputHead = _outputTail;
            if (ch > 0xFF) { // beyond 8 bytes
                int hi = (ch >> 8) & 0xFF;
                int lo = ch & 0xFF;
                ent[10] = HEX_CHARS_UPPER[hi >> 4];
                ent[11] = HEX_CHARS_UPPER[hi & 0xF];
                ent[12] = HEX_CHARS_UPPER[lo >> 4];
                ent[13] = HEX_CHARS_UPPER[lo & 0xF];
                _writer.write(ent, 8, 6);
            } else { // We know it's a control char, so only the last 2 chars are non-0
                ent[6] = HEX_CHARS_UPPER[ch >> 4];
                ent[7] = HEX_CHARS_UPPER[ch & 0xF];
                _writer.write(ent, 2, 6);
            }
        }
        return ptr;
    }

    private char[] _allocateEntityBuffer() {
        char[] buf = new char[14];
        // first 2 chars, non-numeric escapes (like \n)
        buf[0] = '\\';
        // next 6; 8-bit escapes (control chars mostly)
        buf[2] = '\\';
        buf[3] = 'u';
        buf[4] = '0';
        buf[5] = '0';
        // last 6, beyond 8 bits
        buf[8] = '\\';
        buf[9] = 'u';
        _entityBuffer = buf;
        return buf;
    }

    protected void _flushBuffer() throws IOException {
        int len = _outputTail - _outputHead;
        if (len > 0) {
            int offset = _outputHead;
            _outputTail = _outputHead = 0;
            _writer.write(_outputBuffer, offset, len);
        }
    }
}
