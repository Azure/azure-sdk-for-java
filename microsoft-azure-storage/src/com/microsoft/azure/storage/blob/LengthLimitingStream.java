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
package com.microsoft.azure.storage.blob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Utility;

public class LengthLimitingStream extends BlobOutputStream {

    private final OutputStream wrappedStream;
    private long startOffset;
    private Long endOffset;
    private long position;
    private Long length;
    
    public LengthLimitingStream(OutputStream wrappedStream, long start, Long length)
    {
        this.wrappedStream = wrappedStream;
        this.startOffset = start;
        this.length = length;
        if (length != null)
        {
            this.endOffset = this.startOffset + (this.length - 1);
        }
    }
    
    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        // Discard bytes at the beginning if required.
        if (this.position < this.startOffset)
        {
            int discardBytes = (int)Math.min(this.startOffset - this.position, length);
            offset += discardBytes;
            length -= discardBytes;

            this.position += discardBytes;
        }

        // Discard bytes at the end if required.
        if (this.endOffset != null)
        {
            length = (int)Math.min(this.endOffset + 1 - this.position, length);
        }

        // If there are any bytes in the buffer left to be written, write to the underlying stream and update position.
        if (length > 0)
        {
            this.wrappedStream.write(data, offset, length);
            this.position += length;
        }
    }

    @Override
    public void write(InputStream sourceStream, long writeLength) throws IOException, StorageException {
        Utility.writeToOutputStream(sourceStream, this, writeLength, false, false, null, null);
    }

    @Override
    public void flush() throws IOException {
        this.wrappedStream.flush();   
    }

    @Override
    public void close() throws IOException {
        // no op
    }

}
