// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.io;

import java.io.*;

/**
 * Simple {@link InputStream} implementation that is used to "unwind" some
 * data previously read from an input stream; so that as long as some of
 * that data remains, it's returned; but as long as it's read, we'll
 * just use data from the underlying original stream. 
 * This is similar to {@link java.io.PushbackInputStream}, but here there's
 * only one implicit pushback, when instance is constructed.
 */
public final class MergedStream extends InputStream
{
    final private IOContext _ctxt;

    final private InputStream _in;

    private byte[] _b;

    private int _ptr;

    final private int _end;

    public MergedStream(IOContext ctxt, InputStream in, byte[] buf, int start, int end) {
        _ctxt = ctxt;
        _in = in;
        _b = buf;
        _ptr = start;
        _end = end;
    }

    @Override
    public int available() throws IOException {
        if (_b != null) {
            return _end - _ptr;
        }
        return _in.available();
    }

    @Override public void close() throws IOException {
        _free();
        _in.close();
    }

    @Override public synchronized void mark(int readlimit) {
        if (_b == null) { _in.mark(readlimit); }
    }
    
    @Override public boolean markSupported() {
        // Only supports marks past the initial rewindable section...
        return (_b == null) && _in.markSupported();
    }
    
    @Override public int read() throws IOException {
        if (_b != null) {
            int c = _b[_ptr++] & 0xFF;
            if (_ptr >= _end) {
                _free();
            }
            return c;
        }
        return _in.read();
    }
    
    @Override public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (_b != null) {
            int avail = _end - _ptr;
            if (len > avail) {
                len = avail;
            }
            System.arraycopy(_b, _ptr, b, off, len);
            _ptr += len;
            if (_ptr >= _end) {
                _free();
            }
            return len;
        }
        return _in.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (_b == null) { _in.reset(); }
    }

    @Override
    public long skip(long n) throws IOException {
        long count = 0L;

        if (_b != null) {
            int amount = _end - _ptr;

            if (amount > n) { // all in pushed back segment?
                _ptr += (int) n;
                return n;
            }
            _free();
            count += amount;
            n -= amount;
        }

        if (n > 0) { count += _in.skip(n); }
        return count;
    }

    private void _free() {
        byte[] buf = _b;
        if (buf != null) {
            _b = null;
            if (_ctxt != null) {
                _ctxt.releaseReadIOBuffer(buf);
            }
        }
    }
}
