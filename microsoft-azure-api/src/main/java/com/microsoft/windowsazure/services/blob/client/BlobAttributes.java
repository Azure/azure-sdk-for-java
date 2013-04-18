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
package com.microsoft.windowsazure.services.blob.client;

import java.net.URI;
import java.util.HashMap;

/**
 * RESERVED FOR INTERNAL USE. Represents a blob's attributes.
 * 
 */
final class BlobAttributes {

    /**
     * Holds the metadata for the blob.
     */
    private HashMap<String, String> metadata;

    /**
     * Represents the state of the most recent or pending copy operation.
     */
    private CopyState copyState;

    /**
     * Holds the properties of the blob.
     */
    private BlobProperties properties;

    /**
     * Holds the snapshot ID
     */
    public String snapshotID;

    /**
     * Holds the URI of the blob. RESERVED for internal use.
     */
    protected URI uri;

     /**
     * Initializes a new instance of the BlobAttributes class. RESERVED FOR INTERNAL USE.
     * 
     * @param type
     *         The type of blob to set.
     */
    public BlobAttributes(final BlobType type) {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new BlobProperties(type));
    }

    /**
     * Gets the metadata for the blob. RESERVED FOR INTERNAL USE.
     * 
     * @return A <code>HashMap</code> object containing the metadata for the blob.
     */
    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Gets the copy state of the blob. RESERVED FOR INTERNAL USE.
     * 
     * @return A <code>CopyState</code> object representing the copy state.
     */
    public CopyState getCopyState() {
        return this.copyState;
    }

    /**
     * Gets the properties for the blob. RESERVED FOR INTERNAL USE.
     * 
     * @return A <code>BlobProperties</code> object that represents the blob properties.
     */
    public BlobProperties getProperties() {
        return this.properties;
    }

    /**
     * Sets the metadata for a blob. RESERVED FOR INTERNAL USE.
     * 
     * @param metadata
     *         The blob meta data to set.
     */
    protected void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the properties for a blob. RESERVED FOR INTERNAL USE.
     * 
     * @param properties
     *         The blob properties to set.
     */
    protected void setProperties(final BlobProperties properties) {
        this.properties = properties;
    }

    /**
     * Sets the copy state for a blob. RESERVED FOR INTERNAL USE.
     * 
     * @param copyState
     *         The blob copy state to set.
     */
    public void setCopyState(final CopyState copyState) {
        this.copyState = copyState;
    }
}
