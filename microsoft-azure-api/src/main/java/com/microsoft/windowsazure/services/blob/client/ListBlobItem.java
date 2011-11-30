package com.microsoft.windowsazure.services.blob.client;

import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Represents an item that may be returned by a blob listing operation.
 */
public interface ListBlobItem {

    /**
     * Returns the container for the blob item.
     * 
     * @return A {@link CloudBlobContainer} object that represents the blob item's container.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    CloudBlobContainer getContainer() throws URISyntaxException, StorageException;

    /**
     * Returns the parent for the blob item.
     * 
     * @return A {@link CloudBlobDirectory} object that represents the blob item's parent.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    CloudBlobDirectory getParent() throws URISyntaxException, StorageException;

    /**
     * Returns the URI for the blob item.
     * 
     * @return A <code>java.net.URI</code> object that represents the blob item's URI.
     */
    URI getUri();
}
