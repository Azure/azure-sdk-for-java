package com.microsoft.windowsazure.services.blob.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;

import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.StreamDescriptor;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * Represents a blob that is uploaded as a set of blocks.
 * 
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class CloudBlockBlob extends CloudBlob {

    /**
     * Creates an instance of the <code>CloudBlockBlob</code> class by copying values from another cloud block blob.
     * 
     * @param otherBlob
     *            A <code>CloudBlockBlob</code> object that represents the block blob to copy.
     * 
     * @throws StorageException
     *             If a storage service error occurs.
     */
    public CloudBlockBlob(final CloudBlockBlob otherBlob) throws StorageException {
        super(otherBlob);
    }

    /**
     * Creates an instance of the <code>CloudBlockBlob</code> class using the specified relative URI and storage service
     * client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the relative URI to the blob, beginning with the
     *            container name.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudBlockBlob(final URI uri, final CloudBlobClient client) throws StorageException {
        super(BlobType.BLOCK_BLOB, uri, client);
    }

    /**
     * Creates an instance of the <code>CloudBlockBlob</code> class using the specified relative URI, storage service
     * client and container.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the relative URI to the blob.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * @param container
     *            A {@link CloudBlobContainer} object that represents the container to use for the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudBlockBlob(final URI uri, final CloudBlobClient client, final CloudBlobContainer container)
            throws StorageException {
        super(BlobType.BLOCK_BLOB, uri, client, container);
    }

    /**
     * Creates an instance of the <code>CloudBlockBlob</code> class using the specified relative URI, snapshot ID, and
     * storage service client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the relative URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudBlockBlob(final URI uri, final String snapshotID, final CloudBlobClient client) throws StorageException {
        super(BlobType.BLOCK_BLOB, uri, snapshotID, client);
    }

    /**
     * Commits a block list to the storage service.
     * 
     * @param blockList
     *            An enumerable collection of <code>BlockEntry</code> objects that represents the list block items being
     *            committed. The <code>size</code> field is ignored.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void commitBlockList(final Iterable<BlockEntry> blockList) throws StorageException {
        this.commitBlockList(blockList, null, null, null);
    }

    /**
     * Commits a block list to the storage service using the specified lease ID, request options, and operation context.
     * 
     * @param blockList
     *            An enumerable collection of <code>BlockEntry</code> objects that represents the list block items being
     *            committed. The size field is ignored.
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
     */
    @DoesServiceRequest
    public void commitBlockList(final Iterable<BlockEntry> blockList, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.putBlockList(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), blob.properties, accessCondition, blobOptions, opContext);
                BlobRequest.addMetadata(request, blob.metadata, opContext);

                // Potential optimization, we can write this stream outside of
                // the StorageOperation, so it wont need to be rewritten for each retry. Because it would
                // need to be a final member this would require refactoring into an internal method which
                // receives the block list bytestream as a final param.

                final byte[] blockListBytes = BlobRequest.writeBlockListToStream(blockList, opContext);

                final ByteArrayInputStream blockListInputStream = new ByteArrayInputStream(blockListBytes);

                final StreamDescriptor descriptor = Utility.analyzeStream(blockListInputStream, -1L, -1L, true, true);

                request.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, descriptor.getMd5());

                client.getCredentials().signRequest(request, descriptor.getLength());
                Utility.writeToOutputStream(blockListInputStream, request.getOutputStream(), descriptor.getLength(),
                        false, false, null, opContext);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updatePropertiesFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Downloads the committed block list from the block blob.
     * <p>
     * The committed block list includes the list of blocks that have been successfully committed to the block blob. The
     * list of committed blocks is returned in the same order that they were committed to the blob. No block may appear
     * more than once in the committed block list.
     * 
     * @return An <code>ArrayList</code> object of <code>BlockEntry</code> objects that represent the committed list
     *         block items downloaded from the block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ArrayList<BlockEntry> downloadBlockList() throws StorageException {
        return this.downloadBlockList(BlockListingFilter.COMMITTED, null, null, null);
    }

    /**
     * Downloads the block list from the block blob using the specified block listing filter, request options, and
     * operation context.
     * 
     * @param blockListingFilter
     *            A {@link BlockListingFilter} value that specifies whether to download committed blocks, uncommitted
     *            blocks, or all blocks.
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
     * @return An <code>ArrayList</code> object of <code>BlockEntry</code> objects that represent the list block items
     *         downloaded from the block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ArrayList<BlockEntry> downloadBlockList(final BlockListingFilter blockListingFilter,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException {
        Utility.assertNotNull("blockListingFilter", blockListingFilter);

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, ArrayList<BlockEntry>> impl = new StorageOperation<CloudBlobClient, CloudBlob, ArrayList<BlockEntry>>(
                options) {

            @Override
            public ArrayList<BlockEntry> execute(final CloudBlobClient client, final CloudBlob blob,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.getBlockList(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), blob.snapshotID, blockListingFilter, accessCondition,
                        blobOptions, opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updatePropertiesFromResponse(request);
                final GetBlockListResponse response = new GetBlockListResponse(request.getInputStream());
                return response.getBlocks();
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Creates and opens an output stream to write data to the block blob.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public BlobOutputStream openOutputStream() throws StorageException {
        return this.openOutputStream(null, null, null);
    }

    /**
     * Creates and opens an output stream to write data to the block blob using the specified request options and
     * operation context.
     * 
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
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public BlobOutputStream openOutputStream(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);

        return new BlobOutputStream(this, accessCondition, options, opContext);
    }

    /**
     * Uploads the source stream data to the block blob.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to write to the block blob.
     * @param length
     *            The length, in bytes, of the stream data, or -1 if unknown.
     * 
     * @throws IOException
     *             If an I/O error occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    @DoesServiceRequest
    public void upload(final InputStream sourceStream, final long length) throws StorageException, IOException {
        this.upload(sourceStream, length, null, null, null);
    }

    /**
     * Uploads the source stream data to the blob, using the specified lease ID, request options, and operation context.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to write to the block blob.
     * @param length
     *            The length, in bytes, of the stream data, or -1 if unknown.
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
     * @throws IOException
     *             If an I/O error occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    @DoesServiceRequest
    public void upload(final InputStream sourceStream, final long length, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException {
        if (length < -1) {
            throw new IllegalArgumentException(
                    "Invalid stream length, specify -1 for unkown length stream, or a positive number of bytes");
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this.blobServiceClient);
        // Mark sourceStream for current position.
        sourceStream.mark(Integer.MAX_VALUE);

        StreamDescriptor descriptor = new StreamDescriptor();
        descriptor.setLength(length);

        // If the stream is rewindable and the length is unknown or we need to
        // set md5, then analyze the stream.
        // Note this read will abort at
        // serviceClient.getSingleBlobPutThresholdInBytes() bytes and return
        // -1 as length in which case we will revert to using a stream as it is
        // over the single put threshold.
        if (sourceStream.markSupported() && (length < 0 || options.getStoreBlobContentMD5())) {
            // If the stream is of unknown length or we need to calculate
            // the MD5, then we we need to read the stream contents first

            descriptor = Utility.analyzeStream(sourceStream, length,
                    this.blobServiceClient.getSingleBlobPutThresholdInBytes(), true, options.getStoreBlobContentMD5());

            if (descriptor.getMd5() != null && options.getStoreBlobContentMD5()) {
                this.properties.setContentMD5(descriptor.getMd5());
            }
        }

        // If the stream is rewindable, and the length is known and less than
        // threshold the upload in a single put, otherwise use a stream.
        if (sourceStream.markSupported() && descriptor.getLength() != -1
                && descriptor.getLength() < this.blobServiceClient.getSingleBlobPutThresholdInBytes()) {
            this.uploadFullBlob(sourceStream, descriptor.getLength(), accessCondition, options, opContext);
        }
        else {
            final BlobOutputStream writeStream = this.openOutputStream(accessCondition, options, opContext);
            writeStream.write(sourceStream, length);
            writeStream.close();
        }
    }

    /**
     * Uploads a block to the block blob, using the specified block ID and lease ID.
     * 
     * @param blockId
     *            A <code>String</code> that represents the Base-64 encoded block ID. Note for a given blob the length
     *            of all Block IDs must be identical.
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to write to the block blob.
     * @param length
     *            The length, in bytes, of the stream data, or -1 if unknown.
     * @throws IOException
     *             If an I/O error occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadBlock(final String blockId, final InputStream sourceStream, final long length)
            throws StorageException, IOException {
        this.uploadBlock(blockId, sourceStream, length, null, null, null);
    }

    /**
     * Uploads a block to the block blob, using the specified block ID, lease ID, request options, and operation
     * context.
     * 
     * @param blockId
     *            A <code>String</code> that represents the Base-64 encoded block ID. Note for a given blob the length
     *            of all Block IDs must be identical.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to write to the block blob.
     * @param length
     *            The length, in bytes, of the stream data, or -1 if unknown.
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
     * @throws IOException
     *             If an I/O error occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadBlock(final String blockId, final InputStream sourceStream, final long length,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException, IOException {
        if (length < -1) {
            throw new IllegalArgumentException(
                    "Invalid stream length, specify -1 for unkown length stream, or a positive number of bytes");
        }

        if (length > 4 * Constants.MB) {
            throw new IllegalArgumentException(
                    "Invalid stream length, length must be less than or equal to 4 MB in size.");
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);

        // Assert block length
        if (Utility.isNullOrEmpty(blockId) || !Base64.validateIsBase64String(blockId)) {
            throw new IllegalArgumentException("Invalid blockID, BlockID must be a valid Base64 String.");
        }

        // Mark sourceStream for current position.
        sourceStream.mark(Integer.MAX_VALUE);

        InputStream bufferedStreamReference = sourceStream;
        StreamDescriptor descriptor = new StreamDescriptor();
        descriptor.setLength(length);

        if (!sourceStream.markSupported()) {
            // needs buffering
            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            descriptor = Utility.writeToOutputStream(sourceStream, byteStream, length, false,
                    options.getUseTransactionalContentMD5(), null, opContext);

            bufferedStreamReference = new ByteArrayInputStream(byteStream.toByteArray());
        }
        else if (length < 0 || options.getUseTransactionalContentMD5()) {
            // If the stream is of unknown length or we need to calculate the
            // MD5, then we we need to read
            // the stream contents first

            descriptor = Utility
                    .analyzeStream(sourceStream, length, -1L, true, options.getUseTransactionalContentMD5());
        }

        if (descriptor.getLength() > 4 * Constants.MB) {
            throw new IllegalArgumentException(
                    "Invalid stream length, length must be less than or equal to 4 MB in size.");
        }

        this.uploadBlockInternal(blockId, descriptor.getMd5(), bufferedStreamReference, descriptor.getLength(),
                accessCondition, options, opContext);
    }

    /**
     * Uploads a block of the blob to the server.
     * 
     * @param blockId
     *            the Base64 Encoded Block ID
     * @param md5
     *            the MD5 to use if it will be set.
     * @param sourceStream
     *            the InputStream to read from
     * @param length
     *            the OutputStream to write the blob to.
     * @param accessCondition
     *            An {@link AccessCondition} object that represents the access conditions for the blob.
     * @param options
     *            An object that specifies any additional options for the request
     * @param opContext
     *            an object used to track the execution of the operation
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws IOException
     */
    @DoesServiceRequest
    private void uploadBlockInternal(final String blockId, final String md5, final InputStream sourceStream,
            final long length, final AccessCondition accessCondition, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException, IOException {

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.putBlock(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), blockId, accessCondition, blobOptions, opContext);

                if (blobOptions.getUseTransactionalContentMD5()) {
                    request.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, md5);
                }

                client.getCredentials().signRequest(request, length);

                Utility.writeToOutputStream(sourceStream, request.getOutputStream(), length, true, false, null,
                        opContext);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updatePropertiesFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);

    }
}
