// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.*;

/**
 * Simple implementation of {@link Stax2BlockSource} that encapsulates
 * a byte array.
 *<p>
 * Note that no copy is made of the passed-in array, and that further
 * there are direct access methods. Thus, although callers are not
 * to modify contents of returned array, this can not be guaranteed;
 * and as such if this would be a problem (security problem for example),
 * caller has to make a copy of the array and pass that copy to the
 * constructor.
 */
public class Stax2ByteArraySource extends Stax2BlockSource {
    private final static String DEFAULT_ENCODING = "UTF-8";

    final byte[] mBuffer;
    final int mStart;
    final int mLength;

    public Stax2ByteArraySource(byte[] buf, int start, int len) {
        mBuffer = buf;
        mStart = start;
        mLength = len;
    }

    /*
    /////////////////////////////////////////
    // Implementation of the Public API
    /////////////////////////////////////////
     */

    /**
     * Note: if encoding is not explicitly defined, UTF-8 is assumed.
     */
    @Override
    public Reader constructReader() throws IOException {
        String enc = getEncoding();
        InputStream in = constructInputStream();
        if (enc == null || enc.isEmpty()) {
            // 11-Nov-2008, TSa: Used to rely on platform default encoding, which
            // doesn't make sense. XML assumes UTF-8 anyway.
            enc = DEFAULT_ENCODING;
        }
        return new InputStreamReader(in, enc);
    }

    @Override
    public InputStream constructInputStream() {
        return new ByteArrayInputStream(mBuffer, mStart, mLength);
    }

    /*
    /////////////////////////////////////////
    // Additional API for this source
    /////////////////////////////////////////
     */

    public byte[] getBuffer() {
        return mBuffer;
    }

    public int getBufferStart() {
        return mStart;
    }

    public int getBufferLength() {
        return mLength;
    }

}
