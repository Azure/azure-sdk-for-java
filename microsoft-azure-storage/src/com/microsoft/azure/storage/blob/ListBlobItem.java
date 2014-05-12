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

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;

/**
 * Represents an item that may be returned by a blob listing operation.
 */
public interface ListBlobItem {

    /**
     * Returns the container for the blob item.
     * 
     * @return A {@link CloudBlobContainer} object which represents the blob item's container.
     * 
     * @throws StorageException
     *         If a storage service error occurred.
     * @throws URISyntaxException
     *         If the resource URI is invalid.
     */
    CloudBlobContainer getContainer() throws URISyntaxException, StorageException;

    /**
     * Returns the parent for the blob item.
     * 
     * @return A {@link CloudBlobDirectory} object which represents the blob item's parent.
     * 
     * @throws StorageException
     *         If a storage service error occurred.
     * @throws URISyntaxException
     *         If the resource URI is invalid.
     */
    CloudBlobDirectory getParent() throws URISyntaxException, StorageException;

    /**
     * Returns the URI for the blob item.
     * 
     * @return A <code>java.net.URI</code> object which represents the blob item's URI.
     */
    URI getUri();

    /**
     * Returns the list of URIs for all storage locations of the blob item.
     * 
     * @return A <code>{@link StorageUri}</code> object which represents the blob item's URI.
     */
    StorageUri getStorageUri();
}
