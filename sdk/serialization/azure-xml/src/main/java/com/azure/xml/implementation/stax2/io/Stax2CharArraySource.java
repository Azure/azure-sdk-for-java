// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.*;

/**
 * Simple implementation of {@link Stax2BlockSource} that encapsulates
 * an char array.
 *<p>
 * Note that no copy is made of the passed-in array, and that further
 * there are direct access methods. Thus, although callers are not
 * to modify contents of returned array, this can not be guaranteed;
 * and as such if this would be a problem (security problem for example),
 * caller has to make a copy of the array and pass that copy to the
 * constructor.
 */
public class Stax2CharArraySource extends Stax2BlockSource {
    final char[] mBuffer;
    final int mStart;
    final int mLength;

    public Stax2CharArraySource(char[] buf, int start, int len) {
        mBuffer = buf;
        mStart = start;
        mLength = len;
    }

    /*
    /////////////////////////////////////////
    // Implementation of the Public API
    /////////////////////////////////////////
     */

    @Override
    public Reader constructReader() {
        return new CharArrayReader(mBuffer, mStart, mLength);
    }

    @Override
    public InputStream constructInputStream() {
        /* No obvious/easy way; if caller really wants an InputStream, it
         * can get a Reader, add an encoders, and so on.
         */
        return null;
    }

    /*
    /////////////////////////////////////////
    // Additional API for this source
    /////////////////////////////////////////
     */

    public char[] getBuffer() {
        return mBuffer;
    }

    public int getBufferStart() {
        return mStart;
    }

    public int getBufferLength() {
        return mLength;
    }
}
