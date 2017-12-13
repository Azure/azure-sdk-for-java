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

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * RESERVED FOR INTERNAL USE. Wraps a FileStream to allow for more memory efficient uploading.
 */
public final class MarkableFileStream extends FilterInputStream {
    private long mark = -1;
    private FileChannel fileChannel;

    public MarkableFileStream(FileInputStream stream) {
        super(stream);
        this.fileChannel = stream.getChannel();
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.mark = this.fileChannel.position();
        }
        catch (IOException e) {
            this.mark = -1;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if(this.mark == -1){
            throw new IOException("Stream must be marked before calling reset");
        }

        this.fileChannel.position(this.mark);
    }

    @Override
    public boolean markSupported() {
        return true;
    }
}
