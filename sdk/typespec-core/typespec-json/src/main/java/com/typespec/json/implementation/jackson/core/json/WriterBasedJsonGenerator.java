// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.json;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.typespec.json.implementation.jackson.core.*;
import com.typespec.json.implementation.jackson.core.io.CharTypes;
import com.typespec.json.implementation.jackson.core.io.CharacterEscapes;
import com.typespec.json.implementation.jackson.core.io.IOContext;
import com.typespec.json.implementation.jackson.core.io.NumberOutput;

/**
 * {@link JsonGenerator} that outputs JSON content using a {@link java.io.Writer}
 * which handles character encoding.
 */
public class WriterBasedJsonGenerator
    extends JsonGeneratorImpl
{
    protected final static int SHORT_WRITE = 32;

    protected final static char[] HEX_CHARS = CharTypes.copyHexChars();

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
     * When custom escapes are used, this member variable is used
     * internally to hold a reference to currently used escape
     */
    protected SerializableString _currentEscape;

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

    @Deprecated // since 2.10
    public WriterBasedJsonGenerator(IOContext ctxt, int features,
            ObjectCodec codec, Writer w) {
        this(ctxt, features, codec, w, JsonFactory.DEFAULT_QUOTE_CHAR);
    }

    // @since 2.10
    public WriterBasedJsonGenerator(IOContext ctxt, int features,
            ObjectCodec codec, Writer w,
            char quoteChar)
    
    {
        super(ctxt, features, codec);
        _writer = w;
        _outputBuffer = ctxt.allocConcatBuffer();
        _outputEnd = _outputBuffer.length;
        _quoteChar = quoteChar;
        if (quoteChar != '"') { // since 2.10
            _outputEscapes = CharTypes.get7BitOutputEscapes(quoteChar);
        }
    }

    /*
    /**********************************************************
    /* Overridden configuration, introspection methods
    /**********************************************************
     */

    @Override
    public Object getOutputTarget() {
        return _writer;
    }

    @Override
    public int getOutputBuffered() {
        // Assuming tail and head are kept but... trust and verify:
        int len = _outputTail - _outputHead;
        return Math.max(0, len);
    }

    // json does allow this so
    @Override
    public boolean canWriteFormattedNumbers() { return true; }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    public void writeFieldName(String name)  throws IOException
    {
        int status = _writeContext.writeFieldName(name);
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        _writeFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
    }

    @Override
    public void writeFieldName(SerializableString name) throws IOException
    {
        // Object is a value, need to verify it's allowed
        int status = _writeContext.writeFieldName(name.getValue());
        if (status == JsonWriteContext.STATUS_EXPECT_VALUE) {
            _reportError("Can not write a field name, expecting a value");
        }
        _writeFieldName(name, (status == JsonWriteContext.STATUS_OK_AFTER_COMMA));
    }

    protected final void _writeFieldName(String name, boolean commaBefore) throws IOException
    {
        if (_cfgPrettyPrinter != null) {
            _writePPFieldName(name, commaBefore);
            return;
        }
        // for fast+std case, need to output up to 2 chars, comma, dquote
        if ((_outputTail + 1) >= _outputEnd) {
            _flushBuffer();
        }
        if (commaBefore) {
            _outputBuffer[_outputTail++] = ',';
        }
        // Alternate mode, in which quoting of field names disabled?
        if (_cfgUnqNames) {
            _writeString(name);
            return;
        }
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

    protected final void _writeFieldName(SerializableString name, boolean commaBefore) throws IOException
    {
        if (_cfgPrettyPrinter != null) {
            _writePPFieldName(name, commaBefore);
            return;
        }
        // for fast+std case, need to output up to 2 chars, comma, dquote
        if ((_outputTail + 1) >= _outputEnd) {
            _flushBuffer();
        }
        if (commaBefore) {
            _outputBuffer[_outputTail++] = ',';
        }
        // Alternate mode, in which quoting of field names disabled?
        if (_cfgUnqNames) {
            final char[] ch = name.asQuotedChars();
            writeRaw(ch, 0, ch.length);
            return;
        }
        // we know there's room for at least one more char
        _outputBuffer[_outputTail++] = _quoteChar;
        // The beef:
        
        int len = name.appendQuoted(_outputBuffer, _outputTail);
        if (len < 0) {
            _writeFieldNameTail(name);
            return;
        }
        _outputTail += len;
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    private final void _writeFieldNameTail(SerializableString name) throws IOException
    {
        final char[] quoted = name.asQuotedChars();
        writeRaw(quoted, 0, quoted.length);
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
    public void writeStartArray() throws IOException
    {
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartArray(this);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '[';
        }
    }

    @Override // since 2.12
    public void writeStartArray(Object currentValue) throws IOException
    {
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext(currentValue);
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartArray(this);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '[';
        }
    }

    @Override // since 2.12
    public void writeStartArray(Object currentValue, int size) throws IOException
    {
        _verifyValueWrite("start an array");
        _writeContext = _writeContext.createChildArrayContext(currentValue);
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartArray(this);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '[';
        }
    }

    @Override
    public void writeEndArray() throws IOException
    {
        if (!_writeContext.inArray()) {
            _reportError("Current context not Array but "+_writeContext.typeDesc());
        }
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeEndArray(this, _writeContext.getEntryCount());
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = ']';
        }
        _writeContext = _writeContext.clearAndGetParent();
    }

    @Override
    public void writeStartObject() throws IOException
    {
        _verifyValueWrite("start an object");
        _writeContext = _writeContext.createChildObjectContext();
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartObject(this);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '{';
        }
    }

    @Override // since 2.8
    public void writeStartObject(Object forValue) throws IOException
    {
        _verifyValueWrite("start an object");
        JsonWriteContext ctxt = _writeContext.createChildObjectContext(forValue);
        _writeContext = ctxt;
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeStartObject(this);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '{';
        }
    }

    @Override
    public void writeEndObject() throws IOException
    {
        if (!_writeContext.inObject()) {
            _reportError("Current context not Object but "+_writeContext.typeDesc());
        }
        if (_cfgPrettyPrinter != null) {
            _cfgPrettyPrinter.writeEndObject(this, _writeContext.getEntryCount());
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '}';
        }
        _writeContext = _writeContext.clearAndGetParent();
    }

    // Specialized version of <code>_writeFieldName</code>, off-lined
    // to keep the "fast path" as simple (and hopefully fast) as possible.
    protected final void _writePPFieldName(String name, boolean commaBefore) throws IOException
    {
        if (commaBefore) {
            _cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            _cfgPrettyPrinter.beforeObjectEntries(this);
        }

        if (_cfgUnqNames) {// non-standard, omit quotes
            _writeString(name);
        } else { 
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = _quoteChar;
            _writeString(name);
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = _quoteChar;
        }
    }

    protected final void _writePPFieldName(SerializableString name, boolean commaBefore) throws IOException
    {
        if (commaBefore) {
            _cfgPrettyPrinter.writeObjectEntrySeparator(this);
        } else {
            _cfgPrettyPrinter.beforeObjectEntries(this);
        }
        final char[] quoted = name.asQuotedChars();
        if (_cfgUnqNames) {// non-standard, omit quotes
            writeRaw(quoted, 0, quoted.length);
        } else {
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = _quoteChar;
            writeRaw(quoted, 0, quoted.length);
            if (_outputTail >= _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = _quoteChar;
        }
    }

    /*
    /**********************************************************
    /* Output method implementations, textual
    /**********************************************************
     */

    @Override
    public void writeString(String text) throws IOException
    {
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

    @Override
    public void writeString(Reader reader, final int len) throws IOException
    {
        _verifyValueWrite(WRITE_STRING);
        if (reader == null) {
            _reportError("null reader");
            return; // just to block warnings by lgtm.com
        }
        int toRead = (len >= 0) ? len : Integer.MAX_VALUE;
        // Add leading quote
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;

        final char[] buf = _allocateCopyBuffer();
        while (toRead > 0) {
            int toReadNow = Math.min(toRead, buf.length);
            int numRead = reader.read(buf, 0, toReadNow);
            if (numRead <= 0) {
                break;
            }
            _writeString(buf, 0, numRead);
            toRead -= numRead;
        }
        // Add trailing quote
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;

        if (toRead > 0 && len >= 0) {
            _reportError("Didn't read enough from reader");
        }
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException
    {
        _verifyValueWrite(WRITE_STRING);
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        _writeString(text, offset, len);
        // And finally, closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    @Override
    public void writeString(SerializableString sstr) throws IOException
    {
        _verifyValueWrite(WRITE_STRING);
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        int len = sstr.appendQuoted(_outputBuffer, _outputTail);
        if (len < 0) {
            _writeString2(sstr);
            return;
        }
        _outputTail += len;
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    private void _writeString2(SerializableString sstr) throws IOException
    {
        // Note: copied from writeRaw:
        char[] text = sstr.asQuotedChars();
        final int len = text.length;
        if (len < SHORT_WRITE) {
            int room = _outputEnd - _outputTail;
            if (len > room) {
                _flushBuffer();
            }
            System.arraycopy(text, 0, _outputBuffer, _outputTail, len);
            _outputTail += len;
        } else {
            _flushBuffer();
            _writer.write(text, 0, len);
        }
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }
    
    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        // could add support for buffering if we really want it...
        _reportUnsupportedOperation();
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        // could add support for buffering if we really want it...
        _reportUnsupportedOperation();
    }
    
    /*
    /**********************************************************
    /* Output method implementations, unprocessed ("raw")
    /**********************************************************
     */

    @Override
    public void writeRaw(String text) throws IOException
    {
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
    public void writeRaw(String text, int start, int len) throws IOException
    {
        // Nothing to check, can just output as is
        int room = _outputEnd - _outputTail;

        if (room < len) {
            _flushBuffer();
            room = _outputEnd - _outputTail;
        }
        // But would it nicely fit in? If yes, it's easy
        if (room >= len) {
            text.getChars(start, start+len, _outputBuffer, _outputTail);
            _outputTail += len;
        } else {            	
            writeRawLong(text.substring(start, start+len));
        }
    }

    // @since 2.1
    @Override
    public void writeRaw(SerializableString text) throws IOException {
        int len = text.appendUnquoted(_outputBuffer, _outputTail);
        if (len < 0) {
            writeRaw(text.getValue());
            return;
        }
        _outputTail += len;
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException
    {
        // Only worth buffering if it's a short write?
        if (len < SHORT_WRITE) {
            int room = _outputEnd - _outputTail;
            if (len > room) {
                _flushBuffer();
            }
            System.arraycopy(text, offset, _outputBuffer, _outputTail, len);
            _outputTail += len;
            return;
        }
        // Otherwise, better just pass through:
        _flushBuffer();
        _writer.write(text, offset, len);
    }

    @Override
    public void writeRaw(char c) throws IOException
    {
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = c;
    }

    private void writeRawLong(String text) throws IOException
    {
        int room = _outputEnd - _outputTail;
        // If not, need to do it by looping
        text.getChars(0, room, _outputBuffer, _outputTail);
        _outputTail += room;
        _flushBuffer();
        int offset = room;
        int len = text.length() - room;

        while (len > _outputEnd) {
            int amount = _outputEnd;
            text.getChars(offset, offset+amount, _outputBuffer, 0);
            _outputHead = 0;
            _outputTail = amount;
            _flushBuffer();
            offset += amount;
            len -= amount;
        }
        // And last piece (at most length of buffer)
        text.getChars(offset, offset+len, _outputBuffer, 0);
        _outputHead = 0;
        _outputTail = len;
    }

    /*
    /**********************************************************
    /* Output method implementations, base64-encoded binary
    /**********************************************************
     */

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite(WRITE_BINARY);
        // Starting quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        _writeBinary(b64variant, data, offset, offset+len);
        // and closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    @Override
    public int writeBinary(Base64Variant b64variant,
            InputStream data, int dataLength)
        throws IOException, JsonGenerationException
    {
        _verifyValueWrite(WRITE_BINARY);
        // Starting quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        byte[] encodingBuffer = _ioContext.allocBase64Buffer();
        int bytes;
        try {
            if (dataLength < 0) { // length unknown
                bytes = _writeBinary(b64variant, data, encodingBuffer);
            } else {
                int missing = _writeBinary(b64variant, data, encodingBuffer, dataLength);
                if (missing > 0) {
                    _reportError("Too few bytes available: missing "+missing+" bytes (out of "+dataLength+")");
                }
                bytes = dataLength;
            }
        } finally {
            _ioContext.releaseBase64Buffer(encodingBuffer);
        }
        // and closing quotes
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        return bytes;
    }
    
    /*
    /**********************************************************
    /* Output method implementations, primitive
    /**********************************************************
     */

    @Override
    public void writeNumber(short s) throws IOException
    {
        _verifyValueWrite(WRITE_NUMBER);
        if (_cfgNumbersAsStrings) {
            _writeQuotedShort(s);
            return;
        }
        // up to 5 digits and possible minus sign
        if ((_outputTail + 6) >= _outputEnd) {
            _flushBuffer();
        }
        _outputTail = NumberOutput.outputInt(s, _outputBuffer, _outputTail);
    }

    private void _writeQuotedShort(short s) throws IOException {
        if ((_outputTail + 8) >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        _outputTail = NumberOutput.outputInt(s, _outputBuffer, _outputTail);
        _outputBuffer[_outputTail++] = _quoteChar;
    }    

    @Override
    public void writeNumber(int i) throws IOException
    {
        _verifyValueWrite(WRITE_NUMBER);
        if (_cfgNumbersAsStrings) {
            _writeQuotedInt(i);
            return;
        }
        // up to 10 digits and possible minus sign
        if ((_outputTail + 11) >= _outputEnd) {
            _flushBuffer();
        }
        _outputTail = NumberOutput.outputInt(i, _outputBuffer, _outputTail);
    }

    private void _writeQuotedInt(int i) throws IOException {
        if ((_outputTail + 13) >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        _outputTail = NumberOutput.outputInt(i, _outputBuffer, _outputTail);
        _outputBuffer[_outputTail++] = _quoteChar;
    }    

    @Override
    public void writeNumber(long l) throws IOException
    {
        _verifyValueWrite(WRITE_NUMBER);
        if (_cfgNumbersAsStrings) {
            _writeQuotedLong(l);
            return;
        }
        if ((_outputTail + 21) >= _outputEnd) {
            // up to 20 digits, minus sign
            _flushBuffer();
        }
        _outputTail = NumberOutput.outputLong(l, _outputBuffer, _outputTail);
    }

    private void _writeQuotedLong(long l) throws IOException {
        if ((_outputTail + 23) >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        _outputTail = NumberOutput.outputLong(l, _outputBuffer, _outputTail);
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    // !!! 05-Aug-2008, tatus: Any ways to optimize these?

    @Override
    public void writeNumber(BigInteger value) throws IOException
    {
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
        } else if (_cfgNumbersAsStrings) {
            _writeQuotedRaw(value.toString());
        } else {
            writeRaw(value.toString());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void writeNumber(double d) throws IOException
    {
        if (_cfgNumbersAsStrings ||
                (NumberOutput.notFinite(d) && isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
            writeString(String.valueOf(d));
            return;
        }
        // What is the max length for doubles? 40 chars?
        _verifyValueWrite(WRITE_NUMBER);
        writeRaw(String.valueOf(d));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void writeNumber(float f) throws IOException
    {
        if (_cfgNumbersAsStrings ||
                (NumberOutput.notFinite(f) && isEnabled(Feature.QUOTE_NON_NUMERIC_NUMBERS))) {
            writeString(String.valueOf(f));
            return;
        }
        // What is the max length for floats?
        _verifyValueWrite(WRITE_NUMBER);
        writeRaw(String.valueOf(f));
    }

    @Override
    public void writeNumber(BigDecimal value) throws IOException
    {
        // Don't really know max length for big decimal, no point checking
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
        } else  if (_cfgNumbersAsStrings) {
            _writeQuotedRaw(_asString(value));
        } else {
            writeRaw(_asString(value));
        }
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException
    {
        _verifyValueWrite(WRITE_NUMBER);
        if (encodedValue == null) {
            _writeNull();
        } else if (_cfgNumbersAsStrings) {
            _writeQuotedRaw(encodedValue);
        } else {
            writeRaw(encodedValue);
        }
    }

    @Override
    public void writeNumber(char[] encodedValueBuffer, int offset, int length) throws IOException {
        _verifyValueWrite(WRITE_NUMBER);
        if (_cfgNumbersAsStrings) {
            _writeQuotedRaw(encodedValueBuffer, offset, length);
        } else {
            writeRaw(encodedValueBuffer, offset, length);
        }
    }

    private void _writeQuotedRaw(String value) throws IOException
    {
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        writeRaw(value);
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    private void _writeQuotedRaw(char[] text, int offset, int length) throws IOException
    {
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
        writeRaw(text, offset, length);
        if (_outputTail >= _outputEnd) {
            _flushBuffer();
        }
        _outputBuffer[_outputTail++] = _quoteChar;
    }

    @Override
    public void writeBoolean(boolean state) throws IOException
    {
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
        _outputTail = ptr+1;
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
    protected final void _verifyValueWrite(String typeMsg) throws IOException
    {
        final int status = _writeContext.writeValue();
        if (_cfgPrettyPrinter != null) {
            // Otherwise, pretty printer knows what to do...
            _verifyPrettyValueWrite(typeMsg, status);
            return;
        }
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
            if (_rootValueSeparator != null) {
                writeRaw(_rootValueSeparator.getValue());
            }
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
    public void flush() throws IOException
    {
        _flushBuffer();
        if (_writer != null) {
            if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                _writer.flush();
            }
        }
    }

    @Override
    public void close() throws IOException
    {
        super.close();

        // 05-Dec-2008, tatu: To add [JACKSON-27], need to close open scopes
        // First: let's see that we still have buffers...
        if (_outputBuffer != null
            && isEnabled(Feature.AUTO_CLOSE_JSON_CONTENT)) {
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

        /* 25-Nov-2008, tatus: As per [JACKSON-16] we are not to call close()
         *   on the underlying Reader, unless we "own" it, or auto-closing
         *   feature is enabled.
         *   One downside: when using UTF8Writer, underlying buffer(s)
         *   may not be properly recycled if we don't close the writer.
         */
        if (_writer != null) {
            if (_ioContext.isResourceManaged() || isEnabled(Feature.AUTO_CLOSE_TARGET)) {
                _writer.close();
            } else  if (isEnabled(Feature.FLUSH_PASSED_TO_STREAM)) {
                // If we can't close it, we should at least flush
                _writer.flush();
            }
        }
        // Internal buffer(s) generator has can now be released as well
        _releaseBuffers();
    }

    @Override
    protected void _releaseBuffers()
    {
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

    private void _writeString(String text) throws IOException
    {
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

        if (_characterEscapes != null) {
            _writeStringCustom(len);
        } else if (_maximumNonEscapedChar != 0) {
            _writeStringASCII(len, _maximumNonEscapedChar);
        } else {
            _writeString2(len);
        }
    }

    private void _writeString2(final int len) throws IOException
    {
        // And then we'll need to verify need for escaping etc:
        final int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;

        output_loop:
        while (_outputTail < end) {
            // Fast loop for chars not needing escaping
            escape_loop:
            while (true) {
                char c = _outputBuffer[_outputTail];
                if (c < escLen && escCodes[c] != 0) {
                    break escape_loop;
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
    private void _writeLongString(String text) throws IOException
    {
        // First things first: let's flush the buffer to get some more room
        _flushBuffer();

        // Then we can write 
        final int textLen = text.length();
        int offset = 0;
        do {
            int max = _outputEnd;
            int segmentLen = ((offset + max) > textLen)
                ? (textLen - offset) : max;
            text.getChars(offset, offset+segmentLen, _outputBuffer, 0);
            if (_characterEscapes != null) {
                _writeSegmentCustom(segmentLen);
            } else if (_maximumNonEscapedChar != 0) {
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
    private void _writeSegment(int end) throws IOException
    {
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;
    
        int ptr = 0;
        int start = ptr;

        output_loop:
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
                    break output_loop;
                }
            }
            ++ptr;
            // So; either try to prepend (most likely), or write directly:
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCodes[c]);
        }
    }
    
    /**
     * This method called when the string content is already in
     * a char buffer, and need not be copied for processing.
     */
    private void _writeString(char[] text, int offset, int len) throws IOException
    {
        if (_characterEscapes != null) {
            _writeStringCustom(text, offset, len);
            return;
        }
        if (_maximumNonEscapedChar != 0) {
            _writeStringASCII(text, offset, len, _maximumNonEscapedChar);
            return;
        }

        // Let's just find longest spans of non-escapable content, and for
        // each see if it makes sense to copy them, or write through

        len += offset; // -> len marks the end from now on
        final int[] escCodes = _outputEscapes;
        final int escLen = escCodes.length;
        while (offset < len) {
            int start = offset;

            while (true) {
                char c = text[offset];
                if (c < escLen && escCodes[c] != 0) {
                    break;
                }
                if (++offset >= len) {
                    break;
                }
            }

            // Short span? Better just copy it to buffer first:
            int newAmount = offset - start;
            if (newAmount < SHORT_WRITE) {
                // Note: let's reserve room for escaped char (up to 6 chars)
                if ((_outputTail + newAmount) > _outputEnd) {
                    _flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, _outputBuffer, _outputTail, newAmount);
                    _outputTail += newAmount;
                }
            } else { // Nope: better just write through
                _flushBuffer();
                _writer.write(text, start, newAmount);
            }
            // Was this the end?
            if (offset >= len) { // yup
                break;
            }
            // Nope, need to escape the char.
            char c = text[offset++];
            _appendCharacterEscape(c, escCodes[c]);          
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
    private void _writeStringASCII(final int len, final int maxNonEscaped)
        throws IOException, JsonGenerationException
    {
        // And then we'll need to verify need for escaping etc:
        int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        int escCode = 0;
        
        output_loop:
        while (_outputTail < end) {
            char c;
            // Fast loop for chars not needing escaping
            escape_loop:
            while (true) {
                c = _outputBuffer[_outputTail];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break escape_loop;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break escape_loop;
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

    private void _writeSegmentASCII(int end, final int maxNonEscaped)
        throws IOException, JsonGenerationException
    {
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
    
        int ptr = 0;
        int escCode = 0;
        int start = ptr;
    
        output_loop:
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
                    break output_loop;
                }
            }
            ++ptr;
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCode);
        }
    }

    private void _writeStringASCII(char[] text, int offset, int len,
            final int maxNonEscaped)
        throws IOException, JsonGenerationException
    {
        len += offset; // -> len marks the end from now on
        final int[] escCodes = _outputEscapes;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);

        int escCode = 0;
        
        while (offset < len) {
            int start = offset;
            char c;
            
            while (true) {
                c = text[offset];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                }
                if (++offset >= len) {
                    break;
                }
            }

            // Short span? Better just copy it to buffer first:
            int newAmount = offset - start;
            if (newAmount < SHORT_WRITE) {
                // Note: let's reserve room for escaped char (up to 6 chars)
                if ((_outputTail + newAmount) > _outputEnd) {
                    _flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, _outputBuffer, _outputTail, newAmount);
                    _outputTail += newAmount;
                }
            } else { // Nope: better just write through
                _flushBuffer();
                _writer.write(text, start, newAmount);
            }
            // Was this the end?
            if (offset >= len) { // yup
                break;
            }
            // Nope, need to escape the char.
            ++offset;
            _appendCharacterEscape(c, escCode);
        }
    }

    /*
    /**********************************************************
    /* Internal methods, low-level writing, text segment
    /* with custom escaping (possibly coupling with ASCII limits)
    /**********************************************************
     */

    /* Same as "_writeString2()", except needs additional escaping
     * for subset of characters
     */
    private void _writeStringCustom(final int len)
        throws IOException, JsonGenerationException
    {
        // And then we'll need to verify need for escaping etc:
        int end = _outputTail + len;
        final int[] escCodes = _outputEscapes;
        final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        int escCode = 0;
        final CharacterEscapes customEscapes = _characterEscapes;

        output_loop:
        while (_outputTail < end) {
            char c;
            // Fast loop for chars not needing escaping
            escape_loop:
            while (true) {
                c = _outputBuffer[_outputTail];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break escape_loop;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break escape_loop;
                } else {
                    if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = CharacterEscapes.ESCAPE_CUSTOM;
                        break escape_loop;
                    }
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

    private void _writeSegmentCustom(int end)
        throws IOException, JsonGenerationException
    {
        final int[] escCodes = _outputEscapes;
        final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        final CharacterEscapes customEscapes = _characterEscapes;
    
        int ptr = 0;
        int escCode = 0;
        int start = ptr;
    
        output_loop:
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
                } else {
                    if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = CharacterEscapes.ESCAPE_CUSTOM;
                        break;
                    }
                }
                if (++ptr >= end) {
                    break;
                }
            }
            int flushLen = (ptr - start);
            if (flushLen > 0) {
                _writer.write(_outputBuffer, start, flushLen);
                if (ptr >= end) {
                    break output_loop;
                }
            }
            ++ptr;
            start = _prependOrWriteCharacterEscape(_outputBuffer, ptr, end, c, escCode);
        }
    }

    private void _writeStringCustom(char[] text, int offset, int len)
        throws IOException, JsonGenerationException
    {
        len += offset; // -> len marks the end from now on
        final int[] escCodes = _outputEscapes;
        final int maxNonEscaped = (_maximumNonEscapedChar < 1) ? 0xFFFF : _maximumNonEscapedChar;
        final int escLimit = Math.min(escCodes.length, maxNonEscaped+1);
        final CharacterEscapes customEscapes = _characterEscapes;

        int escCode = 0;
        
        while (offset < len) {
            int start = offset;
            char c;
            
            while (true) {
                c = text[offset];
                if (c < escLimit) {
                    escCode = escCodes[c];
                    if (escCode != 0) {
                        break;
                    }
                } else if (c > maxNonEscaped) {
                    escCode = CharacterEscapes.ESCAPE_STANDARD;
                    break;
                } else {
                    if ((_currentEscape = customEscapes.getEscapeSequence(c)) != null) {
                        escCode = CharacterEscapes.ESCAPE_CUSTOM;
                        break;
                    }
                }
                if (++offset >= len) {
                    break;
                }
            }
    
            // Short span? Better just copy it to buffer first:
            int newAmount = offset - start;
            if (newAmount < SHORT_WRITE) {
                // Note: let's reserve room for escaped char (up to 6 chars)
                if ((_outputTail + newAmount) > _outputEnd) {
                    _flushBuffer();
                }
                if (newAmount > 0) {
                    System.arraycopy(text, start, _outputBuffer, _outputTail, newAmount);
                    _outputTail += newAmount;
                }
            } else { // Nope: better just write through
                _flushBuffer();
                _writer.write(text, start, newAmount);
            }
            // Was this the end?
            if (offset >= len) { // yup
                break;
            }
            // Nope, need to escape the char.
            ++offset;
            _appendCharacterEscape(c, escCode);
        }
    }
    
    /*
    /**********************************************************
    /* Internal methods, low-level writing; binary
    /**********************************************************
     */
    
    protected final void _writeBinary(Base64Variant b64variant, byte[] input, int inputPtr, final int inputEnd)
        throws IOException, JsonGenerationException
    {
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
            int b24 = ((int) input[inputPtr++]) << 8;
            b24 |= ((int) input[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) input[inputPtr++]) & 0xFF);
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
            int b24 = ((int) input[inputPtr++]) << 16;
            if (inputLeft == 2) {
                b24 |= (((int) input[inputPtr++]) & 0xFF) << 8;
            }
            _outputTail = b64variant.encodeBase64Partial(b24, inputLeft, _outputBuffer, _outputTail);
        }
    }

    // write-method called when length is definitely known
    protected final int _writeBinary(Base64Variant b64variant,
            InputStream data, byte[] readBuffer, int bytesLeft)
        throws IOException, JsonGenerationException
    {
        int inputPtr = 0;
        int inputEnd = 0;
        int lastFullOffset = -3;       
        
        // Let's also reserve room for possible (and quoted) lf char each round
        int safeOutputEnd = _outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;

        while (bytesLeft > 2) { // main loop for full triplets
            if (inputPtr > lastFullOffset) {
                inputEnd = _readMore(data, readBuffer, inputPtr, inputEnd, bytesLeft);
                inputPtr = 0;
                if (inputEnd < 3) { // required to try to read to have at least 3 bytes
                    break;
                }
                lastFullOffset = inputEnd-3;
            }
            if (_outputTail > safeOutputEnd) { // need to flush
                _flushBuffer();
            }
            int b24 = ((int) readBuffer[inputPtr++]) << 8;
            b24 |= ((int) readBuffer[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) readBuffer[inputPtr++]) & 0xFF);
            bytesLeft -= 3;
            _outputTail = b64variant.encodeBase64Chunk(b24, _outputBuffer, _outputTail);
            if (--chunksBeforeLF <= 0) {
                _outputBuffer[_outputTail++] = '\\';
                _outputBuffer[_outputTail++] = 'n';
                chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
            }
        }
        
        // And then we may have 1 or 2 leftover bytes to encode
        if (bytesLeft > 0) {
            inputEnd = _readMore(data, readBuffer, inputPtr, inputEnd, bytesLeft);
            inputPtr = 0;
            if (inputEnd > 0) { // yes, but do we have room for output?
                if (_outputTail > safeOutputEnd) { // don't really need 6 bytes but...
                    _flushBuffer();
                }
                int b24 = ((int) readBuffer[inputPtr++]) << 16;
                int amount;
                if (inputPtr < inputEnd) {
                    b24 |= (((int) readBuffer[inputPtr]) & 0xFF) << 8;
                    amount = 2;
                } else {
                    amount = 1;
                }
                _outputTail = b64variant.encodeBase64Partial(b24, amount, _outputBuffer, _outputTail);
                bytesLeft -= amount;
            }
        }
        return bytesLeft;
    }
    
    // write method when length is unknown
    protected final int _writeBinary(Base64Variant b64variant,
            InputStream data, byte[] readBuffer)
        throws IOException, JsonGenerationException
    {
        int inputPtr = 0;
        int inputEnd = 0;
        int lastFullOffset = -3;
        int bytesDone = 0;
        
        // Let's also reserve room for possible (and quoted) LF char each round
        int safeOutputEnd = _outputEnd - 6;
        int chunksBeforeLF = b64variant.getMaxLineLength() >> 2;

        // Ok, first we loop through all full triplets of data:
        while (true) {
            if (inputPtr > lastFullOffset) { // need to load more
                inputEnd = _readMore(data, readBuffer, inputPtr, inputEnd, readBuffer.length);
                inputPtr = 0;
                if (inputEnd < 3) { // required to try to read to have at least 3 bytes
                    break;
                }
                lastFullOffset = inputEnd-3;
            }
            if (_outputTail > safeOutputEnd) { // need to flush
                _flushBuffer();
            }
            // First, mash 3 bytes into lsb of 32-bit int
            int b24 = ((int) readBuffer[inputPtr++]) << 8;
            b24 |= ((int) readBuffer[inputPtr++]) & 0xFF;
            b24 = (b24 << 8) | (((int) readBuffer[inputPtr++]) & 0xFF);
            bytesDone += 3;
            _outputTail = b64variant.encodeBase64Chunk(b24, _outputBuffer, _outputTail);
            if (--chunksBeforeLF <= 0) {
                _outputBuffer[_outputTail++] = '\\';
                _outputBuffer[_outputTail++] = 'n';
                chunksBeforeLF = b64variant.getMaxLineLength() >> 2;
            }
        }

        // And then we may have 1 or 2 leftover bytes to encode
        if (inputPtr < inputEnd) { // yes, but do we have room for output?
            if (_outputTail > safeOutputEnd) { // don't really need 6 bytes but...
                _flushBuffer();
            }
            int b24 = ((int) readBuffer[inputPtr++]) << 16;
            int amount = 1;
            if (inputPtr < inputEnd) {
                b24 |= (((int) readBuffer[inputPtr]) & 0xFF) << 8;
                amount = 2;
            }
            bytesDone += amount;
            _outputTail = b64variant.encodeBase64Partial(b24, amount, _outputBuffer, _outputTail);
        }
        return bytesDone;
    }
    
    private int _readMore(InputStream in,
            byte[] readBuffer, int inputPtr, int inputEnd,
            int maxRead) throws IOException
    {
        // anything to shift to front?
        int i = 0;
        while (inputPtr < inputEnd) {
            readBuffer[i++]  = readBuffer[inputPtr++];
        }
        inputPtr = 0;
        inputEnd = i;
        maxRead = Math.min(maxRead, readBuffer.length);
        
        do {
            int length = maxRead - inputEnd;
            if (length == 0) {
                break;
            }
            int count = in.read(readBuffer, inputEnd, length);            
            if (count < 0) {
                return inputEnd;
            }
            inputEnd += count;
        } while (inputEnd < 3);
        return inputEnd;
    }
    
    /*
    /**********************************************************
    /* Internal methods, low-level writing, other
    /**********************************************************
     */
    
    private final void _writeNull() throws IOException
    {
        if ((_outputTail + 4) >= _outputEnd) {
            _flushBuffer();
        }
        int ptr = _outputTail;
        char[] buf = _outputBuffer;
        buf[ptr] = 'n';
        buf[++ptr] = 'u';
        buf[++ptr] = 'l';
        buf[++ptr] = 'l';
        _outputTail = ptr+1;
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
    private void _prependOrWriteCharacterEscape(char ch, int escCode)
        throws IOException, JsonGenerationException
    {
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
        if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
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
            return;
        }
        String escape;

        if (_currentEscape == null) {
            escape = _characterEscapes.getEscapeSequence(ch).getValue();
        } else {
            escape = _currentEscape.getValue();
            _currentEscape = null;
        }
        int len = escape.length();
        if (_outputTail >= len) { // fits in, prepend
            int ptr = _outputTail - len;
            _outputHead = ptr;
            escape.getChars(0, len, _outputBuffer, ptr);
            return;
        }
        // won't fit, write separately
        _outputHead = _outputTail;
        _writer.write(escape);
    }

    /**
     * Method called to try to either prepend character escape at front of
     * given buffer; or if not possible, to write it out directly.
     * 
     * @return Pointer to start of prepended entity (if prepended); or 'ptr'
     *   if not.
     */
    private int _prependOrWriteCharacterEscape(char[] buffer, int ptr, int end,
            char ch, int escCode)
        throws IOException, JsonGenerationException
    {
        if (escCode >= 0) { // \\N (2 char)
            if (ptr > 1 && ptr < end) { // fits, just prepend
                ptr -= 2;
                buffer[ptr] = '\\';
                buffer[ptr+1] = (char) escCode;
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
        if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
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
        String escape;
        if (_currentEscape == null) {
            escape = _characterEscapes.getEscapeSequence(ch).getValue();
        } else {
            escape = _currentEscape.getValue();
            _currentEscape = null;
        }
        int len = escape.length();
        if (ptr >= len && ptr < end) { // fits in, prepend
            ptr -= len;
            escape.getChars(0, len, buffer, ptr);
        } else { // won't fit, write separately
            _writer.write(escape);
        }
        return ptr;
    }

    /**
     * Method called to append escape sequence for given character, at the
     * end of standard output buffer; or if not possible, write out directly.
     */
    private void _appendCharacterEscape(char ch, int escCode)
        throws IOException, JsonGenerationException
    {
        if (escCode >= 0) { // \\N (2 char)
            if ((_outputTail + 2) > _outputEnd) {
                _flushBuffer();
            }
            _outputBuffer[_outputTail++] = '\\';
            _outputBuffer[_outputTail++] = (char) escCode;
            return;
        }
        if (escCode != CharacterEscapes.ESCAPE_CUSTOM) { // std, \\uXXXX
            if ((_outputTail + 5) >= _outputEnd) {
                _flushBuffer();
            }
            int ptr = _outputTail;
            char[] buf = _outputBuffer;
            buf[ptr++] = '\\';
            buf[ptr++] = 'u';
            // We know it's a control char, so only the last 2 chars are non-0
            if (ch > 0xFF) { // beyond 8 bytes
                int hi = (ch >> 8) & 0xFF;
                buf[ptr++] = HEX_CHARS[hi >> 4];
                buf[ptr++] = HEX_CHARS[hi & 0xF];
                ch &= 0xFF;
            } else {
                buf[ptr++] = '0';
                buf[ptr++] = '0';
            }
            buf[ptr++] = HEX_CHARS[ch >> 4];
            buf[ptr++] = HEX_CHARS[ch & 0xF];
            _outputTail = ptr;
            return;
        }
        String escape;
        if (_currentEscape == null) {
            escape = _characterEscapes.getEscapeSequence(ch).getValue();
        } else {
            escape = _currentEscape.getValue();
            _currentEscape = null;
        }
        int len = escape.length();
        if ((_outputTail + len) > _outputEnd) {
            _flushBuffer();
            if (len > _outputEnd) { // very very long escape; unlikely but theoretically possible
                _writer.write(escape);
                return;
            }
        }
        escape.getChars(0, len, _outputBuffer, _outputTail);
        _outputTail += len;
    }
    
    private char[] _allocateEntityBuffer()
    {
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

    /**
     * @since 2.9
     */
    private char[] _allocateCopyBuffer() {
        if (_copyBuffer == null) {
            _copyBuffer = _ioContext.allocNameCopyBuffer(2000);
        }
        return _copyBuffer;
    }

    protected void _flushBuffer() throws IOException
    {
        int len = _outputTail - _outputHead;
        if (len > 0) {
            int offset = _outputHead;
            _outputTail = _outputHead = 0;
            _writer.write(_outputBuffer, offset, len);
        }
    }
}
