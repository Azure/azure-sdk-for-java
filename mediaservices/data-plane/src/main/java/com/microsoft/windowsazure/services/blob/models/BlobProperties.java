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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.microsoft.windowsazure.core.RFC1123DateAdapter;

/**
 * Represents the HTML properties and system properties that may be set on a
 * blob.
 */
public class BlobProperties {
    private Date lastModified;
    private String etag;
    private String contentType;
    private long contentLength;
    private String contentEncoding;
    private String contentLanguage;
    private String contentMD5;
    private String cacheControl;
    private String blobType;
    private String leaseStatus;
    private long sequenceNumber;

    /**
     * Gets the last modified time of the blob. For block blobs, this value is
     * returned only if the blob has committed blocks.
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
    @XmlElement(name = "Last-Modified")
    @XmlJavaTypeAdapter(RFC1123DateAdapter.class)
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Reserved for internal use. Sets the last modified time of the blob from
     * the <code>Last-Modified</code> header value returned in a server
     * response.
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
    @XmlElement(name = "Etag")
    public String getEtag() {
        return etag;
    }

    /**
     * Reserved for internal use. Sets the ETag value of the blob from the
     * <code>ETag</code> header value returned in a server response.
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
     * Gets the MIME content type of the blob.
     * 
     * @return A {@link String} containing the MIME content type value for the
     *         blob.
     */
    @XmlElement(name = "Content-Type")
    public String getContentType() {
        return contentType;
    }

    /**
     * Reserved for internal use. Sets the MIME content type value for the blob
     * from the <code>Content-Type</code> header value returned in the server
     * response.
     * 
     * @param contentType
     *            A {@link String} containing the MIME content type value for
     *            the blob.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the size of the blob in bytes.
     * 
     * @return The size of the blob in bytes.
     */
    @XmlElement(name = "Content-Length")
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Reserved for internal use. Sets the content length value for the blob
     * from the <code>Content-Length</code> header value returned in the server
     * response.
     * 
     * @param contentLength
     *            The size of the blob in bytes.
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Gets the HTTP content encoding value of the blob.
     * 
     * @return A {@link String} containing the HTTP content encoding value set,
     *         if any.
     */
    @XmlElement(name = "Content-Encoding")
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Reserved for internal use. Sets the HTTP content encoding value for the
     * blob from the <code>Content-Encoding</code> header value returned in the
     * server response.
     * 
     * @param contentEncoding
     *            A {@link String} containing the HTTP content encoding value to
     *            set.
     */
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Gets the HTTP content language value of the blob.
     * 
     * @return A {@link String} containing the HTTP content language value set,
     *         if any.
     */
    @XmlElement(name = "Content-Language")
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Reserved for internal use. Sets the HTTP content language value for the
     * blob from the <code>Content-Language</code> header value returned in the
     * server response.
     * 
     * @param contentLanguage
     *            A {@link String} containing the HTTP content language value to
     *            set.
     */
    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     * Gets the MD5 hash value of the blob content.
     * 
     * @return A {@link String} containing the MD5 hash value of the blob
     *         content.
     */
    @XmlElement(name = "Content-MD5")
    public String getContentMD5() {
        return contentMD5;
    }

    /**
     * Reserved for internal use. Sets the MD5 hash value of the blob content
     * from the <code>Content-MD5</code> header value returned in the server
     * response.
     * 
     * @param contentMD5
     *            A {@link String} containing the MD5 hash value of the blob
     *            content.
     */
    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * Gets the HTTP cache control value of the blob.
     * 
     * @return A {@link String} containing the HTTP cache control value of the
     *         blob.
     */
    @XmlElement(name = "Cache-Control")
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Reserved for internal use. Sets the HTTP cache control value of the blob
     * from the <code>Cache-Control</code> header value returned in the server
     * response.
     * 
     * @param cacheControl
     *            A {@link String} containing the HTTP cache control value of
     *            the blob.
     */
    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Gets a string representing the type of the blob, with a value of
     * "BlockBlob" for block blobs, and "PageBlob" for page blobs.
     * 
     * @return A {@link String} containing "BlockBlob" for block blobs, or
     *         "PageBlob" for page blobs.
     */
    @XmlElement(name = "BlobType")
    public String getBlobType() {
        return blobType;
    }

    /**
     * Reserved for internal use. Sets the blob type from the
     * <code>x-ms-blob-type</code> header value returned in the server response.
     * 
     * @param blobType
     *            A {@link String} containing "BlockBlob" for block blobs, or
     *            "PageBlob" for page blobs.
     */
    public void setBlobType(String blobType) {
        this.blobType = blobType;
    }

    /**
     * Gets a string representing the lease status of the blob, with a value of
     * "locked" for blobs with an active lease, and "unlocked" for blobs without
     * an active lease.
     * 
     * @return A {@link String} containing "locked" for blobs with an active
     *         lease, and "unlocked" for blobs without an active lease.
     */
    @XmlElement(name = "LeaseStatus")
    public String getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * Reserved for internal use. Sets the blob lease status from the
     * <code>x-ms-lease-status</code> header value returned in the server
     * response.
     * 
     * @param leaseStatus
     *            A {@link String} containing "locked" for blobs with an active
     *            lease, and "unlocked" for blobs without an active lease.
     */
    public void setLeaseStatus(String leaseStatus) {
        this.leaseStatus = leaseStatus;
    }

    /**
     * Gets the current sequence number for a page blob. This value is not set
     * for block blobs.
     * 
     * @return The current sequence number of the page blob.
     */
    @XmlElement(name = "x-ms-blob-sequence-number")
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Reserved for internal use. Sets the page blob sequence number from the
     * <code>x-ms-blob-sequence-number</code> header value returned in the
     * server response.
     * 
     * @param sequenceNumber
     *            The current sequence number of the page blob.
     */
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
