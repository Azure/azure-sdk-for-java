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
 * Blob Metadata operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getBlobMetadata(String, String)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getBlobMetadata(String, String, GetBlobMetadataOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179350.aspx">Get
 * Blob Metadata</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class GetBlobMetadataResult {
    private String etag;
    private Date lastModified;
    private HashMap<String, String> metadata;

    /**
     * Gets the ETag of the blob. For block blobs, this value is returned only
     * if the blob has committed blocks.
     * <p>
     * This value can be used in an access condition when updating or deleting a
     * blob to prevent the client from modifying data that has been changed by
     * another client.
     * 
     * @return A {@link String} containing the server-assigned ETag value for
     *         the blob.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Reserved for internal use. Sets the ETag of the blob from the
     * <code>ETag</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param etag
     *            A {@link String} containing the server-assigned ETag value for
     *            the blob.
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Gets the last modified time of the block blob. For block blobs, this
     * value is returned only if the blob has committed blocks.
     * <p>
     * Any operation that modifies the blob, including updates to the blob's
     * metadata or properties, changes the last modified time of the blob. This
     * value can be used in an access condition when updating or deleting a blob
     * to prevent the client from modifying data that has been changed by
     * another client.
     * 
     * @return A {@link java.util.Date} containing the last modified time of the
     *         blob.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Reserved for internal use. Sets the last modified time of the blob from
     * the <code>Last-Modified</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param lastModified
     *            A {@link java.util.Date} containing the last modified time of
     *            the blob.
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets the user-defined blob metadata as a map of name and value pairs. The
     * metadata is for client use and is opaque to the server.
     * 
     * @return A {@link java.util.HashMap} of name-value pairs of {@link String}
     *         containing the names and values of the blob metadata.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Reserved for internal use. Sets the blob metadata from the
     * <code>x-ms-meta-<em>name:value</em></code> headers returned in the
     * response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param metadata
     *            A {@link java.util.HashMap} of name-value pairs of
     *            {@link String} containing the names and values of the blob
     *            metadata.
     */
    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }
}
