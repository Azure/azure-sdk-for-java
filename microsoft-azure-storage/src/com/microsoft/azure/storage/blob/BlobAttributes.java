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
 * RESERVED FOR INTERNAL USE. Represents a blob's attributes.
 */
final class BlobAttributes {

    /**
     * Holds the metadata for the blob.
     */
    private HashMap<String, String> metadata;

    /**
     * Holds the properties of the blob.
     */
    private BlobProperties properties;

    /**
     * Holds the snapshot ID
     */
    private String snapshotID;

    /**
     * Holds the list of URIs for all locations.
     */
    private StorageUri storageUri;

    /**
     * Initializes a new instance of the BlobAttributes class.
     * 
     * @param type
     *            The type of blob to set.
     */
    public BlobAttributes(final BlobType type) {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new BlobProperties(type));
    }

    /**
     * Gets the metadata for the blob.
     * 
     * @return A <code>java.util.HashMap</code> object containing the metadata for the blob.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the properties for the blob.
     * 
     * @return A <code>BlobProperties</code> object that represents the blob properties.
     */
    public BlobProperties getProperties() {
        return this.properties;
    }

    /**
     * Gets the snapshot ID of the blob.
     * 
     * @return A <code>String</code> that represents snapshot ID of the blob.
     */
    public final String getSnapshotID() {
        return this.snapshotID;
    }

    /**
     * Gets the list of URIs for all locations for the blob.
     * 
     * @return A {@link StorageUri} object that represents the list of URIs for all locations for the blob.
     */
    public final StorageUri getStorageUri() {
        return this.storageUri;
    }

    /**
     * Gets the URI of the blob.
     * 
     * @return A <code>java.net.URI</code> object that represents the URI of the blob.
     */
    public final URI getUri() {
        return this.storageUri.getPrimaryUri();
    }

    /**
     * Sets the metadata for a blob.
     * 
     * @param metadata
     *            The blob meta data to set.
     */
    protected void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the properties for a blob.
     * 
     * @param properties
     *            The blob properties to set.
     */
    protected void setProperties(final BlobProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the snapshot ID of the blob.
     * 
     * @param snapshotID
     *            A <code>String</code> that represents snapshot ID of the blob.
     */
    protected final void setSnapshotID(String snapshotID) {
        this.snapshotID = snapshotID;
    }

    /**
     * Sets the list of URIs for all locations for the blob.
     * 
     * @param storageUri
     *            The list of URIs for all locations for the blob.
     */
    protected void setStorageUri(final StorageUri storageUri) {
        this.storageUri = storageUri;
    }
}
