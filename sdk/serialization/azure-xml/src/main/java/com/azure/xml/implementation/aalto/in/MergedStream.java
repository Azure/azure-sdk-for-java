// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.in;

import java.io.*;

/**
 * Simple {@link InputStream} implementation that is used to "unwind" some
 * data previously read from an input stream; so that as long as some of
 * that data remains, it's returned; but as long as it's read, we'll
 * just use data from the underlying original stream. 
 * This is similar to {@link java.io.PushbackInputStream}, but here there's
 * only one implicit pushback, when instance is constructed.
 */
public final class MergedStream extends InputStream {
    private final ReaderConfig mConfig;

    private final InputStream mIn;

    private byte[] mData;

    private int mPtr;

    private final int mEnd;

    public MergedStream(ReaderConfig cfg, InputStream in, byte[] buf, int start, int end) {
        mConfig = cfg;
        mIn = in;
        mData = buf;
        mPtr = start;
        mEnd = end;
    }

    @Override
    public int available() throws IOException {
        if (mData != null) {
            return mEnd - mPtr;
        }
        return mIn.available();
    }

    @Override
    public void close() throws IOException {
        freeBuffers();
        mIn.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        if (mData == null) {
            mIn.mark(readlimit);
        }
    }

    @Override
    public boolean markSupported() {
        // Only supports marks past the initial rewindable section...
        return (mData == null) && mIn.markSupported();
    }

    @Override
    public int read() throws IOException {
        if (mData != null) {
            int c = mData[mPtr++] & 0xFF;
            if (mPtr >= mEnd) {
                freeBuffers();
            }
            return c;
        }
        return mIn.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (mData != null) {
            int avail = mEnd - mPtr;
            if (len > avail) {
                len = avail;
            }
            System.arraycopy(mData, mPtr, b, off, len);
            mPtr += len;
            if (mPtr >= mEnd) {
                freeBuffers();
            }
            return len;
        }
        return mIn.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (mData == null) {
            mIn.reset();
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long count = 0L;

        if (mData != null) {
            int amount = mEnd - mPtr;

            if (amount > n) { // all in pushed back segment?
                mPtr += (int) n;
                return n;
            }
            freeBuffers();
            count += amount;
            n -= amount;
        }

        if (n > 0) {
            count += mIn.skip(n);
        }
        return count;
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private void freeBuffers() {
        if (mData != null) {
            byte[] data = mData;
            mData = null;
            if (mConfig != null) {
                mConfig.freeFullBBuffer(data);
            }
        }
    }
}
