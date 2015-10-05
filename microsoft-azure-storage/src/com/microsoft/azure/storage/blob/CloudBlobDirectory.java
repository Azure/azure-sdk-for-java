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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.DoesServiceRequest;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a virtual directory of blobs, designated by a delimiter character.
 * <p>
 * Containers, which are encapsulated as {@link CloudBlobContainer} objects, hold directories, and directories hold
 * block blobs and page blobs. Directories can also contain sub-directories.
 */
public final class CloudBlobDirectory implements ListBlobItem {

    /**
     * Holds the Blobs container Reference.
     */
    private final CloudBlobContainer container;

    /**
     * Represents the blob's directory.
     */
    private CloudBlobDirectory parent;

    /**
     * Represents the blob client.
     */
    private final CloudBlobClient blobServiceClient;

    /**
     * Holds the list of URIs for all locations.
     */
    private final StorageUri storageUri;

    /**
     * Holds the prefix for the directory.
     */
    private final String prefix;

    /**
     * Creates an instance of the <code>CloudBlobDirectory</code> class using the specified address, directory parent,
     * and client.
     * 
     * @param uri
     *            A {@link StorageUri} that represents the blob directory's address.
     * @param prefix
     *            A <code>String</code> that represents the blob directory's prefix.
     * @param client
     *            A {@link CloudBlobClient} object that represents the associated service client.
     */
    protected CloudBlobDirectory(final StorageUri uri, final String prefix, final CloudBlobClient client,
            final CloudBlobContainer container) {
        this(uri, prefix, client, container, null);
    }

    /**
     * Creates an instance of the <code>CloudBlobDirectory</code> class using the specified address, directory parent,
     * and client.
     * 
     * @param uri
     *            A {@link StorageUri} that represents the blob directory's address.
     * @param prefix
     *            A <code>String</code> that represents the blob directory's prefix.
     * @param parent
     *            A <code>CloudBlobDirectory</code> object that represents the parent directory, if applicable.
     * @param client
     *            A {@link CloudBlobClient} object that represents the associated service client.
     */
    protected CloudBlobDirectory(final StorageUri uri, final String prefix, final CloudBlobClient client,
            final CloudBlobContainer container, final CloudBlobDirectory parent) {
        Utility.assertNotNull("uri", uri);
        Utility.assertNotNull("client", client);
        Utility.assertNotNull("container", container);

        this.blobServiceClient = client;
        this.parent = parent;
        this.container = container;
        this.prefix = prefix;
        this.storageUri = uri;
    }

    /**
     * Returns a reference to a {@link CloudAppendBlob} object that represents an append blob in the directory.
     * 
     * @param blobName
     *            A <code>String</code> that represents the name of the blob.
     * 
     * @return A {@link CloudAppendBlob} object that represents a reference to the specified append blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudAppendBlob getAppendBlobReference(final String blobName) throws URISyntaxException, StorageException {
        return this.getAppendBlobReference(blobName, null);
    }

    /**
     * Returns a reference to a {@link CloudAppendBlob} object that represents an append blob in the directory, using the
     * specified snapshot ID.
     * 
     * @param blobName
     *            A <code>String</code> that represents the name of the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the blob.
     * 
     * @return A {@link CloudAppendBlob} object that represents a reference to the specified append blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudAppendBlob getAppendBlobReference(final String blobName, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobName", blobName);
        return new CloudAppendBlob(this.getPrefix().concat(blobName), snapshotID, this.getContainer());
    }
    
    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a block blob in this directory.
     * 
     * @param blobName
     *            A <code>String</code> that represents the name of the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified block blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlockBlob getBlockBlobReference(final String blobName) throws URISyntaxException, StorageException {
        return this.getBlockBlobReference(blobName, null);
    }

    /**
     * Returns a reference to a {@link CloudBlockBlob} object that represents a block blob in this directory, using the
     * specified snapshot ID.
     * 
     * @param blobName
     *            A <code>String</code> that represents the name of the blob.
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
    public CloudBlockBlob getBlockBlobReference(final String blobName, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobName", blobName);
        return new CloudBlockBlob(this.getPrefix().concat(blobName), snapshotID, this.getContainer());
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
    @Override
    public CloudBlobContainer getContainer() throws StorageException, URISyntaxException {
        return this.container;
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a page blob in the directory.
     * 
     * @param blobName
     *            A <code>String</code> that represents the name of the blob.
     * 
     * @return A {@link CloudPageBlob} object that represents a reference to the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobName) throws URISyntaxException, StorageException {
        return this.getPageBlobReference(blobName, null);
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a page blob in the directory, using the
     * specified snapshot ID.
     * 
     * @param blobName
     *            A <code>String</code> that represents the name of the blob.
     * @param snapshotID
     *            A <code>String</code> that represents the snapshot ID of the blob.
     * 
     * @return A {@link CloudPageBlob} object that represents a reference to the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobName, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobName", blobName);
        return new CloudPageBlob(this.getPrefix().concat(blobName), snapshotID, this.getContainer());
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
    @Override
    public CloudBlobDirectory getParent() throws URISyntaxException, StorageException {
        if (this.parent == null) {
            final String parentName = CloudBlob.getParentNameFromURI(this.getStorageUri(),
                    this.blobServiceClient.getDirectoryDelimiter(), this.getContainer());

            if (parentName != null) {
                StorageUri parentURI = PathUtility.appendPathToUri(this.container.getStorageUri(), parentName);
                this.parent = new CloudBlobDirectory(parentURI, parentName, this.blobServiceClient, this.getContainer());
            }
        }
        return this.parent;
    }

    /**
     * Returns the prefix for this directory.
     * 
     * @return A <code>String</code> that represents the prefix for this directory.
     */
    public String getPrefix() {
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
     * @param directoryName
     *            A <code>String</code> that represents the name of the virtual subdirectory.
     * 
     * @return A <code>CloudBlobDirectory</code> object that represents a virtual blob directory beneath this directory.
     * 
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobDirectory getDirectoryReference(String directoryName) throws URISyntaxException {
        Utility.assertNotNullOrEmpty("directoryName", directoryName);

        if (!directoryName.endsWith(this.blobServiceClient.getDirectoryDelimiter())) {
            directoryName = directoryName.concat(this.blobServiceClient.getDirectoryDelimiter());
        }
        final String subDirName = this.getPrefix().concat(directoryName);

        final StorageUri address = PathUtility.appendPathToUri(this.storageUri, directoryName,
                this.blobServiceClient.getDirectoryDelimiter());

        return new CloudBlobDirectory(address, subDirName, this.blobServiceClient, this.container, this);
    }

    /**
     * Returns the URI for this directory.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI for this directory.
     */
    @Override
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Returns the list of URIs for all locations.
     * 
     * @return A {@link StorageUri} that represents the list of URIs for all locations..
     */
    @Override
    public final StorageUri getStorageUri() {
        return this.storageUri;
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
    public Iterable<ListBlobItem> listBlobs(String prefix, final boolean useFlatBlobListing,
            final EnumSet<BlobListingDetails> listingDetails, final BlobRequestOptions options,
            final OperationContext opContext) throws URISyntaxException, StorageException {
        prefix = prefix == null ? Constants.EMPTY_STRING : prefix;
        return this.getContainer().listBlobs(this.getPrefix().concat(prefix), useFlatBlobListing, listingDetails,
                options, opContext);
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
    public ResultSegment<ListBlobItem> listBlobsSegmented(String prefix, final boolean useFlatBlobListing,
            final EnumSet<BlobListingDetails> listingDetails, final Integer maxResults,
            final ResultContinuation continuationToken, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException, URISyntaxException {
        prefix = prefix == null ? Constants.EMPTY_STRING : prefix;
        return this.getContainer().listBlobsSegmented(this.getPrefix().concat(prefix), useFlatBlobListing,
                listingDetails, maxResults, continuationToken, options, opContext);
    }
}
