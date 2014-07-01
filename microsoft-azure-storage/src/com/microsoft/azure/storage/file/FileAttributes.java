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
import java.util.HashMap;

import com.microsoft.azure.storage.StorageUri;

/**
 * RESERVED FOR INTERNAL USE. Represents a file's attributes, including its properties and metadata.
 */
final class FileAttributes {
    /**
     * Holds the file's metadata.
     */
    private HashMap<String, String> metadata;

    /**
     * Holds the file's properties.
     */
    private FileProperties properties;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Initializes a new instance of the CloudFileAttributes class
     */
    public FileAttributes() {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new FileProperties());
    }

    /**
     * Gets the metadata for the file.
     * 
     * @return A <code>java.util.HashMap</code> object containing the metadata for the file.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the properties for the file.
     * 
     * @return A <code>FileProperties</code> object that represents the file properties.
     */
    public FileProperties getProperties() {
        return this.properties;
    }

    /**
     * Gets the list of URIs for all locations for the file.
     * 
     * @return A {@link StorageUri} object that represents the list of URIs for all locations for the file.
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the URI of the file.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI of the file.
     */
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Sets the metadata for a file.
     * 
     * @param metadata
     *            The metadata to set.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the properties for a file.
     * 
     * @param properties
     *            The file properties to set.
     */
    public void setProperties(final FileProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the list of URIs for all locations for the file.
     * 
     * @param storageUri
     *            The list of URIs for all locations for the file.
     */
    protected void setStorageUri(final StorageUri storageUri) {
        this.storageUri = storageUri;
    }
}
