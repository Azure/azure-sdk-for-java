package com.microsoft.windowsazure.services.blob.client;

/**
 * Specifies the level of public access that is allowed on the container.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 * 
 */
public enum BlobContainerPublicAccessType {
    /**
     * Specifies blob-level public access. Clients can read the content and metadata of blobs within this container, but
     * cannot read container metadata or list the blobs within the container.
     */
    BLOB,

    /**
     * Specifies container-level public access. Clients can read blob content and metadata and container metadata, and
     * can list the blobs within the container.
     **/
    CONTAINER,

    /**
     * Specifies no public access. Only the account owner can access resources in this container.
     */
    OFF
}
