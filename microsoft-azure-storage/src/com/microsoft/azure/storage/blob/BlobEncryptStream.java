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

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.Utility;

/**
 * Stream that will be used for encrypting blobs.
 */
final class BlobEncryptStream extends BlobOutputStream {

    /**
     * Holds the OperationContext for the current stream.
     */
    OperationContext opContext;

    /**
     * Holds the options for the current stream.
     */
    BlobRequestOptions options;
    
    /**
     * Holds the cipher stream.
     */
    private CipherOutputStream cipherStream;
    
    /**
     * Initializes a new instance of the BlobEncryptStream class for a CloudBlockBlob
     * 
     * @param blockBlob
     *            A {@link CloudBlockBlob} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object which specifies any additional options for the request.
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation.
     * @param cipher
     *            A {@link Cipher} object used to encrypt the stream.
     * 
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    protected BlobEncryptStream(final CloudBlockBlob blockBlob, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext, final Cipher cipher) 
                    throws StorageException {
        this.opContext = opContext;
        this.options = options;
        
        this.options.setValidateEncryptionPolicy(false);
        BlobOutputStreamInternal blobStream = 
                new BlobOutputStreamInternal(blockBlob, accessCondition, options, opContext);
        this.cipherStream = new CipherOutputStream(blobStream, cipher);
    }

    /**
     * Initializes a new instance of the BlobEncryptStream class for a CloudPageBlob
     * 
     * @param pageBlob
     *            A {@link CloudPageBlob} object which represents the blob that this stream is associated with.
     * @param length
     *            A <code>long</code> which represents the length of the page blob in bytes, which must be a multiple of
     *            512.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object which specifies any additional options for the request
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation
     * @param cipher
     *            A {@link Cipher} object used to encrypt the stream.
     *            
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected BlobEncryptStream(final CloudPageBlob pageBlob, final long length,
            final AccessCondition accessCondition, final BlobRequestOptions options, final OperationContext opContext,
            final Cipher cipher) throws StorageException {
        this.opContext = opContext;
        this.options = options;
        
        this.options.setValidateEncryptionPolicy(false);
        BlobOutputStreamInternal blobStream = 
                new BlobOutputStreamInternal(pageBlob, length, accessCondition, options, opContext);
        this.cipherStream = new CipherOutputStream(blobStream, cipher);
    }
    
    /**
     * Initializes a new instance of the BlobEncryptStream class for a CloudPageBlob
     * 
     * @param appendBlob
     *            A {@link CloudAppendBlob} object which represents the blob that this stream is associated with.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object which specifies any additional options for the request
     * @param opContext
     *            An {@link OperationContext} object which is used to track the execution of the operation
     * @param cipher
     *            A {@link Cipher} object used to encrypt the stream.
     *            
     * @throws StorageException
     *             An exception representing any error which occurred during the operation.
     */
    @DoesServiceRequest
    protected BlobEncryptStream(final CloudAppendBlob appendBlob, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext, final Cipher cipher)
            throws StorageException {
        this.opContext = opContext;
        this.options = options;
        
        this.options.setValidateEncryptionPolicy(false);
        BlobOutputStreamInternal blobStream = 
                new BlobOutputStreamInternal(appendBlob, accessCondition, options, opContext);
        this.cipherStream = new CipherOutputStream(blobStream, cipher);
    }
    
    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        this.cipherStream.write(data, offset, length);     
    }

    @Override
    public void write(InputStream sourceStream, long writeLength) throws IOException, StorageException {
        Utility.writeToOutputStream(sourceStream, this, writeLength, false, false, this.opContext, this.options, false);
    }

    @Override
    public void flush() throws IOException {
        this.cipherStream.flush();
        
    }

    @Override
    public void close() throws IOException {
        this.cipherStream.close();
    }

}
