// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.*;

/**
 * Simple implementation of {@link Stax2BlockSource} that encapsulates
 * a simple {@link String}.
 */
public class Stax2StringSource extends Stax2BlockSource {
    final String mText;

    public Stax2StringSource(String text) {
        mText = text;
    }

    /*
    /////////////////////////////////////////
    // Implementation of the Public API
    /////////////////////////////////////////
     */

    @Override
    public Reader constructReader() throws IOException {
        return new StringReader(mText);
    }

    @Override
    public InputStream constructInputStream() throws IOException {
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

    public String getText() {
        return mText;
    }
}
