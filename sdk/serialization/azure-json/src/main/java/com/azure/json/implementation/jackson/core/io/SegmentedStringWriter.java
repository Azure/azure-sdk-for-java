// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.io;

import java.io.*;

import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import com.azure.json.implementation.jackson.core.util.TextBuffer;

/**
 * Efficient alternative to {@link StringWriter}, based on using segmented
 * internal buffer. Initial input buffer is also recyclable.
 *<p>
 * This class is most useful when serializing JSON content as a String:
 * if so, instance of this class can be given as the writer to
 * <code>JsonGenerator</code>.
 */
public final class SegmentedStringWriter extends Writer implements BufferRecycler.Gettable {
    final private TextBuffer _buffer;

    public SegmentedStringWriter(BufferRecycler br) {
        super();
        _buffer = new TextBuffer(br);
    }

    /*
    /**********************************************************************
    /* BufferRecycler.Gettable implementation
    /**********************************************************************
     */

    @Override
    public BufferRecycler bufferRecycler() {
        return _buffer.bufferRecycler();
    }

    /*
    /**********************************************************************
    /* java.io.Writer implementation
    /**********************************************************************
     */

    @Override
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        String str = csq.toString();
        _buffer.append(str, 0, str.length());
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        String str = csq.subSequence(start, end).toString();
        _buffer.append(str, 0, str.length());
        return this;
    }

    @Override
    public void close() {
    } // NOP

    @Override
    public void flush() {
    } // NOP

    @Override
    public void write(char[] cbuf) throws IOException {
        _buffer.append(cbuf, 0, cbuf.length);
    }

    @Override

    public void write(char[] cbuf, int off, int len) throws IOException {
        _buffer.append(cbuf, off, len);
    }

    @Override
    public void write(int c) throws IOException {
        _buffer.append((char) c);
    }

    @Override
    public void write(String str) throws IOException {
        _buffer.append(str, 0, str.length());
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        _buffer.append(str, off, len);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

}
