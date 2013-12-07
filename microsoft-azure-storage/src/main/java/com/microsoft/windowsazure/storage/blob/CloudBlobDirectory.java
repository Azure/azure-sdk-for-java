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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.DoesServiceRequest;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.ResultContinuation;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageUri;
import com.microsoft.windowsazure.storage.core.PathUtility;
import com.microsoft.windowsazure.storage.core.Utility;

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
    private CloudBlobContainer container;

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
    private String prefix;

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
     * @throws URISyntaxException
     */
    protected CloudBlobDirectory(final StorageUri uri, final CloudBlobDirectory parent, final CloudBlobClient client)
            throws URISyntaxException {
        Utility.assertNotNull("uri", uri);
        Utility.assertNotNull("client", client);

        // if the uri to the directory does not end in the delimiter, add the delimiter
        // do this by constructing a new URI with a modified path as this path will be encoded by the URI constructor
        if (client.getDirectoryDelimiter() != null) {
            URI primaryUri = uri.getPrimaryUri();
            URI secondaryUri = uri.getSecondaryUri();
            if (primaryUri != null && !primaryUri.getPath().endsWith(client.getDirectoryDelimiter())) {
                primaryUri = new URI(primaryUri.getScheme(), primaryUri.getAuthority(), primaryUri.getPath().concat(
                        client.getDirectoryDelimiter()), primaryUri.getQuery(), primaryUri.getFragment());
            }
            if (secondaryUri != null && !secondaryUri.getPath().endsWith(client.getDirectoryDelimiter())) {
                secondaryUri = new URI(secondaryUri.getScheme(), secondaryUri.getAuthority(), secondaryUri.getPath()
                        .concat(client.getDirectoryDelimiter()), secondaryUri.getQuery(), secondaryUri.getFragment());
            }
            this.storageUri = new StorageUri(primaryUri, secondaryUri);
        }

        else {
            this.storageUri = uri;
        }

        this.blobServiceClient = client;
        this.parent = parent;
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

        final StorageUri address = PathUtility.appendPathToUri(this.storageUri, blobName,
                this.blobServiceClient.getDirectoryDelimiter());

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
    @Override
    public CloudBlobContainer getContainer() throws StorageException, URISyntaxException {
        if (this.container == null) {
            final StorageUri containerURI = PathUtility.getContainerURI(this.storageUri,
                    this.blobServiceClient.isUsePathStyleUris());
            this.container = new CloudBlobContainer(containerURI, this.blobServiceClient);
        }

        return this.container;
    }

    /**
     * Returns a reference to a {@link CloudPageBlob} object that represents a page blob in the directory.
     * 
     * @param blobName
     *            A <code>String</code> that represents the name of the blob.
     * 
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified page blob.
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
     * @return A {@link CloudBlockBlob} object that represents a reference to the specified page blob.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudPageBlob getPageBlobReference(final String blobName, final String snapshotID)
            throws URISyntaxException, StorageException {
        Utility.assertNotNullOrEmpty("blobName", blobName);

        final StorageUri address = PathUtility.appendPathToUri(this.storageUri, blobName,
                this.blobServiceClient.getDirectoryDelimiter());

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
    @Override
    public CloudBlobDirectory getParent() throws URISyntaxException, StorageException {
        if (this.parent == null) {
            final StorageUri parentURI = PathUtility.getParentAddress(this.storageUri,
                    this.blobServiceClient.getDirectoryDelimiter(), this.blobServiceClient.isUsePathStyleUris());

            if (parentURI != null) {
                this.parent = new CloudBlobDirectory(parentURI, null, this.blobServiceClient);
            }
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
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    protected String getPrefix() throws StorageException, URISyntaxException {
        if (this.prefix == null) {
            final String containerUri = this.getContainer().getUri().toString().concat("/");
            this.prefix = Utility.safeRelativize(new URI(containerUri), this.getUri());
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
     * @param directoryName
     *            A <code>String</code> that represents the name of the virtual subdirectory.
     * 
     * @return A <code>CloudBlobDirectory</code> object that represents a virtual blob directory beneath this directory.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    public CloudBlobDirectory getSubDirectoryReference(String directoryName) throws StorageException, URISyntaxException {
        Utility.assertNotNullOrEmpty("itemName", directoryName);

        if (this.blobServiceClient.getDirectoryDelimiter() != null
                && !directoryName.endsWith(this.blobServiceClient.getDirectoryDelimiter())) {
            directoryName = directoryName.concat(this.blobServiceClient.getDirectoryDelimiter());
        }

        final StorageUri address = PathUtility.appendPathToUri(this.storageUri, directoryName,
                this.blobServiceClient.getDirectoryDelimiter());

        return new CloudBlobDirectory(address, this, this.blobServiceClient);
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
     * @return A <code>StorageUri</code> that represents the list of URIs for all locations..
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
            final EnumSet<BlobListingDetails> listingDetails, final int maxResults,
            final ResultContinuation continuationToken, final BlobRequestOptions options,
            final OperationContext opContext) throws StorageException, URISyntaxException {
        prefix = prefix == null ? Constants.EMPTY_STRING : prefix;
        return this.getContainer().listBlobsSegmented(this.getPrefix().concat(prefix), useFlatBlobListing,
                listingDetails, maxResults, continuationToken, options, opContext);
    }
}
