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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.xml.stream.XMLStreamException;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.RequestOptions;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.ResultContinuationType;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.ServiceClient;
import com.microsoft.windowsazure.services.core.storage.StorageCredentials;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsAnonymous;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ExecutionEngine;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.LazySegmentedIterable;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.ListingContext;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.SegmentedStorageOperation;
import com.microsoft.windowsazure.services.core.storage.utils.implementation.StorageOperation;

/**
 * Provides a client for accessing the Windows Azure Blob service.
 * <p>
 * This class provides a point of access to the Blob service. The service client
 * encapsulates the base URI for the Blob service. If the service client will be
 * used for authenticated access, it also encapsulates the credentials for
 * accessing the storage account.
 */
public final class CloudBlobClient extends ServiceClient
{
    /**
     * Holds the maximum size of a blob in bytes that may be uploaded as a
     * single blob.
     */
    private int singleBlobPutThresholdInBytes = BlobConstants.DEFAULT_SINGLE_BLOB_PUT_THRESHOLD_IN_BYTES;

    /**
     * Holds the maximum block size for writing to a block blob.
     */
    private int writeBlockSizeInBytes = BlobConstants.DEFAULT_WRITE_BLOCK_SIZE_IN_BYTES;

    /**
     * Holds the number of bytes a BlobStream will write at once for a page
     * blob.
     */
    private int pageBlobStreamWriteSizeInBytes = BlobConstants.DEFAULT_MINIMUM_PAGE_STREAM_WRITE_IN_BYTES;

    /**
     * Holds the minimum read size when using a BlobReadStream.
     */
    private int streamMinimumReadSizeInBytes = BlobConstants.DEFAULT_MINIMUM_READ_SIZE_IN_BYTES;

    /**
     * Holds the number of simultaneous operations a given blob operation may
     * perform.
     */
    private int concurrentRequestCount = BlobConstants.DEFAULT_CONCURRENT_REQUEST_COUNT;

    /**
     * Holds the default delimiter that may be used to create a virtual
     * directory structure of blobs.
     */
    private String directoryDelimiter = BlobConstants.DEFAULT_DELIMITER;

    /**
     * Creates an instance of the <code>CloudBlobClient</code> class using the
     * specified Blob service endpoint.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the Blob
     *            service endpoint used to create the client.
     */
    public CloudBlobClient(final URI baseUri)
    {
        this(baseUri, StorageCredentialsAnonymous.ANONYMOUS);
    }

    /**
     * Creates an instance of the <code>CloudBlobClient</code> class using the
     * specified Blob service endpoint and account credentials.
     * 
     * @param baseUri
     *            A <code>java.net.URI</code> object that represents the Blob
     *            service endpoint used to create the client.
     * @param credentials
     *            A {@link StorageCredentials} object that represents the
     *            account credentials.
     */
    public CloudBlobClient(final URI baseUri, StorageCredentials credentials)
    {
        super(baseUri, credentials);
        this.directoryDelimiter = BlobConstants.DEFAULT_DELIMITER;
        this.streamMinimumReadSizeInBytes = BlobConstants.DEFAULT_MINIMUM_READ_SIZE_IN_BYTES;
    }

    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a
     * block blob.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or
     *            the absolute URI to the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to
     *         the specified block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlockBlob getBlockBlobReference(final String blobAddressUri)
            throws URISyntaxException, StorageException
    {
        return this.getBlockBlobReference(blobAddressUri, null);
    }

    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a
     * block blob in this container, using the specified snapshot ID.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or
     *            the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the
     *            blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to
     *         the specified block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlockBlob getBlockBlobReference(final String blobAddressUri,
            final String snapshotID) throws StorageException,
            URISyntaxException
    {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI completeUri = PathUtility.appendPathToUri(this.endpoint,
                blobAddressUri);

        return new CloudBlockBlob(completeUri, snapshotID, this);
    }

    /**
     * Returns the number of maximum concurrent requests allowed.
     * 
     * @return The number of maximum concurrent requests allowed.
     */
    public int getConcurrentRequestCount()
    {
        return this.concurrentRequestCount;
    }

    /**
     * Returns a reference to a {@link CloudBlobContainer} object that
     * represents the cloud blob container for the specified address.
     * 
     * @param containerAddress
     *            A <code>String</code> that represents the name of the
     *            container, or the absolute URI to the container.
     * @return A {@link CloudBlobContainer} object that represents a reference
     *         to the cloud blob container.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     * @throws StorageException
     *             If a storage service error occurred.
     */
    public CloudBlobContainer getContainerReference(
            final String containerAddress) throws URISyntaxException,
            StorageException
    {
        Utility.assertNotNullOrEmpty("containerAddress", containerAddress);
        return new CloudBlobContainer(containerAddress, this);
    }

    /**
     * Returns the value for the default delimiter used for cloud blob
     * directories. The default is '/'.
     * 
     * @return The value for the default delimiter.
     */
    public String getDirectoryDelimiter()
    {
        return this.directoryDelimiter;
    }

    /**
     * Returns a reference to the specified virtual blob directory.
     * <p>
     * A blob directory simplifies working with a hierarchical organization of
     * blobs. A blob directory is a blob name prefix that can be used to
     * navigate a hierarchy. The prefix may end in a delimiter character, but a
     * delimiter is not required; the directory can end in any character.
     * 
     * @param relativeAddress
     *            A <code>String</code> that represents the name of the virtual
     *            blob directory, or the absolute URI to the virtual blob
     *            directory.
     * 
     * @return A {@link CloudBlobDirectory} object that represents the specified
     *         virtual blob directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobDirectory getDirectoryReference(final String relativeAddress)
            throws URISyntaxException, StorageException
    {
        Utility.assertNotNullOrEmpty("relativeAddress", relativeAddress);

        return new CloudBlobDirectory(relativeAddress, this);
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a
     * page blob.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or
     *            the absolute URI to the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to
     *         the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobAddressUri)
            throws URISyntaxException, StorageException
    {
        return this.getPageBlobReference(blobAddressUri, null);
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a
     * page blob, using the specified snapshot ID.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or
     *            the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the
     *            blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to
     *         the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobAddressUri,
            final String snapshotID) throws URISyntaxException,
            StorageException
    {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI completeUri = PathUtility.appendPathToUri(this.endpoint,
                blobAddressUri);

        return new CloudPageBlob(completeUri, snapshotID, this);
    }

    /**
     * Returns the write page size in use for this Blob service client.
     * 
     * @return The maximum block size, in bytes, for writing to a page blob.
     */
    public int getPageBlobStreamWriteSizeInBytes()
    {
        return this.pageBlobStreamWriteSizeInBytes;
    }

    /**
     * Returns the threshold size used for writing a single blob for this Blob
     * service client.
     * 
     * @return The maximum size, in bytes, of a blob that may be uploaded as a
     *         single blob, ranging from 1 to 64 MB inclusive. The default value
     *         is 32 MBs.
     *         <p>
     *         If a blob size is above the threshold, it will be uploaded as
     *         blocks.
     */
    public int getSingleBlobPutThresholdInBytes()
    {
        return this.singleBlobPutThresholdInBytes;
    }

    /**
     * Returns the minimum read size in use for this Blob service client.
     * 
     * @return The minimum read size, in bytes, when using a
     *         {@link BlobInputStream} object.
     */
    public int getStreamMinimumReadSizeInBytes()
    {
        return this.streamMinimumReadSizeInBytes;
    }

    /**
     * Returns the write block size in use for this Blob service client.
     * 
     * @return The maximum block size, in bytes, for writing to a block blob
     *         while using a {@link BlobOutputStream} object.
     */
    public int getWriteBlockSizeInBytes()
    {
        return this.writeBlockSizeInBytes;
    }

    /**
     * Returns an enumerable collection of blob containers for this Blob service
     * client.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects
     *         that represent the containers for this client.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers()
    {
        return this.listContainersWithPrefix(null,
                ContainerListingDetails.NONE, null, null);
    }

    /**
     * Returns an enumerable collection of blob containers whose names begin
     * with the specified prefix for this Blob service client.
     * 
     * @param prefix
     *            A <code>String</code> that represents the container name
     *            prefix.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects
     *         that represent the containers for this client whose names begin
     *         with the specified prefix.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix)
    {
        return this.listContainersWithPrefix(prefix,
                ContainerListingDetails.NONE, null, null);
    }

    /**
     * Returns an enumerable collection of blob containers whose names begin
     * with the specified prefix for this Blob service client, using the
     * specified details setting, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the container name
     *            prefix.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether
     *            container metadata will be returned.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any
     *            additional options for the request. Specifying
     *            <code>null</code> will use the default request options from
     *            the associated service client ( {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects
     *         that represents the containers for this client.
     */
    @DoesServiceRequest
    public Iterable<CloudBlobContainer> listContainers(final String prefix,
            final ContainerListingDetails detailsIncluded,
            final BlobRequestOptions options, final OperationContext opContext)
    {
        return this.listContainersWithPrefix(prefix, detailsIncluded, options,
                opContext);
    }

    /**
     * Returns a result segment containing a collection of containers whose
     * names begin with the specified prefix.
     * 
     * @param prefix
     *            A <code>String</code> that represents the container name
     *            prefix.
     * @param detailsIncluded
     *            A value that indicates whether to return container metadata
     *            with the listing.
     * @param maxResults
     *            the maximum results to retrieve.
     * @param continuationToken
     *            A continuation token returned by a previous listing operation.
     * @param options
     *            the request options to use for the operation
     * @param taskReference
     *            a reference to the encapsulating task
     * @param opContext
     *            a tracking object for the operation
     * @return a result segment containing a collection of containers whose
     *         names begin with the specified prefix.
     * @throws IOException
     * @throws URISyntaxException
     * @throws XMLStreamException
     * @throws InvalidKeyException
     * @throws StorageException
     */
    @DoesServiceRequest
    ResultSegment<CloudBlobContainer> listContainersCore(
            final String prefix,
            final ContainerListingDetails detailsIncluded,
            final int maxResults,
            final ResultContinuation continuationToken,
            final RequestOptions options,
            final StorageOperation<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>> taskReference,
            final OperationContext opContext) throws IOException,
            URISyntaxException, XMLStreamException, InvalidKeyException,
            StorageException
    {

        Utility.assertContinuationType(continuationToken,
                ResultContinuationType.CONTAINER);

        final ListingContext listingContext = new ListingContext(prefix,
                maxResults);
        listingContext.setMarker(continuationToken != null ? continuationToken
                .getNextMarker() : null);

        final HttpURLConnection listContainerRequest = ContainerRequest.list(
                this.getEndpoint(), options.getTimeoutIntervalInMs(),
                listingContext, detailsIncluded, opContext);
        taskReference.setConnection(listContainerRequest);

        taskReference.signRequest(this, listContainerRequest, -1L, null);

        ExecutionEngine.processRequest(listContainerRequest, opContext,
                taskReference.getResult());

        if (taskReference.getResult().getStatusCode() != HttpURLConnection.HTTP_OK)
        {
            taskReference.setNonExceptionedRetryableFailure(true);
            return null;
        }

        final ListContainersResponse response = new ListContainersResponse(
                listContainerRequest.getInputStream());
        response.parseResponse(this);

        ResultContinuation newToken = null;

        if (response.getNextMarker() != null)
        {
            newToken = new ResultContinuation();
            newToken.setNextMarker(response.getNextMarker());
            newToken.setContinuationType(ResultContinuationType.CONTAINER);
        }

        final ResultSegment<CloudBlobContainer> resSegment = new ResultSegment<CloudBlobContainer>(
                response.getContainers(this), maxResults, newToken);

        return resSegment;
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers
     * for this Blob service client.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the
     *         enumerable collection of {@link CloudBlobContainer} objects that
     *         represent the containers in this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented()
            throws StorageException
    {
        return this.listContainersSegmented(null, ContainerListingDetails.NONE,
                0, null, null, null);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers
     * whose names begin with the specified prefix for this Blob service client.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the
     *            container name.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the
     *         enumerable collection of {@link CloudBlobContainer} objects that
     *         represent the containers whose names begin with the specified
     *         prefix.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented(
            final String prefix) throws StorageException
    {
        return this.listContainersWithPrefixSegmented(prefix, null, 0, null,
                null, null);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers
     * whose names begin with the specified prefix for this container, using the
     * specified listing details options, request options, and operation
     * context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the
     *            container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether
     *            container metadata will be returned.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a
     *            continuation token returned by a previous listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any
     *            additional options for the request. Specifying
     *            <code>null</code> will use the default request options from
     *            the associated service client ( {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the
     *         enumerable collection of {@link CloudBlobContainer} objects that
     *         represent the containers in this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    public ResultSegment<CloudBlobContainer> listContainersSegmented(
            final String prefix, final ContainerListingDetails detailsIncluded,
            final int maxResults, final ResultContinuation continuationToken,
            final BlobRequestOptions options, final OperationContext opContext)
            throws StorageException
    {

        return this.listContainersWithPrefixSegmented(prefix, detailsIncluded,
                maxResults, continuationToken, options, opContext);
    }

    /**
     * Returns an enumerable collection of blob containers whose names begin
     * with the specified prefix, using the specified details setting, request
     * options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the
     *            container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether
     *            container metadata will be returned.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any
     *            additional options for the request. Specifying
     *            <code>null</code> will use the default request options from
     *            the associated service client ( {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return An enumerable collection of {@link CloudBlobContainer} objects
     *         that represent the containers whose names begin with the
     *         specified prefix.
     */
    @DoesServiceRequest
    protected Iterable<CloudBlobContainer> listContainersWithPrefix(
            final String prefix, final ContainerListingDetails detailsIncluded,
            BlobRequestOptions options, OperationContext opContext)
    {
        if (opContext == null)
        {
            opContext = new OperationContext();
        }

        if (options == null)
        {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this);

        final SegmentedStorageOperation<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>> impl = new SegmentedStorageOperation<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>>(
                options, null)
        {
            @Override
            public ResultSegment<CloudBlobContainer> execute(
                    final CloudBlobClient client, final Void dontCare,
                    final OperationContext opContext) throws Exception
            {

                final ResultSegment<CloudBlobContainer> result = CloudBlobClient.this
                        .listContainersCore(prefix, detailsIncluded, -1,
                                this.getToken(), this.getRequestOptions(),
                                this, opContext);

                // Note, setting the token on the SegmentedStorageOperation is
                // key, this is how the iterator
                // will share the token across executions
                this.setToken(result.getContinuationToken());
                return result;
            }
        };

        return new LazySegmentedIterable<CloudBlobClient, Void, CloudBlobContainer>(
                impl, this, null, options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of blob containers
     * whose names begin with the specified prefix for this container, using the
     * specified listing details options, request options, and operation
     * context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the
     *            container name.
     * @param detailsIncluded
     *            A {@link ContainerListingDetails} value that indicates whether
     *            container metadata will be returned.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a
     *            continuation token returned by a previous listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any
     *            additional options for the request. Specifying
     *            <code>null</code> will use the default request options from
     *            the associated service client ( {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context
     *            for the current operation. This object is used to track
     *            requests to the storage service, and to provide additional
     *            runtime information about the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the
     *         enumerable collection of {@link CloudBlobContainer} objects that
     *         represent the containers in this container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    @DoesServiceRequest
    protected ResultSegment<CloudBlobContainer> listContainersWithPrefixSegmented(
            final String prefix, final ContainerListingDetails detailsIncluded,
            final int maxResults, final ResultContinuation continuationToken,
            BlobRequestOptions options, OperationContext opContext)
            throws StorageException
    {
        if (opContext == null)
        {
            opContext = new OperationContext();
        }

        if (options == null)
        {
            options = new BlobRequestOptions();
        }

        opContext.initialize();
        options.applyDefaults(this);

        Utility.assertContinuationType(continuationToken,
                ResultContinuationType.CONTAINER);

        final StorageOperation<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>> impl = new StorageOperation<CloudBlobClient, Void, ResultSegment<CloudBlobContainer>>(
                options)
        {
            @Override
            public ResultSegment<CloudBlobContainer> execute(
                    final CloudBlobClient client, final Void dontCare,
                    final OperationContext opContext) throws Exception
            {
                return CloudBlobClient.this.listContainersCore(prefix,
                        detailsIncluded, maxResults, continuationToken,
                        this.getRequestOptions(), this, opContext);
            }
        };

        return ExecutionEngine.executeWithRetry(this, null, impl,
                options.getRetryPolicyFactory(), opContext);
    }

    /**
     * Sets the maximum number of concurrent requests allowed for the Blob
     * service client.
     * 
     * @param concurrentRequestCount
     *            The value being assigned as the maximum number of concurrent
     *            requests allowed for the Blob service client.
     */
    public void setConcurrentRequestCount(final int concurrentRequestCount)
    {
        this.concurrentRequestCount = concurrentRequestCount;
    }

    /**
     * Sets the value for the default delimiter used for cloud blob directories.
     * 
     * @param directoryDelimiter
     *            A <code>String</code> that represents the value for the
     *            default directory delimiter.
     */
    public void setDirectoryDelimiter(final String directoryDelimiter)
    {
        this.directoryDelimiter = directoryDelimiter;
    }

    /**
     * Sets the write page size in use for this Blob Service client.
     * 
     * @param pageBlobStreamWriteSizeInBytes
     *            The maximum block size, in bytes, for writing to a page blob.
     *            This value must be a multiple of 512 and less than or equal to
     *            4 MB.
     */
    public void setPageBlobStreamWriteSizeInBytes(
            final int pageBlobStreamWriteSizeInBytes)
    {
        if (pageBlobStreamWriteSizeInBytes > BlobConstants.DEFAULT_WRITE_BLOCK_SIZE_IN_BYTES
                || pageBlobStreamWriteSizeInBytes < BlobConstants.PAGE_SIZE
                || pageBlobStreamWriteSizeInBytes % BlobConstants.PAGE_SIZE != 0)
        {
            throw new IllegalArgumentException("PageBlobStreamWriteSizeInBytes");
        }

        this.pageBlobStreamWriteSizeInBytes = pageBlobStreamWriteSizeInBytes;
    }

    /**
     * Sets the threshold size used for writing a single blob to use with this
     * Blob service client.
     * 
     * @param singleBlobPutThresholdInBytes
     *            The maximum size, in bytes, of a blob that may be uploaded as
     *            a single blob, ranging from 1 MB to 64 MB inclusive. If a blob
     *            size is above the threshold, it will be uploaded as blocks.
     * 
     * @throws IllegalArgumentException
     *             If <code>minimumReadSize</code> is less than 1 MB or greater
     *             than 64 MB.
     */
    public void setSingleBlobPutThresholdInBytes(
            final int singleBlobPutThresholdInBytes)
    {
        if (singleBlobPutThresholdInBytes > BlobConstants.MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES
                || singleBlobPutThresholdInBytes < 1 * Constants.MB)
        {
            throw new IllegalArgumentException(
                    "SingleBlobUploadThresholdInBytes");
        }

        this.singleBlobPutThresholdInBytes = singleBlobPutThresholdInBytes;
    }

    /**
     * Sets the minimum read block size to use with this Blob service client.
     * 
     * @param minimumReadSize
     *            The maximum block size, in bytes, for reading from a block
     *            blob while using a {@link BlobInputStream} object, ranging
     *            from 512 bytes to 64 MB, inclusive.
     */
    public void setStreamMinimumReadSizeInBytes(final int minimumReadSize)
    {
        if (minimumReadSize > 64 * Constants.MB
                || minimumReadSize < BlobConstants.PAGE_SIZE)
        {
            throw new IllegalArgumentException("MinimumReadSize");
        }

        this.streamMinimumReadSizeInBytes = minimumReadSize;
    }

    /**
     * Sets the write block size to use with this Blob service client.
     * 
     * @param writeBlockSizeInBytes
     *            The maximum block size, in bytes, for writing to a block blob
     *            while using a {@link BlobOutputStream} object, ranging from 1
     *            MB to 4 MB, inclusive.
     * 
     * @throws IllegalArgumentException
     *             If <code>writeBlockSizeInBytes</code> is less than 1 MB or
     *             greater than 4 MB.
     */
    public void setWriteBlockSizeInBytes(final int writeBlockSizeInBytes)
    {
        if (writeBlockSizeInBytes > BlobConstants.DEFAULT_WRITE_BLOCK_SIZE_IN_BYTES
                || writeBlockSizeInBytes < 1 * Constants.MB)
        {
            throw new IllegalArgumentException("WriteBlockSizeInBytes");
        }

        this.writeBlockSizeInBytes = writeBlockSizeInBytes;
    }
}
