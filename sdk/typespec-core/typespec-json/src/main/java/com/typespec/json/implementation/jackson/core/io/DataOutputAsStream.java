// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.io;

import java.io.*;

/**
 * Helper class to support use of {@link DataOutput} for output, directly,
 * without caller having to provide for implementation.
 *
 * @since 2.8
 */
public class DataOutputAsStream extends OutputStream
{
    protected final DataOutput _output;

    public DataOutputAsStream(DataOutput out) {
        super();
        _output = out;
    }

    @Override
    public void write(int b) throws IOException {
        _output.write(b);
    }

    @Override
    public void write(byte b[]) throws IOException {
        _output.write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int offset, int length) throws IOException {
        _output.write(b, offset, length);
    }

    // These are no-ops, base class impl works fine

    /*
    @Override
    public void flush() throws IOException { }

    @Override
    public void close() throws IOException { }
    */
}
