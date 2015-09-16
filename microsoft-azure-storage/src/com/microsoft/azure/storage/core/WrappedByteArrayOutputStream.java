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
import java.io.OutputStream;

/**
 * RESERVED FOR INTERNAL USE.
 * Wraps a user-specified buffer in a stream.
 */
public class WrappedByteArrayOutputStream extends OutputStream {

    final private int startingOffset;
    private byte[] buffer;
    private int offset;

    public WrappedByteArrayOutputStream(final byte[] buffer, final int bufferOffset) {
        this.buffer = buffer;
        this.offset = bufferOffset;
        this.startingOffset = bufferOffset;
    }
    
    public int getPosition() {
        return this.offset - this.startingOffset;
    }
    
    @Override
    public void write(int b) throws IOException {
        if (this.offset == this.buffer.length) {
            throw new IOException(SR.CONTENT_LENGTH_MISMATCH);
        }
        
        this.buffer[this.offset] = (byte) b;
        this.offset++;
    }
}
