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
package com.microsoft.windowsazure.services.blob.models;

import java.util.Date;
import java.util.HashMap;

/**
 * A wrapper class for the response returned from a Blob Service REST API Get
 * Container Properties and Get Container Metadata operations. This is returned
 * by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerProperties(String)},
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerProperties(String, BlobServiceOptions)},
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerMetadata(String)}, and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerMetadata(String, BlobServiceOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179370.aspx">Get
 * Container Properties</a> and <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/ee691976.aspx">Get
 * Container Metadata</a> documentation on MSDN for details of the underlying
 * Blob Service REST API operations.
 */
public class GetContainerPropertiesResult {
    private String etag;
    private Date lastModified;
    private HashMap<String, String> metadata;

    /**
     * Gets the Etag of the container. This value can be used when updating or
     * deleting a container using an optimistic concurrency model to prevent the
     * client from modifying data that has been changed by another client.
     * 
     * @return A {@link String} containing the server-assigned Etag value for
     *         the container.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Reserved for internal use. Sets the Etag of the container from the
     * <code>ETag</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param etag
     *            A {@link String} containing the server-assigned Etag value for
     *            the container.
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Gets the last modifed time of the container. This value can be used when
     * updating or deleting a container using an optimistic concurrency model to
     * prevent the client from modifying data that has been changed by another
     * client.
     * 
     * @return A {@link java.util.Date} containing the last modified time of the
     *         container.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Reserved for internal use. Sets the last modified time of the container
     * from the <code>Last-Modified</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param lastModified
     *            A {@link java.util.Date} containing the last modified time of
     *            the container.
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets the container metadata as a map of name and value pairs. The
     * container metadata is for client use and is opaque to the server.
     * 
     * @return A {@link java.util.HashMap} of key-value pairs of {@link String}
     *         containing the names and values of the container metadata.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Reserved for internal use. Sets the container metadata from the
     * <code>x-ms-meta-<em>name</em></code> headers returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param metadata
     *            A {@link java.util.HashMap} of key-value pairs of
     *            {@link String} containing the names and values of the
     *            container metadata.
     */
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
