package com.microsoft.windowsazure.services.blob.client;

import java.net.URI;
import java.util.HashMap;

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
    public String snapshotID;

    /**
     * Holds the URI of the blob, Setting this is RESERVED for internal use.
     */
    protected URI uri;

    /**
     * Initializes a new instance of the BlobAttributes class
     */
    public BlobAttributes(final BlobType type) {
        this.setMetadata(new HashMap<String, String>());
        this.setProperties(new BlobProperties(type));
    }

    public HashMap<String, String> getMetadata() {
        return this.metadata;
    }

    public BlobProperties getProperties() {
        return this.properties;
    }

    protected void setMetadata(final HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    protected void setProperties(final BlobProperties properties) {
        this.properties = properties;
    }
}
