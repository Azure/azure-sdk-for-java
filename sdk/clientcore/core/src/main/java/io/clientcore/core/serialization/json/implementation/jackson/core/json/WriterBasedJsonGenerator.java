// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.json;

import io.clientcore.core.serialization.json.implementation.jackson.core.Base64Variants;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonFactory;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonGenerationException;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonGenerator;
import io.clientcore.core.serialization.json.implementation.jackson.core.JsonStreamContext;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.CharTypes;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.IOContext;
import io.clientcore.core.serialization.json.implementation.jackson.core.io.NumberOutput;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link JsonGenerator} that outputs JSON content using a {@link Writer}
 * which handles character encoding.
 */
public class WriterBasedJsonGenerator extends JsonGenerator {
    protected final static char[] HEX_CHARS = CharTypes.copyHexChars();

    // // // Constants for validation messages (since 2.6)

    protected final static String WRITE_BINARY = "write a binary value";
    protected final static String WRITE_BOOLEAN = "write a boolean value";
    protected final static String WRITE_NULL = "write a null";
    protected final static String WRITE_NUMBER = "write a number";
    protected final static String WRITE_STRING = "write a string";

    /*
     * /**********************************************************
     * /* Configuration, basic I/O
     * /**********************************************************
     */

    protected final IOContext _ioContext;

    /*
     * /**********************************************************
     * /* Configuration
     * /**********************************************************
     */

    protected final Writer _writer;
    /*
     * /**********************************************************
     * /* Configuration, other
     * /**********************************************************
     */

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

    /*
     * /**********************************************************
     * /* Output buffering
     * /**********************************************************
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
     * Short (14 char) temporary buffer allocated if needed, for constructing
     * escape sequences
     */
    protected char[] _entityBuffer;

    /*
     * /**********************************************************
     * /* State
     * /**********************************************************
     */

    /**
     * Object that keeps track of the current contextual state
     * of the generator.
     */
    protected JsonWriteContext _writeContext;

    public WriterBasedJsonGenerator(IOContext ctxt, int features, Writer w)

    {
        super(features);
        _ioContext = ctxt;
        _writer = w;
        _outputBuffer = ctxt.allocConcatBuffer();
        _outputEnd = _outputBuffer.length;
        _writeContext = JsonWriteContext.createRootContext();
    }

    /*
     * /**********************************************************
     * /* Overridden methods
     * /**********************************************************
     */

    @Override
    public void writeFieldName(String name) throws IOException {
        int status = _writeContext.writeFieldName(name);
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            throw new JsonGenerationException("Can not write a field name, expecting a value", this);
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
        // we know there's room for at least one more char
        _outputBuffer[_outputTail++] = JsonFactory.DEFAULT_QUOTE_CHAR;
        // The beef:
        _writeString(name);
        // and closing quotes; need room for one more char:
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = JsonFactory.DEFAULT_QUOTE_CHAR;
    }

    /*
     * /**********************************************************
     * /* Output method implementations, structural
     * /**********************************************************
     */

    @Override
    public void writeStartArray() throws IOException {
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext();
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '[';
    }

    @Override
    public void writeEndArray() throws IOException {
        if (!_writeContext.inArray()) {
            throw new JsonGenerationException("Current context not Array but " + _writeContext.typeDesc(), this);
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
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '{';
    }

    @Override
    public void writeEndObject() throws IOException {
        if (!_writeContext.inObject()) {
            throw new JsonGenerationException("Current context not Object but " + _writeContext.typeDesc(), this);
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = '}';
        _writeContext = _writeContext.clearAndGetParent();
    }

    /*
     * /**********************************************************
     * /* Output method implementations, textual
     * /**********************************************************
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
        _outputBuffer[_outputTail++] = JsonFactory.DEFAULT_QUOTE_CHAR;
        _writeString(text);
        // And finally, closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = JsonFactory.DEFAULT_QUOTE_CHAR;
    }

    /*
     * /**********************************************************
     * /* Output method implementations, unprocessed ("raw")
     * /**********************************************************
     */

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
     * /**********************************************************
     * /* Output method implementations, base64-encoded binary
     * /**********************************************************
     */

    @Override
    public void writeBinary(byte[] data) throws IOException {
        _verifyValueWrite(WRITE_BINARY);
        // Starting quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = JsonFactory.DEFAULT_QUOTE_CHAR;
        _writeBinary(data, 0, data.length);
        // and closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = JsonFactory.DEFAULT_QUOTE_CHAR;
    }

    /*
     * /**********************************************************
     * /* Output method implementations, primitive
     * /**********************************************************
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

    @Override
    public void writeNumber(double d) throws IOException {
        if (isQuoteNonNumericNumbersEnabled() && !Double.isFinite(d)) {
            writeString(String.valueOf(d));
            return;
        }
        // What is the max length for doubles? 40 chars?
        _verifyValueWrite(WRITE_NUMBER);
        writeRaw(String.valueOf(d));
    }

    @Override
    public void writeNumber(float f) throws IOException {
        if (isQuoteNonNumericNumbersEnabled() && !Float.isFinite(f)) {
            writeString(String.valueOf(f));
            return;
        }
        // What is the max length for floats?
        _verifyValueWrite(WRITE_NUMBER);
        writeRaw(String.valueOf(f));
    }

    private boolean isQuoteNonNumericNumbersEnabled() {
        // With modifications to the original code, there is only one feature left.
        // So, checking for it being enabled is a simple not zero check.
        return _features != 0;
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

    @Override
    public void writeRawValue(String text) throws IOException {
        _verifyValueWrite("write raw value");
        writeRaw(text);
    }

    /*
     * /**********************************************************
     * /* Public API, accessors
     * /**********************************************************
     */

    /**
     * Note: type was co-variant until Jackson 2.7; reverted back to
     * base type in 2.8 to allow for overriding by subtypes that use
     * custom context type.
     */
    @Override
    public JsonStreamContext getOutputContext() {
        return _writeContext;
    }

    /*
     * /**********************************************************
     * /* Implementations for other methods
     * /**********************************************************
     */

    /**
     * Method called before trying to write a value (scalar or structured),
     * to verify that this is legal in current output state, as well as to
     * output separators if and as necessary.
     *
     * @param typeMsg Additional message used for generating exception message
     *   if value output is NOT legal in current generator output state.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *    issue at format layer
     */
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
                writeRaw(" ");
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
     * /**********************************************************
     * /* Low-level output handling
     * /**********************************************************
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
        // 05-Dec-2008, tatu: To add [JACKSON-27], need to close open scopes
        // First: let's see that we still have buffers...
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
        _outputHead = 0;
        _outputTail = 0;

        if (_writer != null) {
            _writer.flush();
        }
        // Internal buffer(s) generator has can now be released as well
        char[] buf = _outputBuffer;
        if (buf != null) {
            _outputBuffer = null;
            _ioContext.releaseConcatBuffer(buf);
        }
    }

    /*
     * /**********************************************************
     * /* Internal methods, low-level writing; text, default
     * /**********************************************************
     */

    private void _writeString(String text) throws IOException {
        /*
         * One check first: if String won't fit in the buffer, let's
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

        _writeString2(len);
    }

    private void _writeString2(final int len) throws IOException {
        // And then we'll need to verify need for escaping etc:
        final int end = _outputTail + len;
        final int[] escCodes = CharTypes.get7BitOutputEscapes();
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
            /*
             * First things first: need to flush the buffer.
             * Inlined, as we don't want to lose tail pointer
             */
            int flushLen = (_outputTail - _outputHead);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, _outputHead, flushLen);
            }
            /*
             * In any case, tail will be the new start, so hopefully
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
            _writeSegment(segmentLen);
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
        final int[] escCodes = CharTypes.get7BitOutputEscapes();
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
            /*
             * First things first: need to flush the buffer.
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
     * /**********************************************************
     * /* Internal methods, low-level writing; binary
     * /**********************************************************
     */

    protected final void _writeBinary(byte[] input, int inputPtr, final int inputEnd) throws IOException {
        // Encoding is by chunks of 3 input, 4 output chars, so:
        int safeInputEnd = inputEnd - 3;
        // Let's also reserve room for possible (and quoted) lf char each round
        int safeOutputEnd = _outputEnd - 6;
        int chunksBeforeLF = Base64Variants.getDefaultVariant().getMaxLineLength() >> 2;

        // Ok, first we loop through all full triplets of data:
        while (inputPtr <= safeInputEnd) {
            if (_outputTail > safeOutputEnd) { // need to flush
                _flushBuffer();
            }
            // First, mash 3 bytes into lsb of 32-bit int
            int b24 = ((int) input[inputPtr++]) << 8;
            b24 |= ((int) input[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) input[inputPtr++]) & 0xFF);
            _outputTail = Base64Variants.getDefaultVariant().encodeBase64Chunk(b24, _outputBuffer, _outputTail);
            if (--chunksBeforeLF <= 0) {
                // note: must quote in JSON value
                _outputBuffer[_outputTail++] = '\\';
                _outputBuffer[_outputTail++] = 'n';
                chunksBeforeLF = Base64Variants.getDefaultVariant().getMaxLineLength() >> 2;
            }
        }

        // And then we may have 1 or 2 leftover bytes to encode
        int inputLeft = inputEnd - inputPtr; // 0, 1 or 2
        if (inputLeft > 0) { // yes, but do we have room for output?
            if (_outputTail > safeOutputEnd) { // don't really need 6 bytes but...
                _flushBuffer();
            }
            int b24 = ((int) input[inputPtr++]) << 16;
            if (inputLeft == 2) {
                b24 |= (((int) input[inputPtr++]) & 0xFF) << 8;
            }
            _outputTail
                = Base64Variants.getDefaultVariant().encodeBase64Partial(b24, inputLeft, _outputBuffer, _outputTail);
        }
    }

    /*
     * /**********************************************************
     * /* Internal methods, low-level writing, other
     * /**********************************************************
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
     * /**********************************************************
     * /* Internal methods, low-level writing, escapes
     * /**********************************************************
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
                buf[++ptr] = HEX_CHARS[hi >> 4];
                buf[++ptr] = HEX_CHARS[hi & 0xF];
                ch &= 0xFF;
            } else {
                buf[++ptr] = '0';
                buf[++ptr] = '0';
            }
            buf[++ptr] = HEX_CHARS[ch >> 4];
            buf[++ptr] = HEX_CHARS[ch & 0xF];
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
            buf[10] = HEX_CHARS[hi >> 4];
            buf[11] = HEX_CHARS[hi & 0xF];
            buf[12] = HEX_CHARS[lo >> 4];
            buf[13] = HEX_CHARS[lo & 0xF];
            _writer.write(buf, 8, 6);
        } else { // We know it's a control char, so only the last 2 chars are non-0
            buf[6] = HEX_CHARS[ch >> 4];
            buf[7] = HEX_CHARS[ch & 0xF];
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
                buffer[ptr++] = HEX_CHARS[hi >> 4];
                buffer[ptr++] = HEX_CHARS[hi & 0xF];
                ch &= 0xFF;
            } else {
                buffer[ptr++] = '0';
                buffer[ptr++] = '0';
            }
            buffer[ptr++] = HEX_CHARS[ch >> 4];
            buffer[ptr] = HEX_CHARS[ch & 0xF];
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
                ent[10] = HEX_CHARS[hi >> 4];
                ent[11] = HEX_CHARS[hi & 0xF];
                ent[12] = HEX_CHARS[lo >> 4];
                ent[13] = HEX_CHARS[lo & 0xF];
                _writer.write(ent, 8, 6);
            } else { // We know it's a control char, so only the last 2 chars are non-0
                ent[6] = HEX_CHARS[ch >> 4];
                ent[7] = HEX_CHARS[ch & 0xF];
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

    protected void _reportCantWriteValueExpectName(String typeMsg) throws IOException {
        throw new JsonGenerationException(
            String.format("Can not %s, expecting field name (context: %s)", typeMsg, _writeContext.typeDesc()), this);
    }
}
