// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.RepeatingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.zip.CRC32;

/**
 * This class in not thread safe and is expected to be used in a single threaded context!
 */
public class CrcInputStream extends InputStream {
    private final static ClientLogger LOGGER = new ClientLogger(CrcInputStream.class);
    private final RepeatingInputStream repeatingInputStream;
    private final CRC32 crc = new CRC32();

    public CrcInputStream(BinaryData source, long size) {
        this.repeatingInputStream = new RepeatingInputStream(source, size);
    }

    @Override
    public int read() {
        int b = repeatingInputStream.read();
        if (b >= 0) {
            crc.update(b);
        }

        return b;
    }

    @Override
    public int read(byte b[], int off, int len) {
        int read = repeatingInputStream.read(b, off, len);
        if (read > 0) {
            crc.update(b, off, read);
        }
        return read;
    }

    public long getCrc() {
        return crc.getValue();
    }

    @Override
    public void close() {
        try {
            repeatingInputStream.close();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
