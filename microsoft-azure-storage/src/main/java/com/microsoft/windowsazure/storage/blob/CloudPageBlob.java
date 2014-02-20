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
package com.microsoft.windowsazure.storage.blob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.microsoft.windowsazure.storage.AccessCondition;
import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.DoesServiceRequest;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageUri;
import com.microsoft.windowsazure.storage.core.Base64;
import com.microsoft.windowsazure.storage.core.ExecutionEngine;
import com.microsoft.windowsazure.storage.core.RequestLocationMode;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.StorageRequest;
import com.microsoft.windowsazure.storage.core.Utility;

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
        this(new StorageUri(uri));
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified relative URI and storage service
     * client.
     * 
     * @param uri
     *            A <code>StorageUri</code> object that represents the relative URI to the blob, beginning with the
     *            container name.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final StorageUri uri) throws StorageException {
        super(BlobType.PAGE_BLOB);

        Utility.assertNotNull("blobAbsoluteUri", uri);
        this.setStorageUri(uri);
        this.parseURIQueryStringAndVerify(uri, null, Utility.determinePathStyleFromUri(uri.getPrimaryUri(), null));;
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
        this(new StorageUri(uri), client);
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified URI and cloud blob client.
     * 
     * @param uri
     *            A <code>StorageUri</code> object that represents the absolute URI to the blob, beginning with the
     *            container name.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final StorageUri uri, final CloudBlobClient client) throws StorageException {
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
        this(new StorageUri(uri), client, container);
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified URI, cloud blob client, and cloud
     * blob container.
     * 
     * @param uri
     *            A <code>StorageUri</code> object that represents the relative URI to the blob, beginning with the
     *            container name.
     * @param client
     *            A {@link CloudBlobClient} object that specifies the endpoint for the Blob service.
     * @param container
     *            A {@link CloudBlobContainer} object that represents the container to use for the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final StorageUri uri, final CloudBlobClient client, final CloudBlobContainer container)
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
        this(new StorageUri(uri), snapshotID, client);
    }

    /**
     * Creates an instance of the <code>CloudPageBlob</code> class using the specified URI, snapshot ID, and cloud blob
     * client.
     * 
     * @param uri
     *            A <code>StorageUri</code> object that represents the absolute URI to the blob, beginning with the
     *            container name.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot version, if applicable.
     * @param client
     *            A {@link CloudBlobContainer} object that represents the container to use for the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudPageBlob(final StorageUri uri, final String snapshotID, final CloudBlobClient client)
            throws StorageException {
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
        this.clearPages(offset, length, null /* accessCondition */, null /* options */, null /* opContext */);
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
            throw new IllegalArgumentException(SR.INVALID_PAGE_START_OFFSET);
        }

        if (length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException(SR.INVALID_PAGE_BLOB_LENGTH);
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.applyDefaults(options, BlobType.PAGE_BLOB, this.blobServiceClient);
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
        this.create(length, null /* accessCondition */, null /* options */, null /* opContext */);
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
            throw new IllegalArgumentException(SR.INVALID_PAGE_BLOB_LENGTH);
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.applyDefaults(options, BlobType.PAGE_BLOB, this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.createImpl(length, accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> createImpl(final long length,
            final AccessCondition accessCondition, final BlobRequestOptions options) throws StorageException {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.put(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options.getTimeoutIntervalInMs(), blob.properties, BlobType.PAGE_BLOB, length, accessCondition,
                        options, context);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudBlob blob, OperationContext context) {
                BlobRequest.addMetadata(connection, blob.metadata, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobAndQueueRequest(connection, client, 0L, null);
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }

        };

        return putRequest;
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
        return this.downloadPageRanges(null /* accessCondition */, null /* options */, null /* opContext */);
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

        options = BlobRequestOptions.applyDefaults(options, BlobType.PAGE_BLOB, this.blobServiceClient);

        return ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                this.downloadPageRangesImpl(accessCondition, options), options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, ArrayList<PageRange>> downloadPageRangesImpl(
            final AccessCondition accessCondition, final BlobRequestOptions options) throws StorageException {
        final StorageRequest<CloudBlobClient, CloudBlob, ArrayList<PageRange>> getRequest = new StorageRequest<CloudBlobClient, CloudBlob, ArrayList<PageRange>>(
                options, this.getStorageUri()) {

            @Override
            public void setRequestLocationMode() {
                this.setRequestLocationMode(RequestLocationMode.PRIMARY_OR_SECONDARY);
            }

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.getPageRanges(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options.getTimeoutIntervalInMs(), blob.snapshotID, accessCondition, options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobAndQueueRequest(connection, client, -1L, null);
            }

            @Override
            public ArrayList<PageRange> preProcessResponse(CloudBlob parentObject, CloudBlobClient client,
                    OperationContext context) throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                }

                return null;
            }

            @Override
            public ArrayList<PageRange> postProcessResponse(HttpURLConnection connection, CloudBlob blob,
                    CloudBlobClient client, OperationContext context, ArrayList<PageRange> storageObject)
                    throws Exception {
                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                blob.updateLengthFromResponse(this.getConnection());

                return BlobDeserializer.getPageRanges(this.getConnection().getInputStream());
            }

        };

        return getRequest;
    }

    /**
     * Opens an output stream object to write data to the page blob. The page blob must already exist.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public BlobOutputStream openWriteExisting() throws StorageException {
        return this
                .openOutputStreamInternal(null /* length */, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the page blob, using the specified lease ID, request options and
     * operation context. The page blob must already exist.
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
    @DoesServiceRequest
    public BlobOutputStream openWriteExisting(AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        return this
                .openOutputStreamInternal(null /* length */, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the page blob. The page blob does not need to yet exist and will
     * be created with the length specified.
     * 
     * @deprecated As of release 0.6.0, replaced by {@link CloudPageBlob#openWriteNew(long)}
     * 
     * @param length
     *            The length, in bytes, of the stream to create. This value must be a multiple of 512.
     * 
     * @return A {@link BlobOutputStream} object used to write data to the blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @Deprecated
    @DoesServiceRequest
    public BlobOutputStream openOutputStream(final long length) throws StorageException {
        return this
                .openOutputStreamInternal(length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the page blob, using the specified lease ID, request options and
     * operation context. The page blob does not need to yet exist and will be created with the length specified.
     * 
     * @deprecated As of release 0.6.0, replaced by
     *             {@link CloudPageBlob#openWriteNew(long, AccessCondition, BlobRequestOptions, OperationContext)}
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
    @Deprecated
    @DoesServiceRequest
    public BlobOutputStream openOutputStream(final long length, AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        return openOutputStreamInternal(length, accessCondition, options, opContext);
    }

    /**
     * Opens an output stream object to write data to the page blob. The page blob does not need to yet exist and will
     * be created with the length specified.
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
    public BlobOutputStream openWriteNew(final long length) throws StorageException {
        return this
                .openOutputStreamInternal(length, null /* accessCondition */, null /* options */, null /* opContext */);
    }

    /**
     * Opens an output stream object to write data to the page blob, using the specified lease ID, request options and
     * operation context. The page blob does not need to yet exist and will be created with the length specified.
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
    public BlobOutputStream openWriteNew(final long length, AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        return openOutputStreamInternal(length, accessCondition, options, opContext);
    }

    /**
     * Opens an output stream object to write data to the page blob, using the specified lease ID, request options and
     * operation context. If the length is specified, a new page blob will be created with the length specified.
     * Otherwise, the page blob must already exist and a stream of its current length will be opened.
     * 
     * @param length
     *            The length, in bytes, of the stream to create. This value must be a multiple of 512 or null if the
     *            page blob already exists.
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
    private BlobOutputStream openOutputStreamInternal(Long length, AccessCondition accessCondition,
            BlobRequestOptions options, OperationContext opContext) throws StorageException {
        if (opContext == null) {
            opContext = new OperationContext();
        }

        assertNoWriteOperationForSnapshot();

        options = BlobRequestOptions.applyDefaults(options, BlobType.PAGE_BLOB, this.blobServiceClient, false);

        if (options.getStoreBlobContentMD5()) {
            throw new IllegalArgumentException(SR.BLOB_MD5_NOT_SUPPORTED_FOR_PAGE_BLOBS);
        }

        if (length != null) {
            if (length % BlobConstants.PAGE_SIZE != 0) {
                throw new IllegalArgumentException(SR.INVALID_PAGE_BLOB_LENGTH);
            }

            this.create(length, accessCondition, options, opContext);
        }
        else {
            this.downloadAttributes(accessCondition, options, opContext);
            length = this.getProperties().getLength();
        }

        if (accessCondition != null) {
            accessCondition = AccessCondition.generateLeaseCondition(accessCondition.getLeaseID());
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
        ExecutionEngine.executeWithRetry(this.blobServiceClient, this,
                putPagesImpl(pageProperties, data, length, md5, accessCondition, options, opContext),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> putPagesImpl(final PageProperties pageProperties,
            final byte[] data, final long length, final String md5, final AccessCondition accessCondition,
            final BlobRequestOptions options, final OperationContext opContext) throws StorageException {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                if (pageProperties.getPageOperation() == PageOperationType.UPDATE) {
                    this.setSendStream(new ByteArrayInputStream(data));
                    this.setLength(length);
                }

                return BlobRequest.putPage(blob.getTransformedAddress(opContext).getUri(this.getCurrentLocation()),
                        options.getTimeoutIntervalInMs(), pageProperties, accessCondition, options, opContext);
            }

            @Override
            public void setHeaders(HttpURLConnection connection, CloudBlob blob, OperationContext context) {
                if (pageProperties.getPageOperation() == PageOperationType.UPDATE) {
                    if (options.getUseTransactionalContentMD5()) {
                        connection.setRequestProperty(Constants.HeaderConstants.CONTENT_MD5, md5);
                    }
                }
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (pageProperties.getPageOperation() == PageOperationType.UPDATE) {
                    StorageRequest.signBlobAndQueueRequest(connection, client, length, null);
                }
                else {
                    StorageRequest.signBlobAndQueueRequest(connection, client, 0L, null);
                }
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
    }

    /**
     * Resizes the page blob to the specified size.
     * 
     * @param size
     *            The size of the page blob, in bytes.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public void resize(long size) throws StorageException {
        this.resize(size, null /* accessCondition */, null /* options */, null /* operationContext */);
    }

    /**
     * Resizes the page blob to the specified size.
     * 
     * @param size
     *            The size of the page blob, in bytes.
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
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public void resize(long size, AccessCondition accessCondition, BlobRequestOptions options,
            OperationContext opContext) throws StorageException {
        assertNoWriteOperationForSnapshot();

        if (size % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException(SR.INVALID_PAGE_BLOB_LENGTH);
        }

        if (opContext == null) {
            opContext = new OperationContext();
        }

        opContext.initialize();
        options = BlobRequestOptions.applyDefaults(options, this.properties.getBlobType(), this.blobServiceClient);

        ExecutionEngine.executeWithRetry(this.blobServiceClient, this, this.resizeImpl(size, accessCondition, options),
                options.getRetryPolicyFactory(), opContext);
    }

    private StorageRequest<CloudBlobClient, CloudBlob, Void> resizeImpl(final long size,
            final AccessCondition accessCondition, final BlobRequestOptions options) throws StorageException {
        final StorageRequest<CloudBlobClient, CloudBlob, Void> putRequest = new StorageRequest<CloudBlobClient, CloudBlob, Void>(
                options, this.getStorageUri()) {

            @Override
            public HttpURLConnection buildRequest(CloudBlobClient client, CloudBlob blob, OperationContext context)
                    throws Exception {
                return BlobRequest.resize(blob.getTransformedAddress(context).getUri(this.getCurrentLocation()),
                        options.getTimeoutIntervalInMs(), size, accessCondition, options, context);
            }

            @Override
            public void signRequest(HttpURLConnection connection, CloudBlobClient client, OperationContext context)
                    throws Exception {
                StorageRequest.signBlobAndQueueRequest(connection, client, 0L, null);
            }

            @Override
            public Void preProcessResponse(CloudBlob blob, CloudBlobClient client, OperationContext context)
                    throws Exception {
                if (this.getResult().getStatusCode() != HttpURLConnection.HTTP_OK) {
                    this.setNonExceptionedRetryableFailure(true);
                    return null;
                }

                blob.getProperties().setLength(size);
                blob.updateEtagAndLastModifiedFromResponse(this.getConnection());
                return null;
            }
        };

        return putRequest;
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
        this.upload(sourceStream, length, null /* accessCondition */, null /* options */, null /* opContext */);
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

        options = BlobRequestOptions.applyDefaults(options, BlobType.PAGE_BLOB, this.blobServiceClient);

        if (length <= 0 || length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException(SR.INVALID_PAGE_BLOB_LENGTH);
        }

        if (options.getStoreBlobContentMD5()) {
            throw new IllegalArgumentException(SR.BLOB_MD5_NOT_SUPPORTED_FOR_PAGE_BLOBS);
        }

        if (sourceStream.markSupported()) {
            // Mark sourceStream for current position.
            sourceStream.mark(Constants.MAX_MARK_LENGTH);
        }

        final BlobOutputStream streamRef = this.openWriteNew(length, accessCondition, options, opContext);
        try {
            streamRef.write(sourceStream, length);
        }
        finally {
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
        this.uploadPages(sourceStream, offset, length, null /* accessCondition */, null /* options */, null /* opContext */);
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
            throw new IllegalArgumentException(SR.INVALID_PAGE_START_OFFSET);
        }

        if (length == 0 || length % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException(SR.INVALID_PAGE_BLOB_LENGTH);
        }

        if (length > 4 * Constants.MB) {
            throw new IllegalArgumentException(SR.INVALID_MAX_WRITE_SIZE);
        }

        assertNoWriteOperationForSnapshot();

        if (opContext == null) {
            opContext = new OperationContext();
        }

        options = BlobRequestOptions.applyDefaults(options, BlobType.PAGE_BLOB, this.blobServiceClient);

        final PageProperties pageProps = new PageProperties();
        pageProps.setPageOperation(PageOperationType.UPDATE);
        pageProps.getRange().setStartOffset(offset);
        pageProps.getRange().setEndOffset(offset + length - 1);
        final byte[] data = new byte[(int) length];
        String md5 = null;

        int count = 0;
        int total = 0;
        while (total < length) {
            count = sourceStream.read(data, total, (int) Math.min(length - total, Integer.MAX_VALUE));
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

    /**
     * Sets the number of bytes to buffer when writing to a {@link BlobOutputStream}.
     * 
     * @param pageBlobStreamWriteSizeInBytes
     *            The maximum number of bytes to buffer when writing to a page blob stream. This value must be a
     *            multiple of 512 and
     *            less than or equal to 4 MB.
     * 
     * @throws IllegalArgumentException
     *             If <code>streamWriteSizeInBytes</code> is less than 512, greater than 4 MB, or not a multiple or 512.
     */
    @Override
    public void setStreamWriteSizeInBytes(final int streamWriteSizeInBytes) {
        if (streamWriteSizeInBytes > BlobConstants.MAX_COMMIT_SIZE_4_MB
                || streamWriteSizeInBytes < BlobConstants.PAGE_SIZE
                || streamWriteSizeInBytes % BlobConstants.PAGE_SIZE != 0) {
            throw new IllegalArgumentException("StreamWriteSizeInBytes");
        }

        this.streamWriteSizeInBytes = streamWriteSizeInBytes;
    }
}
