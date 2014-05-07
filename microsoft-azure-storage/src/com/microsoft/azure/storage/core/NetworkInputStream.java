/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * RESERVED FOR INTERNAL USE.
 */
public class NetworkInputStream extends InputStream {

    private final long expectedLength;

    private final InputStream inputStream;

    private long bytesRead = 0;

    /**
     * Creates a NetworkInputStream and saves its arguments, the input stream and expected length, for later use.
     * 
     * @param stream
     * @param expectedLength
     */
    public NetworkInputStream(InputStream stream, long expectedLength) {
        this.inputStream = stream;
        this.expectedLength = expectedLength;
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = this.inputStream.read(b, off, len);
        if (count > -1) {
            this.bytesRead += count;
        }
        else {
            if (this.bytesRead != this.expectedLength) {
                throw new IOException(SR.CONTENT_LENGTH_MISMATCH);
            }
        }

        return count;
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }
}
