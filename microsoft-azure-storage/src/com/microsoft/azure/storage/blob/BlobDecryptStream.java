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
import java.util.Map;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Utility;

/**
 * Stream that will be used for decrypting blob ranges. It buffers 16 bytes of IV (if required) before creating a crypto
 * stream and routing the rest of the data through it.
 */
class BlobDecryptStream extends BlobOutputStream {
    private final OutputStream userStream;
    private final Map<String, String> metadata;
    private long position;
    private Long userProvidedLength;
    private byte[] iv = new byte[16];
    private BlobEncryptionPolicy encryptionPolicy;
    private int discardFirst;
    private OutputStream cryptoStream;
    private boolean bufferIV;
    private boolean noPadding;
    private Boolean requireEncryption;
    
    public BlobDecryptStream(OutputStream userStream, Map<String, String> metadata, Long userProvidedLength, 
            int discardFirst, boolean bufferIV, boolean noPadding, BlobEncryptionPolicy policy, 
            final Boolean requireEncryption)
    {
        this.userStream = userStream;
        this.metadata = metadata;
        this.userProvidedLength = userProvidedLength;
        this.discardFirst = discardFirst;
        this.encryptionPolicy = policy;
        this.bufferIV = bufferIV;
        this.noPadding = noPadding;
        this.requireEncryption = requireEncryption;
    }
    
    @Override
    public void close() throws IOException
    {
        this.cryptoStream.close();
    }

    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        // Keep buffering until we have 16 bytes of IV.
        if (this.bufferIV && this.position < 16)
        {
            int bytesToCopy = 16 - (int)this.position;
            bytesToCopy = length > bytesToCopy ? bytesToCopy : length;
            System.arraycopy(data, offset, this.iv, (int)this.position, bytesToCopy);
            this.position += bytesToCopy;
            offset += bytesToCopy;
            length -= bytesToCopy;
        }

        // Wrap user stream with LengthLimitingStream. This stream will be used to discard the extra bytes we downloaded in order to deal with AES block size.
        // Create crypto stream around the length limiting stream once per download and start writing to it. During retries, the state is maintained and 
        // new crypto streams will not be created each time. 
        if (this.cryptoStream == null)
        {
            LengthLimitingStream lengthLimitingStream = new LengthLimitingStream(this.userStream, this.discardFirst, 
                    this.userProvidedLength);
            try {
                this.cryptoStream = this.encryptionPolicy.decryptBlob(lengthLimitingStream, this.metadata,
                        this.requireEncryption, !this.bufferIV ? null : this.iv, this.noPadding);
            }
            catch (StorageException e) {
                throw Utility.initIOException(e);
            }
        }

        // Route the remaining data through the crypto stream.
        if (length > 0)
        {
            this.cryptoStream.write(data, offset, length);
            this.position += length;
        }
    }

    @Override
    public void write(InputStream sourceStream, long writeLength) throws IOException, StorageException {
        Utility.writeToOutputStream(sourceStream, this, writeLength, false, false, null, null);
    }

    @Override
    public void flush() throws IOException {
        this.userStream.flush();
    }
}
