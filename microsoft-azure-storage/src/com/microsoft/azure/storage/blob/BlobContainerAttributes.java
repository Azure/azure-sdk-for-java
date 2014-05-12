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
import java.util.HashMap;

import com.microsoft.azure.storage.StorageUri;

/**
 * RESERVED FOR INTERNAL USE. Represents a container's attributes, including its properties and metadata.
 */
final class BlobContainerAttributes {
    /**
     * Holds the Container Metadata
     */
    private HashMap<String, String> metadata;

    /**
     * Holds the Container Properties
     */
    private BlobContainerProperties properties;

    /**
     * Holds the Name of the Container
     */
    private String name;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Initializes a new instance of the BlobContainerAttributes class
     */
    public BlobContainerAttributes() {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new BlobContainerProperties());
    }

    /**
     * Gets the metadata for the container.
     * 
     * @return A <code>java.util.HashMap</code> object containing the metadata for the container.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the name of the container.
     * 
     * @return A <code>String</code> that represents name of the container.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the properties for the container.
     * 
     * @return A <code>BlobContainerProperties</code> object that represents the container properties.
     */
    public BlobContainerProperties getProperties() {
        return this.properties;
    }

    /**
     * Gets the list of URIs for all locations for the container.
     * 
     * @return A {@link StorageUri} object that represents the list of URIs for all locations for the container.
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the URI of the container.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI of the container.
     */
    public URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Sets the metadata for a container.
     * 
     * @param metadata
     *            The container metadata to set.
     */
    public void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the name of the container.
     * 
     * @param name
     *            A <code>String</code> that represents name of the container.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the properties for a container.
     * 
     * @param properties
     *            The container properties to set.
     */
    public void setProperties(final BlobContainerProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the list of URIs for all locations for the container.
     * 
     * @param storageUri
     *            The list of URIs for all locations for the container.
     */
    protected void setStorageUri(final StorageUri storageUri) {
        this.storageUri = storageUri;
    }
}
