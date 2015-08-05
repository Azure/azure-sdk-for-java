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
 * RESERVED FOR INTERNAL USE. Represents a directory's attributes, including its properties and metadata.
 */
final class FileDirectoryAttributes {
    /**
     * Holds the directory's metadata.
     */
    private HashMap<String, String> metadata;

    /**
     * Holds the directory's properties.
     */
    private FileDirectoryProperties properties;

    /**
     * Holds the name of the directory.
     */
    private String name;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Initializes a new instance of the FileDirectoryAttributes class
     */
    public FileDirectoryAttributes() {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new FileDirectoryProperties());
    }

    /**
     * Gets the metadata for the directory.
     * 
     * @return A <code>java.util.HashMap</code> object containing the metadata for the directory.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the name of the directory.
     * 
     * @return A <code>String</code> that represents name of the directory.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the properties for the directory.
     * 
     * @return A <code>FileDirectoryProperties</code> object that represents the directory properties.
     */
    public FileDirectoryProperties getProperties() {
        return this.properties;
    }

    /**
     * Gets the list of URIs for all locations for the directory.
     * 
     * @return A {@link StorageUri} object that represents the list of URIs for all locations for the directory.
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the URI of the directory.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI of the directory.
     */
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Sets the metadata for a directory.
     * 
     * @param metadata
     *            The metadata to set.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the name of the directory.
     * 
     * @param name
     *            A <code>String</code> that represents name of the directory.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the properties for a directory.
     * 
     * @param properties
     *            The directory properties to set.
     */
    public void setProperties(final FileDirectoryProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the list of URIs for all locations for the directory.
     * 
     * @param storageUri
     *            The list of URIs for all locations for the directory.
     */
    protected void setStorageUri(final StorageUri storageUri) {
        this.storageUri = storageUri;
    }
}