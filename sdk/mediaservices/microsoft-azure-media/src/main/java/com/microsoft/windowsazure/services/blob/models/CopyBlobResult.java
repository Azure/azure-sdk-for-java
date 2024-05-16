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

/**
 * A wrapper class for the response returned from a Blob Service REST API Copy
 * Blob operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#copyBlob(String, String, String, String, CopyBlobOptions)}
 * .
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894037.aspx"
 * >Copy Blob</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class CopyBlobResult {
    private String etag;
    private Date lastModified;

    /**
     * Gets the ETag of the blob.
     * 
     * @return A {@link String} containing the server-assigned ETag value for
     *         the copy blob.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the ETag of the blob from the <code>ETag</code> header returned in
     * the response.
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
     * Gets the last modified time of the blob.
     * <p>
     * 
     * @return A {@link java.util.Date} containing the last modified time of the
     *         blob.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified time of the blob from the
     * <code>Last-Modified</code> header returned in the response.
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
}
