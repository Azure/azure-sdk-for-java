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
package com.microsoft.windowsazure.services.blob.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.microsoft.windowsazure.services.core.storage.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * Represents a Windows Azure page blob.
 */
public final class CloudPageBlob extends CloudBlob {
    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified relative URI and storage service
     * client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the relative URI to the blob, beginning with the
     *            container name.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final URI uri) throws StorageException {
        super(BlobType.PAGE_BLOB);

        Utility.assertNotNull("blobAbsoluteUri", uri);
        this.uri = uri;
        this.parseURIQueryStringAndVerify(uri, null, Utility.determinePathStyleFromUri(uri, null));;
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class by copying values from another page blob.
     * 
     * @param otherBlob
     *            A <code>CloudPageBlob</code> object that represents the page blob to copy.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final CloudPageBlob otherBlob) throws StorageException {
        super(otherBlob);
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified URI and cloud blob client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the blob, beginning with the
     *            container name.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final URI uri, final CloudBlobClient client) throws StorageException {
        super(BlobType.PAGE_BLOB, uri, client);
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified URI, cloud blob client, and cloud
     * blob container.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the relative URI to the blob, beginning with the
     *            container name.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * @param container
     *            A {@link CloudBlobContainer} object that represents the container to use for the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final URI uri, final CloudBlobClient client, final CloudBlobContainer container)
            throws StorageException {
        super(BlobType.PAGE_BLOB, uri, client, container);
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified URI, snapshot ID, and cloud blob
     * client.
     * 
     * @param uri
     *            A <code>java.net.URI</code> object that represents the absolute URI to the blob, beginning with the
     *            container name.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param client
     *            A {@link CloudBlobContainer} object that represents the container to use for the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final URI uri, final String snapshotID, final CloudBlobClient client) throws StorageException {
        super(BlobType.PAGE_BLOB, uri, snapshotID, client);
    }

    /**
     * Clears pages from a page blob.
     * <p>
     * Calling <code>clearPages</code> releases the storage space used by the specified pages. Pages that have been
     * cleared are no longer tracked as part of the page blob, and no longer incur a charge against the storage account.
     * 
     * @param offset
     *            The offset, in bytes, at which to begin clearing pages. This value must be a multiple of 512.
     * @param length
     *            The length, in bytes, of the data range to be cleared. This value must be a multiple of 512.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void clearPages(final long offset, final long length) throws StorageException, IOException {
        this.clearPages(offset, length, null, null, null);
    }

    /**
     * Clears pages from a page blob using the specified lease ID, request options, and operation context.
     * <p>
     * Calling <code>clearPages</code> releases the storage space used by the specified pages. Pages that have been
     * cleared are no longer tracked as part of the page blob, and no longer incur a charge against the storage account.
     * 
     * @param offset
     *            The offset, in bytes, at which to begin clearing pages. This value must be a multiple of 512.
     * @param length
     *            The length, in bytes, of the data range to be cleared. This value must be a multiple of 512.
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
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void clearPages(final long offset, final long length, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException, IOException {
        if (offset % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("Page start offset must be multiple of 512!");
        }

        if (length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("Page data must be multiple of 512!");
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);
        final PageProperties pageProps = new PageProperties();
        pageProps.setPageOperation(PageOperationType.CLEAR);
        pageProps.getRange().setStartOffset(offset);
        pageProps.getRange().setEndOffset(offset + length - 1);

        this.putPagesInternal(pageProps, null, length, null, accessCondition, options, opContext);
    }

    /**
     * Creates a page blob.
     * 
     * @param length
     *            The size, in bytes, of the page blob.
     * 
     * @throws IllegalArgumentException
     *             If the length is not a multiple of 512.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create(final long length) throws StorageException {
        this.create(length, null, null, null);
    }

    /**
     * Creates a page blob using the specified request options and operation context.
     * 
     * @param length
     *            The size, in bytes, of the page blob.
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
     * @throws IllegalArgumentException
     *             If the length is not a multiple of 512.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void create(final long length, final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        assertNoWriteOperationForSnapshot();

        if (length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("Page blob length must be multiple of 512.");
        }

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

                final HttpURLConnection request = BlobRequest.put(blob.getTransformedAddress(opContext), this
                        .getRequestOptions().getTimeoutIntervalInMs(), blob.properties, BlobType.PAGE_BLOB, length,
                        accessCondition, blobOptions, opContext);

                BlobRequest.addMetadata(request, blob.metadata, opContext);

                client.getCredentials().signRequest(request, 0L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns a collection of page ranges and their starting and ending byte offsets.
     * <p>
     * The start and end byte offsets for each page range are inclusive.
     * 
     * @return An <code>ArrayList</code> object that represents the set of page ranges and their starting and ending
     *         byte offsets.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ArrayList<PageRange> downloadPageRanges() throws StorageException {
        return this.downloadPageRanges(null, null, null);
    }

    /**
     * Returns a collection of page ranges and their starting and ending byte offsets using the specified request
     * options and operation context.
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
     * @return An <code>ArrayList</code> object that represents the set of page ranges and their starting and ending
     *         byte offsets.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ArrayList<PageRange> downloadPageRanges(final AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);

        final StorageOperation<CloudBlobClient, CloudBlob, ArrayList<PageRange>> impl = new StorageOperation<CloudBlobClient, CloudBlob, ArrayList<PageRange>>(
                options) {

            @Override
            public ArrayList<PageRange> execute(final CloudBlobClient client, final CloudBlob blob,
                    final OperationContext opContext) throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.getPageRanges(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), blob.snapshotID, accessCondition, blobOptions, opContext);

                client.getCredentials().signRequest(request, -1L);

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                blob.updateLengthFromResponse(request);

                final GetPageRangesResponse response = new GetPageRangesResponse(request.getInputStream());
                return response.getPageRanges();
            }
        };

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(),
                opContext);
    }

    /**
     * Opens an input stream object to write data to the page blob.
     * 
     * @param length
     *            The length, in bytes, of the stream to create. This value must be a multiple of 512.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobOutputStream openOutputStream(final long length) throws StorageException {
        return this.openOutputStream(length, null, null, null);
    }

    /**
     * Opens an input stream object to write data to the page blob, using the specified lease ID, request options and
     * operation context.
     * 
     * @param length
     *            The length, in bytes, of the stream to create. This value must be a multiple of 512.
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
    @DoesServiceRequest
    public BlobOutputStream openOutputStream(final long length, final AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        assertNoWriteOperationForSnapshot();

        options.applyDefaults(this.blobServiceClient);

        if (options.getStoreBlobContentMD5()) {
            throw new IllegalArgumentException("Blob Level MD5 is not supported for PageBlob");
        }

        return new BlobOutputStream(this, length, accessCondition, options, opContext);
    }

    /**
     * Used for both uploadPages and clearPages.
     * 
     * @param pageProperties
     *            The page properties.
     * @param data
     *            The data to write.
     * @param length
     *            The number of bytes to write.
     * @param md5
     *            the MD5 hash for the data.
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
    private void putPagesInternal(final PageProperties pageProperties, final byte[] data, final long length,
            final String md5, final AccessCondition accessCondition, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException {

        final StorageOperation<CloudBlobClient, CloudBlob, Void> impl = new StorageOperation<CloudBlobClient, CloudBlob, Void>(
                options) {

            @Override
            public Void execute(final CloudBlobClient client, final CloudBlob blob, final OperationContext opContext)
                    throws Exception {
                final BlobRequestOptions blobOptions = (BlobRequestOptions) this.getRequestOptions();

                final HttpURLConnection request = BlobRequest.putPage(blob.getTransformedAddress(opContext),
                        blobOptions.getTimeoutIntervalInMs(), pageProperties, accessCondition, blobOptions, opContext);

                if (pageProperties.getPageOperation() == PageOperationType.UPDATE) {
                    if (blobOptions.getUseTransactionalContentMD5()) {
                        request.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, md5);
                    }

                    client.getCredentials().signRequest(request, length);
                    request.getOutputStream().write(data);
                }
                else {
                    client.getCredentials().signRequest(request, 0L);
                }

                this.setResult(ExecutionEngine.processRequest(request, opContext));

                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(request);
                return null;
            }
        };

        ExecutionEngine
                .executeWithRetry(this.blobServiceClient, this, impl, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Uploads the source stream data to the page blob.
     * 
     * @param sourceStream
     *            An <code>IntputStream</code> object to read from.
     * @param length
     *            The length, in bytes, of the stream data, must be non zero and a multiple of 512.
     * 
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Override
    @DoesServiceRequest
    public void upload(final InputStream sourceStream, final long length) throws StorageException, IOException {
        this.upload(sourceStream, length, null, null, null);
    }

    /**
     * Uploads the source stream data to the page blob using the specified lease ID, request options, and operation
     * context.
     * 
     * @param sourceStream
     *            An <code>IntputStream</code> object to read from.
     * @param length
     *            The length, in bytes, of the stream data. This must be great than zero and a multiple of 512.
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

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);
        if (length <= 0 || length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("Page data must be multiple of 512!");
        }

        if (options.getStoreBlobContentMD5()) {
            throw new IllegalArgumentException("Blob Level MD5 is not supported for PageBlob");
        }

        if (sourceStream.markSupported()) {
            // Mark sourceStream for current position.
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        if (length <= 4 * Constants.MB) {
            this.create(length, accessCondition, options, opContext);
            this.uploadPages(sourceStream, 0, length, accessCondition, options, opContext);
        }
        else {
            final OutputStream streamRef = this.openOutputStream(length, accessCondition, options, opContext);
            Utility.writeToOutputStream(sourceStream, streamRef, length, false, false, null, opContext);
            streamRef.close();
        }
    }

    /**
     * Uploads a range of contiguous pages, up to 4 MB in size, at the specified offset in the page blob.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to write to the page blob.
     * @param offset
     *            The offset, in number of bytes, at which to begin writing the data. This value must be a multiple of
     *            512.
     * @param length
     *            The length, in bytes, of the data to write. This value must be a multiple of 512.
     * 
     * @throws IllegalArgumentException
     *             If the offset or length are not multiples of 512, or if the length is greater than 4 MB.
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPages(final InputStream sourceStream, final long offset, final long length)
            throws StorageException, IOException {
        this.uploadPages(sourceStream, offset, length, null, null, null);
    }

    /**
     * Uploads a range of contiguous pages, up to 4 MB in size, at the specified offset in the page blob, using the
     * specified lease ID, request options, and operation context.
     * 
     * @param sourceStream
     *            An <code>InputStream</code> object that represents the input stream to write to the page blob.
     * @param offset
     *            The offset, in number of bytes, at which to begin writing the data. This value must be a multiple of
     *            512.
     * @param length
     *            The length, in bytes, of the data to write. This value must be a multiple of 512.
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
     * @throws IllegalArgumentException
     *             If the offset or length are not multiples of 512, or if the length is greater than 4 MB.
     * @throws IOException
     *             If an I/O exception occurred.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public void uploadPages(final InputStream sourceStream, final long offset, final long length,
            final AccessCondition accessCondition, BlobRequestOptions options, OperationContext opContext)
            throws StorageException, IOException {

        if (offset % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("Page start offset must be multiple of 512!");
        }

        if (length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("Page data must be multiple of 512!");
        }

        if (length > 4 * Constants.MB) {
            throw new IllegalArgumentException("Max write size is 4MB. Please specify a smaller range.");
        }

        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        if (options == null) {
            options = new BlobRequestOptions();
        }

        options.applyDefaults(this.blobServiceClient);

        final PageProperties pageProps = new PageProperties();
        pageProps.setPageOperation(PageOperationType.UPDATE);
        pageProps.getRange().setStartOffset(offset);
        pageProps.getRange().setEndOffset(offset + length - 1);
        final byte[] data = new byte[(int) length];
        String md5 = null;

        int count = 0;
        long total = 0;
        while (total < length) {
            count = sourceStream.read(data, 0, (int) Math.min(length - total, Integer.MAX_VALUE));
            total += count;
        }

        if (options.getUseTransactionalContentMD5()) {
            try {
                final MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(data, 0, data.length);
                md5 = Base64.encode(digest.digest());
            }
            catch (final NoSuchAlgorithmException e) {
                // This wont happen, throw fatal.
                throw Utility.generateNewUnexpectedStorageException(e);
            }
        }

        this.putPagesInternal(pageProps, data, length, md5, accessCondition, options, opContext);
    }
}
