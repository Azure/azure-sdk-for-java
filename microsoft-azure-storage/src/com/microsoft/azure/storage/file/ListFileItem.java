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
package com.microsoft.azure.storage.file;

import java.net.URI;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageUri;

/**
 * Represents an item that may be returned by a file listing operation.
 */
public interface ListFileItem {
    /**
     * Returns the share for the file item.
     * 
     * @return A {@link CloudFileShare} object which represents the file item's share.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    CloudFileShare getShare() throws URISyntaxException, StorageException;

    /**
     * Returns the parent for the file item.
     * 
     * @return A {@link CloudFileDirectory} object which represents the file item's parent.
     * 
     * @throws StorageException
     *             If a storage service error occurred.
     * @throws URISyntaxException
     *             If the resource URI is invalid.
     */
    CloudFileDirectory getParent() throws URISyntaxException, StorageException;

    /**
     * Returns the URI for the file item.
     * 
     * @return A <code>java.net.URI</code> object which represents the file item's URI.
     */
    URI getUri();

    /**
     * Returns the list of URIs for all storage locations of the file item.
     * 
     * @return A <code>{@link StorageUri}</code> object which represents the file item's URI.
     */
    StorageUri getStorageUri();
}