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
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A wrapper class for the response returned from a Blob Service REST API Get
 * Page Ranges operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#listBlobRegions(String, String, ListBlobRegionsOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/ee691973.aspx">Get
 * Page Ranges</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
@XmlRootElement(name = "PageList")
public class ListBlobRegionsResult {
    private Date lastModified;
    private String etag;
    private long contentLength;
    private List<PageRange> pageRanges;

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
     * Gets the size of the blob in bytes.
     * 
     * @return The size of the blob in bytes.
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Reserved for internal use. Sets the content length of the blob from the
     * <code>x-ms-blob-content-length</code> header returned in the response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param contentLength
     *            The size of the blob in bytes.
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Gets the list of non-overlapping valid page ranges in the blob that match
     * the parameters of the request, sorted by increasing address page range.
     * 
     * @return A {@link List} of {@link PageRange} instances containing the
     *         valid page ranges for the blob.
     */
    @XmlElement(name = "PageRange")
    public List<PageRange> getPageRanges() {
        return pageRanges;
    }

    /**
     * Reserved for internal use. Sets the list of valid page ranges in the blob
     * that match the parameters of the request from the
     * <strong>PageRange</strong> elements of the <strong>PageList</strong>
     * element returned by the server in the response body.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param pageRanges
     *            A {@link List} of {@link PageRange} instances containing the
     *            valid page ranges for the blob.
     */
    public void setPageRanges(List<PageRange> pageRanges) {
        this.pageRanges = pageRanges;
    }
}
