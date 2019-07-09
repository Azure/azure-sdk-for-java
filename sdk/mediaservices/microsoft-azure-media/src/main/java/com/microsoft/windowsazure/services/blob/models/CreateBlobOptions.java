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

import java.util.HashMap;

import com.microsoft.windowsazure.core.utils.AccessConditionHeader;

/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createPageBlob(String, String, long, CreateBlobOptions)
 * createPageBlob} or
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlockBlob(String, String, java.io.InputStream, CreateBlobOptions)
 * createBlockBlob} request. These options include an optional server timeout
 * for the operation, the MIME content type and content encoding for the blob,
 * the content language, the MD5 hash, a cache control value, blob metadata, a
 * blob lease ID, a sequence number, and access conditions.
 */
public class CreateBlobOptions extends BlobServiceOptions {
    private String contentType;
    private String contentEncoding;
    private String contentLanguage;
    private String contentMD5;
    private String cacheControl;
    private String blobContentType;
    private String blobContentEncoding;
    private String blobContentLanguage;
    private String blobContentMD5;
    private String blobCacheControl;
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private String leaseId;
    private Long sequenceNumber;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link CreateBlobOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    @Override
    public CreateBlobOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the <code>Content-Type</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the <code>Content-Type</code> header
     *         value set, if any.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the optional <code>Content-Type</code> header value for the blob
     * content. This value will be returned to clients in the headers of the
     * response when the blob data or blob properties are requested. If no
     * content type is specified, the default content type is
     * <strong>application/octet-stream</strong>.
     * 
     * @param contentType
     *            A {@link String} containing the <code>Content-Type</code>
     *            header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the <code>Content-Encoding</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the <code>Content-Encoding</code>
     *         header value set, if any.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Sets the optional <code>Content-Encoding</code> header value for the blob
     * content. Use this value to specify the content encodings applied to the
     * blob. This value will be returned to clients in the headers of the
     * response when the blob data or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param contentEncoding
     *            A {@link String} containing the <code>Content-Encoding</code>
     *            header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * Gets the <code>Content-Language</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the <code>Content-Language</code>
     *         header value set, if any.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Sets the optional <code>Content-Language</code> header value for the blob
     * content. Use this value to specify the content language of the blob. This
     * value will be returned to clients in the headers of the response when the
     * blob data or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param contentLanguage
     *            A {@link String} containing the <code>Content-Language</code>
     *            header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * Gets the <code>Content-MD5</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the <code>Content-MD5</code> header
     *         value set, if any.
     */
    public String getContentMD5() {
        return contentMD5;
    }

    /**
     * Sets the optional <code>Content-MD5</code> header value for the blob
     * content. Use this value to specify an MD5 hash of the blob content. This
     * hash is used to verify the integrity of the blob during transport. When
     * this header is specified, the storage service checks the hash that has
     * arrived with the one that was sent. If the two hashes do not match, the
     * operation will fail with error code 400 (Bad Request). This value will be
     * returned to clients in the headers of the response when the blob data or
     * blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param contentMD5
     *            A {@link String} containing the <code>Content-MD5</code>
     *            header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }

    /**
     * Gets the <code>Cache-Control</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the <code>Cache-Control</code> header
     *         value set, if any.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Sets the optional <code>Cache-Control</code> header value for the blob
     * content. The Blob service stores this value but does not use or modify
     * it. This value will be returned to clients in the headers of the response
     * when the blob data or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param cacheControl
     *            A {@link String} containing the <code>Cache-Control</code>
     *            header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * Gets the <code>x-ms-blob-content-type</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the
     *         <code>x-ms-blob-content-type</code> header value set, if any.
     */
    public String getBlobContentType() {
        return blobContentType;
    }

    /**
     * Sets the optional <code>x-ms-blob-content-type</code> header value for
     * the blob content. This value will be returned to clients in the headers
     * of the response when the blob data or blob properties are requested. If
     * no content type is specified, the default content type is
     * <strong>application/octet-stream</strong>.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param blobContentType
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-type</code> header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setBlobContentType(String blobContentType) {
        this.blobContentType = blobContentType;
        return this;
    }

    /**
     * Gets the <code>x-ms-blob-content-encoding</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the
     *         <code>x-ms-blob-content-encoding</code> header value set, if any.
     */
    public String getBlobContentEncoding() {
        return blobContentEncoding;
    }

    /**
     * Sets the optional <code>x-ms-blob-content-encoding</code> header value
     * for the blob content. Use this value to specify the content encodings
     * applied to the blob. This value will be returned to clients in the
     * headers of the response when the blob data or blob properties are
     * requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param blobContentEncoding
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-encoding</code> header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setBlobContentEncoding(String blobContentEncoding) {
        this.blobContentEncoding = blobContentEncoding;
        return this;
    }

    /**
     * Gets the <code>x-ms-blob-content-language</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the
     *         <code>x-ms-blob-content-language</code> header value set, if any.
     */
    public String getBlobContentLanguage() {
        return blobContentLanguage;
    }

    /**
     * Sets the optional <code>x-ms-blob-content-language</code> header value
     * for the blob content. Use this value to specify the content language of
     * the blob. This value will be returned to clients in the headers of the
     * response when the blob data or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param blobContentLanguage
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-language</code> header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setBlobContentLanguage(String blobContentLanguage) {
        this.blobContentLanguage = blobContentLanguage;
        return this;
    }

    /**
     * Gets the <code>x-ms-blob-content-md5</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the
     *         <code>x-ms-blob-content-md5</code> header value set, if any.
     */
    public String getBlobContentMD5() {
        return blobContentMD5;
    }

    /**
     * Sets the optional MD5 hash value for the blob content. This value will be
     * returned to clients in the <code>x-ms-blob-content-md5</code> header
     * value of the response when the blob data or blob properties are
     * requested. This hash is used to verify the integrity of the blob during
     * transport. When this header is specified, the storage service checks the
     * hash of the content that has arrived with the one that was sent. If the
     * two hashes do not match, the operation will fail with error code 400 (Bad
     * Request), which will cause a ServiceException to be thrown.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param blobContentMD5
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-md5</code> header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setBlobContentMD5(String blobContentMD5) {
        this.blobContentMD5 = blobContentMD5;
        return this;
    }

    /**
     * Gets the <code>x-ms-blob-cache-control</code> header value set in this
     * {@link CreateBlobOptions} instance.
     * 
     * @return A {@link String} containing the
     *         <code>x-ms-blob-cache-control</code> header value set, if any.
     */
    public String getBlobCacheControl() {
        return blobCacheControl;
    }

    /**
     * Sets the optional <code>x-ms-blob-cache-control</code> header value for
     * the blob content. The Blob service stores this value but does not use or
     * modify it. This value will be returned to clients in the headers of the
     * response when the blob data or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param blobCacheControl
     *            A {@link String} containing the
     *            <code>x-ms-blob-cache-control</code> header value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setBlobCacheControl(String blobCacheControl) {
        this.blobCacheControl = blobCacheControl;
        return this;
    }

    /**
     * Gets the blob metadata collection set in this {@link CreateBlobOptions}
     * instance.
     * 
     * @return A {@link HashMap} of name-value pairs of {@link String}
     *         containing the blob metadata set, if any.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the blob metadata collection to associate with the created blob.
     * Metadata is a collection of name-value {@link String} pairs for client
     * use and is opaque to the server. Metadata names must adhere to the naming
     * rules for <a
     * href="http://msdn.microsoft.com/en-us/library/aa664670(VS.71).aspx">C#
     * identifiers</a>.
     * <p>
     * The <em>metadata</em> value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param metadata
     *            A {@link HashMap} of name-value pairs of {@link String}
     *            containing the blob metadata to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds a name-value pair to the blob metadata collection associated with
     * this {@link CreateBlobOptions} instance.
     * 
     * @param key
     *            A {@link String} containing the name portion of the name-value
     *            pair to add to the metadata collection.
     * @param value
     *            A {@link String} containing the value portion of the
     *            name-value pair to add to the metadata collection.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    /**
     * Gets the lease ID for the blob set in this {@link CreateBlobOptions}
     * instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets a lease ID value to match when updating the blob. This value is not
     * used when creating a blob.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the sequence number set in this {@link CreateBlobOptions} instance.
     * 
     * @return The page blob sequence number value set.
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the optional sequence number for a page blob in this
     * {@link CreateBlobOptions} instance. This value is not used for block
     * blobs. The sequence number is a user-controlled value that you can use to
     * track requests. The value of the sequence number must be between 0 and
     * 2^63 - 1. The default value is 0.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobOptions} instance is passed as a parameter.
     * 
     * @param sequenceNumber
     *            The page blob sequence number value to set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link CreateBlobOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions for updating a blob. This value is not used
     * when creating a blob.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link CreateBlobOptions} instance.
     */
    public CreateBlobOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
