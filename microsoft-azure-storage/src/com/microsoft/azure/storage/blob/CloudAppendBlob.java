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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.ExecutionEngine;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.StorageRequest;
import com.microsoft.azure.storage.core.StreamMd5AndLength;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a Microsoft Azure Append Blob.
 */
public final class CloudAppendBlob extends CloudBlob {
    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class using the specified absolute URI and storage service
     * client.
     * 
     * @param blobAbsoluteUri
     *            A <code>java.net.URI</code> object which represents the absolute URI to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudAppendBlob(final URI blobAbsoluteUri) throws StorageException {
        this(new StorageUri(blobAbsoluteUri));
    }

    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class using the specified absolute URI and storage service
     * client.
     * 
     * @param blobAbsoluteUri
     *            A {@link StorageUri} object which represents the absolute URI to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudAppendBlob(final StorageUri blobAbsoluteUri) throws StorageException {
        this(blobAbsoluteUri, (StorageCredentials)null);
    }

    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class by copying values from another append blob.
     * 
     * @param otherBlob
     *            A <code>CloudAppendBlob</code> object which represents the append blob to copy.
     */
    public CloudAppendBlob(final CloudAppendBlob otherBlob) {
        super(otherBlob);
    }
    
    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class using the specified absolute URI and credentials.
     * 
     * @param blobAbsoluteUri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the blob.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudAppendBlob(final URI blobAbsoluteUri, final StorageCredentials credentials) throws StorageException {
        this(new StorageUri(blobAbsoluteUri), credentials);
    }

    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class using the specified absolute URI, snapshot ID, and
     * credentials.
     * 
     * @param blobAbsoluteUri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudAppendBlob(final URI blobAbsoluteUri, final String snapshotID, final StorageCredentials credentials)
            throws StorageException {
        this(new StorageUri(blobAbsoluteUri), snapshotID, credentials);
    }
    
    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class using the specified absolute StorageUri 
     * and credentials.
     * 
     * @param blobAbsoluteUri
     *            A {@link StorageUri} object that represents the absolute URI to the blob.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudAppendBlob(final StorageUri blobAbsoluteUri, final StorageCredentials credentials) throws StorageException {
        this(blobAbsoluteUri, null /* snapshotID */, credentials);
    }

    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class using the specified absolute StorageUri, snapshot
     * ID, and credentials.
     * 
     * @param blobAbsoluteUri
     *            A {@link StorageUri} object that represents the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param credentials
     *            A {@link StorageCredentials} object used to authenticate access.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudAppendBlob(final StorageUri blobAbsoluteUri, final String snapshotID, final StorageCredentials credentials)
            throws StorageException {
        super(BlobType.APPEND_BLOB, blobAbsoluteUri, snapshotID, credentials);
    }
    
    /**
     * Creates an instance of the <code>CloudAppendBlob</code> class using the specified type, name, snapshot ID, and
     * container.
     *
     * @param blobName
     *            Name of the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param container
     *            The reference to the parent container.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected CloudAppendBlob(String blobName, String snapshotID, CloudBlobContainer container)
            throws URISyntaxException {
        super(BlobType.APPEND_BLOB, blobName, snapshotID, container);
    }
    
    /**
     * Requests the service to start copying a append blob's contents, properties, and metadata to a new append blob.
     *
     * @param sourceBlob
     *            A <code>CloudAppendBlob</code> object that represents the source blob to copy.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     */
    @DoesServiceRequest
    public final String startCopy(final CloudAppendBlob sourceBlob) throws StorageException, URISyntaxException {
        return this.startCopy(sourceBlob, null /* sourceAccessCondition */,
                null /* destinationAccessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Requests the service to start copying a append blob's contents, properties, and metadata to a new append blob,
     * using the specified access conditions, lease ID, request options, and operation context.
     *
     * @param sourceBlob
     *            A <code>CloudAppendBlob</code> object that represents the source blob to copy.
     * @param sourceAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the source blob.
     * @param destinationAccessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the destination blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @return A <code>String</code> which represents the copy ID associated with the copy operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *
     */
    @DoesServiceRequest
    public final String startCopy(final CloudAppendBlob sourceBlob, final AccessCondition sourceAccessCondition,
            final AccessCondition destinationAccessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException, URISyntaxException {
        Utility.assertNotNull("sourceBlob", sourceBlob);
        return this.startCopy(
                sourceBlob.getQualifiedUri(), sourceAccessCondition, destinationAccessCondition, options, opContext);
    }

    /**
     * Creates an empty append blob. If the blob already exists, this will replace it. 
     * <p>
     * To avoid overwriting and instead throw an error, please use the 
     * {@link #createOrReplace(AccessCondition, BlobRequestOptions, OperationContext)} overload with the appropriate 
     * {@link AccessCondition}.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void createOrReplace() throws StorageException {
        this.createOrReplace(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Creates an append blob using the specified request options and operation context. If the blob already exists, 
     * this will replace it.
     * <p>
     * To avoid overwriting and instead throw an error, please pass in an {@link AccessCondition} generated using 
     * {@link AccessCondition#generateIfNotExistsCondition()}.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void createOrReplace(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.APPEND_BLOB, this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.createImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> createImpl(final AccessCondition accessCondition, 
            final BlobRequestOptions options) {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.putBlob(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options, context, accessCondition, blob.properties, BlobType.APPEND_BLOB, 0);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudBlob blob, OperationContext context) {
                BlobRequest.addMetadata(connection, blob.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, 0L, context);
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                blob.getProperties().setLength(0);
                return null;
            }

        };

        return putRequest;
    }
    
    /**
     * Commits a new block of data to the end of the blob.
     * 
     * @param sourceStream
     *            An {@link InputStream} object that represents the input stream to write to the append blob.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data, or -1 if unknown.
     *            
     * @return The offset at which the block was appended.
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Long appendBlock(final InputStream sourceStream, final long length) throws IOException, StorageException
    {
        return this.appendBlock(sourceStream, length, null, null, null);
    }
    
    /**
     * Commits a new block of data to the end of the blob.
     * 
     * @param sourceStream
     *            An {@link InputStream} object that represents the input stream to write to the Append blob.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data, or -1 if unknown.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @return The offset at which the block was appended.</returns>
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public Long appendBlock(final InputStream sourceStream, final long length, final AccessCondition accessCondition, 
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException
    {
        if (length < -1) {
            throw new IllegalArgumentException(SR.STREAM_LENGTH_NEGATIVE);
        }

        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.APPEND_BLOB, this.blobServiceClient);

        if (sourceStream.markSupported()) {
            // Mark sourceStream for current position.
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        InputStream bufferedStreamReference = sourceStream;
        StreamMd5AndLength descriptor = new StreamMd5AndLength();
        descriptor.setLength(length);

        if (!sourceStream.markSupported()) {
            // needs buffering
            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            descriptor = Utility.writeToOutputStream(sourceStream, byteStream, length, false /* rewindSourceStream */,
                    options.getUseTransactionalContentMD5(), opContext, options);

            bufferedStreamReference = new ByteArrayInputStream(byteStream.toByteArray());
        }
        else if (length < 0 || options.getUseTransactionalContentMD5()) {
            // If the stream is of unknown length or we need to calculate the
            // MD5, then we we need to read the stream contents first
            descriptor = Utility.analyzeStream(sourceStream, length, -1L, true /* rewindSourceStream */,
                    options.getUseTransactionalContentMD5());
        }

        if (descriptor.getLength() > 4 * Constants.MB) {
            throw new IllegalArgumentException(SR.STREAM_LENGTH_GREATER_THAN_4MB);
        }

        StorageRequest<CloudBlobClient, CloudAppendBlob, Long> appendBlockImpl = appendBlockImpl(descriptor.getMd5(), bufferedStreamReference, 
                descriptor.getLength(), accessCondition, options, opContext);
        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, appendBlockImpl, options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudAppendBlob, Long> appendBlockImpl(final String md5,
            final InputStream sourceStream, final long length, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext) {

        final StorageRequest<CloudBlobClient, CloudAppendBlob, Long> putRequest = new StorageRequest<CloudBlobClient, CloudAppendBlob, Long>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudAppendBlob blob, OperationContext context)
                    throws Exception {
                this.setSendStream(sourceStream);
                this.setLength(length);
                return BlobRequest.appendBlock(blob.getTransformedAddress(opContext).getUri(this.getCurrentLocation()),
                        options, opContext, accessCondition);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudAppendBlob blob, OperationContext context) {
                if (options.getUseTransactionalContentMD5()) {
                    connection.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, md5);
                }
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobQueueAndFileRequest(connection, client, length, context);
            }

            @Override
            public Long preProcessResponse(CloudAppendBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                Long appendOffset = null;
                if (this.getConnection().getHeaderField(Constants.HeaderConstants.BLOB_APPEND_OFFSET) != null)
                {
                    appendOffset = Long.parseLong(this.getConnection().getHeaderField(Constants.HeaderConstants.BLOB_APPEND_OFFSET));
                }

                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                blob.updateCommittedBlockCountFromResponse(this.getConnection());
                
                return appendOffset;
            }

            @Override
            public void recoveryAction(OperationContext context) throws IOException {
                sourceStream.reset();
                sourceStream.mark(Constants.MAX_MARK_LENGTH);
            }
        };

        return putRequest;
    }
    
    /**
     * Updates the blob's committed block count from the web request.
     * 
     * @param request
     *            The web request from which to parse the committed block count.
     */
    private void updateCommittedBlockCountFromResponse(HttpURLConnection request) {
        final String comittedBlockCount = request.getHeaderField(Constants.HeaderConstants.BLOB_COMMITTED_BLOCK_COUNT);
        if (!Utility.isNullOrEmpty(comittedBlockCount))
        {
            this.getProperties().setAppendBlobCommittedBlockCount(Integer.parseInt(comittedBlockCount));
        }
    }
    
    /**
     * Appends a stream to an append blob. This API should be used strictly in a single writer scenario because the API 
     * internally uses the append-offset conditional header to avoid duplicate blocks which does not work in a multiple 
     * writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param sourceStream
     *          A {@link InputStream} object providing the blob content to append.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data, or -1 if unknown.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    @DoesServiceRequest
    public void append(InputStream sourceStream, final long length) throws StorageException, IOException
    {
        this.append(sourceStream, length, null /* accessCondition */, null /* options */, null /* operationContext */);
    }
    
    /**
     * Appends a stream to an append blob. This API should be used strictly in a single writer scenario because the API 
     * internally uses the append-offset conditional header to avoid duplicate blocks which does not work in a multiple 
     * writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param sourceStream
     *          A {@link InputStream} object providing the blob content to append.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data, or -1 if unknown.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    @DoesServiceRequest
    public void append(InputStream sourceStream, final long length, AccessCondition accessCondition, 
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException
    {
        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.APPEND_BLOB, this.blobServiceClient);

        if (sourceStream.markSupported()) {
            // Mark sourceStream for current position.
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        final BlobOutputStream streamRef = this.openWriteExisting(accessCondition, options, opContext);
        try {
            streamRef.write(sourceStream, length);
        }
        finally {
            streamRef.close();
        }
    }
    
    /**
     * Appends the contents of a byte array to an append blob.This API should be used strictly in a single writer 
     * scenario because the API internally uses the append-offset conditional header to avoid duplicate blocks which 
     * does not work in a multiple writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param buffer
     *            A <code>byte</code> array which represents the data to append to the blob.
     * @param offset
     *            A <code>int</code> which represents the offset of the byte array from which to start the data upload.
     * @param length
     *            An <code>int</code> which represents the number of bytes to upload from the input buffer.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    public void appendFromByteArray(final byte[] buffer, final int offset, final int length) throws StorageException,
            IOException {
        appendFromByteArray(buffer, offset, length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Appends the contents of a byte array to an append blob.This API should be used strictly in a single writer 
     * scenario because the API internally uses the append-offset conditional header to avoid duplicate blocks which 
     * does not work in a multiple writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param buffer
     *            A <code>byte</code> array which represents the data to append to the blob.
     * @param offset
     *            A <code>int</code> which represents the offset of the byte array from which to start the data upload.
     * @param length
     *            An <code>int</code> which represents the number of bytes to upload from the input buffer.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    public void appendFromByteArray(final byte[] buffer, final int offset, final int length,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException, IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer, offset, length);
        this.append(inputStream, length, accessCondition, options, opContext);
        inputStream.close();
    }
    
    /**
     * Appends a file to an append blob. This API should be used strictly in a single writer scenario because the API 
     * internally uses the append-offset conditional header to avoid duplicate blocks which does not work in a multiple 
     * writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param path
     *            A <code>String</code> which represents the path to the file to be appended.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    public void appendFromFile(final String path) throws StorageException, IOException {
        appendFromFile(path, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Appends a file to an append blob. This API should be used strictly in a single writer scenario because the API 
     * internally uses the append-offset conditional header to avoid duplicate blocks which does not work in a multiple 
     * writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     *
     * @param path
     *            A <code>String</code> which represents the path to the file to be appended.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     *
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    public void appendFromFile(final String path, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException, IOException {
        File file = new File(path);
        long fileLength = file.length();
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        this.append(inputStream, fileLength, accessCondition, options, opContext);
        inputStream.close();
    }
    
    /**
     * Appends a string of text to an append blob using the platform's default encoding. This API should be used 
     * strictly in a single writer scenario because the API internally uses the append-offset conditional header to 
     * avoid duplicate blocks which does not work in a multiple writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param content
     *            A <code>String</code> which represents the content that will be appended to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    public void appendText(final String content) throws StorageException, IOException {
        this.appendText(content, null /* charsetName */, null /* accessCondition */, null /* options */, 
                null /* opContext */);
    }

    /**
     * Appends a string of text to an append blob using the specified encoding. This API should be used strictly in a 
     * single writer scenario because the API internally uses the append-offset conditional header to avoid duplicate 
     * blocks which does not work in a multiple writer scenario.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param content
     *            A <code>String</code> which represents the content that will be appended to the blob.
     * @param charsetName
     *            A <code>String</code> which represents the name of the charset to use to encode the content.
     *            If null, the platform's default encoding is used.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     *          If an I/O exception occurred.
     */
    public void appendText(final String content, final String charsetName, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException {
        byte[] bytes = (charsetName == null) ? content.getBytes() : content.getBytes(charsetName);
        this.appendFromByteArray(bytes, 0, bytes.length, accessCondition, options, opContext);
    }
    
    /**
     * Opens an output stream object to write data to the append blob. The append blob must already exist and will be
     * appended to.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobOutputStream openWriteExisting() throws StorageException {
        return this.openWriteExisting(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the append blob, using the specified lease ID, request options and
     * operation context. The append blob must already exist and will be appended to.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobOutputStream openWriteExisting(AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        return this.openOutputStreamInternal(false, accessCondition, options, opContext);
    }

    /**
     * Opens an output stream object to write data to the append blob. The append blob does not need to yet exist. If 
     * the blob already exists, this will replace it. 
     * <p>
     * To avoid overwriting and instead throw an error, please use the 
     * {@link #openWriteNew(AccessCondition, BlobRequestOptions, OperationContext)} overload with the appropriate 
     * {@link AccessCondition}.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobOutputStream openWriteNew() throws StorageException {
        return this.openWriteNew(null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the append blob, using the specified lease ID, request options and
     * operation context. The append blob does not need to yet exist. If the blob already exists, this will replace it. 
     * <p>
     * To avoid overwriting and instead throw an error, please pass in an {@link AccessCondition} generated using 
     * {@link AccessCondition#generateIfNotExistsCondition()}.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobOutputStream openWriteNew(AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        return this.openOutputStreamInternal(true, accessCondition, options, opContext);
    }

    /**
     * Opens an output stream object to write data to the append blob, using the specified lease ID, request options and
     * operation context.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    private BlobOutputStream openOutputStreamInternal(boolean create, AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        assertNoWriteOperationForSnapshot();
        
        if (opContext == null) {
            opContext = new OperationContext();
        }
        
        BlobRequestOptions modifiedOptions = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.APPEND_BLOB, 
                this.blobServiceClient, false /* setStartTime */);
        
        if (create) {
            this.createOrReplace(accessCondition, modifiedOptions, opContext);
        } else {
            if (modifiedOptions.getStoreBlobContentMD5()) {
                throw new IllegalArgumentException(SR.APPEND_BLOB_MD5_NOT_POSSIBLE);
            }
            
            // Download attributes to check the etag and date access conditions and
            // to get the blob length to verify the append position on the first write.
            this.downloadAttributes(accessCondition, modifiedOptions, opContext);
        }

        // Use an access condition with the etag and date conditions removed as we will be
        // appending to the blob and these properties will change each time we append a block.
        AccessCondition appendCondition = new AccessCondition();
        if (accessCondition != null) {
            appendCondition.setLeaseID(accessCondition.getLeaseID());
            appendCondition.setIfAppendPositionEqual(accessCondition.getIfAppendPositionEqual());
            appendCondition.setIfMaxSizeLessThanOrEqual(accessCondition.getIfMaxSizeLessThanOrEqual());
        }

        return new BlobOutputStream(this, appendCondition, modifiedOptions, opContext);
    }

    /**
     * Uploads the source stream data to the append blob.  If the blob already exists on the service, it will be 
     * overwritten.
     * <p>
     * If you want to append data to an already existing blob, please see {@link #appendBlock(InputStream, long)}.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param sourceStream
     *            An {@link InputStream} object to read from.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data, or -1 if unknown.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    @DoesServiceRequest
    public void upload(final InputStream sourceStream, final long length) throws StorageException, IOException {
        this.upload(sourceStream, length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Uploads the source stream data to the append blob using the specified lease ID, request options, and operation
     * context. If the blob already exists on the service, it will be overwritten.
     * <p>
     * If you want to append data to an already existing blob, please see {@link #appendBlock(InputStream, long)}.
     * <p>
     * If you are doing writes in a single writer scenario, please look at 
     * {@link BlobRequestOptions#setAbsorbConditionalErrorsOnRetry(Boolean)} and see if setting this flag 
     * to <code>true</code> is acceptable for you.
     * 
     * @param sourceStream
     *            An {@link InputStream} object to read from.
     * @param length
     *            A <code>long</code> which represents the length, in bytes, of the stream data, or -1 if unknown.
     * @param accessCondition
     *            An {@link AccessCondition} object which represents the access conditions for the blob.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object which represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    @DoesServiceRequest
    public void upload(final InputStream sourceStream, final long length, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException {
        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.populateAndApplyDefaults(options, BlobType.APPEND_BLOB, this.blobServiceClient);

        if (sourceStream.markSupported()) {
            // Mark sourceStream for current position.
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        final BlobOutputStream streamRef = this.openWriteNew(accessCondition, options, opContext);
        try {
            streamRef.write(sourceStream, length);
        }
        finally {
            streamRef.close();
        }
    }
    
    /**
     * Sets the number of bytes to buffer when writing to a {@link BlobOutputStream}.
     * 
     * @param streamWriteSizeInBytes
     *            An <code>int</code> which represents the maximum block size, in bytes, for writing to an append blob
     *            while using a {@link BlobOutputStream} object, ranging from 16 KB to 4 MB, inclusive.
     * 
     * @throws IllegalArgumentException
     *             If <code>streamWriteSizeInBytes</code> is less than 16 KB or greater than 4 MB.
     */
    @Override
    public void setStreamWriteSizeInBytes(final int streamWriteSizeInBytes) {
        if (streamWriteSizeInBytes > Constants.MAX_BLOCK_SIZE || streamWriteSizeInBytes < 16 * Constants.KB) {
            throw new IllegalArgumentException("StreamWriteSizeInBytes");
        }

        this.streamWriteSizeInBytes = streamWriteSizeInBytes;
    }
}
