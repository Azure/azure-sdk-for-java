// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.*;
import java.net.URL;

/**
 * This is the mid-level abstract base class for {@link Stax2Source}s
 * that refer to a resource in such a way, that an efficient
 * {@link InputStream} or {@link Reader} can be constructed.
 * Additionally, referenced sources also provide the base URI that allows
 * for resolving relative references from within content read from
 * such resources. Typical examples of references are
 * {@link java.net.URL} and {@link java.io.File}: both for which
 * a default implementations exist in this package
 *
 * @see Stax2FileSource
 * @see Stax2URLSource
 */
public abstract class Stax2ReferentialSource extends Stax2Source {
    protected Stax2ReferentialSource() {
    }

    /*
    /////////////////////////////////////////
    // Public API, simple accessors/mutators
    /////////////////////////////////////////
     */

    @Override
    public abstract URL getReference();

    @Override
    public abstract Reader constructReader() throws IOException;

    @Override
    public abstract InputStream constructInputStream() throws IOException;

    /*
    /////////////////////////////////////////
    // Overrides
    /////////////////////////////////////////
     */

    /**
     * Since we can determine a system id from the base URL
     */
    @Override
    public String getSystemId() {
        String sysId = super.getSystemId();
        if (sysId == null) {
            sysId = getReference().toExternalForm();
        }
        return sysId;
    }
}
