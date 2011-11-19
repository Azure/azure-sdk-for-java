package com.microsoft.windowsazure.services.blob.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.DoesServiceRequest;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;
import com.microsoft.windowsazure.services.core.storage.ResultSegment;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.PathUtility;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Represents a virtual directory of blobs, designated by a delimiter character.
 * <p>
 * Containers, which are encapsulated as {@link CloudBlobContainer} objects, hold directories, and directories hold
 * block blobs and page blobs. Directories can also contain sub-directories.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class CloudBlobDirectory implements ListBlobItem {

    /**
     * Holds the Blobs container Reference.
     */
    private CloudBlobContainer container;

    /**
     * Represents the blob's directory.
     */
    protected CloudBlobDirectory parent;

    /**
     * Represents the blob client.
     */
    protected CloudBlobClient blobServiceClient;

    /**
     * Holds the URI of the directory.
     */
    private final URI uri;

    /**
     * Holds the prefix for the directory.
     */
    private String prefix;

    /**
     * Creates an instance of the <code>CloudBlobDirectory</code> class using the specified address and client.
     * 
     * @param relativeAddress
     *            A <code>String</code> that represents the blob directory's relative address.
     * @param client
     *            A {@link CloudBlobClient} object that represents the associated service client.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected CloudBlobDirectory(String relativeAddress, final CloudBlobClient client) throws URISyntaxException,
            StorageException {
        Utility.assertNotNullOrEmpty("relativeAddress", relativeAddress);
        Utility.assertNotNull("client", client);

        this.blobServiceClient = client;

        if (this.blobServiceClient.getDirectoryDelimiter() != null
                && !relativeAddress.endsWith(this.blobServiceClient.getDirectoryDelimiter())) {
            relativeAddress = relativeAddress.concat(this.blobServiceClient.getDirectoryDelimiter());
        }

        this.uri = PathUtility.appendPathToUri(this.blobServiceClient.getEndpoint(), relativeAddress);
    }

    /**
     * Creates an instance of the <code>CloudBlobDirectory</code> class using the specified address, directory parent,
     * and client.
     * 
     * @param uri
     *            A <code>String</code> that represents the blob directory's address.
     * @param parent
     *            A <code>CloudBlobDirectory</code> object that represents the parent directory, if applicable.
     * @param client
     *            A {@link CloudBlobClient} object that represents the associated service client.
     */
    protected CloudBlobDirectory(final URI uri, final CloudBlobDirectory parent, final CloudBlobClient client) {
        Utility.assertNotNull("uri", uri);
        Utility.assertNotNull("client", client);

        this.blobServiceClient = client;
        this.uri = uri;
        this.parent = parent;
    }

    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a block blob in this directory.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlockBlob getBlockBlobReference(final String blobAddressUri)
            throws URISyntaxException, StorageException {
        return this.getBlockBlobReference(blobAddressUri, null);
    }

    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a block blob in this directory, using the
     * specified snapshot ID.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlockBlob getBlockBlobReference(final String blobAddressUri, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI address =
                PathUtility.appendPathToUri(this.uri, blobAddressUri, this.blobServiceClient.getDirectoryDelimiter());

        final CloudBlockBlob retBlob = new CloudBlockBlob(address, snapshotID, this.blobServiceClient);
        retBlob.setContainer(this.container);
        return retBlob;
    }

    /**
     * Returns the container for this directory.
     * 
     * @return A {@link CloudBlobContainer} that represents the container for this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobContainer getContainer() throws StorageException, URISyntaxException {
        if (this.container == null) {
            final URI containerURI = PathUtility.getContainerURI(this.uri, this.blobServiceClient.isUsePathStyleUris());
            this.container = new CloudBlobContainer(containerURI, this.blobServiceClient);
        }

        return this.container;
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a page blob in the directory.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobAddressUri) throws URISyntaxException, StorageException {
        return this.getPageBlobReference(blobAddressUri, null);
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a page blob in the directory, using the
     * specified snapshot ID.
     * 
     * @param blobAddressUri
     *            A <code>String</code> that represents the name of the blob, or the absolute URI to the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobAddressUri, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobAddressUri", blobAddressUri);

        final URI address =
                PathUtility.appendPathToUri(this.uri, blobAddressUri, this.blobServiceClient.getDirectoryDelimiter());

        final CloudPageBlob retBlob = new CloudPageBlob(address, snapshotID, this.blobServiceClient);
        retBlob.setContainer(this.container);

        return retBlob;
    }

    /**
     * Returns the parent directory of this directory.
     * 
     * @return A {@link CloudBlobDirectory} object that represents the parent of this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobDirectory getParent() throws URISyntaxException, StorageException {
        if (this.parent == null) {
            final URI parentURI =
                    PathUtility.getParentAddress(this.uri,
                            this.blobServiceClient.getDirectoryDelimiter(),
                            this.blobServiceClient.isUsePathStyleUris());
            this.parent = new CloudBlobDirectory(parentURI, null, this.blobServiceClient);
        }
        return this.parent;
    }

    /**
     * Returns the prefix for this directory.
     * 
     * @return A <code>String</code> that represents the prefix for this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     */
    protected String getPrefix() throws StorageException {
        if (this.prefix == null) {
            try {
                final String containerUri = this.getContainer().getUri().toString().concat("/");
                this.prefix = Utility.safeRelativize(new URI(containerUri), this.uri);
            } catch (final URISyntaxException e) {
                final StorageException wrappedUnexpectedException = Utility.generateNewUnexpectedStorageException(e);
                throw wrappedUnexpectedException;
            }
        }

        return this.prefix;
    }

    /**
     * Returns the Blob service client associated with this directory.
     * 
     * @return An {@link CloudBlobClient} object that represents the service client associated with the directory.
     */
    public CloudBlobClient getServiceClient() {
        return this.blobServiceClient;
    }

    /**
     * Returns a reference to a virtual blob directory beneath this directory.
     * 
     * @param relativeAddress
     *            A <code>String</code> that represents the name of the virtual blob directory, or the absolute URI to
     *            the virtual blob directory.
     * 
     * @return A <code>CloudBlobDirectory</code> object that represents a virtual blob directory beneath this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobDirectory getSubDirectoryReference(final String relativeAddress)
            throws StorageException, URISyntaxException {
        Utility.assertNotNullOrEmpty("relativeAddress", relativeAddress);

        final URI address =
                PathUtility.appendPathToUri(this.uri, relativeAddress, this.blobServiceClient.getDirectoryDelimiter());

        return new CloudBlobDirectory(address, this, this.blobServiceClient);
    }

    /**
     * Returns the URI for this directory.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for this directory.
     */
    public URI getUri() {
        return this.uri;
    }

    /**
     * Returns an enumerable collection of blob items for the directory.
     * 
     * @return An enumerable collection of {@link ListBlobItem} objects that represent the block items in this
     *         container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public Iterable<ListBlobItem> listBlobs() throws StorageException, URISyntaxException {
        return this.getContainer().listBlobs(this.getPrefix());
    }

    /**
     * Returns an enumerable collection of blob items whose names begin with the specified prefix for the directory.
     * 
     * @param prefix
     *            A <code>String</code> that represents the blob name prefix.
     * 
     * @return An enumerable collection of {@link ListBlobItem} objects that represent the block items whose names begin
     *         with the specified prefix in this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public Iterable<ListBlobItem> listBlobs(String prefix) throws URISyntaxException, StorageException {
        prefix = prefix == null ? Constants.EMPTY_STRING : prefix;
        return this.getContainer().listBlobs(this.getPrefix().concat(prefix));
    }

    /**
     * Returns an enumerable collection of blob items whose names begin with the specified prefix, using the specified
     * flat or hierarchical option, listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the blob name.
     * @param useFlatBlobListing
     *            <code>true</code> to indicate that the returned list will be flat; <code>false</code> to indicate that
     *            the returned list will be hierarchical.
     * @param listingDetails
     *            A <code>java.util.EnumSet</code> object that contains {@link BlobListingDetails} values that indicate
     *            whether snapshots, metadata, and/or uncommitted blocks are returned. Committed blocks are always
     *            returned.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return An enumerable collection of {@link ListBlobItem} objects that represent the block items whose names begin
     *         with the specified prefix in this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public Iterable<ListBlobItem> listBlobs(
            String prefix, final boolean useFlatBlobListing, final EnumSet<BlobListingDetails> listingDetails,
            final BlobRequestOptions options, final OperationContext opContext)
            throws URISyntaxException, StorageException {
        prefix = prefix == null ? Constants.EMPTY_STRING : prefix;
        return this.getContainer().listBlobs(this.getPrefix().concat(prefix),
                useFlatBlobListing,
                listingDetails,
                options,
                opContext);
    }

    /**
     * Returns a result segment of an enumerable collection of blob items in the directory.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListBlobItem} objects that represent the blob items in the directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public ResultSegment<ListBlobItem> listBlobsSegmented() throws StorageException, URISyntaxException {
        return this.getContainer().listBlobsSegmented(this.getPrefix());
    }

    /**
     * Returns a result segment containing a collection of blob items whose names begin with the specified prefix.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the blob name.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListBlobItem} objects that represent the blob items whose names begin with the specified prefix in
     *         the directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public ResultSegment<ListBlobItem> listBlobsSegmented(String prefix) throws StorageException, URISyntaxException {
        prefix = prefix == null ? Constants.EMPTY_STRING : prefix;
        return this.getContainer().listBlobsSegmented(this.getPrefix().concat(prefix));
    }

    /**
     * Returns a result segment containing a collection of blob items whose names begin with the specified prefix, using
     * the specified flat or hierarchical option, listing details options, request options, and operation context.
     * 
     * @param prefix
     *            A <code>String</code> that represents the prefix of the blob name.
     * @param useFlatBlobListing
     *            <code>true</code> to indicate that the returned list will be flat; <code>false</code> to indicate that
     *            the returned list will be hierarchical.
     * @param listingDetails
     *            A <code>java.util.EnumSet</code> object that contains {@link BlobListingDetails} values that indicate
     *            whether snapshots, metadata, and/or uncommitted blocks are returned. Committed blocks are always
     *            returned.
     * @param maxResults
     *            The maximum number of results to retrieve.
     * @param continuationToken
     *            A {@link ResultContinuation} object that represents a continuation token returned by a previous
     *            listing operation.
     * @param options
     *            A {@link BlobRequestOptions} object that specifies any additional options for the request. Specifying
     *            <code>null</code> will use the default request options from the associated service client (
     *            {@link CloudBlobClient}).
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * 
     * @return A {@link ResultSegment} object that contains a segment of the enumerable collection of
     *         {@link ListBlobItem} objects that represent the block items whose names begin with the specified prefix
     *         in the directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    @DoesServiceRequest
    public ResultSegment<ListBlobItem> listBlobsSegmented(
            String prefix, final boolean useFlatBlobListing, final EnumSet<BlobListingDetails> listingDetails,
            final int maxResults, final ResultContinuation continuationToken, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException, URISyntaxException {
        prefix = prefix == null ? Constants.EMPTY_STRING : prefix;
        return this.getContainer().listBlobsSegmented(this.getPrefix().concat(prefix),
                useFlatBlobListing,
                listingDetails,
                maxResults,
                continuationToken,
                options,
                opContext);
    }
}
