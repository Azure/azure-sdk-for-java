// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
package com.azure.xml.implementation.stax2.io;

import java.io.*;

/**
 * This is the mid-level abstract base class for {@link Stax2Result}s
 * that an be used to write to in-memory (low-level) data structures,
 * such as byte and char arrays, StringBuffers and so forth.
 * The main reason for using such a result object (instead of constructing
 * wrapper Readers or InputStreams) is that concrete implementations
 * usually also allow more direct access to the underlying data, so
 * that stream reader implementations may be able to do more optimal
 * access.
 */
public abstract class Stax2BlockResult extends Stax2Result {
    protected Stax2BlockResult() {
    }

    /*
    /////////////////////////////////////////
    // Public API, simple accessors/mutators
    /////////////////////////////////////////
     */

    @Override
    public abstract Writer constructWriter() throws IOException;

    @Override
    public abstract OutputStream constructOutputStream() throws IOException;
}
