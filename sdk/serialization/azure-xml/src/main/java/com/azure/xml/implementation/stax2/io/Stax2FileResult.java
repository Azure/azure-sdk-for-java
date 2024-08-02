// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.*;

/**
 * Simple implementation of {@link Stax2ReferentialResult}, which refers
 * to the specific file.
 */
public class Stax2FileResult extends Stax2ReferentialResult {
    final File mFile;

    public Stax2FileResult(File f) {
        mFile = f;
    }

    /*
    /////////////////////////////////////////
    // Implementation of the Public API
    /////////////////////////////////////////
     */

    @Override
    public Writer constructWriter() throws IOException {
        String enc = getEncoding();
        if (enc != null && !enc.isEmpty()) {
            return new OutputStreamWriter(constructOutputStream(), enc);
        }
        // Sub-optimal; really shouldn't use the platform default encoding
        return new FileWriter(mFile);
    }

    @Override
    public OutputStream constructOutputStream() throws IOException {
        return new FileOutputStream(mFile);
    }

    /*
    /////////////////////////////////////////
    // Additional API for this Result
    /////////////////////////////////////////
     */

    public File getFile() {
        return mFile;
    }
}
