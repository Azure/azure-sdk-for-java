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
 * A wrapper class for the response returned from a Blob Service REST API Set
 * Blob Properties operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#setBlobProperties(String, String, SetBlobPropertiesOptions)}
 * .
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/ee691966.aspx">Set
 * Blob Properties</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class SetBlobPropertiesResult {
    private String etag;
    private Date lastModified;
    private Long sequenceNumber;

    /**
     * Gets the ETag of the blob.
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
     * <strong>ETag</strong> element returned in the response.
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
     * Any operation that modifies the blob, including updates to the blob's
     * metadata or properties, changes the last modified time of the blob. This
     * value can be used in an access condition when updating or deleting a blob
     * to prevent the client from modifying data that has been changed by
     * another client.
     * 
     * @return A {@link java.util.Date} containing the last modified time of the
     *         page blob.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Reserved for internal use. Sets the last modified time of the blob from
     * the <strong>Last-Modified</strong> element returned in the response.
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
     * Gets the sequence number of the page blob. This value can be used when
     * updating or deleting a page blob using an optimistic concurrency model to
     * prevent the client from modifying data that has been changed by another
     * client.
     * 
     * @return A {@link String} containing the client-assigned sequence number
     *         value for the page blob.
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Reserved for internal use. Sets the sequence number of the page blob from
     * the <strong>x-ms-blob-sequence-number</strong> element returned in the
     * response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param sequenceNumber
     *            A {@link String} containing the client-assigned sequence
     *            number value for the page blob.
     */
    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
