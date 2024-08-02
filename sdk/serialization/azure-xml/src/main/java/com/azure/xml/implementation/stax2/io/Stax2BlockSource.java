// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.*;
import java.net.URL;

/**
 * This is the mid-level abstract base class for {@link Stax2Source}s
 * that an be used to access fixed-length in-memory data sources, such
 * as byte and char arrays, Strings, StringBuffers and so forth.
 * The main reason for using such a source object (instead of constructing
 * wrapper Readers or InputStreams) is that concrete implementations
 * usually also allow more direct access to the underlying data, so
 * that stream reader implementations may be able to do more optimal
 * access.
 */
public abstract class Stax2BlockSource extends Stax2Source {
    protected Stax2BlockSource() {
    }

    /*
    /////////////////////////////////////////
    // Public API, simple accessors/mutators
    /////////////////////////////////////////
     */

    /**
     * Usually there is no way to refer to the underlying data source,
     * since they are in-memory data structures. Because of this, the
     * base implementation just returns null.
     */
    @Override
    public URL getReference() {
        return null;
    }

    @Override
    public abstract Reader constructReader() throws IOException;

    @Override
    public abstract InputStream constructInputStream() throws IOException;
}
