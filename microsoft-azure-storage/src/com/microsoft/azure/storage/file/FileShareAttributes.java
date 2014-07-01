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
 * RESERVED FOR INTERNAL USE. Represents a share's attributes, including its properties and metadata.
 */
final class FileShareAttributes {
    /**
     * Holds the share's metadata.
     */
    private HashMap<String, String> metadata;

    /**
     * Holds the share's properties.
     */
    private FileShareProperties properties;

    /**
     * Holds the name of the share.
     */
    private String name;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Initializes a new instance of the FileShareAttributes class
     */
    public FileShareAttributes() {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new FileShareProperties());
    }

    /**
     * Gets the metadata for the share.
     * 
     * @return A <code>java.util.HashMap</code> object containing the metadata for the share.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the name of the share.
     * 
     * @return A <code>String</code> that represents name of the share.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the properties for the share.
     * 
     * @return A <code>FileShareProperties</code> object that represents the share properties.
     */
    public FileShareProperties getProperties() {
        return this.properties;
    }

    /**
     * Gets the list of URIs for all locations for the share.
     * 
     * @return A {@link StorageUri} object that represents the list of URIs for all locations for the share.
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the URI of the share.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI of the share.
     */
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Sets the metadata for a share.
     * 
     * @param metadata
     *            The metadata to set.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the name of the share.
     * 
     * @param name
     *            A <code>String</code> that represents name of the share.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the properties for a share.
     * 
     * @param properties
     *            The share properties to set.
     */
    public void setProperties(final FileShareProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the list of URIs for all locations for the share.
     * 
     * @param storageUri
     *            The list of URIs for all locations for the share.
     */
    protected void setStorageUri(final StorageUri storageUri) {
        this.storageUri = storageUri;
    }
}
